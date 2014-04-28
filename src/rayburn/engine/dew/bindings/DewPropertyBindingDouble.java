package rayburn.engine.dew.bindings;

/**
 * @author Will Stuckey
 * @date 2/22/14
 * <p></p>
 */
public class DewPropertyBindingDouble extends AbstractDewPropertyBinding
{
	private double property;

	public DewPropertyBindingDouble(String propertyID, double value)
	{
		super(propertyID);
		property = value;
	}

	public void setPropertyValue(Object value)
	{
		if (value.getClass() == Double.class)
			property = ((Double) value).doubleValue();
		else
			property = Double.parseDouble(value.toString());
	}

	public void setPropertyValue(double value)
	{
		property = value;
	}

	public Object getPropertyValueObject()
	{
		return property;
	}

	public double getPropertyValue()
	{
		return property;
	}
}
