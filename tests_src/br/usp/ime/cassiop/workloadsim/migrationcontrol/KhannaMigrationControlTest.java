package br.usp.ime.cassiop.workloadsim.migrationcontrol;

import static br.usp.ime.cassiop.workloadsim.util.TestUtils.buildServer;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.buildVirtualMachine;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.failConfiguringInitialState;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.failConfiguringMocks;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.failVerifyingMethodsCalls;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import br.usp.ime.cassiop.workloadsim.StatisticsModule;
import br.usp.ime.cassiop.workloadsim.VirtualizationManager;
import br.usp.ime.cassiop.workloadsim.exceptions.DependencyNotSetException;
import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;
import br.usp.ime.cassiop.workloadsim.util.TestUtils.RemoveVmFromServer;

public class KhannaMigrationControlTest {

	@Test
	public void testControl() throws DependencyNotSetException {
		KhannaMigrationControl migrationControl = new KhannaMigrationControl();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		migrationControl.setStatisticsModule(statisticsModule);
		migrationControl.setVirtualizationManager(virtualizationManager);

		Server server1 = buildServer(1.0, 1.0);
		server1.setKneePerformanceLoss(ResourceType.CPU, 0.7);
		server1.setKneePerformanceLoss(ResourceType.MEMORY, 0.7);

		VirtualMachine vm1 = buildVirtualMachine("1", 0.25, 0.25);
		VirtualMachine vm2 = buildVirtualMachine("2", 0.2, 0.2);
		VirtualMachine vm3 = buildVirtualMachine("3", 0.15, 0.15);

		VirtualMachine newVm1 = buildVirtualMachine("1", 0.1, 0.1);
		VirtualMachine newVm2 = buildVirtualMachine("2", 0.25, 0.25);
		VirtualMachine newVm3 = buildVirtualMachine("3", 0.5, 0.5);

		List<VirtualMachine> demand = new ArrayList<VirtualMachine>();
		demand.add(newVm1);
		demand.add(newVm2);
		demand.add(newVm3);

		List<Server> activeServerList = new ArrayList<Server>();
		activeServerList.add(server1);

		try {
			server1.addVirtualMachine(vm1);
			server1.addVirtualMachine(vm2);
			server1.addVirtualMachine(vm3);
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		try {
			when(virtualizationManager.getActiveServersList()).thenReturn(
					activeServerList);
			doAnswer(new RemoveVmFromServer()).when(virtualizationManager)
					.deallocate(any(VirtualMachine.class));
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		List<VirtualMachine> result = migrationControl.control(demand);

		try {
			assertTrue(result.contains(newVm1));
			assertTrue(result.contains(newVm2));
			assertFalse(result.contains(newVm3));

			verify(virtualizationManager, times(2)).deallocate(
					any(VirtualMachine.class));

			for (VirtualMachine vm : result) {
				assertNull(vm.getCurrentServer());
				assertNotNull(vm.getLastServer());
			}

			verify(statisticsModule, times(2)).addToStatisticValue(
					eq(Constants.STATISTIC_VIRTUAL_MACHINES_TO_REALLOCATE),
					eq(1));

		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test
	public void testControlReallocateAll() throws DependencyNotSetException {
		KhannaMigrationControl migrationControl = new KhannaMigrationControl();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		migrationControl.setStatisticsModule(statisticsModule);
		migrationControl.setVirtualizationManager(virtualizationManager);

		Server server1 = buildServer(1.0, 1.0);
		server1.setKneePerformanceLoss(ResourceType.CPU, 0.7);
		server1.setKneePerformanceLoss(ResourceType.MEMORY, 0.7);

		VirtualMachine vm1 = buildVirtualMachine("1", 0.25, 0.25);
		VirtualMachine vm2 = buildVirtualMachine("2", 0.2, 0.2);
		VirtualMachine vm3 = buildVirtualMachine("3", 0.15, 0.15);

		VirtualMachine newVm1 = buildVirtualMachine("1", 0.1, 0.1);
		VirtualMachine newVm2 = buildVirtualMachine("2", 0.25, 0.25);
		VirtualMachine newVm3 = buildVirtualMachine("3", 0.75, 0.5);

		List<VirtualMachine> demand = new ArrayList<VirtualMachine>();
		demand.add(newVm1);
		demand.add(newVm2);
		demand.add(newVm3);

		List<Server> activeServerList = new ArrayList<Server>();
		activeServerList.add(server1);

		try {
			server1.addVirtualMachine(vm1);
			server1.addVirtualMachine(vm2);
			server1.addVirtualMachine(vm3);
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		try {
			when(virtualizationManager.getActiveServersList()).thenReturn(
					activeServerList);
			doAnswer(new RemoveVmFromServer()).when(virtualizationManager)
					.deallocate(any(VirtualMachine.class));
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		List<VirtualMachine> result = migrationControl.control(demand);

		try {
			assertTrue(result.contains(newVm1));
			assertTrue(result.contains(newVm2));
			assertTrue(result.contains(newVm3));

			verify(virtualizationManager, times(3)).deallocate(
					any(VirtualMachine.class));

			for (VirtualMachine vm : result) {
				assertNull(vm.getCurrentServer());
				assertNotNull(vm.getLastServer());
			}

			verify(statisticsModule, times(3)).addToStatisticValue(
					eq(Constants.STATISTIC_VIRTUAL_MACHINES_TO_REALLOCATE),
					eq(1));

		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test
	public void testControlDoesntReallocate() throws DependencyNotSetException {
		KhannaMigrationControl migrationControl = new KhannaMigrationControl();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		migrationControl.setStatisticsModule(statisticsModule);
		migrationControl.setVirtualizationManager(virtualizationManager);

		Server server1 = buildServer(1.0, 1.0);
		server1.setKneePerformanceLoss(ResourceType.CPU, 0.7);
		server1.setKneePerformanceLoss(ResourceType.MEMORY, 0.7);

		VirtualMachine vm1 = buildVirtualMachine("1", 0.25, 0.25);
		VirtualMachine vm2 = buildVirtualMachine("2", 0.2, 0.2);
		VirtualMachine vm3 = buildVirtualMachine("3", 0.15, 0.15);

		VirtualMachine newVm1 = buildVirtualMachine("1", 0.1, 0.1);
		VirtualMachine newVm2 = buildVirtualMachine("2", 0.25, 0.25);
		VirtualMachine newVm3 = buildVirtualMachine("3", 0.25, 0.2);

		List<VirtualMachine> demand = new ArrayList<VirtualMachine>();
		demand.add(newVm1);
		demand.add(newVm2);
		demand.add(newVm3);

		List<Server> activeServerList = new ArrayList<Server>();
		activeServerList.add(server1);

		try {
			server1.addVirtualMachine(vm1);
			server1.addVirtualMachine(vm2);
			server1.addVirtualMachine(vm3);
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		try {
			when(virtualizationManager.getActiveServersList()).thenReturn(
					activeServerList);
			doAnswer(new RemoveVmFromServer()).when(virtualizationManager)
					.deallocate(any(VirtualMachine.class));
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		List<VirtualMachine> result = migrationControl.control(demand);

		try {
			assertTrue(result.isEmpty());

			verify(virtualizationManager, never()).deallocate(
					any(VirtualMachine.class));

			for (VirtualMachine vm : result) {
				assertNull(vm.getCurrentServer());
				assertNotNull(vm.getLastServer());
			}

			verify(statisticsModule, never()).addToStatisticValue(
					eq(Constants.STATISTIC_VIRTUAL_MACHINES_TO_REALLOCATE),
					eq(1));

		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test
	public void testControlKeepNewVms() throws DependencyNotSetException {
		KhannaMigrationControl migrationControl = new KhannaMigrationControl();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		migrationControl.setStatisticsModule(statisticsModule);
		migrationControl.setVirtualizationManager(virtualizationManager);

		Server server1 = buildServer(1.0, 1.0);
		server1.setKneePerformanceLoss(ResourceType.CPU, 0.7);
		server1.setKneePerformanceLoss(ResourceType.MEMORY, 0.7);

		VirtualMachine vm1 = buildVirtualMachine("1", 0.25, 0.25);

		VirtualMachine newVm1 = buildVirtualMachine("1", 0.1, 0.1);
		VirtualMachine newVm2 = buildVirtualMachine("2", 0.25, 0.25);
		VirtualMachine newVm3 = buildVirtualMachine("3", 0.25, 0.2);

		List<VirtualMachine> demand = new ArrayList<VirtualMachine>();
		demand.add(newVm1);
		demand.add(newVm2);
		demand.add(newVm3);

		List<Server> activeServerList = new ArrayList<Server>();
		activeServerList.add(server1);

		try {
			server1.addVirtualMachine(vm1);
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		try {
			when(virtualizationManager.getActiveServersList()).thenReturn(
					activeServerList);
			doAnswer(new RemoveVmFromServer()).when(virtualizationManager)
					.deallocate(any(VirtualMachine.class));
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		List<VirtualMachine> result = migrationControl.control(demand);

		try {
			assertTrue(result.contains(newVm2));
			assertTrue(result.contains(newVm3));

			verify(statisticsModule, never()).addToStatisticValue(
					eq(Constants.STATISTIC_VIRTUAL_MACHINES_TO_REALLOCATE),
					eq(1));

		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}
}
