package br.usp.ime.cassiop.workloadsim.migrationcontrol;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.MigrationController;
import br.usp.ime.cassiop.workloadsim.exceptions.DependencyNotSetException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownServerException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownVirtualMachineException;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class NoMigrationControl extends MigrationController {

	final Logger logger = LoggerFactory.getLogger(NoMigrationControl.class);

	@Override
	public List<VirtualMachine> control(List<VirtualMachine> demand)
			throws DependencyNotSetException {
		verifyDependencies(demand);

		Map<String, VirtualMachine> isInDemand = new HashMap<String, VirtualMachine>(
				demand.size());
		for (VirtualMachine vm : demand) {
			isInDemand.put(vm.getName(), vm);
		}

		List<VirtualMachine> shouldDeallocate = new LinkedList<VirtualMachine>();

		int newVirtualMachines = demand.size();

		// for each vm already allocated
		for (Server server : virtualizationManager.getActiveServersList()) {
			for (VirtualMachine vm : server.getVirtualMachines()) {
				shouldDeallocate.add(vm);
				// if it is in the updated demand
				if (isInDemand.containsKey(vm.getName())) {
					newVirtualMachines--;
					// set info about last server
					isInDemand.get(vm.getName()).setLastServer(server);

				} else {
					// put in demand to reallocate
					demand.add(vm);
				}
			}
		}

		for (VirtualMachine vm : shouldDeallocate) {
			try {
				virtualizationManager.deallocate(vm);
			} catch (UnknownVirtualMachineException e) {
				logger.error(
						"UnknownVirtualMachineException thrown while trying to deallocate VMs. VM: {}",
						vm);
			} catch (UnknownServerException e) {
				logger.error(
						"UnknownServerException thrown while trying to deallocate VMs. VM: {} ; Server: {}",
						vm, vm.getCurrentServer());
			}
		}

		statisticsModule.setStatisticValue(
				Constants.STATISTIC_NEW_VIRTUAL_MACHINES, newVirtualMachines);
		statisticsModule.setStatisticValue(
				Constants.STATISTIC_VIRTUAL_MACHINES_TO_REALLOCATE,
				demand.size() - newVirtualMachines);

		virtualizationManager.clear();

		return demand;
	}
}
