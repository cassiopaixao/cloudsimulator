package br.usp.ime.cassiop.workloadsim.util;

import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;

public class TestUtils {

	public static Server buildServer(double cpu, double mem) {
		Server server = new Server();

		server.setCapacity(ResourceType.CPU, cpu);
		server.setCapacity(ResourceType.MEMORY, mem);

		return server;
	}

	public static Server buildServer(String name, double cpu, double mem) {
		Server server = buildServer(cpu, mem);
		server.setName(name);
		return server;
	}

	public static VirtualMachine buildVirtualMachine(double cpu, double mem) {
		VirtualMachine vm = new VirtualMachine();

		vm.setDemand(ResourceType.CPU, cpu);
		vm.setDemand(ResourceType.MEMORY, mem);

		return vm;
	}

	public static VirtualMachine buildVirtualMachine(String name, double cpu,
			double mem) {
		VirtualMachine vm = buildVirtualMachine(cpu, mem);
		vm.setName(name);
		return vm;
	}
}
