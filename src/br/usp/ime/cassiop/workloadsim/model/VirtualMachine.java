package br.usp.ime.cassiop.workloadsim.model;

public class VirtualMachine extends Machine {

	private Server lastServer = null;
	private Server currentServer = null;
	private long endTime = 0;

	public Server getCurrentServer() {
		return currentServer;
	}

	public void setLastServer(Server lastServer) {
		this.lastServer = lastServer;
	}

	public void setCurrentServer(Server currentServer) {
		if (this.currentServer != null) {
			this.lastServer = this.currentServer;
		}
		this.currentServer = currentServer;
	}

	public Server getLastServer() {
		return lastServer;
	}

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

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public double getResourceUtilization() {
		return Math.sqrt(Math.pow(resourceCpu, 2) + Math.pow(resourceMem, 2));
	}
}
