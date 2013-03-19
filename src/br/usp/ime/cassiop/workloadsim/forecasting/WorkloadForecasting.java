package br.usp.ime.cassiop.workloadsim.forecasting;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.ForecastingModule;
import br.usp.ime.cassiop.workloadsim.Workload;
import br.usp.ime.cassiop.workloadsim.exceptions.InvalidParameterException;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class WorkloadForecasting implements ForecastingModule {

	protected Workload workload = null;

	private long lastPredictionTime = -1;

	final Logger logger = LoggerFactory.getLogger(WorkloadForecasting.class);

	public WorkloadForecasting() {
		super();
	}

	public void setWorkload(Workload workload) {
		this.workload = workload;
	}

	@Override
	public void setParameters(Map<String, Object> parameters)
			throws InvalidParameterException {
		Object o = parameters.get(Constants.PARAMETER_WORKLOAD);
		if (o instanceof Workload) {
			setWorkload((Workload) o);
		} else {
			throw new InvalidParameterException(Constants.PARAMETER_WORKLOAD,
					Workload.class);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.ime.usp.cassiop.workloadsim.ForecastingModule#getPredictions(long)
	 */
	@Override
	public List<VirtualMachine> getPredictions(long currentTime)
			throws Exception {
		if (workload == null) {
			throw new Exception("Workload for demands is not set.");
		}
		if (!workload.hasDemand(currentTime)) {
			throw new Exception("Workload time limit has been reached.");
		}

		List<VirtualMachine> predictedDemand = workload.getDemand(currentTime);

		logger.debug(String
				.format("Demand for time %d retrieved.", currentTime));

		lastPredictionTime = currentTime;

		return predictedDemand;
	}

	public boolean hasPrediction(long currentTime) {
		return workload.hasDemand(currentTime);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.ime.usp.cassiop.workloadsim.ForecastingModule#getTimeOfLastPredictions
	 * ()
	 */
	@Override
	public long getTimeOfLastPredictions() {
		return lastPredictionTime;
	}

}