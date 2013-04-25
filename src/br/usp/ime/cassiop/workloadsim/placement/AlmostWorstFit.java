package br.usp.ime.cassiop.workloadsim.placement;

import java.util.List;

import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;

public class AlmostWorstFit extends PlacementStrategy {

	@Override
	public void orderServers(List<Server> servers) {
	}

	@Override
	public void orderDemand(List<VirtualMachine> demand) {
	}

	@Override
	public Server selectDestinationServer(VirtualMachine vm,
			List<Server> servers) {
		Server destinationServer = null;

		Server worstFitServer = null;
		double worstFitLeavingResource = -1.0;

		Server almostWorstFitServer = null;
		double almostWorstFitLeavingResource = -1.0;

		double leavingResource;

		for (Server server : servers) {
			if (server.canHost(vm)) {
				// stores the almost worst-fit allocation
				leavingResource = placementUtils.leavingResource(server, vm);

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

		destinationServer = (almostWorstFitServer != null) ? almostWorstFitServer
				: worstFitServer;

		return destinationServer;

	}

	@Override
	public Server chooseServerType(VirtualMachine vmDemand,
			List<Server> machineTypes) {
		Server worstFitMachine = null;
		double worstFitLeavingResource = -1.0;

		Server almostWorstFitMachine = null;
		double almostWorstFitLeavingResource = -1.0;

		double leavingResource;

		for (Server server : machineTypes) {
			if (server.canHost(vmDemand)) {
				leavingResource = placementUtils.leavingResource(server,
						vmDemand);
				if (leavingResource > worstFitLeavingResource) {
					almostWorstFitMachine = worstFitMachine;
					almostWorstFitLeavingResource = worstFitLeavingResource;

					worstFitMachine = server;
					worstFitLeavingResource = leavingResource;
				} else if (leavingResource > almostWorstFitLeavingResource) {
					almostWorstFitMachine = server;
					almostWorstFitLeavingResource = leavingResource;
				}
			}
		}

		if (almostWorstFitMachine == null) {
			if (worstFitMachine != null) {
				almostWorstFitMachine = worstFitMachine;
			} else {
//				logger.debug("No inactive physical machine can satisfy the virtual machine's demand. Activating the physical machine with lowest loss of performance.");

				Server lessLossOfPerformanceMachine = placementUtils
						.lessLossOfPerformanceMachine(machineTypes, vmDemand);

				if (lessLossOfPerformanceMachine == null) {
//					logger.debug("There is no inactive physical machine. Need to overload one.");
					return null;
				}

				almostWorstFitMachine = lessLossOfPerformanceMachine;
			}
		}
		return almostWorstFitMachine;
	}

}
