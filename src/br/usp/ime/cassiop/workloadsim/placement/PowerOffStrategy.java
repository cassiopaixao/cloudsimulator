package br.usp.ime.cassiop.workloadsim.placement;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.StatisticsModule;
import br.usp.ime.cassiop.workloadsim.VirtualizationManager;
import br.usp.ime.cassiop.workloadsim.exceptions.ServerNotEmptyException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownServerException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownVirtualMachineException;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.MathUtils;

public class PowerOffStrategy {

	static final Logger logger = LoggerFactory
			.getLogger(PowerOffStrategy.class);

	static int powerOff(List<Server> servers,
			PlacementWithPowerOffStrategy placementStrategy,
			StatisticsModule statisticsModule,
			VirtualizationManager virtualizationManager)
			throws UnknownServerException {
		return powerOff(servers, 0.0, placementStrategy, statisticsModule,
				virtualizationManager);
	}

	static int powerOff(List<Server> servers, double lowUtilization,
			PlacementWithPowerOffStrategy placementStrategy,
			StatisticsModule statisticsModule,
			VirtualizationManager virtualizationManager)
			throws UnknownServerException {
		int servers_turned_off = 0;

		for (Server server : servers) {
			if (MathUtils.lessThanOrEquals(server.getResourceUtilization(),
					lowUtilization)) {
				List<VirtualMachine> shouldMigrate = new LinkedList<VirtualMachine>();
				List<VirtualMachine> vmsOnServer = new ArrayList<VirtualMachine>(
						server.getVirtualMachines());

				for (VirtualMachine vm : vmsOnServer) {
					try {
						virtualizationManager.deallocate(vm);
						shouldMigrate.add(vm);
					} catch (UnknownVirtualMachineException e) {
						logger.error(
								"Tryed to deallocate an unknown virtual machine: {}",
								vm);
					}
				}

				for (VirtualMachine vm : shouldMigrate) {
					try {
						placementStrategy.allocate(vm, servers);
					} catch (UnknownVirtualMachineException e) {
						logger.error(
								"Tryed to allocate an unknown virtual machine: {}",
								vm);
					}
				}

			}
		}

		for (Server server : servers) {
			if (server.getVirtualMachines().isEmpty()) {
				try {
					virtualizationManager.turnOffServer(server);
					servers_turned_off++;
				} catch (ServerNotEmptyException e) {
					logger.error("Tryed to turn off a nonempty server.");
				}
			}
		}

		return servers_turned_off;
	}
}
