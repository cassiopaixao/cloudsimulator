package br.usp.ime.cassiop.workloadsim.migrationcontrol;

import static br.usp.ime.cassiop.workloadsim.util.TestUtils.buildServer;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.buildVirtualMachine;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.failConfiguringInitialState;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.failConfiguringMocks;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.failVerifyingMethodsCalls;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import br.usp.ime.cassiop.workloadsim.StatisticsModule;
import br.usp.ime.cassiop.workloadsim.VirtualizationManager;
import br.usp.ime.cassiop.workloadsim.exceptions.DependencyNotSetException;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;
import br.usp.ime.cassiop.workloadsim.util.TestUtils.RemoveVmFromServer;

public class NoMigrationControlTest {

	@Test
	public void testControl() throws DependencyNotSetException {
		NoMigrationControl migrationControl = new NoMigrationControl();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		migrationControl.setStatisticsModule(statisticsModule);
		migrationControl.setVirtualizationManager(virtualizationManager);

		Server server1 = buildServer(1.0, 1.0);

		VirtualMachine vm1 = buildVirtualMachine("1", 0.25, 0.25);
		VirtualMachine vm2 = buildVirtualMachine("2", 0.25, 0.25);
		VirtualMachine vm3 = buildVirtualMachine("3", 0.25, 0.25);

		VirtualMachine newVm1 = buildVirtualMachine("1", 0.1, 0.1);
		VirtualMachine newVm2 = buildVirtualMachine("2", 0.25, 0.25);
		VirtualMachine newVm3 = buildVirtualMachine("3", 0.5, 0.5);

		List<VirtualMachine> demand = new ArrayList<VirtualMachine>();
		demand.add(newVm1);
		demand.add(newVm2);
		demand.add(newVm3);

		HashMap<String, VirtualMachine> vmMap = new HashMap<String, VirtualMachine>();
		vmMap.put(vm1.getName(), vm1);
		vmMap.put(vm2.getName(), vm2);
		vmMap.put(vm3.getName(), vm3);

		try {
			server1.addVirtualMachine(vm1);
			server1.addVirtualMachine(vm2);
			server1.addVirtualMachine(vm3);
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		try {
			when(virtualizationManager.getActiveVirtualMachines()).thenReturn(
					vmMap);
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

			verify(statisticsModule).setStatisticValue(
					eq(Constants.STATISTIC_VIRTUAL_MACHINES_TO_REALLOCATE),
					eq(3));

		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test
	public void testControlKeepingNewVms() throws DependencyNotSetException {
		NoMigrationControl migrationControl = new NoMigrationControl();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		migrationControl.setStatisticsModule(statisticsModule);
		migrationControl.setVirtualizationManager(virtualizationManager);

		Server server1 = buildServer(1.0, 1.0);

		VirtualMachine vm1 = buildVirtualMachine("1", 0.25, 0.25);

		VirtualMachine newVm1 = buildVirtualMachine("1", 0.1, 0.1);
		VirtualMachine newVm2 = buildVirtualMachine("2", 0.25, 0.25);
		VirtualMachine newVm3 = buildVirtualMachine("3", 0.5, 0.5);

		List<VirtualMachine> demand = new ArrayList<VirtualMachine>();
		demand.add(newVm1);
		demand.add(newVm2);
		demand.add(newVm3);

		HashMap<String, VirtualMachine> vmMap = new HashMap<String, VirtualMachine>();
		vmMap.put(vm1.getName(), vm1);

		try {
			server1.addVirtualMachine(vm1);
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		try {
			when(virtualizationManager.getActiveVirtualMachines()).thenReturn(
					vmMap);
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

			verify(virtualizationManager).deallocate(any(VirtualMachine.class));
			for (VirtualMachine vm : result) {
				assertNull(vm.getCurrentServer());
			}

		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

}
