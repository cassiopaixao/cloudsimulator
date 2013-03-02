package br.usp.ime.cassiop.workloadsim.placement;

import java.util.List;

import br.usp.ime.cassiop.workloadsim.PlacementModule;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;

public interface PlacementWithPowerOffStrategy extends PlacementModule {

	void allocate(VirtualMachine vm, List<Server> servers) throws Exception;
}
