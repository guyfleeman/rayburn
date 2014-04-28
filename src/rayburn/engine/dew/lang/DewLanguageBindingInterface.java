package rayburn.engine.dew.lang;

/**
 * @author Will Stuckey
 * @date 3/3/14
 * <p></p>
 */
public interface DewLanguageBindingInterface
{
	public String getName();

	public void getBoundEntity(String name);

	public void getBoundProperty(String name);

	public void setBoundProperty(String path, Object property);

 	public void print(Object o);

	public void command(String commandName);

	public void runtime(String program);
}
