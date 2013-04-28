package br.usp.ime.cassiop.workloadsim;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import br.usp.ime.cassiop.workloadsim.exceptions.DependencyNotSetException;
import br.usp.ime.cassiop.workloadsim.exceptions.NoMoreServersAvailableException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownServerException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownVirtualMachineException;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.placement.PlacementStrategy;
import br.usp.ime.cassiop.workloadsim.placement.PlacementUtils;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class PlacementModuleTest {
	PlacementModule placementModule = null;

	StatisticsModule statisticsModule = null;
	VirtualizationManager virtualizationManager = null;
	PlacementUtils placementUtils = null;
	PlacementStrategy placementStrategy = null;

	@Before
	public void setUp() throws Exception {
		statisticsModule = mock(StatisticsModule.class);
		virtualizationManager = mock(VirtualizationManager.class);
		placementUtils = mock(PlacementUtils.class);
		placementStrategy = mock(PlacementStrategy.class);

		placementModule = new PlacementModule();
		placementModule.setPlacementStrategy(placementStrategy);
		placementModule.setPlacementUtils(placementUtils);
		placementModule.setStatisticsModule(statisticsModule);
		placementModule.setVirtualizationManager(virtualizationManager);
	}

	@Test
	public void testConsolidateAll() throws DependencyNotSetException {
		VirtualMachine vm1 = mock(VirtualMachine.class);
		VirtualMachine vm2 = mock(VirtualMachine.class);
		VirtualMachine vm3 = mock(VirtualMachine.class);

		List<VirtualMachine> vmList = new ArrayList<VirtualMachine>();
		vmList.add(vm1);
		vmList.add(vm2);
		vmList.add(vm3);

		Server server1 = mock(Server.class);
		Server server2 = mock(Server.class);

		List<Server> servers = new ArrayList<Server>();
		servers.add(server1);
		servers.add(server2);

		try {
			when(virtualizationManager.getActiveServersList()).thenReturn(
					servers);
			when(
					placementStrategy.selectDestinationServer(
							any(VirtualMachine.class), eq(servers)))
					.thenReturn(server1);
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		placementModule.consolidateAll(vmList);

		try {
			verify(placementStrategy).orderDemand(eq(vmList));
			verify(placementStrategy).orderServers(eq(servers));
			verify(virtualizationManager).setVmToServer(eq(vm1), eq(server1));
			verify(virtualizationManager).setVmToServer(eq(vm2), eq(server1));
			verify(virtualizationManager).setVmToServer(eq(vm3), eq(server1));
			verify(virtualizationManager, times(3)).setVmToServer(
					any(VirtualMachine.class), any(Server.class));

			verify(virtualizationManager, never()).getNextInactiveServer(
					any(VirtualMachine.class), any(ServerTypeChooser.class));
			verify(placementUtils, never()).lessLossEmptyServer(eq(servers),
					any(VirtualMachine.class));
		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}

	}

	@Test
	public void testAllocateInActiveServer()
			throws UnknownVirtualMachineException, UnknownServerException {
		VirtualMachine vm = mock(VirtualMachine.class);

		Server server1 = mock(Server.class);
		Server server2 = mock(Server.class);

		List<Server> servers = new ArrayList<Server>();
		servers.add(server1);
		servers.add(server2);

		try {
			when(placementStrategy.selectDestinationServer(vm, servers))
					.thenReturn(server2);
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		placementModule.allocate(vm, servers);

		try {
			verify(virtualizationManager).setVmToServer(vm, server2);
			verify(virtualizationManager).setVmToServer(
					any(VirtualMachine.class), any(Server.class));
			verify(virtualizationManager, never()).getNextInactiveServer(
					eq(vm), any(ServerTypeChooser.class));
			verify(placementUtils, never()).lessLossEmptyServer(eq(servers),
					eq(vm));
			verify(statisticsModule, never()).addToStatisticValue(
					eq(Constants.STATISTIC_VIRTUAL_MACHINES_NOT_ALLOCATED),
					eq(1));
		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test
	public void testAllocateRequiringNewServer()
			throws UnknownVirtualMachineException, UnknownServerException {
		VirtualMachine vm = mock(VirtualMachine.class);

		Server server1 = mock(Server.class);
		Server server2 = mock(Server.class);

		List<Server> servers = new ArrayList<Server>();
		servers.add(server1);

		try {
			when(placementStrategy.selectDestinationServer(vm, servers))
					.thenReturn(null);
			when(
					virtualizationManager.getNextInactiveServer(eq(vm),
							any(ServerTypeChooser.class))).thenReturn(server2);
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		placementModule.allocate(vm, servers);

		try {
			verify(virtualizationManager).setVmToServer(vm, server2);
			verify(virtualizationManager).setVmToServer(
					any(VirtualMachine.class), any(Server.class));
			verify(virtualizationManager).getNextInactiveServer(eq(vm),
					any(ServerTypeChooser.class));
			assertEquals(2, servers.size());
			assertTrue(servers.contains(server2));
			verify(placementUtils, never()).lessLossEmptyServer(eq(servers),
					eq(vm));
			verify(statisticsModule, never()).addToStatisticValue(
					eq(Constants.STATISTIC_VIRTUAL_MACHINES_NOT_ALLOCATED),
					eq(1));
		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test
	public void testAllocateNoMoreServersAvailable()
			throws UnknownVirtualMachineException, UnknownServerException {
		VirtualMachine vm = mock(VirtualMachine.class);

		Server server1 = mock(Server.class);

		List<Server> servers = new ArrayList<Server>();
		servers.add(server1);

		try {
			when(placementStrategy.selectDestinationServer(vm, servers))
					.thenReturn(null);
			when(
					virtualizationManager.getNextInactiveServer(eq(vm),
							any(ServerTypeChooser.class))).thenThrow(
					new NoMoreServersAvailableException());
			when(placementUtils.lessLossEmptyServer(servers, vm)).thenReturn(
					server1);
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		placementModule.allocate(vm, servers);

		try {
			verify(virtualizationManager).setVmToServer(vm, server1);
			verify(virtualizationManager).setVmToServer(
					any(VirtualMachine.class), any(Server.class));

			verify(virtualizationManager).getNextInactiveServer(eq(vm),
					any(ServerTypeChooser.class));

			verify(placementUtils).lessLossEmptyServer(eq(servers), eq(vm));
			verify(statisticsModule, never()).addToStatisticValue(
					eq(Constants.STATISTIC_VIRTUAL_MACHINES_NOT_ALLOCATED),
					eq(1));
		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test
	public void testDoesNotAllocate() throws UnknownVirtualMachineException,
			UnknownServerException {
		VirtualMachine vm = mock(VirtualMachine.class);

		Server server1 = mock(Server.class);

		List<Server> servers = new ArrayList<Server>();
		servers.add(server1);

		try {
			when(placementStrategy.selectDestinationServer(vm, servers))
					.thenReturn(null);
			when(
					virtualizationManager.getNextInactiveServer(eq(vm),
							any(ServerTypeChooser.class))).thenThrow(
					new NoMoreServersAvailableException());
			when(placementUtils.lessLossEmptyServer(servers, vm)).thenReturn(
					null);
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		placementModule.allocate(vm, servers);

		try {
			verify(virtualizationManager, never()).setVmToServer(
					any(VirtualMachine.class), any(Server.class));

			verify(virtualizationManager).getNextInactiveServer(eq(vm),
					any(ServerTypeChooser.class));
			verify(placementUtils).lessLossEmptyServer(eq(servers), eq(vm));
			verify(statisticsModule).addToStatisticValue(
					eq(Constants.STATISTIC_VIRTUAL_MACHINES_NOT_ALLOCATED),
					eq(1));
		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

}
