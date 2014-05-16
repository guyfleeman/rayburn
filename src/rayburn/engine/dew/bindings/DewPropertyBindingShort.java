package rayburn.engine.dew.bindings;

/**
 * @author Will Stuckey
 * @date 2/22/14
 * <p></p>
 */
public class DewPropertyBindingShort extends AbstractDewPropertyBinding
{
	private short property;

	public DewPropertyBindingShort(String propertyID, short value)
	{
		super(propertyID);
		property = value;
	}

	public void setPropertyValue(Object value)
	{
		if (value.getClass() == Short.class)
			property = ((Short) value).shortValue();
		else
			property = Short.parseShort(value.toString());
	}

	public void setPropertyValue(short value)
	{
		property = value;
	}

	public Object getPropertyValueObject()
	{
		return property;
	}

	public short getPropertyValue()
	{
		return property;
	}
}
