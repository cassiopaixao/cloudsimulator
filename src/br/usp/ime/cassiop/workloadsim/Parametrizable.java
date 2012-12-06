package br.usp.ime.cassiop.workloadsim;

import java.util.Map;

public interface Parametrizable {

	/**
	 * 
	 * @param parameters
	 *            parameters to set
	 * @throws Exception
	 *             if at least one of the parameters is invalid.
	 */
	public void setParameters(Map<String, Object> parameters) throws Exception;
}
