package com.rayburn.engine.dew.binding;

/**
 * @author Will Stuckey
 * @date 2/23/14
 * <p></p>
 */
public class DewPropertyBindingString extends AbstractDewPropertyBinding
{
	private String property;

	public DewPropertyBindingString(String propertyID, String value)
	{
		super(propertyID);
		this.property = value;
	}

	public void setPropertyValue(Object value)
	{
		property = value.toString();
	}

	public void setPropertyValue(String value)
	{
		property = value;
	}

	public Object getPropertyValueObject()
	{
		return property;
	}

	public String getPropertyValue()
	{
		return property;
	}
}
