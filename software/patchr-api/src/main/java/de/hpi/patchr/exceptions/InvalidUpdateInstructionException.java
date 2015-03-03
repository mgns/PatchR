package de.hpi.patchr.exceptions;

/**
 * @author magnus
 */
public class InvalidUpdateInstructionException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 */
	public InvalidUpdateInstructionException(String message) {
		super(message);
	}
	
}
