package com.normalizedinsanity.rayburn.engine;

import com.normalizedinsanity.rayburn.engine.dew.binding.AbstractDewEntityBinding;
import com.normalizedinsanity.rayburn.engine.dew.binding.DewPropertyBindingBoolean;
import com.normalizedinsanity.rayburn.engine.dew.binding.DewPropertyBindingInt;

import java.awt.*;

/**
 * @author willstuckey
 * @date 9/28/14 <p></p>
 */
public abstract class WindowManager extends AbstractDewEntityBinding
{
	protected DewPropertyBindingBoolean windowed      = new DewPropertyBindingBoolean("windowed",   true);
	protected DewPropertyBindingBoolean fullscreen    = new DewPropertyBindingBoolean("fullscreen", false);
	protected DewPropertyBindingBoolean vsync         = new DewPropertyBindingBoolean("vsync",      true);
	protected DewPropertyBindingBoolean stereo        = new DewPropertyBindingBoolean("stereo",     false);
	protected DewPropertyBindingBoolean floatingPoint = new DewPropertyBindingBoolean("fp",         false);
	protected DewPropertyBindingInt width             = new DewPropertyBindingInt("width",   640);
	protected DewPropertyBindingInt height            = new DewPropertyBindingInt("height",  480);
	protected DewPropertyBindingInt bitsPerPixel      = new DewPropertyBindingInt("bpp",     24);
	protected DewPropertyBindingInt alpha             = new DewPropertyBindingInt("alpha",   8);
	protected DewPropertyBindingInt depth             = new DewPropertyBindingInt("depth",   8);
	protected DewPropertyBindingInt stencil           = new DewPropertyBindingInt("stencil", 8);
	protected DewPropertyBindingInt samples           = new DewPropertyBindingInt("samples", 8);
	protected DewPropertyBindingInt numAuxBuffers     = new DewPropertyBindingInt("nab",     8);
	protected DewPropertyBindingInt accumBitsPerPixel = new DewPropertyBindingInt("abpp",    24);
	protected DewPropertyBindingInt accumAlpha        = new DewPropertyBindingInt("aa",      8);

	protected String lastWarningMessage;
	protected String lastErrorMessage;

	protected WindowManager()
	{
		this("WindowManager");
	}

	protected WindowManager(String objectID)
	{
		super(objectID);
		super.addProperties(windowed,
		                    fullscreen,
		                    vsync,
		                    stereo,
		                    floatingPoint,
		                    width,
		                    height,
		                    bitsPerPixel,
		                    alpha,
		                    depth,
		                    stencil,
		                    samples,
		                    numAuxBuffers,
		                    accumBitsPerPixel,
		                    accumAlpha);
	}

	/*
	 * Abstract methods
	 */

	public abstract void createWindow();

	public abstract void recreateWindow();

	public abstract void destroyWindow();

	public abstract void update();

	/*
	 * Implemented methods
	 */

	public void setResolution(int width,
	                          int height)
	{
		this.width.setPropertyValue(width);
		this.height.setPropertyValue(height);
	}

	public void fullResolution()
	{
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screen = toolkit.getScreenSize();
		this.width.setPropertyValue((int) screen.getWidth());
		this.height.setPropertyValue((int) screen.getHeight());
	}

	public abstract void setTitle(String title);

	public String getLastWarningMessage()
	{
		return lastWarningMessage;
	}

	public String getLastErrorMessage()
	{
		return lastErrorMessage;
	}

	/*
	 * Accessors
	 */

	public boolean isWindowed()
	{
		return this.windowed.getPropertyValue();
	}

	public void setWindowed(boolean windowed)
	{
		this.windowed.setPropertyValue(windowed);
	}

	public boolean isFullscreen()
	{
		return this.fullscreen.getPropertyValue();
	}

	public byte setFullscreen(boolean fullscreen)
	{
		this.fullscreen.setPropertyValue(fullscreen);
		return Byte.MAX_VALUE;
	}

	public boolean isVsync()
	{
		return this.vsync.getPropertyValue();
	}

	public void setVsync(boolean vsync)
	{
		this.vsync.setPropertyValue(vsync);
	}

	public boolean isStereo()
	{
		return this.stereo.getPropertyValue();
	}

	public void setStereo(boolean stereo)
	{
		this.stereo.setPropertyValue(stereo);
	}

	public boolean isFloatingPoint()
	{
		return this.floatingPoint.getPropertyValue();
	}

	public void setFloatingPoint(boolean floatingPoint)
	{
		this.floatingPoint.setPropertyValue(floatingPoint);
	}

	public int getWidth()
	{
		return this.width.getPropertyValue();
	}

	public void setWidth(int width)
	{
		this.width.setPropertyValue(width);
	}

	public int getHeight()
	{
		return this.height.getPropertyValue();
	}

	public void setHeight(int height)
	{
		this.height.setPropertyValue(height);
	}

	public int getBitsPerPixel()
	{
		return this.bitsPerPixel.getPropertyValue();
	}

	public void setBitsPerPixel(int bitsPerPixel)
	{
		this.bitsPerPixel.setPropertyValue(bitsPerPixel);
	}

	public int getAlpha()
	{
		return this.alpha.getPropertyValue();
	}

	public void setAlpha(int alpha)
	{
		this.alpha.setPropertyValue(alpha);
	}

	public int getDepth()
	{
		return this.depth.getPropertyValue();
	}

	public void setDepth(int depth)
	{
		this.depth.setPropertyValue(depth);
	}

	public int getStencil()
	{
		return this.stencil.getPropertyValue();
	}

	public void setStencil(int stencil)
	{
		this.stencil.setPropertyValue(stencil);
	}

	public int getSamples()
	{
		return this.samples.getPropertyValue();
	}

	public void setSamples(int samples)
	{
		this.samples.setPropertyValue(samples);
	}

	public int getNumAuxBuffers()
	{
		return this.numAuxBuffers.getPropertyValue();
	}

	public void setNumAuxBuffers(int numAuxBuffers)
	{
		this.numAuxBuffers.setPropertyValue(numAuxBuffers);
	}

	public int getAccumBitsPerPixel()
	{
		return this.accumBitsPerPixel.getPropertyValue();
	}

	public void setAccumBitsPerPixel(int accumBitsPerPixel)
	{
		this.accumBitsPerPixel.setPropertyValue(accumBitsPerPixel);
	}

	public int getAccumAlpha()
	{
		return this.accumAlpha.getPropertyValue();
	}

	public void setAccumAlpha(int accumAlpha)
	{
		this.accumAlpha.setPropertyValue(accumAlpha);
	}
}
