package br.usp.ime.cassiop.workloadsim.placement;

import java.util.Collections;
import java.util.List;

import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;

public class AlmostWorstFitDecreasing extends AlmostWorstFit {

	@Override
	public void orderDemand(List<VirtualMachine> demand) {
		// demand.sort desc
		Collections.sort(demand);
		Collections.reverse(demand);
	}
}
