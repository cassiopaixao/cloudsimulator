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

		long start, finish, interval;
		start = Calendar.getInstance().getTime().getTime();

		ExecutionConfiguration execution = ExecutionBuilder.build(
				EnvironmentToUse.GOOGLE,
				ForecasterToUse.WORKLOAD_ERROR_INJECTOR,
				PlacementType.BEST_FIT, StatisticsType.FIXED_ERRORS,
				WorkloadToUse.GOOGLE_TRACE_2);

		for (int i = 0; i < 9; i++) { // 0.8 to 1.2 fixed
			// for (int i = 4; i < 5; i++) { // 1.0 fixed
			// for (int i = 0; i < 10; i++) { // 20 times randomized
			try {
				double meanError, variation;

				// -0.8 a 1.2 fixo
				meanError = -0.2 + i * 0.05;
				variation = 0;

				// 0.8 a 1.2 aleatorio
				// meanError = 0;
				// variation = 0.2;

				configureError(execution, meanError, variation);

				execution.setParameter(
						Constants.PARAMETER_STATISTICS_EXECUTION_IDENTIFIER,
						String.format("%d", -20 + i * 5));

				execution.run();

				logger.info("Simulation {} for error {} ~ {} done.", i, 1
						+ meanError - variation, 1 + meanError + variation);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
		finish = Calendar.getInstance().getTime().getTime();
		interval = finish - start;

		logger.info("Execution time: {}", interval);

	}

	private static void configureError(ExecutionConfiguration execution,
			double meanError, double variation) {
		execution.setParameter(Constants.PARAMETER_FORECASTING_MEAN_ERROR,
				new Double(meanError));
		execution.setParameter(Constants.PARAMETER_FORECASTING_VARIATION,
				new Double(variation));

	}
}