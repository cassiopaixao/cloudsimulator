package br.usp.ime.cassiop.workloadsim.placement;

import static br.usp.ime.cassiop.workloadsim.util.TestUtils.buildServer;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.buildVirtualMachine;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.failConfiguringMocks;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;

public class BestFitDecreasingTest {

	BestFitDecreasing bfd = null;

	PlacementUtils placementUtils = null;

	@Before
	public void setUp() throws Exception {
		placementUtils = mock(PlacementUtils.class);

		bfd = new BestFitDecreasing();
		bfd.setPlacementUtils(placementUtils);
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

		bfd.orderServers(servers);

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

		bfd.orderDemand(vmList);

		assertTrue(vmList.get(0).equals(vm3));
		assertTrue(vmList.get(1).equals(vm1));
		assertTrue(vmList.get(2).equals(vm4));
		assertTrue(vmList.get(3).equals(vm2));
		assertTrue(vmList.get(4).equals(vm5));
	}

	@Test
	public void testSelectDestinationServer() {
		VirtualMachine vm = mock(VirtualMachine.class);

		Server server1 = mock(Server.class);
		Server server2 = mock(Server.class);
		Server server3 = mock(Server.class);

		List<Server> servers = new ArrayList<Server>();
		servers.add(server1);
		servers.add(server2);
		servers.add(server3);

		try {
			when(placementUtils.leavingResource(server1, vm)).thenReturn(0.5);
			when(placementUtils.leavingResource(server2, vm)).thenReturn(0.3);
			when(placementUtils.leavingResource(server3, vm)).thenReturn(0.7);

			when(server1.canHost(vm)).thenReturn(true);
			when(server2.canHost(vm)).thenReturn(true);
			when(server3.canHost(vm)).thenReturn(true);
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		Server destinationServer = bfd.selectDestinationServer(vm, servers);

		assertTrue(server2.equals(destinationServer));
	}

	@Test
	public void testSelectDestinationServerThatCanHost() {
		VirtualMachine vm = mock(VirtualMachine.class);

		Server server1 = mock(Server.class);
		Server server2 = mock(Server.class);
		Server server3 = mock(Server.class);

		List<Server> servers = new ArrayList<Server>();
		servers.add(server1);
		servers.add(server2);
		servers.add(server3);

		try {
			when(placementUtils.leavingResource(server1, vm)).thenReturn(0.5);
			when(placementUtils.leavingResource(server2, vm)).thenReturn(0.3);
			when(placementUtils.leavingResource(server3, vm)).thenReturn(0.7);

			when(server1.canHost(vm)).thenReturn(true);
			when(server2.canHost(vm)).thenReturn(false);
			when(server3.canHost(vm)).thenReturn(true);
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		Server destinationServer = bfd.selectDestinationServer(vm, servers);

		assertTrue(server1.equals(destinationServer));
	}

	@Test
	public void testSelectNoneDestinationServer() {
		VirtualMachine vm = mock(VirtualMachine.class);

		Server server1 = mock(Server.class);

		List<Server> servers = new ArrayList<Server>();
		servers.add(server1);

		try {
			when(server1.canHost(vm)).thenReturn(false);
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		Server destinationServer = bfd.selectDestinationServer(vm, servers);

		assertNull(destinationServer);
		verify(placementUtils, never()).leavingResource(any(Server.class),
				any(VirtualMachine.class));
	}

	@Test
	public void testChooseServerType() {
		VirtualMachine vm = buildVirtualMachine(0.3, 0.7);

		Server server1 = buildServer(1.0, 0.7);
		Server server2 = buildServer(0.5, 0.6);
		Server server3 = buildServer(0.3, 0.8);

		List<Server> servers = new ArrayList<Server>();
		servers.add(server1);
		servers.add(server2);
		servers.add(server3);

		Server destinationServer = bfd.chooseServerType(vm, servers);

		assertTrue(servers.get(0).equals(server3));
		assertTrue(servers.get(1).equals(server2));
		assertTrue(servers.get(2).equals(server1));

		assertTrue(server3.equals(destinationServer));
	}
	
	@Test
	public void testChooseNoneServerType() {
		VirtualMachine vm = buildVirtualMachine(0.3, 0.7);

		Server server1 = buildServer(1.0, 0.6);
		Server server2 = buildServer(0.5, 0.6);
		Server server3 = buildServer(0.2, 0.8);

		List<Server> servers = new ArrayList<Server>();
		servers.add(server1);
		servers.add(server2);
		servers.add(server3);

		Server destinationServer = bfd.chooseServerType(vm, servers);

		assertTrue(servers.get(0).equals(server3));
		assertTrue(servers.get(1).equals(server2));
		assertTrue(servers.get(2).equals(server1));

		assertNull(destinationServer);
	}

}
