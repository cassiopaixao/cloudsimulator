package br.usp.ime.cassiop.workloadsim.placement;

import java.util.ArrayList;
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

public class AlmostWorstFit extends PlacementModule {

	private List<Server> servers = null;

	final Logger logger = LoggerFactory.getLogger(AlmostWorstFit.class);

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
		if (demand == null) {
			throw new DependencyNotSetException("Demand is not set.");
		}

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

		Server worstFitServer = null;
		double worstFitLeavingResource = -1.0;

		Server almostWorstFitServer = null;
		double almostWorstFitLeavingResource = -1.0;

		double leavingResource;

		for (Server server : virtualizationManager.getActiveServersList()) {
			if (server.canHost(vm)) {
				// stores the almost worst-fit allocation
				leavingResource = PlacementUtils.leavingResource(server, vm);

				if (leavingResource > worstFitLeavingResource) {
					almostWorstFitServer = worstFitServer;
					almostWorstFitLeavingResource = worstFitLeavingResource;

					worstFitServer = server;
					worstFitLeavingResource = leavingResource;
				} else if (leavingResource > almostWorstFitLeavingResource) {
					almostWorstFitServer = server;
					almostWorstFitLeavingResource = leavingResource;
				}

			}
		}

		destinationServer = (almostWorstFitServer != null) ? almostWorstFitServer
				: worstFitServer;

		if (destinationServer == null) {
			try {
				destinationServer = virtualizationManager
						.getNextInactiveServer(vm,
								new AlmostWorstFitTypeChooser());

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
					Constants.STATISTIC_VIRTUAL_MACHINES_NOT_ALLOCATED, 1);

		}

		if (destinationServer != null) {
			virtualizationManager.setVmToServer(vm, destinationServer);
		}

	}
}
