package br.usp.ime.cassiop.workloadsim;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.exceptions.DependencyNotSetException;
import br.usp.ime.cassiop.workloadsim.exceptions.InvalidParameterException;
import br.usp.ime.cassiop.workloadsim.exceptions.NoMoreServersAvailableException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownServerException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownVirtualMachineException;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.placement.PlacementStrategy;
import br.usp.ime.cassiop.workloadsim.placement.PlacementUtils;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class PlacementModule implements Parametrizable {

	final Logger logger = LoggerFactory.getLogger(PlacementModule.class);

	protected VirtualizationManager virtualizationManager = null;

	protected StatisticsModule statisticsModule = null;

	protected PlacementUtils placementUtils = null;

	protected PlacementStrategy placementStrategy = null;

	public void setVirtualizationManager(
			VirtualizationManager virtualizationManager) {
		this.virtualizationManager = virtualizationManager;
	}

	public void setStatisticsModule(StatisticsModule statisticsModule) {
		this.statisticsModule = statisticsModule;
	}

	public void setPlacementUtils(PlacementUtils placementUtils) {
		this.placementUtils = placementUtils;
	}

	public void setPlacementStrategy(PlacementStrategy placementStrategy) {
		this.placementStrategy = placementStrategy;
	}

	public void setParameters(Map<String, Object> parameters)
			throws InvalidParameterException {
		Object o = null;

		o = parameters.get(Constants.PARAMETER_VIRTUALIZATION_MANAGER);
		if (o instanceof VirtualizationManager) {
			setVirtualizationManager((VirtualizationManager) o);
		} else {
			throw new InvalidParameterException(
					Constants.PARAMETER_VIRTUALIZATION_MANAGER,
					VirtualizationManager.class);
		}

		o = parameters.get(Constants.PARAMETER_STATISTICS_MODULE);
		if (o instanceof StatisticsModule) {
			setStatisticsModule((StatisticsModule) o);
		} else {
			throw new InvalidParameterException(
					Constants.PARAMETER_STATISTICS_MODULE,
					StatisticsModule.class);
		}

		o = parameters.get(Constants.PARAMETER_PLACEMENT_STRATEGY);
		if (o instanceof PlacementStrategy) {
			setPlacementStrategy((PlacementStrategy) o);
		} else {
			throw new InvalidParameterException(
					Constants.PARAMETER_PLACEMENT_STRATEGY,
					PlacementStrategy.class);
		}

		o = parameters.get(Constants.PARAMETER_PLACEMENT_UTILS);
		if (o instanceof PlacementUtils) {
			setPlacementUtils((PlacementUtils) o);
		} else {
			throw new InvalidParameterException(
					Constants.PARAMETER_PLACEMENT_UTILS, PlacementUtils.class);
		}
	}

	protected void verifyDependencies(List<VirtualMachine> demand)
			throws DependencyNotSetException {
		if (virtualizationManager == null) {
			throw new DependencyNotSetException(
					"VirtualizationManager is not set.");
		}
		if (statisticsModule == null) {
			throw new DependencyNotSetException("StatisticsModule is not set.");
		}
		if (placementUtils == null) {
			throw new DependencyNotSetException("PlacementUtils is not set.");
		}
		if (placementStrategy == null) {
			throw new DependencyNotSetException("PlacementStrategy is not set.");
		}
		if (demand == null) {
			throw new DependencyNotSetException("Demand is not set.");
		}
	}

	public void consolidateAll(List<VirtualMachine> demand)
			throws DependencyNotSetException {
		verifyDependencies(demand);

		List<VirtualMachine> virtualMachines = new ArrayList<VirtualMachine>(
				demand);
		List<Server> servers = new ArrayList<Server>(
				virtualizationManager.getActiveServersList());

		placementStrategy.orderServers(servers);
		placementStrategy.orderDemand(virtualMachines);

		for (VirtualMachine vm : virtualMachines) {
			try {
				allocate(vm, servers);
			} catch (UnknownVirtualMachineException e) {
				logger.error("UnknownVirtualMachineException thrown. VM: {}",
						vm);
			} catch (UnknownServerException e) {
				logger.error("UnknownServerException thrown. {}",
						e.getMessage());
			}
		}
	}

	public final void allocate(VirtualMachine vm, List<Server> servers)
			throws UnknownVirtualMachineException, UnknownServerException {

		Server destinationServer = placementStrategy.selectDestinationServer(
				vm, servers);

		if (destinationServer == null) {
			try {
				destinationServer = virtualizationManager
						.getNextInactiveServer(vm, placementStrategy);

				if (destinationServer != null) {
					servers.add(destinationServer);
				}
			} catch (NoMoreServersAvailableException e) {
			}
		}

		if (destinationServer == null) {
			destinationServer = placementUtils.lessLossEmptyServer(servers, vm);
		}

		if (destinationServer == null) {
			logger.debug("No server could allocate the virtual machine: {}.",
					vm.toString());

			statisticsModule.addToStatisticValue(
					Constants.STATISTIC_VIRTUAL_MACHINES_NOT_ALLOCATED, 1);

		}

		if (destinationServer != null) {
			virtualizationManager.setVmToServer(vm, destinationServer);
		}
	}
}