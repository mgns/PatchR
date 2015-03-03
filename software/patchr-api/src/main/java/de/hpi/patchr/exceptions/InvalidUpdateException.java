package de.hpi.patchr.exceptions;

/**
 * @author magnus
 */
public class InvalidUpdateException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 */
	public InvalidUpdateException(String message) {
		super(message);
	}

}
