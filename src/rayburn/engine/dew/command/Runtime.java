package rayburn.engine.dew.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author guyfleeman
 *
 * <b>This class describes the runtime (rt) command.</b>
 */
public class Runtime extends Command
{
	/**
	 * Constructor sets the name and description in the super class.
	 */
	public Runtime()
	{
		super("Windows Runtime Command",
				"rt",
				"This command will execute a runtime command via a background shell.",
				"rt <runtime command> <runtime command args...>");
	}

	/**
	 * This method contains the code to execute the "rt" command. It does so via a hidden shell and can return the
	 * standard and error returns of the command(s) entered.
	 */
	public void execute()
	{
		String[] args = getArgs();
		String commandLine = "";

		/*
		 * subshells are not currently supported because the subshell IO stream severs the active stream locking the
		 * console.
		 */
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equalsIgnoreCase("powershell")
					|| args[i].equalsIgnoreCase("ps")
					|| args[i].equalsIgnoreCase("powershell.exe")
					|| args[i].equalsIgnoreCase("cmd")
					|| args[i].equalsIgnoreCase("cmd.exe")
					|| args[i].equalsIgnoreCase("bash"))
			{
                System.out.println("A subshell cannot be invoked. The command line will freeze.");
				return;
			}
			commandLine += args[i] + " ";
		}

		try
		{
			//Process process = java.lang.Runtime.getRuntime().exec("cmd /c " + commandLine);
			Process process = java.lang.Runtime.getRuntime().exec(commandLine);

			/*
			 * Open readers to the system's return streams (standard and error)
			 */
			BufferedReader standardReturn = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader errorReturn = new BufferedReader(new InputStreamReader(process.getErrorStream()));

			/*
			 * Print everything from the return streams
			 */
			String commandReturn;
			while ((commandReturn = standardReturn.readLine()) != null)
				System.out.println(commandReturn);

			while ((commandReturn = errorReturn.readLine()) != null)
				System.out.println(commandReturn);

			/*
			 * clean
			 */
			standardReturn.close();
			errorReturn.close();
		}
		catch (IOException e) { System.out.println(e.toString()); }
	}
}
