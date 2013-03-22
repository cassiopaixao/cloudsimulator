package br.usp.ime.cassiop.workloadsim.poweroff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.exceptions.DependencyNotSetException;
import br.usp.ime.cassiop.workloadsim.exceptions.InvalidParameterException;
import br.usp.ime.cassiop.workloadsim.exceptions.ServerNotEmptyException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownServerException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownVirtualMachineException;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.placement.PowerOffStrategy;
import br.usp.ime.cassiop.workloadsim.util.Constants;
import br.usp.ime.cassiop.workloadsim.util.MathUtils;

public class LowUtilizationPowerOffStrategy extends PowerOffStrategy {

	private double lowUtilization = 0.0;

	@Override
	public void setParameters(Map<String, Object> parameters)
			throws InvalidParameterException {
		super.setParameters(parameters);

		Object o = null;

		o = parameters.get(Constants.PARAMETER_POWER_OFF_LOW_UTILIZATION);
		if (o instanceof Double) {
			setLowUtilization(((Double) o).doubleValue());
		} else {
			setLowUtilization(0.0);
			logger.debug("Parameter {} is not set. Using default {}",
					Constants.PARAMETER_POWER_OFF_LOW_UTILIZATION, 0.0);
		}
	}

	static final Logger logger = LoggerFactory
			.getLogger(LowUtilizationPowerOffStrategy.class);

	public void powerOff(Collection<Server> serversCollection)
			throws DependencyNotSetException {
		if (placementModule == null) {
			throw new DependencyNotSetException("PlacementModule is not set.");
		}
		if (statisticsModule == null) {
			throw new DependencyNotSetException(
					"VirtualizationManager is not set.");
		}
		if (virtualizationManager == null) {
			throw new DependencyNotSetException(
					"VirtualizationManager is not set.");
		}
		if (serversCollection == null) {
			return;
		}

		List<Server> servers = new ArrayList<Server>(serversCollection);

		int servers_turned_off = 0;

		for (Server server : servers) {
			if (MathUtils.lessThanOrEquals(server.getResourceUtilization(),
					lowUtilization)) {
				List<VirtualMachine> shouldMigrate = new LinkedList<VirtualMachine>();
				List<VirtualMachine> vmsOnServer = new ArrayList<VirtualMachine>(
						server.getVirtualMachines());

				for (VirtualMachine vm : vmsOnServer) {
					try {
						virtualizationManager.deallocate(vm);
						shouldMigrate.add(vm);
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

				for (VirtualMachine vm : shouldMigrate) {
					try {
						placementModule.allocate(vm, servers);
					} catch (UnknownVirtualMachineException e) {
						logger.error(
								"Tryed to allocate an unknown virtual machine: {}",
								vm);
					} catch (UnknownServerException e) {
						logger.error(
								"Tryed to allocate a virtual machine ({}) to an unknown server.",
								vm);
					}
				}

			}
		}

		for (Server server : servers) {
			if (server.getVirtualMachines().isEmpty()) {
				try {
					virtualizationManager.turnOffServer(server);
					servers_turned_off++;
				} catch (ServerNotEmptyException e) {
					logger.error("Tryed to turn off a nonempty server.");
				} catch (UnknownServerException e) {
					logger.error("Tryed to turn off an unknown server: {}",
							server);
				}
			}
		}

		statisticsModule.setStatisticValue(
				Constants.STATISTIC_SERVERS_TURNED_OFF, servers_turned_off);
	}

	public void setLowUtilization(double lowUtilization) {
		this.lowUtilization = lowUtilization;
	}
}
