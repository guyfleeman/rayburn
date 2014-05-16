package rayburn.engine.dew.command;

/**
 * @author guyfleeman
 *
 * <b>This class describes the exit command.</b>
 */
public class Exit extends Command
{
	/**
	 * Constructor sets the name and description in the super class.
	 */
	public Exit()
	{
		super("exit", "exit", "This command will stop the current process with exit code\"1000.\"", "exit");
	}

	/**
	 * This method contains the code to execute the "exit" command.
	 */
	public void execute()
	{
		System.exit(1000);
	}
}
