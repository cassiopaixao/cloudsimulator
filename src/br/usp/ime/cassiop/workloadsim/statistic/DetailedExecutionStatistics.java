package br.usp.ime.cassiop.workloadsim.statistic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.usp.ime.cassiop.workloadsim.Measurement;
import br.usp.ime.cassiop.workloadsim.StatisticsModule;
import br.usp.ime.cassiop.workloadsim.environment.MachineStatus;
import br.usp.ime.cassiop.workloadsim.exceptions.InvalidParameterException;
import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;
import br.usp.ime.cassiop.workloadsim.util.MathUtils;

public class DetailedExecutionStatistics extends StatisticsModule {

	private static final String DELIMITER = "\t";
	private static final String NEW_LINE = "\n";

	private Collection<Server> physicalMachines = null;

	private Measurement measurement = null;

	private File statisticsPMsFile = null;
	private File statisticsSlaFile = null;

	private String executionIdentifier = null;

	private List<Server> machineTypes = null;

	@Override
	public void setParameters(Map<String, Object> parameters)
			throws InvalidParameterException {
		super.setParameters(parameters);
		
		Object o = null;

		o = parameters.get(Constants.PARAMETER_STATISTICS_EXECUTION_IDENTIFIER);
		if (o instanceof String) {
			setExecutionIdentifier((String) o);
		} else {
			throw new InvalidParameterException(
					Constants.PARAMETER_STATISTICS_EXECUTION_IDENTIFIER,
					String.class);
		}
	}

	public void initialize() throws Exception {
		super.initialize();

		if (statisticsFile == null) {
			statisticsFile = new File("res/statistics.csv");
		}

		if (executionIdentifier == null) {
			executionIdentifier = Calendar.getInstance().getTime().toString();
		}

		statisticsPMsFile = new File(statisticsFile.toString().replace(".csv",
				"Pms.csv"));
		statisticsSlaFile = new File(statisticsFile.toString().replace(".csv",
				"Sla.csv"));

		Map<Server, MachineStatus> environmentStatus = virtualizationManager
				.getEnvironment().getEnvironmentStatus();

		machineTypes = new ArrayList<Server>(environmentStatus.keySet());

		Collections.sort(machineTypes);
		Collections.reverse(machineTypes);

		StringBuilder sb = new StringBuilder();
		sb.append(executionIdentifier).append(DELIMITER);
		for (Server pm : machineTypes) {
			sb.append(environmentStatus.get(pm).getAvailable()).append(
					DELIMITER);
		}

		sb.append(NEW_LINE);
		sb.append("MachineType").append(DELIMITER);
		for (Server pm : machineTypes) {
			sb.append(pm.getType().replaceAll(DELIMITER, "_"))
					.append(DELIMITER);
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
					statisticsSlaFile));
			writer.write(sb.toString());

			writer.flush();
			writer.close();
		} catch (IOException x) {
			System.err.format("IOException: %s%n", x);
		}
	}

	@Override
	public void generateStatistics(long currentTime) throws Exception {
		physicalMachines = virtualizationManager.getActiveServersList();

		measurement = measurementModule.measureSystem(currentTime);

		Map<String, Integer> slaViolations = new HashMap<String, Integer>();
		for (Server pm : machineTypes) {
			slaViolations.put(pm.getType(), new Integer(0));
		}

		Map<Server, MachineStatus> pmsUsed = virtualizationManager
				.getEnvironment().getEnvironmentStatus();

		for (Server pm : physicalMachines) {
			if (isPmOverloaded(pm)) {
				slaViolations.put(pm.getType(), slaViolations.get(pm.getType())
						.intValue() + pm.getVirtualMachines().size());
			}
		}

		// write statistics
		if (statisticsFile == null) {
			initialize();
		}

		// PMs used
		StringBuilder sb = new StringBuilder();
		sb.append(NEW_LINE);
		sb.append(currentTime).append(DELIMITER);
		for (Server pm : machineTypes) {
			sb.append(pmsUsed.get(pm).getUsed()).append(DELIMITER);
		}

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
		sb.append(NEW_LINE);
		sb.append(currentTime).append(DELIMITER);
		for (Server pm : machineTypes) {
			sb.append(slaViolations.get(pm.getType())).append(DELIMITER);
		}

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

	public String getExecutionIdentifier() {
		return executionIdentifier;
	}

	public void setExecutionIdentifier(String executionIdentifier) {
		this.executionIdentifier = executionIdentifier;
	}
}
