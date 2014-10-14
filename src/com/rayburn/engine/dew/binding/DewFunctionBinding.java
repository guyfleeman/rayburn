package com.rayburn.engine.dew.binding;

import java.lang.reflect.InvocationTargetException;

/**
 * @author willstuckey
 * @date 9/30/14 <p></p>
 */
public class DewFunctionBinding extends AbstractDewFunctionBinding
{
	/**
	 * Comparable constructor for bin searches
	 * @param functionID the id of the the function
	 */
	public DewFunctionBinding(String functionID)
	{
		super(functionID, null, null);
	}

	/**
	 * Default constructor
	 * @param functionID the id of the function
	 * @param functionContainer the object containing the method to be run
	 * @param functionName the name of the function to be run
	 * @throws NoSuchMethodException if ya done fucked up a name
	 */
	public DewFunctionBinding(String functionID, Object functionContainer, String functionName)
			throws NoSuchMethodException
	{
		super(functionID,
		      functionContainer.getClass().getMethod(functionName),
		      functionContainer);
	}

	/**
	 * invokes a void method
	 * @return the return of the function if called properly, if parameter mismatch, an instance of
	 * DewFunctionInvocationError
	 */
	public Object invoke()
	{
		if (super.method.getParameterTypes().length == 0)
		{
			return new DewFunctionInvocationError("method does not have void parameter or does not have void " +
					                                      "parameter overload");
		}

		try
		{
			return super.method.invoke(super.functionContainer);
		}
		catch (IllegalAccessException e)
		{
			return new DewFunctionInvocationError(e);
		}
		catch (InvocationTargetException e)
		{
			return new DewFunctionInvocationError(e);
		}
	}

	/**
	 * invokes a parameterized method
	 * @param args the arguments of the function. length and type check ahead of time
	 * @return the return of the function if called properly, if parameter mismatch, and instance of
	 * DewFunctionInvocationError
	 */
	public Object invoke(Object... args)
	{
		if (super.method.getParameterTypes().length != args.length)
		{
			return new DewFunctionInvocationError("method parameter length does not match provided parameter length");
		}

		try
		{
			return super.method.invoke(super.functionContainer);
		}
		catch (IllegalAccessException e)
		{
			return new DewFunctionInvocationError(e);
		}
		catch (InvocationTargetException e)
		{
			return new DewFunctionInvocationError(e);
		}
	}
}
