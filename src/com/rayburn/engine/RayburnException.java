package com.rayburn.engine;

/**
 * @author willstuckey
 * @date 10/6/14 <p></p>
 */
public class RayburnException extends Exception
{
	public RayburnException()
	{
		super();
	}

	public RayburnException(String message)
	{
		super(message);
	}

	public RayburnException(Throwable cause)
	{
		super(cause);
	}

	public RayburnException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
