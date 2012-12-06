package br.usp.ime.cassiop.workloadsim.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.usp.ime.cassiop.workloadsim.Environment;
import br.usp.ime.cassiop.workloadsim.model.PhysicalMachine;
import br.usp.ime.cassiop.workloadsim.model.ResourceType;

public class GoogleCluster implements Environment {
	private Map<PhysicalMachine, MachineStatus> environment = null;

	public GoogleCluster() {
		initialize();
	}

	private void initialize() {
		environment = new HashMap<PhysicalMachine, MachineStatus>();

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

	private void addPmStatus(int machinesAvailable, double cpuCapacity,
			double memoryCapacity) {
		PhysicalMachine pm = new PhysicalMachine();
		MachineStatus status = new MachineStatus();

		status.setAvailable(machinesAvailable);
		pm.setCapacity(ResourceType.CPU, cpuCapacity);
		pm.setCapacity(ResourceType.MEMORY, memoryCapacity);

		environment.put(pm, status);
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
		MachineStatus status = environment.get(pm);
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
	public List<PhysicalMachine> getMachineTypes() {
		List<PhysicalMachine> availableMachines = new ArrayList<PhysicalMachine>();

		for (PhysicalMachine pm : environment.keySet()) {
			if (environment.get(pm).getAvailable() > environment.get(pm)
					.getUsed()) {
				availableMachines.add(pm);
			}
		}

		return availableMachines;
	}

	private class MachineStatus {
		private int available = 0;
		private int used = 0;

		public int getAvailable() {
			return available;
		}

		public void setAvailable(int available) {
			this.available = available;
		}

		public int getUsed() {
			return used;
		}

		public void useOne() throws Exception {
			if (used == available) {
				throw new Exception(
						"There is no more Physical Machines of this type available.");
			}
			used++;
		}

		public void clear() {
			used = 0;
		}

	}

	@Override
	public void clear() {
		for (MachineStatus ms : environment.values()) {
			ms.clear();
		}
	}

}
