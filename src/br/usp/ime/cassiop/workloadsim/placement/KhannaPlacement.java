package br.usp.ime.cassiop.workloadsim.placement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.StatisticsModule;
import br.usp.ime.cassiop.workloadsim.VirtualizationManager;
import br.usp.ime.cassiop.workloadsim.exceptions.DependencyNotSetException;
import br.usp.ime.cassiop.workloadsim.exceptions.InvalidParameterException;
import br.usp.ime.cassiop.workloadsim.exceptions.NoMoreServersAvailableException;
import br.usp.ime.cassiop.workloadsim.exceptions.ServerOverloadedException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownServerException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownVirtualMachineException;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.placement.KhannaTypeChooser.ServerOrderedByResidualCapacityComparator;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class KhannaPlacement implements PlacementWithPowerOffStrategy {

	private VirtualizationManager virtualizationManager = null;

	private StatisticsModule statisticsModule = null;

	private int vms_not_allocated;

	private int servers_turned_off;

	private double lowUtilization = 0;

	public void setLowUtilization(double lowUtilization) {
		this.lowUtilization = lowUtilization;
	}

	public void setStatisticsModule(StatisticsModule statisticsModule) {
		this.statisticsModule = statisticsModule;
	}

	final Logger logger = LoggerFactory.getLogger(KhannaPlacement.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.ime.usp.cassiop.workloadsim.PlacementModule#setVirtualizationManager
	 * (br.ime.usp.cassiop.workloadsim.VirtualizationManager)
	 */
	@Override
	public void setVirtualizationManager(
			VirtualizationManager virtualizationManager) {
		this.virtualizationManager = virtualizationManager;
	}

	@Override
	public void consolidateAll(List<VirtualMachine> demand)
			throws DependencyNotSetException {
		if (virtualizationManager == null) {
			throw new DependencyNotSetException(
					"VirtualizationManager is not set.");
		}
		if (demand == null) {
			throw new DependencyNotSetException("Demand is not set.");
		}

		List<Server> servers = new ArrayList<Server>(
				virtualizationManager.getActiveServersList());

		vms_not_allocated = 0;

		for (VirtualMachine vm : demand) {
			try {
				if (vm.getCurrentServer() == null) {
					allocate(vm, servers);
				} else {
					try {
						vm.getCurrentServer().updateVm(vm);
						if (vm.getCurrentServer().isAlmostOverloaded()) {
							migrate(vm.getCurrentServer(), servers);
						}
					} catch (ServerOverloadedException ex) {
						migrate(vm.getCurrentServer(), servers);
					}
				}
			} catch (UnknownVirtualMachineException e) {
				logger.error("UnknownVirtualMachineException thrown. VM: {}",
						vm);
			} catch (UnknownServerException e) {
				logger.error("UnknownServerException thrown. {}",
						e.getMessage());
			}
		}

		for (VirtualMachine vm : demand) {
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

		try {
			servers_turned_off = PowerOffStrategy.powerOff(servers,
					lowUtilization, this, statisticsModule,
					virtualizationManager);
		} catch (UnknownServerException e) {
			logger.error(e.getMessage());
			servers_turned_off = -1;
		}

		statisticsModule.addToStatisticValue(
				Constants.STATISTIC_VIRTUAL_MACHINES_NOT_ALLOCATED,
				vms_not_allocated);

		statisticsModule.addToStatisticValue(
				Constants.STATISTIC_SERVERS_TURNED_OFF, servers_turned_off);
	}

	private void migrate(Server overloadedServer, List<Server> servers) {

		while (overloadedServer.isAlmostOverloaded()) {

			VirtualMachine smallestVm = null;
			double smallestVmResourceUtilization = Double.MAX_VALUE;

			for (VirtualMachine vm : overloadedServer.getVirtualMachines()) {
				if (vm.getResourceUtilization() < smallestVmResourceUtilization) {
					smallestVm = vm;
					smallestVmResourceUtilization = smallestVm
							.getResourceUtilization();
				}
			}

			try {
				virtualizationManager.deallocate(smallestVm);
			} catch (UnknownVirtualMachineException e) {
				logger.error("UnknownVirtualMachineException thrown. VM: {}",
						smallestVm);
			} catch (UnknownServerException e) {
				logger.error(
						"UnknownServerException thrown while trying to deallocate VM ({}). Server: {}",
						smallestVm, smallestVm.getCurrentServer());
			}

			try {
				allocate(smallestVm, servers);
			} catch (UnknownVirtualMachineException e) {
				logger.error("UnknownVirtualMachineException thrown. VM: {}",
						smallestVm);
			} catch (UnknownServerException e) {
				logger.error("UnknownServerException thrown. {}",
						e.getMessage());
			}
		}
	}

	public void allocate(VirtualMachine vm, List<Server> servers)
			throws UnknownVirtualMachineException, UnknownServerException {
		// sort servers by residual capacity
		Collections.sort(servers,
				new ServerOrderedByResidualCapacityComparator());

		// allocate in the first server with available capacity
		Server destinationServer = null;
		for (Server server : servers) {
			if (server.canHost(vm, true)) {
				destinationServer = server;
				break;
			}
		}

		if (destinationServer == null) {
			try {
				destinationServer = virtualizationManager
						.getNextInactiveServer(vm, new KhannaTypeChooser());

				if (destinationServer != null) {
					servers.add(destinationServer);
				}
			} catch (NoMoreServersAvailableException e) {
			}
		}

		if (destinationServer == null) {
			destinationServer = PlacementUtils.lessLossEmptyServer(servers, vm);
		}

		if (destinationServer == null) {
			logger.info("No server could allocate the virtual machine: {}.",
					vm.toString());
			vms_not_allocated++;
		}

		if (destinationServer != null) {
			virtualizationManager.setVmToServer(vm, destinationServer);
		}
	}

	@Override
	public void setParameters(Map<String, Object> parameters)
			throws InvalidParameterException {
		Object o = parameters.get(Constants.PARAMETER_VIRTUALIZATION_MANAGER);
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

		o = parameters.get(Constants.PARAMETER_RESOURCE_LOW_UTILIZATION);
		if (o instanceof Double) {
			setLowUtilization(((Double) o).doubleValue());
		} else {
			setLowUtilization(0);
			logger.debug("Parameter {} is not set. Using default {}",
					Constants.PARAMETER_RESOURCE_LOW_UTILIZATION, 0);
		}
	}
}
