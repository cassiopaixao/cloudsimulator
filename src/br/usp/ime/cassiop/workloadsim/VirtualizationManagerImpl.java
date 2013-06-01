package br.usp.ime.cassiop.workloadsim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.environment.HomogeneousCluster;
import br.usp.ime.cassiop.workloadsim.exceptions.IncompatibleObjectsException;
import br.usp.ime.cassiop.workloadsim.exceptions.InvalidParameterException;
import br.usp.ime.cassiop.workloadsim.exceptions.NoMoreServersAvailableException;
import br.usp.ime.cassiop.workloadsim.exceptions.ServerNotEmptyException;
import br.usp.ime.cassiop.workloadsim.exceptions.ServerOverloadedException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownServerException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownVirtualMachineException;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class VirtualizationManagerImpl implements Parametrizable,
		VirtualizationManager {

	protected HashMap<String, Server> serverMap = null;

	protected HashMap<String, VirtualMachine> vmMap = null;

	private long usedServers = 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.usp.ime.cassiop.workloadsim.VirtualizationManager#getActiveVirtualMachines
	 * ()
	 */
	@Override
	public HashMap<String, VirtualMachine> getActiveVirtualMachines() {
		return vmMap;
	}

	protected Environment environment = null;

	protected StatisticsModule statisticsModule = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.usp.ime.cassiop.workloadsim.VirtualizationManager#setStatisticsModule
	 * (br.usp.ime.cassiop.workloadsim.StatisticsModule)
	 */
	@Override
	public void setStatisticsModule(StatisticsModule statisticsModule) {
		this.statisticsModule = statisticsModule;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.usp.ime.cassiop.workloadsim.VirtualizationManager#getEnvironment()
	 */
	@Override
	public Environment getEnvironment() {
		return environment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.usp.ime.cassiop.workloadsim.VirtualizationManager#setEnvironment(br
	 * .usp.ime.cassiop.workloadsim.Environment)
	 */
	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	final Logger logger = LoggerFactory
			.getLogger(VirtualizationManagerImpl.class);

	public VirtualizationManagerImpl() {
		serverMap = new HashMap<String, Server>();
		vmMap = new HashMap<String, VirtualMachine>();
		setEnvironment(new HomogeneousCluster());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.usp.ime.cassiop.workloadsim.VirtualizationManager#setVmToServer(br
	 * .usp.ime.cassiop.workloadsim.model.VirtualMachine,
	 * br.usp.ime.cassiop.workloadsim.model.Server)
	 */
	@Override
	public void setVmToServer(VirtualMachine vm, Server server)
			throws UnknownVirtualMachineException, UnknownServerException {

		if (server == null || serverMap.get(server.getName()) == null
				|| serverMap.get(server.getName()) != server) {
			throw new UnknownServerException();
		}
		if (vm == null) {
			throw new UnknownVirtualMachineException();
		}

		if (!server.canHost(vm)) {
			logger.debug(
					"Server {} could be overloaded. Virtual machine {}'s demands extrapolates the pm resources' capacities",
					server.getName(), vm.getName());
		}

		if (vm.getCurrentServer() != null) {
			if (vm.getCurrentServer() == server) {
				try {
					server.updateVm(vm);
					return;
				} catch (ServerOverloadedException e) {
				}
			} else {
				try {
					deallocate(vm);
				} catch (UnknownVirtualMachineException e) {
				}
			}
		}

		// logger.info("Adding VM {} to server {} (type: {})", vm.getName(),
		// server.getName(), server.getType());
		server.addVirtualMachine(vm);
		// logger.info("VM {} added to server {} (type: {})", vm.getName(),
		// server.getName(), server.getType());

		vmMap.put(vm.getName(), vm);

		if (statisticsModule != null) {
			if (vm.getLastServer() != null
					&& !vm.getLastServer().getName().equals(server.getName())) {
				statisticsModule.addToStatisticValue(
						Constants.STATISTIC_MIGRATIONS, 1);
				statisticsModule.addToStatisticValue(
						Constants.STATISTIC_MIGRATIONS_COST,
						vm.getResourceUtilization());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.usp.ime.cassiop.workloadsim.VirtualizationManager#getActiveServersList
	 * ()
	 */
	@Override
	public Collection<Server> getActiveServersList() {
		return serverMap.values();
	}

	@Override
	public Server getNextInactiveServer(VirtualMachine vmDemand,
			ServerTypeChooser pmTypeChooser)
			throws NoMoreServersAvailableException {

		Server selectedMachine = pmTypeChooser.chooseServerType(vmDemand,
				environment.getAvailableMachineTypes());

		if (selectedMachine == null) {
			selectedMachine = pmTypeChooser.chooseServerTypeEvenOverloading(
					vmDemand, environment.getAvailableMachineTypes());
		}

		if (selectedMachine == null) {
			throw new NoMoreServersAvailableException();
		}

		Server newServer;
		try {
			newServer = activateServerOfType(selectedMachine);
		} catch (UnknownServerException e) {
			logger.error("Tryed to activate an unknown Server type: {}",
					selectedMachine.getName());
			throw new NoMoreServersAvailableException();
		}

		logger.debug("Server {} activated: {}", newServer.getName(),
				newServer.toString());

		return newServer;
	}

	public Server activateServerOfType(Server selectedMachine)
			throws UnknownServerException, NoMoreServersAvailableException {
		Server server = environment.getMachineOfType(selectedMachine);

		server.setName(new Long(++usedServers).toString());

		serverMap.put(server.getName(), server);

		return server;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.usp.ime.cassiop.workloadsim.VirtualizationManager#clear()
	 */
	@Override
	public void clear() {
		for (Server server : serverMap.values()) {
			server.clear();
		}
		vmMap.clear();
		serverMap.clear();
		environment.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.usp.ime.cassiop.workloadsim.VirtualizationManager#setParameters(java
	 * .util.Map)
	 */
	@Override
	public void setParameters(Map<String, Object> parameters)
			throws InvalidParameterException {
		Object o = parameters.get(Constants.PARAMETER_ENVIRONMENT);
		if (o instanceof Environment) {
			setEnvironment((Environment) o);
		} else {
			throw new InvalidParameterException(
					Constants.PARAMETER_ENVIRONMENT, Environment.class);
		}

		o = parameters.get(Constants.PARAMETER_STATISTICS_MODULE);
		if (o instanceof StatisticsModule) {
			setStatisticsModule((StatisticsModule) o);
		} else {
			throw new InvalidParameterException(
					Constants.PARAMETER_STATISTICS_MODULE,
					StatisticsModule.class);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.usp.ime.cassiop.workloadsim.VirtualizationManager#deallocateFinishedVms
	 * (java.util.List, long)
	 */
	@Override
	public void deallocateFinishedVms(List<VirtualMachine> demand,
			long currentTime) {
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
						try {
							deallocate(vm);
						} catch (UnknownVirtualMachineException e) {
							logger.error(
									"Tryed to deallocate an unknown virtual machine: {}",
									vm);
						} catch (UnknownServerException e) {
							logger.error(
									"Tryed to deallocate a virtual machine ({}) from an unknown server: {}",
									vm, vm.getCurrentServer());
						}
					}
				} else {
					newVirtualMachines--;
				}
			}
		}

		statisticsModule.setStatisticValue(
				Constants.STATISTIC_NEW_VIRTUAL_MACHINES, newVirtualMachines);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.usp.ime.cassiop.workloadsim.VirtualizationManager#deallocate(br.usp
	 * .ime.cassiop.workloadsim.model.VirtualMachine)
	 */
	@Override
	public void deallocate(VirtualMachine vm)
			throws UnknownVirtualMachineException, UnknownServerException {
		if (vm == null) {
			throw new UnknownVirtualMachineException();
		}

		Server currentServer = vm.getCurrentServer();

		if (currentServer != null
				&& serverMap.get(currentServer.getName()) == currentServer) {
			currentServer.removeVirtualMachine(vm);
			vmMap.remove(vm.getName());
		} else {
			throw new UnknownServerException();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.usp.ime.cassiop.workloadsim.VirtualizationManager#turnOffServer(br
	 * .usp.ime.cassiop.workloadsim.model.Server)
	 */
	@Override
	public void turnOffServer(Server server) throws UnknownServerException,
			ServerNotEmptyException, NoMoreServersAvailableException {
		if (server == null || serverMap.get(server.getName()) == null) {
			throw new UnknownServerException(
					"Tryed to turn off an unknown server.");
		}
		if (!server.getVirtualMachines().isEmpty()) {
			throw new ServerNotEmptyException(
					"Tryed to turn off a server with active virtual machines.");
		}

		serverMap.remove(server.getName());
		environment.turnOffMachineOfType(server);
	}

	@Override
	public void copyAllocationStatus(
			VirtualizationManager virtualizationManager,
			List<VirtualMachine> demand) throws IncompatibleObjectsException {
		/*
		 * verifica se ambiente é o mesmo limpa alocação atual ** provavelmente
		 * isso mudará depois.. **
		 * 
		 * desliga máquinas
		 * 
		 * para cada máquina no objeto do parâmetro:
		 * 
		 * ativa um servidor do mesmo tipo
		 * 
		 * adiciona as vms *** tem q ser as originais, e não as clonadas ***
		 */
		if (!environment.isSubset(virtualizationManager.getEnvironment())) {
			throw new IncompatibleObjectsException(
					"Environments aren't compatible.");
		}

		List<String> oldServers = new ArrayList<String>(serverMap.keySet());
		for (Server server : serverMap.values()) {
			server.clear();
		}
		for (String serverName : oldServers) {
			try {
				turnOffServer(serverMap.get(serverName));
			} catch (UnknownServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServerNotEmptyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoMoreServersAvailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		HashMap<String, Server> serverTypes = new HashMap<String, Server>();
		for (Server serverType : environment.getAvailableMachineTypes()) {
			serverTypes.put(serverType.getType(), serverType);
		}

		HashMap<String, VirtualMachine> originalDemand = new HashMap<String, VirtualMachine>();
		for (VirtualMachine vm : demand) {
			originalDemand.put(vm.getName(), vm);
		}

		for (Server serverToCopy : virtualizationManager.getActiveServersList()) {
			Server server;
			try {
				server = activateServerOfType(serverTypes.get(serverToCopy
						.getType()));

				for (VirtualMachine vmToCopy : serverToCopy
						.getVirtualMachines()) {
					server.addVirtualMachine(originalDemand.get(vmToCopy
							.getName()));
				}

			} catch (UnknownServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoMoreServersAvailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownVirtualMachineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}
