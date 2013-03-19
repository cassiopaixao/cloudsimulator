package br.usp.ime.cassiop.workloadsim.environment;

import br.usp.ime.cassiop.workloadsim.Environment;

public class HomogeneousCluster extends Environment {

	public HomogeneousCluster() {
		super();
		initialize();
	}

	protected void initialize() {
		clear(true);

		addServerStatus(Integer.MAX_VALUE, 1.0, 1.0);
	}

}
