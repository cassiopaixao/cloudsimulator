package br.usp.ime.cassiop.workloadsim.environment;

import java.util.Map;

import br.usp.ime.cassiop.workloadsim.Environment;

public class HomogeneousCluster extends Environment {

	public HomogeneousCluster() {
		super();
		initialize();
	}

	protected void initialize() {
		clear(true);

		addPmStatus(Integer.MAX_VALUE, 1.0, 1.0);
	}

	@Override
	public void setParameters(Map<String, Object> parameters) throws Exception {
		super.setParameters(parameters);
	}

}
