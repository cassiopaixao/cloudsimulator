package br.usp.ime.cassiop.workloadsim.placement;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import br.usp.ime.cassiop.workloadsim.StatisticsModule;
import br.usp.ime.cassiop.workloadsim.VirtualizationManager;
import br.usp.ime.cassiop.workloadsim.exceptions.DependencyNotSetException;
import br.usp.ime.cassiop.workloadsim.exceptions.NoMoreServersAvailableException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownServerException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownVirtualMachineException;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class FirstFitDecreasingTest {

	private class AddVmToServer implements Answer<Object> {
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

	@Test
	public void testConsolidateAll() throws DependencyNotSetException {
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		FirstFitDecreasing firstFitDecreasing = new FirstFitDecreasing();
		firstFitDecreasing.setStatisticsModule(statisticsModule);
		firstFitDecreasing.setVirtualizationManager(virtualizationManager);

		Server server1 = buildServer(0.5, 0.5);
		Server server2 = buildServer(1.0, 1.0);
		Server server3 = buildServer(1.0, 0.5);

		VirtualMachine vm1 = buildVirtualMachine(0.6, 0.25);
		VirtualMachine vm2 = buildVirtualMachine(0.75, 0.25);

		Collection<Server> serverList = new ArrayList<Server>();
		serverList.add(server1);
		serverList.add(server2);
		serverList.add(server3);

		List<VirtualMachine> vmList = new ArrayList<VirtualMachine>();
		vmList.add(vm1);
		vmList.add(vm2);

		try {
			when(virtualizationManager.getActiveServersList()).thenReturn(
					serverList);
			doAnswer(new AddVmToServer())
					.when(virtualizationManager)
					.setVmToServer(any(VirtualMachine.class), any(Server.class));
		} catch (Exception e) {
			fail("Exception thrown in mock configuration: " + e.getMessage());
		}

		firstFitDecreasing.consolidateAll(vmList);

		try {
			verify(virtualizationManager).setVmToServer(vm2, server2);
			verify(virtualizationManager).setVmToServer(vm1, server3);
		} catch (Exception e) {
			fail("Exception thrown verifying methods' calls: " + e.getMessage());
		}
	}

	@Test
	public void testConsolidateAllOverloadingEmptyServer()
			throws DependencyNotSetException {
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		FirstFitDecreasing firstFitDecreasing = new FirstFitDecreasing();
		firstFitDecreasing.setStatisticsModule(statisticsModule);
		firstFitDecreasing.setVirtualizationManager(virtualizationManager);

		Server server1 = buildServer(0.5, 0.25);
		Server server2 = buildServer(0.3, 1.0);

		VirtualMachine vm1 = buildVirtualMachine(0.5, 0.5);

		Collection<Server> serverList = new ArrayList<Server>();
		serverList.add(server1);
		serverList.add(server2);

		List<VirtualMachine> vmList = new ArrayList<VirtualMachine>();
		vmList.add(vm1);

		try {
			when(virtualizationManager.getActiveServersList()).thenReturn(
					serverList);
			doAnswer(new AddVmToServer())
					.when(virtualizationManager)
					.setVmToServer(any(VirtualMachine.class), any(Server.class));
			when(
					virtualizationManager.getNextInactiveServer(
							any(VirtualMachine.class),
							any(FirstFitTypeChooser.class))).thenThrow(
					new NoMoreServersAvailableException());
		} catch (Exception e) {
			fail("Exception thrown in mock configuration: " + e.getMessage());
		}

		firstFitDecreasing.consolidateAll(vmList);

		try {
			verify(virtualizationManager).setVmToServer(vm1, server2);
		} catch (Exception e) {
			fail("Exception thrown verifying methods' calls: " + e.getMessage());
		}

	}

	@Test
	public void testConsolidateAllCouldntAllocateOne()
			throws DependencyNotSetException {
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		FirstFitDecreasing firstFitDecreasing = new FirstFitDecreasing();
		firstFitDecreasing.setStatisticsModule(statisticsModule);
		firstFitDecreasing.setVirtualizationManager(virtualizationManager);

		Server server1 = buildServer(0.5, 0.25);

		VirtualMachine vm1 = buildVirtualMachine(0.25, 0.25);
		VirtualMachine vm2 = buildVirtualMachine(0.1, 0.1);

		Collection<Server> serverList = new ArrayList<Server>();
		serverList.add(server1);

		List<VirtualMachine> vmList = new ArrayList<VirtualMachine>();
		vmList.add(vm1);
		vmList.add(vm2);

		try {
			when(virtualizationManager.getActiveServersList()).thenReturn(
					serverList);
			doAnswer(new AddVmToServer())
					.when(virtualizationManager)
					.setVmToServer(any(VirtualMachine.class), any(Server.class));
			when(
					virtualizationManager.getNextInactiveServer(
							any(VirtualMachine.class),
							any(FirstFitTypeChooser.class))).thenThrow(
					new NoMoreServersAvailableException());
		} catch (Exception e) {
			fail("Exception thrown in mock configuration: " + e.getMessage());
		}

		firstFitDecreasing.consolidateAll(vmList);

		try {
			verify(virtualizationManager).setVmToServer(vm1, server1);
			verify(statisticsModule).addToStatisticValue(
					eq(Constants.STATISTIC_VIRTUAL_MACHINES_NOT_ALLOCATED),
					eq(1));
			assertNull(vm2.getCurrentServer());
		} catch (Exception e) {
			fail("Exception thrown verifying methods' calls: " + e.getMessage());
		}
	}

	@Test(expected = DependencyNotSetException.class)
	public void testConsolidateAllMissingStatisticsModule()
			throws DependencyNotSetException {
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = null;

		FirstFitDecreasing firstFitDecreasing = new FirstFitDecreasing();
		firstFitDecreasing.setStatisticsModule(statisticsModule);
		firstFitDecreasing.setVirtualizationManager(virtualizationManager);

		List<VirtualMachine> vmList = new LinkedList<VirtualMachine>();

		firstFitDecreasing.consolidateAll(vmList);
	}

	@Test(expected = DependencyNotSetException.class)
	public void testConsolidateAllMissingDemand()
			throws DependencyNotSetException {
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		FirstFitDecreasing firstFitDecreasing = new FirstFitDecreasing();
		firstFitDecreasing.setStatisticsModule(statisticsModule);
		firstFitDecreasing.setVirtualizationManager(virtualizationManager);

		List<VirtualMachine> vmList = null;

		firstFitDecreasing.consolidateAll(vmList);
	}

	@Test(expected = DependencyNotSetException.class)
	public void testConsolidateAllMissingVirtualizationManager()
			throws DependencyNotSetException {
		VirtualizationManager virtualizationManager = null;
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		FirstFitDecreasing firstFitDecreasing = new FirstFitDecreasing();
		firstFitDecreasing.setStatisticsModule(statisticsModule);
		firstFitDecreasing.setVirtualizationManager(virtualizationManager);

		List<VirtualMachine> vmList = new LinkedList<VirtualMachine>();

		firstFitDecreasing.consolidateAll(vmList);
	}

	@Test
	public void testAllocate() throws UnknownVirtualMachineException,
			UnknownServerException {
		FirstFitDecreasing firstFitDecreasing = new FirstFitDecreasing();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		Server server = mock(Server.class);
		VirtualMachine virtualMachine = mock(VirtualMachine.class);

		firstFitDecreasing.setVirtualizationManager(virtualizationManager);

		List<Server> serverList = new ArrayList<Server>();
		serverList.add(server);

		when(server.canHost(virtualMachine)).thenReturn(Boolean.TRUE);

		firstFitDecreasing.allocate(virtualMachine, serverList);

		try {
			verify(virtualizationManager).setVmToServer(virtualMachine, server);
		} catch (Exception e) {
			fail("Exception thrown verifying methods' calls: " + e.getMessage());
		}
	}

	@Test
	public void testAllocateToTheThirdServer()
			throws UnknownVirtualMachineException, UnknownServerException {
		FirstFitDecreasing firstFitDecreasing = new FirstFitDecreasing();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		Server server1 = mock(Server.class);
		Server server2 = mock(Server.class);
		Server server3 = mock(Server.class);
		Server server4 = mock(Server.class);
		VirtualMachine virtualMachine = mock(VirtualMachine.class);

		firstFitDecreasing.setVirtualizationManager(virtualizationManager);

		List<Server> serverList = new ArrayList<Server>();
		serverList.add(server1);
		serverList.add(server2);
		serverList.add(server3);
		serverList.add(server4);

		when(server1.canHost(virtualMachine)).thenReturn(Boolean.FALSE);
		when(server2.canHost(virtualMachine)).thenReturn(Boolean.FALSE);
		when(server3.canHost(virtualMachine)).thenReturn(Boolean.TRUE);
		when(server4.canHost(virtualMachine)).thenReturn(Boolean.TRUE);

		firstFitDecreasing.allocate(virtualMachine, serverList);

		try {
			verify(server1).canHost(virtualMachine);
			verify(server2).canHost(virtualMachine);
			verify(server3).canHost(virtualMachine);
			verify(server4, never()).canHost(virtualMachine);
			verify(virtualizationManager, never()).setVmToServer(
					virtualMachine, server1);
			verify(virtualizationManager, never()).setVmToServer(
					virtualMachine, server2);
			verify(virtualizationManager)
					.setVmToServer(virtualMachine, server3);
			verify(virtualizationManager, never()).setVmToServer(
					virtualMachine, server4);
		} catch (Exception e) {
			fail("Exception thrown verifying methods' calls: " + e.getMessage());
		}
	}

	@Test
	public void testAllocateWithEmptyServerList()
			throws UnknownVirtualMachineException, UnknownServerException {
		FirstFitDecreasing firstFitDecreasing = new FirstFitDecreasing();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		Server server = mock(Server.class);
		VirtualMachine virtualMachine = mock(VirtualMachine.class);

		firstFitDecreasing.setVirtualizationManager(virtualizationManager);

		List<Server> serverList = new ArrayList<Server>();

		try {
			when(
					virtualizationManager.getNextInactiveServer(
							eq(virtualMachine), any(FirstFitTypeChooser.class)))
					.thenReturn(server);
		} catch (Exception e) {
			fail("Exception thrown configuring mocks: " + e.getMessage());
		}

		firstFitDecreasing.allocate(virtualMachine, serverList);

		try {
			verify(virtualizationManager).setVmToServer(virtualMachine, server);
			verify(virtualizationManager).getNextInactiveServer(
					eq(virtualMachine), any(FirstFitTypeChooser.class));
			assertTrue(serverList.contains(server));
		} catch (Exception e) {
			fail("Exception thrown verifying methods' calls: " + e.getMessage());
		}
	}

	@Test
	public void testAllocateToNewServer()
			throws UnknownVirtualMachineException, UnknownServerException {
		FirstFitDecreasing firstFitDecreasing = new FirstFitDecreasing();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		Server server1 = mock(Server.class);
		Server server2 = mock(Server.class);
		VirtualMachine virtualMachine = mock(VirtualMachine.class);

		firstFitDecreasing.setVirtualizationManager(virtualizationManager);

		List<Server> serverList = new ArrayList<Server>();
		serverList.add(server1);

		try {
			when(server1.canHost(virtualMachine)).thenReturn(Boolean.FALSE);
			when(
					virtualizationManager.getNextInactiveServer(
							eq(virtualMachine), any(FirstFitTypeChooser.class)))
					.thenReturn(server2);
		} catch (Exception e) {
			fail("Exception thrown configuring mocks: " + e.getMessage());
		}

		firstFitDecreasing.allocate(virtualMachine, serverList);

		try {
			verify(virtualizationManager)
					.setVmToServer(virtualMachine, server2);
			verify(virtualizationManager).getNextInactiveServer(
					eq(virtualMachine), any(FirstFitTypeChooser.class));
			assertTrue(serverList.contains(server1));
			assertTrue(serverList.contains(server2));
		} catch (Exception e) {
			fail("Exception thrown verifying methods' calls: " + e.getMessage());
		}
	}

	@Test
	public void testDoesntAllocate() throws UnknownVirtualMachineException,
			UnknownServerException {
		FirstFitDecreasing firstFitDecreasing = new FirstFitDecreasing();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);
		VirtualMachine virtualMachine = mock(VirtualMachine.class);

		firstFitDecreasing.setVirtualizationManager(virtualizationManager);
		firstFitDecreasing.setStatisticsModule(statisticsModule);

		List<Server> serverList = new ArrayList<Server>();

		try {
			when(
					virtualizationManager.getNextInactiveServer(
							eq(virtualMachine), any(FirstFitTypeChooser.class)))
					.thenThrow(new NoMoreServersAvailableException());
		} catch (Exception e) {
			fail("Exception thrown configuring mocks: " + e.getMessage());
		}

		firstFitDecreasing.allocate(virtualMachine, serverList);

		try {
			verify(virtualizationManager, never()).setVmToServer(
					eq(virtualMachine), any(Server.class));
			verify(virtualizationManager).getNextInactiveServer(
					eq(virtualMachine), any(FirstFitTypeChooser.class));
			verify(statisticsModule).addToStatisticValue(
					eq(Constants.STATISTIC_VIRTUAL_MACHINES_NOT_ALLOCATED),
					eq(1));
		} catch (Exception e) {
			fail("Exception thrown verifying methods' calls: " + e.getMessage());
		}

	}

	@Test(expected = UnknownServerException.class)
	public void testAllocateToUnknownServer()
			throws UnknownVirtualMachineException, UnknownServerException {
		FirstFitDecreasing firstFitDecreasing = new FirstFitDecreasing();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		VirtualMachine virtualMachine = mock(VirtualMachine.class);
		Server server = mock(Server.class);

		firstFitDecreasing.setVirtualizationManager(virtualizationManager);

		List<Server> serverList = new ArrayList<Server>();
		serverList.add(server);

		try {
			when(server.canHost(eq(virtualMachine))).thenReturn(Boolean.TRUE);
			doThrow(new UnknownServerException()).when(virtualizationManager)
					.setVmToServer(eq(virtualMachine), eq(server));
		} catch (Exception e) {
			fail("Exception thrown configuring mocks: " + e.getMessage());
		}

		firstFitDecreasing.allocate(virtualMachine, serverList);
	}

	@Test(expected = UnknownVirtualMachineException.class)
	public void testAllocateNullVmToServer()
			throws UnknownVirtualMachineException, UnknownServerException {
		FirstFitDecreasing firstFitDecreasing = new FirstFitDecreasing();

		firstFitDecreasing.allocate(null, new ArrayList<Server>());
	}

}
