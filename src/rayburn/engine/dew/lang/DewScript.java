package rayburn.engine.dew.lang;

import rayburn.engine.dew.lang.DewInterpreter.DewParser.SyntaxBranch;

/**
 * @author Will Stuckey
 * @date 3/1/14
 * <p>Object of this class represent runable parsed source.</p>
 */
public class DewScript
{
	private String name;
	private SyntaxBranch primaryBranch;

	/**
	 * Default constructor.
	 * @param name name of the script
	 * @param primaryBranch the trunk of the syntax tree
	 */
	public DewScript(String name,
	                 SyntaxBranch primaryBranch)
	{
		this.name = name;
		this.primaryBranch = primaryBranch;
	}

	/**
	 * @return the name of the script
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Runs the script using the DewInterpreter
	 * @see rayburn.engine.dew.lang.DewInterpreter
	 */
	public void run()
	{
		primaryBranch.call();
	}
}
