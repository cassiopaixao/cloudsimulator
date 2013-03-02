package br.usp.ime.cassiop.workloadsim;

import java.util.ArrayList;
import java.util.List;

public class ExecutionQueue {
	private List<Runnable> executionList = null;

	private int maxExecutions = 1;
	
	public void setMaxExecutions(int maxExecutions) {
		this.maxExecutions = maxExecutions;
	}

	private int running = 0;
	private int finished = 0;
	private int lastExecution = -1;

	private static ExecutionQueue instance = null;

	public static synchronized ExecutionQueue getInstance() {
		if (instance == null) {
			instance = new ExecutionQueue();
		}
		return instance;
	}

	private ExecutionQueue() {
		executionList = new ArrayList<Runnable>();
	}

	public synchronized void addExecution(Runnable runnable) {
		executionList.add(runnable);
		if (running < maxExecutions) {
			runNext();
		}
	}

	public synchronized void endRun() {
		finished++;
		running--;

		if (finished < executionList.size()) {
			runNext();
		}
	}

	private synchronized void runNext() {
		if (lastExecution < executionList.size() - 1) {
			lastExecution++;
			running++;

			new Thread(executionList.get(lastExecution)).start();
		}
	}
}
