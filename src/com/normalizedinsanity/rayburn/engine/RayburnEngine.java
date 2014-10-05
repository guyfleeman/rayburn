package com.normalizedinsanity.rayburn.engine;

import com.normalizedinsanity.rayburn.engine.dew.binding.AbstractDewEntityBinding;
import org.lwjgl.opengl.Display;

/**
 * @author willstuckey
 * @date 9/28/14 <p></p>
 */
public class RayburnEngine extends AbstractDewEntityBinding
{
	protected boolean running;

	protected WindowManager windowManager = new RayburnWindowManager();

	public RayburnEngine()
	{
		super("Engine");
		super.addSubObject(windowManager);


		windowManager.createWindow();
		Display.update();
		sleep();

		System.exit(1);
	}

	protected void main()
	{

	}

	public void sleep()
	{
		try
		{
			Thread.sleep(5 * 1000);
		}
		catch (Exception e) {}
	}

	protected void render()
	{

	}
}
