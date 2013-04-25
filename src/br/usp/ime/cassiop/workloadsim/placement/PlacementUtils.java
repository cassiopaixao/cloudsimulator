package br.usp.ime.cassiop.workloadsim.placement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;

public class PlacementUtils {
	public double leavingResource(Server server, VirtualMachine vm) {
		double leavingCpu, leavingMem;
		leavingCpu = server.getFreeResource(ResourceType.CPU)
				- vm.getDemand(ResourceType.CPU);
		leavingMem = server.getFreeResource(ResourceType.MEMORY)
				- vm.getDemand(ResourceType.MEMORY);

		return leavingCpu + leavingMem;
	}

	public Server lessLossOfPerformanceMachine(List<Server> servers,
			VirtualMachine vmDemand) {
		Server lessLossOfPerformanceMachine = null;
		double lessLossOfPerformance = Double.MAX_VALUE;

		for (Server server : servers) {
			if (lossOfPerformance(server, vmDemand) < lessLossOfPerformance) {
				lessLossOfPerformance = lossOfPerformance(server, vmDemand);
				lessLossOfPerformanceMachine = server;
			}
		}
		return lessLossOfPerformanceMachine;
	}

	public double lossOfPerformance(Server server, VirtualMachine vm) {
		double leavingCpu, leavingMem;
		double sum = 0;
		leavingCpu = server.getFreeResource(ResourceType.CPU)
				- vm.getDemand(ResourceType.CPU);
		leavingMem = server.getFreeResource(ResourceType.MEMORY)
				- vm.getDemand(ResourceType.MEMORY);

		sum += (leavingCpu < 0) ? -leavingCpu : 0;
		sum += (leavingMem < 0) ? -leavingMem : 0;

		return sum;
	}

	public Server lessLossEmptyServer(Collection<Server> servers,
			VirtualMachine vm) {
		List<Server> emptyServers = new LinkedList<Server>();
		for (Server server : servers) {
			if (server.getVirtualMachines().isEmpty()) {
				emptyServers.add(server);
			}
		}

		return lessLossOfPerformanceMachine(emptyServers, vm);

	}
}
