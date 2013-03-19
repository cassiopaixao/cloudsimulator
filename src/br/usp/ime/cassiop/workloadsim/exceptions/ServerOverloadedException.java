package br.usp.ime.cassiop.workloadsim.exceptions;

public class ServerOverloadedException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ServerOverloadedException() {
		super();
	}

	public ServerOverloadedException(String message) {
		super(message);
	}
}
