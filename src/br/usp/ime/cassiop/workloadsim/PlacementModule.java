package br.usp.ime.cassiop.workloadsim;

import java.util.List;

import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;

public interface PlacementModule extends Parametrizable {

	public void setVirtualizationManager(
			VirtualizationManager virtualizationManager);

	public void setDemand(List<VirtualMachine> demand);

	public void consolidateAll() throws Exception;

}