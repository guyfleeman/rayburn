package rayburn.engine.util;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author Will Stuckey
 * @date 2/28/14
 * <p></p>
 */
public class Logger
{
	/**
	 * Is the logger actively logging
	 */
	public boolean isLogging = true;

	/**
	 * Is the logger logging to System.out as well
	 */
	public boolean duplicateLogToSystemOut = false;

	/**
	 * Is the logger recording the time stamp of events
	 */
	public boolean isUsingTimeStamp = true;

	/**
	 * Force the logger to sync events to the stream, (e.g. System.out is not thread safe. Should it be synchronized?)
	 */
	public boolean forceLogToSyncPrint = false;

	/**
	 * The object used for synchronization.
	 */
	public final Object logPrintSyncObj = new Object();

	private int loggingLevel = 1;
	private PrintWriter logWriter = null;
	private Calendar timer = new GregorianCalendar();
	private SimpleDateFormat logTimeFormat = new SimpleDateFormat("hh:mm:ss");

	/**
	 * Log levels.
	 */
	public enum LogLevel
	{
		INFO,
		WARNING,
		SEVERE,
		FATAL;
	}

	/**
	 * Default Constructor. Initializes the logger to an unsynchronized, appending System.out.
	 */
	public Logger()
	{
		this(new PrintWriter(System.out));
	}

	/**
	 * Secondary constructor.Initializes the logger to the unsynchronized PrintWriter given.
	 * @param logWriter
	 */
	public Logger(PrintWriter logWriter)
	{
		this.logWriter = logWriter;
	}

	/**
	 * Closes the stream and logs the event, before GC finalize call is made.
	 * @throws Throwable
	 */
	public void finalize() throws Throwable
	{
		log(LogLevel.WARNING, "Closing logger stream. GC called on logger.");
		logWriter.close();
		super.finalize();
	}

	/**
	 * Sets the time format of the logger.
	 * @param logTimeFormat
	 */
	public void setLogTimeFormat(SimpleDateFormat logTimeFormat)
	{
		this.logTimeFormat = logTimeFormat;
	}

	/**
	 * Set the minimium level event to log
	 * @param loggingLevel minimum logging level
	 */
	public void setLoggingLevel(LogLevel loggingLevel)
	{
		this.loggingLevel = getComparableLevel(loggingLevel);
	}

	/**
	 * Logs message.
	 * @param logLevel The notification level
	 * @param message The message
	 */
	public void log(LogLevel logLevel, String message)
	{
		log(logLevel, message, false);
	}

	/**
	 * Logs message.
	 * @param logLevel The notification level
	 * @param message The message
	 * @param overrideLoggingStatus Override the isLogging status
	 */
	public void log(LogLevel logLevel, String message, boolean overrideLoggingStatus)
	{
		String log =
				isUsingTimeStamp
				? "[" + logLevel.toString() + "]" + "[" + logTimeFormat.format(timer.getTime()) + "] ###" + message
				: "[" + logLevel.toString() + "] ###" + message;

		if ((isLogging || overrideLoggingStatus) && getComparableLevel(logLevel) >= loggingLevel)
		{
			if (forceLogToSyncPrint)
			{
				synchronized (logPrintSyncObj)
				{
					logWriter.println(log);
					logWriter.flush();

					if (duplicateLogToSystemOut)
					{
						System.out.println(log);
					}
				}
			}
			else
			{
				logWriter.println(log);
				logWriter.flush();

				if (duplicateLogToSystemOut)
				{
					System.out.println(log);
				}
			}
		}
	}

	/**
	 * Converts enum to comparable integer value
	 * @param logLevel log level
	 * @return comparable log level
	 */
	protected static int getComparableLevel(LogLevel logLevel)
	{
		if (logLevel == LogLevel.INFO)
			return 1;
		else if (logLevel == LogLevel.WARNING)
			return 2;
		else if (logLevel == LogLevel.SEVERE)
			return 3;
		else
			return 4;
	}
}
