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

public class FirstFitDecreasing extends PlacementModule {

	private List<Server> servers = null;

	final Logger logger = LoggerFactory.getLogger(FirstFitDecreasing.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.ime.usp.cassiop.workloadsim.PlacementModule#consolidateAll()
	 */
	public void consolidateAll(List<VirtualMachine> demand)
			throws DependencyNotSetException {
		verifyDependencies(demand);

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

	@Override
	public void allocate(VirtualMachine vm, List<Server> servers)
			throws UnknownVirtualMachineException, UnknownServerException {
		if (vm == null) {
			throw new UnknownVirtualMachineException();
		}
		Server destinationServer = null;

		for (Server server : servers) {
			if (server.canHost(vm)) {
				destinationServer = server;
				break;
			}
		}

		if (destinationServer == null) {
			try {
				destinationServer = virtualizationManager
						.getNextInactiveServer(vm, new FirstFitTypeChooser());

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
