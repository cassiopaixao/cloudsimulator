package br.usp.ime.cassiop.workloadsim.placement;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.PhysicalMachineTypeChooser;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;

public class AlmostWorstFitTypeChooser implements PhysicalMachineTypeChooser {

	final Logger logger = LoggerFactory
			.getLogger(AlmostWorstFitTypeChooser.class);

	public Server chooseServerType(List<Server> machineTypes,
			VirtualMachine vmDemand) {
		Server worstFitMachine = null;
		double worstFitLeavingResource = -1.0;

		Server almostWorstFitMachine = null;
		double almostWorstFitLeavingResource = -1.0;

		double leavingResource;

		for (Server server : machineTypes) {
			if (server.canHost(vmDemand)) {
				leavingResource = PlacementUtils.leavingResource(server,
						vmDemand);
				if (leavingResource > worstFitLeavingResource) {
					almostWorstFitMachine = worstFitMachine;
					almostWorstFitLeavingResource = worstFitLeavingResource;

					worstFitMachine = server;
					worstFitLeavingResource = leavingResource;
				} else if (leavingResource > almostWorstFitLeavingResource) {
					almostWorstFitMachine = server;
					almostWorstFitLeavingResource = leavingResource;
				}
			}
		}

		if (almostWorstFitMachine == null) {
			if (worstFitMachine != null) {
				almostWorstFitMachine = worstFitMachine;
			} else {
				logger.info("No inactive physical machine can satisfy the virtual machine's demand. Activating the physical machine with lowest loss of performance.");

				Server lessLossOfPerformanceMachine = PlacementUtils
						.lessLossOfPerformanceMachine(machineTypes, vmDemand);

				if (lessLossOfPerformanceMachine == null) {
					logger.info("There is no inactive physical machine. Need to overload one.");
					return null;
				}

				almostWorstFitMachine = lessLossOfPerformanceMachine;
			}
		}
		return almostWorstFitMachine;
	}

}
