package br.usp.ime.cassiop.workloadsim.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a physical machine (PM) with its resource capacities.
 * 
 * @author cassio
 * 
 */
public class PhysicalMachine extends Machine {

	private List<VirtualMachine> virtualMachines = null;

	private double freeResourceCpu;
	private double freeResourceMem;

	public PhysicalMachine() {
		virtualMachines = new ArrayList<VirtualMachine>();
		updateFreeResources();
	}

	public List<VirtualMachine> getVirtualMachines() {
		return virtualMachines;
	}

	public void addVirtualMachine(VirtualMachine newVm) throws Exception {
		if (newVm == null) {
			throw new Exception(
					"It is not possible to assign a null VM to a physical machine.");
		}
		virtualMachines.add(newVm);

		freeResourceCpu -= newVm.getResource(ResourceType.CPU);
		freeResourceMem -= newVm.getResource(ResourceType.MEMORY);
	}

	public void clear() {
		virtualMachines.clear();

		freeResourceCpu = resourceCpu;
		freeResourceMem = resourceMem;
	}

	/**
	 * Sets a resource capacity to the physical machine. If the resource is
	 * already set, the new value will overwrite the old one.
	 * 
	 * @param type
	 *            the type of the resource to set
	 * @param amount
	 *            the amount of the resource capacity
	 */
	public void setCapacity(ResourceType type, double amount) {
		setResource(type, amount);
		updateFreeResources();
	}

	private void updateFreeResources() {
		freeResourceCpu = resourceCpu;
		freeResourceMem = resourceMem;
		for (VirtualMachine vm : virtualMachines) {
			freeResourceCpu -= vm.getResource(ResourceType.CPU);
			freeResourceCpu -= vm.getResource(ResourceType.MEMORY);
		}
	}

	/**
	 * Gets a resource capacity of the physical machine.
	 * 
	 * @param type
	 *            type of the resource wanted
	 * @return the resource capacity of the type wanted
	 */
	public double getCapacity(ResourceType type) {
		return getResource(type);
	}

	/**
	 * Verifies if the physical machine can host the vmDemand, i.e., if the
	 * resources' demands are lower or equal to resources' capacity.
	 * 
	 * @param vmDemand
	 * @return
	 */
	public boolean canHost(VirtualMachine vmDemand) {
		if (freeResourceCpu > vmDemand.resourceCpu
				&& freeResourceMem > vmDemand.resourceMem) {
			return true;
		}
		return false;
	}

	public double getFreeResource(ResourceType type) {
		switch (type) {
		case CPU:
			return freeResourceCpu;
		case MEMORY:
			return freeResourceMem;
		}
		return 0;
	}

	public String getType() {
		return this.toString().replace(name + ":", "");
	}
}
