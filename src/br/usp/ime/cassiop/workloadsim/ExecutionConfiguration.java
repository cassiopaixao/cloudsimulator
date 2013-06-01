package br.usp.ime.cassiop.workloadsim;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.migrationcontrol.NoMigrationControl;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.placement.PlacementStrategy;
import br.usp.ime.cassiop.workloadsim.placement.PlacementUtils;
import br.usp.ime.cassiop.workloadsim.poweroff.IdleMachinesPowerOffStrategy;
import br.usp.ime.cassiop.workloadsim.statistic.AllocationLog;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class ExecutionConfiguration extends Thread {

	static final Logger logger = LoggerFactory
			.getLogger(ExecutionConfiguration.class);

	private VirtualizationManager virtualizationManager = null;
	private PlacementModule placementModule = null;
	private PlacementStrategy placementStrategy = null;
	private PlacementUtils placementUtils = null;
	private ForecastingModule forecastingModule = null;
	private MeasurementModule measurementModule = null;
	private StatisticsModule statisticsModule = null;
	private Environment environment = null;
	private MigrationController migrationController = null;
	private PowerOffStrategy powerOffStrategy = null;
	private AllocationLog allocationLog = null;

	private Map<String, Object> parameters = null;

	private String fileSuffix = "";

	public ExecutionConfiguration() {
		parameters = new HashMap<String, Object>();
	}

	public void run() {
		Object o;
		double meanError = 0, variation = 0;
		long start_execution, interval, finish;

		o = parameters.get(Constants.PARAMETER_FORECASTING_MEAN_ERROR);
		if (o instanceof Double) {
			meanError = ((Double) o).doubleValue();
		}

		o = parameters.get(Constants.PARAMETER_FORECASTING_VARIATION);
		if (o instanceof Double) {
			variation = ((Double) o).doubleValue();
		}

		start_execution = Calendar.getInstance().getTime().getTime();

		logger.info("Starting simulation for error {} ~ {}.", 1 + meanError
				- variation, 1 + meanError + variation);

		try {
			execute();
		} catch (Exception e) {
			try {
				logger.error(
						"PlacementStrategy: {}\nEnvironment: {}\nMigrationController: {}",
						placementStrategy.getClass(), environment.getClass(),
						migrationController.getClass());
			} catch (Exception ex) {
				logger.error(
						"PlacementStrategy: {}\nEnvironment: {}\nMigrationController: {}",
						placementStrategy, environment, migrationController);
			}
			e.printStackTrace();
		} finally {

			ExecutionQueue.getInstance().endRun();

			logger.info("Simulation for error {} ~ {} done. File: {}", 1
					+ meanError - variation, 1 + meanError + variation,
					parameters.get(Constants.PARAMETER_STATISTICS_FILE));

			finish = Calendar.getInstance().getTime().getTime();
			interval = finish - start_execution;

			logger.info("Execution time: {}s", interval / 1000);
		}
	}

	public void execute() throws Exception {
		if (!parametersOk()) {
			throw new Exception(
					"There are some parameters missing for execution. Check the log file.");
		}

		parameters.put(Constants.PARAMETER_ENVIRONMENT, environment);
		parameters.put(Constants.PARAMETER_FORECASTING_MODULE,
				forecastingModule);
		parameters.put(Constants.PARAMETER_MEASUREMENT_MODULE,
				measurementModule);
		parameters.put(Constants.PARAMETER_PLACEMENT_MODULE, placementModule);
		parameters.put(Constants.PARAMETER_PLACEMENT_STRATEGY,
				placementStrategy);
		parameters.put(Constants.PARAMETER_VIRTUALIZATION_MANAGER,
				virtualizationManager);

		virtualizationManager.setParameters(parameters);
		placementModule.setParameters(parameters);
		if (placementStrategy != null) {
			placementStrategy.setParameters(parameters);
		}
		forecastingModule.setParameters(parameters);
		measurementModule.setParameters(parameters);
		statisticsModule.setParameters(parameters);
		environment.setParameters(parameters);
		migrationController.setParameters(parameters);
		powerOffStrategy.setParameters(parameters);

		if (allocationLog != null) {
			allocationLog.setParameters(parameters);
		}

		Object workload = parameters.get(Constants.PARAMETER_WORKLOAD);
		if (workload instanceof Workload) {
			workloadExecution((Workload) workload);
		}

	}

	private void workloadExecution(Workload workload) throws Exception {

		List<VirtualMachine> demand = null;

		long timeInterval = workload.getTimeInterval();
		long currentTime = workload.getInitialTime();
		long lastTime = workload.getLastTime();

		Object o = parameters.get(Constants.PARAMETER_CUSTOM_LAST_TIME);
		if (o instanceof Long) {
			if (((Long) o).longValue() > 0) {
				lastTime = ((Long) o).longValue();
			}
		}

		statisticsModule.initialize();
		if (allocationLog != null) {
			allocationLog.initialize();
		}
		// new consolidation status
		virtualizationManager.clear();
		try {
			while (currentTime <= lastTime) {

				demand = forecastingModule.getPredictions(currentTime);

				virtualizationManager
						.deallocateFinishedVms(demand, currentTime);

				demand = migrationController.control(demand);

				placementModule.consolidateAll(demand);

				powerOffStrategy.powerOff(virtualizationManager
						.getActiveServersList());

				if (allocationLog != null) {
					allocationLog.generateStatistics(currentTime);
				}

				statisticsModule.generateStatistics(currentTime);

				// some time after
				currentTime += timeInterval;
			}

		} catch (Exception ex) {
			logger.error("Exception thrown at time {}.", currentTime);

			throw ex;
		}
	}

	private boolean parametersOk() {
		boolean parametersOk = true;

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
		if (migrationController == null) {
			logger.info("MigrationController is not set. Using default NoMigrationControl");
			migrationController = new NoMigrationControl();
		}
		if (powerOffStrategy == null) {
			logger.info("PowerOffStrategy is not set. Using default IdleMachinesPowerOffStrategy");
			powerOffStrategy = new IdleMachinesPowerOffStrategy();
		}
		return parametersOk;
	}

	public void addToFileName(String suffix) {
		String strPath = ((File) parameters
				.get(Constants.PARAMETER_STATISTICS_FILE)).toString();
		this.setParameter(Constants.PARAMETER_STATISTICS_FILE,
				new File(strPath.replace(fileSuffix + ".csv", suffix + ".csv")));

		fileSuffix = suffix;
	}

	public void setParameter(String name, Object value) {
		parameters.put(name, value);
		if (name.equals(Constants.PARAMETER_VIRTUALIZATION_MANAGER)) {
			setVirtualizationManager((VirtualizationManagerImpl) value);
		} else if (name.equals(Constants.PARAMETER_ENVIRONMENT)) {
			setEnvironment((Environment) value);
		} else if (name.equals(Constants.PARAMETER_PLACEMENT_MODULE)) {
			setPlacementModule((PlacementModule) value);
		} else if (name.equals(Constants.PARAMETER_PLACEMENT_STRATEGY)) {
			setPlacementStrategy((PlacementStrategy) value);
		} else if (name.equals(Constants.PARAMETER_PLACEMENT_UTILS)) {
			setPlacementUtils((PlacementUtils) value);
		} else if (name.equals(Constants.PARAMETER_FORECASTING_MODULE)) {
			setForecastingModule((ForecastingModule) value);
		} else if (name.equals(Constants.PARAMETER_STATISTICS_MODULE)) {
			setStatisticsModule((StatisticsModule) value);
		} else if (name.equals(Constants.PARAMETER_MIGRATION_CONTROLLER)) {
			setMigrationController((MigrationController) value);
		} else if (name.equals(Constants.PARAMETER_MEASUREMENT_MODULE)) {
			setMeasurementModule((MeasurementModule) value);
		} else if (name.equals(Constants.PARAMETER_LOG_PATH)) {
			allocationLog = new AllocationLog();
		}
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public VirtualizationManager getVirtualizationManager() {
		return virtualizationManager;
	}

	public void setVirtualizationManager(
			VirtualizationManagerImpl virtualizationManager) {
		this.virtualizationManager = virtualizationManager;
	}

	public PlacementModule getPlacementModule() {
		return placementModule;
	}

	public void setPlacementModule(PlacementModule placementModule) {
		this.placementModule = placementModule;
	}

	public PlacementStrategy getPlacementStrategy() {
		return placementStrategy;
	}

	public void setPlacementStrategy(PlacementStrategy placementStrategy) {
		this.placementStrategy = placementStrategy;
	}

	public PlacementUtils getPlacementUtils() {
		return placementUtils;
	}

	public void setPlacementUtils(PlacementUtils placementUtils) {
		this.placementUtils = placementUtils;
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

	public MigrationController getMigrationController() {
		return migrationController;
	}

	public void setMigrationController(MigrationController migrationController) {
		this.migrationController = migrationController;
	}

	public PowerOffStrategy getPowerOffStrategy() {
		return powerOffStrategy;
	}

	public void setPowerOffStrategy(PowerOffStrategy powerOffStrategy) {
		this.powerOffStrategy = powerOffStrategy;
	}
}
