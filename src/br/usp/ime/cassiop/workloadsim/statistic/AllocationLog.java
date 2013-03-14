package br.usp.ime.cassiop.workloadsim.statistic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.StatisticsModule;
import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.Server;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class AllocationLog extends StatisticsModule {

	final Logger logger = LoggerFactory.getLogger(AllocationLog.class);

	private static final String DELIMITER = "\t";
	private static final String NEW_LINE = "\n";

	@Override
	public void setParameters(Map<String, Object> parameters) throws Exception {
		super.setParameters(parameters);

		Object o = null;

		o = parameters.get(Constants.PARAMETER_LOG_PATH);
		if (o instanceof File) {
			setStatisticsFile((File) o);

			if (!statisticsFile.isDirectory() || !statisticsFile.canWrite()) {
				throw new Exception(String.format(
						"%s should be a writtable directory.",
						statisticsFile.getCanonicalPath()));
			}

		} else {
			throw new Exception(String.format("Invalid parameter: %s",
					Constants.PARAMETER_LOG_PATH));
		}
	}

	public void initialize() throws Exception {
		super.initialize();

		if (statisticsFile == null) {
			statisticsFile = new File("res/log/");
		}
	}

	@Override
	public void generateStatistics(long currentTime) throws Exception {

		Collection<Server> servers = virtualizationManager
				.getActiveServersList();

		int serversUsed = servers.size();

		StringBuilder sb = new StringBuilder();

		sb.append(currentTime).append(DELIMITER);
		sb.append(serversUsed).append(DELIMITER);

		for (Server server : servers) {
			sb.append(NEW_LINE);
			sb.append(
					String.format("%s(%f,%f)", server.getName(),
							server.getCapacity(ResourceType.CPU),
							server.getCapacity(ResourceType.MEMORY))).append(
					DELIMITER);

			sb.append(server.getVirtualMachines().size()).append(DELIMITER);

			for (VirtualMachine vm : server.getVirtualMachines()) {
				sb.append(
						String.format("%s(%f,%f)", vm.getName(),
								vm.getDemand(ResourceType.CPU),
								vm.getDemand(ResourceType.MEMORY))).append(
						DELIMITER);
			}
		}

		// try (BufferedWriter writer = Files.newBufferedWriter(statisticsFile,
		// charset, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
		try {
			File currentFile = new File(statisticsFile.getAbsolutePath()
					.concat(String.format("/log_%d.out", currentTime)));

			BufferedWriter writer = new BufferedWriter(new FileWriter(
					currentFile, false));

			writer.write(sb.toString());

			writer.flush();
			writer.close();
		} catch (IOException x) {
			System.err.format("IOException: %s%n", x);
		}

	}
}
