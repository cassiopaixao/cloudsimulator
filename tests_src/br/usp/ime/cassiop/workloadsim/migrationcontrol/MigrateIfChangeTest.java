package br.usp.ime.cassiop.workloadsim.migrationcontrol;

import static br.usp.ime.cassiop.workloadsim.util.TestUtils.buildServer;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.buildVirtualMachine;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.failConfiguringInitialState;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.failConfiguringMocks;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import br.usp.ime.cassiop.workloadsim.StatisticsModule;
import br.usp.ime.cassiop.workloadsim.VirtualizationManager;
import br.usp.ime.cassiop.workloadsim.exceptions.DependencyNotSetException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownVirtualMachineException;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;
import br.usp.ime.cassiop.workloadsim.util.TestUtils.RemoveVmFromServer;

public class MigrateIfChangeTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testControlWithoutReallocation()
			throws DependencyNotSetException {
		MigrateIfChange migrationControl = new MigrateIfChange();

		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		migrationControl.setStatisticsModule(statisticsModule);
		migrationControl.setVirtualizationManager(virtualizationManager);

		VirtualMachine vm1 = buildVirtualMachine("vm1", 0.5, 0.75);
		VirtualMachine vm2 = buildVirtualMachine("vm2", 0.2, 0.1);
		VirtualMachine vm3 = buildVirtualMachine("vm3", 0.1, 0.1);

		VirtualMachine newVm1 = buildVirtualMachine("vm1", 0.5, 0.75);
		VirtualMachine newVm2 = buildVirtualMachine("vm2", 0.2, 0.1);
		VirtualMachine newVm3 = buildVirtualMachine("vm3", 0.1, 0.1);

		Server server1 = buildServer(1.0, 1.0);

		try {
			server1.addVirtualMachine(vm1);
			server1.addVirtualMachine(vm2);
			server1.addVirtualMachine(vm3);
		} catch (UnknownVirtualMachineException e) {
			failConfiguringInitialState(e);
		}

		HashMap<String, VirtualMachine> vmMap = new HashMap<String, VirtualMachine>();
		vmMap.put(vm1.getName(), vm1);
		vmMap.put(vm2.getName(), vm2);
		vmMap.put(vm3.getName(), vm3);

		List<VirtualMachine> demand = new ArrayList<VirtualMachine>();
		demand.add(newVm1);
		demand.add(newVm2);
		demand.add(newVm3);

		when(virtualizationManager.getActiveVirtualMachines())
				.thenReturn(vmMap);

		List<VirtualMachine> result = migrationControl.control(demand);

		assertTrue(result.isEmpty());
		verify(statisticsModule).setStatisticValue(
				eq(Constants.STATISTIC_VIRTUAL_MACHINES_TO_REALLOCATE), eq(0));
	}

	@Test
	public void testControlReallocatingAll() throws DependencyNotSetException {
		MigrateIfChange migrationControl = new MigrateIfChange();

		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		migrationControl.setStatisticsModule(statisticsModule);
		migrationControl.setVirtualizationManager(virtualizationManager);

		VirtualMachine vm1 = buildVirtualMachine("vm1", 0.5, 0.75);
		VirtualMachine vm2 = buildVirtualMachine("vm2", 0.2, 0.1);
		VirtualMachine vm3 = buildVirtualMachine("vm3", 0.2, 0.1);

		VirtualMachine newVm1 = buildVirtualMachine("vm1", 0.5, 0.5);
		VirtualMachine newVm2 = buildVirtualMachine("vm2", 0.2, 0.2);
		VirtualMachine newVm3 = buildVirtualMachine("vm3", 0.1, 0.1);

		Server server1 = buildServer(1.0, 1.0);

		try {
			server1.addVirtualMachine(vm1);
			server1.addVirtualMachine(vm2);
			server1.addVirtualMachine(vm3);
		} catch (UnknownVirtualMachineException e) {
			failConfiguringInitialState(e);
		}

		HashMap<String, VirtualMachine> vmMap = new HashMap<String, VirtualMachine>();
		vmMap.put(vm1.getName(), vm1);
		vmMap.put(vm2.getName(), vm2);
		vmMap.put(vm3.getName(), vm3);

		List<VirtualMachine> demand = new ArrayList<VirtualMachine>();
		demand.add(newVm1);
		demand.add(newVm2);
		demand.add(newVm3);

		try {
			when(virtualizationManager.getActiveVirtualMachines())
			.thenReturn(vmMap);
			doAnswer(new RemoveVmFromServer()).when(virtualizationManager)
					.deallocate(any(VirtualMachine.class));
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		List<VirtualMachine> result = migrationControl.control(demand);

		assertTrue(result.contains(newVm1));
		assertTrue(result.contains(newVm2));
		assertTrue(result.contains(newVm3));
		assertTrue(server1.getVirtualMachines().isEmpty());
		verify(statisticsModule).setStatisticValue(
				eq(Constants.STATISTIC_VIRTUAL_MACHINES_TO_REALLOCATE), eq(3));
		for (VirtualMachine vm : result) {
			assertNull(vm.getCurrentServer());
		}
	}
	
	@Test
	public void testControlDoesntRemoveNewVms() throws DependencyNotSetException {
		MigrateIfChange migrationControl = new MigrateIfChange();

		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		migrationControl.setStatisticsModule(statisticsModule);
		migrationControl.setVirtualizationManager(virtualizationManager);

		VirtualMachine vm1 = buildVirtualMachine("vm1", 0.5, 0.75);

		VirtualMachine newVm1 = buildVirtualMachine("vm1", 0.5, 0.75);
		VirtualMachine newVm2 = buildVirtualMachine("vm2", 0.2, 0.2);

		Server server1 = buildServer(1.0, 1.0);

		try {
			server1.addVirtualMachine(vm1);
		} catch (UnknownVirtualMachineException e) {
			failConfiguringInitialState(e);
		}

		HashMap<String, VirtualMachine> vmMap = new HashMap<String, VirtualMachine>();
		vmMap.put(vm1.getName(), vm1);

		List<VirtualMachine> demand = new ArrayList<VirtualMachine>();
		demand.add(newVm1);
		demand.add(newVm2);

		try {
			when(virtualizationManager.getActiveVirtualMachines())
			.thenReturn(vmMap);
			doAnswer(new RemoveVmFromServer()).when(virtualizationManager)
					.deallocate(any(VirtualMachine.class));
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		List<VirtualMachine> result = migrationControl.control(demand);

		assertTrue(result.contains(newVm2));
	}

}
