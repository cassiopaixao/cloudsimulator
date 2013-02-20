package br.usp.ime.cassiop.workloadsim.environment;

public class MachineStatus {
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

	public void useOne() throws Exception {
		if (used == available) {
			throw new Exception(
					"There is no more Physical Machines of this type available.");
		}
		used++;
	}

	public void turnOffOne() throws Exception {
		if (used == 0) {
			throw new Exception(
					"Couldn't turn off a machine that wasn't being used.");
		}
		used--;
	}

	public void clear() {
		used = 0;
	}

}