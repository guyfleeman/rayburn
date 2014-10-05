package com.normalizedinsanity.rayburn.engine.dew.binding;

/**
 * @author Will Stuckey
 * @date 2/22/14
 * <p></p>
 */
public class DewPropertyBindingLong extends AbstractDewPropertyBinding
{
	private long property;

	public DewPropertyBindingLong(String propertyID, long value)
	{
		super(propertyID);
		property = value;
	}

	public void setPropertyValue(Object value)
	{
		if (value.getClass() == Long.class)
			property = ((Long) value).longValue();
		else
			property = Long.parseLong(value.toString());
	}

	public void setPropertyValue(long value)
	{
		property = value;
	}

	public Object getPropertyValueObject()
	{
		return property;
	}

	public long getPropertyValue()
	{
		return property;
	}
}
