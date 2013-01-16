package br.usp.ime.cassiop.workloadsim;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.environment.GoogleCluster;
import br.usp.ime.cassiop.workloadsim.environment.IdealCluster;
import br.usp.ime.cassiop.workloadsim.forecasting.ErrorInjectorForecasting;
import br.usp.ime.cassiop.workloadsim.forecasting.WorkloadForecasting;
import br.usp.ime.cassiop.workloadsim.measurement.WorkloadMeasurement;
import br.usp.ime.cassiop.workloadsim.placement.BestFitDecreasing;
import br.usp.ime.cassiop.workloadsim.placement.FirstFitDecreasing;
import br.usp.ime.cassiop.workloadsim.placement.WorstFitDecreasing;
import br.usp.ime.cassiop.workloadsim.statistic.DetailedExecutionStatistics;
import br.usp.ime.cassiop.workloadsim.statistic.FixedErrosExecutionsStatistics;
import br.usp.ime.cassiop.workloadsim.statistic.OneExecutionStatistics;
import br.usp.ime.cassiop.workloadsim.statistic.RandomizedExecutionsStatistics;
import br.usp.ime.cassiop.workloadsim.util.Constants;
import br.usp.ime.cassiop.workloadsim.workload.GoogleClusterdataFileWorkload;
import br.usp.ime.cassiop.workloadsim.workload.GoogleClusterdataWorkload;
import br.usp.ime.cassiop.workloadsim.workload.GoogleWorkload;

public class ExecutionBuilder {
	public enum PlacementType {
		FIRST_FIT, BEST_FIT, WORST_FIT
	};

	public enum StatisticsType {
		ONE_EXECUTION, FIXED_ERRORS, RANDOMIZED_EXECUTION, DETAILED_EXECUTION
	}

	public enum WorkloadToUse {
		GOOGLE_TRACE_1, GOOGLE_TRACE_2, GOOGLE_TRACE_FILE_2, NONE
	}

	public enum EnvironmentToUse {
		IDEAL, GOOGLE
	}

	public enum ForecasterToUse {
		WORKLOAD, WORKLOAD_ERROR_INJECTOR
	}

	public enum MeasurementToUse {
		WORKLOAD
	}

	static final Logger logger = LoggerFactory
			.getLogger(ExecutionBuilder.class);

	public static ExecutionConfiguration build(EnvironmentToUse environment,
			ForecasterToUse forecasting, PlacementType placement,
			StatisticsType statistics, WorkloadToUse workload) {

		ExecutionConfiguration execution = new ExecutionConfiguration();

		execution.setEnvironment(getEnvironment(environment));
		execution.setVirtualizationManager(new VirtualizationManager());
		execution.setPlacementModule(getPlacementModule(placement));
		execution.setStatisticsModule(getStatisticsModule(statistics));
		execution
				.setMeasurementModule(getMeasurementModule(MeasurementToUse.WORKLOAD));
		execution.setForecastingModule(getForecastingModule(forecasting));

		configureWorkload(execution, workload);

		execution.setParameter(
				Constants.PARAMETER_STATISTICS_FILE,
				buildStatisticsFilename(environment, placement, statistics,
						workload));

		return execution;
	}

	private static Path buildStatisticsFilename(EnvironmentToUse environment,
			PlacementType placement, StatisticsType statistics,
			WorkloadToUse workload) {
		StringBuilder sb = new StringBuilder();

		sb.append("res/statistics_");
		switch (environment) {
		case IDEAL:
			sb.append("ideal_");
			break;
		case GOOGLE:
			sb.append("googlecluster_");
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

		return Paths.get(sb.toString());
	}

	private static MeasurementModule getMeasurementModule(
			MeasurementToUse measurement) {
		switch (measurement) {
		case WORKLOAD:
		default:
			return new WorkloadMeasurement();
		}
	}

	private static ForecastingModule getForecastingModule(
			ForecasterToUse forecasting) {
		switch (forecasting) {
		case WORKLOAD_ERROR_INJECTOR:
			return new ErrorInjectorForecasting();
		case WORKLOAD:
		default:
			return new WorkloadForecasting();
		}
	}

	private static StatisticsModule getStatisticsModule(
			StatisticsType statistics) {
		switch (statistics) {
		case FIXED_ERRORS:
			return new FixedErrosExecutionsStatistics();
		case RANDOMIZED_EXECUTION:
			return new RandomizedExecutionsStatistics();
		case ONE_EXECUTION:
			return new OneExecutionStatistics();
		case DETAILED_EXECUTION:
			return new DetailedExecutionStatistics();
		default:
			return null;
		}
	}

	private static PlacementModule getPlacementModule(PlacementType placement) {
		switch (placement) {
		case BEST_FIT:
			return new BestFitDecreasing();
		case WORST_FIT:
			return new WorstFitDecreasing();
		case FIRST_FIT:
		default:
			return new FirstFitDecreasing();
		}
	}

	private static Environment getEnvironment(EnvironmentToUse environment) {
		switch (environment) {
		case IDEAL:
			return new IdealCluster();
		case GOOGLE:
			return new GoogleCluster();
		default:
			logger.info("Could not instantiate the environment {}",
					environment.toString());
			return null;
		}
	}

	private static void configureWorkload(ExecutionConfiguration execution,
			WorkloadToUse workload) {
		try {
			switch (workload) {
			case GOOGLE_TRACE_1:
				execution.setParameter(Constants.PARAMETER_WORKLOAD,
						GoogleWorkload.build());
				break;
			case GOOGLE_TRACE_2:
				execution.setParameter(Constants.PARAMETER_WORKLOAD,
						GoogleClusterdataWorkload.build());
				break;
			case GOOGLE_TRACE_FILE_2:
				execution.setParameter(Constants.PARAMETER_WORKLOAD,
						GoogleClusterdataFileWorkload.build());
				break;
			case NONE:
				break;
			}
		} catch (Exception ex) {
			logger.info("Could not instantiate the workload {}",
					workload.toString());
		}
	}
}
