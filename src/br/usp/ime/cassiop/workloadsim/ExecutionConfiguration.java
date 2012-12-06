package br.usp.ime.cassiop.workloadsim;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class ExecutionConfiguration {

	static final Logger logger = LoggerFactory
			.getLogger(ExecutionConfiguration.class);

	private VirtualizationManager virtualizationManager = null;
	private PlacementModule placementModule = null;
	private ForecastingModule forecastingModule = null;
	private MeasurementModule measurementModule = null;
	private StatisticsModule statisticsModule = null;
	private Environment environment = null;

	private Map<String, Object> parameters = null;

	public ExecutionConfiguration() {
		parameters = new HashMap<String, Object>();
	}

	public void run() throws Exception {
		if (!parametersOk()) {
			throw new Exception(
					"There are some parameters missing for execution. Check the log file.");
		}

		try {

			parameters.put(Constants.PARAMETER_ENVIRONMENT, environment);
			parameters.put(Constants.PARAMETER_FORECASTING_MODULE,
					forecastingModule);
			parameters.put(Constants.PARAMETER_MEASUREMENT_MODULE,
					measurementModule);
			parameters.put(Constants.PARAMETER_PLACEMENT_MODULE,
					placementModule);
			parameters.put(Constants.PARAMETER_VIRTUALIZATION_MANAGER,
					virtualizationManager);

			virtualizationManager.setParameters(parameters);
			placementModule.setParameters(parameters);
			forecastingModule.setParameters(parameters);
			measurementModule.setParameters(parameters);
			statisticsModule.setParameters(parameters);

			Object workload = parameters.get(Constants.PARAMETER_WORKLOAD);

			if (workload instanceof Workload) {
				workloadExecution((Workload) workload);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void workloadExecution(Workload workload) throws Exception {

		List<VirtualMachine> demand = null;

		long timeInterval = workload.getTimeInterval();
		long currentTime = workload.getInitialTime();
		long lastTime = workload.getLastTime();

		while (currentTime <= lastTime) {
			// new consolidation status
			virtualizationManager.clear();
			demand = forecastingModule.getPredictions(currentTime);
			placementModule.setDemand(demand);
			placementModule.consolidateAll();

			// some time after
			currentTime += timeInterval;

			statisticsModule.generateStatistics(currentTime);
		}
	}

	private boolean parametersOk() {
		boolean parametersOk = true;
		// TODO Verify if all needed parameters are set.

		if (virtualizationManager == null) {
			logger.info("VirtualizationManager is not set.");
			parametersOk = false;
		} else {
			// verify if virtualizationManager's parameters are set.
		}
		if (placementModule == null) {
			logger.info("PlacementModule is not set.");
			parametersOk = false;
		}
		if (forecastingModule == null) {
			logger.info("ForecastingModule is not set.");
			parametersOk = false;
		}
		if (measurementModule == null) {
			logger.info("MeasurementModule is not set.");
			parametersOk = false;
		}
		if (statisticsModule == null) {
			logger.info("StatisticsModule is not set.");
			parametersOk = false;
		}
		if (environment == null) {
			logger.info("Environment is not set.");
			parametersOk = false;
		}
		return parametersOk;
	}

	public void setParameter(String name, Object value) {
		parameters.put(name, value);
	}

	public VirtualizationManager getVirtualizationManager() {
		return virtualizationManager;
	}

	public void setVirtualizationManager(
			VirtualizationManager virtualizationManager) {
		this.virtualizationManager = virtualizationManager;
	}

	public PlacementModule getPlacementModule() {
		return placementModule;
	}

	public void setPlacementModule(PlacementModule placementModule) {
		this.placementModule = placementModule;
	}

	public ForecastingModule getForecastingModule() {
		return forecastingModule;
	}

	public void setForecastingModule(ForecastingModule forecastingModule) {
		this.forecastingModule = forecastingModule;
	}

	public MeasurementModule getMeasurementModule() {
		return measurementModule;
	}

	public void setMeasurementModule(MeasurementModule measurementModule) {
		this.measurementModule = measurementModule;
	}

	public StatisticsModule getStatisticsModule() {
		return statisticsModule;
	}

	public void setStatisticsModule(StatisticsModule statisticsModule) {
		this.statisticsModule = statisticsModule;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}
}
