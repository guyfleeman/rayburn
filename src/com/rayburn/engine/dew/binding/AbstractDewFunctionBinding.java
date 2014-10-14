package com.rayburn.engine.dew.binding;

import java.lang.reflect.Method;
import java.util.Comparator;

/**
 * @author willstuckey
 * @date 9/30/14 <p></p>
 * <p>This class is a binding that interfaces engine and user-defined functions with a property-object tree for the
 * engine. The property tree can be navigated and modified at runtime through the DewLanguageBindingInterface. This
 * allows for the full control of engine properties through the Dew language and the Dew command processor that uses
 * the language in the engine shell. Properties abstracted from the class though this binding can be modified by the
 * method described above.</p>
 * @see com.rayburn.engine.dew.lang.DewLanguageBindingInterface
 */
public abstract class AbstractDewFunctionBinding
{
	/**
	 * The comparator for this class.
	 */
	public static final Comparator<AbstractDewFunctionBinding> functionComparator;

	private final String functionID;
	protected final Method method;
	protected final Object functionContainer;

	/**
	 * static initializer to create function comparator
	 */
	static
	{
		functionComparator = new Comparator<AbstractDewFunctionBinding>()
		{
			public int compare(AbstractDewFunctionBinding function1, AbstractDewFunctionBinding function2)
			{
				return function1.getFunctionID().compareTo(function2.getFunctionID());
			}
		};
	}

	/**
	 * Default constructor for the bound function.
	 * @param functionID The ID used to identify the property in the property-object tree.
	 * @param method the bound function
	 */
	public AbstractDewFunctionBinding(String functionID, final Method method, final Object functionContainer)
	{
		this.functionID = functionID;
		this.method = method;
		this.functionContainer = functionContainer;
	}

	/**
	 * @return function ID
	 */
	public String getFunctionID()
	{
		return functionID;
	}

	public abstract Object invoke();

	public abstract Object invoke(Object... args);
}
