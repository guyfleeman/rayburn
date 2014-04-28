package rayburn.engine.dew.bindings;

/**
 * @author Will Stuckey
 * @date 2/22/14
 * <p></p>
 */
public class DewPropertyBindingInt extends AbstractDewPropertyBinding
{
	private int property;

	public DewPropertyBindingInt(String propertyID, int value)
	{
		super(propertyID);
		property = value;
	}

	public void setPropertyValue(Object value)
	{
		if (value.getClass() == Integer.class)
			property = ((Integer) value).intValue();
		else
			property = Integer.parseInt(value.toString());
	}

	public void setPropertyValue(int value)
	{
		property = value;
	}

	public Object getPropertyValueObject()
	{
		return property;
	}

	public int getPropertyValue()
	{
		return property;
	}
}
