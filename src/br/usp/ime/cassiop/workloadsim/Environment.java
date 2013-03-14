package br.usp.ime.cassiop.workloadsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.usp.ime.cassiop.workloadsim.environment.MachineStatus;
import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.util.Constants;
import br.usp.ime.cassiop.workloadsim.util.MathUtils;

public abstract class Environment implements Parametrizable {
	private Map<Server, MachineStatus> environmentStatus = null;

	private double environmentMultiplier = 1.0;

	public Environment() {
		environmentStatus = new HashMap<Server, MachineStatus>();
	}

	public Server getMachineOfType(Server pm) throws Exception {
		MachineStatus status = environmentStatus.get(pm);
		if (status == null) {
			throw new Exception(
					"There is no Physical Machines of this type in the environment.");
		}

		status.useOne();

		Server newPm = new Server();
		newPm.setCapacity(ResourceType.CPU, pm.getCapacity(ResourceType.CPU));
		newPm.setCapacity(ResourceType.MEMORY,
				pm.getCapacity(ResourceType.MEMORY));

		return newPm;
	}

	public List<Server> getAvailableMachineTypes() {
		List<Server> availableMachines = new ArrayList<Server>();

		for (Server pm : environmentStatus.keySet()) {
			if (environmentStatus.get(pm).getAvailable() > environmentStatus
					.get(pm).getUsed()) {
				availableMachines.add(pm);
			}
		}

		return availableMachines;
	}

	public Map<Server, MachineStatus> getEnvironmentStatus() {
		return environmentStatus;
	}

	public void clear() {
		clear(false);
	}
	
	public void clear(boolean deleteMachinesInfo) {
		for (MachineStatus ms : environmentStatus.values()) {
			ms.clear();
		}
		if (deleteMachinesInfo) {
			environmentStatus.clear();
		}
	}

	public void turnOffMachineOfType(Server server) throws Exception {
		for (Server sv : environmentStatus.keySet()) {
			if (server.getType().equals(sv.getType())) {
				environmentStatus.get(sv).turnOffOne();
				break;
			}
		}
	}

	protected void addPmStatus(int machinesAvailable, double cpuCapacity,
			double memoryCapacity) {
		if (MathUtils.equals(machinesAvailable * environmentMultiplier, 0)
				|| ((int) (machinesAvailable * environmentMultiplier) == 0)) {
			return;
		}

		Server pm = new Server();
		MachineStatus status = new MachineStatus();

		status.setAvailable((int) (machinesAvailable * environmentMultiplier));
		pm.setCapacity(ResourceType.CPU, cpuCapacity);
		pm.setCapacity(ResourceType.MEMORY, memoryCapacity);

		environmentStatus.put(pm, status);
	}

	public void setParameters(Map<String, Object> parameters) throws Exception {
		Object o = parameters.get(Constants.PARAMETER_ENVIRONMENT_MULTIPLIER);
		if (o instanceof Double) {
			setEnvironmentMultiplier(((Double) o).doubleValue());
		} else {
			throw new Exception(String.format("Invalid parameter: %s",
					Constants.PARAMETER_ENVIRONMENT_MULTIPLIER));
		}
	}

	private void setEnvironmentMultiplier(double environmentMultiplier) {
		this.environmentMultiplier = environmentMultiplier;
		initialize();
	}

	protected abstract void initialize();
}