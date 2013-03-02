package br.usp.ime.cassiop.workloadsim.placement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.PhysicalMachineTypeChooser;
import br.usp.ime.cassiop.workloadsim.StatisticsModule;
import br.usp.ime.cassiop.workloadsim.VirtualizationManager;
import br.usp.ime.cassiop.workloadsim.exceptions.ServerOverloadedException;
import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class WorstFitDecreasing implements PlacementWithPowerOffStrategy {

	private VirtualizationManager virtualizationManager = null;

	private StatisticsModule statisticsModule = null;

	private List<Server> servers = null;

	private int vms_not_allocated = 0;
	private int servers_turned_off = 0;

	public void setStatisticsModule(StatisticsModule statisticsModule) {
		this.statisticsModule = statisticsModule;
	}

	final Logger logger = LoggerFactory.getLogger(WorstFitDecreasing.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.ime.usp.cassiop.workloadsim.PlacementModule#setVirtualizationManager
	 * (br.ime.usp.cassiop.workloadsim.VirtualizationManager)
	 */
	@Override
	public void setVirtualizationManager(
			VirtualizationManager virtualizationManager) {
		this.virtualizationManager = virtualizationManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.ime.usp.cassiop.workloadsim.PlacementModule#consolidateAll()
	 */
	@Override
	public void consolidateAll(List<VirtualMachine> demand) throws Exception {
		if (virtualizationManager == null) {
			throw new Exception("VirtualizationManager is not set.");
		}
		if (demand == null) {
			throw new Exception("Demand is not set.");
		}

		// demand.sort desc
		Collections.sort(demand);
		Collections.reverse(demand);

		servers = new ArrayList<Server>(
				virtualizationManager.getActiveServerList());

		vms_not_allocated = 0;

		for (VirtualMachine vm : demand) {
			try {
				allocate(vm, servers);
			} catch (ServerOverloadedException ex) {
			}

		}

		servers_turned_off = PowerOffStrategy.powerOff(servers, this,
				statisticsModule, virtualizationManager);

		statisticsModule.addToStatisticValue(
				Constants.STATISTIC_VIRTUAL_MACHINES_NOT_ALLOCATED,
				vms_not_allocated);

		statisticsModule.addToStatisticValue(
				Constants.STATISTIC_SERVERS_TURNED_OFF, servers_turned_off);

	}

	private double leavingResource(Server pm, VirtualMachine vm) {
		double leavingCpu, leavingMem;
		leavingCpu = pm.getFreeResource(ResourceType.CPU)
				- vm.getDemand(ResourceType.CPU);
		leavingMem = pm.getFreeResource(ResourceType.MEMORY)
				- vm.getDemand(ResourceType.MEMORY);

		return leavingCpu * leavingMem;
	}

	public class WorstFitTypeChooser implements PhysicalMachineTypeChooser {

		public Server chooseServerType(List<Server> machineTypes,
				VirtualMachine vmDemand) {
			Server selectedMachine = null;
			double leavingResource = 0.0;

			Collections.sort(machineTypes);

			for (Server server : machineTypes) {
				if (server.canHost(vmDemand)) {
					if (leavingResource(server, vmDemand) > leavingResource) {
						selectedMachine = server;
						leavingResource = leavingResource(server, vmDemand);
					}
				}
			}

			if (selectedMachine == null) {
				logger.info("No inactive physical machine can satisfy the virtual machine's demand. Activating the physical machine with lowest loss of performance.");

				Server lessLossOfPerformanceMachine = null;
				double lessLossOfPerformance = Double.MAX_VALUE;

				for (Server pm : machineTypes) {
					if (!pm.canHost(vmDemand)) {
						if (lossOfPerformance(pm, vmDemand) < lessLossOfPerformance) {
							lessLossOfPerformance = lossOfPerformance(pm,
									vmDemand);
							lessLossOfPerformanceMachine = pm;
						}
					}
				}

				if (lessLossOfPerformanceMachine == null) {
					logger.info("There is no inactive physical machine. Need to overload one.");
					return null;
				}

				selectedMachine = lessLossOfPerformanceMachine;
			}
			return selectedMachine;
		}

		private double lossOfPerformance(Server pm, VirtualMachine vm) {
			double leavingCpu, leavingMem;
			double sum = 0;
			leavingCpu = pm.getFreeResource(ResourceType.CPU)
					- vm.getDemand(ResourceType.CPU);
			leavingMem = pm.getFreeResource(ResourceType.MEMORY)
					- vm.getDemand(ResourceType.MEMORY);

			sum += (leavingCpu < 0) ? -leavingCpu : 0;
			sum += (leavingMem < 0) ? -leavingMem : 0;

			return sum;
		}

	}

	@Override
	public void setParameters(Map<String, Object> parameters) throws Exception {
		Object o = parameters.get(Constants.PARAMETER_VIRTUALIZATION_MANAGER);
		if (o instanceof VirtualizationManager) {
			setVirtualizationManager((VirtualizationManager) o);
		} else {
			throw new Exception(String.format("Invalid parameter: %s",
					Constants.PARAMETER_VIRTUALIZATION_MANAGER));
		}

		o = parameters.get(Constants.PARAMETER_STATISTICS_MODULE);
		if (o instanceof StatisticsModule) {
			setStatisticsModule((StatisticsModule) o);
		} else {
			throw new Exception(String.format("Invalid parameter: %s",
					Constants.PARAMETER_STATISTICS_MODULE));
		}
	}

	@Override
	public void allocate(VirtualMachine vm, List<Server> servers)
			throws Exception {

		Server worstFitServer = null;
		double worstFitLeavingResource = -1.0;

		for (Server pm : virtualizationManager.getActiveServerList()) {
			if (pm.canHost(vm)) {
				// stores the worst-fit allocation
				if (leavingResource(pm, vm) > worstFitLeavingResource) {
					worstFitServer = pm;
					worstFitLeavingResource = leavingResource(pm, vm);
				}

			}
		}

		if (worstFitServer != null) {
			virtualizationManager.setVmToServer(vm, worstFitServer);
		} else {
			Server inactivePm = virtualizationManager.getNextInactiveServer(vm,
					new WorstFitTypeChooser());
			if (inactivePm != null) {
				try {
					virtualizationManager.setVmToServer(vm, inactivePm);
				} catch (ServerOverloadedException ex) {
				}

				servers.add(inactivePm);
			} else {
				logger.info(
						"No inactive physical machine provided. Could not consolidate VM {}.",
						vm.toString());
				vms_not_allocated++;
			}
		}

	}
}
