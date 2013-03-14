package br.usp.ime.cassiop.workloadsim.model;

/**
 * 
 * @author cassio
 * 
 */
abstract class Machine implements Comparable<Machine> {

	protected String name = null;

	protected double resourceCpu;
	protected double resourceMem;

	public Machine() {
		name = "";
		resourceCpu = 0.0;
		resourceMem = 0.0;
	}

	/**
	 * Sets a resource feature to the machine. If the resource is already set,
	 * the new value will overwrite the old one.
	 * 
	 * @param type
	 *            type of the resource to set
	 * @param amount
	 *            amount of the resource feature
	 */
	protected void setResource(ResourceType type, double amount) {
		switch (type) {
		case CPU:
			resourceCpu = amount;
			break;
		case MEMORY:
			resourceMem = amount;
			break;
		default:
			break;
		}
	}

	/**
	 * Gets a resource feature of the machine.
	 * 
	 * @param type
	 *            type of the resource desired
	 * @return the resource desired, or <code>null</code> if the resource is not
	 *         set
	 */
	protected double getResource(ResourceType type) {
		switch (type) {
		case CPU:
			return resourceCpu;
		case MEMORY:
			return resourceMem;
		default:
			return 0.0;
		}
	}

	/**
	 * Gets the machine name
	 * 
	 * @return the machine name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the machine name
	 * 
	 * @param name
	 *            the new name of the machine
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Creates a string to represent the Machine. The resulting string is
	 * <code>name:resource1;resource2;...;resourceN.</code>. If there is no
	 * resource set, the resulting string will be <code>name.</code>
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(name).append(':');
		sb.append(String.format("CPU(%.2f)", resourceCpu)).append(";");
		sb.append(String.format("MEM(%.2f)", resourceMem)).append(".");

		return sb.toString();
	}

	@Override
	public int compareTo(Machine o) {

		if (resourceCpu < o.resourceCpu) {
			return -1;
		} else if (resourceCpu > o.resourceCpu) {
			return 1;
		} else {
			if (resourceMem < o.resourceMem) {
				return -1;
			} else if (resourceMem > o.resourceMem) {
				return 1;
			} else {
				return 0;
			}
		}
	}
}