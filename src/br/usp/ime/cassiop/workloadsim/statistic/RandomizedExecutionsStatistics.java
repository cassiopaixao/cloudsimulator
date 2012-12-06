package br.usp.ime.cassiop.workloadsim.statistic;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;
import java.util.List;

import br.usp.ime.cassiop.workloadsim.Measurement;
import br.usp.ime.cassiop.workloadsim.StatisticsModule;
import br.usp.ime.cassiop.workloadsim.Workload;
import br.usp.ime.cassiop.workloadsim.model.PhysicalMachine;
import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.MathUtils;
import br.usp.ime.cassiop.workloadsim.workload.GoogleWorkload;

public class RandomizedExecutionsStatistics extends StatisticsModule {

	private static final String DELIMITER = "\t";
	private static final String NEW_LINE = "\n";

	private List<PhysicalMachine> physicalMachines = null;

	private Measurement measurement = null;

	private Path statisticsPMsFile = null;
	private Path statisticsSlaFile = null;

	private Workload workload = null;

	public void setStatisticsFile(Path statisticsFile) {
		super.setStatisticsFile(statisticsFile);
		try {
			initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initialize() throws Exception {
		if (statisticsFile == null) {
			statisticsFile = Paths.get("res/statistics.csv");
		}

		statisticsPMsFile = Paths.get(statisticsFile.toString().replace(".csv",
				"Pms.csv"));
		statisticsSlaFile = Paths.get(statisticsFile.toString().replace(".csv",
				"Sla.csv"));

		long time, initialTime, timeInterval, lastTime;
		workload = GoogleWorkload.build();

		initialTime = workload.getInitialTime();
		timeInterval = workload.getTimeInterval();
		lastTime = workload.getLastTime();

		StringBuilder sb = new StringBuilder();
		sb.append("predictionTime").append(DELIMITER);
		for (time = initialTime; time <= lastTime; time += timeInterval) {
			sb.append(time).append(DELIMITER);
		}

		try (BufferedWriter writer = Files.newBufferedWriter(statisticsPMsFile,
				charset, StandardOpenOption.WRITE,
				StandardOpenOption.CREATE_NEW)) {
			writer.write(sb.toString());

			writer.flush();
			writer.close();
		} catch (IOException x) {
			System.err.format("IOException: %s%n", x);
		}

		try (BufferedWriter writer = Files.newBufferedWriter(statisticsSlaFile,
				charset, StandardOpenOption.WRITE,
				StandardOpenOption.CREATE_NEW)) {
			writer.write(sb.toString());

			writer.flush();
			writer.close();
		} catch (IOException x) {
			System.err.format("IOException: %s%n", x);
		}

	}

	@Override
	public void generateStatistics(long currentTime) throws Exception {
		physicalMachines = virtualizationManager.getActivePmList();

		measurement = measurementModule.measureSystem(currentTime);

		long slaViolations = 0;
		long pmsUsed = 0;

		for (PhysicalMachine pm : physicalMachines) {
			if (isPmActive(pm)) {
				pmsUsed++;
			}
			if (isPmOverloaded(pm)) {

				// TODO confirm how to count broken sla restrictions
				// by now, each vm violation corresponds to one agreement
				// violation
				slaViolations += pm.getVirtualMachines().size();

			}
		}

		// write statistics
		if (statisticsFile == null) {
			initialize();
		}

		// PMs used

		StringBuilder sb = new StringBuilder();
		if (currentTime == workload.getInitialTime()
				+ workload.getTimeInterval()) {
			sb.append(NEW_LINE);
			sb.append(Calendar.getInstance().getTimeInMillis()).append(
					DELIMITER);
		}
		sb.append(pmsUsed).append(DELIMITER);

		try (BufferedWriter writer = Files.newBufferedWriter(statisticsPMsFile,
				charset, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
			writer.write(sb.toString());

			writer.flush();
			writer.close();
		} catch (IOException x) {
			System.err.format("IOException: %s%n", x);
		}

		// SLA Violations

		sb = new StringBuilder();
		if (currentTime == workload.getInitialTime()
				+ workload.getTimeInterval()) {
			sb.append(NEW_LINE);
			sb.append(Calendar.getInstance().getTimeInMillis()).append(
					DELIMITER);
		}
		sb.append(slaViolations).append(DELIMITER);

		try (BufferedWriter writer = Files.newBufferedWriter(statisticsSlaFile,
				charset, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
			writer.write(sb.toString());

			writer.flush();
			writer.close();
		} catch (IOException x) {
			System.err.format("IOException: %s%n", x);
		}
	}

	private boolean isPmOverloaded(PhysicalMachine pm) {
		List<VirtualMachine> vms = pm.getVirtualMachines();

		double sum;
		for (ResourceType type : ResourceType.values()) {
			sum = 0;
			for (VirtualMachine vm : vms) {
				sum += measurement.getVmActualDemand(vm, type);
			}
			if (MathUtils.greaterThan(sum, pm.getCapacity(type))) {
				return true;
			}
		}
		return false;
	}

	private boolean isPmActive(PhysicalMachine pm) {
		return !pm.getVirtualMachines().isEmpty();
	}
}
