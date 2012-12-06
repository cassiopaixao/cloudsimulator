package br.usp.ime.cassiop.workloadsim.environment;

import java.util.ArrayList;
import java.util.List;

import br.usp.ime.cassiop.workloadsim.Environment;
import br.usp.ime.cassiop.workloadsim.model.PhysicalMachine;
import br.usp.ime.cassiop.workloadsim.model.ResourceType;

public class IdealCluster implements Environment {

	private PhysicalMachine referenceMachine = null;
	
	public IdealCluster() {
		referenceMachine = new PhysicalMachine();
		referenceMachine.setCapacity(ResourceType.CPU, 1.0);
		referenceMachine.setCapacity(ResourceType.MEMORY, 1.0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.usp.ime.cassiop.workloadsim.environment.Environment#getMachineOfType
	 * (br.usp.ime.cassiop.workloadsim.model.PhysicalMachine)
	 */
	@Override
	public PhysicalMachine getMachineOfType(PhysicalMachine pm)
			throws Exception {
		PhysicalMachine newPm = new PhysicalMachine();

		newPm.setCapacity(ResourceType.CPU, pm.getCapacity(ResourceType.CPU));
		newPm.setCapacity(ResourceType.MEMORY,
				pm.getCapacity(ResourceType.MEMORY));

		return newPm;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.usp.ime.cassiop.workloadsim.environment.Environment#getMachineTypes()
	 */
	@Override
	public List<PhysicalMachine> getMachineTypes() {
		List<PhysicalMachine> availableMachines = new ArrayList<PhysicalMachine>(
				1);

		PhysicalMachine newPm = new PhysicalMachine();
		newPm.setCapacity(ResourceType.CPU,
				referenceMachine.getCapacity(ResourceType.CPU));
		newPm.setCapacity(ResourceType.MEMORY,
				referenceMachine.getCapacity(ResourceType.MEMORY));

		availableMachines.add(newPm);
		
		return availableMachines;
	}

	@Override
	public void clear() {
		// nothing to do
	}
}
