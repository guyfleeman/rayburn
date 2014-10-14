package com.rayburn.engine.dew.lang;

import com.rayburn.engine.dew.DewException;

/**
* @author willstuckey
* @date 9/21/14 <p></p>
*/
public class DewRuntimeException extends DewException
{
	public DewRuntimeException() {}

	/**
	 * @param message
	 */
	public DewRuntimeException(String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	public DewRuntimeException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DewRuntimeException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
