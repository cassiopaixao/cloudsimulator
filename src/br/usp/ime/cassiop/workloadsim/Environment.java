package br.usp.ime.cassiop.workloadsim;

import java.util.List;

import br.usp.ime.cassiop.workloadsim.model.PhysicalMachine;

public interface Environment extends Parametrizable {

	public abstract PhysicalMachine getMachineOfType(PhysicalMachine pm)
			throws Exception;

	public abstract List<PhysicalMachine> getAvailableMachineTypes();

	public abstract void clear();

}