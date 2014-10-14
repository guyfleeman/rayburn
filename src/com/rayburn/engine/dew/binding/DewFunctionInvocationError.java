package com.rayburn.engine.dew.binding;

/**
 * @author willstuckey
 * @date 9/30/14 <p></p>
 */
public class DewFunctionInvocationError
{
	public final String message;
	public final Throwable cause;

	public DewFunctionInvocationError()
	{
		this(null, null);
	}

	public DewFunctionInvocationError(String message)
	{
		this(message, null);
	}

	public DewFunctionInvocationError(Throwable cause)
	{
		this(null, cause);
	}

	public DewFunctionInvocationError(String message, Throwable cause)
	{
		this.message = message;
		this.cause = cause;
	}

}
