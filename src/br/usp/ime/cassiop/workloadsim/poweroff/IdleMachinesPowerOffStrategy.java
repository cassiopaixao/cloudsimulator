package br.usp.ime.cassiop.workloadsim.poweroff;

import java.util.Map;

import br.usp.ime.cassiop.workloadsim.exceptions.InvalidParameterException;

public class IdleMachinesPowerOffStrategy extends LowUtilizationPowerOffStrategy{

	@Override
	public void setParameters(Map<String, Object> parameters)
			throws InvalidParameterException {
		super.setParameters(parameters);
		
		setLowUtilization(0.0);
	}
	


}
