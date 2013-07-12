package br.usp.ime.cassiop.workloadsim.placement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.PlacementModule;
import br.usp.ime.cassiop.workloadsim.StatisticsModule;
import br.usp.ime.cassiop.workloadsim.VirtualizationManager;
import br.usp.ime.cassiop.workloadsim.VirtualizationManagerImpl;
import br.usp.ime.cassiop.workloadsim.exceptions.DependencyNotSetException;
import br.usp.ime.cassiop.workloadsim.exceptions.InvalidParameterException;
import br.usp.ime.cassiop.workloadsim.exceptions.NoMoreServersAvailableException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownServerException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownVirtualMachineException;
import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;
import br.usp.ime.cassiop.workloadsim.util.MathUtils;

public class FirstFitByOrderedDeviation extends PlacementModule {

	final Logger logger = LoggerFactory
			.getLogger(FirstFitByOrderedDeviation.class);

	// HashMap<VM.name, HashMap<Server.type, Double>>
	private HashMap<String, HashMap<String, Double>> cpuRelDem = null;
	private HashMap<String, HashMap<String, Double>> memRelDem = null;

	public void setParameters(Map<String, Object> parameters)
			throws InvalidParameterException {
		Object o = null;

		o = parameters.get(Constants.PARAMETER_VIRTUALIZATION_MANAGER);
		if (o instanceof VirtualizationManager) {
			setVirtualizationManager((VirtualizationManager) o);
		} else {
			throw new InvalidParameterException(
					Constants.PARAMETER_VIRTUALIZATION_MANAGER,
					VirtualizationManager.class);
		}

		o = parameters.get(Constants.PARAMETER_STATISTICS_MODULE);
		if (o instanceof StatisticsModule) {
			setStatisticsModule((StatisticsModule) o);
		} else {
			throw new InvalidParameterException(
					Constants.PARAMETER_STATISTICS_MODULE,
					StatisticsModule.class);
		}

		o = parameters.get(Constants.PARAMETER_PLACEMENT_UTILS);
		if (o instanceof PlacementUtils) {
			setPlacementUtils((PlacementUtils) o);
		} else {
			throw new InvalidParameterException(
					Constants.PARAMETER_PLACEMENT_UTILS, PlacementUtils.class);
		}
	}

	protected void verifyDependencies(List<VirtualMachine> demand)
			throws DependencyNotSetException {
		if (virtualizationManager == null) {
			throw new DependencyNotSetException(
					"VirtualizationManager is not set.");
		}
		if (statisticsModule == null) {
			throw new DependencyNotSetException("StatisticsModule is not set.");
		}
		if (placementUtils == null) {
			throw new DependencyNotSetException("PlacementUtils is not set.");
		}
		if (demand == null) {
			throw new DependencyNotSetException("Demand is not set.");
		}
	}

