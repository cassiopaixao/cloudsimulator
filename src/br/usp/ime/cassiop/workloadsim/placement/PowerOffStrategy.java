package br.usp.ime.cassiop.workloadsim.placement;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import br.usp.ime.cassiop.workloadsim.StatisticsModule;
import br.usp.ime.cassiop.workloadsim.VirtualizationManager;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.MathUtils;

public class PowerOffStrategy {

	static int powerOff(List<Server> servers,
			PlacementWithPowerOffStrategy placementStrategy,
			StatisticsModule statisticsModule,
			VirtualizationManager virtualizationManager) throws Exception {
		return powerOff(servers, 0.0, placementStrategy, statisticsModule,
				virtualizationManager);
	}

	static int powerOff(List<Server> servers, double lowUtilization,
			PlacementWithPowerOffStrategy placementStrategy,
			StatisticsModule statisticsModule,
			VirtualizationManager virtualizationManager) throws Exception {
		int servers_turned_off = 0;

		for (Server server : servers) {
			if (MathUtils.lessThanOrEquals(server.getResourceUtilization(),
					lowUtilization)) {
				List<VirtualMachine> shouldMigrate = new LinkedList<VirtualMachine>();
				List<VirtualMachine> vmsOnServer = new ArrayList<VirtualMachine>(
						server.getVirtualMachines());

				for (VirtualMachine vm : vmsOnServer) {
					virtualizationManager.deallocate(vm);
					shouldMigrate.add(vm);
				}

				for (VirtualMachine vm : shouldMigrate) {
					placementStrategy.allocate(vm, servers);
				}

			}
		}

		for (Server server : servers) {
			if (server.getVirtualMachines().isEmpty()) {
				virtualizationManager.turnOffServer(server);
				servers_turned_off++;
			}
		}

		return servers_turned_off;
	}
}
