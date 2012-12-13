package br.usp.ime.cassiop.workloadsim;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.ExecutionBuilder.EnvironmentToUse;
import br.usp.ime.cassiop.workloadsim.ExecutionBuilder.ForecasterToUse;
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

		long start, finish, interval, start_execution;
		start = Calendar.getInstance().getTime().getTime();

		for (PlacementType placement : PlacementType.values()) {

			ExecutionConfiguration execution = ExecutionBuilder.build(
					EnvironmentToUse.IDEAL,
					ForecasterToUse.WORKLOAD_ERROR_INJECTOR, placement,
					StatisticsType.FIXED_ERRORS,
					WorkloadToUse.GOOGLE_TRACE_FILE_2);

//			 for (int i = 0; i < 9; i++) { // 0.8 to 1.2 fixed
			for (int i = 2; i < 7; i+= 2) { // 0.9, 1.0, 1.1
			// for (int i = 0; i < 7; i++) { // 0.97 to 1.03 fixed
			// for (int i = 0; i < 4; i++) { // 0.992 to 0.998 fixed
			// for (int i = 0; i < 5; i++) { // 0.9990 to 1.0000 fixed
			// for (int i = 4; i < 5; i++) { // 1.0 fixed
//			for (int i = 2; i < 3; i++) { // 0.9 fixed
				// for (int i = 0; i < 5; i++) { // 20 times randomized

				try {
					start_execution = Calendar.getInstance().getTime()
							.getTime();

					execution.setParameter(
							Constants.PARAMETER_ENVIRONMENT_MULTIPLIER,
							new Double(1));

					double meanError, variation;

					// 0.8 a 1.2 fixo
					meanError = -0.2 + i * 0.05;
					variation = 0;

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

					execution
							.setParameter(
									Constants.PARAMETER_STATISTICS_EXECUTION_IDENTIFIER,
									String.format("%d", -20 + i * 5));

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

					logger.info("Starting simulation {} for error {} ~ {}.", i,
							1 + meanError - variation, 1 + meanError
									+ variation);

					execution.run();

					logger.info("Simulation {} for error {} ~ {} done.", i, 1
							+ meanError - variation, 1 + meanError + variation);

					finish = Calendar.getInstance().getTime().getTime();
					interval = finish - start_execution;

					logger.info("Execution time: {}s", interval / 1000);

				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}
		}
		finish = Calendar.getInstance().getTime().getTime();
		interval = finish - start;

		logger.info("Total execution time: {}", interval / 1000);

	}

	private static void configureError(ExecutionConfiguration execution,
			double meanError, double variation) {
		execution.setParameter(Constants.PARAMETER_FORECASTING_MEAN_ERROR,
				new Double(meanError));
		execution.setParameter(Constants.PARAMETER_FORECASTING_VARIATION,
				new Double(variation));

	}
}