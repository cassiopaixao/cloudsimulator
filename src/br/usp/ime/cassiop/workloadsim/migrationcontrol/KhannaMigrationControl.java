package br.usp.ime.cassiop.workloadsim.migrationcontrol;

import java.util.ArrayList;
import java.util.Collection;
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

		Collection<Server> activeServers = virtualizationManager
				.getActiveServersList();

		int virtualMachinesToReallocate = 0;
		
		List<Server> almostOverloadedServers = new LinkedList<Server>();

		// for each vm already allocated
		for (Server server : activeServers) {
			for (VirtualMachine vm : server.getVirtualMachines()) {
				// updates demands
				try {
					server.updateVm(isInDemand.get(vm.getName()));
				} catch (ServerOverloadedException ex) {
					// will be correctly handled
				}
			}
			// checks if the server is almost overloaded
			if (server.isAlmostOverloaded()) {
				almostOverloadedServers.add(server);
			}
		}

		List<VirtualMachine> vms = null;
		for (Server overloadedServer : almostOverloadedServers) {
			vms = new ArrayList<VirtualMachine>(
					overloadedServer.getVirtualMachines());

			// orders virtual machines ascendently by resource utilization
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
					// when the server isn't overloaded anymore, the VMs left
					// shouln't be reallocated.
					for (VirtualMachine vm : overloadedServer
							.getVirtualMachines()) {
						isInDemand.remove(vm.getName());
					}
				}
				virtualizationManager.deallocate(smallestVm);
				virtualMachinesToReallocate++;
			}
		}

		statisticsModule.setStatisticValue(
				Constants.STATISTIC_VIRTUAL_MACHINES_TO_REALLOCATE,
				virtualMachinesToReallocate);

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
