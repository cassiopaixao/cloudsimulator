package br.usp.ime.cassiop.workloadsim;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.environment.GoogleCluster;
import br.usp.ime.cassiop.workloadsim.environment.HomogeneousCluster;
import br.usp.ime.cassiop.workloadsim.environment.TestCluster;
import br.usp.ime.cassiop.workloadsim.forecasting.ErrorInjectorForecasting;
import br.usp.ime.cassiop.workloadsim.forecasting.MeasurementsMeanForecasting;
import br.usp.ime.cassiop.workloadsim.forecasting.WorkloadForecasting;
import br.usp.ime.cassiop.workloadsim.measurement.WorkloadMeasurement;
import br.usp.ime.cassiop.workloadsim.migrationcontrol.KhannaMigrationControl;
import br.usp.ime.cassiop.workloadsim.migrationcontrol.MigrateIfChange;
import br.usp.ime.cassiop.workloadsim.migrationcontrol.MigrateIfChangeAndServerBecomesOverloaded;
import br.usp.ime.cassiop.workloadsim.migrationcontrol.NoMigrationControl;
import br.usp.ime.cassiop.workloadsim.placement.AlmostWorstFit;
import br.usp.ime.cassiop.workloadsim.placement.AlmostWorstFitDecreasing;
import br.usp.ime.cassiop.workloadsim.placement.BestFitDecreasing;
import br.usp.ime.cassiop.workloadsim.placement.FirstFitDecreasing;
import br.usp.ime.cassiop.workloadsim.placement.KhannaPlacement;
import br.usp.ime.cassiop.workloadsim.placement.WorstFitDecreasing;
import br.usp.ime.cassiop.workloadsim.poweroff.IdleMachinesPowerOffStrategy;
import br.usp.ime.cassiop.workloadsim.poweroff.LowUtilizationPowerOffStrategy;
import br.usp.ime.cassiop.workloadsim.statistic.DetailedExecutionStatistics;
import br.usp.ime.cassiop.workloadsim.statistic.FixedErrosExecutionsStatistics;
import br.usp.ime.cassiop.workloadsim.statistic.MigrationStatistics;
import br.usp.ime.cassiop.workloadsim.statistic.OneExecutionStatistics;
import br.usp.ime.cassiop.workloadsim.statistic.RandomizedExecutionsStatistics;
import br.usp.ime.cassiop.workloadsim.util.Constants;
import br.usp.ime.cassiop.workloadsim.workload.GoogleClusterdataFileWorkload;
import br.usp.ime.cassiop.workloadsim.workload.GoogleClusterdataWorkload;
import br.usp.ime.cassiop.workloadsim.workload.GoogleWorkload;
import br.usp.ime.cassiop.workloadsim.workload.TestWorkload;

public class ExecutionBuilder {
	public enum PlacementType {
		FIRST_FIT_DEC, BEST_FIT_DEC, WORST_FIT_DEC, KHANNA, ALMOST_WORST_FIT, ALMOST_WORST_FIT_DEC
	};

	public enum StatisticsType {
		ONE_EXECUTION, FIXED_ERRORS, RANDOMIZED_EXECUTION, DETAILED_EXECUTION, MIGRATION_STATISTICS
	}

	public enum WorkloadToUse {
		GOOGLE_TRACE_1, GOOGLE_TRACE_2, GOOGLE_TRACE_FILE_2, TEST_TRACE, NONE
	}

	public enum EnvironmentToUse {
		HOMOGENEOUS, GOOGLE, TEST
	}

	public enum ForecasterToUse {
		WORKLOAD, WORKLOAD_ERROR_INJECTOR, MEASUREMENTS_MEAN
	}

	public enum MeasurementToUse {
		WORKLOAD
	}

	public enum PowerOffStrategyToUse {
		LOW_UTILIZATION, IDLE_MACHINES
	}

	public enum MigrationControlToUse {
		NONE, MIGRATE_IF_CHANGE, MIGRATE_IF_CHANGE_AND_SERVER_BECOMES_OVERLOADED, KHANNA
	}

