package br.usp.ime.cassiop.workloadsim.migrationcontrol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;
import br.usp.ime.cassiop.workloadsim.util.MathUtils;

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

		int virtualMachinesToReallocate = 0;

		List<Server> almostOverloadedServers = new LinkedList<Server>();

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
			// checks if the server is almost overloaded
			if (server.isAlmostOverloaded()) {
				almostOverloadedServers.add(server);
			}
		}

		List<VirtualMachine> vms = null;
		for (Server overloadedServer : almostOverloadedServers) {
			vms = new ArrayList<VirtualMachine>(
					overloadedServer.getVirtualMachines());

			// orders virtual machines ascendently by resource utilization
			Collections.sort(vms, new Comparator<VirtualMachine>() {

				@Override
				public int compare(VirtualMachine vm0, VirtualMachine vm1) {
					if (vm0.getResourceUtilization() < vm1
							.getResourceUtilization()) {
						return -1;
					} else if (MathUtils.equals(vm0.getResourceUtilization(),
							vm1.getResourceUtilization())) {
						return 0;
					} else {
						return 1;
					}
				}
			});

			for (VirtualMachine smallestVm : vms) {
				if (!overloadedServer.isAlmostOverloaded()) {
					// when the server isn't overloaded anymore, the VMs left
					// shouln't be reallocated.
					for (VirtualMachine vm : overloadedServer
							.getVirtualMachines()) {
						isInDemand.remove(vm.getName());
					}
				}
				try {

					virtualizationManager.deallocate(smallestVm);
					virtualMachinesToReallocate++;

				} catch (UnknownVirtualMachineException e) {
					logger.error(
							"UnknownVirtualMachineException thrown while trying to deallocate VMs. VM: {}",
							smallestVm);
				} catch (UnknownServerException e) {
					logger.error(
							"UnknownServerException thrown while trying to deallocate VMs. VM: {} ; Server: {}",
							smallestVm, smallestVm.getCurrentServer());
				}
			}
		}

		statisticsModule.setStatisticValue(
				Constants.STATISTIC_VIRTUAL_MACHINES_TO_REALLOCATE,
				virtualMachinesToReallocate);

		return new ArrayList<VirtualMachine>(isInDemand.values());
	}
}
