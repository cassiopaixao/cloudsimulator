package br.usp.ime.cassiop.workloadsim.model;

import java.util.ArrayList;
import java.util.List;

import br.usp.ime.cassiop.workloadsim.exceptions.ServerOverloadedException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownVirtualMachineException;

/**
 * Represents a server machine with its resource capacities.
 * 
 * @author cassio
 * 
 */
public class Server extends Machine {

	private List<VirtualMachine> virtualMachines = null;

	private double freeResourceCpu;
	private double freeResourceMem;

	private double resourceUtilization;
	private double residualCapacity;

	private double kneePerformanceLossCpu = 1;
	private double kneePerformanceLossMem = 1;

	private double kneePerformanceLossCpuNrml = 1;
	private double kneePerformanceLossMemNrml = 1;

	public double getResourceUtilization() {
		return resourceUtilization;
	}

	public double getResidualCapacity() {
		return residualCapacity;
	}

	public double getLoadPercentage() {
		return ((resourceCpu - freeResourceCpu) * (resourceMem - freeResourceMem))
				/ (resourceCpu * resourceMem);
	}

	public Server clone() {
		Server s = new Server();

		s.resourceCpu = this.resourceCpu;
		s.resourceMem = this.resourceMem;

		s.kneePerformanceLossCpu = this.kneePerformanceLossCpu;
		s.kneePerformanceLossMem = this.kneePerformanceLossMem;

		s.updateFreeResources();
		
		return s;
	}

	public Server() {
		super();
		virtualMachines = new ArrayList<VirtualMachine>();
		updateFreeResources();
	}

	public List<VirtualMachine> getVirtualMachines() {
		return virtualMachines;
	}

	public void addVirtualMachine(VirtualMachine newVm)
			throws UnknownVirtualMachineException {
		if (newVm == null) {
			throw new UnknownVirtualMachineException(
					"It is not possible to assign a null VM to a physical machine.");
		}
		if (newVm.getCurrentServer() == this) {
			return;
		}

		virtualMachines.add(newVm);
		newVm.setCurrentServer(this);

		freeResourceCpu -= newVm.getResource(ResourceType.CPU);
		freeResourceMem -= newVm.getResource(ResourceType.MEMORY);

		updateResourceUtilization();
	}

	public void clear() {
		for (VirtualMachine vm : virtualMachines) {
			vm.setCurrentServer(null);
		}
		virtualMachines.clear();

		freeResourceCpu = resourceCpu;
		freeResourceMem = resourceMem;

		updateResourceUtilization();
	}

	public void removeVirtualMachine(VirtualMachine vm)
			throws UnknownVirtualMachineException {
		if (!virtualMachines.remove(vm)) {
			throw new UnknownVirtualMachineException(
					"Could not remove VM from server: VM was not allocated to this server.");
		}

		vm.setCurrentServer(null);

		freeResourceCpu += vm.getResource(ResourceType.CPU);
		freeResourceMem += vm.getResource(ResourceType.MEMORY);

		updateResourceUtilization();
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
		switch (type) {
		case CPU:
			kneePerformanceLossCpuNrml = amount * kneePerformanceLossCpu;
			break;
		case MEMORY:
			kneePerformanceLossMemNrml = amount * kneePerformanceLossMem;
			break;
		}

		setResource(type, amount);
		updateFreeResources();
	}

	public void setKneePerformanceLoss(ResourceType type, double amount) {
		switch (type) {
		case CPU:
			kneePerformanceLossCpu = amount;
			kneePerformanceLossCpuNrml = amount * resourceCpu;
			break;
		case MEMORY:
			kneePerformanceLossMem = amount;
			kneePerformanceLossMemNrml = amount * resourceMem;
			break;
		}
	}

