package rayburn.engine.util;

/**
 * @author Will Stuckey
 * @date 11/26/13
 * <p>Class containing generic, engine-related utility methods and constants.</p>
 */
public final class EngineUtil
{
	/**
	 * Engine states
	 */
	public static enum engineState
	{
		CREATE_DISPLAY,
		DESTROY_DISPLAY,
		DESTROY_ENGINE;
	}

	public static enum aspectRatio
	{
		FOUR_TO_THREE,
		SIXTEEN_TO_NINE,
		ONE_TO_ONE
	}

	public static enum fourToThreeKnownResolutions
	{

	}

	public static enum sixteenToNineKnownResolutions
	{

	}

	public static enum oneToOneKnownResolutions
	{
		x256_256,
		x512_512,
		x600_600,
		x800_800,
		x1000_1000,
		x1024_1024;
	}

	private EngineUtil() {}
}
