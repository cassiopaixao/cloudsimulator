package br.usp.ime.cassiop.workloadsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.usp.ime.cassiop.workloadsim.environment.MachineStatus;
import br.usp.ime.cassiop.workloadsim.exceptions.InvalidParameterException;
import br.usp.ime.cassiop.workloadsim.exceptions.NoMoreServersAvailableException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownServerException;
import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.util.Constants;
import br.usp.ime.cassiop.workloadsim.util.MathUtils;

public abstract class Environment implements Parametrizable {
	private Map<Server, MachineStatus> environmentStatus = null;

	private double environmentMultiplier = 1.0;

	public Environment() {
		environmentStatus = new HashMap<Server, MachineStatus>();
		initialize();
	}

	public Server getMachineOfType(Server server)
			throws UnknownServerException, NoMoreServersAvailableException {
		MachineStatus status = environmentStatus.get(server);
		if (status == null) {
			throw new UnknownServerException(
					"There is no Physical Machines of this type in the environment.");
		}

		status.useOne();

		Server newServer = server.clone();

		return newServer;
	}

	public List<Server> getAvailableMachineTypes() {
		List<Server> availableMachines = new ArrayList<Server>();

		for (Server server : environmentStatus.keySet()) {
			if (environmentStatus.get(server).getAvailable() > environmentStatus
					.get(server).getUsed()) {
				availableMachines.add(server);
			}
		}

		return availableMachines;
	}

	public List<Server> getAllMachineTypes() {
		return new ArrayList<Server>(environmentStatus.keySet());
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

	public void turnOffMachineOfType(Server server)
			throws NoMoreServersAvailableException {
		for (Server sv : environmentStatus.keySet()) {
			if (server.getType().equals(sv.getType())) {
				environmentStatus.get(sv).turnOffOne();
				break;
			}
		}
	}

	protected void addServerStatus(int machinesAvailable, double cpuCapacity,
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

	public void setParameters(Map<String, Object> parameters)
			throws InvalidParameterException {
		Object o = parameters.get(Constants.PARAMETER_ENVIRONMENT_MULTIPLIER);
		if (o instanceof Double) {
			setEnvironmentMultiplier(((Double) o).doubleValue());
		} else {
			throw new InvalidParameterException(
					Constants.PARAMETER_ENVIRONMENT_MULTIPLIER, Double.class);
		}
	}

	private void setEnvironmentMultiplier(double environmentMultiplier) {
		this.environmentMultiplier = environmentMultiplier;
		initialize();
	}

	protected abstract void initialize();

	@Override
	public Environment clone() {
		Environment newEnvironment = null;
		try {
			newEnvironment = this.getClass().newInstance();
			newEnvironment.setEnvironmentMultiplier(environmentMultiplier);
			newEnvironment.initialize();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newEnvironment;
	}

	public boolean isSubset(Environment environment) {
		HashMap<String, Server> keyList = new HashMap<String, Server>();
		for (Server server : environmentStatus.keySet()) {
			keyList.put(server.getType(), server);
		}

		for (Server server : environment.environmentStatus.keySet()) {
			MachineStatus status = environmentStatus.get(keyList.get(server
					.getType()));
			if (status == null) {
				return false;
			}

			if (status.getAvailable() < environment.environmentStatus.get(
					server).getAvailable()) {
				return false;
			}
		}

		return true;
	}
}