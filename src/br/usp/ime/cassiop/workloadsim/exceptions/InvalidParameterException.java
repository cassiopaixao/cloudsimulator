package br.usp.ime.cassiop.workloadsim.exceptions;

public class InvalidParameterException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidParameterException() {
		super();
	}

	public InvalidParameterException(String message) {
		super(message);
	}

	public <T> InvalidParameterException(String parameter,
			Class<T> expectedClass) {
		super(String.format("Invalid parameter %s. Expected class: %s",
				parameter, expectedClass.getName()));
	}
}
