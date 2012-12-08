package br.usp.ime.cassiop.workloadsim;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.environment.IdealCluster;
import br.usp.ime.cassiop.workloadsim.model.PhysicalMachine;
import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class VirtualizationManager implements Parametrizable {

	protected List<PhysicalMachine> pmList = null;

	protected Environment environment = null;

	// protected Map<PhysicalMachine, Integer> environmentStatus;

	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;

		// environmentStatus = new HashMap<PhysicalMachine, Integer>();
		// for (PhysicalMachine pm : environment.getMachineTypes()) {
		// environmentStatus.put(pm, new Integer(0));
		// }
	}

	final Logger logger = LoggerFactory.getLogger(VirtualizationManager.class);

	public VirtualizationManager() {
		pmList = new ArrayList<PhysicalMachine>();
		setEnvironment(new IdealCluster());
	}

	public void consolidate(VirtualMachine vm, PhysicalMachine pm)
			throws Exception {
		// TODO is there a way to optimize this test?
		// for (PhysicalMachine m : consolidation.keySet()) {
		// if (consolidation.get(m).contains(vm)) {
		// migrate(vm, m, pm);
		// return;
		// }
		// }

		setVmToPm(vm, pm);
	}

	// private void migrate(VirtualMachine vm, PhysicalMachine oldPm,
	// PhysicalMachine newPm) throws Exception {
	// logger.info("Migrating VirtualMachine {} from {} to {}", vm.getName(),
	// oldPm.getName(), newPm.getName());
	//
	// setVmToPm(vm, newPm);
	// consolidation.get(oldPm).remove(vm);
	// }

	private void setVmToPm(VirtualMachine vm, PhysicalMachine pm)
			throws Exception {
		if (pmList.contains(pm)) {

			if (!pm.canHost(vm)) {
				logger.info(
						"Physical machine {} could be overloaded. Virtual machine {}'s demands extrapolates the pm resources' capacities",
						pm.getName(), vm.getName());
			}

			pm.addVirtualMachine(vm);

			// TODO count how many machines of each type is active
			// for (PhysicalMachine pmtype : environmentStatus.keySet()) {
			// if (pm.toString().equals(pmtype.toString())) {
			// environmentStatus.put(pmtype, new Integer(environmentStatus
			// .get(pmtype).intValue() + 1));
			// }
			// }

			logger.debug(
					"VirtualMachine {} consolidated to PhysicalMachine {}",
					vm.getName(), pm.getName());
		} else {
			throw new Exception("Unknown destination physical machine.");
		}
	}

	public List<PhysicalMachine> getActivePmList() {
		return pmList;
	}

	public PhysicalMachine getNextInactivePm(VirtualMachine vmDemand)
			throws Exception {

		return getNextInactivePm(vmDemand, new FirstFitTypeChooser());

	}

	public PhysicalMachine getNextInactivePm(VirtualMachine vmDemand,
			PhysicalMachineTypeChooser pmTypeChooser) throws Exception {

		PhysicalMachine selectedMachine = pmTypeChooser.choosePMType(
				environment.getAvailableMachineTypes(), vmDemand);

		if (selectedMachine == null) {
			logger.info("No more physical machines available.");
			return null;
		}
		
		PhysicalMachine pm = environment.getMachineOfType(selectedMachine);

		pm.setName(Integer.toString(pmList.size() + 1));

		logger.debug("PhysicalMachine {} activated: {}", pm.getName(),
				pm.toString());

		pmList.add(pm);

		return pm;
	}

	// public Map<PhysicalMachine, Integer> getEnvironmentStatus() {
	// return environmentStatus;
	// }

	public void clear() {
		// for (PhysicalMachine pm : pmList) {
		// pm.clear();
		// }
		// TODO "desligar" todas as máquinas?
		pmList.clear();
		environment.clear();

		// for (PhysicalMachine pm : environmentStatus.keySet()) {
		// environmentStatus.put(pm, new Integer(0));
		// }
	}

	public class FirstFitTypeChooser implements PhysicalMachineTypeChooser {

		public PhysicalMachine choosePMType(List<PhysicalMachine> machineTypes,
				VirtualMachine vmDemand) {
			PhysicalMachine selectedMachine = null;

			for (PhysicalMachine pm : machineTypes) {
				if (pm.canHost(vmDemand)) {
					selectedMachine = pm;
					break;
				}
			}

			if (selectedMachine == null) {
				logger.info("No inactive physical machine can satisfy the virtual machine's demand. Activating the physical machine with lowest loss of performance.");

				PhysicalMachine lessLossOfPerformanceMachine = null;
				double lessLossOfPerformance = Double.MAX_VALUE;

				for (PhysicalMachine pm : machineTypes) {
					if (!pm.canHost(vmDemand)) {
						if (lossOfPerformance(pm, vmDemand) < lessLossOfPerformance) {
							lessLossOfPerformance = lossOfPerformance(pm,
									vmDemand);
							lessLossOfPerformanceMachine = pm;
						}
					}
				}

				if (lessLossOfPerformanceMachine == null) {
					logger.info("There is no inactive physical machine. Need to overload one.");
					return null;
				}

				selectedMachine = lessLossOfPerformanceMachine;
			}

			return selectedMachine;
		}

		private double lossOfPerformance(PhysicalMachine pm, VirtualMachine vm) {
			double leavingCpu, leavingMem;
			double sum = 0;
			leavingCpu = pm.getFreeResource(ResourceType.CPU)
					- vm.getDemand(ResourceType.CPU);
			leavingMem = pm.getFreeResource(ResourceType.MEMORY)
					- vm.getDemand(ResourceType.MEMORY);

			sum += (leavingCpu < 0) ? -leavingCpu : 0;
			sum += (leavingMem < 0) ? -leavingMem : 0;

			return sum;
		}

	}

	@Override
	public void setParameters(Map<String, Object> parameters) throws Exception {
		Object e = parameters.get(Constants.PARAMETER_ENVIRONMENT);
		if (e instanceof Environment) {
			setEnvironment((Environment) e);
		} else {
			throw new Exception(String.format("Invalid parameter: %s",
					Constants.PARAMETER_ENVIRONMENT));
		}
	}
}
