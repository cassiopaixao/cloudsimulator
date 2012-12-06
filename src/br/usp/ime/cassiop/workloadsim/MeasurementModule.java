package br.usp.ime.cassiop.workloadsim;

public interface MeasurementModule extends Parametrizable {

	public Measurement measureSystem(long currentTime) throws Exception;

}