package br.usp.ime.cassiop.workloadsim.forecasting;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.VirtualizationManager;
import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;
import br.usp.ime.cassiop.workloadsim.util.Constants;

public class MeasurementsMeanForecasting extends WorkloadForecasting {

	final Logger logger = LoggerFactory
			.getLogger(MeasurementsMeanForecasting.class);

	private VirtualizationManager virtualizationManager = null;

	private Map<String, VmHistory> vmsHistory = null;

	private int measurementWindow = 1;

	public int getMeasurementWindow() {
		return measurementWindow;
	}

	public void setMeasurementWindow(int measurementWindow) {
		this.measurementWindow = measurementWindow;
	}

	public MeasurementsMeanForecasting() {
		super();

		vmsHistory = new HashMap<String, MeasurementsMeanForecasting.VmHistory>();
	}

	@Override
	public void setParameters(Map<String, Object> parameters) throws Exception {
		super.setParameters(parameters);

		Object o = null;

		o = parameters.get(Constants.PARAMETER_VIRTUALIZATION_MANAGER);
		if (o instanceof VirtualizationManager) {
			setVirtualizationManager((VirtualizationManager) o);
		} else {
			throw new Exception(String.format("Invalid parameter: %s",
					Constants.PARAMETER_VIRTUALIZATION_MANAGER));
		}

		o = parameters.get(Constants.PARAMETER_FORECASTING_MEASUREMENT_WINDOW);
		if (o instanceof Integer) {
			setMeasurementWindow(((Integer) o).intValue());
		} else {
			throw new Exception(String.format("Invalid parameter: %s",
					Constants.PARAMETER_FORECASTING_MEASUREMENT_WINDOW));
		}
	}

	public List<VirtualMachine> getPredictions(long currentTime)
			throws Exception {
		List<VirtualMachine> actualFutureDemand = super
				.getPredictions(currentTime);

		List<VirtualMachine> predictedDemand = new LinkedList<VirtualMachine>();

		Map<String, VirtualMachine> activeVmsMap = virtualizationManager
				.getActiveVirtualMachines();

		if (currentTime > workload.getInitialTime()) {
			updateVmsHistory(currentTime - workload.getTimeInterval());

			removeOldVmsHistory(actualFutureDemand);
		}

		for (VirtualMachine vm : actualFutureDemand) {
			// if it's already running
			if (activeVmsMap.containsKey(vm.getName())) {
				// predict using last measurements
				vm.setDemand(ResourceType.CPU, vmsHistory.get(vm.getName())
						.getMeanCpu());
				vm.setDemand(ResourceType.MEMORY, vmsHistory.get(vm.getName())
						.getMeanMem());
			}
			predictedDemand.add(vm);
		}

		logger.debug(
				"Measurement window prediction for time {}. Max window size: {}.",
				currentTime, measurementWindow);

		return predictedDemand;
	}

	private void removeOldVmsHistory(List<VirtualMachine> predictedDemand) {
		Map<String, VirtualMachine> keepRunning = new HashMap<String, VirtualMachine>(
				predictedDemand.size());
		for (VirtualMachine vm : predictedDemand) {
			keepRunning.put(vm.getName(), vm);
		}

		List<String> shouldRemove = new LinkedList<String>();

		for (String vmName : vmsHistory.keySet()) {
			if (!keepRunning.containsKey(vmName)) {
				shouldRemove.add(vmName);
			}
		}

		for (String vmName : shouldRemove) {
			vmsHistory.remove(vmName);
		}
	}

	private void updateVmsHistory(long lastTime) throws Exception {
		List<VirtualMachine> actualLastDemand = super.getPredictions(lastTime);
		for (VirtualMachine vm : actualLastDemand) {
			if (!vmsHistory.containsKey(vm.getName())) {
				vmsHistory.put(vm.getName(), new VmHistory(measurementWindow));
			}
			vmsHistory.get(vm.getName()).addMeasurement(
					vm.getDemand(ResourceType.CPU),
					vm.getDemand(ResourceType.MEMORY));
		}
	}

	public void setVirtualizationManager(
			VirtualizationManager virtualizationManager) {
		this.virtualizationManager = virtualizationManager;
	}

	private class VmHistory {
		private double[] measurementsCpu, measurementsMem;
		private int qtyMeasurements = 0;
		private int maxMeasurements = 0;

		public VmHistory(int maxMeasurements) {
			measurementsCpu = new double[maxMeasurements];
			measurementsMem = new double[maxMeasurements];
			this.maxMeasurements = maxMeasurements;
		}

		public void addMeasurement(double measurementCpu, double measurementMem) {
			measurementsCpu[qtyMeasurements % maxMeasurements] = measurementCpu;
			measurementsMem[qtyMeasurements % maxMeasurements] = measurementMem;
			qtyMeasurements++;
		}

		public double getMeanCpu() {
			return getMean(measurementsCpu);
		}

		public double getMeanMem() {
			return getMean(measurementsMem);
		}

		private double getMean(double[] measurements) {
			double mean = 0;
			if (qtyMeasurements < maxMeasurements) {
				for (int i = 0; i < qtyMeasurements; i++) {
					mean += measurements[i];
				}
				mean = mean / qtyMeasurements;
			} else {
				for (int i = 0; i < maxMeasurements; i++) {
					mean += measurements[i];
				}
				mean = mean / maxMeasurements;
			}
			return mean;
		}

	}
}
