package com.normalizedinsanity.rayburn.engine.dew.binding;

import com.normalizedinsanity.rayburn.engine.dew.Console;

import java.util.AbstractList;
import java.util.Iterator;

/**
 * @author Will Stuckey
 * @date 2/24/14
 * <p>This class functions as a generic binding between dynamic object properties, and a runtime console, command
 * (pre)processor, or script engine. This allows previously locked internal values to be modified at runtime by a user
 * or user-written script without reinitialization.</p>
 */
public class DewPropertyBindingObject<E> extends AbstractDewPropertyBinding
{
	private E property;
	private Class<E> typeParameter;

	public DewPropertyBindingObject(String name, E property, Class<E> typeParameter)
	{
		super(name);
		this.property = property;
		this.typeParameter = typeParameter;
	}

	/**
	 * @return the type parameter for the given DewPropertyBindingObject instance
	 */
	public Class getPropertyType()
	{
		return typeParameter;
	}

	/**
	 * @return the property value
	 */
	public Object getPropertyValueObject()
	{
		return property;
	}

	/**
	 * @return is the property object an instance of the class AbstractList or a valid subclass of AbstractList(List,
	 * ArrayList, Vector)
	 */
	public boolean isPropertyInstanceOfAbstractList()
	{
		return AbstractList.class.isInstance(property);
	}

	/**
	 * Sets the property from a generic Object. If the generic object is not of the same type parameter, the request to
	 * set the value will be ignored.
	 *
	 * ----------*----------
	 * This explains why a setPropertyValue(E property) was not implemented. If this does not pertain to you, please
	 * skip it.
	 *
	 * The Java Compiler is currently unable to distinguish the difference between an Object parameter and type
	 * parameter. Legacy java code may still use raw type, meaning the implementation of ambiguous erasure solution
	 * would break old code. Since this class acts as a language binding between the Engine's java core and is scripting
	 * support, implementing a type parameter would remove the scripting language's ability to set a
	 * DewPropertyBindingObject's property object to that of a valid subclass. In order to keep the language extensible,
	 * an Object parameter is taken rather than that of the instance's type. It is recommended that the user use the
	 * method getPropertyType() and compare instance type against the object type before passing it as a parameter. This
	 * will ensure the cast is successful, and will avoid a situation where the property is not updated because the
	 * parameter was not of a valid type.
	 * ----------*----------
	 *
	 * @param o the new property value
	 */
	public void setPropertyValue(Object o)
	{
		if (typeParameter.isInstance(o))
			this.property = (E) o;
		else if (Console.getInstance().printDebug)
			System.out.println("Object passed as property value does not match type parameter.");
	}

	/**
	 * Checks class types of all elements of the property AbstractList(List, ArrayList, Vector). If all held objects are of the
	 * same class, that class type will be returned. If the held objects are not of the same class, the highest level
	 * superclass (other than Object) will be used for comparison, and then returned if all the objects held by the
	 * AbstractList property are instances of the superclass. If no common class is found, or the property held within
	 * this instance of DewPropertyBindingObject, null will be returned. To check if the property held within this instance
	 * of this class use isPropertyInstanceOfAbstractList().
	 *
	 * @return the lowest level common class among elements of the AbstractList, or null if the property is not an
	 * instance of AbstractList, or no common class is found.
	 */
	public Class getAbstractListGenericType()
	{
		return getAbstractListGenericType(property);
	}

	/**
	 * Checks if class types of all elements of the property AbstractList(List, ArrayList, Vector) against the Class
	 * parameter taken. If all held objects are of this class, that class type will be returned. If all elements of the
	 * property AbstractList are not instances of the class provided, or the property of the DewPropertyBindingObject
	 * instance is not an AbstractList, null will be returned.
	 *
	 * @param c the class that will be used to check for commonality among the objects in the property AbstractList
	 * @return the lowest level common class among elements of the AbstractList, or null if the property is not an
	 * instance of AbstractList, or no common class is found.
	 */
	public Class getAbstractListGenericType(Class c)
	{
		return getAbstractListGenericType(property, c);
	}

	/**
	 * Checks class types of all elements of the list AbstractList(List, ArrayList, Vector). If all held objects are of the
	 * same class, that class type will be returned. If the held objects are not of the same class, the highest level
	 * superclass (other than Object) will be used for comparison, and then returned if all the objects held by the
	 * list are instances of the superclass. If no common class is found, the object list provided is not an instance
	 * of AbstractList, or the list has zero length, null will be returned.
	 *
	 * @param list the object to find a generic type
	 * @return the lowest level common class among elements of the AbstractList list, or null if the object is not an
	 * instance of AbstractList, or if the list has zero length, or no common class is found.
	 */
	public static Class getAbstractListGenericType(Object list)
	{
		if (list == null
				|| !(list instanceof AbstractList)
				|| ((AbstractList) list).size() == 0)
			return null;

		Class genericType = ((AbstractList) list).get(0).getClass();
		Iterator iterator = ((Iterable) list).iterator();
		while (iterator.hasNext())
		{
			Object instance = iterator.next();
			if (instance.getClass() != genericType)
				return instance.getClass().getGenericSuperclass() == null
						? null
						: getAbstractListGenericType(list, instance.getClass().getSuperclass());
		}

		return genericType;
	}

	/**
	 * Checks class types of all elements of the list AbstractList(List, ArrayList, Vector) against the provided class.
	 * If all held objects are of the provided class, that class will be returned. If the held objects are not of the
	 * provided class, null will be returned. If the object list provided is not an instance of AbstractList, or the
	 * list has zero length, null will be returned.
	 *
	 * @param list the object to find a generic type
	 * @param comparatorClass the generic type that will be crossed checked against the common type of the list
	 * @return the lowest level common class among elements of the AbstractList list, or null if the object is not an
	 * instance of AbstractList, or if the list has zero length, or no common class is found.
	 */
	public static Class getAbstractListGenericType(Object list, Class comparatorClass)
	{
		if (comparatorClass == null
				|| list == null
				|| !(list instanceof AbstractList)
				|| ((AbstractList) list).size() == 0)
			return null;

		Iterator iterator = ((Iterable) list).iterator();
		while (iterator.hasNext())
			if (!comparatorClass.isInstance(iterator.next()))
				return null;

		return comparatorClass;
	}
}