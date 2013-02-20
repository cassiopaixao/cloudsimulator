package br.usp.ime.cassiop.workloadsim;

import java.util.List;
import java.util.Map;

import br.usp.ime.cassiop.workloadsim.environment.MachineStatus;
import br.usp.ime.cassiop.workloadsim.model.Server;

public interface Environment extends Parametrizable {

	public abstract Server getMachineOfType(Server pm)
			throws Exception;

	public abstract List<Server> getAvailableMachineTypes();

	public abstract Map<Server, MachineStatus> getEnvironmentStatus();
	
	public abstract void clear();

	public abstract void turnOffMachineOfType(Server server) throws Exception;

}