	private PlacementType placement = null;
	private StatisticsType statistics = null;
	private WorkloadToUse workload = null;
	private EnvironmentToUse environment = null;
	private ForecasterToUse forecaster = null;
	private MeasurementToUse measurement = null;
	private MigrationControlToUse migration = null;
	private PowerOffStrategyToUse powerOffStrategy = null;
	private boolean shouldLog = false;

	public void setMigrationController(MigrationControlToUse migration) {
		executionConfiguration.setParameter(
				Constants.PARAMETER_MIGRATION_CONTROLLER,
				getMigrationController(migration));
		this.migration = migration;
	}

	private ExecutionConfiguration executionConfiguration = null;

	static final Logger logger = LoggerFactory
			.getLogger(ExecutionBuilder.class);

	public ExecutionBuilder() {
		executionConfiguration = new ExecutionConfiguration();

		executionConfiguration
				.setVirtualizationManager(new VirtualizationManagerImpl());

	}

	public ExecutionConfiguration build() throws IOException {
		executionConfiguration.setParameter(
				Constants.PARAMETER_STATISTICS_FILE, buildStatisticsFilename());

		if (shouldLog) {
			executionConfiguration.setParameter(Constants.PARAMETER_LOG_PATH,
					buildLogPath());
		}

		return executionConfiguration;
	}

	public void setParameter(String parameter, Object value) {
		executionConfiguration.setParameter(parameter, value);
	}

	public void setEnvironment(EnvironmentToUse environment) {
		executionConfiguration.setParameter(Constants.PARAMETER_ENVIRONMENT,
				getEnvironment(environment));
		this.environment = environment;
	}

	public void setWorkload(WorkloadToUse workload) {
		executionConfiguration.setParameter(Constants.PARAMETER_WORKLOAD,
				getWorkload(workload));
		this.workload = workload;
	}

	public void setVirtualizationManager(
			VirtualizationManager virtualizationManager) {
		executionConfiguration.setParameter(
				Constants.PARAMETER_VIRTUALIZATION_MANAGER,
				virtualizationManager);
	}

	public void setPlacementModule(PlacementType placement) {
		executionConfiguration.setParameter(
				Constants.PARAMETER_PLACEMENT_MODULE,
				getPlacementModule(placement));
		this.placement = placement;
	}

	public void setStatisticsModule(StatisticsType statistics) {
		executionConfiguration.setParameter(
				Constants.PARAMETER_STATISTICS_MODULE,
				getStatisticsModule(statistics));
		this.statistics = statistics;
	}

	public void setMeasurementModule(MeasurementToUse measurement) {
		executionConfiguration.setParameter(
				Constants.PARAMETER_MEASUREMENT_MODULE,
				getMeasurementModule(measurement));
		this.measurement = measurement;
	}

	public void setForecastingModule(ForecasterToUse forecaster) {
		executionConfiguration.setParameter(
				Constants.PARAMETER_FORECASTING_MODULE,
				getForecastingModule(forecaster));
		this.forecaster = forecaster;
	}

	public void setPowerOffStrategy(PowerOffStrategyToUse powerOffStrategy) {
		executionConfiguration.setParameter(
				Constants.PARAMETER_POWER_OFF_STRATEGY,
				getPowerOffStrategy(powerOffStrategy));
		this.powerOffStrategy = powerOffStrategy;
	}

	public void setShouldLog(boolean shouldLog) {
		this.shouldLog = shouldLog;
	}

