package br.usp.ime.cassiop.workloadsim;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.exceptions.DependencyNotSetException;
import br.usp.ime.cassiop.workloadsim.exceptions.InvalidParameterException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownServerException;
import br.usp.ime.cassiop.workloadsim.exceptions.UnknownVirtualMachineException;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.placement.FirstFitDecreasing;
import br.usp.ime.cassiop.workloadsim.placement.PowerOffStrategy;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public abstract class PlacementModule implements Parametrizable {

	protected VirtualizationManager virtualizationManager = null;

	protected StatisticsModule statisticsModule = null;

	protected PowerOffStrategy powerOffStrategy = null;

	final Logger logger = LoggerFactory.getLogger(FirstFitDecreasing.class);

	public void setVirtualizationManager(
			VirtualizationManager virtualizationManager) {
		this.virtualizationManager = virtualizationManager;
	}

	public void setStatisticsModule(StatisticsModule statisticsModule) {
		this.statisticsModule = statisticsModule;
	}

	public void setPowerOffStrategy(PowerOffStrategy powerOffStrategy) {
		this.powerOffStrategy = powerOffStrategy;
	}

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

		o = parameters.get(Constants.PARAMETER_POWER_OFF_STRATEGY);
		if (o instanceof PowerOffStrategy) {
			setPowerOffStrategy((PowerOffStrategy) o);
		} else {
			throw new InvalidParameterException(
					Constants.PARAMETER_POWER_OFF_STRATEGY,
					PowerOffStrategy.class);
		}
	}

	public abstract void consolidateAll(List<VirtualMachine> demand)
			throws DependencyNotSetException;

	public abstract void allocate(VirtualMachine vm, List<Server> servers)
			throws UnknownVirtualMachineException, UnknownServerException;

}