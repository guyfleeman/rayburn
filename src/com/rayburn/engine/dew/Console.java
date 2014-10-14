package com.rayburn.engine.dew;

import com.rayburn.engine.dew.binding.AbstractDewEntityBinding;
import com.rayburn.engine.dew.command.*;

import java.io.PrintStream;
import java.util.ArrayList;

/**
 * @author guyfleeman
 *
 * <b>This class contains the framework for a user defined command addDefaultCommands</b>
 */
public class Console extends AbstractDewEntityBinding
{
	/**
	 * console debug state
	 */
	public static boolean printDebug = false;

	/**
	 * The master list of command
	 */
	private ArrayList<Command> commands = new ArrayList<Command>();

	public Console()
	{
		super("Console");
	}

	public Console(String id)
	{
		super(id);
	}

	/**
	 * Used to load any required command
	 */
	private void addDefaultCommands()
	{
		addCommand(new Exit());
		addCommand(new Help());
		addCommand(new Echo());
	}

	/**
	 * @param commands An ArrayList of Command Objects
	 *
	 * Allows the user to set a new custom list of Command Objects at anytime
	 */
	public void setCommandList(ArrayList<Command> commands)
	{
		this.commands = commands;
	}

	/**
	 * @return An ArrayList of the loaded Command Objects
	 */
	public ArrayList<Command> getCommandlist()
	{
		return commands;
	}

	/**
	 * @param command A Command Object
	 *
	 * Allows the user to add a command to the master command list
	 */
	public void addCommand(Command command)
	{
		boolean exists = false;
	 	for (Command c : commands)
			if (c.getName().equalsIgnoreCase(command.getName()))
				exists = true;

		if (exists)
			System.out.println("Error loading command \"" + command.getName() + "\". Command with that name already exists.");
		else
			commands.add(command);
	}

	/**
	 * Iterates through the list of Command Objects. If a Command Object matching the command line is found, the arguments will be passed and the command invoked. If a file is found, its validity will be checked.
	 * @param commandLine A String of the entire command to be executed
	 */
	public void executeCommand(String commandLine)
	{
		String[] command = commandLine.split(" ");

		boolean found = false;
		for (Command c : commands)
			if (c.getCommand().equalsIgnoreCase(command[0]))
			{
				c.setParentConsole(this);
				c.setArgs(command);

				c.execute();
				return;
			}



		System.out.println("Input not recognized as an internal resource, external resource, or operable command.");
	}

	/**
	 * Sets the output of the console so it can be incorporated into graphical and file based applications;
	 * @param consoleOutputStream PrintStream, the PrintStream the console will print to.
	 */
	public void setConsoleOutput(PrintStream consoleOutputStream)
	{
		System.setOut(consoleOutputStream);
	}

	public void updateProperties()
	{

	}
}
