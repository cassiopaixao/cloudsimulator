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

		return selectDestinationServer(vmDemand, machineTypes);
	}

}
