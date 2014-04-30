package rayburn.engine.dew.bindings;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import rayburn.engine.dew.command.Command;

import java.util.Collections;
import java.util.Vector;

/**
 * @author Will Stuckey
 * @date 2/22/14
 * <p>This class represents an Object in the Object-Property tree readable by the DewConsole and DewInterpreter. Any
 * entity that has properties that can be modified at runtime or has sub-objects that exhibit ownership properties
 * should extend this class.</p>
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

	/**
	 * Default constructor.
	 * @param objectID the entity name, used when searching the Object-Property tree. If the given name is null or
	 *                    invalid (contains a '.'), the object's hashcode will be used for the name
	 */
	public AbstractDewEntityBinding(String objectID)
	{
		if (objectID == null || objectID.contains("."))
			this.objectID = Integer.toString(this.hashCode());
		else
			this.objectID = objectID;
	}

	/**
	 * @return the objectID
	 */
	public String getObjectID()
	{
		return objectID;
	}

	/**
	 * gets all sub-entities. The next level of depth in the tree
	 * @return all sub-objects
	 */
	public Vector<AbstractDewEntityBinding> getSubObjects()
	{
		return subObjects;
	}

	/**
	 * adds a subobject to the current entity
	 * @param object the sub-object being added
	 * @return if the addition was successful (no name conflicts)
	 */
	public boolean addSubObject(AbstractDewEntityBinding object)
	{
	 	for (AbstractDewEntityBinding subObject : subObjects)
	    {
		    if (object.getObjectID().equals(subObject.getObjectID()))
		    {
			    return false;
		    }
	    }

		subObjects.add(object);
		return true;
	}

	/**
	 * get all properties belonging to the entity
	 * @return all properties
	 */
	public Vector<AbstractDewPropertyBinding> getObjectProperties()
	{
		return objectProperties;
	}

	/**
	 * adds a property to the current entity
	 * @param boundProperty the property being added
	 * @return if the addition was successful (no name conflicts)
	 */
	public boolean addProperty(AbstractDewPropertyBinding boundProperty)
	{
		for (AbstractDewPropertyBinding property : objectProperties)
		{
			if (boundProperty.getPropertyID().equals(property.getPropertyID()))
			{
				return false;
			}
		}

		/*
		 * Add a new property.
		 */
		objectProperties.add(boundProperty);

		/*
		 * Sort the ArrayList so binary search can be used at rt
		 */
		Collections.sort(objectProperties, Ordering.natural().onResultOf(
				new Function<AbstractDewPropertyBinding, String>() {
					public String apply(AbstractDewPropertyBinding property) {
						return property.getPropertyID();
					}
				}
		));

		return true;
	}

	/**
	 * gets a property from the property list using a binary search
	 * @param propertyID proeprtyID
	 * @return property or null if no match
	 */
	public AbstractDewPropertyBinding getProperty(String propertyID)
	{
		int index = Collections.binarySearch(objectProperties,
				new DewPropertyBindingByte(propertyID, (byte) 0),
				AbstractDewPropertyBinding.propertyComparator);

		if (index < 0)
			return null;
		else
			return objectProperties.get(0);
	}
}
