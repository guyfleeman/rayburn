package com.rayburn.engine;

import com.rayburn.engine.dew.binding.AbstractDewEntityBinding;
import com.rayburn.engine.dew.binding.DewPropertyBindingBoolean;
import com.rayburn.engine.entity.geom.Cube;
import com.rayburn.engine.util.ToggleKey;
import com.rayburn.engine.util.debug.console.EngineConsole;
import com.rayburn.engine.util.debug.console.RayburnConsole;
import com.rayburn.engine.util.debug.console.RayburnConsoleInputRegister;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;
import org.w3c.dom.Document;

import static org.lwjgl.opengl.GL11.GL_ALPHA;
import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11.GL_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glClearDepth;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawBuffer;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;

import static org.lwjgl.opengl.GL11.glScissor;
import static org.lwjgl.opengl.GL11.glShadeModel;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.util.glu.GLU.gluPerspective;

/**
 * @author willstuckey
 * @date 9/28/14 <p></p>
 */
public class RayburnEngine extends AbstractDewEntityBinding implements Runnable
{
	private Cube testCube = new Cube();

	protected boolean running = true;

	protected EngineState engineState = EngineState.RENDER;

	protected WindowManager windowManager    = null;
	protected EngineConsole engineConsole    = null;
	protected Document      initRoutine      = null;
	protected ToggleKey     consoleToggleKey = new ToggleKey();
	protected RayburnConsoleInputRegister rcir = null;

	protected DewPropertyBindingBoolean reverseDefaultRenderOrder = new DewPropertyBindingBoolean("rdro", false);

	protected enum EngineState
	{
		RENDER,
		CONSOLE,
		HALT,
		DEAD
	}

	public RayburnEngine(String id, WindowManager windowManager, EngineConsole engineConsole, Document initRoutine)
	{
		super(id);
		this.windowManager = windowManager;
		this.engineConsole = engineConsole;
		this.initRoutine = initRoutine;

		super.addSubObject(windowManager);
		super.addSubObject(engineConsole);

		windowManager.createWindow();
		windowManager.fullResolution();
		System.out.println(windowManager.setFullscreen(true));
		engineConsole.initialize();
		this.rcir = new RayburnConsoleInputRegister<RayburnConsole>((RayburnConsole) engineConsole);

		testCube.setIncrementRotation(new Vector3f(0.1f, 0.1f, 0.1f));
	}

	public void run()
	{
		while (running)
		{
			switch (engineState)
			{
				case RENDER:
					render();
					break;
				case CONSOLE:
					console();
					break;
				case HALT:
					halt();
					break;
				case DEAD:
					dead();
					break;
			}
		}
	}

	protected void render()
	{
		renderInit();

		while (engineState == EngineState.RENDER)
		{
			globalEntrantIterativeCall();

			preRender();
			if (this.reverseDefaultRenderOrder.getPropertyValue())
			{
				render2DCore();
				render3DCore();
			}
			else
			{
				render3DCore();
				render2DCore();
			}

			windowManager.rUpdate();
			globalExitIterativeCall();
		}
	}

	protected void renderInit()
	{
		glDisable(GL_TEXTURE_2D);

		glEnable(GL_BLEND);
		glEnable(GL_LIGHTING);
		glEnable(GL_CULL_FACE);
		glEnable(GL_DEPTH_TEST);

		glCullFace(GL_BACK);

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		//gluPerspective(45.0f,
		//               ((float) windowManager.getWidth()) / ((float) windowManager.getHeight()),
		//              0.1f,
		//               100.f);

		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
	}

	protected void preRender()
	{
		glClear(GL_COLOR_BUFFER_BIT);
		glClear(GL_DEPTH_BUFFER_BIT);
	}

	protected void render3DCore()
	{
		testCube.draw();
	}

	protected void render2DCore()
	{
		return;
	}

	protected void console()
	{
		consoleInit();

		while (engineState == EngineState.CONSOLE)
		{
			globalEntrantIterativeCall();

			consolePreRender();
			renderConsoleCore();

			windowManager.rUpdate(24);
			globalExitIterativeCall();
		}

		consoleClose();
	}

	protected void consoleInit()
	{
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_LIGHTING);

		glEnable(GL_TEXTURE_2D);
		glEnable(GL_BLEND);
		glEnable(GL_ALPHA);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glShadeModel(GL_SMOOTH);

		glClearColor(0f, 0f, 0f, 0f);
		glClearDepth(1);

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, windowManager.getWidth(), windowManager.getHeight(), 0, 1, -1);

		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();

		rcir.setActive();
	}

	protected void consolePreRender()
	{
		glEnable(GL_SCISSOR_TEST);
		glScissor(0, 0, windowManager.getWidth(), windowManager.getHeight());
		glDisable(GL_SCISSOR_TEST);

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}

	protected void renderConsoleCore()
	{
		engineConsole.draw();
	}

	protected void consoleClose()
	{
		rcir.setInactive();
	}

	protected void halt()
	{
		try
		{
			Thread.sleep(100);
		}
		catch (InterruptedException e)
		{
			engineState = EngineState.DEAD;
		}
	}

	protected void globalEntrantIterativeCall()
	{
		//`System.out.println(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE));
		if (Display.isCloseRequested() ||
				(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)))
		{
			engineState = EngineState.DEAD;
			return;
		}

		//System.out.println(engineState);

		/*
		 * console toggling
		 */
		consoleToggleKey.updateKeyStatus(Keyboard.isKeyDown(Keyboard.KEY_GRAVE));
		if (engineState == EngineState.RENDER && consoleToggleKey.isKeyActive())
		{
			engineState = EngineState.CONSOLE;
		}
		else if (engineState == EngineState.CONSOLE && !consoleToggleKey.isKeyActive())
		{
			engineState = EngineState.RENDER;
		}
	}

	protected void globalExitIterativeCall()
	{

	}

	protected void dead()
	{
		//rcir.setInactive();
		this.running = false;
		windowManager.destroyWindow();
		System.exit(0);
	}
}
