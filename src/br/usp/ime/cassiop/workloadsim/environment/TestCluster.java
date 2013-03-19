package br.usp.ime.cassiop.workloadsim.environment;

import br.usp.ime.cassiop.workloadsim.Environment;

public class TestCluster extends Environment {

	public TestCluster() {
		super();
		initialize();
	}

	protected void initialize() {
		clear(true);
		// NumberOfMachines CPUs Memory
		addServerStatus(2, 1.00, 1.00);
		addServerStatus(1, 0.50, 0.50);
		addServerStatus(1, 1.00, 0.50);
		addServerStatus(1, 0.50, 1.00);
	}

}
