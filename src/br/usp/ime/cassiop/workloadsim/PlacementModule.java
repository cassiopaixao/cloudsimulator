package br.usp.ime.cassiop.workloadsim;

import java.util.List;

import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;

public interface PlacementModule extends Parametrizable {

	public void setVirtualizationManager(
			VirtualizationManager virtualizationManager);

	public void consolidateAll(List<VirtualMachine> demand) throws Exception;

}