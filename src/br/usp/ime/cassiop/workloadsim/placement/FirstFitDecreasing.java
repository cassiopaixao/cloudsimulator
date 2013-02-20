package br.usp.ime.cassiop.workloadsim.placement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.PlacementModule;
import br.usp.ime.cassiop.workloadsim.StatisticsModule;
import br.usp.ime.cassiop.workloadsim.VirtualizationManager;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class FirstFitDecreasing implements PlacementModule {

	private VirtualizationManager virtualizationManager = null;

	private StatisticsModule statisticsModule = null;

	public void setStatisticsModule(StatisticsModule statisticsModule) {
		this.statisticsModule = statisticsModule;
	}

	final Logger logger = LoggerFactory.getLogger(FirstFitDecreasing.class);
	
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

		List<Server> servers = new ArrayList<Server>(
				virtualizationManager.getActiveServerList());
		int vms_not_allocated = 0;

		// demand.sort desc
		Collections.sort(demand);
		Collections.reverse(demand);
		// server.sort asc
		Collections.sort(servers);

		for (VirtualMachine vm : demand) {
			boolean allocated = false;

			for (Server server : servers) {
				if (server.canHost(vm)) {
					virtualizationManager.setVmToServer(vm, server);
					allocated = true;
					break;
				}
			}
			if (!allocated) {
				Server inactiveServer = virtualizationManager
						.getNextInactiveServer(vm);
				if (inactiveServer != null) {
					virtualizationManager.setVmToServer(vm, inactiveServer);
					
					// server.sort asc
					servers.add(inactiveServer);
					Collections.sort(servers);
				} else {
					logger.info(
							"No inactive physical machine provided. Could not consolidate VM {}.",
							vm.toString());
					vms_not_allocated++;
				}
			}

		}
		statisticsModule.addToStatisticValue(
				Constants.STATISTIC_VIRTUAL_MACHINES_NOT_ALLOCATED,
				vms_not_allocated);

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
}
