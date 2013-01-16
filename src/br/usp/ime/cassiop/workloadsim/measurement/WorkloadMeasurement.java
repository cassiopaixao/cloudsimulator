package br.usp.ime.cassiop.workloadsim.measurement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.usp.ime.cassiop.workloadsim.Measurement;
import br.usp.ime.cassiop.workloadsim.MeasurementModule;
import br.usp.ime.cassiop.workloadsim.Workload;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class WorkloadMeasurement implements MeasurementModule {

	private Workload workload = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.ime.usp.cassiop.workloadsim.MeasurementModule#measureSystem(long)
	 */
	@Override
	public Measurement measureSystem(long currentTime) throws Exception {

		Measurement currentMeasure = new Measurement();

		List<VirtualMachine> demand = workload.getDemand(currentTime);

		Map<String, VirtualMachine> actualDemand = new HashMap<String, VirtualMachine>();

		for (VirtualMachine vm : demand) {
			actualDemand.put(vm.getName(), vm);
		}

		currentMeasure.setTime(currentTime);
		currentMeasure.setActualDemand(actualDemand);

		return currentMeasure;
	}

	public void setWorkload(Workload workload) {
		this.workload = workload;
	}

	public Workload getWorkload() {
		return workload;
	}
	
	@Override
	public void setParameters(Map<String, Object> parameters) throws Exception {
		Object o = parameters.get(Constants.PARAMETER_WORKLOAD);
		if (o instanceof Workload) {
			setWorkload((Workload) o);
		} else {
			throw new Exception(String.format("Invalid parameter: %s",
					Constants.PARAMETER_WORKLOAD));
		}
	}
}
