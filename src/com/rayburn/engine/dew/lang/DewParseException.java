package com.rayburn.engine.dew.lang;

import com.rayburn.engine.dew.DewException;

/**
 *
 */
public class DewParseException extends DewException
{
	/**
	 *
	 */
	public DewParseException()
	{

	}

	/**
	 * @param message
	 */
	public DewParseException(String message)
	{
		super(message);
	}

	/**
	 *
	 * @param cause
	 */
	public DewParseException(Throwable cause)
	{
		super(cause);
	}

	/**
	 *
	 * @param message
	 * @param cause
	 */
	public DewParseException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