	private void updateFreeResources() {
		freeResourceCpu = resourceCpu;
		freeResourceMem = resourceMem;
		for (VirtualMachine vm : virtualMachines) {
			freeResourceCpu -= vm.getResource(ResourceType.CPU);
			freeResourceMem -= vm.getResource(ResourceType.MEMORY);
		}

		updateResourceUtilization();
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
	 *            virtual machine you'd like to allocate to the server
	 * @return
	 */
	public boolean canHost(VirtualMachine vmDemand) {
		return canHost(vmDemand, false);
	}

	/**
	 * Verifies if the physical machine can host the vmDemand, i.e., if the
	 * resources' demands are lower or equal to resources' capacity.
	 * 
	 * @param vmDemand
	 *            virtual machine you'd like to allocate to the server
	 * @param avoidBeingAlmostOverloaded
	 *            if <code>true</code>, verifies if the server can host the
	 *            vmDemand and doesn't become almost overloaded
	 * @return
	 */
	public boolean canHost(VirtualMachine vmDemand,
			boolean avoidBeingAlmostOverloaded) {
		if (vmDemand == null) {
			return false;
		}
		if (avoidBeingAlmostOverloaded) {
			if (freeResourceCpu + kneePerformanceLossCpuNrml - resourceCpu >= vmDemand.resourceCpu
					&& freeResourceMem + kneePerformanceLossMemNrml
							- resourceMem >= vmDemand.resourceMem) {
				return true;
			}
		} else {
			if (freeResourceCpu >= vmDemand.resourceCpu
					&& freeResourceMem >= vmDemand.resourceMem) {
				return true;
			}
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

	private void updateVm(VirtualMachine vm, VirtualMachine vmInDemand)
			throws ServerOverloadedException {
		freeResourceCpu += vm.getDemand(ResourceType.CPU)
				- vmInDemand.getDemand(ResourceType.CPU);
		freeResourceMem += vm.getDemand(ResourceType.MEMORY)
				- vmInDemand.getDemand(ResourceType.MEMORY);

		updateResourceUtilization();

		vm.setDemand(ResourceType.CPU, vmInDemand.getResource(ResourceType.CPU));
		vm.setDemand(ResourceType.MEMORY,
				vmInDemand.getResource(ResourceType.MEMORY));
		vm.setEndTime(vmInDemand.getEndTime());

		if (freeResourceCpu < 0 || freeResourceMem < 0) {
			throw new ServerOverloadedException();
		}
	}

	public void updateVm(VirtualMachine vmInDemand)
			throws ServerOverloadedException, UnknownVirtualMachineException {
		if (vmInDemand == null) {
			throw new UnknownVirtualMachineException();
		}
		for (VirtualMachine vm : virtualMachines) {
			if (vm.getName().equals(vmInDemand.getName())) {
				updateVm(vm, vmInDemand);
				return;
			}
		}
		throw new UnknownVirtualMachineException();

	}

	private void updateResourceUtilization() {
		resourceUtilization = Math.sqrt(Math.pow(resourceCpu - freeResourceCpu,
				2) + Math.pow(resourceMem - freeResourceMem, 2));

		if (freeResourceCpu < 0 || freeResourceMem < 0) {
			double nonNegativeFreeResourceCpu = freeResourceCpu < 0 ? 0
					: freeResourceCpu;
			double nonNegativeFreeResourceMem = freeResourceMem < 0 ? 0
					: freeResourceMem;

			residualCapacity = Math.sqrt(Math
					.pow(nonNegativeFreeResourceCpu, 2)
					+ Math.pow(nonNegativeFreeResourceMem, 2));
		} else {
			residualCapacity = Math.sqrt(Math.pow(freeResourceCpu, 2)
					+ Math.pow(freeResourceMem, 2));
		}

	}

	public boolean isOverloaded() {
		return (freeResourceCpu < 0 || freeResourceMem < 0);
	}

	public boolean isAlmostOverloaded() {
		return (freeResourceCpu + kneePerformanceLossCpuNrml < resourceCpu || freeResourceMem
				+ kneePerformanceLossMemNrml < resourceMem);
	}
}
