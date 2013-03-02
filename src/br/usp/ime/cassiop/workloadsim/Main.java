package br.usp.ime.cassiop.workloadsim;

import java.util.List;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.ExecutionBuilder.EnvironmentToUse;
import br.usp.ime.cassiop.workloadsim.ExecutionBuilder.ForecasterToUse;
import br.usp.ime.cassiop.workloadsim.ExecutionBuilder.MeasurementToUse;
import br.usp.ime.cassiop.workloadsim.ExecutionBuilder.MigrationControlToUse;
import br.usp.ime.cassiop.workloadsim.ExecutionBuilder.PlacementType;
import br.usp.ime.cassiop.workloadsim.ExecutionBuilder.StatisticsType;
import br.usp.ime.cassiop.workloadsim.ExecutionBuilder.WorkloadToUse;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class Main {

	static final Logger logger = LoggerFactory.getLogger(Main.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		List<Double> errorArgs = null;

		boolean shouldLog = false;

		MigrationControlToUse migrationToUse = MigrationControlToUse.NONE;

		PlacementType placementToUse = null;

		EnvironmentToUse environmentToUse = EnvironmentToUse.HOMOGENEOUS;

		try {
			OptionParser parser = new OptionParser();
			// error in prediction
			OptionSpec<Double> predictionErrorArg = parser
					.accepts("e", "error in prediction").withRequiredArg()
					.ofType(Double.class).describedAs("error")
					.defaultsTo(new Double(0.0));

			// migration control to use
			OptionSpec<String> migrationArg = parser
					.accepts("m",
							"migration control to use (khanna, ifchange, ifoverload, none, all)")
					.withRequiredArg().ofType(String.class).defaultsTo("none");

			// placement strategy to use
			OptionSpec<String> placementArg = parser
					.accepts("p",
							"placement strategy to use (khanna, ffd, bfd, wfd, all)")
					.withRequiredArg().ofType(String.class).defaultsTo("all");

			// environment
			parser.accepts("g", "use heterogeneous google environment");
			// accepts("h", "use homogeneous environment");

			// log
			parser.accepts("l", "log path");

			// number of threads
			OptionSpec<Integer> numberOfThreadsArg = parser
					.accepts("t", "maximum threads").withRequiredArg()
					.ofType(Integer.class).describedAs("threads").defaultsTo(1);

			OptionSet options = parser.parse(args);

			// error in prediction
			errorArgs = predictionErrorArg.values(options);

			for (Double errorArg : errorArgs) {
				if (errorArg.doubleValue() <= -1.0) {
					throw new Exception(
							"Error can't be less than or equals -1.0");
				}
			}

			// migration control to use - optional
			if (options.has(migrationArg)) {
				String value = migrationArg.value(options);

				if (value.equals("ifchange")) {
					migrationToUse = MigrationControlToUse.MIGRATE_IF_CHANGE;
				} else if (value.equals("ifoverload")) {
					migrationToUse = MigrationControlToUse.MIGRATE_IF_CHANGE_AND_SERVER_BECOMES_OVERLOADED;
				} else if (value.equals("khanna")) {
					migrationToUse = MigrationControlToUse.KHANNA;
				} else if (value.equals("all")) {
					migrationToUse = null;
				}
			}

			// placement strategy to use - optional
			if (options.has(placementArg)) {
				String value = placementArg.value(options);

				if (value.equals("ffd")) {
					placementToUse = PlacementType.FIRST_FIT;
				} else if (value.equals("bfd")) {
					placementToUse = PlacementType.BEST_FIT;
				} else if (value.equals("wfd")) {
					placementToUse = PlacementType.WORST_FIT;
				} else if (value.equals("khanna")) {
					placementToUse = PlacementType.KHANNA;
				} else if (value.equals("all")) {
					placementToUse = null;
				}
			}

			// environment to use -- optional
			if (options.has("g")) {
				environmentToUse = EnvironmentToUse.GOOGLE;
			} else {
				environmentToUse = EnvironmentToUse.HOMOGENEOUS;
			}

			// log -- optional
			if (options.has("l")) {
				shouldLog = true;
			}

			// max number of threads -- optional
			if (options.has(numberOfThreadsArg)) {
				Integer value = numberOfThreadsArg.value(options);

				if (value.intValue() < 1) {
					throw new Exception(
							"Couldn't execute with at least 1 thread");
				}

				ExecutionQueue.getInstance().setMaxExecutions(value.intValue());
			}

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return;
		}

		for (Double errorArg : errorArgs) {

			for (MigrationControlToUse migration : MigrationControlToUse
					.values()) {

				if (migrationToUse != null) {
					if (migrationToUse != migration) {
						continue;
					}
				}
				if (migration == MigrationControlToUse.NONE) {
					continue;
				}

				for (PlacementType placement : PlacementType.values()) {

					try {
						if (placementToUse != null
								&& placement != placementToUse) {
							continue;
						}

						if (migration == MigrationControlToUse.KHANNA) {
							if (placement != PlacementType.KHANNA) {
								continue;
							}
						} else {
							if (placement == PlacementType.KHANNA) {
								continue;
							}
						}

						ExecutionBuilder executionBuilder = new ExecutionBuilder();

						executionBuilder.setShouldLog(shouldLog);

						executionBuilder.setEnvironment(environmentToUse);

						executionBuilder
								.setForecastingModule(ForecasterToUse.WORKLOAD_ERROR_INJECTOR);

						executionBuilder.setPlacementModule(placement);

						executionBuilder
								.setStatisticsModule(StatisticsType.MIGRATION_STATISTICS);

						executionBuilder
								.setWorkload(WorkloadToUse.GOOGLE_TRACE_FILE_2);

						executionBuilder
								.setMeasurementModule(MeasurementToUse.WORKLOAD);

						executionBuilder.setMigrationController(migration);

						executionBuilder.setParameter(
								Constants.PARAMETER_ENVIRONMENT_MULTIPLIER,
								new Double(1));

						double meanError, variation;

						// command line arguments
						meanError = errorArg.doubleValue();
						variation = 0;

						executionBuilder.setParameter(
								Constants.PARAMETER_FORECASTING_MEAN_ERROR,
								new Double(meanError));
						executionBuilder.setParameter(
								Constants.PARAMETER_FORECASTING_VARIATION,
								new Double(variation));

						executionBuilder
								.setParameter(
										Constants.PARAMETER_STATISTICS_EXECUTION_IDENTIFIER,
										String.format("%.2f", errorArg));

						// execution.setParameter(Constants.PARAMETER_FORECASTING_MEASUREMENT_WINDOW,
						// new Integer(2));

						// new Thread(execution).start();

						ExecutionQueue.getInstance().addExecution(
								executionBuilder.build());

					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}

	}

}