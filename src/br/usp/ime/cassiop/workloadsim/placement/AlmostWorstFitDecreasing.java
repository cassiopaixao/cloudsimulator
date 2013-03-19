package br.usp.ime.cassiop.workloadsim.placement;

import java.util.Collections;
import java.util.List;

import br.usp.ime.cassiop.workloadsim.exceptions.DependencyNotSetException;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;

public class AlmostWorstFitDecreasing extends AlmostWorstFit {

	@Override
	public void consolidateAll(List<VirtualMachine> demand)
			throws DependencyNotSetException {
		if (demand == null) {
			throw new DependencyNotSetException("Demand is not set.");
		}

		// demand.sort desc
		Collections.sort(demand);
		Collections.reverse(demand);

		super.consolidateAll(demand);
	}
}
