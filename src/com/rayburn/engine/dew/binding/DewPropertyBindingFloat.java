package com.rayburn.engine.dew.binding;

/**
 * @author Will Stuckey
 * @date 2/22/14
 * <p></p>
 */
public class DewPropertyBindingFloat extends AbstractDewPropertyBinding
{
	private float property;

	public DewPropertyBindingFloat(String propertyID, float value)
	{
		super(propertyID);
		property = value;
	}

	public void setPropertyValue(Object value)
	{
		if (value.getClass() == Float.class)
			property = ((Float) value).floatValue();
		else
			property = Float.parseFloat(value.toString());
	}

	public void setPropertyValue(float value)
	{
		property = value;
	}

	public Object getPropertyValueObject()
	{
		return property;
	}

	public float getPropertyValue()
	{
		return property;
	}
}