package br.usp.ime.cassiop.workloadsim;

import java.util.List;

import br.usp.ime.cassiop.workloadsim.model.PhysicalMachine;

public interface Environment {

	public abstract PhysicalMachine getMachineOfType(PhysicalMachine pm)
			throws Exception;

	public abstract List<PhysicalMachine> getMachineTypes();

	public abstract void clear();

}