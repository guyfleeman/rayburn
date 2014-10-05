package com.normalizedinsanity.rayburn.engine.dew;

/**
 * Generic exception class for the lang interpreter.
 */
public class DewException extends Exception
{
	public DewException() {}

	/**
	 * @param message
	 */
	public DewException(String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	public DewException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DewException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public String toString()
	{
		return "DEW EXCEPTION!" + getMessage();
	}
}
