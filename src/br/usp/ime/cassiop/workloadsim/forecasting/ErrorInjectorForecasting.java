package br.usp.ime.cassiop.workloadsim.forecasting;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.exceptions.InvalidParameterException;
import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class ErrorInjectorForecasting extends WorkloadForecasting {

	private double variation = 0.0;

	private double meanError = 0.0;

	final Logger logger = LoggerFactory
			.getLogger(ErrorInjectorForecasting.class);

	public double getVariation() {
		return variation;
	}

	public void setVariation(double variation) {
		this.variation = variation;
	}

	public double getMeanError() {
		return meanError;
	}

	public void setMeanError(double meanError) {
		this.meanError = meanError;
	}

	@Override
	public void setParameters(Map<String, Object> parameters)
			throws InvalidParameterException {
		super.setParameters(parameters);

		Object o = null;

		o = parameters.get(Constants.PARAMETER_FORECASTING_MEAN_ERROR);
		if (o instanceof Double) {
			setMeanError(((Double) o).doubleValue());
		} else {
			throw new InvalidParameterException(
					Constants.PARAMETER_FORECASTING_MEAN_ERROR, Double.class);
		}

		o = parameters.get(Constants.PARAMETER_FORECASTING_VARIATION);
		if (o instanceof Double) {
			setVariation(((Double) o).doubleValue());
		} else {
			throw new InvalidParameterException(
					Constants.PARAMETER_FORECASTING_VARIATION, Double.class);
		}
	}

	public List<VirtualMachine> getPredictions(long currentTime)
			throws Exception {
		List<VirtualMachine> predictedDemand = super
				.getPredictions(currentTime);

		injectError(predictedDemand);

		logger.debug("Injected error in demand for time {}: {} ~ {}.",
				currentTime, meanError - variation, meanError + variation);

		return predictedDemand;
	}

	private void injectError(List<VirtualMachine> predictedDemand) {
		double errorFactor;
		Random random = new Random();

		double alfa, beta;
		alfa = variation * 2.0;
		beta = -variation + meanError + 1;

		for (VirtualMachine vm : predictedDemand) {

			// errorFactor = random() * variation * 2.0 - variation + meanError
			// + 1;
			// errorFactor = random() * alfa + beta;
			errorFactor = random.nextDouble() * alfa + beta;
			vm.setDemand(ResourceType.CPU, vm.getDemand(ResourceType.CPU)
					* errorFactor);

			errorFactor = random.nextDouble() * alfa + beta;
			vm.setDemand(ResourceType.MEMORY, vm.getDemand(ResourceType.MEMORY)
					* errorFactor);
		}
	}

}
