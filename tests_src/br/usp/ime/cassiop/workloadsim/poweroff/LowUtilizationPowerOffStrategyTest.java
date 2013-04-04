package br.usp.ime.cassiop.workloadsim.poweroff;

import static br.usp.ime.cassiop.workloadsim.util.TestUtils.failConfiguringMocks;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.failVerifyingMethodsCalls;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import br.usp.ime.cassiop.workloadsim.PlacementModule;
import br.usp.ime.cassiop.workloadsim.StatisticsModule;
import br.usp.ime.cassiop.workloadsim.VirtualizationManager;
import br.usp.ime.cassiop.workloadsim.exceptions.DependencyNotSetException;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class LowUtilizationPowerOffStrategyTest {

	@SuppressWarnings("unchecked")
	@Test
	public void testPowerOffLowUtilizationServers()
			throws DependencyNotSetException {
		LowUtilizationPowerOffStrategy powerOffStrategy = new LowUtilizationPowerOffStrategy();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);
		PlacementModule placementModule = mock(PlacementModule.class);

		powerOffStrategy.setVirtualizationManager(virtualizationManager);
		powerOffStrategy.setStatisticsModule(statisticsModule);
		powerOffStrategy.setPlacementModule(placementModule);
		powerOffStrategy.setLowUtilization(0.25);

		Server server1 = mock(Server.class);
		Server server2 = mock(Server.class);

		List<Server> servers = new ArrayList<Server>();
		servers.add(server1);
		servers.add(server2);

		try {
			when(server1.getResourceUtilization()).thenReturn(0.15);
			when(server2.getResourceUtilization()).thenReturn(0.25);
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		powerOffStrategy.powerOff(servers);

		try {
			verify(virtualizationManager).turnOffServer(server1);
			verify(virtualizationManager).turnOffServer(server2);
			verify(statisticsModule).setStatisticValue(
					eq(Constants.STATISTIC_SERVERS_TURNED_OFF), eq(2));
			verify(placementModule, never()).allocate(
					any(VirtualMachine.class), any(List.class));
		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDoesntPowerOffServers() throws DependencyNotSetException {
		LowUtilizationPowerOffStrategy powerOffStrategy = new LowUtilizationPowerOffStrategy();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);
		PlacementModule placementModule = mock(PlacementModule.class);

		powerOffStrategy.setVirtualizationManager(virtualizationManager);
		powerOffStrategy.setStatisticsModule(statisticsModule);
		powerOffStrategy.setPlacementModule(placementModule);
		powerOffStrategy.setLowUtilization(0.25);

		Server server1 = mock(Server.class);
		Server server2 = mock(Server.class);

		List<Server> servers = new ArrayList<Server>();
		servers.add(server1);
		servers.add(server2);

		List<VirtualMachine> nonEmptyVmList = new ArrayList<VirtualMachine>();
		nonEmptyVmList.add(new VirtualMachine());

		try {
			when(server1.getResourceUtilization()).thenReturn(0.5);
			when(server2.getResourceUtilization()).thenReturn(0.3);
			when(server1.getVirtualMachines()).thenReturn(nonEmptyVmList);
			when(server2.getVirtualMachines()).thenReturn(nonEmptyVmList);
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		powerOffStrategy.powerOff(servers);

		try {
			verify(virtualizationManager, never()).turnOffServer(server1);
			verify(virtualizationManager, never()).turnOffServer(server2);
			verify(statisticsModule).setStatisticValue(
					eq(Constants.STATISTIC_SERVERS_TURNED_OFF), eq(0));
			verify(placementModule, never()).allocate(
					any(VirtualMachine.class), any(List.class));
		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPowerOffNonemptyServers() throws DependencyNotSetException {
		LowUtilizationPowerOffStrategy powerOffStrategy = new LowUtilizationPowerOffStrategy();
		VirtualizationManager virtualizationManager = mock(VirtualizationManager.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);
		PlacementModule placementModule = mock(PlacementModule.class);

		powerOffStrategy.setVirtualizationManager(virtualizationManager);
		powerOffStrategy.setStatisticsModule(statisticsModule);
		powerOffStrategy.setPlacementModule(placementModule);
		powerOffStrategy.setLowUtilization(0.25);

		Server server1 = mock(Server.class);

		VirtualMachine vm1 = mock(VirtualMachine.class);
		VirtualMachine vm2 = mock(VirtualMachine.class);

		List<Server> servers = new ArrayList<Server>();
		servers.add(server1);

		final List<VirtualMachine> nonEmptyVmList = new ArrayList<VirtualMachine>();
		nonEmptyVmList.add(vm1);
		nonEmptyVmList.add(vm2);

		try {
			when(server1.getResourceUtilization()).thenReturn(0.2);
			when(server1.getVirtualMachines()).thenReturn(nonEmptyVmList);
			doAnswer(new Answer<Object>() {

				@Override
				public Object answer(InvocationOnMock invocation)
						throws Throwable {
					nonEmptyVmList.remove((VirtualMachine) invocation
							.getArguments()[0]);
					return null;
				}
			}).when(virtualizationManager)
					.deallocate(any(VirtualMachine.class));
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		powerOffStrategy.powerOff(servers);

		try {
			verify(virtualizationManager).turnOffServer(server1);
			verify(virtualizationManager).deallocate(vm1);
			verify(virtualizationManager).deallocate(vm2);

			verify(placementModule).allocate(eq(vm1), any(List.class));
			verify(placementModule).allocate(eq(vm2), any(List.class));
			verify(placementModule, times(2)).allocate(
					any(VirtualMachine.class), any(List.class));

			verify(statisticsModule).setStatisticValue(
					eq(Constants.STATISTIC_SERVERS_TURNED_OFF), eq(1));
		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

}
