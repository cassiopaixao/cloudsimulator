package br.usp.ime.cassiop.workloadsim;

import static org.junit.Assert.*;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.*;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import br.usp.ime.cassiop.workloadsim.environment.MachineStatus;
import br.usp.ime.cassiop.workloadsim.exceptions.NoMoreServersAvailableException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownServerException;
import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.util.MathUtils;

public class EnvironmentTest {

	private Environment environment = null;

	@Before
	public void before() {
		environment = new Environment() {

			@Override
			protected void initialize() {
				addServerStatus(2, 0.75, 0.5);
			}
		};
	}

	@Test
	public void testEnvironment() {
		assertNotNull(environment.getEnvironmentStatus());
		assertEquals(1, environment.getAvailableMachineTypes().size());
	}

	@Test
	public void testGetMachineOfType() throws UnknownServerException,
			NoMoreServersAvailableException {
		List<Server> servers = environment.getAvailableMachineTypes();
		Map<Server, MachineStatus> map = environment.getEnvironmentStatus();

		Server serverType = servers.get(0);

		Server newServer = environment.getMachineOfType(serverType);

		assertEquals(1, map.get(serverType).getUsed());
		assertTrue(newServer.getType().equals(serverType.getType()));
		assertNotSame(serverType, newServer);
	}

	@Test(expected = UnknownServerException.class)
	public void testGetMachineOfUnknownType() throws UnknownServerException,
			NoMoreServersAvailableException {
		Server serverType = buildServer(0.75, 0.75);

		environment.getMachineOfType(serverType);
	}

	@Test(expected = NoMoreServersAvailableException.class)
	public void testGetMachineOfSaturedType() throws UnknownServerException,
			NoMoreServersAvailableException {
		List<Server> servers = environment.getAvailableMachineTypes();

		Server serverType = servers.get(0);

		environment.getMachineOfType(serverType);
		environment.getMachineOfType(serverType);

		assertEquals(2, environment.getEnvironmentStatus().get(serverType)
				.getAvailable());
		assertEquals(2, environment.getEnvironmentStatus().get(serverType)
				.getUsed());

		environment.getMachineOfType(serverType);
	}

	@Test
	public void testGetAvailableMachineTypes() {
		List<Server> servers = environment.getAvailableMachineTypes();

		Server serverType = servers.get(0);

		assertTrue(serverType.getType().equals("CPU(0.75);MEM(0.50)."));
	}

	@Test
	public void testClear() {
		List<Server> servers = environment.getAvailableMachineTypes();
		Server serverType = servers.get(0);

		try {
			environment.getMachineOfType(serverType);
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		assertEquals(1, environment.getAvailableMachineTypes().size());
		assertEquals(1, environment.getEnvironmentStatus().get(serverType)
				.getUsed());

		environment.clear();

		servers = environment.getAvailableMachineTypes();
		serverType = servers.get(0);

		assertEquals(1, environment.getAvailableMachineTypes().size());
		assertEquals(0, environment.getEnvironmentStatus().get(serverType)
				.getUsed());
	}

	@Test
	public void testClearBoolean() {
		List<Server> servers = environment.getAvailableMachineTypes();
		Server serverType = servers.get(0);

		try {
			environment.getMachineOfType(serverType);
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		assertEquals(1, environment.getAvailableMachineTypes().size());
		assertEquals(1, environment.getEnvironmentStatus().get(serverType)
				.getUsed());

		environment.clear(true);

		servers = environment.getAvailableMachineTypes();

		assertEquals(0, environment.getAvailableMachineTypes().size());
	}

	@Test
	public void testTurnOffMachineOfType()
			throws NoMoreServersAvailableException {
		List<Server> servers = environment.getAvailableMachineTypes();

		Server serverType = servers.get(0);
		Server server1 = null;
		Server server2 = null;

		try {
			server1 = environment.getMachineOfType(serverType);
			server2 = environment.getMachineOfType(serverType);
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		environment.turnOffMachineOfType(server1);

		assertEquals(1, environment.getEnvironmentStatus().get(serverType)
				.getUsed());

		environment.turnOffMachineOfType(server2);

		assertEquals(0, environment.getEnvironmentStatus().get(serverType)
				.getUsed());
	}

	@Test(expected = NoMoreServersAvailableException.class)
	public void testTurnOffMachineOfUnavailableType()
			throws NoMoreServersAvailableException {
		List<Server> servers = environment.getAvailableMachineTypes();

		Server serverType = servers.get(0);
		Server server1 = null;

		try {
			server1 = environment.getMachineOfType(serverType);
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		environment.turnOffMachineOfType(server1);

		assertEquals(0, environment.getEnvironmentStatus().get(serverType)
				.getUsed());

		environment.turnOffMachineOfType(server1);
	}

	@Test
	public void testAddServerStatus() {
		environment.addServerStatus(5, 0.25, 0.85);

		List<Server> servers = environment.getAvailableMachineTypes();

		assertEquals(2, servers.size());

		boolean found = false;
		for (Server server : servers) {
			if (server.getType().equals("CPU(0.25);MEM(0.85).")) {
				found = true;

				assertTrue(MathUtils.equals(0.25,
						server.getCapacity(ResourceType.CPU)));
				assertTrue(MathUtils.equals(0.85,
						server.getCapacity(ResourceType.MEMORY)));
				assertEquals(5, environment.getEnvironmentStatus().get(server)
						.getAvailable());
				assertEquals(0, environment.getEnvironmentStatus().get(server)
						.getUsed());
			}
		}

		if (!found) {
			fail("Should have found the new machine type.");
		}
	}

	@Test
	public void testClone() {
		fail("Not implemented yet.");
	}

	@Test
	public void testEquals() {
		fail("Not implemented yet.");
	}

}
