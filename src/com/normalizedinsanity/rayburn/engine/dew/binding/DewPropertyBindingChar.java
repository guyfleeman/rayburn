package com.normalizedinsanity.rayburn.engine.dew.binding;

/**
 * @author Will Stuckey
 * @date 2/22/14
 * <p></p>
 */
public class DewPropertyBindingChar extends AbstractDewPropertyBinding
{
	private char property;

	public DewPropertyBindingChar(String propertyID, char value)
	{
		super(propertyID);
		property = value;
	}

	public void setPropertyValue(Object value)
	{
		if (value.getClass() == Character.class)
			property = ((Character) value).charValue();
	}

	public void setPropertyValue(char value)
	{
		property = value;
	}

	public Object getPropertyValueObject()
	{
		return property;
	}

	public char getPropertyValue()
	{
		return property;
	}
}
