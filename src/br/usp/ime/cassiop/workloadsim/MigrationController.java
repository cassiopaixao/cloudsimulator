package br.usp.ime.cassiop.workloadsim;

import java.util.List;

import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;

public interface MigrationController extends Parametrizable {
	/**
	 * Controls which Virtual Machines should be allocated or reallocated. If a
	 * VM should be reallocated, this method removes it from the Physical
	 * Machine that allocated it before.
	 * 
	 * @param demand
	 *            list of VMs whose demands has changed
	 * @return a list with all VMs that should be [re]allocated.
	 * @throws Exception
	 */
	public List<VirtualMachine> control(List<VirtualMachine> demand);

	public void setVirtualizationManager(
			VirtualizationManager virtualizationManager);

}
