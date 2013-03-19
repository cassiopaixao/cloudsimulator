package br.usp.ime.cassiop.workloadsim;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.usp.ime.cassiop.workloadsim.exceptions.InvalidParameterException;
import br.usp.ime.cassiop.workloadsim.exceptions.NoMoreServersAvailableException;
import br.usp.ime.cassiop.workloadsim.exceptions.ServerNotEmptyException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownServerException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownVirtualMachineException;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;

public interface VirtualizationManager {

	public abstract HashMap<String, VirtualMachine> getActiveVirtualMachines();

	public abstract void setStatisticsModule(StatisticsModule statisticsModule);

	public abstract Environment getEnvironment();

	public abstract void setEnvironment(Environment environment);

	public abstract void setVmToServer(VirtualMachine vm, Server server)
			throws UnknownVirtualMachineException, UnknownServerException;

	public abstract Collection<Server> getActiveServersList();

	public abstract Server getNextInactiveServer(VirtualMachine vmDemand,
			ServerTypeChooser pmTypeChooser)
			throws NoMoreServersAvailableException;

	public abstract Server activateServerOfType(Server selectedMachine)
			throws UnknownServerException, NoMoreServersAvailableException;

	public abstract void clear();

	public abstract void setParameters(Map<String, Object> parameters)
			throws InvalidParameterException;

	public abstract void deallocateFinishedVms(List<VirtualMachine> demand,
			long currentTime);

	public abstract void deallocate(VirtualMachine vm)
			throws UnknownVirtualMachineException, UnknownServerException;

	public abstract void turnOffServer(Server server)
			throws UnknownServerException, ServerNotEmptyException;

}