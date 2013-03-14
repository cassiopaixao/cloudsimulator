package br.usp.ime.cassiop.workloadsim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.environment.HomogeneousCluster;
import br.usp.ime.cassiop.workloadsim.exceptions.ServerNotEmptyException;
import br.usp.ime.cassiop.workloadsim.exceptions.ServerOverloadedException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownServerException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownVirtualMachineException;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.placement.FirstFitTypeChooser;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class VirtualizationManager implements Parametrizable {

	protected HashMap<String, Server> serverMap = null;

	protected HashMap<String, VirtualMachine> vmMap = null;

	private long usedServers = 0;

	public HashMap<String, VirtualMachine> getActiveVirtualMachines() {
		return vmMap;
	}

	protected Environment environment = null;

	protected StatisticsModule statisticsModule = null;

	public void setStatisticsModule(StatisticsModule statisticsModule) {
		this.statisticsModule = statisticsModule;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	final Logger logger = LoggerFactory.getLogger(VirtualizationManager.class);

	public VirtualizationManager() {
		serverMap = new HashMap<String, Server>();
		vmMap = new HashMap<String, VirtualMachine>();
		setEnvironment(new HomogeneousCluster());
	}

	public void setVmToServer(VirtualMachine vm, Server server)
			throws UnknownVirtualMachineException, UnknownServerException {
		if (serverMap.get(server.getName()) != null
				&& serverMap.get(server.getName()) == server) {

			if (!server.canHost(vm)) {
				logger.info(
						"Server {} could be overloaded. Virtual machine {}'s demands extrapolates the pm resources' capacities",
						server.getName(), vm.getName());
			}

			try {
				server.addVirtualMachine(vm);
			} catch (ServerOverloadedException ex) {
			}

			vmMap.put(vm.getName(), vm);

			if (vm.getLastServer() != null
					&& !vm.getLastServer().getName().equals(server.getName())) {
				statisticsModule.addToStatisticValue(
						Constants.STATISTIC_MIGRATIONS, 1);
				statisticsModule.addToStatisticValue(
						Constants.STATISTIC_MIGRATIONS_COST,
						vm.getResourceUtilization());
			}
		} else {
			throw new UnknownServerException();
		}
	}

	public Collection<Server> getActiveServersList() {
		return serverMap.values();
	}

	public Server getNextInactiveServer(VirtualMachine vmDemand)
			throws Exception {

		return getNextInactiveServer(vmDemand, new FirstFitTypeChooser());
	}

	public Server getNextInactiveServer(VirtualMachine vmDemand,
			PhysicalMachineTypeChooser pmTypeChooser) throws Exception {

		Server selectedMachine = pmTypeChooser.chooseServerType(
				environment.getAvailableMachineTypes(), vmDemand);

		if (selectedMachine == null) {
			logger.info("No more physical machines available.");
			return null;
		}

		Server server = environment.getMachineOfType(selectedMachine);

		server.setName(new Long(++usedServers).toString());

		logger.debug("PhysicalMachine {} activated: {}", server.getName(),
				server.toString());

		serverMap.put(server.getName(), server);

		return server;
	}

	public void clear() {
		for (Server pm : serverMap.values()) {
			pm.clear();
		}
		environment.clear();
	}

	@Override
	public void setParameters(Map<String, Object> parameters) throws Exception {
		Object o = parameters.get(Constants.PARAMETER_ENVIRONMENT);
		if (o instanceof Environment) {
			setEnvironment((Environment) o);
		} else {
			throw new Exception(String.format("Invalid parameter: %s",
					Constants.PARAMETER_ENVIRONMENT));
		}

		o = parameters.get(Constants.PARAMETER_STATISTICS_MODULE);
		if (o instanceof StatisticsModule) {
			setStatisticsModule((StatisticsModule) o);
		} else {
			throw new Exception(String.format("Invalid parameter: %s",
					Constants.PARAMETER_STATISTICS_MODULE));
		}
	}

	public void deallocateFinishedVms(List<VirtualMachine> demand,
			long currentTime) throws Exception {
		Map<String, Object> keepRunning = new HashMap<String, Object>(
				demand.size());
		for (VirtualMachine vm : demand) {
			keepRunning.put(vm.getName(), null);
		}

		int newVirtualMachines = demand.size();

		for (Server server : serverMap.values()) {
			List<VirtualMachine> vms = new ArrayList<VirtualMachine>(
					server.getVirtualMachines());
			for (VirtualMachine vm : vms) {
				if (!keepRunning.containsKey(vm.getName())) {
					if (vm.getEndTime() <= currentTime) {
						deallocate(vm);
					}
				} else {
					newVirtualMachines--;
				}
			}
		}

		statisticsModule.setStatisticValue(
				Constants.STATISTIC_NEW_VIRTUAL_MACHINES, newVirtualMachines);

	}

	public void deallocate(VirtualMachine vm) throws Exception {
		Server currentServer = vm.getCurrentServer();

		currentServer.removeVirtualMachine(vm);
		vmMap.remove(vm.getName());
	}

	public void turnOffServer(Server server) throws Exception {
		if (!server.getVirtualMachines().isEmpty()) {
			throw new ServerNotEmptyException();
		}
		if (serverMap.get(server.getName()) == null) {
			throw new UnknownServerException();
		}

		serverMap.remove(server.getName());
		environment.turnOffMachineOfType(server);
	}
}
