package br.usp.ime.cassiop.workloadsim;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Map;

import br.usp.ime.cassiop.workloadsim.util.Constants;

public abstract class StatisticsModule implements Parametrizable {

	protected VirtualizationManager virtualizationManager = null;
	protected MeasurementModule measurementModule = null;
	protected ForecastingModule forecastingModule = null;

	protected Path statisticsFile = null;
	protected static final Charset charset = Charset.forName("UTF-8");

	public Path getStatisticsFile() {
		return statisticsFile;
	}

	public void setStatisticsFile(Path statisticsFile) {
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

	public abstract void initialize() throws Exception;

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
		if (o instanceof Path) {
			setStatisticsFile((Path) o);
		} else {
			throw new Exception(String.format("Invalid parameter: %s",
					Constants.PARAMETER_STATISTICS_FILE));
		}
	}
}