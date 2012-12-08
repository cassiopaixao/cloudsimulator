package br.usp.ime.cassiop.workloadsim;

import java.util.List;

import br.usp.ime.cassiop.workloadsim.model.PhysicalMachine;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;

public interface PhysicalMachineTypeChooser {
	public PhysicalMachine choosePMType(
			List<PhysicalMachine> availableMachineTypes, VirtualMachine vmDemand);

}
