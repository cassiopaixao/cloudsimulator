package br.usp.ime.cassiop.workloadsim.migrationcontrol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.MigrationController;
import br.usp.ime.cassiop.workloadsim.exceptions.DependencyNotSetException;
import br.usp.ime.cassiop.workloadsim.exceptions.ServerOverloadedException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownServerException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownVirtualMachineException;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;
import br.usp.ime.cassiop.workloadsim.util.VirtualizationUtils.OrderByResourceUtilization;

public class KhannaMigrationControl extends MigrationController {

	final Logger logger = LoggerFactory.getLogger(KhannaMigrationControl.class);

	@Override
	public List<VirtualMachine> control(List<VirtualMachine> demand)
			throws DependencyNotSetException {
		verifyDependencies(demand);

		Map<String, VirtualMachine> isInDemand = new HashMap<String, VirtualMachine>(
				demand.size());
		for (VirtualMachine vm : demand) {
			isInDemand.put(vm.getName(), vm);
		}

		Collection<Server> activeServers = virtualizationManager
				.getActiveServersList();

		// for each vm already allocated
		for (Server server : activeServers) {
			for (VirtualMachine vm : server.getVirtualMachines()) {
				// updates demands
				try {
					server.updateVm(isInDemand.get(vm.getName()));
				} catch (ServerOverloadedException e) {
					// will be correctly handled
				} catch (UnknownVirtualMachineException e) {
					logger.error(
							"UnknownVirtualMachineException thrown while trying to update VM: {}",
							vm);
				}
			}
		}

		migrateFromAlmostOverloadedServers(activeServers, isInDemand);

		return new ArrayList<VirtualMachine>(isInDemand.values());
	}

	private void migrateFromAlmostOverloadedServers(
			Collection<Server> activeServers,
			Map<String, VirtualMachine> isInDemand) {

		for (Server server : activeServers) {

			if (server.isAlmostOverloaded()) {
				List<VirtualMachine> vms = new ArrayList<VirtualMachine>(
						server.getVirtualMachines());

				// orders virtual machines ascendently by resource utilization
				Collections.sort(vms, new OrderByResourceUtilization());

				for (VirtualMachine smallestVm : vms) {
					if (server.isAlmostOverloaded()) {
						try {
							isInDemand.get(smallestVm.getName()).setLastServer(
									smallestVm.getCurrentServer());

							virtualizationManager.deallocate(smallestVm);

							statisticsModule
									.addToStatisticValue(
											Constants.STATISTIC_VIRTUAL_MACHINES_TO_REALLOCATE,
											1);
						} catch (UnknownVirtualMachineException e) {
							logger.error(
									"UnknownVirtualMachineException thrown while trying to deallocate VMs. VM: {}",
									smallestVm);
						} catch (UnknownServerException e) {
							logger.error(
									"UnknownServerException thrown while trying to deallocate VMs. VM: {} ; Server: {}",
									smallestVm, smallestVm.getCurrentServer());
						}
					} else {
						break;
					}
				}
			}

			// when the server isn't almost overloaded anymore, the VMs left
			// shouln't be reallocated.
			for (VirtualMachine vm : server.getVirtualMachines()) {
				isInDemand.remove(vm.getName());
			}

		}
	}
}
