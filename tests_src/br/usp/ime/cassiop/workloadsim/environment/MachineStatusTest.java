package br.usp.ime.cassiop.workloadsim.environment;

import static br.usp.ime.cassiop.workloadsim.util.TestUtils.failConfiguringInitialState;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import br.usp.ime.cassiop.workloadsim.exceptions.NoMoreServersAvailableException;

public class MachineStatusTest {

	@Test
	public void testMachineStatus() {
		MachineStatus machineStatus = new MachineStatus();

		assertEquals(0, machineStatus.getAvailable());
		assertEquals(0, machineStatus.getUsed());
	}

	@Test
	public void testUseOne() throws NoMoreServersAvailableException {
		MachineStatus machineStatus = new MachineStatus();

		machineStatus.setAvailable(5);

		machineStatus.useOne();

		assertEquals(5, machineStatus.getAvailable());
		assertEquals(1, machineStatus.getUsed());
	}

	@Test
	public void testUseTheLastOne() throws NoMoreServersAvailableException {
		MachineStatus machineStatus = new MachineStatus();

		machineStatus.setAvailable(1);

		machineStatus.useOne();

		assertEquals(1, machineStatus.getAvailable());
		assertEquals(1, machineStatus.getUsed());
	}

	@Test(expected = NoMoreServersAvailableException.class)
	public void testUseOneUnavailable() throws NoMoreServersAvailableException {
		MachineStatus machineStatus = new MachineStatus();

		machineStatus.setAvailable(0);

		machineStatus.useOne();
	}

	@Test(expected = NoMoreServersAvailableException.class)
	public void testUseOneNoMoreAvailable()
			throws NoMoreServersAvailableException {
		MachineStatus machineStatus = new MachineStatus();

		machineStatus.setAvailable(3);

		machineStatus.useOne();
		machineStatus.useOne();
		machineStatus.useOne();
		machineStatus.useOne();
	}

	@Test
	public void testTurnOffOne() throws NoMoreServersAvailableException {
		MachineStatus machineStatus = new MachineStatus();

		machineStatus.setAvailable(3);

		try {
			machineStatus.useOne();
			machineStatus.useOne();
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		machineStatus.turnOffOne();
		
		assertEquals(1, machineStatus.getUsed());
	}
	
	@Test(expected = NoMoreServersAvailableException.class)
	public void testTurnOffOneWithoutMachinesBeingUsed() throws NoMoreServersAvailableException {
		MachineStatus machineStatus = new MachineStatus();

		machineStatus.setAvailable(3);

		try {
			machineStatus.useOne();
		} catch (Exception e) {
			failConfiguringInitialState(e);
		}

		machineStatus.turnOffOne();
		machineStatus.turnOffOne();
	}

	@Test
	public void testClear() throws NoMoreServersAvailableException {
		MachineStatus machineStatus = new MachineStatus();

		machineStatus.setAvailable(3);

		assertEquals(3, machineStatus.getAvailable());
		assertEquals(0, machineStatus.getUsed());

		machineStatus.useOne();
		machineStatus.useOne();

		assertEquals(3, machineStatus.getAvailable());
		assertEquals(2, machineStatus.getUsed());

		machineStatus.clear();

		assertEquals(3, machineStatus.getAvailable());
		assertEquals(0, machineStatus.getUsed());
	}

}
