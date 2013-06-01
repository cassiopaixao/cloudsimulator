package br.usp.ime.cassiop.workloadsim.placement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
				List<Server> K = fakeVirtMan.getEnvironment()
						.getAvailableMachineTypes();
				List<VirtualMachine> virtualMachines = cloneVMList(demand);

				// 2.1 2.2 2.3
				Collections.sort(virtualMachines, new FFODComparator(k));

				// 2.4
				Server kStar = minimumOportunityCost(virtualMachines.get(0), K);

				double TC = 1; // charge (Fk) of every bin is 1
				Server newServer = fakeVirtMan.activateServerOfType(kStar);
				fakeVirtMan.setVmToServer(virtualMachines.get(0), newServer);

				// 2.5
				for (int i = 1; i < virtualMachines.size(); i++) {
					// 2.5.1
					Server jStar = minimumOportunityCostInActiveServers(
							virtualMachines.get(i),
							fakeVirtMan.getActiveServersList());
					// 2.5.2
					kStar = minimumOportunityCostInInactiveServers(
							virtualMachines.get(i), fakeVirtMan
									.getEnvironment()
									.getAvailableMachineTypes());
					// 2.5.3
					if (jStar == null
							|| MathUtils.greaterThan(
									opportunityCost(virtualMachines.get(i),
											jStar),
									opportunityCost(virtualMachines.get(i),
											kStar))) {
						TC = TC + 1; // charge(Fk) of every bin is 1
						newServer = fakeVirtMan.activateServerOfType(kStar);
						fakeVirtMan.setVmToServer(virtualMachines.get(i),
								newServer);
					} else {
						fakeVirtMan
								.setVmToServer(virtualMachines.get(i), jStar);
					}
				}

				// 2.6
				if (TC < zub) {
					zub = TC;
					virtManWithLowestZub = fakeVirtMan;
				}
			}

			if (virtManWithLowestZub != null) {
				//copiar alocação para oficial
				virtualizationManager.copyAllocationStatus(virtManWithLowestZub, demand);	
			} else {
				throw new Exception("Ferrou!");
			}
			// step 3
			// stop.
		} catch (UnknownVirtualMachineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoMoreServersAvailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
		double P = server.getLoadPercentage(ResourceType.CPU);
		double Q = server.getLoadPercentage(ResourceType.MEMORY);

		double p = vm.getDemand(ResourceType.CPU)
				/ server.getCapacity(ResourceType.CPU);
		double q = vm.getDemand(ResourceType.MEMORY)
				/ server.getCapacity(ResourceType.MEMORY);

		double factor;
		if (P > Q) {
			factor = Math.max(p, q - (P - Q));
		} else {
			factor = Math.max(q, p - (Q - P));
		}
		return factor;
	}

	private Server minimumOportunityCostInInactiveServers(
			VirtualMachine virtualMachine, List<Server> availableMachineTypes) {
		List<Server> eligibleServers = new LinkedList<Server>();
		for (Server server : availableMachineTypes) {
			if (MathUtils.greaterThanOrEquals(1,
					virtualMachine.getDemand(ResourceType.CPU))
					&& MathUtils.greaterThanOrEquals(1,
							virtualMachine.getDemand(ResourceType.MEMORY))) {
				eligibleServers.add(server);
			}
		}
		return minimumOportunityCost(virtualMachine, eligibleServers);
	}

	private Server minimumOportunityCostInActiveServers(
			VirtualMachine virtualMachine, Collection<Server> activeServersList) {
		List<Server> eligibleServers = new LinkedList<Server>();
		for (Server server : activeServersList) {
			if (MathUtils.greaterThanOrEquals(
					1 - server.getLoadPercentage(ResourceType.CPU),
					virtualMachine.getDemand(ResourceType.CPU))
					&& MathUtils.greaterThanOrEquals(
							1 - server.getLoadPercentage(ResourceType.MEMORY),
							virtualMachine.getDemand(ResourceType.MEMORY))) {
				eligibleServers.add(server);
			}
		}
		return minimumOportunityCost(virtualMachine, eligibleServers);
	}

	private Server minimumOportunityCost(VirtualMachine vm, List<Server> servers) {
		double min = Double.MAX_VALUE;
		Server minOCServer = null;

		for (Server server : servers) {
			double factor = opportunityCost(vm, server);

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
			destinationServer = placementUtils.lessLossEmptyServer(servers, vm);
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
		private double Pk, Qk;

		public FFODComparator(Server serverType) {
			super();
			this.Pk = serverType.getCapacity(ResourceType.CPU);
			this.Qk = serverType.getCapacity(ResourceType.MEMORY);
		}

		@Override
		public int compare(VirtualMachine o1, VirtualMachine o2) {
			double o1p = o1.getDemand(ResourceType.CPU) / Pk;
			double o1q = o1.getDemand(ResourceType.MEMORY) / Qk;

			double o2p = o2.getDemand(ResourceType.CPU) / Pk;
			double o2q = o2.getDemand(ResourceType.MEMORY) / Qk;

			double o1balanced = Math.abs(o1p - o1q) / (o1p + o1q);
			double o2balanced = Math.abs(o2p - o2q) / (o2p + o2q);

			if (MathUtils.lessThan(o1balanced, o2balanced)) {
				return -1;
			} else if (MathUtils.equals(o1balanced, o2balanced)) {
				return 0;
			} else {
				return 1;
			}
		}

	}
}
