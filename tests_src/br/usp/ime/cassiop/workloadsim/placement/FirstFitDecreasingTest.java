package br.usp.ime.cassiop.workloadsim.placement;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;

public class FirstFitDecreasingTest {
	FirstFitDecreasing firstFitDecreasing = null;

	PlacementUtils placementUtils = null;

	@Before
	public void setUp() throws Exception {
		placementUtils = mock(PlacementUtils.class);

		firstFitDecreasing = new FirstFitDecreasing();
		firstFitDecreasing.setPlacementUtils(placementUtils);
	}

	@Test
	public void testOrderServers() {
		Server server1 = buildServer(0.7, 0.7);
		Server server2 = buildServer(0.5, 0.5);
		Server server3 = buildServer(1.0, 1.0);

		List<Server> servers = new ArrayList<Server>();
		servers.add(server1);
		servers.add(server2);
		servers.add(server3);

		firstFitDecreasing.orderServers(servers);

		assertTrue(servers.get(0).equals(server1));
		assertTrue(servers.get(1).equals(server2));
		assertTrue(servers.get(2).equals(server3));
	}

	@Test
	public void testOrderDemand() {
		VirtualMachine vm1 = buildVirtualMachine(0.7, 0.7);
		VirtualMachine vm2 = buildVirtualMachine(0.5, 0.5);
		VirtualMachine vm3 = buildVirtualMachine(1.0, 1.0);
		VirtualMachine vm4 = buildVirtualMachine(0.7, 0.3);
		VirtualMachine vm5 = buildVirtualMachine(0.2, 1.0);

		List<VirtualMachine> vmList = new ArrayList<VirtualMachine>();
		vmList.add(vm1);
		vmList.add(vm2);
		vmList.add(vm3);
		vmList.add(vm4);
		vmList.add(vm5);

		firstFitDecreasing.orderDemand(vmList);

		assertTrue(vmList.get(0).equals(vm3));
		assertTrue(vmList.get(1).equals(vm1));
		assertTrue(vmList.get(2).equals(vm4));
		assertTrue(vmList.get(3).equals(vm2));
		assertTrue(vmList.get(4).equals(vm5));
	}

	@Test
	public void testSelectDestinationServer() {

		VirtualMachine vm = buildVirtualMachine(0.3, 0.7);

		Server server1 = buildServer(1.0, 0.5);
		Server server2 = buildServer(0.5, 0.8);
		Server server3 = buildServer(1.0, 1.0);

		List<Server> servers = new ArrayList<Server>();
		servers.add(server1);
		servers.add(server2);
		servers.add(server3);

		Server destinationServer = firstFitDecreasing.selectDestinationServer(
				vm, servers);

		assertTrue(server2.equals(destinationServer));
	}

	@Test
	public void testSelectNoneDestinationServer() {

		VirtualMachine vm = buildVirtualMachine(0.3, 0.7);

		Server server1 = buildServer(1.0, 0.5);
		Server server2 = buildServer(0.5, 0.5);
		Server server3 = buildServer(1.0, 0.25);

		List<Server> servers = new ArrayList<Server>();
		servers.add(server1);
		servers.add(server2);
		servers.add(server3);

		Server destinationServer = firstFitDecreasing.selectDestinationServer(
				vm, servers);

		assertNull(destinationServer);
	}

	@Test
	public void testChooseServerType() {
		VirtualMachine vm = buildVirtualMachine(0.3, 0.7);

		Server server1 = buildServer(1.0, 0.5);
		Server server2 = buildServer(0.5, 0.6);
		Server server3 = buildServer(1.0, 1.0);

		List<Server> servers = new ArrayList<Server>();
		servers.add(server1);
		servers.add(server2);
		servers.add(server3);

		Server destinationServer = firstFitDecreasing.chooseServerType(vm,
				servers);

		assertTrue(servers.get(0).equals(server2));
		assertTrue(servers.get(1).equals(server1));
		assertTrue(servers.get(2).equals(server3));

		assertTrue(server3.equals(destinationServer));
	}

	@Test
	public void testChooseNoneServerType() {
		VirtualMachine vm = buildVirtualMachine(0.3, 0.7);

		Server server1 = buildServer(1.0, 0.5);
		Server server2 = buildServer(0.5, 0.6);
		Server server3 = buildServer(0.2, 1.0);

		List<Server> servers = new ArrayList<Server>();
		servers.add(server1);
		servers.add(server2);
		servers.add(server3);

		Server destinationServer = firstFitDecreasing.chooseServerType(vm,
				servers);

		assertTrue(servers.get(0).equals(server3));
		assertTrue(servers.get(1).equals(server2));
		assertTrue(servers.get(2).equals(server1));

		assertNull(destinationServer);
	}

}
