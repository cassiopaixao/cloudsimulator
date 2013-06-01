package br.usp.ime.cassiop.workloadsim.placement;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.MathUtils;

public class KhannaPlacement extends PlacementStrategy {

	@Override
	public void orderServers(List<Server> servers) {
	}

	@Override
	public void orderDemand(List<VirtualMachine> demand) {
	}

	@Override
	public Server selectDestinationServer(VirtualMachine vm,
			List<Server> servers) {
		// sort servers by residual capacity
		Collections.sort(servers,
				new ServerOrderedByResidualCapacityComparator());

		// allocate in the first server with available capacity
		Server destinationServer = null;
		for (Server server : servers) {
			if (server.canHost(vm, true)) {
				destinationServer = server;
				break;
			}
		}

		return destinationServer;
	}

	@Override
	public Server chooseServerType(VirtualMachine vmDemand,
			List<Server> machineTypes) {
		return selectDestinationServer(vmDemand, machineTypes);
	}

	public static class ServerOrderedByResidualCapacityComparator implements
			Comparator<Server> {
		@Override
		public int compare(Server o1, Server o2) {
			if (MathUtils.lessThan(o1.getResidualCapacity(),
					o2.getResidualCapacity())) {
				return -1;
			} else if (MathUtils.equals(o1.getResidualCapacity(),
					o2.getResidualCapacity())) {
				return 0;
			} else {
				return 1;
			}
		}

	}
}
