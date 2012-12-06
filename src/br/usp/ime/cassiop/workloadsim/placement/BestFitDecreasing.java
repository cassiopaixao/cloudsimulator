package br.usp.ime.cassiop.workloadsim.placement;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.PhysicalMachineTypeChooser;
import br.usp.ime.cassiop.workloadsim.PlacementModule;
import br.usp.ime.cassiop.workloadsim.VirtualizationManager;
import br.usp.ime.cassiop.workloadsim.model.PhysicalMachine;
import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class BestFitDecreasing implements PlacementModule {
	private List<VirtualMachine> demand = null;

	private VirtualizationManager virtualizationManager = null;

	final Logger logger = LoggerFactory.getLogger(BestFitDecreasing.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.ime.usp.cassiop.workloadsim.PlacementModule#setVirtualizationManager
	 * (br.ime.usp.cassiop.workloadsim.VirtualizationManager)
	 */
	@Override
	public void setVirtualizationManager(
			VirtualizationManager virtualizationManager) {
		this.virtualizationManager = virtualizationManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.ime.usp.cassiop.workloadsim.PlacementModule#setDemand(java.util.List)
	 */
	@Override
	public void setDemand(List<VirtualMachine> demand) {
		this.demand = demand;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.ime.usp.cassiop.workloadsim.PlacementModule#consolidateAll()
	 */
	@Override
	public void consolidateAll() throws Exception {
		if (virtualizationManager == null) {
			throw new Exception("VirtualizationManager is not set.");
		}
		if (demand == null) {
			throw new Exception("Demand is not set.");
		}

		List<PhysicalMachine> pms = virtualizationManager.getActivePmList();

		// demand.sort desc
		Collections.sort(demand);
		Collections.reverse(demand);

		PhysicalMachine bestFitPm = null;
		double bestFitLeavingResource = Double.MAX_VALUE;

		for (VirtualMachine vm : demand) {
			bestFitPm = null;
			bestFitLeavingResource = Double.MAX_VALUE;

			for (PhysicalMachine pm : pms) {
				if (pm.canHost(vm)) {
					// TODO can optimize this? the leaving resource is
					// calculated for every pm and every vm
					// maybe a "freeResourceFactor" (cpu * mem OR cpu + mem).
					// sum is better, 'cause cpu or mem could be zero.

					// stores the best-fit allocation
					if (leavingResource(pm, vm) < bestFitLeavingResource) {
						bestFitPm = pm;
						bestFitLeavingResource = leavingResource(pm, vm);
					}

				}
			}

			if (bestFitPm != null) {
				virtualizationManager.consolidate(vm, bestFitPm);
			} else {
				PhysicalMachine inactivePm = virtualizationManager
						.getNextInactivePm(vm, new BestFitTypeChooser());
				virtualizationManager.consolidate(vm, inactivePm);
			}
		}

	}

	private double leavingResource(PhysicalMachine pm, VirtualMachine vm) {
		double leavingCpu, leavingMem;
		leavingCpu = pm.getFreeResource(ResourceType.CPU)
				- vm.getDemand(ResourceType.CPU);
		leavingMem = pm.getFreeResource(ResourceType.MEMORY)
				- vm.getDemand(ResourceType.MEMORY);

		return leavingCpu + leavingMem;
	}

	public class BestFitTypeChooser implements PhysicalMachineTypeChooser {

		public PhysicalMachine choosePMType(List<PhysicalMachine> machineTypes,
				VirtualMachine vmDemand) {
			PhysicalMachine selectedMachine = null;
			double leavingResource = Double.MAX_VALUE;

			for (PhysicalMachine pm : machineTypes) {
				if (pm.canHost(vmDemand)) {
					if (leavingResource(pm, vmDemand) < leavingResource) {
						selectedMachine = pm;
						leavingResource = leavingResource(pm, vmDemand);
					}
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
		Object e = parameters.get(Constants.PARAMETER_VIRTUALIZATION_MANAGER);
		if (e instanceof VirtualizationManager) {
			setVirtualizationManager((VirtualizationManager) e);
		} else {
			throw new Exception(String.format("Invalid parameter: %s",
					Constants.PARAMETER_VIRTUALIZATION_MANAGER));
		}
	}
}
