package br.usp.ime.cassiop.workloadsim.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.usp.ime.cassiop.workloadsim.Environment;
import br.usp.ime.cassiop.workloadsim.model.PhysicalMachine;
import br.usp.ime.cassiop.workloadsim.model.ResourceType;

public class IdealCluster implements Environment {

	private PhysicalMachine referenceMachine = null;

	private Map<PhysicalMachine, MachineStatus> environmentStatus = null;

	public IdealCluster() {
		referenceMachine = new PhysicalMachine();
		referenceMachine.setCapacity(ResourceType.CPU, 1.0);
		referenceMachine.setCapacity(ResourceType.MEMORY, 1.0);

		environmentStatus = new HashMap<PhysicalMachine, MachineStatus>(1);
		environmentStatus.put(referenceMachine, new MachineStatus(
				Integer.MAX_VALUE, 0));
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
		MachineStatus status = environmentStatus.get(pm);
		if (status == null) {
			throw new Exception(
					"There is no Physical Machines of this type in the environment.");
		}

		status.useOne();

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
	public List<PhysicalMachine> getAvailableMachineTypes() {
		List<PhysicalMachine> availableMachines = new ArrayList<PhysicalMachine>(
				1);

		availableMachines.add(referenceMachine);

		return availableMachines;
	}

	@Override
	public void clear() {
		for (MachineStatus ms : environmentStatus.values()) {
			ms.clear();
		}
	}

	@Override
	public void setParameters(Map<String, Object> parameters) throws Exception {
		// nothing to do
	}

	@Override
	public Map<PhysicalMachine, MachineStatus> getPhysicalMachineStatus() {
		return environmentStatus;
	}
}
