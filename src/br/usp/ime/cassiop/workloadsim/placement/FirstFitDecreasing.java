package br.usp.ime.cassiop.workloadsim.placement;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import br.usp.ime.cassiop.workloadsim.PlacementModule;
import br.usp.ime.cassiop.workloadsim.VirtualizationManager;
import br.usp.ime.cassiop.workloadsim.model.PhysicalMachine;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class FirstFitDecreasing implements PlacementModule {
	private List<VirtualMachine> demand = null;

	private VirtualizationManager virtualizationManager = null;

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
		// pms.sort asc
		Collections.sort(demand);
		Collections.reverse(demand);
		// Collections.sort(pms);

		for (VirtualMachine vm : demand) {
			boolean consolidated = false;
			for (PhysicalMachine pm : pms) {
				if (pm.canHost(vm)) {
					virtualizationManager.consolidate(vm, pm);
					consolidated = true;
					break;
				}
			}
			if (!consolidated) {
				PhysicalMachine inactivePm = virtualizationManager
						.getNextInactivePm(vm);
				virtualizationManager.consolidate(vm, inactivePm);
			}
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
