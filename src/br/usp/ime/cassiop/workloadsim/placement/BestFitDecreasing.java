package br.usp.ime.cassiop.workloadsim.placement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.PlacementModule;
import br.usp.ime.cassiop.workloadsim.exceptions.DependencyNotSetException;
import br.usp.ime.cassiop.workloadsim.exceptions.NoMoreServersAvailableException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownServerException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownVirtualMachineException;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class BestFitDecreasing extends PlacementModule {

	private List<Server> servers = null;

	final Logger logger = LoggerFactory.getLogger(BestFitDecreasing.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.ime.usp.cassiop.workloadsim.PlacementModule#consolidateAll()
	 */
	public void consolidateAll(List<VirtualMachine> demand)
			throws DependencyNotSetException {
		if (virtualizationManager == null) {
			throw new DependencyNotSetException(
					"VirtualizationManager is not set.");
		}
		if (statisticsModule == null) {
			throw new DependencyNotSetException(
					"StatisticsModule is not set.");
		}
		if (demand == null) {
			throw new DependencyNotSetException("Demand is not set.");
		}

		// demand.sort desc
		Collections.sort(demand);
		Collections.reverse(demand);

		servers = new ArrayList<Server>(
				virtualizationManager.getActiveServersList());

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

	}

	public void allocate(VirtualMachine vm, List<Server> servers)
			throws UnknownVirtualMachineException, UnknownServerException {
		Server destinationServer = null;
		double leavingResource = Double.MAX_VALUE;

		for (Server server : servers) {
			if (server.canHost(vm)) {
				// stores the best-fit allocation
				if (PlacementUtils.leavingResource(server, vm) < leavingResource) {
					destinationServer = server;
					leavingResource = PlacementUtils
							.leavingResource(server, vm);
				}

			}
		}

		if (destinationServer == null) {
			try {
				destinationServer = virtualizationManager
						.getNextInactiveServer(vm, new BestFitTypeChooser());

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
			statisticsModule.addToStatisticValue(
					Constants.STATISTIC_VIRTUAL_MACHINES_NOT_ALLOCATED,
					1);
		}

		if (destinationServer != null) {
			virtualizationManager.setVmToServer(vm, destinationServer);
		}
	}
}
