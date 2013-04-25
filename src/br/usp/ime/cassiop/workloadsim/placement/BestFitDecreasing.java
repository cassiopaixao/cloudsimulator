package br.usp.ime.cassiop.workloadsim.placement;

import java.util.Collections;
import java.util.List;

import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;

public class BestFitDecreasing extends PlacementStrategy {

	@Override
	public void orderServers(List<Server> servers) {
	}

	@Override
	public void orderDemand(List<VirtualMachine> demand) {
		// demand.sort desc
		Collections.sort(demand);
		Collections.reverse(demand);
	}

	@Override
	public Server selectDestinationServer(VirtualMachine vm,
			List<Server> servers) {
		Server destinationServer = null;
		double leavingResource = Double.MAX_VALUE;

		for (Server server : servers) {
			if (server.canHost(vm)) {
				// stores the best-fit allocation
				if (placementUtils.leavingResource(server, vm) < leavingResource) {
					destinationServer = server;
					leavingResource = placementUtils
							.leavingResource(server, vm);
				}

			}
		}

		return destinationServer;
	}

	@Override
	public Server chooseServerType(VirtualMachine vmDemand,
			List<Server> machineTypes) {
		Server selectedMachine = null;
		double leavingResource = Double.MAX_VALUE;

		Collections.sort(machineTypes);

		for (Server server : machineTypes) {
			if (server.canHost(vmDemand)) {
				if (placementUtils.leavingResource(server, vmDemand) < leavingResource) {
					selectedMachine = server;
					leavingResource = placementUtils.leavingResource(server,
							vmDemand);
				}
			}
		}

		if (selectedMachine == null) {
//			logger.debug("No inactive physical machine can satisfy the virtual machine's demand. Activating the physical machine with lowest loss of performance.");

			Server lessLossOfPerformanceMachine = placementUtils
					.lessLossOfPerformanceMachine(machineTypes, vmDemand);

			if (lessLossOfPerformanceMachine == null) {
//				logger.debug("There is no inactive physical machine. Need to overload one.");
				return null;
			}

			selectedMachine = lessLossOfPerformanceMachine;
		}
		return selectedMachine;
	}
}
