package com.rayburn.engine.dew.binding;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;

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
	public static int subObjectInitialCapacity = 1;

	/**
	 * Increment amount of the Vectors containing subobjects. Default 1.
	 */
	public static int subObjectIncrementAmount = 1;

	/**
	 * Initial capacity of the Vectors containing properties. Default 1.
	 */
	public static int propertyBindingInitialCapacity = 1;

	/**
	 * Increment amount of the Vectors containing properties. Default 1.
	 */
	public static int propertyBindingIncrementAmount = 1;

	public static int functionBindingInitialCapacity = 1;

	public static int functionBindingIncrementAmount = 1;

	private String objectID;

	protected Vector<AbstractDewEntityBinding>   subObjects       =
			new Vector<AbstractDewEntityBinding>(subObjectInitialCapacity, subObjectIncrementAmount);
	protected Vector<AbstractDewPropertyBinding> objectProperties =
			new Vector<AbstractDewPropertyBinding>(propertyBindingInitialCapacity, propertyBindingIncrementAmount);
	protected Vector<AbstractDewFunctionBinding> objectFunctions  =
			new Vector<AbstractDewFunctionBinding>(functionBindingInitialCapacity, functionBindingIncrementAmount);

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
	 * @param sort if the subobjects are being sorted after the addition
	 * @return if the addition was successful
	 */
	public boolean addSubObject(AbstractDewEntityBinding object, boolean sort)
	{
		for (AbstractDewEntityBinding subObject : subObjects)
		{
			if (object.getObjectID().equals(subObject.getObjectID()))
			{
				return false;
			}
		}

		subObjects.add(object);

		if (sort)
		{
			sortSubObjects();
		}

		return true;
	}

	/**
	 * adds a subobject to the current entity, then sorts subobjects
	 * @param object the sub-object being added
	 * @return if the addition was successful (no name conflicts)
	 */
	public boolean addSubObject(AbstractDewEntityBinding object)
	{
	 	return addSubObject(object, true);
	}

	/**
	 * adds subobjects to the current entity
	 * @param subObjs the sub-objects being created
	 * @return if there was no conflict in the additions. NOTE: if an addition fails the function continues to check
	 * and add other properties. A false could indicate one, several, or total failure. If necessary, pull down the
	 * Vector and cross check.
	 */
	public boolean addSubObjects(AbstractDewEntityBinding... subObjs)
	{
		boolean hadConflict = false;
		for (AbstractDewEntityBinding subObj : subObjs)
		{
			hadConflict = !addSubObject(subObj, false) || hadConflict;
		}

		sortSubObjects();

		return !hadConflict;
	}

	/**
	 * adds a property to the current entity
	 * @param property the property being added
	 * @param sort if the properties should be sorted after the addition
	 * @return if the addition was successful
	 */
	public boolean addProperty(AbstractDewPropertyBinding property, boolean sort)
	{
		for (AbstractDewPropertyBinding boundProperty : objectProperties)
		{
			if (boundProperty.getPropertyID().equals(property.getPropertyID()))
			{
				return false;
			}
		}

		/*
		 * Add a new property.
		 */
		objectProperties.add(property);

		if (sort)
		{
			sortProperties();
		}

		return true;
	}

	/**
	 * adds a property to the current entity
	 * @param property the property being added
	 * @return if the addition was successful (no name conflicts)
	 */
	public boolean addProperty(AbstractDewPropertyBinding property)
	{
		return addProperty(property, true);
	}

	/**
	 * adds properties to the current entity
	 * @param boundProperties the properties being added
	 * @return if there was a no conflict in the additions. NOTE: if an addition fails the function continues to check
	 * and add other properties. A false could indicate one, several, or total failure. If necessary, pull down the
	 * Vector and cross check.
	 */
	public boolean addProperties(AbstractDewPropertyBinding... boundProperties)
	{
		boolean hadConflict = false;
		for (AbstractDewPropertyBinding property : boundProperties)
		{
			hadConflict = !addProperty(property, false) || hadConflict;
		}

		/*
		 * Sort the ArrayList so binary search can be used at rt
		 */
		sortProperties();

		return !hadConflict;
	}

	/**
	 * get all properties belonging to the object
	 * @return all properties
	 */
	public Vector<AbstractDewPropertyBinding> getProperties()
	{
		return objectProperties;
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
			return objectProperties.get(index);
	}

	/**
	 * adds a function to the current entity
	 * @param function the function to be bound
	 * @param sort if the functions should be sorted immediately after adding the binding
	 * @return if the addition was successful
	 */
	public boolean addFunction(AbstractDewFunctionBinding function, boolean sort)
	{
		for (AbstractDewFunctionBinding boundFunction : objectFunctions)
		{
			if (boundFunction.getFunctionID().equals(function.getFunctionID()))
			{
				return false;
			}
		}

		objectFunctions.add(function);

		if (sort)
		{
			sortFunctions();
		}

		return true;
	}

	/**
	 * adds a function to the current entity, and then sorts
	 * @param function the function to be bound
	 * @return if the addition was successful
	 */
	public boolean addFunction(AbstractDewFunctionBinding function)
	{
		return addFunction(function, true);
	}

	/**
	 * adds functions to the current entity
	 * @param functions the functions to be bound
	 * @return if there was no conflict in the additions. NOTE: if an addition fails the function continues to check and
	 * add other properties. A false could indicate one, several, or total failure. If necessary, pull down the Vector
	 * and cross check.
	 */
	public boolean addFunctions(AbstractDewFunctionBinding... functions)
	{
		boolean hadConflict = false;
		for (AbstractDewFunctionBinding function : functions)
		{
			hadConflict = !addFunction(function, false) || hadConflict;
		}

		sortFunctions();

		return !hadConflict;
	}

	/**
	 * get all functions belonging to the object
	 * @return all functions
	 */
	public Vector<AbstractDewFunctionBinding> getFunctions()
	{
		return objectFunctions;
	}

	/**
	 * gets a function from the list using a binary search
	 * @param functionID the if of the function to be found
	 * @return the object, null if not present
	 */
	public AbstractDewFunctionBinding getFunction(String functionID)
	{
		int index = Collections.binarySearch(objectFunctions,
		                                     new DewFunctionBinding(functionID),
		                                     AbstractDewFunctionBinding.functionComparator);

		if (index < 0)
			return null;
		else
			return objectFunctions.get(index);
	}

	/**
	 * adds any abstract dew binding type
	 * @param binding the binding
	 * @param sort if the list of the addition should be sorted
	 * @return
	 */
	public boolean addBinding(Object binding, boolean sort)
	{
		if (binding instanceof AbstractDewEntityBinding)
		{
			return addSubObject((AbstractDewEntityBinding) binding, sort);
		}
		else if (binding instanceof AbstractDewPropertyBinding)
		{
			return addProperty((AbstractDewPropertyBinding) binding, sort);
		}
		else if (binding instanceof AbstractDewFunctionBinding)
		{
			return addFunction((AbstractDewFunctionBinding) binding, sort);
		}

		return false;
	}

	/**
	 * adds any abstract dew binding type, then sorts that type
	 * @param binding teh binding
	 * @return if the addition was successful
	 */
	public boolean addBinding(Object binding)
	{
		return addBinding(binding, true);
	}

	/**
	 *
	 * @param bindings
	 * @return
	 */
	public boolean addBindings(Object... bindings)
	{
		boolean hadError = false;
		for (Object binding : bindings)
		{
			hadError = !addBinding(binding, false) || hadError;
		}

		return hadError;
	}

	/**
	 * Sorts all subobjects based on ID. Must be called before using a binary search. Look at methods in this class to
	 * see which ones automatically invoke this action.
	 */
	public final void sortSubObjects()
	{
		Collections.sort(subObjects, Ordering.natural().onResultOf(new Function<AbstractDewEntityBinding, Comparable>()
		{
			@Override
			public Comparable apply(AbstractDewEntityBinding abstractDewEntityBinding)
			{
				return abstractDewEntityBinding.getObjectID();
			}
		}));
	}

	/**
	 * Sorts all properties based on ID. Must be called before using a binary search. Look at methods in this class to
	 * see which ones automatically invoke this action.
	 */
	public final void sortProperties()
	{
		Collections.sort(objectProperties, Ordering.natural().onResultOf(
				new Function<AbstractDewPropertyBinding, Comparable>() {
					@Override
					public Comparable apply(AbstractDewPropertyBinding abstractDewPropertyBinding) {
						return abstractDewPropertyBinding.getPropertyID();
					}
				}
		));
	}

	/**
	 * Sorts all functions based on ID. Must be called before using a binary search. Look at methods in this class to
	 * see which ones automatically invoke this action.
	 */
	public final void sortFunctions()
	{
		Collections.sort(objectFunctions, Ordering.natural().onResultOf(new Function<AbstractDewFunctionBinding, Comparable>()
		{
			@Override
			public Comparable apply(AbstractDewFunctionBinding abstractDewFunctionBinding)
			{
				return abstractDewFunctionBinding.getFunctionID();
			}
		}));
	}
}
