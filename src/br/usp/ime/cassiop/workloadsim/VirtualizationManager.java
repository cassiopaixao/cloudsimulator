package br.usp.ime.cassiop.workloadsim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.environment.IdealCluster;
import br.usp.ime.cassiop.workloadsim.exceptions.ServerNotEmptyException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownServerException;
import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
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
		setEnvironment(new IdealCluster());
	}

	public void setVmToServer(VirtualMachine vm, Server server)
			throws Exception {
		if (serverMap.get(server.getName()) != null
				&& serverMap.get(server.getName()) == server) {

			if (!server.canHost(vm)) {
				logger.info(
						"Server {} could be overloaded. Virtual machine {}'s demands extrapolates the pm resources' capacities",
						server.getName(), vm.getName());
			}

			server.addVirtualMachine(vm);

			vmMap.put(vm.getName(), vm);

			if (vm.getLastServer() != null
					&& !vm.getLastServer().getName().equals(server.getName())) {
				statisticsModule.addToStatisticValue(
						Constants.STATISTIC_MIGRATIONS, 1);
			}
		} else {
			throw new Exception("Unknown destination server.");
		}
	}

	public Collection<Server> getActiveServerList() {
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

		Server pm = environment.getMachineOfType(selectedMachine);

		pm.setName(new Long(++usedServers).toString());

		logger.debug("PhysicalMachine {} activated: {}", pm.getName(),
				pm.toString());

		serverMap.put(pm.getName(), pm);

		return pm;
	}

	public void clear() {
		for (Server pm : serverMap.values()) {
			pm.clear();
		}
		environment.clear();
	}

	public class FirstFitTypeChooser implements PhysicalMachineTypeChooser {

		public Server chooseServerType(List<Server> machineTypes,
				VirtualMachine vmDemand) {
			Server selectedMachine = null;

			Collections.sort(machineTypes);

			for (Server server : machineTypes) {
				if (server.canHost(vmDemand)) {
					selectedMachine = server;
					break;
				}
			}

			if (selectedMachine == null) {
				logger.info("No inactive physical machine can satisfy the virtual machine's demand. Activating the physical machine with lowest loss of performance.");

				Server lessLossOfPerformanceMachine = null;
				double lessLossOfPerformance = Double.MAX_VALUE;

				for (Server pm : machineTypes) {
					if (!pm.canHost(vmDemand)) {
						if (lossOfPerformance(pm, vmDemand) < lessLossOfPerformance) {
							lessLossOfPerformance = lossOfPerformance(pm,
									vmDemand);
							lessLossOfPerformanceMachine = pm;
						}
					}
				}

				if (lessLossOfPerformanceMachine == null) {
					logger.info("There is no inactive physical machine. Need to overload one.");
					return null;
				}

				selectedMachine = lessLossOfPerformanceMachine;
			}

			return selectedMachine;
		}

		private double lossOfPerformance(Server pm, VirtualMachine vm) {
			double leavingCpu, leavingMem;
			double sum = 0;
			leavingCpu = pm.getFreeResource(ResourceType.CPU)
					- vm.getDemand(ResourceType.CPU);
			leavingMem = pm.getFreeResource(ResourceType.MEMORY)
					- vm.getDemand(ResourceType.MEMORY);

			sum += (leavingCpu < 0) ? -leavingCpu : 0;
			sum += (leavingMem < 0) ? -leavingMem : 0;

			return sum;
		}

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

		List<String> activeVms = new ArrayList<String>(vmMap.keySet());

		VirtualMachine vm = null;

		// for each vm already allocated
		for (String vmName : activeVms) {
			// if it isn't in the new demands' list
			if (!keepRunning.containsKey(vmName)) {
				vm = vmMap.get(vmName);
				// and it's endTime was reached
				if (vm.getEndTime() <= currentTime) {
					// should deallocate the resources.
					deallocate(vm);
				}
			}
		}
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
