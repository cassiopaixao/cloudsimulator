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
		Collections.sort(machineTypes);

		return selectDestinationServer(vmDemand, machineTypes);
	}
}
