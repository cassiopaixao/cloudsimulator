package br.usp.ime.cassiop.workloadsim.placement;

import static br.usp.ime.cassiop.workloadsim.util.TestUtils.buildServer;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.buildVirtualMachine;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.failConfiguringMocks;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.failVerifyingMethodsCalls;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import br.usp.ime.cassiop.workloadsim.ServerTypeChooser;
import br.usp.ime.cassiop.workloadsim.StatisticsModule;
import br.usp.ime.cassiop.workloadsim.VirtualizationManager;
import br.usp.ime.cassiop.workloadsim.exceptions.DependencyNotSetException;
import br.usp.ime.cassiop.workloadsim.exceptions.NoMoreServersAvailableException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownServerException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownVirtualMachineException;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;
import br.usp.ime.cassiop.workloadsim.util.TestUtils.AddVmToServer;

public class BestFitDecreasingTest {

	@Test
	public void testConsolidateAll() throws DependencyNotSetException {
		BestFitDecreasing bfd = new BestFitDecreasing();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		bfd.setVirtualizationManager(virtualizationManager);
		bfd.setStatisticsModule(statisticsModule);

		Server server1 = buildServer(1.0, 1.0);
		Server server2 = buildServer(0.5, 0.5);
		Server server3 = buildServer(1.0, 0.5);

		VirtualMachine vm1 = buildVirtualMachine(0.75, 0.25);
		VirtualMachine vm2 = buildVirtualMachine(0.25, 0.25);
		VirtualMachine vm3 = buildVirtualMachine(0.5, 0.25);

		List<Server> servers = new ArrayList<Server>();
		servers.add(server1);
		servers.add(server2);
		servers.add(server3);

		List<VirtualMachine> vmList = new ArrayList<VirtualMachine>();
		vmList.add(vm1);
		vmList.add(vm2);
		vmList.add(vm3);

		try {
			when(virtualizationManager.getActiveServersList()).thenReturn(
					servers);
			doAnswer(new AddVmToServer())
					.when(virtualizationManager)
					.setVmToServer(any(VirtualMachine.class), any(Server.class));
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		bfd.consolidateAll(vmList);

		try {
			verify(virtualizationManager).getActiveServersList();
			verify(virtualizationManager, never()).getNextInactiveServer(
					any(VirtualMachine.class), any(BestFitTypeChooser.class));
			verify(virtualizationManager).setVmToServer(vm1, server3);
			verify(virtualizationManager).setVmToServer(vm3, server2);
			verify(virtualizationManager).setVmToServer(vm2, server3);
			verify(virtualizationManager, times(3)).setVmToServer(
					any(VirtualMachine.class), any(Server.class));
		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}

	}

	@Test
	public void testConsolidateAllRequestingNewMachine()
			throws DependencyNotSetException {
		BestFitDecreasing bfd = new BestFitDecreasing();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		bfd.setVirtualizationManager(virtualizationManager);
		bfd.setStatisticsModule(statisticsModule);

		Server server1 = buildServer(1.0, 1.0);

		VirtualMachine vm1 = buildVirtualMachine(0.75, 0.25);

		List<Server> servers = new ArrayList<Server>();

		List<VirtualMachine> vmList = new ArrayList<VirtualMachine>();
		vmList.add(vm1);

		try {
			when(virtualizationManager.getActiveServersList()).thenReturn(
					servers);
			when(
					virtualizationManager.getNextInactiveServer(eq(vm1),
							any(FirstFitTypeChooser.class)))
					.thenReturn(server1);
			doAnswer(new AddVmToServer())
					.when(virtualizationManager)
					.setVmToServer(any(VirtualMachine.class), any(Server.class));
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		bfd.consolidateAll(vmList);

		try {
			verify(virtualizationManager).getActiveServersList();
			verify(virtualizationManager).getNextInactiveServer(
					any(VirtualMachine.class), any(FirstFitTypeChooser.class));
			verify(virtualizationManager).setVmToServer(vm1, server1);
		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test(expected = DependencyNotSetException.class)
	public void testConsolidateAllMissingDemand()
			throws DependencyNotSetException {
		BestFitDecreasing bfd = new BestFitDecreasing();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		bfd.setVirtualizationManager(virtualizationManager);
		bfd.setStatisticsModule(statisticsModule);

		bfd.consolidateAll(null);
	}

	@Test(expected = DependencyNotSetException.class)
	public void testConsolidateAllMissingVirtualizationManager()
			throws DependencyNotSetException {
		BestFitDecreasing bfd = new BestFitDecreasing();
		VirtualizationManager virtualizationManager = null;
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		bfd.setVirtualizationManager(virtualizationManager);

		bfd.setStatisticsModule(statisticsModule);
		bfd.consolidateAll(new LinkedList<VirtualMachine>());
	}

	@Test(expected = DependencyNotSetException.class)
	public void testConsolidateAllMissingStatisticsModule()
			throws DependencyNotSetException {
		BestFitDecreasing bfd = new BestFitDecreasing();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = null;

		bfd.setVirtualizationManager(virtualizationManager);
		bfd.setStatisticsModule(statisticsModule);

		bfd.consolidateAll(new LinkedList<VirtualMachine>());
	}

	@Test
	public void testAllocate() throws UnknownVirtualMachineException,
			UnknownServerException {
		BestFitDecreasing bfd = new BestFitDecreasing();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		bfd.setVirtualizationManager(virtualizationManager);
		bfd.setStatisticsModule(statisticsModule);

		VirtualMachine vm = buildVirtualMachine(0.75, 0.5);

		Server server1 = buildServer(1.0, 1.0);
		Server server2 = buildServer(1.0, 0.5);
		Server server3 = buildServer(0.5, 0.5);

		List<Server> serverList = new ArrayList<Server>();
		serverList.add(server1);
		serverList.add(server2);
		serverList.add(server3);

		bfd.allocate(vm, serverList);

		try {
			verify(virtualizationManager).setVmToServer(vm, server2);
			verify(virtualizationManager, never()).getNextInactiveServer(
					any(VirtualMachine.class), any(ServerTypeChooser.class));
		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test
	public void testAllocateRequestingNewServer()
			throws UnknownVirtualMachineException, UnknownServerException {
		BestFitDecreasing bfd = new BestFitDecreasing();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		bfd.setVirtualizationManager(virtualizationManager);
		bfd.setStatisticsModule(statisticsModule);

		VirtualMachine vm = buildVirtualMachine(0.75, 0.5);

		Server server1 = buildServer(1.0, 0.5);

		List<Server> serverList = new ArrayList<Server>();

		List<Server> serversTypeList = new ArrayList<Server>();
		serversTypeList.add(server1);

		try {
			when(
					virtualizationManager.getNextInactiveServer(eq(vm),
							any(BestFitTypeChooser.class))).thenReturn(server1);
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		bfd.allocate(vm, serverList);

		try {
			verify(virtualizationManager).getNextInactiveServer(eq(vm),
					any(BestFitTypeChooser.class));
			verify(virtualizationManager).setVmToServer(vm, server1);
			assertTrue(serverList.contains(server1));
		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test
	public void testAllocateToEmptyServerEvenOverloading()
			throws UnknownVirtualMachineException, UnknownServerException {
		BestFitDecreasing bfd = new BestFitDecreasing();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		bfd.setVirtualizationManager(virtualizationManager);
		bfd.setStatisticsModule(statisticsModule);

		VirtualMachine vm = buildVirtualMachine(0.75, 0.5);

		Server server1 = buildServer(0.5, 0.5);

		List<Server> serverList = new ArrayList<Server>();
		serverList.add(server1);

		try {
			when(
					virtualizationManager.getNextInactiveServer(eq(vm),
							any(BestFitTypeChooser.class))).thenThrow(
					new NoMoreServersAvailableException());
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		bfd.allocate(vm, serverList);

		try {
			verify(virtualizationManager).getNextInactiveServer(eq(vm),
					any(BestFitTypeChooser.class));
			verify(virtualizationManager).setVmToServer(vm, server1);
		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test
	public void testDoesntAllocate() throws UnknownVirtualMachineException,
			UnknownServerException {
		BestFitDecreasing bfd = new BestFitDecreasing();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		bfd.setVirtualizationManager(virtualizationManager);
		bfd.setStatisticsModule(statisticsModule);

		VirtualMachine vm = buildVirtualMachine(0.75, 0.5);

		List<Server> serverList = new ArrayList<Server>();

		try {
			when(
					virtualizationManager.getNextInactiveServer(eq(vm),
							any(BestFitTypeChooser.class))).thenThrow(
					new NoMoreServersAvailableException());
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		bfd.allocate(vm, serverList);

		try {
			verify(virtualizationManager).getNextInactiveServer(eq(vm),
					any(BestFitTypeChooser.class));
			verify(virtualizationManager, never()).setVmToServer(
					any(VirtualMachine.class), any(Server.class));
			verify(statisticsModule).addToStatisticValue(
					eq(Constants.STATISTIC_VIRTUAL_MACHINES_NOT_ALLOCATED),
					eq(1));
			assertTrue(serverList.isEmpty());
		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

}
