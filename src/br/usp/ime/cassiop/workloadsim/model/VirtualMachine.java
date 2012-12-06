package br.usp.ime.cassiop.workloadsim.model;

public class VirtualMachine extends Machine {

	/**
	 * Sets a resource demand to the virtual machine. If the resource is already
	 * set, the new value will overwrite the old one.
	 * 
	 * @param type
	 *            the type of the resource to set
	 * @param amount
	 *            the amount of the resource demand
	 */
	public void setDemand(ResourceType type, double amount) {
		setResource(type, amount);
	}

	/**
	 * Gets a resource demand of the virtual machine.
	 * 
	 * @param type
	 *            type of the resource wanted
	 * @return the resource demand of the type wanted
	 */
	public double getDemand(ResourceType type) {
		return getResource(type);
	}
}
