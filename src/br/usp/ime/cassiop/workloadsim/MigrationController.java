package br.usp.ime.cassiop.workloadsim;

import java.util.List;
import java.util.Map;

import br.usp.ime.cassiop.workloadsim.exceptions.DependencyNotSetException;
import br.usp.ime.cassiop.workloadsim.exceptions.InvalidParameterException;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public abstract class MigrationController implements Parametrizable {

	protected VirtualizationManager virtualizationManager = null;

	protected StatisticsModule statisticsModule = null;

	public void setVirtualizationManager(
			VirtualizationManager virtualizationManager) {
		this.virtualizationManager = virtualizationManager;
	}

	public void setStatisticsModule(StatisticsModule statisticsModule) {
		this.statisticsModule = statisticsModule;
	}

	/**
	 * Sets the dependencies to use the object. At least two parameters should
	 * be set: {@link Constants#PARAMETER_VIRTUALIZATION_MANAGER} and
	 * {@link Constants#PARAMETER_STATISTICS_MODULE}.
	 */
	public void setParameters(Map<String, Object> parameters)
			throws InvalidParameterException {
		Object o = null;

		o = parameters.get(Constants.PARAMETER_VIRTUALIZATION_MANAGER);
		if (o instanceof VirtualizationManager) {
			setVirtualizationManager((VirtualizationManager) o);
		} else {
			throw new InvalidParameterException(
					Constants.PARAMETER_VIRTUALIZATION_MANAGER,
					VirtualizationManager.class);
		}

		o = parameters.get(Constants.PARAMETER_STATISTICS_MODULE);
		if (o instanceof StatisticsModule) {
			setStatisticsModule((StatisticsModule) o);
		} else {
			throw new InvalidParameterException(
					Constants.PARAMETER_STATISTICS_MODULE,
					StatisticsModule.class);
		}
	}

	protected void verifyDependencies(List<VirtualMachine> demand)
			throws DependencyNotSetException {
		if (virtualizationManager == null) {
			throw new DependencyNotSetException(
					"VirtualizationManager is not set.");
		}
		if (statisticsModule == null) {
			throw new DependencyNotSetException("StatisticsModule is not set.");
		}
		if (demand == null) {
			throw new DependencyNotSetException("Demand is not set.");
		}
	}

	/**
	 * Controls which Virtual Machines should be allocated or reallocated. If a
	 * VM should be reallocated, this method removes it from the Physical
	 * Machine that has allocated it before.
	 * 
	 * @param demand
	 *            list of VMs whose demands has changed
	 * @return a list with all VMs that should be [re]allocated
	 * @throws DependencyNotSetException
	 *             if at least one dependency wasn't set before the method call.
	 */
	public abstract List<VirtualMachine> control(List<VirtualMachine> demand)
			throws DependencyNotSetException;

}
