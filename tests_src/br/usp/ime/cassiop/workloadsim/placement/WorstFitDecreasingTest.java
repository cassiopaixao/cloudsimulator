package br.usp.ime.cassiop.workloadsim.placement;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import br.usp.ime.cassiop.workloadsim.StatisticsModule;
import br.usp.ime.cassiop.workloadsim.VirtualizationManager;
import br.usp.ime.cassiop.workloadsim.exceptions.DependencyNotSetException;
import br.usp.ime.cassiop.workloadsim.exceptions.NoMoreServersAvailableException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownServerException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownVirtualMachineException;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class WorstFitDecreasingTest {

	@Test
	public void testConsolidateAll() throws DependencyNotSetException {
		WorstFitDecreasing wfd = new WorstFitDecreasing();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		wfd.setVirtualizationManager(virtualizationManager);
		wfd.setStatisticsModule(statisticsModule);

		Server server1 = buildServer(0.75, 1.0);
		Server server2 = buildServer(0.5, 0.75);
		Server server3 = buildServer(1.0, 1.0);

		VirtualMachine vm1 = buildVirtualMachine(0.25, 0.75);
		VirtualMachine vm2 = buildVirtualMachine(0.75, 0.1);
		VirtualMachine vm3 = buildVirtualMachine(0.5, 0.5);

		List<Server> serverList = new ArrayList<Server>();
		serverList.add(server1);
		serverList.add(server2);
		serverList.add(server3);

		List<VirtualMachine> vmList = new ArrayList<VirtualMachine>();
		vmList.add(vm1);
		vmList.add(vm2);
		vmList.add(vm3);

		try {
			when(virtualizationManager.getActiveServersList()).thenReturn(
					serverList);
			doAnswer(new AddVmToServer())
					.when(virtualizationManager)
					.setVmToServer(any(VirtualMachine.class), any(Server.class));
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		wfd.consolidateAll(vmList);

		try {
			verify(virtualizationManager).setVmToServer(vm2, server3);
			verify(virtualizationManager).setVmToServer(vm3, server1);
			verify(virtualizationManager).setVmToServer(vm1, server2);

			verify(virtualizationManager).getActiveServersList();
			verify(virtualizationManager, never()).getNextInactiveServer(
					any(VirtualMachine.class), any(WorstFitTypeChooser.class));

		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test
	public void testConsolidateAllRequestingNewServer()
			throws DependencyNotSetException {
		WorstFitDecreasing wfd = new WorstFitDecreasing();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		wfd.setVirtualizationManager(virtualizationManager);
		wfd.setStatisticsModule(statisticsModule);

		Server server1 = buildServer(1.0, 1.0);

		VirtualMachine vm1 = buildVirtualMachine(0.25, 0.75);

		List<Server> serverList = new ArrayList<Server>();

		List<VirtualMachine> vmList = new ArrayList<VirtualMachine>();
		vmList.add(vm1);

		try {
			when(virtualizationManager.getActiveServersList()).thenReturn(
					serverList);
			when(
					virtualizationManager.getNextInactiveServer(eq(vm1),
							any(WorstFitTypeChooser.class)))
					.thenReturn(server1);
			doAnswer(new AddVmToServer())
					.when(virtualizationManager)
					.setVmToServer(any(VirtualMachine.class), any(Server.class));
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		wfd.consolidateAll(vmList);

		try {
			verify(virtualizationManager).setVmToServer(vm1, server1);

			verify(virtualizationManager).getActiveServersList();
			verify(virtualizationManager).getNextInactiveServer(eq(vm1),
					any(WorstFitTypeChooser.class));

		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test(expected = DependencyNotSetException.class)
	public void testConsolidateAllMissingDemand()
			throws DependencyNotSetException {
		WorstFitDecreasing wfd = new WorstFitDecreasing();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		wfd.setVirtualizationManager(virtualizationManager);
		wfd.setStatisticsModule(statisticsModule);

		wfd.consolidateAll(null);
	}

	@Test(expected = DependencyNotSetException.class)
	public void testConsolidateAllMissingStatisticsModule()
			throws DependencyNotSetException {
		WorstFitDecreasing wfd = new WorstFitDecreasing();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = null;

		wfd.setVirtualizationManager(virtualizationManager);
		wfd.setStatisticsModule(statisticsModule);

		wfd.consolidateAll(new LinkedList<VirtualMachine>());
	}

	@Test(expected = DependencyNotSetException.class)
	public void testConsolidateAllMissingVirtulizationManager()
			throws DependencyNotSetException {
		WorstFitDecreasing wfd = new WorstFitDecreasing();
		VirtualizationManager virtualizationManager = null;
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		wfd.setVirtualizationManager(virtualizationManager);
		wfd.setStatisticsModule(statisticsModule);

		wfd.consolidateAll(new LinkedList<VirtualMachine>());
	}

	@Test
	public void testAllocate() throws UnknownVirtualMachineException,
			UnknownServerException {
		WorstFitDecreasing wfd = new WorstFitDecreasing();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		wfd.setVirtualizationManager(virtualizationManager);
		wfd.setStatisticsModule(statisticsModule);

		VirtualMachine vm = buildVirtualMachine(0.75, 0.25);

		Server server1 = buildServer(0.25, 1.0);
		Server server2 = buildServer(1.0, 1.0);
		Server server3 = buildServer(1.0, 0.5);

		List<Server> serverList = new ArrayList<Server>();
		serverList.add(server1);
		serverList.add(server2);
		serverList.add(server3);

		wfd.allocate(vm, serverList);

		try {
			verify(virtualizationManager).setVmToServer(vm, server2);
			verify(virtualizationManager, never()).getNextInactiveServer(
					eq(vm), any(WorstFitTypeChooser.class));
		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test
	public void testAllocateOverloadEmptyServer()
			throws UnknownVirtualMachineException, UnknownServerException {
		WorstFitDecreasing wfd = new WorstFitDecreasing();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		wfd.setVirtualizationManager(virtualizationManager);
		wfd.setStatisticsModule(statisticsModule);

		VirtualMachine vm = buildVirtualMachine(0.75, 0.25);

		Server server1 = buildServer(0.25, 1.0);

		List<Server> serverList = new ArrayList<Server>();
		serverList.add(server1);

		try {
			when(
					virtualizationManager.getNextInactiveServer(eq(vm),
							any(WorstFitTypeChooser.class))).thenThrow(
					new NoMoreServersAvailableException());
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		wfd.allocate(vm, serverList);

		try {
			verify(virtualizationManager).setVmToServer(vm, server1);
			verify(virtualizationManager).getNextInactiveServer(eq(vm),
					any(WorstFitTypeChooser.class));
		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test
	public void testAllocateRequestingNewServer()
			throws UnknownVirtualMachineException, UnknownServerException {
		WorstFitDecreasing wfd = new WorstFitDecreasing();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		wfd.setVirtualizationManager(virtualizationManager);
		wfd.setStatisticsModule(statisticsModule);

		VirtualMachine vm = buildVirtualMachine(0.75, 0.25);

		Server server1 = buildServer(0.25, 1.0);

		List<Server> serverList = new ArrayList<Server>();

		try {
			when(
					virtualizationManager.getNextInactiveServer(eq(vm),
							any(WorstFitTypeChooser.class)))
					.thenReturn(server1);
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		wfd.allocate(vm, serverList);

		try {
			verify(virtualizationManager).setVmToServer(vm, server1);
			verify(virtualizationManager).getNextInactiveServer(eq(vm),
					any(WorstFitTypeChooser.class));
			assertTrue(serverList.contains(server1));
		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test
	public void testDoesntAllocate() throws UnknownVirtualMachineException,
			UnknownServerException {
		WorstFitDecreasing wfd = new WorstFitDecreasing();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		wfd.setVirtualizationManager(virtualizationManager);
		wfd.setStatisticsModule(statisticsModule);

		VirtualMachine vm = buildVirtualMachine(0.75, 0.25);

		List<Server> serverList = new ArrayList<Server>();

		try {
			when(
					virtualizationManager.getNextInactiveServer(eq(vm),
							any(WorstFitTypeChooser.class))).thenThrow(
					new NoMoreServersAvailableException());
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		wfd.allocate(vm, serverList);

		try {
			verify(virtualizationManager, never()).setVmToServer(eq(vm),
					any(Server.class));
			verify(virtualizationManager).getNextInactiveServer(eq(vm),
					any(WorstFitTypeChooser.class));
			verify(statisticsModule).addToStatisticValue(
					eq(Constants.STATISTIC_VIRTUAL_MACHINES_NOT_ALLOCATED),
					eq(1));
			assertTrue(serverList.isEmpty());
		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}
}
