package br.usp.ime.cassiop.workloadsim.statistic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.Measurement;
import br.usp.ime.cassiop.workloadsim.StatisticsModule;
import br.usp.ime.cassiop.workloadsim.environment.MachineStatus;
import br.usp.ime.cassiop.workloadsim.exceptions.ForcedEndException;
import br.usp.ime.cassiop.workloadsim.exceptions.InvalidParameterException;
import br.usp.ime.cassiop.workloadsim.exceptions.ServerOverloadedException;
import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;
import br.usp.ime.cassiop.workloadsim.util.MathUtils;

public class MigrationStatistics extends StatisticsModule {

	final Logger logger = LoggerFactory.getLogger(MigrationStatistics.class);

	private static final String DELIMITER = "\t";
	private static final String NEW_LINE = "\n";

	private Collection<Server> servers = null;

	private Measurement measurement = null;

	private String executionIdentifier = null;

	private List<String> statisticsFields = null;

	private List<Server> machineTypes = null;

	private Map<String, Object> parameters = null;
	
	private File headerFile = null;

	@Override
	public void setParameters(Map<String, Object> parameters)
			throws InvalidParameterException {
		super.setParameters(parameters);

		Object o = null;

		o = parameters.get(Constants.PARAMETER_STATISTICS_EXECUTION_IDENTIFIER);
		if (o instanceof String) {
			setExecutionIdentifier((String) o);
		} else {
			setExecutionIdentifier("");
			logger.debug("Parameter {} is not set. Using default {}",
					Constants.PARAMETER_STATISTICS_EXECUTION_IDENTIFIER, "");
		}
		o = parameters.get(Constants.PARAMETER_STATISTICS_HEADER_FILE);
		if (o instanceof File) {
			setHeaderFile((File) o);
		}

		this.parameters = parameters;
	}

	private void setHeaderFile(File headerFile) {
		this.headerFile = headerFile;
	}
	
