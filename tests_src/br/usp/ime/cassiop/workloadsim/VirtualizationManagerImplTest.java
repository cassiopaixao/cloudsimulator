package br.usp.ime.cassiop.workloadsim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import br.usp.ime.cassiop.workloadsim.exceptions.InvalidParameterException;
import br.usp.ime.cassiop.workloadsim.exceptions.NoMoreServersAvailableException;
import br.usp.ime.cassiop.workloadsim.exceptions.ServerNotEmptyException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownServerException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownVirtualMachineException;
import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class VirtualizationManagerImplTest {

	private Server buildServer(String name, double cpu, double mem) {
		Server server = buildServer(cpu, mem);
		server.setName(name);
		return server;
	}

	private Server buildServer(double cpu, double mem) {
		Server server = new Server();
		server.setCapacity(ResourceType.CPU, cpu);
		server.setCapacity(ResourceType.MEMORY, mem);
		return server;
	}

	private VirtualMachine buildVm(double cpu, double mem) {
		VirtualMachine vm = new VirtualMachine();
		vm.setDemand(ResourceType.CPU, cpu);
		vm.setDemand(ResourceType.MEMORY, mem);
		return vm;
	}

	@Before
	public void setUp() throws Exception {
	}

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
	public void testSetVmToServerOk() {
		VirtualizationManager virtMan = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		virtMan.setEnvironment(environment);
		virtMan.setStatisticsModule(statisticsModule);

		// http://gojko.net/2009/10/23/mockito-in-six-easy-examples/
		// http://schuchert.wikispaces.com/Mockito.LoginServiceExample

		Server server1 = buildServer(1.0, 1.0);

		// configuring mock;
		try {
			when(environment.getMachineOfType(server1)).thenReturn(
					buildServer(1.0, 1.0));
		} catch (Exception e) {
			fail("Mocking configuration shouldn't throw any exception.");
		}

		try {
			server1 = virtMan.activateServerOfType(server1);
		} catch (UnknownServerException e) {
			fail("There should be servers of this type.");
		} catch (NoMoreServersAvailableException e) {
			fail("There should be servers of this type.");
		}

		try {
			VirtualMachine vm1 = buildVm(0.1, 0.1);

			virtMan.setVmToServer(vm1, server1);

			assertTrue(server1.getVirtualMachines().contains(vm1));
			assertTrue(virtMan.getActiveServersList().contains(server1));
			assertEquals(virtMan.getActiveVirtualMachines().get(vm1.getName()),
					vm1);
			verify(statisticsModule, never()).addToStatisticValue(
					eq(Constants.STATISTIC_MIGRATIONS), anyInt());
			verify(statisticsModule, never()).addToStatisticValue(
					eq(Constants.STATISTIC_MIGRATIONS_COST), anyDouble());

		} catch (UnknownVirtualMachineException e) {
			fail("Virtual machine isn't null. It exists.");
		} catch (UnknownServerException e) {
			fail("This server exists. Should be known.");
		}

	}

	@Test
	public void testSetVmToServerMigrationPreviousDeallocation() {
		VirtualizationManager virtMan = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		virtMan.setEnvironment(environment);
		virtMan.setStatisticsModule(statisticsModule);

		// http://gojko.net/2009/10/23/mockito-in-six-easy-examples/
		// http://schuchert.wikispaces.com/Mockito.LoginServiceExample

		Server server1 = buildServer(1.0, 1.0);

		// configuring mock;
		try {
			when(environment.getMachineOfType(server1)).thenReturn(
					buildServer(1.0, 1.0));
		} catch (Exception e) {
			fail("Mocking configuration shouldn't throw any exception.");
		}

		try {
			server1 = virtMan.activateServerOfType(server1);
		} catch (UnknownServerException e) {
			fail("There should be servers of this type.");
		} catch (NoMoreServersAvailableException e) {
			fail("There should be servers of this type.");
		}

		try {
			VirtualMachine vm1 = buildVm(0.1, 0.1);
			vm1.setLastServer(buildServer(0.5, 0.5));

			virtMan.setVmToServer(vm1, server1);

			assertTrue(server1.getVirtualMachines().contains(vm1));
			assertTrue(virtMan.getActiveServersList().contains(server1));
			assertEquals(virtMan.getActiveVirtualMachines().get(vm1.getName()),
					vm1);
			verify(statisticsModule, times(1)).addToStatisticValue(
					eq(Constants.STATISTIC_MIGRATIONS), eq(1));
			verify(statisticsModule, times(1)).addToStatisticValue(
					eq(Constants.STATISTIC_MIGRATIONS_COST),
					eq(vm1.getResourceUtilization()));

		} catch (UnknownVirtualMachineException e) {
			fail("Virtual machine isn't null. It exists.");
		} catch (UnknownServerException e) {
			fail("This server exists. Should be known.");
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
			fail("Mocking configuration shouldn't throw any exception.");
		}

		try {
			server1 = virtMan.activateServerOfType(server1);
		} catch (UnknownServerException e) {
			fail("There should be servers of this type.");
		} catch (NoMoreServersAvailableException e) {
			fail("There should be servers of this type.");
		}

		try {
			virtMan.setVmToServer(buildVm(0.1, 0.1), server2);
			fail("Server 2 doesn't exist in environment.");
		} catch (UnknownVirtualMachineException e) {
			fail("A valid virtual machine was provided.");
		} catch (UnknownServerException e) {
			assertNotNull("server2 is not active.", e);
		}

		try {
			virtMan.setVmToServer(buildVm(0.1, 0.1), falseServer1);
			fail("Server 1 is different from the \"Server 1\" in argument.");
		} catch (UnknownVirtualMachineException e) {
			fail("A valid virtual machine was provided.");
		} catch (UnknownServerException e) {
			assertNotNull("Server in argument should be different.", e);
		}

		try {
			virtMan.setVmToServer(buildVm(0.1, 0.1), null);
			fail("A null server shouldn't be accepted.");
		} catch (UnknownVirtualMachineException e) {
			fail("A valid virtual machine was provided.");
		} catch (UnknownServerException e) {
			assertNotNull("A null server shouldn't be accepted.", e);
		}

	}

	@Test
	public void testSetVmToServerUnknownVirtualMachineException() {
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
			fail("Mocking configuration shouldn't throw any exception.");
		}

		try {
			server1 = virtMan.activateServerOfType(server1);
			server2 = virtMan.activateServerOfType(server2);
		} catch (UnknownServerException e) {
			fail("There should be servers of this type.");
		} catch (NoMoreServersAvailableException e) {
			fail("There should be servers of this type.");
		}

		try {
			virtMan.setVmToServer(null, server1);
			fail("A null virtual machine shouldn't be accepted.");
		} catch (UnknownVirtualMachineException e) {
			assertNotNull("A null virtual machine shouldn't be accepted", e);
		} catch (UnknownServerException e) {
			fail("Server 1 is valid.");
		}
	}

	@Test
	public void testSetVmToServerMigrationOnDemmandDeallocation() {
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
			fail("Mocking configuration shouldn't throw any exception.");
		}

		try {
			server1 = virtMan.activateServerOfType(server1);
			server2 = virtMan.activateServerOfType(server2);
		} catch (UnknownServerException e) {
			fail("There should be servers of this type.");
		} catch (NoMoreServersAvailableException e) {
			fail("There should be servers of this type.");
		}

		try {
			VirtualMachine vm = buildVm(0.1, 0.1);

			virtMan.setVmToServer(vm, server1);

			virtMan.setVmToServer(vm, server2);

			assertFalse(server1.getVirtualMachines().contains(vm));
			assertTrue(server2.getVirtualMachines().contains(vm));

			assertEquals(server1, vm.getLastServer());
			assertEquals(server2, vm.getCurrentServer());

			verify(statisticsModule, times(1)).addToStatisticValue(
					eq(Constants.STATISTIC_MIGRATIONS), eq(1));
			verify(statisticsModule, times(1)).addToStatisticValue(
					eq(Constants.STATISTIC_MIGRATIONS_COST),
					eq(vm.getResourceUtilization()));

		} catch (UnknownVirtualMachineException e) {
			fail("Virtual machine is valid.");
		} catch (UnknownServerException e) {
			fail("Servers are valid.");
		}
	}

	@Test
	public void testSetVmToServerMigrationOnDemmandNotNeeded() {
		VirtualizationManager virtMan = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		virtMan.setEnvironment(environment);
		virtMan.setStatisticsModule(statisticsModule);

		// http://gojko.net/2009/10/23/mockito-in-six-easy-examples/
		// http://schuchert.wikispaces.com/Mockito.LoginServiceExample

		Server server1 = buildServer("1", 1.0, 1.0);

		// configuring mock;
		try {
			when(environment.getMachineOfType(server1)).thenReturn(
					buildServer(1.0, 1.0));
		} catch (Exception e) {
			fail("Mocking configuration shouldn't throw any exception.");
		}

		try {
			server1 = virtMan.activateServerOfType(server1);
		} catch (UnknownServerException e) {
			fail("There should be servers of this type.");
		} catch (NoMoreServersAvailableException e) {
			fail("There should be servers of this type.");
		}

		try {
			VirtualMachine vm = buildVm(0.1, 0.1);

			virtMan.setVmToServer(vm, server1);

			virtMan.setVmToServer(vm, server1);

			assertTrue(server1.getVirtualMachines().contains(vm));
			assertEquals(server1, vm.getCurrentServer());
			assertNotSame(server1, vm.getLastServer());

			verify(statisticsModule, never()).addToStatisticValue(
					eq(Constants.STATISTIC_MIGRATIONS), anyInt());
			verify(statisticsModule, never()).addToStatisticValue(
					eq(Constants.STATISTIC_MIGRATIONS_COST), anyDouble());

		} catch (UnknownVirtualMachineException e) {
			fail("Virtual machine is valid.");
		} catch (UnknownServerException e) {
			fail("Servers are valid.");
		}
	}

	@Test
	public void testActivateServerOfType() {
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
			fail("Mocking configuration shouldn't throw any exception.");
		}

		try {
			server1 = virtMan.activateServerOfType(server1);

			assertTrue(virtMan.getActiveServersList().contains(server1));
		} catch (UnknownServerException e) {
			fail("There should be servers of this type.");
		} catch (NoMoreServersAvailableException e) {
			fail("There should be servers of this type.");
		}
	}

	@Test(expected = UnknownServerException.class)
	public void testActivateServerOfTypeRequestingAnUnexistentServer()
			throws UnknownServerException {
		VirtualizationManager virtMan = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		virtMan.setEnvironment(environment);
		virtMan.setStatisticsModule(statisticsModule);

		Server server1 = buildServer("1", 1.0, 1.0);

		// configuring mock;
		try {
			when(environment.getMachineOfType(server1)).thenThrow(
					new UnknownServerException());
			when(environment.getMachineOfType(null)).thenThrow(
					new UnknownServerException());
		} catch (Exception e) {
			fail("Mocking configuration shouldn't throw any exception.");
		}

		try {
			server1 = virtMan.activateServerOfType(server1);
			fail("There's no Server1 type in environment.");
		} catch (UnknownServerException e) {
			throw e;
		} catch (NoMoreServersAvailableException e) {
			fail("Shouldn't throw NoMoreServersAvailableException.");
		}
	}

	@Test(expected = UnknownServerException.class)
	public void testActivateServerOfTypeRequestingANullServer()
			throws UnknownServerException {
		VirtualizationManager virtMan = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		virtMan.setEnvironment(environment);
		virtMan.setStatisticsModule(statisticsModule);

		// configuring mock;
		try {
			when(environment.getMachineOfType(any(Server.class))).thenReturn(
					buildServer(1.0, 1.0));
			when(environment.getMachineOfType(null)).thenThrow(
					new UnknownServerException());
		} catch (Exception e) {
			fail("Mocking configuration shouldn't throw any exception.");
		}

		try {
			virtMan.activateServerOfType(null);
			fail("Server's argument is null. Should throw an UnknownServerException.");
		} catch (UnknownServerException e) {
			throw e;
		} catch (NoMoreServersAvailableException e) {
			fail("Shouldn't throw NoMoreServersAvailableException.");
		}
	}

	@Test(expected = NoMoreServersAvailableException.class)
	public void testActivateServerOfTypeRequestingAServerWithNoMoreMachinesAvailable()
			throws NoMoreServersAvailableException {
		VirtualizationManager virtMan = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		StatisticsModule statisticsModule = mock(StatisticsModule.class);

		virtMan.setEnvironment(environment);
		virtMan.setStatisticsModule(statisticsModule);

		Server server1 = buildServer(1.0, 1.0);

		// configuring mock;
		try {
			when(environment.getMachineOfType(server1)).thenThrow(
					new NoMoreServersAvailableException());
			when(environment.getMachineOfType(null)).thenThrow(
					new UnknownServerException());
		} catch (Exception e) {
			fail("Mocking configuration shouldn't throw any exception.");
		}

		try {
			virtMan.activateServerOfType(server1);
			fail("There's no more machines of this type. Should throw an NoMoreServersAvailableException.");
		} catch (UnknownServerException e) {
			fail("Shouldn't throw UnknownServerException.");
		} catch (NoMoreServersAvailableException e) {
			throw e;
		}
	}

	@Test
	public void testGetNextInactiveServerVirtualMachine() {
		VirtualizationManager virtualizationManager = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		ServerTypeChooser serverTypeChooser = mock(ServerTypeChooser.class);

		virtualizationManager.setEnvironment(environment);

		Server server1 = buildServer(1.0, 1.0);
		Server server2 = buildServer(1.0, 1.0);
		VirtualMachine vmDemand = buildVm(0.5, 0.5);
		List<Server> serverList = new ArrayList<Server>();
		serverList.add(server1);

		try {
			when(environment.getAvailableMachineTypes()).thenReturn(serverList);
			when(serverTypeChooser.chooseServerType(vmDemand, serverList))
					.thenReturn(server1);
			when(environment.getMachineOfType(server1)).thenReturn(server2);
		} catch (Exception e) {
			fail("Shouldn't trhow exceptions in mock configuration.");
		}

		try {
			Server result = virtualizationManager.getNextInactiveServer(
					vmDemand, serverTypeChooser);

			assertNotSame(result, server1);
			assertTrue(server1.getType().equals(result.getType()));

			verify(environment).getAvailableMachineTypes();
			verify(environment).getMachineOfType(server1);
			verify(serverTypeChooser).chooseServerType(vmDemand, serverList);

		} catch (NoMoreServersAvailableException e) {
			fail("There are available servers. Shouldn't throw NoMoreServersAvailableException.");
		} catch (UnknownServerException e) {
			fail("Server should be a valid one.");
		}
	}

	@Test
	public void testGetNextInactiveServerVirtualMachineOverloadingMachine() {
		VirtualizationManager virtualizationManager = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		ServerTypeChooser serverTypeChooser = mock(ServerTypeChooser.class);

		virtualizationManager.setEnvironment(environment);

		Server server1 = buildServer(0.5, 0.5);
		Server server2 = buildServer(0.5, 0.5);
		VirtualMachine vmDemand = buildVm(1.0, 1.0);
		List<Server> serverList = new ArrayList<Server>();
		serverList.add(server1);

		try {
			when(environment.getAvailableMachineTypes()).thenReturn(serverList);
			when(serverTypeChooser.chooseServerType(vmDemand, serverList))
					.thenReturn(server1);
			when(environment.getMachineOfType(server1)).thenReturn(server2);
		} catch (Exception e) {
			fail("Shouldn't trhow exceptions in mock configuration.");
		}

		try {
			Server result = virtualizationManager.getNextInactiveServer(
					vmDemand, serverTypeChooser);

			assertNotSame(result, server1);
			assertTrue(server1.getType().equals(result.getType()));

			verify(environment).getAvailableMachineTypes();
			verify(environment).getMachineOfType(server1);
			verify(serverTypeChooser).chooseServerType(vmDemand, serverList);

		} catch (NoMoreServersAvailableException e) {
			fail("There are available servers. Shouldn't throw NoMoreServersAvailableException.");
		} catch (UnknownServerException e) {
			fail("Server should be a valid one.");
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
		VirtualMachine vmDemand = buildVm(0.5, 0.5);
		List<Server> serverList = new ArrayList<Server>();
		serverList.add(server1);

		try {
			when(environment.getAvailableMachineTypes()).thenReturn(serverList);
			when(serverTypeChooser.chooseServerType(vmDemand, serverList))
					.thenReturn(server1);
			when(environment.getMachineOfType(server1)).thenThrow(
					new NoMoreServersAvailableException());
		} catch (Exception e) {
			fail("Shouldn't throw exceptions in mock configuration.");
		}

		virtualizationManager
				.getNextInactiveServer(vmDemand, serverTypeChooser);

		fail("Should throw NoMoreServersAvailableException.");
	}

	@Test(expected = NoMoreServersAvailableException.class)
	public void testGetNextInactiveServerVirtualMachineWithNoMoreMachinesAvailable()
			throws NoMoreServersAvailableException {
		VirtualizationManager virtualizationManager = new VirtualizationManagerImpl();
		Environment environment = mock(Environment.class);
		ServerTypeChooser serverTypeChooser = mock(ServerTypeChooser.class);

		virtualizationManager.setEnvironment(environment);

		VirtualMachine vmDemand = buildVm(0.5, 0.5);
		List<Server> serverList = new ArrayList<Server>();

		try {
			when(environment.getAvailableMachineTypes()).thenReturn(serverList);
			when(serverTypeChooser.chooseServerType(vmDemand, serverList))
					.thenReturn(null);
			when(environment.getMachineOfType(any(Server.class))).thenThrow(
					new NoMoreServersAvailableException());
		} catch (Exception e) {
			fail("Shouldn't throw exceptions in mock configuration.");
		}

		try {
			virtualizationManager.getNextInactiveServer(vmDemand,
					serverTypeChooser);
		} finally {
			try {
				verify(environment).getAvailableMachineTypes();
				verify(environment, never())
						.getMachineOfType(any(Server.class));
				verify(serverTypeChooser)
						.chooseServerType(vmDemand, serverList);
			} catch (UnknownServerException e) {
				fail("Shouldn't throw any exception.");
			}
		}

		fail("Should throw NoMoreServersAvailableException.");
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
			fail("Shouldn't throw exceptions in mock configuration.");
		}

		try {
			virtualizationManager.activateServerOfType(server1);
		} catch (Exception e) {
			fail("Mocked classes should return valid objects. Exception: "
					+ e.getMessage());
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
			fail("Shouldn't throw exceptions in mock configuration. Exception: "
					+ e.getMessage());
		}

		try {
			virtualizationManager.activateServerOfType(server);
			virtualizationManager.setVmToServer(vm1, server);
		} catch (Exception e) {
			fail("Mocked classes should return valid objects. Exception: "
					+ e.getMessage());
		}

		virtualizationManager.deallocateFinishedVms(remainingVms, 200);

		try {
			verify(server).getVirtualMachines();
			verify(server, never()).removeVirtualMachine(vm1);
			verify(vm1, never()).getEndTime();

		} catch (UnknownVirtualMachineException e) {
			fail("Shouldn't throw any exception.");
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
			fail("Shouldn't throw exceptions in mock configuration. Exception: "
					+ e.getMessage());
		}

		try {
			virtualizationManager.activateServerOfType(server);
			virtualizationManager.setVmToServer(vm1, server);
		} catch (Exception e) {
			fail("Mocked classes should return valid objects. Exception: "
					+ e.getMessage());
		}

		virtualizationManager.deallocateFinishedVms(remainingVms, 200);

		try {
			verify(server).getVirtualMachines();
			verify(server).removeVirtualMachine(vm1);
			verify(vm1).getEndTime();

		} catch (UnknownVirtualMachineException e) {
			fail("Shouldn't throw any exception.");
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
			fail("Shouldn't throw exceptions in mock configuration. Exception: "
					+ e.getMessage());
		}

		try {
			virtualizationManager.activateServerOfType(server);
			virtualizationManager.setVmToServer(vm1, server);
		} catch (Exception e) {
			fail("Mocked classes should return valid objects. Exception: "
					+ e.getMessage());
		}

		virtualizationManager.deallocateFinishedVms(remainingVms, 200);

		try {
			verify(server).getVirtualMachines();
			verify(server, never()).removeVirtualMachine(vm1);
			verify(vm1).getEndTime();

		} catch (UnknownVirtualMachineException e) {
			fail("Shouldn't throw any exception.");
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
			fail("Shouldn't throw exceptions in mock configuration. Exception: "
					+ e.getMessage());
		}

		try {
			virtualizationManager.activateServerOfType(server);
			virtualizationManager.setVmToServer(vm1, server);
		} catch (Exception e) {
			fail("Mocked classes should return valid objects. Exception: "
					+ e.getMessage());
		}

		virtualizationManager.deallocate(vm1);

		try {
			verify(server).removeVirtualMachine(vm1);

		} catch (UnknownVirtualMachineException e) {
			fail("Shouldn't throw any exception.");
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
			fail("Shouldn't throw exceptions in mock configuration. Exception: "
					+ e.getMessage());
		}

		try {
			virtualizationManager.activateServerOfType(server);
			virtualizationManager.setVmToServer(vm1, server);
		} catch (Exception e) {
			fail("Mocked classes should return valid objects. Exception: "
					+ e.getMessage());
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
			fail("Shouldn't throw exceptions in mock configuration. Exception: "
					+ e.getMessage());
		}

		try {
			virtualizationManager.activateServerOfType(server);
		} catch (Exception e) {
			fail("Mocked classes should return valid objects. Exception: "
					+ e.getMessage());
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
			fail("Shouldn't throw exceptions in mock configuration. Exception: "
					+ e.getMessage());
		}

		try {
			virtualizationManager.activateServerOfType(server);
		} catch (Exception e) {
			fail("Mocked classes should return valid objects. Exception: "
					+ e.getMessage());
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

}
