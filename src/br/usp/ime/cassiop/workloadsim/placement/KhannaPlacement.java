package br.usp.ime.cassiop.workloadsim.placement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.StatisticsModule;
import br.usp.ime.cassiop.workloadsim.VirtualizationManager;
import br.usp.ime.cassiop.workloadsim.exceptions.ServerOverloadedException;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.placement.KhannaTypeChooser.ServerOrderedByResidualCapacityComparator;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class KhannaPlacement implements PlacementWithPowerOffStrategy {

	private VirtualizationManager virtualizationManager = null;

	private StatisticsModule statisticsModule = null;

	private int vms_not_allocated;

	private int servers_turned_off;

	private double lowUtilization = 0;

	public void setLowUtilization(double lowUtilization) {
		this.lowUtilization = lowUtilization;
	}

	public void setStatisticsModule(StatisticsModule statisticsModule) {
		this.statisticsModule = statisticsModule;
	}

	final Logger logger = LoggerFactory.getLogger(KhannaPlacement.class);

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

		vms_not_allocated = 0;
		List<Server> servers = new ArrayList<Server>(
				virtualizationManager.getActiveServersList());

		for (VirtualMachine vm : demand) {
			try {
				if (vm.getCurrentServer() == null) {
					allocate(vm, servers);
				} else {
					try {
						vm.getCurrentServer().updateVm(vm);
						if (vm.getCurrentServer().isAlmostOverloaded()) {
							migrate(vm.getCurrentServer(), servers);
						}
					} catch (ServerOverloadedException ex) {
						migrate(vm.getCurrentServer(), servers);
					}
				}
			} catch (ServerOverloadedException ex) {
			}
		}

		servers_turned_off = PowerOffStrategy.powerOff(servers, lowUtilization,
				this, statisticsModule, virtualizationManager);

		statisticsModule.addToStatisticValue(
				Constants.STATISTIC_VIRTUAL_MACHINES_NOT_ALLOCATED,
				vms_not_allocated);

		statisticsModule.addToStatisticValue(
				Constants.STATISTIC_SERVERS_TURNED_OFF, servers_turned_off);
	}

	private void migrate(Server overloadedServer, List<Server> servers)
			throws Exception {

		while (overloadedServer.isAlmostOverloaded()) {

			VirtualMachine smallestVm = null;
			double smallestVmResourceUtilization = Double.MAX_VALUE;

			for (VirtualMachine vm : overloadedServer.getVirtualMachines()) {
				if (vm.getResourceUtilization() < smallestVmResourceUtilization) {
					smallestVm = vm;
					smallestVmResourceUtilization = smallestVm
							.getResourceUtilization();
				}
			}

			virtualizationManager.deallocate(smallestVm);

			allocate(smallestVm, servers);
		}
	}

	public void allocate(VirtualMachine vm, List<Server> servers)
			throws Exception {
		// sort servers by residual capacity
		Collections.sort(servers,
				new ServerOrderedByResidualCapacityComparator());

		// allocate in the first server with available capacity
		Server targetServer = null;
		for (Server server : servers) {
			if (server.canHost(vm, false)) {
				targetServer = server;
				break;
			}
		}

		if (targetServer != null) {
			virtualizationManager.setVmToServer(vm, targetServer);
		} else {
			Server inactivePm = virtualizationManager.getNextInactiveServer(vm,
					new KhannaTypeChooser());
			if (inactivePm != null) {
				virtualizationManager.setVmToServer(vm, inactivePm);

				servers.add(inactivePm);
			} else {
				logger.info(
						"No inactive physical machine provided. Could not consolidate VM {}.",
						vm.toString());
				vms_not_allocated++;
			}

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

		o = parameters.get(Constants.PARAMETER_RESOURCE_LOW_UTILIZATION);
		if (o instanceof Double) {
			setLowUtilization(((Double) o).doubleValue());
		} else {
			setLowUtilization(0);
			logger.debug("Parameter {} is not set. Using default {}",
					Constants.PARAMETER_RESOURCE_LOW_UTILIZATION, 0);
		}
	}
}
