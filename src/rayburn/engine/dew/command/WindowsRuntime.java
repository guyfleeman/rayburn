package rayburn.engine.dew.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author guyfleeman
 *
 * <b>This class describes the windows runtime (wrt) command.</b>
 */
public class WindowsRuntime extends Command
{
	/**
	 * Constructor sets the name and description in the super class.
	 */
	public WindowsRuntime()
	{
		super("Windows Runtime Command", "wrt", "This command will execute a windows runtime command via a hidden cmd shell.", "wrt <runtime command> <runtime command args...>");
	}

	/**
	 * This method contains the code to execute the "wrt" command. It does so via a hidden shell and can return the standard and error returns of the command(s) entered.
	 */
	public void execute()
	{
		String[] args = getArgs();
		String commandLine = "";
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equalsIgnoreCase("powershell")
					|| args[i].equalsIgnoreCase("ps")
					|| args[i].equalsIgnoreCase("powershell.exe")
					|| args[i].equalsIgnoreCase("cmd")
					|| args[i].equalsIgnoreCase("cmd.exe"))
			{
                System.out.println("A subshell cannot be invoked. The command line will freeze.");
				return;
			}
			commandLine += args[i] + " ";
		}

		try
		{
			Process process = Runtime.getRuntime().exec("cmd /c " + commandLine);

			BufferedReader standardReturn = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader errorReturn = new BufferedReader(new InputStreamReader(process.getErrorStream()));

			String commandReturn;
			while ((commandReturn = standardReturn.readLine()) != null)
				System.out.println(commandReturn);

			while ((commandReturn = errorReturn.readLine()) != null)
				System.out.println(commandReturn);

			standardReturn.close();
			errorReturn.close();
		}
		catch (IOException e) { System.out.println(e.toString()); }
	}
}