	public void consolidateAll(List<VirtualMachine> demand)
			throws DependencyNotSetException {
		verifyDependencies(demand);

		try {

			// K = available machine types
			List<Server> globalK = virtualizationManager.getEnvironment()
					.getAvailableMachineTypes();

			fillRelativeDemmands(demand);

			VirtualizationManager virtManWithLowestZub = null;

			// step 1
			// upper bound
			double zub = Double.MAX_VALUE;

			// step 2
			for (Server k : globalK) {
				// cloudsimulator
				VirtualizationManager fakeVirtMan = new VirtualizationManagerImpl();
				fakeVirtMan.setEnvironment(virtualizationManager
						.getEnvironment().clone());
				// List<Server> K = fakeVirtMan.getEnvironment()
				// .getAvailableMachineTypes();
				List<VirtualMachine> virtualMachines = cloneVMList(demand);
				fakeVirtMan.copyAllocationStatus(virtualizationManager,
						virtualMachines);

				// 2.1 2.2 2.3
				Collections.sort(virtualMachines, new FFODComparator(k,
						virtualMachines));

				// 2.4 (modified. No special first case)

				Server kStar = null;
				Server jStar = null;
				Server newServer = null;

				// total cost
				double TC = 0; // charge (Fk) of every bin is 1
				// 2.5
				for (VirtualMachine vm : virtualMachines) {
					// 2.5.1
					jStar = minimumOportunityCostInActiveServers(vm,
							fakeVirtMan.getActiveServersList());
					// 2.5.2
					kStar = minimumOportunityCostInInactiveServers(vm,
							fakeVirtMan.getEnvironment()
									.getAvailableMachineTypes());

					// 2.5.3
					if (jStar == null && kStar == null) {
						logger.debug(
								"No server could allocate the virtual machine: {}.",
								vm.toString());

						StringBuffer sb = new StringBuffer();
						sb.append("VM not allocated").append("\t");
						sb.append(vm.toString()).append("\t");
						for (Server s : fakeVirtMan.getEnvironment()
								.getAvailableMachineTypes()) {
							sb.append(s.toString()).append("\t");
						}

						logger.info(sb.substring(0, sb.length() - 1));

						statisticsModule
								.addToStatisticValue(
										Constants.STATISTIC_VIRTUAL_MACHINES_NOT_ALLOCATED,
										1);
					} else if (jStar == null
							|| MathUtils.greaterThan(
									opportunityCost(vm, jStar),
									opportunityCost(vm, kStar))) {
						TC = TC + 1; // charge(Fk) of every bin is 1
						newServer = fakeVirtMan.activateServerOfType(kStar);
						if (!newServer.canHost(vm)) {
							if (!newServer.canHost(vm)) {
								StringBuffer sb = new StringBuffer();
								sb.append(
										"VM allocated, but new server can't host")
										.append("\t");
								sb.append(vm.toString()).append("\t");
								sb.append(newServer.toString());
								logger.info(sb.toString());
							}
						}
						fakeVirtMan.setVmToServer(vm, newServer);
					} else {
						if (!jStar.canHost(vm)) {
							StringBuffer sb = new StringBuffer();
							sb.append("VM allocated, but server can't host")
									.append("\t");
							sb.append(vm.toString()).append("\t");
							sb.append(jStar.toString());
							logger.info(sb.toString());
						}
						fakeVirtMan.setVmToServer(vm, jStar);
					}
				}

				// 2.6
				if (TC < zub) {
					zub = TC;
					virtManWithLowestZub = fakeVirtMan;
				}
			}

			if (virtManWithLowestZub != null) {
				// copiar alocação para oficial
				virtualizationManager.copyAllocationStatus(
						virtManWithLowestZub, demand);
			} else {
				throw new Exception("Ferrou!");
			}
			// step 3
			// stop.
		} catch (UnknownVirtualMachineException e) {
			// TODO Auto-generated catch block --- tratar exceções
			e.printStackTrace();
		} catch (UnknownServerException e) {
			e.printStackTrace();
		} catch (NoMoreServersAvailableException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<VirtualMachine> cloneVMList(List<VirtualMachine> demand) {
		List<VirtualMachine> clonedList = new ArrayList<VirtualMachine>(
				demand.size());

		for (VirtualMachine vm : demand) {
			clonedList.add(vm.clone());
		}

		return clonedList;
	}

	private double opportunityCost(VirtualMachine vm, Server server) {
		double P, Q, p, q;
		P = server.getLoadPercentage(ResourceType.CPU);
		Q = server.getLoadPercentage(ResourceType.MEMORY);
		p = cpuRelDem.get(vm.getName()).get(server.getType()).doubleValue();
		q = memRelDem.get(vm.getName()).get(server.getType()).doubleValue();

		if (P > Q) {
			return Math.max(p, q - (P - Q));
		} else {
			return Math.max(q, p - (Q - P));
		}
	}

	private Server minimumOportunityCostInInactiveServers(
			VirtualMachine virtualMachine, List<Server> availableMachineTypes) {
		List<Server> eligibleServers = new LinkedList<Server>();
		HashMap<String, Double> vmRelativeCpu = cpuRelDem.get(virtualMachine
				.getName());
		HashMap<String, Double> vmRelativeMem = memRelDem.get(virtualMachine
				.getName());

		for (Server server : availableMachineTypes) {
			// FIXME cpuRelativeDemmmand é só com dados do servidor atual
			// FIXED?
			if ((1 >= vmRelativeCpu.get(server.getType()).doubleValue())
					&& (1 >= vmRelativeMem.get(server.getType()).doubleValue())) {
				eligibleServers.add(server);
			}
		}
		return minimumOportunityCost(virtualMachine, eligibleServers);
	}

	/*
	 * TODO Consuming a lot of CPU... can optimize?
	 * 
	 * refactored: 2013_07_09... does it keep consuming a lot? refactored:
	 * 2013_07_11... calculation fixed
	 */
	private Server minimumOportunityCostInActiveServers(
			VirtualMachine virtualMachine, Collection<Server> activeServersList) {
		List<Server> eligibleServers = new LinkedList<Server>();
		HashMap<String, Double> vmRelativeCpu = cpuRelDem.get(virtualMachine
				.getName());
		HashMap<String, Double> vmRelativeMem = memRelDem.get(virtualMachine
				.getName());

		for (Server server : activeServersList) {
			if ((1 - server.getLoadPercentage(ResourceType.CPU) >= vmRelativeCpu
					.get(server.getType()).doubleValue())
					&& (1 - server.getLoadPercentage(ResourceType.MEMORY) >= vmRelativeMem
							.get(server.getType()).doubleValue())) {
				eligibleServers.add(server);
			}
		}
		return minimumOportunityCost(virtualMachine, eligibleServers);
	}

	private Server minimumOportunityCost(VirtualMachine vm, List<Server> servers) {
		double factor;
		double min = Double.MAX_VALUE;
		Server minOCServer = null;

		for (Server server : servers) {
			factor = opportunityCost(vm, server);

			if (factor < min) {
				min = 1 * factor; // Ftj = 1, 'cause every bin has a fixed
									// charge
				minOCServer = server;
			}
		}

		return minOCServer;
	}

	public void allocate(VirtualMachine vm, List<Server> servers)
			throws UnknownVirtualMachineException, UnknownServerException {

		Server destinationServer = placementStrategy.selectDestinationServer(
				vm, servers);

		if (destinationServer == null) {
			try {
				destinationServer = virtualizationManager
						.getNextInactiveServer(vm, placementStrategy);

				if (destinationServer != null) {
					servers.add(destinationServer);
				}
			} catch (NoMoreServersAvailableException e) {
			}
		}

		if (destinationServer == null) {
			logger.debug("No server could allocate the virtual machine: {}.",
					vm.toString());

			statisticsModule.addToStatisticValue(
					Constants.STATISTIC_VIRTUAL_MACHINES_NOT_ALLOCATED, 1);

		}

		if (destinationServer != null) {
			virtualizationManager.setVmToServer(vm, destinationServer);
		}
	}

	public static class FFODComparator implements Comparator<VirtualMachine> {
		private HashMap<VirtualMachine, Double> values = null;

		public FFODComparator(Server serverType, List<VirtualMachine> vmList) {
			super();
			double Pk, Qk;
			Pk = serverType.getCapacity(ResourceType.CPU);
			Qk = serverType.getCapacity(ResourceType.MEMORY);

			this.values = new HashMap<VirtualMachine, Double>();

			for (VirtualMachine vm : vmList) {
				double pk, qk;
				pk = vm.getDemand(ResourceType.CPU) / Pk;
				qk = vm.getDemand(ResourceType.MEMORY) / Qk;

				values.put(vm, new Double(Math.abs(pk - qk) / (pk + qk)));
			}
		}

		@Override
		public int compare(VirtualMachine o1, VirtualMachine o2) {
			return values.get(o1).compareTo(values.get(o2));
		}

	}

	private void fillRelativeDemmands(List<VirtualMachine> vmList) {
		cpuRelDem = new HashMap<String, HashMap<String, Double>>();
		memRelDem = new HashMap<String, HashMap<String, Double>>();

		HashMap<String, Double> cpuValues, memValues;
		for (VirtualMachine vm : vmList) {
			cpuValues = new HashMap<String, Double>();
			memValues = new HashMap<String, Double>();

			double cpuVm = vm.getDemand(ResourceType.CPU);
			double memVm = vm.getDemand(ResourceType.MEMORY);

			for (Server s : virtualizationManager.getEnvironment()
					.getAllMachineTypes()) {
				cpuValues.put(s.getType(),
						new Double(cpuVm / s.getCapacity(ResourceType.CPU)));
				memValues.put(s.getType(),
						new Double(memVm / s.getCapacity(ResourceType.MEMORY)));
			}

			cpuRelDem.put(vm.getName(), cpuValues);
			memRelDem.put(vm.getName(), memValues);
		}
	}
}
