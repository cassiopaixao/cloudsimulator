package br.usp.ime.cassiop.workloadsim.migrationcontrol;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import br.usp.ime.cassiop.workloadsim.MigrationController;
import br.usp.ime.cassiop.workloadsim.StatisticsModule;
import br.usp.ime.cassiop.workloadsim.VirtualizationManager;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class NoMigrationControl implements MigrationController {
	private VirtualizationManager virtualizationManager = null;

	private StatisticsModule statisticsModule = null;

	public void setVirtualizationManager(
			VirtualizationManager virtualizationManager) {
		this.virtualizationManager = virtualizationManager;
	}

	@Override
	public List<VirtualMachine> control(List<VirtualMachine> demand) {
		Map<String, VirtualMachine> isInDemand = new HashMap<String, VirtualMachine>(
				demand.size());
		for (VirtualMachine vm : demand) {
			isInDemand.put(vm.getName(), vm);
		}

		List<VirtualMachine> shouldDeallocate = new LinkedList<VirtualMachine>();

		int newVirtualMachines = demand.size();

		// for each vm already allocated
		for (Server server : virtualizationManager.getActiveServerList()) {
			for (VirtualMachine vm : server.getVirtualMachines()) {
				shouldDeallocate.add(vm);
				// if it is in the updated demand
				if (isInDemand.containsKey(vm.getName())) {
					newVirtualMachines--;
					// set info about last server
					isInDemand.get(vm.getName()).setLastServer(server);

				} else {
					// put in demand to reallocate
					demand.add(vm);
				}
			}
		}

		for (VirtualMachine vm : shouldDeallocate) {
			try {
				virtualizationManager.deallocate(vm);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}

		statisticsModule.setStatisticValue(
				Constants.STATISTIC_NEW_VIRTUAL_MACHINES, newVirtualMachines);
		statisticsModule.setStatisticValue(
				Constants.STATISTIC_VIRTUAL_MACHINES_TO_REALLOCATE,
				demand.size() - newVirtualMachines);

		virtualizationManager.clear();

		return demand;
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

	public void setStatisticsModule(StatisticsModule statisticsModule) {
		this.statisticsModule = statisticsModule;
	}

}
