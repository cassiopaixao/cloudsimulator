package br.usp.ime.cassiop.workloadsim.environment;

import java.util.Map;

import br.usp.ime.cassiop.workloadsim.Environment;

public class TestCluster extends Environment {

	public TestCluster() {
		super();
		initialize();
	}

	protected void initialize() {
		clear(true);
		// NumberOfMachines CPUs Memory
		addPmStatus(2, 1.00, 1.00);
		addPmStatus(1, 0.50, 0.50);
		addPmStatus(1, 1.00, 0.50);
		addPmStatus(1, 0.50, 1.00);
	}

	@Override
	public void setParameters(Map<String, Object> parameters) throws Exception {
		super.setParameters(parameters);
	}

}
