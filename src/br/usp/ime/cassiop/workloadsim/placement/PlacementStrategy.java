package br.usp.ime.cassiop.workloadsim.placement;

import java.util.List;
import java.util.Map;

import br.usp.ime.cassiop.workloadsim.Parametrizable;
import br.usp.ime.cassiop.workloadsim.ServerTypeChooser;
import br.usp.ime.cassiop.workloadsim.exceptions.InvalidParameterException;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public abstract class PlacementStrategy implements Parametrizable,
		ServerTypeChooser {

	protected PlacementUtils placementUtils = null;

	public void setPlacementUtils(PlacementUtils placementUtils) {
		this.placementUtils = placementUtils;
	}

	public void setParameters(Map<String, Object> parameters)
			throws InvalidParameterException {
		Object o = null;

		o = parameters.get(Constants.PARAMETER_PLACEMENT_UTILS);
		if (o instanceof PlacementUtils) {
			setPlacementUtils((PlacementUtils) o);
		} else {
			throw new InvalidParameterException(
					Constants.PARAMETER_PLACEMENT_UTILS, PlacementUtils.class);
		}
	}

	public abstract void orderServers(List<Server> servers);

	public abstract void orderDemand(List<VirtualMachine> demand);

	public abstract Server selectDestinationServer(VirtualMachine vm,
			List<Server> servers);

	public abstract Server chooseServerType(VirtualMachine vmDemand,
			List<Server> machineTypes);
}
