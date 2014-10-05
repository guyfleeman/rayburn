package com.normalizedinsanity.rayburn.engine.dew.command;

//import com.google.common.base.Function;
//import com.google.common.collect.Ordering;


import com.google.common.base.Function;
import com.google.common.collect.Ordering;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author guyfleeman
 *
 * <b>This class describes the help command.</b>
 */
public class Help extends Command
{
	/**
	 * An ArrayList containing all command. It will be sorted and then used to display help information.
	 */
	private ArrayList<Command> commands;

	/**
	 * Constructor sets the name and description in the super class.
	 */
	public Help()
	{
		super("help", "help", "This command lists the names, descriptions, and usages of other command.", "help");
	}

	/**
	 * This method contains the code to execute the "exit" command. It sorts a copy of the command list to display the name, description, and usage in alpha order.
	 */
	public void execute()
	{
		commands = (ArrayList<Command>) getParentConsole().getCommandlist().clone();

		/*
		 * Sort commands alphabetically
		 */
		Collections.sort(commands, Ordering.natural().onResultOf(
				new Function<Command, String>() {
					public String apply(Command c) {
						return c.getName();
					}
				}
		));

		/*
		 * Print the commands
		 */
		for (Command c : commands)
		{
			System.out.println(c.getName() + ":");
			System.out.println(c.getDescription() + "\n");
			System.out.println(c.getUsage() + "\t");
		}
	}
}