	private void fillExecutionParameters(Map<String, Object> parameters) {

		try {
			statistics
					.put(Constants.STATISTIC_PARAM_EXECUTION_IDENTIFIER,
							parameters
									.get(Constants.PARAMETER_STATISTICS_EXECUTION_IDENTIFIER)
									.toString());
		} catch (NullPointerException e) {
			statistics.put(Constants.STATISTIC_PARAM_EXECUTION_IDENTIFIER, "");
		}
		try {
			statistics.put(Constants.STATISTIC_PARAM_WORKLOAD,
					parameters.get(Constants.PARAMETER_WORKLOAD).getClass()
							.getSimpleName());
		} catch (NullPointerException e) {
			statistics.put(Constants.STATISTIC_PARAM_WORKLOAD, "");
		}
		try {
			statistics.put(Constants.STATISTIC_PARAM_ENVIRONMENT, parameters
					.get(Constants.PARAMETER_ENVIRONMENT).getClass()
					.getSimpleName());
		} catch (NullPointerException e) {
			statistics.put(Constants.STATISTIC_PARAM_ENVIRONMENT, "");
		}
		try {
			statistics.put(Constants.STATISTIC_PARAM_ENVIRONMENT_MULTIPLIER,
					parameters.get(Constants.PARAMETER_ENVIRONMENT_MULTIPLIER)
							.toString());
		} catch (NullPointerException e) {
			statistics
					.put(Constants.STATISTIC_PARAM_ENVIRONMENT_MULTIPLIER, "");
		}
		try {
			statistics.put(Constants.STATISTIC_PARAM_MIGRATION_CONTROL,
					parameters.get(Constants.PARAMETER_MIGRATION_CONTROLLER)
							.getClass().getSimpleName());
		} catch (NullPointerException e) {
			statistics.put(Constants.STATISTIC_PARAM_MIGRATION_CONTROL, "");
		}
		try {
			statistics.put(Constants.STATISTIC_PARAM_PLACEMENT_MODULE,
					parameters.get(Constants.PARAMETER_PLACEMENT_MODULE)
							.getClass().getSimpleName());
		} catch (NullPointerException e) {
			statistics.put(Constants.STATISTIC_PARAM_PLACEMENT_MODULE, "");
		}
		try {
			statistics.put(Constants.STATISTIC_PARAM_PLACEMENT_STRATEGY,
					parameters.get(Constants.PARAMETER_PLACEMENT_STRATEGY)
							.getClass().getSimpleName());
		} catch (NullPointerException e) {
			statistics.put(Constants.STATISTIC_PARAM_PLACEMENT_STRATEGY, "");
		}
		try {
			statistics.put(Constants.STATISTIC_PARAM_FORECASTING_STRATEGY,
					parameters.get(Constants.PARAMETER_FORECASTING_MODULE)
							.getClass().getSimpleName());
		} catch (NullPointerException e) {
			statistics.put(Constants.STATISTIC_PARAM_FORECASTING_STRATEGY, "");
		}
		try {
			statistics.put(Constants.STATISTIC_PARAM_FORECASTING_ERROR,
					parameters.get(Constants.PARAMETER_FORECASTING_MEAN_ERROR)
							.toString());
		} catch (NullPointerException e) {
			statistics.put(Constants.STATISTIC_PARAM_FORECASTING_ERROR, "");
		}
		try {
			statistics.put(Constants.STATISTIC_PARAM_FORECASTING_VARIATION,
					parameters.get(Constants.PARAMETER_FORECASTING_VARIATION)
							.toString());
		} catch (NullPointerException e) {
			statistics.put(Constants.STATISTIC_PARAM_FORECASTING_VARIATION, "");
		}
		try {
			statistics.put(
					Constants.STATISTIC_PARAM_FORECASTING_MEASUREMENT_WINDOW,
					parameters.get(
							Constants.PARAMETER_FORECASTING_MEASUREMENT_WINDOW)
							.toString());
		} catch (NullPointerException e) {
			statistics.put(
					Constants.STATISTIC_PARAM_FORECASTING_MEASUREMENT_WINDOW,
					"");
		}
		try {
			statistics.put(Constants.STATISTIC_PARAM_POWER_OFF_STRATEGY,
					parameters.get(Constants.PARAMETER_POWER_OFF_STRATEGY)
							.getClass().getSimpleName());
		} catch (NullPointerException e) {
			statistics.put(Constants.STATISTIC_PARAM_POWER_OFF_STRATEGY, "");
		}
		try {
			statistics.put(
					Constants.STATISTIC_PARAM_LOW_UTILIZATION_VALUE,
					parameters.get(
							Constants.PARAMETER_POWER_OFF_LOW_UTILIZATION)
							.toString());
		} catch (NullPointerException e) {
			statistics.put(Constants.STATISTIC_PARAM_LOW_UTILIZATION_VALUE, "");
		}
	}

