package br.usp.ime.cassiop.workloadsim;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
import br.usp.ime.cassiop.workloadsim.ExecutionBuilder.PowerOffStrategyToUse;
import br.usp.ime.cassiop.workloadsim.ExecutionBuilder.StatisticsType;
import br.usp.ime.cassiop.workloadsim.ExecutionBuilder.WorkloadToUse;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class Main {

	static final Logger logger = LoggerFactory.getLogger(Main.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Locale.setDefault(Locale.US);

		List<Double> errorArgs = null;

		boolean shouldLog = false;

		List<MigrationControlToUse> migrationsToUse = new ArrayList<MigrationControlToUse>();

		List<PlacementType> placementsToUse = new ArrayList<PlacementType>();

		List<EnvironmentToUse> environmentsToUse = new ArrayList<EnvironmentToUse>();

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
							"migration control to use (khanna, ifchange, ifoverload, if-, none, all)")
					.withRequiredArg().ofType(String.class).defaultsTo("none");

			// placement strategy to use
			OptionSpec<String> placementArg = parser
					.accepts("p",
							"placement strategy to use (khanna, ffd, bfd, wfd, awf, awfd, all)")
					.withRequiredArg().ofType(String.class).defaultsTo("all");

			// environment
			OptionSpec<String> environmentArgs = parser
					.accepts("c",
							"cluster (environment) to use (homogeneous, google)")
					.withRequiredArg().ofType(String.class)
					.defaultsTo("homogeneous");

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
				for (String value : migrationArg.values(options)) {
					if (value.equals("ifchange")) {
						migrationsToUse
								.add(MigrationControlToUse.MIGRATE_IF_CHANGE);
					} else if (value.equals("ifoverload")) {
						migrationsToUse
								.add(MigrationControlToUse.MIGRATE_IF_CHANGE_AND_SERVER_BECOMES_OVERLOADED);
					} else if (value.equals("if-")) {
						migrationsToUse
								.add(MigrationControlToUse.MIGRATE_IF_CHANGE);
						migrationsToUse
								.add(MigrationControlToUse.MIGRATE_IF_CHANGE_AND_SERVER_BECOMES_OVERLOADED);
					} else if (value.equals("khanna")) {
						migrationsToUse.add(MigrationControlToUse.KHANNA);
					} else if (value.equals("none")) {
						migrationsToUse.add(MigrationControlToUse.NONE);
					} else if (value.equals("all")) {
						migrationsToUse.clear();
						for (MigrationControlToUse migration : MigrationControlToUse
								.values()) {
							migrationsToUse.add(migration);
						}
					}
				}
			}

			// placement strategy to use - optional
			if (options.has(placementArg)) {
				for (String value : placementArg.values(options)) {

					if (value.equals("ffd")) {
						placementsToUse.add(PlacementType.FIRST_FIT);
					} else if (value.equals("bfd")) {
						placementsToUse.add(PlacementType.BEST_FIT);
					} else if (value.equals("wfd")) {
						placementsToUse.add(PlacementType.WORST_FIT);
					} else if (value.equals("khanna")) {
						placementsToUse.add(PlacementType.KHANNA);
					} else if (value.equals("awf")) {
						placementsToUse.add(PlacementType.ALMOST_WORST_FIT);
					} else if (value.equals("awfd")) {
						placementsToUse.add(PlacementType.ALMOST_WORST_FIT_DEC);
					} else if (value.equals("all")) {
						placementsToUse = new ArrayList<PlacementType>();
						for (PlacementType placement : PlacementType.values()) {
							placementsToUse.add(placement);
						}
						break;
					}
				}
			}

			// environment to use -- optional
			if (options.has(environmentArgs)) {
				for (String value : environmentArgs.values(options)) {
					if (value.equals("homogeneous")) {
						environmentsToUse.add(EnvironmentToUse.HOMOGENEOUS);
					} else if (value.equals("google")) {
						environmentsToUse.add(EnvironmentToUse.GOOGLE);
					} else if (value.equals("test")) {
						environmentsToUse.add(EnvironmentToUse.TEST);
					}
				}
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

		for (EnvironmentToUse environmentToUse : environmentsToUse) {

			for (Double errorArg : errorArgs) {

				for (MigrationControlToUse migration : migrationsToUse) {

					for (PlacementType placement : placementsToUse) {

						try {

							if (migration == MigrationControlToUse.KHANNA
									|| placement == PlacementType.KHANNA) {
								if ((migration != MigrationControlToUse.KHANNA)
										|| (placement != PlacementType.KHANNA)) {
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

							if (environmentToUse == EnvironmentToUse.TEST) {
								executionBuilder
										.setWorkload(WorkloadToUse.TEST_TRACE);
							} else {
								executionBuilder
										.setWorkload(WorkloadToUse.GOOGLE_TRACE_FILE_2);
							}

							executionBuilder
									.setMeasurementModule(MeasurementToUse.WORKLOAD);

							executionBuilder.setMigrationController(migration);

							if (placement == PlacementType.KHANNA) {
								executionBuilder
										.setPowerOffStrategy(PowerOffStrategyToUse.LOW_UTILIZATION);
							} else {
								executionBuilder
										.setPowerOffStrategy(PowerOffStrategyToUse.IDLE_MACHINES);
							}

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

}