package br.usp.ime.cassiop.workloadsim.environment;

import java.util.Map;

import br.usp.ime.cassiop.workloadsim.Environment;

public class GoogleCluster extends Environment {

	public GoogleCluster() {
		super();
		initialize();
	}

	protected void initialize() {
		clear(true);

		// NumberOfMachines CPUs Memory
		// 6732 0.50 0.50
		// 3863 0.50 0.25
		// 1001 0.50 0.75
		// .795 1.00 1.00
		// .126 0.25 0.25
		// ..52 0.50 0.12
		// ...5 0.50 0.03
		// ...5 0.50 0.97
		// ...3 1.00 0.50
		// ...1 0.50 0.06

		addPmStatus(6732, 0.50, 0.50);
		addPmStatus(3863, 0.50, 0.25);
		addPmStatus(1001, 0.50, 0.75);
		addPmStatus(795, 1.00, 1.00);
		addPmStatus(126, 0.25, 0.25);
		addPmStatus(52, 0.50, 0.12);
		addPmStatus(5, 0.50, 0.03);
		addPmStatus(5, 0.50, 0.97);
		addPmStatus(3, 1.00, 0.50);
		addPmStatus(1, 0.50, 0.06);

	}

	@Override
	public void setParameters(Map<String, Object> parameters) throws Exception {
		super.setParameters(parameters);
	}

}
