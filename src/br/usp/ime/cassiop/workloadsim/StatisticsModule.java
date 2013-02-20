package br.usp.ime.cassiop.workloadsim;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import br.usp.ime.cassiop.workloadsim.util.Constants;

public abstract class StatisticsModule implements Parametrizable {

	protected VirtualizationManager virtualizationManager = null;
	protected MeasurementModule measurementModule = null;
	protected ForecastingModule forecastingModule = null;

//	protected Path statisticsFile = null;
	protected File statisticsFile = null;
	protected static final Charset charset = Charset.forName("UTF-8");

	protected Map<String, Integer> statistics = null;

	public File getStatisticsFile() {
		return statisticsFile;
	}

	public void setStatisticsFile(File statisticsFile) {
		this.statisticsFile = statisticsFile;
	}

	public VirtualizationManager getVirtualizationManager() {
		return virtualizationManager;
	}

	public void setVirtualizationManager(
			VirtualizationManager virtualizationManager) {
		this.virtualizationManager = virtualizationManager;
	}

	public MeasurementModule getMeasurementModule() {
		return measurementModule;
	}

	public void setMeasurementModule(MeasurementModule measurementModule) {
		this.measurementModule = measurementModule;
	}

	public ForecastingModule getForecastingModule() {
		return forecastingModule;
	}

	public void setForecastingModule(ForecastingModule forecastingModule) {
		this.forecastingModule = forecastingModule;
	}

	public abstract void generateStatistics(long currentTime) throws Exception;

	public void initialize() throws Exception {
		statistics = new HashMap<String, Integer>();
	}

	public void setStatisticValue(String name, int value) {
		statistics.put(name, new Integer(value));
	}

	public void addToStatisticValue(String name, int value) {
		if (statistics.containsKey(name)) {
			statistics.put(name, new Integer(statistics.get(name).intValue()
					+ value));
		} else {
			setStatisticValue(name, value);
		}
	}

	public void clearStatistics() {
		for (String key : statistics.keySet()) {
			statistics.put(key, new Integer(0));
		}
	}

	@Override
	public void setParameters(Map<String, Object> parameters) throws Exception {
		Object o = null;

		o = parameters.get(Constants.PARAMETER_VIRTUALIZATION_MANAGER);
		if (o instanceof VirtualizationManager) {
			setVirtualizationManager((VirtualizationManager) o);
		} else {
			throw new Exception(String.format("Invalid parameter: %s",
					Constants.PARAMETER_VIRTUALIZATION_MANAGER));
		}

		o = parameters.get(Constants.PARAMETER_FORECASTING_MODULE);
		if (o instanceof ForecastingModule) {
			setForecastingModule((ForecastingModule) o);
		} else {
			throw new Exception(String.format("Invalid parameter: %s",
					Constants.PARAMETER_FORECASTING_MODULE));
		}

		o = parameters.get(Constants.PARAMETER_MEASUREMENT_MODULE);
		if (o instanceof MeasurementModule) {
			setMeasurementModule((MeasurementModule) o);
		} else {
			throw new Exception(String.format("Invalid parameter: %s",
					Constants.PARAMETER_MEASUREMENT_MODULE));
		}

		o = parameters.get(Constants.PARAMETER_STATISTICS_FILE);
		if (o instanceof File) {
			setStatisticsFile((File) o);
		} else {
			throw new Exception(String.format("Invalid parameter: %s",
					Constants.PARAMETER_STATISTICS_FILE));
		}
	}

}