	public void initialize() throws Exception {
		super.initialize();

		fillExecutionParameters(parameters);

		if (statisticsFile == null) {
			statisticsFile = new File("res/statistics.csv");
		}

		if (executionIdentifier == null) {
			executionIdentifier = Calendar.getInstance().getTime().toString();
		}

		statisticsFields = new ArrayList<String>();
		statisticsFields.add(Constants.STATISTIC_PARAM_EXECUTION_IDENTIFIER);
		statisticsFields.add(Constants.STATISTIC_PARAM_WORKLOAD);
		statisticsFields.add(Constants.STATISTIC_PARAM_ENVIRONMENT);
		statisticsFields.add(Constants.STATISTIC_PARAM_ENVIRONMENT_MULTIPLIER);
		statisticsFields.add(Constants.STATISTIC_PARAM_MIGRATION_CONTROL);
		statisticsFields.add(Constants.STATISTIC_PARAM_PLACEMENT_MODULE);
		statisticsFields.add(Constants.STATISTIC_PARAM_PLACEMENT_STRATEGY);
		statisticsFields.add(Constants.STATISTIC_PARAM_FORECASTING_STRATEGY);
		statisticsFields.add(Constants.STATISTIC_PARAM_FORECASTING_ERROR);
		statisticsFields.add(Constants.STATISTIC_PARAM_FORECASTING_VARIATION);
		statisticsFields
				.add(Constants.STATISTIC_PARAM_FORECASTING_MEASUREMENT_WINDOW);
		statisticsFields.add(Constants.STATISTIC_PARAM_POWER_OFF_STRATEGY);
		statisticsFields.add(Constants.STATISTIC_PARAM_LOW_UTILIZATION_VALUE);

		statisticsFields.add(Constants.STATISTIC_SERVERS);
		statisticsFields.add(Constants.STATISTIC_USED_SERVERS);
		statisticsFields.add(Constants.STATISTIC_SERVERS_TURNED_OFF);
		statisticsFields.add(Constants.STATISTIC_OVERLOADED_SERVERS);
		statisticsFields.add(Constants.STATISTIC_SERVERS_LOAD_AVERAGE);
		statisticsFields.add(Constants.STATISTIC_SERVERS_LOAD_STD_DEV);
		statisticsFields.add(Constants.STATISTIC_SERVERS_CPU_LOAD_AVERAGE);
		statisticsFields.add(Constants.STATISTIC_SERVERS_CPU_LOAD_STD_DEV);
		statisticsFields.add(Constants.STATISTIC_SERVERS_MEM_LOAD_AVERAGE);
		statisticsFields.add(Constants.STATISTIC_SERVERS_MEM_LOAD_STD_DEV);
		statisticsFields.add(Constants.STATISTIC_VIRTUAL_MACHINES);
		statisticsFields.add(Constants.STATISTIC_NEW_VIRTUAL_MACHINES);
		statisticsFields
				.add(Constants.STATISTIC_VIRTUAL_MACHINES_TO_REALLOCATE);
		statisticsFields.add(Constants.STATISTIC_MIGRATIONS);
		statisticsFields.add(Constants.STATISTIC_MIGRATIONS_COST);
		statisticsFields.add(Constants.STATISTIC_RESIDUAL_CAPACITY);
		statisticsFields.add(Constants.STATISTIC_SLA_VIOLATIONS);
		statisticsFields
				.add(Constants.STATISTIC_VIRTUAL_MACHINES_NOT_ALLOCATED);

		Map<Server, MachineStatus> environmentStatus = virtualizationManager
				.getEnvironment().getEnvironmentStatus();

		machineTypes = new ArrayList<Server>(environmentStatus.keySet());

		Collections.sort(machineTypes);
		Collections.reverse(machineTypes);

		for (String field : statisticsFields) {
			if (!statistics.containsKey(field)) {
				setStatisticValue(field, 0);
			}
		}
		
		if (headerFile != null) {
			printHeader(headerFile, environmentStatus);
			throw new ForcedEndException("Statistics header file was printed.");
		}
	}

