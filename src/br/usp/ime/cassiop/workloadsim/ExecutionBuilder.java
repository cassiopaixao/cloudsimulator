package br.usp.ime.cassiop.workloadsim;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.environment.GoogleCluster;
import br.usp.ime.cassiop.workloadsim.environment.IdealCluster;
import br.usp.ime.cassiop.workloadsim.forecasting.ErrorInjectorForecasting;
import br.usp.ime.cassiop.workloadsim.forecasting.MeasurementsMeanForecasting;
import br.usp.ime.cassiop.workloadsim.forecasting.WorkloadForecasting;
import br.usp.ime.cassiop.workloadsim.measurement.WorkloadMeasurement;
import br.usp.ime.cassiop.workloadsim.migrationcontrol.KhannaMigrationControl;
import br.usp.ime.cassiop.workloadsim.migrationcontrol.MigrateIfChange;
import br.usp.ime.cassiop.workloadsim.migrationcontrol.MigrateIfChangeAndServerBecomesOverloaded;
import br.usp.ime.cassiop.workloadsim.migrationcontrol.NoMigrationControl;
import br.usp.ime.cassiop.workloadsim.placement.BestFitDecreasing;
import br.usp.ime.cassiop.workloadsim.placement.FirstFitDecreasing;
import br.usp.ime.cassiop.workloadsim.placement.KhannaPlacement;
import br.usp.ime.cassiop.workloadsim.placement.WorstFitDecreasing;
import br.usp.ime.cassiop.workloadsim.statistic.DetailedExecutionStatistics;
import br.usp.ime.cassiop.workloadsim.statistic.FixedErrosExecutionsStatistics;
import br.usp.ime.cassiop.workloadsim.statistic.MigrationStatistics;
import br.usp.ime.cassiop.workloadsim.statistic.OneExecutionStatistics;
import br.usp.ime.cassiop.workloadsim.statistic.RandomizedExecutionsStatistics;
import br.usp.ime.cassiop.workloadsim.util.Constants;
import br.usp.ime.cassiop.workloadsim.workload.GoogleClusterdataFileWorkload;
import br.usp.ime.cassiop.workloadsim.workload.GoogleClusterdataWorkload;
import br.usp.ime.cassiop.workloadsim.workload.GoogleWorkload;

public class ExecutionBuilder {
	public enum PlacementType {
		FIRST_FIT, BEST_FIT, WORST_FIT, KHANNA
	};

	public enum StatisticsType {
		ONE_EXECUTION, FIXED_ERRORS, RANDOMIZED_EXECUTION, DETAILED_EXECUTION, MIGRATION_STATISTICS
	}

	public enum WorkloadToUse {
		GOOGLE_TRACE_1, GOOGLE_TRACE_2, GOOGLE_TRACE_FILE_2, NONE
	}

	public enum EnvironmentToUse {
		IDEAL, GOOGLE
	}

	public enum ForecasterToUse {
		WORKLOAD, WORKLOAD_ERROR_INJECTOR, MEASUREMENTS_MEAN
	}

	public enum MeasurementToUse {
		WORKLOAD
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
				.setVirtualizationManager(new VirtualizationManager());
	}

	public ExecutionConfiguration build() {
		executionConfiguration.setParameter(
				Constants.PARAMETER_STATISTICS_FILE, buildStatisticsFilename());

		return executionConfiguration;
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

	private File buildStatisticsFilename() {
		StringBuilder sb = new StringBuilder();

		sb.append("res/statistics_");
		switch (environment) {
		case IDEAL:
			sb.append("ideal_");
			break;
		case GOOGLE:
			sb.append("googlecluster_");
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
		case FIRST_FIT:
			sb.append("ffd_");
			break;
		case BEST_FIT:
			sb.append("bfd_");
			break;
		case WORST_FIT:
			sb.append("wfd_");
			break;
		case KHANNA:
			sb.append("kanna_");
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
		case NONE:
		}

		sb.append(".csv");

		return new File(sb.toString());
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
		case BEST_FIT:
			return new BestFitDecreasing();
		case WORST_FIT:
			return new WorstFitDecreasing();
		case FIRST_FIT:
			return new FirstFitDecreasing();
		case KHANNA:
			return new KhannaPlacement();
		}
		return new FirstFitDecreasing();
	}

	private Environment getEnvironment(EnvironmentToUse environment) {
		switch (environment) {
		case IDEAL:
			return new IdealCluster();
		case GOOGLE:
			return new GoogleCluster();
		}
		logger.info("Could not instantiate the environment {}",
				environment.toString());
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
			}
		} catch (Exception ex) {
			logger.info("Could not instantiate the workload {}",
					workload.toString());
		}
		return null;
	}
}
