package com.normalizedinsanity.rayburn.engine;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

/**
 * @author willstuckey
 * @date 9/28/14 <p></p>
 */
public class RayburnWindowManager extends WindowManager
{
	public static boolean glxFallbackEnabled = true;

	public RayburnWindowManager()
	{
		super();
	}

	public RayburnWindowManager(String objectID)
	{
		super(objectID);
	}

	public void createWindow()
	{
		createWindow(false);
	}

	protected void createWindow(boolean GLXFallback)
	{
		try
		{
			update();

			if (GLXFallback)
			{
				Display.create(new PixelFormat(super.alpha.getPropertyValue(),
				                               super.depth.getPropertyValue(),
				                               super.stencil.getPropertyValue()));
			}
			else
			{
				Display.create(new PixelFormat(super.bitsPerPixel.getPropertyValue(),
				                               super.alpha.getPropertyValue(),
				                               super.depth.getPropertyValue(),
				                               super.stencil.getPropertyValue(),
				                               super.samples.getPropertyValue(),
				                               super.numAuxBuffers.getPropertyValue(),
				                               super.accumBitsPerPixel.getPropertyValue(),
				                               super.accumAlpha.getPropertyValue(),
				                               super.stereo.getPropertyValue(),
				                               super.floatingPoint.getPropertyValue()));
			}

			Display.setInitialBackground(1, 1, 1);
			Display.update();
		}
		catch (LWJGLException e)
		{
			if (glxFallbackEnabled && !GLXFallback && e.getMessage().equalsIgnoreCase("Could not choose GLX13 config"))
			{
				super.lastWarningMessage = "[EX] failed to create window: " + e.getMessage() + ".\n" +
						"trying GLX fallback mode (check err on fail)...";
				System.out.println(super.lastWarningMessage);
				createWindow(true);
			}
			else
			{
				super.lastErrorMessage = "[EX] failed to create window: " + e.getMessage();
				System.out.println(super.lastErrorMessage);
			}
		}
	}

	public void recreateWindow()
	{
		destroyWindow();
		createWindow();
	}

	public void destroyWindow()
	{
		Display.destroy();
	}

	public void update()
	{
		try
		{
			if (Display.getWidth() != super.width.getPropertyValue()
					|| Display.getHeight() != super.height.getPropertyValue())
			{
				Display.setDisplayMode(new DisplayMode(super.width.getPropertyValue(), super.height.getPropertyValue()));
			}

			if (Boolean.parseBoolean(System.getProperty("org.lwjgl.opengl.Window.undecorated"))
					== super.windowed.getPropertyValue())
			{
				System.setProperty("org.lwjgl.opengl.Window.undecorated",
				                   Boolean.toString(!super.windowed.getPropertyValue()));
			}

			Display.setVSyncEnabled(super.vsync.getPropertyValue());
		}
		catch (LWJGLException e)
		{
			super.lastErrorMessage = "[EX] failed to create window: " + e.getMessage();
		}
	}

	@Override
	public void setResolution(int width,
	                          int height)
	{
		super.setResolution(width, height);
		update();
	}

	@Override
	public void fullResolution()
	{
		super.fullResolution();
		update();
	}

	public byte setFullscreen(boolean fullscreen)
	{
		if (fullscreen)
		{
			if (this.width.getPropertyValue() == Display.getWidth() &&
					this.height.getPropertyValue() == Display.getHeight() &&
					this.fullscreen.getPropertyValue() == Display.isFullscreen())
			{
				return 1;
			}

			try
			{
				int frequency = 0;
				DisplayMode targetMode = null;
				DisplayMode[] availableModes = Display.getAvailableDisplayModes();

				for (DisplayMode mode : availableModes)
				{
					if (mode.getWidth() == super.width.getPropertyValue()
							&& mode.getHeight() == super.height.getPropertyValue())
					{
						if (mode.getBitsPerPixel() == Display.getDesktopDisplayMode().getBitsPerPixel() &&
								mode.getFrequency() == Display.getDesktopDisplayMode().getFrequency())
						{
							targetMode = mode;
							break;
						}

						if (targetMode == null || mode.getFrequency() >= frequency)
						{
							if (targetMode == null || mode.getBitsPerPixel() > targetMode.getBitsPerPixel())
							{
								targetMode = mode;
								frequency = targetMode.getFrequency();
							}
						}
					}
				}

				if (targetMode == null)
				{
					lastErrorMessage = "[FAIL] cannot find compatible mode with resolution w=" + width + ", " +
							"h=" + height + "\ncheck aspect ratio";
					return 2;
				}

				Display.setDisplayMode(targetMode);
				Display.setFullscreen(true);
			}
			catch (LWJGLException e)
			{
				lastErrorMessage = "[EX] cannot go fullscreen: " + e.getMessage();
				return 3;
			}
		}
		else
		{
			try
			{
				Display.setFullscreen(false);
			}
			catch (LWJGLException e)
			{
				lastErrorMessage = "[EX] cannot fall back from fullscreen: " + e.getMessage();
				return 4;
			}

		}

		this.fullscreen.setPropertyValue(fullscreen);
		Display.update();

		return 0;
	}

	public void setWindowed(boolean windowed)
	{
		this.windowed.setPropertyValue(windowed);
		update();
	}

	public void setVsync(boolean vsync)
	{
		this.vsync.setPropertyValue(vsync);
		update();
	}

	public void setTitle(String title)
	{
		Display.setTitle(title);
	}

	public static String fullscreenReturnToString(byte returnCode)
	{
		switch (returnCode)
		{
			case 0: return "[ OK ] requested operation completed without warnings";
			case 1: return "[NOOP] requested operation is already in place";
			case 2: return "[FAIL] cannot find compatible mode with resolution. check aspect ratio";
			case 3: return "[ EX ] cannot go fullscreen";
			case 4: return "[ EX ] cannot fall back from fullscreen";
			default: return "[UNKN] unrecognized return code";
		}
	}
}
