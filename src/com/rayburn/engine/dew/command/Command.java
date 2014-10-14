package com.rayburn.engine.dew.command;

import com.rayburn.engine.dew.Console;

/**
 * @author guyfleeman
 *
 * <b>This abstract class acts as a bridge between a user created command class and the addDefaultCommands class.</b>
 */
public abstract class Command
{
	/**
	 * The name of the command.
	 */
	private final String name;

	/**
	 * The tect used to invoek the command.
	 */
	private final String command;

	/**
	 * The description contains information and usage for the command.
	 */
	private final String description;

	/**
	 * The usage description for the command.
	 */
	private final String usage;

	/**
	 * A String Array of the command's arguments.
	 */
	private String[] args;

	/**
	 * A copy of the master command ArrayList for use by the user created command, if necessary.
	 */
	private Console console;

	/**
	 * @param name A string of the name of the command, also the text used to invoke the command
	 * @param description A string containing the command usage and information
	 *
	 * Default constructor. Initializes required fields and validates name information.
	 */
	public Command(String name, String command, String description, String usage)
	{
		if (name.equalsIgnoreCase("") || name == null)
			this.name = "Name data not found";
		else
			this.name = name;

		if (command.equalsIgnoreCase("") || command == null)
		{
			System.out.println("Command not valid.");
		    this.command = Integer.toString(Integer.MIN_VALUE);
			System.out.println("Command initialized to " + Integer.MAX_VALUE);
		}
		else
			this.command = command;

		if (description.equalsIgnoreCase("") || description == null)
			this.description = "Description data not found.";
		else
			this.description = description;

		if (usage.equalsIgnoreCase("") || usage == null)
			this.usage = "Usage data not found";
		else
			this.usage = usage;
	}

	/**
	 * @return command name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return command
	 */
	public String getCommand()
	{
		return command;
	}

	/**
	 * @return command description
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * @return command usage
	 */
	public String getUsage()
	{
		return usage;
	}

    /**
     * Forces users to implement thier command's execute method linking it to the console
     */
    public abstract void execute();

    /**
     * Set parent console
     * @param console
     */
	public void setParentConsole(Console console)
	{
		this.console = console;
	}

	/**
	 * @return An ArrayList copy of the master command list
	 */
	public Console getParentConsole()
	{
		return console;
	}

	/**
	 * @return A String array of the arguments
	 */
	public String[] getArgs()
	{
		return args;
	}

	/**
	 * @param args A String Array of the command arguemnts
	 */
	public void setArgs(String[] args)
	{
        this.args = new String[args.length - 1];

        for (int i = 1; i < args.length; i++)
            this.args[i - 1] = args[i];
	}
}