package br.usp.ime.cassiop.workloadsim;

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

		double errorArg = 0.0;

		MigrationControlToUse migrationToUse = MigrationControlToUse.NONE;

		try {
			// error in prediction - mandatory
			if (args.length == 0) {
				throw new Exception(
						"Should specify the error in prediction.\nEx.: -0.10");
			}
			errorArg = Double.parseDouble(args[0]);

			if (errorArg <= -1.0) {
				throw new Exception("Error couldn't be less than -1.0");
			}

			// migration control to use - optional
			if (args.length > 1) {
				if (args[1].equals("ifchange")) {
					migrationToUse = MigrationControlToUse.MIGRATE_IF_CHANGE;
				} else if (args[1].equals("ifoverload")) {
					migrationToUse = MigrationControlToUse.MIGRATE_IF_CHANGE_AND_SERVER_BECOMES_OVERLOADED;
				} else if (args[1].equals("khanna")) {
					migrationToUse = MigrationControlToUse.KHANNA;
				}
			}

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return;
		}

		// for (EnvironmentToUse environment : EnvironmentToUse.values()) {
		for (PlacementType placement : PlacementType.values()) {
			// for (MigrationControlToUse migrationToUse : MigrationControlToUse
			// .values()) {

			// for (int i = 0; i < 9; i++) { // 0.8 to 1.2 fixed
			// for (int i = 2; i < 7; i += 2) { // 0.9, 1.0, 1.1
			// for (int i = 2; i < 7; i += 4) { // 0.9, 1.1
			// for (int i = 2; i < 7; i++) { // 0.9, 0.95 1.0, 1.05 1.1
			// for (int i = 6; i < 7; i++) { // 1.1 fixed
			// for (int i = 4; i < 5; i++) { // 1.0 fixed
			// for (int i = 2; i < 3; i++) { // 0.9 fixed
			// for (int i = 0; i < 5; i++) { // 20 times randomized

			try {

				if (migrationToUse == MigrationControlToUse.KHANNA) {
					if (placement != PlacementType.KHANNA) {
						continue;
					}
				} else {
					if (placement == PlacementType.KHANNA) {
						continue;
					}
				}

				ExecutionBuilder executionBuilder = new ExecutionBuilder();

				// executionBuilder.setEnvironment(environment);
				executionBuilder.setEnvironment(EnvironmentToUse.GOOGLE);

				executionBuilder
						.setForecastingModule(ForecasterToUse.WORKLOAD_ERROR_INJECTOR);

				executionBuilder.setPlacementModule(placement);

				executionBuilder
						.setStatisticsModule(StatisticsType.MIGRATION_STATISTICS);

				executionBuilder.setWorkload(WorkloadToUse.GOOGLE_TRACE_FILE_2);

				executionBuilder
						.setMeasurementModule(MeasurementToUse.WORKLOAD);

				executionBuilder.setMigrationController(migrationToUse);

				ExecutionConfiguration execution = executionBuilder.build();

				execution.setParameter(
						Constants.PARAMETER_ENVIRONMENT_MULTIPLIER, new Double(
								1));

				double meanError, variation;

				// command line arguments
				meanError = errorArg;
				variation = 0;

				// 0.8 a 1.2 fixo
				// meanError = -0.2 + i * 0.05;
				// variation = 0;

				// 0.97 a 1.03 fixo
				// meanError = -0.03 + i * 0.01;
				// variation = 0;

				// 0.992 a 0.998 fixo
				// meanError = -0.008 + i * 0.002;
				// variation = 0;

				// // 0.999 a 1.00 fixo
				// meanError = -0.001 + i * 0.00025;
				// variation = 0;

				// 0.8 a 1.2 aleatorio
				// meanError = 0;
				// variation = 0.2;

				configureError(execution, meanError, variation);

				execution.setParameter(
						Constants.PARAMETER_STATISTICS_EXECUTION_IDENTIFIER,
						String.format("(%.2f)", errorArg));

				execution.addToFileName(String.format("(%.2f)", errorArg));

				// execution.setParameter(Constants.PARAMETER_FORECASTING_MEASUREMENT_WINDOW,
				// new Integer(2));

				// execution.setParameter(
				// Constants.PARAMETER_STATISTICS_EXECUTION_IDENTIFIER,
				// String.format("%d", -3 + i));

				// execution
				// .setParameter(
				// Constants.PARAMETER_STATISTICS_EXECUTION_IDENTIFIER,
				// String.format("%.2f", -0.8 + i * 0.2));

				// execution
				// .setParameter(
				// Constants.PARAMETER_STATISTICS_EXECUTION_IDENTIFIER,
				// String.format("%.3f", -0.1 + i * 0.025));

				new Thread(execution).start();

			} catch (Exception ex) {
				ex.printStackTrace();
			}
			// }
			// }
		}

	}

	private static void configureError(ExecutionConfiguration execution,
			double meanError, double variation) {
		execution.setParameter(Constants.PARAMETER_FORECASTING_MEAN_ERROR,
				new Double(meanError));
		execution.setParameter(Constants.PARAMETER_FORECASTING_VARIATION,
				new Double(variation));

	}
}