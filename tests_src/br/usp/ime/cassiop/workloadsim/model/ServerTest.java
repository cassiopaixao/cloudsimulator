package br.usp.ime.cassiop.workloadsim.model;

import static org.junit.Assert.*;

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

	private Server buildCanonicalServer() {
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
		Server server = buildCanonicalServer();
		assertEquals(0.0, server.getResourceUtilization(), MathUtils.EPSILON);

		VirtualMachine vm1 = buildVM(0.5, 0.5);
		VirtualMachine vm2 = buildVM(0.25, 0.25);

		try {
			server.addVirtualMachine(vm1);
			assertEquals(0.7071, server.getResourceUtilization(), 0.0001);

		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		}

		try {
			server.addVirtualMachine(vm2);
			assertEquals(1.0606, server.getResourceUtilization(), 0.0001);

		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		}

		try {
			server.removeVirtualMachine(vm2);
			assertEquals(0.7071, server.getResourceUtilization(), 0.0001);

		} catch (UnknownVirtualMachineException e) {
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
	public void testGetResidualCapacityEmptyServer() {
		Server server = buildCanonicalServer();

		assertEquals(1.4142, server.getResidualCapacity(), 0.0001);
	}

	@Test
	public void testGetResidualCapacity() {
		Server server = buildCanonicalServer();

		VirtualMachine vm1 = buildVM(0.5, 0.5);

		try {
			server.addVirtualMachine(vm1);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetResidualCapacityOverloadedServer1() {
		Server server = buildCanonicalServer();

		VirtualMachine vm1 = buildVM(1.25, 0.75);

		try {
			server.addVirtualMachine(vm1);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		}

		// freecpu: -0.25, freemem: 0.25
		assertEquals(0.25, server.getResidualCapacity(), 0.001);

	}

	@Test
	public void testGetResidualCapacityOverloadedServer2() {
		Server server = buildCanonicalServer();

		VirtualMachine vm1 = buildVM(1.25, 1.25);

		try {
			server.addVirtualMachine(vm1);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		}

		// freecpu: -0.25, freemem: -0.25
		assertEquals(0.0, server.getResidualCapacity(), 0.001);

	}

	@Test
	public void testGetLoadPercentage() {
		Server server = buildCanonicalServer();

		assertEquals(0.0, server.getLoadPercentage(), 0.0001);

		VirtualMachine vm1 = buildVM(0.5, 0.5);

		try {
			server.addVirtualMachine(vm1);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		}

		// cpu: 0.5, mem: 0.5
		assertEquals(0.25, server.getLoadPercentage(), 0.0001);
	}

	@Test
	public void testGetLoadPercentageWithTwoVirtualMachines() {
		Server server = buildCanonicalServer();

		VirtualMachine vm1 = buildVM(0.5, 0.25);
		VirtualMachine vm2 = buildVM(0.25, 0.25);

		try {
			server.addVirtualMachine(vm1);
			server.addVirtualMachine(vm2);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		}

		// cpu: 0.75, mem: 0.5
		assertEquals(0.375, server.getLoadPercentage(), 0.0001);
	}

	@Test
	public void testGetLoadPercentageOfAnOverloadedServer1() {
		Server server = buildCanonicalServer();

		VirtualMachine vm1 = buildVM(0.25, 1.25);

		try {
			server.addVirtualMachine(vm1);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		}

		// cpu: 0.25, mem: 1.25
		assertEquals(0.3125, server.getLoadPercentage(), 0.0001);
	}

	@Test
	public void testGetLoadPercentageOfAnOverloadedServer2() {
		Server server = buildCanonicalServer();

		VirtualMachine vm1 = buildVM(1.25, 1.5);

		try {
			server.addVirtualMachine(vm1);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
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
		Server server = buildCanonicalServer();
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
		Server server = buildCanonicalServer();
		VirtualMachine vm1 = buildVM(0.5, 0.5);
		VirtualMachine vm2 = buildVM(0.75, 0.5);

		assertFalse(server.getVirtualMachines().contains(vm1));
		assertFalse(server.getVirtualMachines().contains(vm2));

		try {
			server.addVirtualMachine(vm1);
		} catch (UnknownVirtualMachineException e) {
			fail("Virtual machine shouldn't be null.");
		}

		assertTrue(server.getVirtualMachines().contains(vm1));
		assertFalse(server.getVirtualMachines().contains(vm2));

		try {
			server.addVirtualMachine(vm2);
		} catch (UnknownVirtualMachineException e) {
			fail("Virtual machine shouldn't be null.");
		}

		assertTrue(server.getVirtualMachines().contains(vm1));
		assertTrue(server.getVirtualMachines().contains(vm2));

	}

	@Test(expected = UnknownVirtualMachineException.class)
	public void testAddNullVirtualMachine()
			throws UnknownVirtualMachineException {
		Server server = buildCanonicalServer();

		server.addVirtualMachine(null);
	}

	@Test
	public void testAddVirtualMachineAlreadyAllocated() {
		Server server = buildCanonicalServer();
		VirtualMachine vm1 = buildVM(0.5, 0.5);

		try {
			server.addVirtualMachine(vm1);
		} catch (UnknownVirtualMachineException e) {
			fail("Virtual machine shouldn't be null.");
		}

		assertTrue(server.getVirtualMachines().contains(vm1));
		assertEquals(0.5, server.getFreeResource(ResourceType.CPU), 0.0001);
		assertEquals(0.5, server.getFreeResource(ResourceType.MEMORY), 0.0001);

		try {
			server.addVirtualMachine(vm1);
		} catch (UnknownVirtualMachineException e) {
			fail("Virtual machine shouldn't be null.");
		}

		assertTrue(server.getVirtualMachines().contains(vm1));
		assertEquals(0.5, server.getFreeResource(ResourceType.CPU), 0.0001);
		assertEquals(0.5, server.getFreeResource(ResourceType.MEMORY), 0.0001);
	}

	@Test
	public void testClear() {
		Server server = buildCanonicalServer();
		VirtualMachine vm1 = buildVM(0.5, 0.5);
		VirtualMachine vm2 = buildVM(0.25, 0.25);

		try {
			server.addVirtualMachine(vm1);
			server.addVirtualMachine(vm2);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		}

		assertTrue(server.getVirtualMachines().contains(vm1));
		assertTrue(server.getVirtualMachines().contains(vm2));

		server.clear();

		assertTrue(server.getVirtualMachines().isEmpty());
		assertEquals(0.0, server.getResourceUtilization(), 0.0001);
		assertEquals(1.4142, server.getResidualCapacity(), 0.0001);
		assertEquals(1.0, server.getFreeResource(ResourceType.CPU), 0.0001);
		assertEquals(1.0, server.getFreeResource(ResourceType.MEMORY), 0.0001);
	}

	@Test
	public void testRemoveVirtualMachine() {
		Server server = buildCanonicalServer();
		VirtualMachine vm1 = buildVM(0.5, 0.5);
		VirtualMachine vm2 = buildVM(0.25, 0.25);

		try {
			server.addVirtualMachine(vm1);
			server.addVirtualMachine(vm2);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		}

		try {
			server.removeVirtualMachine(vm1);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		}

		assertFalse(server.getVirtualMachines().contains(vm1));
		assertTrue(server.getVirtualMachines().contains(vm2));

		assertNull(vm1.getCurrentServer());
		assertNotNull(vm2.getCurrentServer());

		assertNotNull(vm1.getLastServer());
	}

	@Test(expected = UnknownVirtualMachineException.class)
	public void testRemoveNotAllocatedVirtualMachine()
			throws UnknownVirtualMachineException {
		Server server = buildCanonicalServer();
		VirtualMachine vm1 = buildVM(0.5, 0.5);

		server.removeVirtualMachine(vm1);
	}

	@Test(expected = UnknownVirtualMachineException.class)
	public void testRemoveNullVirtualMachine()
			throws UnknownVirtualMachineException {
		Server server = buildCanonicalServer();

		server.removeVirtualMachine(null);
	}

	@Test
	public void testSetCapacity1() {
		Server server = new Server();
		VirtualMachine vm1 = buildVM(0.5, 0.5);

		server.setCapacity(ResourceType.CPU, 1.0);
		server.setCapacity(ResourceType.MEMORY, 1.0);

		assertEquals(1.0, server.getCapacity(ResourceType.CPU), 0.0001);
		assertEquals(1.0, server.getCapacity(ResourceType.MEMORY), 0.0001);

		try {
			server.addVirtualMachine(vm1);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testSetCapacity2() {
		Server server = new Server();
		VirtualMachine vm1 = buildVM(0.5, 0.5);

		server.setCapacity(ResourceType.CPU, 0.25);
		server.setCapacity(ResourceType.MEMORY, 1.00);

		assertEquals(0.25, server.getCapacity(ResourceType.CPU), 0.0001);
		assertEquals(1.00, server.getCapacity(ResourceType.MEMORY), 0.0001);

		try {
			server.addVirtualMachine(vm1);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		}

		assertEquals(-0.25, server.getFreeResource(ResourceType.CPU), 0.0001);
		assertEquals(0.5, server.getFreeResource(ResourceType.MEMORY), 0.0001);

		assertEquals(1.0, server.getLoadPercentage(), 0.0001);
		assertEquals(0.5, server.getResidualCapacity(), 0.0001);
		assertEquals(0.7071, server.getResourceUtilization(), 0.0001);
	}

	@Test
	public void testSetKneePerformanceLoss() {
		Server server = buildCanonicalServer();
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
		Server server = buildCanonicalServer();
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
		Server server = buildCanonicalServer();
		VirtualMachine vm1 = buildVM(0.5, 0.5);
		VirtualMachine vm2 = buildVM(0.5, 0.75);
		VirtualMachine vm3 = buildVM(0.75, 0.75);

		assertTrue(server.canHost(vm1, true));
		assertTrue(server.canHost(vm2, true));
		assertTrue(server.canHost(vm3, true));

		server.setKneePerformanceLoss(ResourceType.CPU, 0.5);

		assertTrue(server.canHost(vm1, true));
		assertTrue(server.canHost(vm2, true));
		assertFalse(server.canHost(vm3, true));

		server.setKneePerformanceLoss(ResourceType.MEMORY, 0.6);

		assertTrue(server.canHost(vm1, true));
		assertFalse(server.canHost(vm2, true));
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
		Server server = buildCanonicalServer();
		VirtualMachine vm1 = buildVM(0.5, 0.5);
		VirtualMachine vm2 = buildVM(0.25, 0.25);

		vm1.setName("testVm");
		vm2.setName("testVm");

		try {
			server.addVirtualMachine(vm1);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		}

		assertTrue(server.getVirtualMachines().contains(vm1));
		assertEquals(0.5, vm1.getDemand(ResourceType.CPU), 0.0001);
		assertEquals(0.5, vm1.getDemand(ResourceType.MEMORY), 0.0001);

		assertEquals(0.5, server.getFreeResource(ResourceType.CPU), 0.0001);
		assertEquals(0.5, server.getFreeResource(ResourceType.MEMORY), 0.0001);

		try {
			server.updateVm(vm2);
		} catch (ServerOverloadedException e) {
			fail("VM's new values aren't big enough. Shouldn't throw ServerOverloadedException.");
		} catch (UnknownVirtualMachineException e) {
			fail("Both virtual machines has the same name. Shouldn't throw UnknownVirtualMachineException.");
		}

		assertTrue(server.getVirtualMachines().contains(vm1));
		assertEquals(0.25, vm1.getDemand(ResourceType.CPU), 0.0001);
		assertEquals(0.25, vm1.getDemand(ResourceType.MEMORY), 0.0001);

		assertEquals(0.75, server.getFreeResource(ResourceType.CPU), 0.0001);
		assertEquals(0.75, server.getFreeResource(ResourceType.MEMORY), 0.0001);
	}

	@Test(expected = ServerOverloadedException.class)
	public void testUpdateVmToOverload() throws ServerOverloadedException {
		Server server = buildCanonicalServer();
		VirtualMachine vm1 = buildVM(0.5, 0.5);
		VirtualMachine vm2 = buildVM(1.25, 0.75);

		vm1.setName("testVm");
		vm2.setName("testVm");

		try {
			server.addVirtualMachine(vm1);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		}

		try {
			server.updateVm(vm2);
		} catch (UnknownVirtualMachineException e) {
			fail("Both virtual machines has the same name. Shouldn't throw UnknownVirtualMachineException.");
		}
	}

	@Test(expected = UnknownVirtualMachineException.class)
	public void testUpdateVmToANullOne() throws UnknownVirtualMachineException {
		Server server = buildCanonicalServer();
		VirtualMachine vm1 = buildVM(0.5, 0.5);

		vm1.setName("testVm");

		try {
			server.addVirtualMachine(vm1);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		}

		try {
			server.updateVm(null);
		} catch (ServerOverloadedException e) {
			fail("VM's new values aren't big enough. Shouldn't throw ServerOverloadedException.");
		}
	}

	@Test(expected = UnknownVirtualMachineException.class)
	public void testUpdateVmWithDifferentName()
			throws UnknownVirtualMachineException {
		Server server = buildCanonicalServer();
		VirtualMachine vm1 = buildVM(0.5, 0.5);
		VirtualMachine vm2 = buildVM(0.25, 0.25);

		vm1.setName("testVm");
		vm2.setName("testVmException");

		try {
			server.addVirtualMachine(vm1);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		}

		try {
			server.updateVm(vm2);
		} catch (ServerOverloadedException e) {
			fail("VM's new values aren't big enough. Shouldn't throw ServerOverloadedException.");
		}
	}

	@Test
	public void testIsOverloaded() {
		Server server = buildCanonicalServer();
		VirtualMachine vm1 = buildVM(0.5, 0.5);
		VirtualMachine vm2 = buildVM(0.25, 0.25);
		VirtualMachine vm3 = buildVM(0.5, 0.5);

		try {
			server.addVirtualMachine(vm1);
			server.addVirtualMachine(vm2);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		}

		assertFalse(server.isOverloaded());

		try {
			server.addVirtualMachine(vm3);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
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
	public void testIsAlmostOverloadedByCpu() {
		Server server = buildCanonicalServer();
		VirtualMachine vm1 = buildVM(0.75, 0.1);

		server.setKneePerformanceLoss(ResourceType.CPU, 0.7);
		server.setKneePerformanceLoss(ResourceType.MEMORY, 0.7);

		try {
			server.addVirtualMachine(vm1);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		}

		assertTrue(server.isAlmostOverloaded());
	}

	@Test
	public void testIsAlmostOverloadedByMemory() {
		Server server = buildCanonicalServer();
		VirtualMachine vm1 = buildVM(0.1, 0.75);

		server.setKneePerformanceLoss(ResourceType.CPU, 0.7);
		server.setKneePerformanceLoss(ResourceType.MEMORY, 0.7);

		try {
			server.addVirtualMachine(vm1);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		}

		assertTrue(server.isAlmostOverloaded());
	}

	@Test
	public void testIsNotAlmostOverloaded() {
		Server server = buildCanonicalServer();
		VirtualMachine vm1 = buildVM(0.5, 0.25);

		server.setKneePerformanceLoss(ResourceType.CPU, 0.7);
		server.setKneePerformanceLoss(ResourceType.MEMORY, 0.7);

		try {
			server.addVirtualMachine(vm1);
		} catch (UnknownVirtualMachineException e) {
			fail(e.getMessage());
		}

		assertFalse(server.isAlmostOverloaded());
	}
}
