package rayburn.engine.dew.lang;

import rayburn.engine.dew.lang.DewInterpreter.DewParser.SyntaxBranch;

/**
 * @author Will Stuckey
 * @date 3/1/14
 * <p></p>
 */
public class DewScript
{
	private String name;

	public DewScript(String name, SyntaxBranch scriptSyntaxTree)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void call()
	{
		//TODO call script
	}
}
