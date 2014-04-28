package rayburn.engine.dew.command;

import java.io.File;

/**
 * @author Will Stuckey
 *         9/8/13
 *         <p/>
 *         <b></b>
 */
public class Drives extends Command
{
	public Drives()
	{
		super("Print Drives", "drives", "Shows a list of all valid drives.", "drives");
	}

	public void execute()
	{
		File[] roots = File.listRoots();

		for (File f : roots)
		{
			long space = f.getFreeSpace();
			double size;

			if (space < 1024)
				size = Math.floor((((double)space) * 1e3) / 1e3);
			else if (space < 1048576)
				size = Math.floor(((((double)space) + 512.0) / 1024 * 1e3) / 1e3);
			else if (space < 1073741824)
				size = Math.floor(((((double)space) + 524288.0) / 1048576 * 1e3) / 1e3);
			else
				size = Math.floor(((((double)space) + 536870912.0) / 1073741824 * 1e3) / 1e3);

			System.out.println(f.toString() + " ~" + size);
		}
	}
}
