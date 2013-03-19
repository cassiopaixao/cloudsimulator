package br.usp.ime.cassiop.workloadsim.exceptions;

public class NoMoreServersAvailableException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NoMoreServersAvailableException() {
		super();
	}

	public NoMoreServersAvailableException(String message) {
		super(message);
	}
}
