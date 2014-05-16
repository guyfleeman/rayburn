package rayburn.engine.dew.bindings;

/**
 * @author Will Stuckey
 * @date 2/22/14
 * <p></p>
 */
public class DewPropertyBindingByte extends AbstractDewPropertyBinding
{
	private byte property;

	public DewPropertyBindingByte(String propertyID, byte value)
	{
		super(propertyID);
		property = value;
	}

	public void setPropertyValue(Object value)
	{
		if (value.getClass() == Byte.class)
			property = ((Byte) value).byteValue();
		else
			property = Byte.parseByte(value.toString());
	}

	public void setPropertyValue(byte value)
	{
		property = value;
	}

	public Object getPropertyValueObject()
	{
		return property;
	}

	public byte getPropertyValue()
	{
		return property;
	}
}