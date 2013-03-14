package br.usp.ime.cassiop.workloadsim.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;

import br.usp.ime.cassiop.workloadsim.exceptions.ServerOverloadedException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownVirtualMachineException;
import br.usp.ime.cassiop.workloadsim.util.MathUtils;

public class ServerTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Locale.setDefault(Locale.US);
	}

	private Server buildServer() {
		return buildServer(1.0, 1.0);
	}

	private Server buildServer(double cpu, double memory) {
		Server server = new Server();
		server.setCapacity(ResourceType.CPU, cpu);
		server.setCapacity(ResourceType.MEMORY, memory);

		return server;
	}

	private VirtualMachine buildVM(double cpu, double memory) {
		VirtualMachine vm = new VirtualMachine();
		vm.setDemand(ResourceType.CPU, cpu);
		vm.setDemand(ResourceType.MEMORY, memory);

		return vm;
	}

	@Test
	public void testGetResourceUtilization() {
		Server server = buildServer();
		assertEquals(0.0, server.getResourceUtilization(), MathUtils.EPSILON);

		VirtualMachine vm1 = buildVM(0.5, 0.5);
		VirtualMachine vm2 = buildVM(0.25, 0.25);

		try {
			server.addVirtualMachine(vm1);
			assertEquals(0.7071, server.getResourceUtilization(), 0.0001);

		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		} catch (ServerOverloadedException e) {
			fail(e.getMessage());
		}

		try {
			server.addVirtualMachine(vm2);
			assertEquals(1.0606, server.getResourceUtilization(), 0.0001);

		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		} catch (ServerOverloadedException e) {
			fail(e.getMessage());
		}

		try {
			server.removeVirtualMachine(vm2);
			assertEquals(0.7071, server.getResourceUtilization(), 0.0001);

		} catch (Exception e) {
			fail(e.getMessage());
		}

		try {
			server.removeVirtualMachine(vm1);
			assertEquals(0.0, server.getResourceUtilization(),
					MathUtils.EPSILON);

		} catch (Exception e) {
			fail(e.getMessage());
		}

		assertEquals(0.0, server.getResourceUtilization(), MathUtils.EPSILON);

	}

	@Test
	public void testGetResidualCapacity() {
		Server server = buildServer();

		assertEquals(1.4142, server.getResidualCapacity(), 0.0001);

		VirtualMachine vm1 = buildVM(0.5, 0.5);
		VirtualMachine vm2 = buildVM(0.25, 0.5);
		VirtualMachine vm3 = buildVM(0.0, 0.25);
		VirtualMachine vm4 = buildVM(0.5, 0.25);

		try {
			server.addVirtualMachine(vm1);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		} catch (ServerOverloadedException e) {
			fail("Server shouldn't be overloaded.");
		}

		// freecpu: 0.5, freemem: 0.5
		assertEquals(0.7071, server.getResidualCapacity(), 0.0001);

		try {
			server.addVirtualMachine(vm2);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		} catch (ServerOverloadedException e) {
			fail("Server shouldn't be overloaded.");
		}

		// freecpu: 0.25, freemem: 0.0
		assertEquals(0.25, server.getResidualCapacity(), 0.001);

		try {
			server.addVirtualMachine(vm3);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		} catch (ServerOverloadedException e) {
			// ok, server is expected to be overloaded.
		}

		// freecpu: 0.25, freemem: -0.25
		assertEquals(0.25, server.getResidualCapacity(), 0.0001);

		try {
			server.addVirtualMachine(vm4);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		} catch (ServerOverloadedException e) {
			// ok, server is expected to be overloaded.
		}

		// freecpu: -0.25, freemem: -0.5
		assertEquals(0.0, server.getResidualCapacity(), MathUtils.EPSILON);
	}

	@Test
	public void testGetLoadPercentage() {
		Server server = buildServer();

		assertEquals(0.0, server.getLoadPercentage(), 0.0001);

		VirtualMachine vm1 = buildVM(0.5, 0.5);
		VirtualMachine vm2 = buildVM(0.25, 0.5);
		VirtualMachine vm3 = buildVM(0.0, 0.25);
		VirtualMachine vm4 = buildVM(0.5, 0.25);

		try {
			server.addVirtualMachine(vm1);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		} catch (ServerOverloadedException e) {
			fail("Server shouldn't be overloaded.");
		}

		// cpu: 0.5, mem: 0.5
		assertEquals(0.25, server.getLoadPercentage(), 0.0001);

		try {
			server.addVirtualMachine(vm2);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		} catch (ServerOverloadedException e) {
			fail("Server shouldn't be overloaded.");
		}

		// cpu: 0.75, mem: 1.0
		assertEquals(0.75, server.getLoadPercentage(), 0.001);

		try {
			server.addVirtualMachine(vm3);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		} catch (ServerOverloadedException e) {
			// ok, server is expected to be overloaded.
		}

		// cpu: 0.75, mem: 1.25
		assertEquals(0.9375, server.getLoadPercentage(), 0.0001);

		try {
			server.addVirtualMachine(vm4);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		} catch (ServerOverloadedException e) {
			// ok, server is expected to be overloaded.
		}

		// cpu: 1.25, mem: 1.5
		assertEquals(1.875, server.getLoadPercentage(), 0.0001);
	}

	@Test
	public void testServer() {
		Server server = new Server();

		assertEquals(0.0, server.getCapacity(ResourceType.CPU),
				MathUtils.EPSILON);

		assertEquals(0.0, server.getCapacity(ResourceType.MEMORY),
				MathUtils.EPSILON);

		assertEquals(0.0, server.getResourceUtilization(), MathUtils.EPSILON);

		assertEquals(0.0, server.getResidualCapacity(), MathUtils.EPSILON);

		assertTrue(server.getVirtualMachines().isEmpty());

	}

	@Test
	public void testGetVirtualMachines() {
		Server server = buildServer();
		VirtualMachine vm1 = buildVM(0.5, 0.5);
		VirtualMachine vm2 = buildVM(0.25, 0.25);

		assertFalse(server.getVirtualMachines().contains(vm1));
		assertFalse(server.getVirtualMachines().contains(vm2));

		try {
			server.addVirtualMachine(vm1);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		assertTrue(server.getVirtualMachines().contains(vm1));
		assertFalse(server.getVirtualMachines().contains(vm2));

		try {
			server.addVirtualMachine(vm2);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		assertTrue(server.getVirtualMachines().contains(vm1));
		assertTrue(server.getVirtualMachines().contains(vm2));

	}

	@Test
	public void testAddVirtualMachine() {
		Server server = buildServer();
		VirtualMachine vm1 = buildVM(0.5, 0.5);
		VirtualMachine vm2 = buildVM(0.75, 0.5);

		assertFalse(server.getVirtualMachines().contains(vm1));
		assertFalse(server.getVirtualMachines().contains(vm2));
		assertEquals(server.getVirtualMachines().size(), 0);

		try {
			server.addVirtualMachine(vm1);
		} catch (UnknownVirtualMachineException e) {
			fail("Virtual machine shouldn't be null.");
		} catch (ServerOverloadedException e) {
			fail("Server shouldn't be overloaded.");
		}

		assertTrue(server.getVirtualMachines().contains(vm1));
		assertFalse(server.getVirtualMachines().contains(vm2));
		assertEquals(server.getVirtualMachines().size(), 1);

		try {
			server.addVirtualMachine(vm2);
			fail("ServerOverloadedExecption was not thrown.");
		} catch (UnknownVirtualMachineException e) {
			fail("Virtual machine shouldn't be null.");
		} catch (ServerOverloadedException e) {
			assertNotNull(e);
		}

		assertTrue(server.getVirtualMachines().contains(vm1));
		assertTrue(server.getVirtualMachines().contains(vm2));
		assertEquals(server.getVirtualMachines().size(), 2);

		try {
			server.addVirtualMachine(null);
			fail("UnknownVirtualMachineExecption was not thrown.");
		} catch (UnknownVirtualMachineException e) {
			assertNotNull("UnknownVirtualMachineException should be thrown.", e);
		} catch (ServerOverloadedException e) {
			fail("Shouldn't throw ServerOverloadException.");
		}

		assertTrue(server.getVirtualMachines().contains(vm1));
		assertTrue(server.getVirtualMachines().contains(vm2));
		assertEquals(server.getVirtualMachines().size(), 2);

	}

	@Test
	public void testClear() {
		Server server = buildServer();
		VirtualMachine vm1 = buildVM(0.5, 0.5);
		VirtualMachine vm2 = buildVM(0.25, 0.25);

		try {
			server.addVirtualMachine(vm1);
			server.addVirtualMachine(vm2);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		assertTrue(server.getVirtualMachines().contains(vm1));
		assertTrue(server.getVirtualMachines().contains(vm2));

		server.clear();

		assertTrue(server.getVirtualMachines().isEmpty());
		assertEquals(0.0, server.getResourceUtilization(), MathUtils.EPSILON);
		assertEquals(1.4142, server.getResidualCapacity(), 0.0001);
		assertTrue(MathUtils.equals(server.getFreeResource(ResourceType.CPU),
				server.getCapacity(ResourceType.CPU)));
		assertTrue(MathUtils.equals(
				server.getFreeResource(ResourceType.MEMORY),
				server.getCapacity(ResourceType.MEMORY)));
	}

	@Test
	public void testRemoveVirtualMachine() {
		Server server = buildServer();
		VirtualMachine vm1 = buildVM(0.5, 0.5);
		VirtualMachine vm2 = buildVM(0.25, 0.25);

		try {
			server.addVirtualMachine(vm1);
			server.addVirtualMachine(vm2);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		assertTrue(server.getVirtualMachines().contains(vm1));
		assertTrue(server.getVirtualMachines().contains(vm2));

		assertNotNull(vm1.getCurrentServer());
		assertNotNull(vm2.getCurrentServer());

		try {
			server.removeVirtualMachine(vm1);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		assertFalse(server.getVirtualMachines().contains(vm1));
		assertTrue(server.getVirtualMachines().contains(vm2));

		assertNull(vm1.getCurrentServer());
		assertNotNull(vm2.getCurrentServer());

		assertNotNull(vm1.getLastServer());

		try {
			server.removeVirtualMachine(vm2);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		assertFalse(server.getVirtualMachines().contains(vm1));
		assertFalse(server.getVirtualMachines().contains(vm2));

		assertNull(vm1.getCurrentServer());
		assertNull(vm2.getCurrentServer());

		assertNotNull(vm1.getLastServer());
		assertNotNull(vm2.getLastServer());
	}

	@Test
	public void testSetCapacity() {
		Server server = new Server();
		VirtualMachine vm1 = buildVM(0.5, 0.5);

		server.setCapacity(ResourceType.CPU, 1.0);
		server.setCapacity(ResourceType.MEMORY, 1.0);

		assertEquals(1.0, server.getCapacity(ResourceType.CPU),
				MathUtils.EPSILON);
		assertEquals(1.0, server.getCapacity(ResourceType.MEMORY),
				MathUtils.EPSILON);

		try {
			server.addVirtualMachine(vm1);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		assertEquals(0.5, server.getFreeResource(ResourceType.CPU), 0.0001);
		assertEquals(0.5, server.getFreeResource(ResourceType.MEMORY), 0.0001);

		assertEquals(0.25, server.getLoadPercentage(), 0.0001);
		assertEquals(0.7071, server.getResidualCapacity(), 0.0001);
		assertEquals(0.7071, server.getResourceUtilization(), 0.0001);

		server.setCapacity(ResourceType.CPU, 0.5);
		server.setCapacity(ResourceType.MEMORY, 0.5);

		assertEquals(0.5, server.getCapacity(ResourceType.CPU),
				MathUtils.EPSILON);
		assertEquals(0.5, server.getCapacity(ResourceType.MEMORY),
				MathUtils.EPSILON);

		assertEquals(0.0, server.getFreeResource(ResourceType.CPU), 0.0001);
		assertEquals(0.0, server.getFreeResource(ResourceType.MEMORY), 0.0001);

		assertEquals(1.0, server.getLoadPercentage(), 0.0001);
		assertEquals(0.0, server.getResidualCapacity(), 0.0001);
		assertEquals(0.7071, server.getResourceUtilization(), 0.0001);

		server.setCapacity(ResourceType.CPU, 0.25);
		server.setCapacity(ResourceType.MEMORY, 0.25);

		assertEquals(0.25, server.getCapacity(ResourceType.CPU),
				MathUtils.EPSILON);
		assertEquals(0.25, server.getCapacity(ResourceType.MEMORY),
				MathUtils.EPSILON);

		assertEquals(-0.25, server.getFreeResource(ResourceType.CPU), 0.0001);
		assertEquals(-0.25, server.getFreeResource(ResourceType.MEMORY), 0.0001);

		assertEquals(4.0, server.getLoadPercentage(), 0.0001);
		assertEquals(0.0, server.getResidualCapacity(), 0.0001);
		assertEquals(0.7071, server.getResourceUtilization(), 0.0001);

	}

	@Test
	public void testSetKneePerformanceLoss() {
		Server server = buildServer();
		VirtualMachine vm1 = buildVM(0.75, 0.5);

		try {
			server.addVirtualMachine(vm1);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		assertFalse(server.isAlmostOverloaded());

		server.setKneePerformanceLoss(ResourceType.CPU, 0.5);

		assertTrue(server.isAlmostOverloaded());

	}

	@Test
	public void testCanHostVirtualMachine() {
		Server server = buildServer();
		VirtualMachine vm1 = buildVM(0.5, 0.5);
		VirtualMachine vm2 = buildVM(0.25, 0.25);
		VirtualMachine vm3 = buildVM(0.5, 0.5);
		VirtualMachine vm4 = buildVM(0.75, 0.75);

		assertTrue(server.canHost(vm1));
		assertTrue(server.canHost(vm2));
		assertTrue(server.canHost(vm3));
		assertTrue(server.canHost(vm4));

		try {
			server.addVirtualMachine(vm1);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		assertTrue(server.canHost(vm2));
		assertTrue(server.canHost(vm3));
		assertFalse(server.canHost(vm4));
	}

	@Test
	public void testCanHostVirtualMachineBoolean() {
		Server server = buildServer();
		VirtualMachine vm1 = buildVM(0.5, 0.5);
		VirtualMachine vm2 = buildVM(0.25, 0.25);
		VirtualMachine vm3 = buildVM(0.75, 0.75);

		assertTrue(server.canHost(vm1, true));
		assertTrue(server.canHost(vm2, true));
		assertTrue(server.canHost(vm3, true));

		server.setKneePerformanceLoss(ResourceType.CPU, 0.5);

		assertTrue(server.canHost(vm1, true));
		assertTrue(server.canHost(vm2, true));
		assertFalse(server.canHost(vm3, true));

		server.setKneePerformanceLoss(ResourceType.CPU, 0.6);

		assertTrue(server.canHost(vm1, true));
		assertTrue(server.canHost(vm2, true));
		assertFalse(server.canHost(vm3, true));
	}

	@Test
	public void testGetType() {
		Server server = buildServer(0.5, 0.75);
		server.setName("test");

		assertTrue("CPU(0.50);MEM(0.75).".equals(server.getType()));

		server.setCapacity(ResourceType.MEMORY, 1.0);

		assertTrue("CPU(0.50);MEM(1.00).".equals(server.getType()));
	}

	@Test
	public void testUpdateVm() {
		Server server = buildServer();
		VirtualMachine vm1 = buildVM(0.5, 0.5);
		VirtualMachine vm2 = buildVM(0.25, 0.25);
		VirtualMachine vm3 = buildVM(1.5, 1.5);
		VirtualMachine vm4 = buildVM(0.5, 0.5);

		vm1.setName("testVm");
		vm2.setName("testVm");
		vm3.setName("testVm");
		vm4.setName("testVmException");
		
		try {
			server.addVirtualMachine(vm1);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		assertTrue(server.getVirtualMachines().contains(vm1));
		assertEquals(0.5, vm1.getDemand(ResourceType.CPU), MathUtils.EPSILON);
		assertEquals(0.5, vm1.getDemand(ResourceType.MEMORY), MathUtils.EPSILON);
		
		try {
			server.updateVm(vm2);
		} catch (ServerOverloadedException e) {
			fail("VM's new values aren't big enough. Shouldn't throw ServerOverloadedException.");
		} catch (UnknownVirtualMachineException e) {
			fail("Both virtual machines has the same name. Shouldn't throw UnknownVirtualMachineException.");
		}
		
		assertTrue(server.getVirtualMachines().contains(vm1));
		assertEquals(0.25, vm1.getDemand(ResourceType.CPU), MathUtils.EPSILON);
		assertEquals(0.25, vm1.getDemand(ResourceType.MEMORY), MathUtils.EPSILON);
		
		try {
			server.updateVm(vm3);
			fail("VM's new values are big enough. Should throw UnknownVirtualMachineException.");			
		} catch (ServerOverloadedException e) {
			assertNotNull("VM's new values are big enough. Should throw ServerOverloadedException.", e);
		} catch (UnknownVirtualMachineException e) {
			fail("Both virtual machines has the same name. Shouldn't throw UnknownVirtualMachineException.");
		}

		assertTrue(server.getVirtualMachines().contains(vm1));
		assertEquals(1.5, vm1.getDemand(ResourceType.CPU), MathUtils.EPSILON);
		assertEquals(1.5, vm1.getDemand(ResourceType.MEMORY), MathUtils.EPSILON);
		
		try {
			server.updateVm(vm4);
			fail("vm4 doesn't have same name. Should throw UnknownVirtualMachineException.");			
		} catch (ServerOverloadedException e) {
			fail("VM's new values aren't big enough. Shouldn't throw ServerOverloadedException.");
		} catch (UnknownVirtualMachineException e) {
			assertNotNull("vm4 doesn't have same name. Should throw UnknownVirtualMachineException.", e);
		}

	}

	@Test
	public void testIsOverloaded() {
		Server server = buildServer();
		VirtualMachine vm1 = buildVM(0.5, 0.5);
		VirtualMachine vm2 = buildVM(0.25, 0.25);
		VirtualMachine vm3 = buildVM(0.5, 0.5);

		try {
			server.addVirtualMachine(vm1);
			server.addVirtualMachine(vm2);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		} catch (ServerOverloadedException e) {
			fail("Server shouldn't be overloaded.");
		}

		assertFalse(server.isOverloaded());

		try {
			server.addVirtualMachine(vm3);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		} catch (ServerOverloadedException e) {
			assertTrue("Server should be overloaded.", server.isOverloaded());
		}

		assertTrue(server.isOverloaded());

		try {
			server.removeVirtualMachine(vm3);
		} catch (UnknownVirtualMachineException e) {
			fail("The virtual machine should be allocated to the server.");
		}

		assertFalse(server.isOverloaded());
	}

	@Test
	public void testIsAlmostOverloaded() {
		Server server = buildServer();
		VirtualMachine vm1 = buildVM(0.5, 0.25);
		VirtualMachine vm2 = buildVM(0.25, 0.1);
		VirtualMachine vm3 = buildVM(0.25, 0.65);

		server.setKneePerformanceLoss(ResourceType.CPU, 0.7);
		server.setKneePerformanceLoss(ResourceType.MEMORY, 0.7);

		try {
			server.addVirtualMachine(vm1);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		} catch (ServerOverloadedException e) {
			fail("Server shouldn't be overloaded.");
		}
		// cpu: 0.5, mem: 0.5
		assertFalse(server.isAlmostOverloaded());

		try {
			server.addVirtualMachine(vm2);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		} catch (ServerOverloadedException e) {
			fail("Server shouldn't be overloaded.");
		}

		// cpu: 0.75, mem: 0.6
		// almost overloaded by cpu
		assertTrue(server.isAlmostOverloaded());

		try {
			server.removeVirtualMachine(vm1);
		} catch (UnknownVirtualMachineException e) {
			fail("The virtual machine should be allocated to the server.");
		}

		// cpu: 0.25, mem: 0.1
		assertFalse(server.isAlmostOverloaded());

		try {
			server.addVirtualMachine(vm3);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		} catch (ServerOverloadedException e) {
			fail("Server shouldn't be overloaded.");
		}

		// cpu: 0.5, mem: 0.75
		// almost overloaded by mem
		assertTrue(server.isAlmostOverloaded());

		try {
			server.removeVirtualMachine(vm2);
		} catch (UnknownVirtualMachineException e) {
			fail("The virtual machine should be allocated to the server.");
		}

		// cpu: 0.25, mem: 0.65
		assertFalse(server.isAlmostOverloaded());

		try {
			server.addVirtualMachine(vm1);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		} catch (ServerOverloadedException e) {
			fail("Server shouldn't be overloaded.");
		}

		// cpu: 0.75, mem: 0.90
		// almost overloaded by cpu and mem
		assertTrue(server.isAlmostOverloaded());
	}

}
