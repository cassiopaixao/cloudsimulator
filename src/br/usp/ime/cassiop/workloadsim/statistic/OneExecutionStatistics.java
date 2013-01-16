package br.usp.ime.cassiop.workloadsim.statistic;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.usp.ime.cassiop.workloadsim.Measurement;
import br.usp.ime.cassiop.workloadsim.StatisticsModule;
import br.usp.ime.cassiop.workloadsim.model.PhysicalMachine;
import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.MathUtils;

public class OneExecutionStatistics extends StatisticsModule {
	List<PhysicalMachine> physicalMachines = null;

	Measurement measurement = null;

	Map<String, Double> totalSlaViolations = null;

	public Map<String, Double> getTotalSlaViolations() {
		return totalSlaViolations;
	}

	static final String DELIMITER = "\t";
	static final String NEW_LINE = "\n";

	private double getIdleResource(PhysicalMachine pm, ResourceType type) {
		double total = pm.getCapacity(type);
		for (VirtualMachine vm : pm.getVirtualMachines()) {
			total -= vm.getDemand(type);
		}
		if (total > 0) {
			return total;
		}
		return 0;
	}

	public void setStatisticsFile(Path statisticsFile) {
		super.setStatisticsFile(statisticsFile);
		initialize();
	}

	public void initialize() {
		if (statisticsFile == null) {
			statisticsFile = Paths.get("res/statistics.csv");
		}

		totalSlaViolations = new HashMap<String, Double>();

		StringBuilder sb = new StringBuilder();
		sb.append("predictionTime").append(DELIMITER);
		sb.append("pmsUsed").append(DELIMITER);
		sb.append("pmsOverloaded").append(DELIMITER);
		sb.append("vms").append(DELIMITER);
		sb.append("slaViolations");// .append(DELIMITER);
		// sb.append("totalIdleCpu").append(DELIMITER);
		// sb.append("totalIdleMem");
		try (BufferedWriter writer = Files.newBufferedWriter(statisticsFile,
				charset, StandardOpenOption.WRITE,
				StandardOpenOption.CREATE_NEW)) {
			writer.write(sb.toString());

			writer.flush();
			writer.close();
		} catch (IOException x) {
			System.err.format("IOException: %s%n", x);
		}
	}

	private boolean isPmActive(PhysicalMachine pm) {
		return !pm.getVirtualMachines().isEmpty();
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

	public void generateStatistics(long currentTime) throws Exception {
		physicalMachines = virtualizationManager.getActivePmList();

		measurement = measurementModule.measureSystem(currentTime);

		// long intervalSinceLastPrediction = measurement.getTime()
		// - forecastingModule.getTimeOfLastPredictions();

		Map<ResourceType, Double> totalIdleResources = new HashMap<ResourceType, Double>();

		long slaViolations = 0;

		long pmsOverloaded = 0;
		long pmsUsed = 0;

		for (ResourceType type : ResourceType.values()) {
			totalIdleResources.put(type, new Double(0.0));
		}

		for (PhysicalMachine pm : physicalMachines) {
			if (isPmActive(pm)) {
				pmsUsed++;
			}
			if (isPmOverloaded(pm)) {
				pmsOverloaded++;

				// TODO confirm how to count broken sla restrictions
				// by now, each vm violation corresponds to one agreement
				// violation
				slaViolations += pm.getVirtualMachines().size();
				for (VirtualMachine vm : pm.getVirtualMachines()) {
					totalSlaViolations.put(vm.getName(), new Double(1.0));
				}

			}
			if (isPmActive(pm)) {
				for (ResourceType type : ResourceType.values()) {
					totalIdleResources.put(type, totalIdleResources.get(type)
							+ getIdleResource(pm, type));
				}
			}
		}

		// write statistics
		// predictionTime vms pmsUsed pmsOverloaded slaViolations totalIdleCpu
		// totalIdleMem

		StringBuilder sb = new StringBuilder();
		sb.append(NEW_LINE);
		sb.append(forecastingModule.getTimeOfLastPredictions()).append(
				DELIMITER);
		sb.append(pmsUsed).append(DELIMITER);
		sb.append(pmsOverloaded).append(DELIMITER);
		sb.append(measurement.getActualDemand().size()).append(DELIMITER);
		sb.append(slaViolations);// .append(DELIMITER);
		// sb.append(totalIdleResources.get(ResourceType.CPU)).append(DELIMITER);
		// sb.append(totalIdleResources.get(ResourceType.MEMORY));

		if (statisticsFile == null) {
			initialize();
		}

		try (BufferedWriter writer = Files.newBufferedWriter(statisticsFile,
				charset, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
			writer.write(sb.toString());

			writer.flush();
			writer.close();
		} catch (IOException x) {
			System.err.format("IOException: %s%n", x);
		}
	}
}
