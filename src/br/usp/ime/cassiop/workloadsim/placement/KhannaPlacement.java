package br.usp.ime.cassiop.workloadsim.placement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.PlacementModule;
import br.usp.ime.cassiop.workloadsim.exceptions.DependencyNotSetException;
import br.usp.ime.cassiop.workloadsim.exceptions.NoMoreServersAvailableException;
import br.usp.ime.cassiop.workloadsim.exceptions.ServerOverloadedException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownServerException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownVirtualMachineException;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.placement.KhannaTypeChooser.ServerOrderedByResidualCapacityComparator;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class KhannaPlacement extends PlacementModule {

	final Logger logger = LoggerFactory.getLogger(KhannaPlacement.class);

	public void consolidateAll(List<VirtualMachine> demand)
			throws DependencyNotSetException {
		verifyDependencies(demand);

		List<Server> servers = new ArrayList<Server>(
				virtualizationManager.getActiveServersList());

		for (VirtualMachine vm : demand) {
			try {
				if (vm.getCurrentServer() == null) {
					allocate(vm, servers);
				}
			} catch (UnknownVirtualMachineException e) {
				logger.error("UnknownVirtualMachineException thrown. VM: {}",
						vm);
			} catch (UnknownServerException e) {
				logger.error("UnknownServerException thrown. {}",
						e.getMessage());
			}
		}
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
			statisticsModule.addToStatisticValue(
					Constants.STATISTIC_VIRTUAL_MACHINES_NOT_ALLOCATED, 1);

		}

		if (destinationServer != null) {
			virtualizationManager.setVmToServer(vm, destinationServer);
		}
	}
}
