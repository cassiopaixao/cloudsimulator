package br.usp.ime.cassiop.workloadsim.statistic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import br.usp.ime.cassiop.workloadsim.Measurement;
import br.usp.ime.cassiop.workloadsim.StatisticsModule;
import br.usp.ime.cassiop.workloadsim.Workload;
import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.MathUtils;
import br.usp.ime.cassiop.workloadsim.workload.GoogleWorkload;

public class RandomizedExecutionsStatistics extends StatisticsModule {

	private static final String DELIMITER = "\t";
	private static final String NEW_LINE = "\n";

	private Collection<Server> physicalMachines = null;

	private Measurement measurement = null;

	private File statisticsPMsFile = null;
	private File statisticsSlaFile = null;

	private Workload workload = null;

	public void setStatisticsFile(File statisticsFile) {
		super.setStatisticsFile(statisticsFile);
		try {
			initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initialize() throws Exception {
		super.initialize();

		if (statisticsFile == null) {
			statisticsFile = new File("res/statistics.csv");
		}

		statisticsPMsFile = new File(statisticsFile.toString().replace(".csv",
				"Pms.csv"));
		statisticsSlaFile = new File(statisticsFile.toString().replace(".csv",
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

		// try (BufferedWriter writer =
		// Files.newBufferedWriter(statisticsPMsFile,
		// charset, StandardOpenOption.WRITE,
		// StandardOpenOption.CREATE_NEW)) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					statisticsPMsFile));
			writer.write(sb.toString());

			writer.flush();
			writer.close();
		} catch (IOException x) {
			System.err.format("IOException: %s%n", x);
		}

		// try (BufferedWriter writer =
		// Files.newBufferedWriter(statisticsSlaFile,
		// charset, StandardOpenOption.WRITE,
		// StandardOpenOption.CREATE_NEW)) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					statisticsSlaFile, true));
			writer.write(sb.toString());

			writer.flush();
			writer.close();
		} catch (IOException x) {
			System.err.format("IOException: %s%n", x);
		}

	}

	@Override
	public void generateStatistics(long currentTime) throws Exception {
		physicalMachines = virtualizationManager.getActiveServerList();

		measurement = measurementModule.measureSystem(currentTime);

		long slaViolations = 0;
		long pmsUsed = 0;

		for (Server pm : physicalMachines) {
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

		// try (BufferedWriter writer =
		// Files.newBufferedWriter(statisticsPMsFile,
		// charset, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					statisticsPMsFile, true));
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

		// try (BufferedWriter writer =
		// Files.newBufferedWriter(statisticsSlaFile,
		// charset, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					statisticsSlaFile, true));
			writer.write(sb.toString());

			writer.flush();
			writer.close();
		} catch (IOException x) {
			System.err.format("IOException: %s%n", x);
		}
	}

	private boolean isPmOverloaded(Server pm) {
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

	private boolean isPmActive(Server pm) {
		return !pm.getVirtualMachines().isEmpty();
	}
}
