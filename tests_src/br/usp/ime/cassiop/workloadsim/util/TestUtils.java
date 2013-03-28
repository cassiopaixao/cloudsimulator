package br.usp.ime.cassiop.workloadsim.util;

import static org.junit.Assert.*;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;

public class TestUtils {

	public static class AddVmToServer implements Answer<Object> {
		@Override
		public Object answer(InvocationOnMock invocation) throws Throwable {
			Object[] arguments = invocation.getArguments();

			VirtualMachine vm = (VirtualMachine) arguments[0];
			Server server = (Server) arguments[1];

			if (vm != null && server != null) {
				server.addVirtualMachine(vm);
			}

			return null;
		}
	}
	
	public static class RemoveVmFromServer implements Answer<Object> {
		@Override
		public Object answer(InvocationOnMock invocation) throws Throwable {
			Object[] arguments = invocation.getArguments();

			VirtualMachine vm = (VirtualMachine) arguments[0];
			
			if (vm != null && vm.getCurrentServer() != null) {
				vm.getCurrentServer().removeVirtualMachine(vm);
			}

			return null;
		}
	}

	public static void failConfiguringMocks(Exception e) {
		fail("Exception thrown configuring mocks: " + e.getMessage());
	}

	public static void failConfiguringInitialState(Exception e) {
		fail("Exception thrown configuring initial state: " + e.getMessage());
	}

	public static void failVerifyingMethodsCalls(Exception e) {
		fail("Exception thrown verifying methods' calls: " + e.getMessage());
	}

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
