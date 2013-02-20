package br.usp.ime.cassiop.workloadsim;

import java.util.List;

import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;

public interface ForecastingModule extends Parametrizable {

	public List<VirtualMachine> getPredictions(long currentTime)
			throws Exception;

	public long getTimeOfLastPredictions();

}