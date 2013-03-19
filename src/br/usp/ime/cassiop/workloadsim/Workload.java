package br.usp.ime.cassiop.workloadsim;

import java.util.List;

import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;

public abstract class Workload {

	protected long initialTime = -1;
	protected long timeInterval = -1;
	protected long lastTime = -1;

	public Workload(long initialTime, long timeInterval, long lastTime) {
		this.initialTime = initialTime;
		this.timeInterval = timeInterval;
		this.lastTime = lastTime;
	}

	public abstract List<VirtualMachine> getDemand(long time);

	public long getTimeInterval() {
		return timeInterval;
	}

	public long getInitialTime() {
		return initialTime;
	}

	public long getLastTime() {
		return lastTime;
	}

	public boolean hasDemand(long currentTime) {
		return (currentTime >= initialTime && currentTime <= lastTime && currentTime
				% timeInterval == 0);
	}
}
