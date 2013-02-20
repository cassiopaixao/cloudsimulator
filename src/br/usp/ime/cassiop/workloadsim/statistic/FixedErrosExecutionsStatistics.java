package br.usp.ime.cassiop.workloadsim.statistic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import br.usp.ime.cassiop.workloadsim.Measurement;
import br.usp.ime.cassiop.workloadsim.StatisticsModule;
import br.usp.ime.cassiop.workloadsim.Workload;
import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;
import br.usp.ime.cassiop.workloadsim.util.MathUtils;

public class FixedErrosExecutionsStatistics extends StatisticsModule {

	private static final String DELIMITER = "\t";
	private static final String NEW_LINE = "\n";

	private Collection<Server> physicalMachines = null;

	private Measurement measurement = null;

	private File statisticsPMsFile = null;
	private File statisticsSlaFile = null;

	private Workload workload = null;

	public void setWorkload(Workload workload) {
		this.workload = workload;
	}

	private String executionIdentifier = null;

	@Override
	public void setParameters(Map<String, Object> parameters) throws Exception {
		Object o = null;
		o = parameters.get(Constants.PARAMETER_WORKLOAD);
		if (o instanceof Workload) {
			setWorkload((Workload) o);
		} else {
			throw new Exception(String.format("Invalid parameter: %s",
					Constants.PARAMETER_WORKLOAD));
		}

		o = parameters.get(Constants.PARAMETER_STATISTICS_EXECUTION_IDENTIFIER);
		if (o instanceof String) {
			setExecutionIdentifier((String) o);
		} else {
			throw new Exception(String.format("Invalid parameter: %s",
					Constants.PARAMETER_STATISTICS_EXECUTION_IDENTIFIER));
		}

		super.setParameters(parameters);
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

		if (executionIdentifier == null) {
			executionIdentifier = Calendar.getInstance().getTime().toString();
		}

		long time, initialTime, timeInterval, lastTime;

		initialTime = workload.getInitialTime();
		timeInterval = workload.getTimeInterval();
		lastTime = workload.getLastTime();

		StringBuilder sb = new StringBuilder();
		sb.append("Execution").append(DELIMITER);
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
		if (currentTime == workload.getInitialTime()) {
			sb.append(NEW_LINE);
			sb.append(executionIdentifier).append(DELIMITER);
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
		if (currentTime == workload.getInitialTime()) {
			sb.append(NEW_LINE);
			sb.append(executionIdentifier).append(DELIMITER);
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

	public String getExecutionIdentifier() {
		return executionIdentifier;
	}

	public void setExecutionIdentifier(String executionIdentifier) {
		this.executionIdentifier = executionIdentifier;
	}
}