	private File buildStatisticsFilename() {
		StringBuilder sb = new StringBuilder();

		sb.append("res/statistics_");
		switch (environment) {
		case HOMOGENEOUS:
			sb.append("homogeneous_");
			break;
		case GOOGLE:
			sb.append("googlecluster_");
			break;
		case TEST:
			sb.append("test_");
		}

		switch (forecaster) {
		case MEASUREMENTS_MEAN:
			sb.append("mean_");
			break;
		case WORKLOAD:
			sb.append("workload_");
			break;
		case WORKLOAD_ERROR_INJECTOR:
			sb.append("errorinjector_");
			break;
		}

		switch (migration) {
		case MIGRATE_IF_CHANGE:
			sb.append("ifchange_");
			break;
		case MIGRATE_IF_CHANGE_AND_SERVER_BECOMES_OVERLOADED:
			sb.append("ifoverload_");
			break;
		case KHANNA:
			sb.append("khanna_");
			break;
		case NONE:
			break;
		}

		switch (placement) {
		case FIRST_FIT_DEC:
			sb.append("ffd_");
			break;
		case BEST_FIT_DEC:
			sb.append("bfd_");
			break;
		case WORST_FIT_DEC:
			sb.append("wfd_");
			break;
		case KHANNA:
			sb.append("khanna_");
			break;
		case ALMOST_WORST_FIT:
			sb.append("awf_");
			break;
		case ALMOST_WORST_FIT_DEC:
			sb.append("awfd_");
			break;
		}

		switch (statistics) {
		case FIXED_ERRORS:
			sb.append("fixed_");
			break;
		case RANDOMIZED_EXECUTION:
			sb.append("random_");
		case ONE_EXECUTION:
			break;
		case DETAILED_EXECUTION:
			sb.append("detailed_");
			break;
		case MIGRATION_STATISTICS:
			sb.append("migration_");
			break;
		}

		switch (workload) {
		case GOOGLE_TRACE_1:
			sb.append("7h");
			break;
		case GOOGLE_TRACE_2:
			sb.append("30d");
			break;
		case GOOGLE_TRACE_FILE_2:
			sb.append("30d-file");
			break;
		case TEST_TRACE:
			sb.append("test");
			break;
		case NONE:
		}

		if (forecaster == ForecasterToUse.WORKLOAD_ERROR_INJECTOR) {
			sb.append(String.format(
					"(%.2f)",
					(Double) executionConfiguration.getParameters().get(
							Constants.PARAMETER_FORECASTING_MEAN_ERROR)));
		}

		sb.append(".csv");

		return new File(sb.toString());
	}

	private File buildLogPath() throws IOException {
		File path = null;
		String canonicalPath = "log/";
		boolean canContinue = false;

		path = new File(canonicalPath);

		if (path.isDirectory() && path.canWrite()) {
			canContinue = true;
		} else {
			canContinue = path.mkdir();
		}
		if (!canContinue) {
			throw new IOException("Can't write to " + path.getCanonicalPath()
					+ " directory. Check folder's permissions.");
		}

		String migControl = null;
		switch (migration) {
		case MIGRATE_IF_CHANGE:
			migControl = "ifchange";
			break;
		case MIGRATE_IF_CHANGE_AND_SERVER_BECOMES_OVERLOADED:
			migControl = "ifoverload";
			break;
		case KHANNA:
			migControl = "khanna";
			break;
		case NONE:
			migControl = "nomigcontrol";
			break;
		}
		canonicalPath = String.format(
				"%s%s%.0f/",
				canonicalPath,
				migControl,
				(((Double) executionConfiguration.getParameters().get(
						Constants.PARAMETER_FORECASTING_MEAN_ERROR)) * 100));

		path = new File(canonicalPath);

		if (path.isDirectory() && path.canWrite()) {
			canContinue = true;
		} else {
			canContinue = path.mkdir();
		}
		if (!canContinue) {
			throw new IOException("Can't write to " + path.getCanonicalPath()
					+ " directory. Check folder's permissions.");
		}

		switch (placement) {
		case BEST_FIT_DEC:
			canonicalPath = canonicalPath.concat("bfd/");
			break;
		case FIRST_FIT_DEC:
			canonicalPath = canonicalPath.concat("ffd/");
			break;
		case WORST_FIT_DEC:
			canonicalPath = canonicalPath.concat("wfd/");
			break;
		case ALMOST_WORST_FIT:
			canonicalPath = canonicalPath.concat("awf/");
			break;
		case ALMOST_WORST_FIT_DEC:
			canonicalPath = canonicalPath.concat("awfd/");
			break;
		case KHANNA:
		}

		path = new File(canonicalPath);

		if (path.isDirectory() && path.canWrite()) {
			canContinue = true;
		} else {
			canContinue = path.mkdir();
		}
		if (!canContinue) {
			throw new IOException("Can't write to " + path.getCanonicalPath()
					+ " directory. Check folder's permissions.");
		}

		return path;
	}

