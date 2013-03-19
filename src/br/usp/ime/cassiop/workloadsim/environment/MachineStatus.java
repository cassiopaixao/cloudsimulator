package br.usp.ime.cassiop.workloadsim.environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.exceptions.NoMoreServersAvailableException;

public class MachineStatus {

	final Logger logger = LoggerFactory.getLogger(MachineStatus.class);

	private int available;
	private int used;

	public MachineStatus() {
		available = 0;
		used = 0;
	}

	public MachineStatus(int available, int used) {
		this.available = available;
		this.used = used;
	}

	public int getAvailable() {
		return available;
	}

	public void setAvailable(int available) {
		this.available = available;
	}

	public int getUsed() {
		return used;
	}

	public void useOne() throws NoMoreServersAvailableException {
		if (used == available) {
			throw new NoMoreServersAvailableException(
					"There is no more Servers of this type available.");
		}
		used++;
	}

	public void turnOffOne() {
		if (used == 0) {
			logger.error("Tryed to turn off a machine that wasn't being used.");
			return;
		}
		used--;
	}

	public void clear() {
		used = 0;
	}

}