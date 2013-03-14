package br.usp.ime.cassiop.workloadsim.placement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.StatisticsModule;
import br.usp.ime.cassiop.workloadsim.VirtualizationManager;
import br.usp.ime.cassiop.workloadsim.exceptions.ServerOverloadedException;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class AlmostWorstFit implements PlacementWithPowerOffStrategy {

	private VirtualizationManager virtualizationManager = null;

	private StatisticsModule statisticsModule = null;

	private List<Server> servers = null;

	private int vms_not_allocated = 0;
	private int servers_turned_off = 0;

	public void setStatisticsModule(StatisticsModule statisticsModule) {
		this.statisticsModule = statisticsModule;
	}

	final Logger logger = LoggerFactory.getLogger(AlmostWorstFit.class);

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

		servers = new ArrayList<Server>(
				virtualizationManager.getActiveServersList());

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

		Server almostWorstFitServer = null;
		double almostWorstFitLeavingResource = -1.0;

		double leavingResource;

		for (Server server : virtualizationManager.getActiveServersList()) {
			if (server.canHost(vm)) {
				// stores the worst-fit allocation
				leavingResource = PlacementUtils.leavingResource(server, vm);

				if (leavingResource > worstFitLeavingResource) {
					almostWorstFitServer = worstFitServer;
					almostWorstFitLeavingResource = worstFitLeavingResource;

					worstFitServer = server;
					worstFitLeavingResource = leavingResource;
				} else if (leavingResource > almostWorstFitLeavingResource) {
					almostWorstFitServer = server;
					almostWorstFitLeavingResource = leavingResource;
				}

			}
		}

		if (almostWorstFitServer != null) {
			virtualizationManager.setVmToServer(vm, almostWorstFitServer);
		} else if (worstFitServer != null) {
			virtualizationManager.setVmToServer(vm, worstFitServer);
		} else {
			Server inactivePm = virtualizationManager.getNextInactiveServer(vm,
					new AlmostWorstFitTypeChooser());
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
}
