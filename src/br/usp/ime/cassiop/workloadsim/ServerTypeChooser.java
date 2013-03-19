package br.usp.ime.cassiop.workloadsim;

import java.util.List;

import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;

public interface ServerTypeChooser {
	public Server chooseServerType(List<Server> availableMachineTypes,
			VirtualMachine vmDemand);

}
