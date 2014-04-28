package rayburn.engine.dew.bindings;

import java.util.Vector;

/**
 * @author Will Stuckey
 * @date 2/22/14
 * <p></p>
 */
public abstract class AbstractDewEntityBinding
{
	/**
	 * Initial capacity of the Vectors containing subobjects. Default 1.
	 */
	public static volatile int subObjectInitialCapacity = 1;

	/**
	 * Increment amount of the Vectors containing subobjects. Default 1.
	 */
	public static volatile int subObjectIncrementAmount = 1;

	/**
	 * Initial capacity of the Vectors containing properties. Default 1.
	 */
	public static volatile int propertyBindingInitialCapacity = 1;

	/**
	 * Increment amount of the Vectors containing properties. Default 1.
	 */
	public static volatile int propertyBindingIncrementAmount = 1;

	private String objectID;

	private Vector<AbstractDewEntityBinding> subObjects =
			new Vector<AbstractDewEntityBinding>(subObjectInitialCapacity, subObjectIncrementAmount);
	private Vector<AbstractDewPropertyBinding> objectProperties =
			new Vector<AbstractDewPropertyBinding>(propertyBindingInitialCapacity, propertyBindingIncrementAmount);

	public AbstractDewEntityBinding(String objectID)
	{
		if (objectID == null)
			this.objectID = this.toString().substring(this.toString().length() - 9);
		else
			this.objectID = objectID;
	}

	public String getObjectID()
	{
		return objectID;
	}

	public Vector<AbstractDewEntityBinding> getSubObjects()
	{
		return subObjects;
	}

	public void addSubObject(AbstractDewEntityBinding subObject)
	{

	}

	public Vector<AbstractDewPropertyBinding> getConsoleObjectProperties()
	{
		return objectProperties;
	}

	public void addProperty(AbstractDewPropertyBinding boundProperty)
	{

	}

	public abstract void updateProperties();
}
