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

	public HashMap<String, VirtualMachine> getActiveVirtualMachines();

	public void setStatisticsModule(StatisticsModule statisticsModule);

	public Environment getEnvironment();

	public void setEnvironment(Environment environment);

	public void setVmToServer(VirtualMachine vm, Server server)
			throws UnknownVirtualMachineException, UnknownServerException;

	public Collection<Server> getActiveServersList();

	public Server getNextInactiveServer(VirtualMachine vmDemand,
			ServerTypeChooser pmTypeChooser)
			throws NoMoreServersAvailableException;

	public Server activateServerOfType(Server selectedMachine)
			throws UnknownServerException, NoMoreServersAvailableException;

	public void clear();

	public void setParameters(Map<String, Object> parameters)
			throws InvalidParameterException;

	public void deallocateFinishedVms(List<VirtualMachine> demand,
			long currentTime);

	public void deallocate(VirtualMachine vm)
			throws UnknownVirtualMachineException, UnknownServerException;

	public void turnOffServer(Server server) throws UnknownServerException,
			ServerNotEmptyException, NoMoreServersAvailableException;

}