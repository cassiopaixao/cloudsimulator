package br.usp.ime.cassiop.workloadsim;

import java.util.Map;

import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;

public class Measurement {

	private Map<String, VirtualMachine> actualDemand = null;

	private Long time = null;

	public Map<String, VirtualMachine> getActualDemand() {
		return actualDemand;
	}

	public void setActualDemand(Map<String, VirtualMachine> actualDemand) {
		this.actualDemand = actualDemand;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public double getVmActualDemand(VirtualMachine vm, ResourceType type) {
		VirtualMachine actualVm = actualDemand.get(vm.getName());
		if (actualVm == null) {
			System.out.println(vm.toString());
		}
		return actualVm.getDemand(type);
	}

	public Long getTime() {
		return time;
	}

}
