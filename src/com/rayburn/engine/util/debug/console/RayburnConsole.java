package com.rayburn.engine.util.debug.console;

import com.rayburn.engine.WindowManager;
import com.rayburn.engine.dew.DewException;
import com.rayburn.engine.util.interfaces.CommandBuffer;

import com.rayburn.engine.util.interfaces.Drawable2D;
import com.sun.istack.internal.Nullable;

/**
 * @author willstuckey
 * @date 10/9/14
 * <p>This class represents an implementation of the EngineConsole class designed to be used with the Rayburn Engine
 * system.</p>
 */
public class RayburnConsole extends EngineConsole implements CommandBuffer
{
	/**
	 * Default constructor. This instance's DewEntityBindingID is "RayCon"
	 * @param windowManager the window manager handling the display for the instance of the engine to which this console
	 *                      will be attached.
	 */
	public RayburnConsole(WindowManager windowManager)

	{
		super(windowManager, "RayCon");
	}

	/**
	 * Advanced constructor. This instance's DewEntityBindingID is "RayCon"
	 * @param windowManager the window manager handling the display for the instance of the engine to which this console
	 *                      will be attached.
	 * @param inputBufferLen the char limit of the keyboard input buffer
	 * @param outputBufferLen the String count limit of the output on the screen
	 * @param commandHistoryBufferLen the String count limit of the command history, set to zero to avoid commandHistory
	 *                                memory allocation
	 * @throws DewException if any array length provided is negative
	 */
	public RayburnConsole(WindowManager windowManager,
	                      int inputBufferLen,
	                      int outputBufferLen,
	                      int commandHistoryBufferLen) throws DewException
	{
		super(windowManager, "RayCon", inputBufferLen, outputBufferLen, commandHistoryBufferLen);
	}

	/**
	 * Invoking this method invokes the input buffer handler mechanism. It will process the command, add it to command
	 * history if applicable, and then print the output to the screen.
	 */
	public void trigger()
	{
		/*
		 * convert char array to command string and then clear the input buffer
		 */
		String command = new String(super.inputBuffer);
		if (command.replaceAll("\0", "").equalsIgnoreCase("fexitn"))
		{
			System.out.println("force exit now invoked");
			System.exit(1);
		}

		for (int index = 0; index < super.inputBuffer.length; index++)
		{
			super.inputBuffer[index] = '\0';
		}

		/*
		 * rewind the command history buffer and add the latest command to the most current spot
		 */
		for (int index = super.commandHistory.length - 1; index > 0; index--)
		{
			super.commandHistory[index] = super.commandHistory[index - 1];
		}
		super.commandHistory[0] = command;

		/*
		 * add the command to the output (display) buffer
		 */
		for (int index = super.outputBuffer.length - 1; index > 0; index--)
		{
			super.outputBuffer[index] = super.outputBuffer[index - 1];
		}
		super.outputBuffer[0] = consoleHeader + command;

		//TODO
		//process input buffer as command in a command processor
		//add command processor output to output buffer
	}

	/**
	 * @return the input buffer
	 */
	public char[] getInputBuffer()
	{
		return super.inputBuffer;
	}

	/**
	 * clears the input buffer
	 */
	public void clearInputBuffer()
	{
		for (int index = 0; index < super.inputBuffer.length; index++)
		{
			inputBuffer[index] = '\0';
		}
	}

	/**
	 * @return the output buffer
	 */
	public String[] getOutputBuffer()
	{
		return super.outputBuffer;
	}

	/**
	 * clears the output buffer
	 */
	public void clearOutputBuffer()
	{
		for (int index = 0; index < super.outputBuffer.length; index++)
		{
			outputBuffer[index] = "";
		}
	}

	/**
	 * @return command history
	 */
	public String[] getCommandHistory()
	{
		return super.commandHistory;
	}

	/**
	 * clears command history
	 */
	public void clearCommandHistory()
	{
		if (super.commandHistory != null)
		{
			for (int index = 0; index < super.commandHistory.length; index++)
			{
				commandHistory[index] = "";
			}
		}
	}
}
