package br.usp.ime.cassiop.workloadsim;

import java.util.List;
import java.util.Map;

import br.usp.ime.cassiop.workloadsim.environment.MachineStatus;
import br.usp.ime.cassiop.workloadsim.model.PhysicalMachine;

public interface Environment extends Parametrizable {

	public abstract PhysicalMachine getMachineOfType(PhysicalMachine pm)
			throws Exception;

	public abstract List<PhysicalMachine> getAvailableMachineTypes();

	public abstract Map<PhysicalMachine, MachineStatus> getPhysicalMachineStatus();
	
	public abstract void clear();

}