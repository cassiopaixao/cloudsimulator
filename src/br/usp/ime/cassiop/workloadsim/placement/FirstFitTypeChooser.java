package br.usp.ime.cassiop.workloadsim.placement;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.ServerTypeChooser;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;

public class FirstFitTypeChooser implements ServerTypeChooser {

	final Logger logger = LoggerFactory.getLogger(FirstFitTypeChooser.class);

	public Server chooseServerType(List<Server> machineTypes,
			VirtualMachine vmDemand) {
		Server selectedMachine = null;

		Collections.sort(machineTypes);

		for (Server server : machineTypes) {
			if (server.canHost(vmDemand)) {
				selectedMachine = server;
				break;
			}
		}

		if (selectedMachine == null) {
			logger.info("No inactive physical machine can satisfy the virtual machine's demand. Activating the physical machine with lowest loss of performance.");

			Server lessLossOfPerformanceMachine = PlacementUtils
					.lessLossOfPerformanceMachine(machineTypes, vmDemand);

			if (lessLossOfPerformanceMachine == null) {
				logger.info("There is no inactive physical machine. Need to overload one.");
				return null;
			}

			selectedMachine = lessLossOfPerformanceMachine;
		}

		return selectedMachine;
	}

}
