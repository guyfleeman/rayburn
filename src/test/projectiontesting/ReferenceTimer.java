package test.projectiontesting;

import org.lwjgl.Sys;

/**
 * @author Will Stuckey
 * @date 11/14/13
 * <p></p>
 */
public class ReferenceTimer
{
	private final long startTime;
	private long lastTime;
	private long interval = 1L;

    private String name = "Timer";

    /**
     * Initializes the timer
     */
	public ReferenceTimer()
	{
		startTime = getTime();
		lastTime = startTime;
	}

    public ReferenceTimer(String name)
    {
        this();
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Updates the timer. Should be called once per render cycle, at the beginning.
     */
	public void update()
	{
		long temp = getTime();
		interval = temp - lastTime;
		if (interval == 0)
			interval = 1;
		lastTime = temp;
	}

	public long getIntervalLong()
	{
		return interval;
	}

	public int getIntervalInt()
	{
		return (int) interval;
	}

    public long getStartTime()
    {
        return startTime;
    }

    public long getTime()
    {
        long time = (Sys.getTime() * 1000) / Sys.getTimerResolution();
        return time == 0L ? 1L : time;
    }
}