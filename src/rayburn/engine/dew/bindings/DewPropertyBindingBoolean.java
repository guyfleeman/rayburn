package rayburn.engine.dew.bindings;

/**
 * @author Will Stuckey
 * @date 2/22/14
 * <p></p>
 */
public class DewPropertyBindingBoolean extends AbstractDewPropertyBinding
{
	private boolean property;

	public DewPropertyBindingBoolean(String propertyID, boolean value)
	{
		super(propertyID);
		property = value;
	}

	public void setPropertyValue(Object value)
	{
		if (value.getClass() == Boolean.class)
			property = ((Boolean) value).booleanValue();
		else
			property = Boolean.parseBoolean(value.toString());
	}

	public void setPropertyValue(boolean value)
	{
		property = value;
	}

	public Object getPropertyValueObject()
	{
		return property;
	}

	public boolean getPropertyValue()
	{
		return property;
	}
}