	private MeasurementModule getMeasurementModule(MeasurementToUse measurement) {
		switch (measurement) {
		case WORKLOAD:
			return new WorkloadMeasurement();
		}
		return new WorkloadMeasurement();
	}

	private ForecastingModule getForecastingModule(ForecasterToUse forecasting) {
		switch (forecasting) {
		case WORKLOAD_ERROR_INJECTOR:
			return new ErrorInjectorForecasting();
		case WORKLOAD:
			return new WorkloadForecasting();
		case MEASUREMENTS_MEAN:
			return new MeasurementsMeanForecasting();
		}
		return new WorkloadForecasting();
	}

	private StatisticsModule getStatisticsModule(StatisticsType statistics) {
		switch (statistics) {
		case FIXED_ERRORS:
			return new FixedErrosExecutionsStatistics();
		case RANDOMIZED_EXECUTION:
			return new RandomizedExecutionsStatistics();
		case ONE_EXECUTION:
			return new OneExecutionStatistics();
		case DETAILED_EXECUTION:
			return new DetailedExecutionStatistics();
		case MIGRATION_STATISTICS:
			return new MigrationStatistics();
		}
		return null;
	}

	private PlacementModule getPlacementModule(PlacementType placement) {
		switch (placement) {
		case BEST_FIT_DEC:
			return new BestFitDecreasing();
		case WORST_FIT_DEC:
			return new WorstFitDecreasing();
		case FIRST_FIT_DEC:
			return new FirstFitDecreasing();
		case KHANNA:
			return new KhannaPlacement();
		case ALMOST_WORST_FIT:
			return new AlmostWorstFit();
		case ALMOST_WORST_FIT_DEC:
			return new AlmostWorstFitDecreasing();
		}
		return new FirstFitDecreasing();
	}

	private Environment getEnvironment(EnvironmentToUse environment) {
		switch (environment) {
		case HOMOGENEOUS:
			return new HomogeneousCluster();
		case GOOGLE:
			return new GoogleCluster();
		case TEST:
			return new TestCluster();
		}
		logger.info("Could not instantiate the environment {}",
				environment.toString());
		return null;
	}

	private PowerOffStrategy getPowerOffStrategy(
			PowerOffStrategyToUse powerOffStrategy) {
		switch (powerOffStrategy) {
		case LOW_UTILIZATION:
			return new LowUtilizationPowerOffStrategy();
		case IDLE_MACHINES:
			return new IdleMachinesPowerOffStrategy();
		}
		return null;
	}

	private MigrationController getMigrationController(
			MigrationControlToUse migration) {
		switch (migration) {
		case NONE:
			return new NoMigrationControl();
		case MIGRATE_IF_CHANGE:
			return new MigrateIfChange();
		case MIGRATE_IF_CHANGE_AND_SERVER_BECOMES_OVERLOADED:
			return new MigrateIfChangeAndServerBecomesOverloaded();
		case KHANNA:
			return new KhannaMigrationControl();
		}
		return new NoMigrationControl();
	}

	private Workload getWorkload(WorkloadToUse workload) {
		try {
			switch (workload) {
			case GOOGLE_TRACE_1:
				return GoogleWorkload.build();
			case GOOGLE_TRACE_2:
				return GoogleClusterdataWorkload.build();
			case GOOGLE_TRACE_FILE_2:
				return GoogleClusterdataFileWorkload.build();
			case NONE:
				return null;
			case TEST_TRACE:
				return TestWorkload.build();
			}
		} catch (Exception ex) {
			logger.info("Could not instantiate the workload {}",
					workload.toString());
		}
		return null;
	}
}
