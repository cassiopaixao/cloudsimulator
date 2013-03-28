package br.usp.ime.cassiop.workloadsim.migrationcontrol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.MigrationController;
import br.usp.ime.cassiop.workloadsim.exceptions.DependencyNotSetException;
import br.usp.ime.cassiop.workloadsim.exceptions.ServerOverloadedException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownServerException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownVirtualMachineException;
import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;
import br.usp.ime.cassiop.workloadsim.util.MathUtils;

public class MigrateIfChange extends MigrationController {

	final Logger logger = LoggerFactory.getLogger(MigrateIfChange.class);

	@Override
	public List<VirtualMachine> control(List<VirtualMachine> demand)
			throws DependencyNotSetException {
		verifyDependencies(demand);

		Map<String, VirtualMachine> isInDemand = new HashMap<String, VirtualMachine>(
				demand.size());
		for (VirtualMachine vm : demand) {
			isInDemand.put(vm.getName(), vm);
		}

		int newVirtualMachines = demand.size();
		VirtualMachine vmInDemand = null;
		VirtualMachine activeVm = null;

		List<String> shouldNotReallocate = new LinkedList<String>();

		Map<String, VirtualMachine> activeVmsMap = virtualizationManager
				.getActiveVirtualMachines();

		// for each vm already allocated
		List<String> activeVms = new ArrayList<String>(activeVmsMap.keySet());
		for (String activeVmName : activeVms) {
			if (isInDemand.containsKey(activeVmName)) {
				newVirtualMachines--;
				vmInDemand = isInDemand.get(activeVmName);
				activeVm = activeVmsMap.get(activeVmName);

				// check if the demand changed
				if (demandChanged(activeVm, vmInDemand)) {
					// reallocate
					vmInDemand.setLastServer(activeVm.getCurrentServer());
					try {
						virtualizationManager.deallocate(activeVm);
					} catch (UnknownVirtualMachineException e) {
						logger.error(
								"UnknownVirtualMachineException thrown while trying to deallocate VMs. VM: {}",
								activeVm);
					} catch (UnknownServerException e) {
						logger.error(
								"UnknownServerException thrown while trying to deallocate VMs. VM: {} ; Server: {}",
								activeVm, activeVm.getCurrentServer());
					}
				} else {
					try {
						// update info
						activeVm.getCurrentServer().updateVm(vmInDemand);
						// do not reallocate (only if server doesn't become
						// overloaded)
						shouldNotReallocate.add(vmInDemand.getName());

					} catch (ServerOverloadedException ex) {
						// reallocate the VM
						vmInDemand.setLastServer(activeVm.getCurrentServer());
						try {
							virtualizationManager.deallocate(activeVm);
						} catch (UnknownVirtualMachineException e) {
							logger.error(
									"UnknownVirtualMachineException thrown while trying to deallocate VMs. VM: {}",
									activeVm);
						} catch (UnknownServerException e) {
							logger.error(
									"UnknownServerException thrown while trying to deallocate VMs. VM: {} ; Server: {}",
									activeVm, activeVm.getCurrentServer());
						}
					} catch (UnknownVirtualMachineException e) {
						logger.error(
								"UnknownVirtualMachineException thrown while trying to update VM: {}",
								activeVm);
					}
				}
			}
		}

		for (String vmName : shouldNotReallocate) {
			isInDemand.remove(vmName);
		}

		statisticsModule.setStatisticValue(
				Constants.STATISTIC_NEW_VIRTUAL_MACHINES, newVirtualMachines);
		statisticsModule.setStatisticValue(
				Constants.STATISTIC_VIRTUAL_MACHINES_TO_REALLOCATE,
				isInDemand.size() - newVirtualMachines);

		return new ArrayList<VirtualMachine>(isInDemand.values());
	}

	private boolean demandChanged(VirtualMachine vm, VirtualMachine newVm) {
		if (!MathUtils.equals(vm.getDemand(ResourceType.CPU),
				newVm.getDemand(ResourceType.CPU))) {
			return true;
		}
		if (!MathUtils.equals(vm.getDemand(ResourceType.MEMORY),
				newVm.getDemand(ResourceType.MEMORY))) {
			return true;
		}
		return false;
	}
}
