package br.usp.ime.cassiop.workloadsim.placement;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.ServerTypeChooser;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.MathUtils;

public class KhannaTypeChooser implements ServerTypeChooser {

	final Logger logger = LoggerFactory.getLogger(KhannaTypeChooser.class);

	public Server chooseServerType(List<Server> machineTypes,
			VirtualMachine vmDemand) {

		Collections.sort(machineTypes,
				new ServerOrderedByResidualCapacityComparator());

		Server selectedMachine = null;

		for (Server server : machineTypes) {
			if (server.canHost(vmDemand, true)) {
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

	public static class ServerOrderedByResidualCapacityComparator implements
			Comparator<Server> {
		@Override
		public int compare(Server o1, Server o2) {
			if (MathUtils.lessThan(o1.getResidualCapacity(),
					o2.getResidualCapacity())) {
				return -1;
			} else if (MathUtils.equals(o1.getResidualCapacity(),
					o2.getResidualCapacity())) {
				return 0;
			} else {
				return 1;
			}
		}

	}
}