package br.usp.ime.cassiop.workloadsim;

import java.util.Collection;
import java.util.Map;

import br.usp.ime.cassiop.workloadsim.exceptions.DependencyNotSetException;
import br.usp.ime.cassiop.workloadsim.exceptions.InvalidParameterException;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public abstract class PowerOffStrategy implements Parametrizable {

	protected VirtualizationManager virtualizationManager = null;
	protected StatisticsModule statisticsModule = null;
	protected PlacementModule placementModule = null;

	public void setVirtualizationManager(
			VirtualizationManager virtualizationManager) {
		this.virtualizationManager = virtualizationManager;
	}

	public void setStatisticsModule(StatisticsModule statisticsModule) {
		this.statisticsModule = statisticsModule;
	}

	public void setPlacementModule(PlacementModule placementModule) {
		this.placementModule = placementModule;
	}

	@Override
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

		o = parameters.get(Constants.PARAMETER_PLACEMENT_MODULE);
		if (o instanceof PlacementModule) {
			setPlacementModule((PlacementModule) o);
		} else {
			throw new InvalidParameterException(
					Constants.PARAMETER_PLACEMENT_MODULE, PlacementModule.class);
		}

	}

	public abstract void powerOff(Collection<Server> servers)
			throws DependencyNotSetException;

}