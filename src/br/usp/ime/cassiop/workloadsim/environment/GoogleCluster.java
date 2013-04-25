package br.usp.ime.cassiop.workloadsim.environment;

import br.usp.ime.cassiop.workloadsim.Environment;

public class GoogleCluster extends Environment {

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

		addServerStatus(6732, 0.50, 0.50);
		addServerStatus(3863, 0.50, 0.25);
		addServerStatus(1001, 0.50, 0.75);
		addServerStatus(795, 1.00, 1.00);
		addServerStatus(126, 0.25, 0.25);
		addServerStatus(52, 0.50, 0.12);
		addServerStatus(5, 0.50, 0.03);
		addServerStatus(5, 0.50, 0.97);
		addServerStatus(3, 1.00, 0.50);
		addServerStatus(1, 0.50, 0.06);

	}

}
