package br.usp.ime.cassiop.workloadsim.placement;

import static br.usp.ime.cassiop.workloadsim.util.TestUtils.buildServer;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.buildVirtualMachine;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.failConfiguringMocks;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.failVerifyingMethodsCalls;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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

public class AlmostWorstFitTest {

	@Test
	public void testConsolidateAll() throws DependencyNotSetException {
		AlmostWorstFit awf = new AlmostWorstFit();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		awf.setVirtualizationManager(virtualizationManager);
		awf.setStatisticsModule(statisticsModule);

		Server server1 = buildServer(1.0, 1.0);
		Server server2 = buildServer(0.5, 0.5);
		Server server3 = buildServer(1.0, 0.5);

		VirtualMachine vm1 = buildVirtualMachine(0.25, 0.25);
		VirtualMachine vm2 = buildVirtualMachine(0.75, 0.25);
		VirtualMachine vm3 = buildVirtualMachine(0.25, 0.25);

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

		awf.consolidateAll(vmList);

		try {
			verify(virtualizationManager).setVmToServer(eq(vm1), eq(server3));
			verify(virtualizationManager).setVmToServer(eq(vm2), eq(server3));
			verify(virtualizationManager).setVmToServer(eq(vm3), eq(server2));
			verify(virtualizationManager, never()).getNextInactiveServer(
					any(VirtualMachine.class), any(ServerTypeChooser.class));
		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test
	public void testAllocate() throws UnknownVirtualMachineException,
			UnknownServerException {
		AlmostWorstFit awf = new AlmostWorstFit();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		awf.setVirtualizationManager(virtualizationManager);
		awf.setStatisticsModule(statisticsModule);

		Server server1 = buildServer(1.0, 1.0);
		Server server2 = buildServer(0.5, 0.5);
		Server server3 = buildServer(1.0, 0.5);

		VirtualMachine vm = buildVirtualMachine(0.25, 0.25);

		List<Server> serverList = new ArrayList<Server>();
		serverList.add(server1);
		serverList.add(server2);
		serverList.add(server3);

		awf.allocate(vm, serverList);

		try {
			verify(virtualizationManager).setVmToServer(eq(vm), eq(server3));
			verify(virtualizationManager, never()).getNextInactiveServer(
					any(VirtualMachine.class), any(ServerTypeChooser.class));
		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test
	public void testAllocateOverloadingEmptyServer()
			throws UnknownVirtualMachineException, UnknownServerException {
		AlmostWorstFit awf = new AlmostWorstFit();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		awf.setVirtualizationManager(virtualizationManager);
		awf.setStatisticsModule(statisticsModule);

		Server server1 = buildServer(0.25, 0.25);
		Server server2 = buildServer(0.5, 0.5);
		Server server3 = buildServer(0.5, 0.25);

		VirtualMachine vm = buildVirtualMachine(0.75, 0.25);

		List<Server> serverList = new ArrayList<Server>();
		serverList.add(server1);
		serverList.add(server2);
		serverList.add(server3);

		try {
			when(
					virtualizationManager.getNextInactiveServer(eq(vm),
							any(AlmostWorstFitTypeChooser.class))).thenThrow(
					new NoMoreServersAvailableException());
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		awf.allocate(vm, serverList);

		try {
			verify(virtualizationManager).setVmToServer(vm, server2);
			verify(virtualizationManager).getNextInactiveServer(eq(vm),
					any(AlmostWorstFitTypeChooser.class));
		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test
	public void testAllocateRequestingNewServer()
			throws UnknownVirtualMachineException, UnknownServerException {
		AlmostWorstFit awf = new AlmostWorstFit();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		awf.setVirtualizationManager(virtualizationManager);
		awf.setStatisticsModule(statisticsModule);

		Server server1 = buildServer(0.50, 0.75);

		VirtualMachine vm = buildVirtualMachine(0.5, 0.5);

		List<Server> serverList = new ArrayList<Server>();

		try {
			when(
					virtualizationManager.getNextInactiveServer(eq(vm),
							any(AlmostWorstFitTypeChooser.class))).thenReturn(
					server1);
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		awf.allocate(vm, serverList);

		try {
			verify(virtualizationManager).setVmToServer(vm, server1);
			verify(virtualizationManager).getNextInactiveServer(eq(vm),
					any(AlmostWorstFitTypeChooser.class));
		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test
	public void testDoesntAllocate() throws UnknownVirtualMachineException,
			UnknownServerException {
		AlmostWorstFit awf = new AlmostWorstFit();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		awf.setVirtualizationManager(virtualizationManager);
		awf.setStatisticsModule(statisticsModule);

		VirtualMachine vm = buildVirtualMachine(0.5, 0.5);

		List<Server> serverList = new ArrayList<Server>();

		try {
			when(
					virtualizationManager.getNextInactiveServer(eq(vm),
							any(AlmostWorstFitTypeChooser.class))).thenThrow(
					new NoMoreServersAvailableException());
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		awf.allocate(vm, serverList);

		try {
			verify(virtualizationManager, never()).setVmToServer(
					any(VirtualMachine.class), any(Server.class));
			verify(virtualizationManager).getNextInactiveServer(eq(vm),
					any(AlmostWorstFitTypeChooser.class));
			verify(statisticsModule).addToStatisticValue(
					eq(Constants.STATISTIC_VIRTUAL_MACHINES_NOT_ALLOCATED),
					eq(1));
		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}
}
