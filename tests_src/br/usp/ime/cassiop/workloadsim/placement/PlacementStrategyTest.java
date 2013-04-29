package br.usp.ime.cassiop.workloadsim.placement;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static br.usp.ime.cassiop.workloadsim.util.TestUtils.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;

public class PlacementStrategyTest {

	private PlacementStrategy placementStrategy = null;

	@Before
	public void setUp() throws Exception {
		placementStrategy = new PlacementStrategy() {

			@Override
			public Server selectDestinationServer(VirtualMachine vm,
					List<Server> servers) {
				return null;
			}

			@Override
			public void orderServers(List<Server> servers) {
			}

			@Override
			public void orderDemand(List<VirtualMachine> demand) {
			}

			@Override
			public Server chooseServerType(VirtualMachine vmDemand,
					List<Server> machineTypes) {
				return null;
			}
		};
	}

	@Test
	public void testChooseServerTypeEvenOverloading() {
		PlacementUtils placementUtils = mock(PlacementUtils.class);

		placementStrategy.setPlacementUtils(placementUtils);

		Server server = mock(Server.class);
		VirtualMachine vm = mock(VirtualMachine.class);

		List<Server> servers = new ArrayList<Server>();
		servers.add(server);

		try {
			when(placementUtils.lessLossOfPerformanceMachine(servers, vm))
					.thenReturn(server);
		} catch (Exception e) {
			failConfiguringMocks(e);
		}

		Server destinationServer = placementStrategy
				.chooseServerTypeEvenOverloading(vm, servers);

		try {
			assertTrue(server.equals(destinationServer));
			verify(placementUtils).lessLossOfPerformanceMachine(servers, vm);
		} catch (Exception e) {
			failVerifyingMethodsCalls(e);
		}
	}
}
