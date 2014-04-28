package rayburn.engine.dew.bindings;

/**
 * @author Will Stuckey
 * @date 2/22/14
 * <p>This class is a binding that interfaces engine and user-defined properties with a property-object tree for the
 * engine. The property tree can be navigated and modified at runtime through the DewLanguageBindingInterface. This
 * allows for the full control of engine properties through the Dew language and the Dew command processor that uses
 * the language in the engine shell. Properties abstracted from the class though this binding can be modified by the
 * method described above.</p>
 * @see rayburn.engine.dew.lang.DewLanguageBindingInterface
 */
public abstract class AbstractDewPropertyBinding
{
	/**
	 * Lock-Always. Describes the behavior of rounding when a the generic object type is a float and must be cast to a
	 * data type that does not support decimal or float data.
	 */
	public static volatile boolean roundFloatCast = false;

	/**
	 * Lock-Always. Describes the behavior of rounding when a the generic object type is a double and must be cast to a
	 * data type that does not support decimal or double data.
	 */
	public static volatile boolean roundDoubleCase = false;

	/**
	 * Lock-Always. Describes the behavior of casting a generic object paramterer to a numeric data type when the cast
	 * type is not natively supported by java. If true, when an object is cast to a numeric data type and the the
	 * numeric cannot be parsed or cast, should the Object's hex pointer be used rather than no value be assigned.
	 */
	public static volatile boolean allowCastObjectToNumericLiteral = false;

	private final String propertyID;

	/**
	 * Default constructor for the bound property.
	 * @param propertyID The ID used to identify the property in the property-object tree.
	 */
	public AbstractDewPropertyBinding(String propertyID)
	{
		this.propertyID = propertyID;
	}

	/**
	 * @return property ID
	 */
	public String getPropertyID()
	{
		return propertyID;
	}

	/**
	 * @return the property ID followed by its value
	 */
	public String toString()
	{
		return getPropertyID() + ": " + getPropertyValueObject().toString();
	}

	/**
	 * @param value the value of the property. Casting and security is handled by the subclass implementing this method.
	 * See teh documentation in the subclass for thread safety and value handling.
	 */
	public abstract void setPropertyValue(Object value);

	/**
	 * @return and object representing the property value
	 */
	public abstract Object getPropertyValueObject();
}
