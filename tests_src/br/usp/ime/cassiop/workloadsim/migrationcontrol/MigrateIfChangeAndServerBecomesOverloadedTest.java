package br.usp.ime.cassiop.workloadsim.migrationcontrol;

import static br.usp.ime.cassiop.workloadsim.util.TestUtils.buildServer;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.buildVirtualMachine;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.failConfiguringInitialState;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.failConfiguringMocks;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.failVerifyingMethodsCalls;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import br.usp.ime.cassiop.workloadsim.util.TestUtils.RemoveVmFromServer;

public class MigrateIfChangeAndServerBecomesOverloadedTest {

	@Test
	public void testControlMigratingAllVMs() throws DependencyNotSetException {
		MigrateIfChangeAndServerBecomesOverloaded migrationControl = new MigrateIfChangeAndServerBecomesOverloaded();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		migrationControl.setStatisticsModule(statisticsModule);
		migrationControl.setVirtualizationManager(virtualizationManager);

		Server server1 = buildServer(0.5, 0.5);
		Server server2 = buildServer(0.75, 0.5);

		VirtualMachine vm1 = buildVirtualMachine("1", 0.25, 0.25);
		VirtualMachine vm2 = buildVirtualMachine("2", 0.25, 0.25);
		VirtualMachine vm3 = buildVirtualMachine("3", 0.25, 0.25);

		VirtualMachine newVm1 = buildVirtualMachine("1", 0.8, 0.8);
		VirtualMachine newVm2 = buildVirtualMachine("2", 0.8, 0.8);
		VirtualMachine newVm3 = buildVirtualMachine("3", 0.8, 0.8);

		// List<Server> serverList = new ArrayList<Server>();
		// serverList.add(server1);
		// serverList.add(server2);

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
			server2.addVirtualMachine(vm3);
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		try {
			// when(virtualizationManager.getActiveServersList());
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

		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test
	public void testControlWithoutMigration() throws DependencyNotSetException {
		MigrateIfChangeAndServerBecomesOverloaded migrationControl = new MigrateIfChangeAndServerBecomesOverloaded();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		migrationControl.setStatisticsModule(statisticsModule);
		migrationControl.setVirtualizationManager(virtualizationManager);

		Server server1 = buildServer(0.5, 0.5);
		Server server2 = buildServer(0.75, 0.5);

		VirtualMachine vm1 = buildVirtualMachine("1", 0.25, 0.25);
		VirtualMachine vm2 = buildVirtualMachine("2", 0.25, 0.25);
		VirtualMachine vm3 = buildVirtualMachine("3", 0.25, 0.25);

		VirtualMachine newVm1 = buildVirtualMachine("1", 0.2, 0.1);
		VirtualMachine newVm2 = buildVirtualMachine("2", 0.25, 0.25);
		VirtualMachine newVm3 = buildVirtualMachine("3", 0.5, 0.5);

		// List<Server> serverList = new ArrayList<Server>();
		// serverList.add(server1);
		// serverList.add(server2);

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
			server2.addVirtualMachine(vm3);
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		try {
			// when(virtualizationManager.getActiveServersList());
			when(virtualizationManager.getActiveVirtualMachines()).thenReturn(
					vmMap);
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

		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test
	public void testControlKeepingNewVms() throws DependencyNotSetException {
		MigrateIfChangeAndServerBecomesOverloaded migrationControl = new MigrateIfChangeAndServerBecomesOverloaded();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		migrationControl.setStatisticsModule(statisticsModule);
		migrationControl.setVirtualizationManager(virtualizationManager);

		Server server1 = buildServer(0.5, 0.5);

		VirtualMachine vm1 = buildVirtualMachine("1", 0.25, 0.25);

		VirtualMachine newVm1 = buildVirtualMachine("1", 0.25, 0.3);
		VirtualMachine newVm2 = buildVirtualMachine("2", 0.5, 0.5);
		VirtualMachine newVm3 = buildVirtualMachine("3", 0.5, 0.5);

		// List<Server> serverList = new ArrayList<Server>();
		// serverList.add(server1);
		// serverList.add(server2);

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
			// when(virtualizationManager.getActiveServersList());
			when(virtualizationManager.getActiveVirtualMachines()).thenReturn(
					vmMap);
			doAnswer(new RemoveVmFromServer()).when(virtualizationManager)
					.deallocate(any(VirtualMachine.class));
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		List<VirtualMachine> result = migrationControl.control(demand);

		try {
			assertFalse(result.contains(newVm1));
			assertTrue(result.contains(newVm2));
			assertTrue(result.contains(newVm3));

			verify(virtualizationManager, never()).deallocate(
					any(VirtualMachine.class));
			for (VirtualMachine vm : result) {
				assertNull(vm.getCurrentServer());
			}

		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}
}
