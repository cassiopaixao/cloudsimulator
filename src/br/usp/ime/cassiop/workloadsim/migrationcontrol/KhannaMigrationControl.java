package br.usp.ime.cassiop.workloadsim.migrationcontrol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import br.usp.ime.cassiop.workloadsim.MigrationController;
import br.usp.ime.cassiop.workloadsim.StatisticsModule;
import br.usp.ime.cassiop.workloadsim.VirtualizationManager;
import br.usp.ime.cassiop.workloadsim.exceptions.ServerOverloadedException;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;
import br.usp.ime.cassiop.workloadsim.util.MathUtils;

public class KhannaMigrationControl implements MigrationController {
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

		Map<String, Server> activeServersMap = new HashMap<String, Server>();
		for (Server server : virtualizationManager.getActiveServerList()) {
			activeServersMap.put(server.getName(), server);
		}

		int newVirtualMachines = demand.size();
		VirtualMachine vmInDemand = null;
		VirtualMachine activeVm = null;

		List<String> almostOverloadedServers = new LinkedList<String>();

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

				isInDemand.remove(activeVmName);

				// updatesVm
				// and identify which servers becomes overloaded, or almost
				// overloaded
				try {
					activeVm.getCurrentServer().updateVm(vmInDemand);

					if (activeVm.getCurrentServer().isAlmostOverloaded()) {
						if (!almostOverloadedServers.contains(activeVm
								.getCurrentServer().getName())) {
							almostOverloadedServers.add(activeVm
									.getCurrentServer().getName());
						}
					}

				} catch (ServerOverloadedException ex) {
					if (!almostOverloadedServers.contains(activeVm
							.getCurrentServer().getName())) {
						almostOverloadedServers.add(activeVm.getCurrentServer()
								.getName());
					}
				}
			}
		}

		List<VirtualMachine> vms = null;
		for (String overloadedServerName : almostOverloadedServers) {
			Server overloadedServer = activeServersMap
					.get(overloadedServerName);

			vms = new ArrayList<VirtualMachine>(
					overloadedServer.getVirtualMachines());

			Collections.sort(vms, new Comparator<VirtualMachine>() {

				@Override
				public int compare(VirtualMachine vm0, VirtualMachine vm1) {
					if (vm0.getResourceUtilization() < vm1
							.getResourceUtilization()) {
						return -1;
					} else if (MathUtils.equals(vm0.getResourceUtilization(),
							vm1.getResourceUtilization())) {
						return 0;
					} else {
						return 1;
					}
				}
			});

			for (VirtualMachine smallestVm : vms) {
				if (!overloadedServer.isAlmostOverloaded()) {
					break;
				}

				virtualizationManager.deallocate(smallestVm);

				isInDemand.put(smallestVm.getName(), smallestVm);
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
