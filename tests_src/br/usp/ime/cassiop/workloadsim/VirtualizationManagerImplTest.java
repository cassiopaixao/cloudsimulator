package br.usp.ime.cassiop.workloadsim;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import br.usp.ime.cassiop.workloadsim.exceptions.IncompatibleObjectsException;
import br.usp.ime.cassiop.workloadsim.exceptions.InvalidParameterException;
import br.usp.ime.cassiop.workloadsim.exceptions.NoMoreServersAvailableException;
import br.usp.ime.cassiop.workloadsim.exceptions.ServerNotEmptyException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownServerException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownVirtualMachineException;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class VirtualizationManagerImplTest {

	@Test
	public void testGetActiveVirtualMachines() {
		VirtualizationManager virtMan = new VirtualizationManagerImpl();

		assertTrue(virtMan.getActiveServersList().isEmpty());
	}

	@Test
	public void testVirtualizationManagerImpl() {
		VirtualizationManager virtMan = new VirtualizationManagerImpl();

		assertTrue(virtMan.getActiveServersList().isEmpty());
		assertTrue(virtMan.getActiveVirtualMachines().isEmpty());
	}

	@Test
	public void testSetVmToServerOk() throws UnknownVirtualMachineException,
			UnknownServerException {
		VirtualizationManager virtMan = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		virtMan.setEnvironment(environment);
		virtMan.setStatisticsModule(statisticsModule);

		// http://gojko.net/2009/10/23/mockito-in-six-easy-examples/
		// http://schuchert.wikispaces.com/Mockito.LoginServiceExample

		Server server1 = buildServer(1.0, 1.0);
		VirtualMachine vm1 = buildVirtualMachine(0.1, 0.1);

		// configuring mock;
		try {
			when(environment.getMachineOfType(server1)).thenReturn(
					buildServer(1.0, 1.0));
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		try {
			server1 = virtMan.activateServerOfType(server1);
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		virtMan.setVmToServer(vm1, server1);

		try {
			assertTrue(server1.getVirtualMachines().contains(vm1));
			assertTrue(virtMan.getActiveServersList().contains(server1));
			assertEquals(virtMan.getActiveVirtualMachines().get(vm1.getName()),
					vm1);
			verify(statisticsModule, never()).addToStatisticValue(
					eq(Constants.STATISTIC_MIGRATIONS), anyInt());
			verify(statisticsModule, never()).addToStatisticValue(
					eq(Constants.STATISTIC_MIGRATIONS_COST), anyDouble());

		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}

	}

	@Test
	public void testSetVmToServerMigrationPreviousDeallocation()
			throws UnknownVirtualMachineException, UnknownServerException {
		VirtualizationManager virtMan = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		virtMan.setEnvironment(environment);
		virtMan.setStatisticsModule(statisticsModule);

		// http://gojko.net/2009/10/23/mockito-in-six-easy-examples/
		// http://schuchert.wikispaces.com/Mockito.LoginServiceExample

		Server server1 = buildServer(1.0, 1.0);
		VirtualMachine vm1 = buildVirtualMachine(0.1, 0.1);
		vm1.setLastServer(buildServer(0.5, 0.5));

		// configuring mock;
		try {
			when(environment.getMachineOfType(server1)).thenReturn(
					buildServer(1.0, 1.0));
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		try {
			server1 = virtMan.activateServerOfType(server1);
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		virtMan.setVmToServer(vm1, server1);

		try {
			assertTrue(server1.getVirtualMachines().contains(vm1));
			assertTrue(virtMan.getActiveServersList().contains(server1));
			assertEquals(virtMan.getActiveVirtualMachines().get(vm1.getName()),
					vm1);
			verify(statisticsModule, times(1)).addToStatisticValue(
					eq(Constants.STATISTIC_MIGRATIONS), eq(1));
			verify(statisticsModule, times(1)).addToStatisticValue(
					eq(Constants.STATISTIC_MIGRATIONS_COST),
					eq(vm1.getResourceUtilization()));

		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}

	}

	@Test
	public void testSetVmToServerUnknownServerException() {
		VirtualizationManager virtMan = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		virtMan.setEnvironment(environment);
		virtMan.setStatisticsModule(statisticsModule);

		// http://gojko.net/2009/10/23/mockito-in-six-easy-examples/
		// http://schuchert.wikispaces.com/Mockito.LoginServiceExample

		Server server1 = buildServer("1", 1.0, 1.0);
		Server server2 = buildServer("2", 0.5, 0.5);
		Server falseServer1 = buildServer("1", 0.75, 0.75);

		// configuring mock;
		try {
			when(environment.getMachineOfType(server1)).thenReturn(
					buildServer(1.0, 1.0));
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		try {
			server1 = virtMan.activateServerOfType(server1);
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		// FIXME should refactor tests
		try {
			virtMan.setVmToServer(buildVirtualMachine(0.1, 0.1), server2);
			fail("Server 2 doesn't exist in environment.");
		} catch (UnknownVirtualMachineException e) {
			fail("A valid virtual machine was provided.");
		} catch (UnknownServerException e) {
			assertNotNull("server2 is not active.", e);
		}

		try {
			virtMan.setVmToServer(buildVirtualMachine(0.1, 0.1), falseServer1);
			fail("Server 1 is different from the \"Server 1\" in argument.");
		} catch (UnknownVirtualMachineException e) {
			fail("A valid virtual machine was provided.");
		} catch (UnknownServerException e) {
			assertNotNull("Server in argument should be different.", e);
		}

		try {
			virtMan.setVmToServer(buildVirtualMachine(0.1, 0.1), null);
			fail("A null server shouldn't be accepted.");
		} catch (UnknownVirtualMachineException e) {
			fail("A valid virtual machine was provided.");
		} catch (UnknownServerException e) {
			assertNotNull("A null server shouldn't be accepted.", e);
		}

	}

	@Test(expected = UnknownVirtualMachineException.class)
	public void testSetVmToServerUnknownVirtualMachineException()
			throws UnknownVirtualMachineException, UnknownServerException {
		VirtualizationManager virtMan = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		virtMan.setEnvironment(environment);
		virtMan.setStatisticsModule(statisticsModule);

		// http://gojko.net/2009/10/23/mockito-in-six-easy-examples/
		// http://schuchert.wikispaces.com/Mockito.LoginServiceExample

		Server server1 = buildServer("1", 1.0, 1.0);
		Server server2 = buildServer("2", 1.0, 1.0);

		// configuring mock;
		try {
			when(environment.getMachineOfType(server1)).thenReturn(
					buildServer(1.0, 1.0));
			when(environment.getMachineOfType(server2)).thenReturn(
					buildServer(1.0, 1.0));
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		try {
			server1 = virtMan.activateServerOfType(server1);
			server2 = virtMan.activateServerOfType(server2);
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		virtMan.setVmToServer(null, server1);
	}

	@Test
	public void testSetVmToServerMigrationOnDemmandDeallocation()
			throws UnknownVirtualMachineException, UnknownServerException {
		VirtualizationManager virtMan = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		virtMan.setEnvironment(environment);
		virtMan.setStatisticsModule(statisticsModule);

		// http://gojko.net/2009/10/23/mockito-in-six-easy-examples/
		// http://schuchert.wikispaces.com/Mockito.LoginServiceExample

		Server server1 = buildServer("1", 1.0, 1.0);
		Server server2 = buildServer("2", 1.0, 1.0);

		VirtualMachine vm = buildVirtualMachine(0.1, 0.1);

		// configuring mock;
		try {
			when(environment.getMachineOfType(server1)).thenReturn(
					buildServer(1.0, 1.0));
			when(environment.getMachineOfType(server2)).thenReturn(
					buildServer(1.0, 1.0));
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		try {
			server1 = virtMan.activateServerOfType(server1);
			server2 = virtMan.activateServerOfType(server2);

			virtMan.setVmToServer(vm, server1);
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		virtMan.setVmToServer(vm, server2);

		try {
			assertFalse(server1.getVirtualMachines().contains(vm));
			assertTrue(server2.getVirtualMachines().contains(vm));

			assertEquals(server1, vm.getLastServer());
			assertEquals(server2, vm.getCurrentServer());

			verify(statisticsModule, times(1)).addToStatisticValue(
					eq(Constants.STATISTIC_MIGRATIONS), eq(1));
			verify(statisticsModule, times(1)).addToStatisticValue(
					eq(Constants.STATISTIC_MIGRATIONS_COST),
					eq(vm.getResourceUtilization()));

		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test
	public void testSetVmToServerMigrationOnDemmandNotNeeded()
			throws UnknownVirtualMachineException, UnknownServerException {
		VirtualizationManager virtMan = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		virtMan.setEnvironment(environment);
		virtMan.setStatisticsModule(statisticsModule);

		// http://gojko.net/2009/10/23/mockito-in-six-easy-examples/
		// http://schuchert.wikispaces.com/Mockito.LoginServiceExample

		Server server1 = buildServer("1", 1.0, 1.0);

		VirtualMachine vm = buildVirtualMachine(0.1, 0.1);

		// configuring mock;
		try {
			when(environment.getMachineOfType(server1)).thenReturn(
					buildServer(1.0, 1.0));
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		try {
			server1 = virtMan.activateServerOfType(server1);
			virtMan.setVmToServer(vm, server1);
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		virtMan.setVmToServer(vm, server1);

		try {
			assertTrue(server1.getVirtualMachines().contains(vm));
			assertEquals(server1, vm.getCurrentServer());
			assertNotSame(server1, vm.getLastServer());

			verify(statisticsModule, never()).addToStatisticValue(
					eq(Constants.STATISTIC_MIGRATIONS), anyInt());
			verify(statisticsModule, never()).addToStatisticValue(
					eq(Constants.STATISTIC_MIGRATIONS_COST), anyDouble());

		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test
	public void testActivateServerOfType() throws UnknownServerException,
			NoMoreServersAvailableException {
		VirtualizationManager virtMan = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		virtMan.setEnvironment(environment);
		virtMan.setStatisticsModule(statisticsModule);

		// http://gojko.net/2009/10/23/mockito-in-six-easy-examples/
		// http://schuchert.wikispaces.com/Mockito.LoginServiceExample

		Server server1 = buildServer("1", 1.0, 1.0);
		Server server2 = buildServer("2", 0.75, 0.75);
		Server server3 = buildServer("3", 0.5, 0.5);

		// configuring mock;
		try {
			when(environment.getMachineOfType(server1)).thenReturn(
					buildServer(1.0, 1.0));
			when(environment.getMachineOfType(server2)).thenThrow(
					new NoMoreServersAvailableException());
			when(environment.getMachineOfType(server3)).thenThrow(
					new UnknownServerException());
			when(environment.getMachineOfType(null)).thenThrow(
					new UnknownServerException());
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		server1 = virtMan.activateServerOfType(server1);

		assertTrue(virtMan.getActiveServersList().contains(server1));
	}

	@Test(expected = UnknownServerException.class)
	public void testActivateServerOfTypeRequestingAnUnexistentServer()
			throws UnknownServerException, NoMoreServersAvailableException {
		VirtualizationManager virtMan = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		virtMan.setEnvironment(environment);
		virtMan.setStatisticsModule(statisticsModule);

		Server server1 = buildServer("1", 1.0, 1.0);

		try {
			when(environment.getMachineOfType(server1)).thenThrow(
					new UnknownServerException());
			when(environment.getMachineOfType(null)).thenThrow(
					new UnknownServerException());
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		server1 = virtMan.activateServerOfType(server1);
	}

	@Test(expected = UnknownServerException.class)
	public void testActivateServerOfTypeRequestingANullServer()
			throws UnknownServerException, NoMoreServersAvailableException {
		VirtualizationManager virtMan = new VirtualizationManagerImpl();

		virtMan.activateServerOfType(null);
	}

	@Test(expected = NoMoreServersAvailableException.class)
	public void testActivateServerOfTypeRequestingAServerWithNoMoreMachinesAvailable()
			throws NoMoreServersAvailableException, UnknownServerException {
		VirtualizationManager virtMan = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		virtMan.setEnvironment(environment);
		virtMan.setStatisticsModule(statisticsModule);

		Server server1 = buildServer(1.0, 1.0);

		try {
			when(environment.getMachineOfType(server1)).thenThrow(
					new NoMoreServersAvailableException());
			when(environment.getMachineOfType(null)).thenThrow(
					new UnknownServerException());
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		virtMan.activateServerOfType(server1);
	}

	@Test
	public void testGetNextInactiveServerVirtualMachine()
			throws NoMoreServersAvailableException {
		VirtualizationManager virtualizationManager = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		ServerTypeChooser serverTypeChooser = mock(ServerTypeChooser.class);

		virtualizationManager.setEnvironment(environment);

		Server server1 = buildServer(1.0, 1.0);
		Server server2 = buildServer(1.0, 1.0);
		VirtualMachine vmDemand = buildVirtualMachine(0.5, 0.5);
		List<Server> serverList = new ArrayList<Server>();
		serverList.add(server1);

		try {
			when(environment.getAvailableMachineTypes()).thenReturn(serverList);
			when(serverTypeChooser.chooseServerType(vmDemand, serverList))
					.thenReturn(server1);
			when(environment.getMachineOfType(server1)).thenReturn(server2);
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		Server result = null;
		try {
			result = virtualizationManager.getNextInactiveServer(vmDemand,
					serverTypeChooser);
		} finally {
			try {
				assertNotSame(result, server1);
				assertTrue(server1.getType().equals(result.getType()));

				verify(environment).getAvailableMachineTypes();
				verify(environment).getMachineOfType(server1);
				verify(serverTypeChooser)
						.chooseServerType(vmDemand, serverList);
				verify(serverTypeChooser, never())
						.chooseServerTypeEvenOverloading(vmDemand, serverList);
			} catch (Exception e) {
				failVerifyingMethodsCalls(e);
			}
		}
	}

	@Test
	public void testGetNextInactiveServerVirtualMachineOverloadingMachine()
			throws NoMoreServersAvailableException {
		VirtualizationManager virtualizationManager = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		ServerTypeChooser serverTypeChooser = mock(ServerTypeChooser.class);

		virtualizationManager.setEnvironment(environment);

		Server server1 = buildServer(0.5, 0.5);
		Server server2 = buildServer(0.5, 0.5);
		VirtualMachine vmDemand = buildVirtualMachine(1.0, 1.0);
		List<Server> serverList = new ArrayList<Server>();
		serverList.add(server1);

		try {
			when(environment.getAvailableMachineTypes()).thenReturn(serverList);
			when(serverTypeChooser.chooseServerType(vmDemand, serverList))
					.thenReturn(null);
			when(
					serverTypeChooser.chooseServerTypeEvenOverloading(vmDemand,
							serverList)).thenReturn(server1);
			when(environment.getMachineOfType(server1)).thenReturn(server2);
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		Server result = null;
		try {
			result = virtualizationManager.getNextInactiveServer(vmDemand,
					serverTypeChooser);
		} finally {
			try {
				assertNotSame(result, server1);
				assertTrue(server1.getType().equals(result.getType()));

				verify(environment, times(2)).getAvailableMachineTypes();
				verify(environment).getMachineOfType(server1);
				verify(serverTypeChooser)
						.chooseServerType(vmDemand, serverList);
				verify(serverTypeChooser).chooseServerTypeEvenOverloading(
						vmDemand, serverList);
			} catch (Exception e) {
				failVerifyingMethodsCalls(e);
			}
		}
	}

	@Test(expected = NoMoreServersAvailableException.class)
	public void testGetNextInactiveServerVirtualMachineWithoutAvailableMachines()
			throws NoMoreServersAvailableException {
		VirtualizationManager virtualizationManager = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		ServerTypeChooser serverTypeChooser = mock(ServerTypeChooser.class);

		virtualizationManager.setEnvironment(environment);

		Server server1 = buildServer(1.0, 1.0);
		VirtualMachine vmDemand = buildVirtualMachine(0.5, 0.5);
		List<Server> serverList = new ArrayList<Server>();
		serverList.add(server1);

		try {
			when(environment.getAvailableMachineTypes()).thenReturn(serverList);
			when(serverTypeChooser.chooseServerType(vmDemand, serverList))
					.thenReturn(server1);
			when(environment.getMachineOfType(server1)).thenThrow(
					new NoMoreServersAvailableException());
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		virtualizationManager
				.getNextInactiveServer(vmDemand, serverTypeChooser);
	}

	@Test(expected = NoMoreServersAvailableException.class)
	public void testGetNextInactiveServerVirtualMachineWithNoMoreMachinesAvailable()
			throws NoMoreServersAvailableException {
		VirtualizationManager virtualizationManager = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		ServerTypeChooser serverTypeChooser = mock(ServerTypeChooser.class);

		virtualizationManager.setEnvironment(environment);

		VirtualMachine vmDemand = buildVirtualMachine(0.5, 0.5);
		List<Server> serverList = new ArrayList<Server>();

		try {
			when(environment.getAvailableMachineTypes()).thenReturn(serverList);
			when(serverTypeChooser.chooseServerType(vmDemand, serverList))
					.thenReturn(null);
			when(
					serverTypeChooser.chooseServerTypeEvenOverloading(vmDemand,
							serverList)).thenReturn(null);
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		try {
			virtualizationManager.getNextInactiveServer(vmDemand,
					serverTypeChooser);
		} finally {
			try {
				verify(environment, times(2)).getAvailableMachineTypes();
				verify(environment, never())
						.getMachineOfType(any(Server.class));
				verify(serverTypeChooser)
						.chooseServerType(vmDemand, serverList);
				verify(serverTypeChooser).chooseServerTypeEvenOverloading(
						vmDemand, serverList);
			} catch (Exception e) {
				failVerifyingMethodsCalls(e);
			}
		}
	}

	@Test
	public void testClear() {
		VirtualizationManager virtualizationManager = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		Server server1 = mock(Server.class);
		Server server2 = mock(Server.class);

		virtualizationManager.setEnvironment(environment);

		try {
			when(environment.getMachineOfType(server1)).thenReturn(server2);
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		try {
			virtualizationManager.activateServerOfType(server1);
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		virtualizationManager.clear();

		verify(environment).clear();
		verify(server1, never()).clear();
		verify(server2).clear();

	}

	@Test
	public void testSetParameters() throws InvalidParameterException {
		VirtualizationManager virtualizationManager = new VirtualizationManagerImpl();

		StatisticsModule statisticsModule = mock(StatisticsModule.class);
		Environment environment = mock(Environment.class);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.PARAMETER_ENVIRONMENT, environment);
		parameters.put(Constants.PARAMETER_STATISTICS_MODULE, statisticsModule);

		virtualizationManager.setParameters(parameters);
	}

	@Test(expected = InvalidParameterException.class)
	public void testSetParametersMissingEnvironment()
			throws InvalidParameterException {
		VirtualizationManager virtualizationManager = new VirtualizationManagerImpl();

		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.PARAMETER_STATISTICS_MODULE, statisticsModule);

		virtualizationManager.setParameters(parameters);
	}

	@Test(expected = InvalidParameterException.class)
	public void testSetParametersMissingStatisticsModule()
			throws InvalidParameterException {
		VirtualizationManager virtualizationManager = new VirtualizationManagerImpl();

		Environment environment = mock(Environment.class);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.PARAMETER_ENVIRONMENT, environment);

		virtualizationManager.setParameters(parameters);
	}

	@Test
	public void testDontDeallocateVmInDemmand() {
		VirtualizationManager virtualizationManager = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);
		Server server = mock(Server.class);
		VirtualMachine vm1 = mock(VirtualMachine.class);

		List<VirtualMachine> serverVms = new ArrayList<VirtualMachine>();
		serverVms.add(vm1);

		List<VirtualMachine> remainingVms = new ArrayList<VirtualMachine>();
		remainingVms.add(vm1);

		virtualizationManager.setEnvironment(environment);
		virtualizationManager.setStatisticsModule(statisticsModule);

		try {
			when(environment.getMachineOfType(server)).thenReturn(server);
			when(server.getVirtualMachines()).thenReturn(serverVms);
			when(server.canHost(any(VirtualMachine.class))).thenReturn(
					Boolean.TRUE);
			when(vm1.getEndTime()).thenReturn(Long.valueOf(200));
			when(vm1.getName()).thenReturn("1-0");
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		try {
			virtualizationManager.activateServerOfType(server);
			virtualizationManager.setVmToServer(vm1, server);
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		virtualizationManager.deallocateFinishedVms(remainingVms, 200);

		try {
			verify(server).getVirtualMachines();
			verify(server, never()).removeVirtualMachine(vm1);
			verify(vm1, never()).getEndTime();

		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test
	public void testDeallocateFinishedVmThatIsNotInDemand() {
		VirtualizationManager virtualizationManager = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);
		Server server = mock(Server.class);
		VirtualMachine vm1 = mock(VirtualMachine.class);

		List<VirtualMachine> serverVms = new ArrayList<VirtualMachine>();
		serverVms.add(vm1);

		List<VirtualMachine> remainingVms = new ArrayList<VirtualMachine>();

		virtualizationManager.setEnvironment(environment);
		virtualizationManager.setStatisticsModule(statisticsModule);

		try {
			when(environment.getMachineOfType(server)).thenReturn(server);
			when(server.getVirtualMachines()).thenReturn(serverVms);
			when(server.canHost(any(VirtualMachine.class))).thenReturn(
					Boolean.TRUE);
			when(vm1.getEndTime()).thenReturn(Long.valueOf(200));
			when(vm1.getName()).thenReturn("1-0");
			when(vm1.getCurrentServer()).thenReturn(server);
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		try {
			virtualizationManager.activateServerOfType(server);
			virtualizationManager.setVmToServer(vm1, server);
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		virtualizationManager.deallocateFinishedVms(remainingVms, 200);

		try {
			verify(server).getVirtualMachines();
			verify(server).removeVirtualMachine(vm1);
			verify(vm1).getEndTime();

		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test
	public void testDontDeallocateVmThatDoesntEndRunning() {
		VirtualizationManager virtualizationManager = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);
		Server server = mock(Server.class);
		VirtualMachine vm1 = mock(VirtualMachine.class);

		List<VirtualMachine> serverVms = new ArrayList<VirtualMachine>();
		serverVms.add(vm1);

		List<VirtualMachine> remainingVms = new ArrayList<VirtualMachine>();

		virtualizationManager.setEnvironment(environment);
		virtualizationManager.setStatisticsModule(statisticsModule);

		try {
			when(environment.getMachineOfType(server)).thenReturn(server);
			when(server.getVirtualMachines()).thenReturn(serverVms);
			when(server.canHost(any(VirtualMachine.class))).thenReturn(
					Boolean.TRUE);
			when(vm1.getEndTime()).thenReturn(Long.valueOf(300));
			when(vm1.getName()).thenReturn("1-0");
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		try {
			virtualizationManager.activateServerOfType(server);
			virtualizationManager.setVmToServer(vm1, server);
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		virtualizationManager.deallocateFinishedVms(remainingVms, 200);

		try {
			verify(server).getVirtualMachines();
			verify(server, never()).removeVirtualMachine(vm1);
			verify(vm1).getEndTime();

		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test
	public void testDeallocate() throws UnknownVirtualMachineException,
			UnknownServerException {
		VirtualizationManager virtualizationManager = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		Server server = mock(Server.class);
		VirtualMachine vm1 = mock(VirtualMachine.class);

		List<VirtualMachine> serverVms = new ArrayList<VirtualMachine>();
		serverVms.add(vm1);

		virtualizationManager.setEnvironment(environment);

		try {
			when(environment.getMachineOfType(server)).thenReturn(server);
			when(server.getVirtualMachines()).thenReturn(serverVms);
			when(server.canHost(any(VirtualMachine.class))).thenReturn(
					Boolean.TRUE);
			when(vm1.getName()).thenReturn("1-0");
			when(vm1.getCurrentServer()).thenReturn(server);
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		try {
			virtualizationManager.activateServerOfType(server);
			virtualizationManager.setVmToServer(vm1, server);
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		virtualizationManager.deallocate(vm1);

		try {
			verify(server).removeVirtualMachine(vm1);

		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}

	@Test(expected = UnknownVirtualMachineException.class)
	public void testDeallocateNullVm() throws UnknownVirtualMachineException,
			UnknownServerException {
		VirtualizationManager virtualizationManager = new VirtualizationManagerImpl();

		virtualizationManager.deallocate(null);
	}

	@Test(expected = UnknownServerException.class)
	public void testDeallocateVmWithANullServer()
			throws UnknownVirtualMachineException, UnknownServerException {
		VirtualizationManager virtualizationManager = new VirtualizationManagerImpl();
		VirtualMachine vm = mock(VirtualMachine.class);

		when(vm.getCurrentServer()).thenReturn(null);

		virtualizationManager.deallocate(vm);
	}

	@Test(expected = UnknownServerException.class)
	public void testDeallocateVmWithAUnknownServer()
			throws UnknownVirtualMachineException, UnknownServerException {
		VirtualizationManager virtualizationManager = new VirtualizationManagerImpl();
		VirtualMachine vm = mock(VirtualMachine.class);
		Server server = mock(Server.class);

		when(vm.getCurrentServer()).thenReturn(server);
		when(server.getName()).thenReturn("1");

		virtualizationManager.deallocate(vm);
	}

	@Test(expected = UnknownVirtualMachineException.class)
	public void testDeallocateVmFromServerThatDoesntHaveTheVm()
			throws UnknownVirtualMachineException, UnknownServerException {
		VirtualizationManager virtualizationManager = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		Server server = mock(Server.class);
		VirtualMachine vm1 = mock(VirtualMachine.class);
		VirtualMachine vm2 = mock(VirtualMachine.class);

		List<VirtualMachine> serverVms = new ArrayList<VirtualMachine>();
		serverVms.add(vm1);

		virtualizationManager.setEnvironment(environment);

		try {
			when(environment.getMachineOfType(server)).thenReturn(server);
			when(server.getVirtualMachines()).thenReturn(serverVms);
			when(server.canHost(any(VirtualMachine.class))).thenReturn(
					Boolean.TRUE);
			doThrow(new UnknownVirtualMachineException()).when(server)
					.removeVirtualMachine(vm2);
			when(vm1.getName()).thenReturn("1-0");
			when(vm2.getName()).thenReturn("2-0");
			when(vm1.getCurrentServer()).thenReturn(server);
			when(vm2.getCurrentServer()).thenReturn(server);
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		try {
			virtualizationManager.activateServerOfType(server);
			virtualizationManager.setVmToServer(vm1, server);
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		virtualizationManager.deallocate(vm2);
	}

	@Test
	public void testTurnOffServer() throws UnknownServerException,
			ServerNotEmptyException, NoMoreServersAvailableException {
		VirtualizationManager virtualizationManager = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);
		Server server = mock(Server.class);

		List<VirtualMachine> serverVms = new ArrayList<VirtualMachine>();

		virtualizationManager.setEnvironment(environment);
		virtualizationManager.setStatisticsModule(statisticsModule);

		try {
			when(environment.getMachineOfType(server)).thenReturn(server);
			when(server.getVirtualMachines()).thenReturn(serverVms);
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		try {
			virtualizationManager.activateServerOfType(server);
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		virtualizationManager.turnOffServer(server);

		verify(server).getVirtualMachines();
		verify(environment).turnOffMachineOfType(server);
		assertTrue(virtualizationManager.getActiveServersList().isEmpty());
	}

	@Test(expected = ServerNotEmptyException.class)
	public void testTurnOffANonemptyServer() throws UnknownServerException,
			ServerNotEmptyException, NoMoreServersAvailableException {
		VirtualizationManager virtualizationManager = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		Server server = mock(Server.class);
		VirtualMachine vm1 = mock(VirtualMachine.class);

		List<VirtualMachine> serverVms = new ArrayList<VirtualMachine>();
		serverVms.add(vm1);

		virtualizationManager.setEnvironment(environment);

		try {
			when(environment.getMachineOfType(server)).thenReturn(server);
			when(server.getVirtualMachines()).thenReturn(serverVms);
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		try {
			virtualizationManager.activateServerOfType(server);
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		virtualizationManager.turnOffServer(server);
	}

	@Test(expected = UnknownServerException.class)
	public void testTurnOffAnUnknownServer() throws UnknownServerException,
			ServerNotEmptyException, NoMoreServersAvailableException {
		VirtualizationManager virtualizationManager = new VirtualizationManagerImpl();
		Server server = mock(Server.class);

		when(server.getName()).thenReturn("1");

		virtualizationManager.turnOffServer(server);
	}

	@Test(expected = UnknownServerException.class)
	public void testTurnOffANullServer() throws UnknownServerException,
			ServerNotEmptyException, NoMoreServersAvailableException {
		VirtualizationManager virtualizationManager = new VirtualizationManagerImpl();

		virtualizationManager.turnOffServer(null);
	}

	@Test
	public void testCopyAllocationStatus() throws IncompatibleObjectsException {
		VirtualizationManager virtualizationManager = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		virtualizationManager.setEnvironment(environment);
		
		VirtualizationManager virtManToCopy = mock(VirtualizationManager.class);
		Environment environmentOfSecondVirtMan = mock(Environment.class);
		
		List<VirtualMachine> demand = new ArrayList<VirtualMachine>();
		
		try {
			
		} catch (Exception e) {
			failConfiguringMocks(e);
		}
		
		virtualizationManager.copyAllocationStatus(virtManToCopy, demand);
		
		fail("Not implemented yet");
	}

}
