package br.usp.ime.cassiop.workloadsim.migrationcontrol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import br.usp.ime.cassiop.workloadsim.MigrationController;
import br.usp.ime.cassiop.workloadsim.StatisticsModule;
import br.usp.ime.cassiop.workloadsim.VirtualizationManager;
import br.usp.ime.cassiop.workloadsim.exceptions.ServerOverloadedException;
import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;
import br.usp.ime.cassiop.workloadsim.util.MathUtils;

public class MigrateIfChangeAndServerBecomesOverloaded implements
		MigrationController {
	private VirtualizationManager virtualizationManager = null;

	private StatisticsModule statisticsModule = null;

	public void setVirtualizationManager(
			VirtualizationManager virtualizationManager) {
		this.virtualizationManager = virtualizationManager;
	}

	@Override
	public List<VirtualMachine> control(List<VirtualMachine> demand)
			throws Exception {
		Map<String, VirtualMachine> isInDemand = new HashMap<String, VirtualMachine>(
				demand.size());
		for (VirtualMachine vm : demand) {
			isInDemand.put(vm.getName(), vm);
		}

		int newVirtualMachines = demand.size();
		VirtualMachine vmInDemand = null;
		VirtualMachine activeVm = null;

		List<String> shouldNotReallocate = new LinkedList<String>();

		Map<String, VirtualMachine> activeVmsMap = virtualizationManager
				.getActiveVirtualMachines();

		// for each vm already allocated
		List<String> activeVms = new ArrayList<String>(activeVmsMap.keySet());
		for (String activeVmName : activeVms) {
			if (isInDemand.containsKey(activeVmName)) {
				newVirtualMachines--;
				vmInDemand = isInDemand.get(activeVmName);
				activeVm = activeVmsMap.get(activeVmName);

				// check if the demand is bigger and if server becomes
				// overloaded
				if (demandIsBigger(activeVm, vmInDemand)
						&& serverBecomesOverloaded(activeVm.getCurrentServer(),
								activeVm, vmInDemand)) {
					// reallocate
					vmInDemand.setLastServer(activeVm.getCurrentServer());
					virtualizationManager.deallocate(activeVm);
				} else {
					try {
						// update info
						activeVm.getCurrentServer().updateVm(vmInDemand);
						// do not reallocate
						shouldNotReallocate.add(vmInDemand.getName());

					} catch (ServerOverloadedException ex) {
						// reallocate
						vmInDemand.setLastServer(activeVm.getCurrentServer());
						virtualizationManager.deallocate(activeVm);
					}
				}
			}
		}

		for (String vmName : shouldNotReallocate) {
			isInDemand.remove(vmName);
		}

		statisticsModule.setStatisticValue(
				Constants.STATISTIC_NEW_VIRTUAL_MACHINES, newVirtualMachines);
		statisticsModule.setStatisticValue(
				Constants.STATISTIC_VIRTUAL_MACHINES_TO_REALLOCATE,
				isInDemand.size() - newVirtualMachines);

		return new ArrayList<VirtualMachine>(isInDemand.values());
	}

	private boolean serverBecomesOverloaded(Server server, VirtualMachine vm,
			VirtualMachine vmInDemand) {
		if (server.getFreeResource(ResourceType.CPU)
				+ vm.getDemand(ResourceType.CPU)
				- vmInDemand.getDemand(ResourceType.CPU) < 0) {
			return true;
		}
		if (server.getFreeResource(ResourceType.MEMORY)
				+ vm.getDemand(ResourceType.MEMORY)
				- vmInDemand.getDemand(ResourceType.MEMORY) < 0) {
			return true;
		}
		return false;
	}

	private boolean demandIsBigger(VirtualMachine vm, VirtualMachine newVm) {
		if (!MathUtils.lessThan(vm.getDemand(ResourceType.CPU),
				newVm.getDemand(ResourceType.CPU))) {
			return true;
		}
		if (!MathUtils.lessThan(vm.getDemand(ResourceType.MEMORY),
				newVm.getDemand(ResourceType.MEMORY))) {
			return true;
		}
		return false;
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
