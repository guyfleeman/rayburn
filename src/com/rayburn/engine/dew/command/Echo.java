package com.rayburn.engine.dew.command;

/**
 * @author guyfleeman
 *
 * <b>This class describes the echo command.</b>
 */
public class Echo extends Command
{
	/**
	 * Constructor sets the name and description in the super class.
	 */
	public Echo()
	{
		super("echo", "echo", "This command echos text. Used to check for functionality.", "echo <text>");
	}

	/**
	 * This method contains the code to execute the "echo" command.
	 */
	public void execute()
	{
		String line = "";
		for (String arg : getArgs())
			line += arg;

		System.out.println(line);
	}
}