	public void printHeader(File headerFile, Map<Server, MachineStatus> environmentStatus) {

		StringBuilder sb = new StringBuilder();
		sb.append("time").append(DELIMITER);
		for (String field : statisticsFields) {
			sb.append(field).append(DELIMITER);
		}

		for (Server pm : machineTypes) {
			sb.append(
					String.format("%d(%.2f~%.2f)", environmentStatus.get(pm)
							.getAvailable(), pm.getCapacity(ResourceType.CPU),
							pm.getCapacity(ResourceType.MEMORY))).append(
					DELIMITER);
		}

		// removes last delimiter
		sb.setLength(sb.length()-1);
		
		// try (BufferedWriter writer = Files.newBufferedWriter(statisticsFile,
		// charset, StandardOpenOption.WRITE,
		// StandardOpenOption.CREATE_NEW)) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					headerFile));

			writer.write(sb.toString());

			writer.flush();
			writer.close();
		} catch (IOException x) {
			System.err.format("IOException: %s%n", x);
		}
	}

	@Override
	public void generateStatistics(long currentTime) throws Exception {

		measurement = measurementModule.measureSystem(currentTime);

		servers = virtualizationManager.getActiveServersList();

		statistics.put(Constants.STATISTIC_VIRTUAL_MACHINES, new Integer(
				measurement.getActualDemand().size()));

		statistics
				.put(Constants.STATISTIC_SERVERS, new Integer(servers.size()));

		Map<Server, MachineStatus> environmentStatus = virtualizationManager
				.getEnvironment().getEnvironmentStatus();

		int serversUsed = servers.size();
		int serversOverloaded = 0;
		int slaViolations = 0;
		double residualCapacity = 0;

		double servers_load[] = new double[serversUsed];
		double servers_cpu_load[] = new double[serversUsed];
		double servers_mem_load[] = new double[serversUsed];
		int servers_load_i = 0;

		for (Server server : servers) {
			if (server.getVirtualMachines().isEmpty()) {
				serversUsed--;
			} else {
				// update the vms' needs
				for (VirtualMachine vm : server.getVirtualMachines()) {
					try {
						server.updateVm(measurement.getActualDemand().get(
								vm.getName()));
					} catch (ServerOverloadedException ex) {
					}
				}

				residualCapacity += server.getResidualCapacity();

				if (isServerOverloaded(server)) {
					serversOverloaded++;
					slaViolations += server.getVirtualMachines().size();
				}
			}
			servers_load[servers_load_i] = server.getLoadPercentage();
			servers_cpu_load[servers_load_i] = (server
					.getCapacity(ResourceType.CPU) - server
					.getFreeResource(ResourceType.CPU))
					/ server.getCapacity(ResourceType.CPU);
			servers_mem_load[servers_load_i] = (server
					.getCapacity(ResourceType.MEMORY) - server
					.getFreeResource(ResourceType.MEMORY))
					/ server.getCapacity(ResourceType.MEMORY);
			servers_load_i++;
		}

		slaViolations += ((Integer) statistics
				.get(Constants.STATISTIC_VIRTUAL_MACHINES_NOT_ALLOCATED))
				.intValue();

		StandardDeviation stdDeviation = new StandardDeviation();
		Mean mean = new Mean();

		statistics.put(Constants.STATISTIC_SERVERS_LOAD_AVERAGE, new Double(
				mean.evaluate(servers_load, 0, servers_load_i)));
		statistics.put(Constants.STATISTIC_SERVERS_LOAD_STD_DEV, new Double(
				stdDeviation.evaluate(servers_load, 0, servers_load_i)));
		mean.clear();
		stdDeviation.clear();
		statistics.put(Constants.STATISTIC_SERVERS_CPU_LOAD_AVERAGE,
				new Double(mean.evaluate(servers_cpu_load, 0, servers_load_i)));
		statistics.put(
				Constants.STATISTIC_SERVERS_CPU_LOAD_STD_DEV,
				new Double(stdDeviation.evaluate(servers_cpu_load, 0,
						servers_load_i)));
		mean.clear();
		stdDeviation.clear();
		statistics.put(Constants.STATISTIC_SERVERS_MEM_LOAD_AVERAGE,
				new Double(mean.evaluate(servers_mem_load, 0, servers_load_i)));
		statistics.put(
				Constants.STATISTIC_SERVERS_MEM_LOAD_STD_DEV,
				new Double(stdDeviation.evaluate(servers_mem_load, 0,
						servers_load_i)));
		mean.clear();
		stdDeviation.clear();

		statistics.put(Constants.STATISTIC_USED_SERVERS, new Integer(
				serversUsed));
		statistics.put(Constants.STATISTIC_OVERLOADED_SERVERS, new Integer(
				serversOverloaded));
		statistics.put(Constants.STATISTIC_SLA_VIOLATIONS, new Integer(
				slaViolations));
		statistics.put(Constants.STATISTIC_RESIDUAL_CAPACITY, new Double(
				residualCapacity));

		// write statistics
		if (statisticsFile == null) {
			initialize();
		}

		StringBuilder sb = new StringBuilder();
		sb.append(NEW_LINE);
		sb.append(currentTime).append(DELIMITER);
		for (String field : statisticsFields) {
			sb.append(statistics.get(field)).append(DELIMITER);
		}

		for (Server serverType : machineTypes) {
			sb.append(environmentStatus.get(serverType).getUsed()).append(
					DELIMITER);
		}

		clearStatistics();

		// removes last delimiter
		sb.setLength(sb.length()-1);

		// try (BufferedWriter writer = Files.newBufferedWriter(statisticsFile,
		// charset, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					statisticsFile, true));

			writer.write(sb.toString());

			writer.flush();
			writer.close();
		} catch (IOException x) {
			System.err.format("IOException: %s%n", x);
		}

	}

	private boolean isServerOverloaded(Server server) {
		List<VirtualMachine> vms = server.getVirtualMachines();

		double sum;
		for (ResourceType type : ResourceType.values()) {
			sum = 0;
			for (VirtualMachine vm : vms) {
				sum += measurement.getVmActualDemand(vm, type);
			}
			if (MathUtils.greaterThan(sum, server.getCapacity(type))) {
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
