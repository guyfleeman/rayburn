package com.rayburn.engine.util.debug.console;

import com.rayburn.Rayburn;
import com.rayburn.engine.WindowManager;
import com.rayburn.engine.dew.DewException;
import com.rayburn.engine.dew.binding.AbstractDewEntityBinding;
import com.rayburn.engine.util.interfaces.Drawable2D;
import com.rayburn.engine.util.interfaces.Initializable;
import com.rayburn.engine.util.interfaces.Triggerable;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.Color;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glVertex2f;

/**
 * @author willstuckey
 * @date 10/9/14
 * <p>This class is the framework upon which in-render consoles for the Rayburn engine are built</p>
 */
public abstract class EngineConsole extends AbstractDewEntityBinding implements Initializable, Triggerable, Drawable2D
{
	private TrueTypeFont consoleFont;

	@NotNull
	protected char[]   inputBuffer;
	@NotNull
	protected String[] outputBuffer;
	@Nullable
	protected String[] commandHistory;

	protected WindowManager windowManager;

	public final String consoleHeader = ">> ";

	public static float fontSize = 16f;
	public static float buffer   = 3f;
	public static float xOffset  = 5f;
	public static float yOffset  = 5f;
	public static String fontLocation = "com/rayburn/rsc/fonts/ubuntu/UbuntuMono-B.ttf";

	/**
	 * Default constructor. This instance's DewEntityBindingID is "Console"
	 * @param windowManager the window manager handling the display for the instance of the engine to which this console
	 *                      will be attached.
	 */
	public EngineConsole(WindowManager windowManager)
	{
		this(windowManager, "Console");
	}

	/**
	 * Default constructor. This instance's DewEntityBindingID is value provided in the parameter id
	 * @param windowManager the window manager handling the display for the instance of the engine to which this console
	 *                      will be attached.
	 * @param id the ID for this instance of DewEntityBinding
	 */
	public EngineConsole(WindowManager windowManager, String id)
	{
		super(id);
		this.windowManager = windowManager;
		inputBuffer = new char[256];
		outputBuffer = new String[50];
		commandHistory = new String[50];
	}

	/**
	 * Advanced constructor. This instance's DewEntityBindingID is "Console"
	 * @param windowManager the window manager handling the display for the instance of the engine to which this console
	 *                      will be attached.
	 * @param inputBufferLen the char limit of the keyboard input buffer
	 * @param outputBufferLen the String count limit of the output on the screen
	 * @param commandHistoryBufferLen the String count limit of the command history, set to zero to avoid commandHistory
	 *                                memory allocation
	 * @throws DewException if any array length provided is negative
	 */
	public EngineConsole(WindowManager windowManager,
	                     int inputBufferLen,
	                     int outputBufferLen,
	                     int commandHistoryBufferLen) throws DewException
	{
		this(windowManager, "Console", inputBufferLen, outputBufferLen, commandHistoryBufferLen);
	}

	/**
	 * This instance's DewEntityBindingID is value provided in the parameter id
	 * @param windowManager the window manager handling the display for the instance of the engine to which this console
	 *                      will be attached.
	 * @param id the ID for this instance of DewEntityBinding
	 * @param inputBufferLen the char limit of the keyboard input buffer
	 * @param outputBufferLen the String count limit of the output on the screen
	 * @param commandHistoryBufferLen the String count limit of the command history, set to zero to avoid commandHistory
	 *                                memory allocation
	 * @throws DewException if any array length provided is negative
	 */
	public EngineConsole(WindowManager windowManager,
	                     String id,
	                     int inputBufferLen,
	                     int outputBufferLen,
	                     int commandHistoryBufferLen) throws DewException
	{
		super(id);
		this.windowManager = windowManager;

		if (inputBufferLen < 0 || outputBufferLen < 0 || commandHistoryBufferLen < 0)
		{
			throw new DewException("Input buffers lengths cannot be negative");
		}

		inputBuffer    = new char[inputBufferLen];
		outputBuffer   = new String[outputBufferLen];

		if (commandHistoryBufferLen == 0)
		{
			commandHistory = null;
		}
		else
		{
			commandHistory = new String[commandHistoryBufferLen];
		}
	}

	/**
	 * This framework relies on utilities that require an opengl context to be active before they can load resources the
	 * console needs to render font. As such, this is not called in the constructor; it is likely the engine will need
	 * to partially initialize and gather resources before it is initialized. Once the engine and its opengl context are
	 * up, the console may be initialized. This framework includes the code to render the console to the screen.
	 */
	public void initialize()
	{
		Font consoleFont = null;
		try
		{
			consoleFont = Font.createFont(Font.TRUETYPE_FONT, Rayburn.class.getClassLoader()
			                                                               .getResourceAsStream(fontLocation));
			consoleFont = consoleFont.deriveFont(EngineConsole.fontSize);
		}
		catch (IOException | FontFormatException e)
		{
			System.out.println("ya got the dir wrong again");
		}

		this.consoleFont = new TrueTypeFont(consoleFont, true);

		for (int index = 0; index < this.inputBuffer.length; index++)
		{
			this.inputBuffer[index] = '\0';
		}

		for (int index = 0; index < this.outputBuffer.length; index++)
		{
			this.outputBuffer[index] = "";
		}

		if (commandHistory != null)
		{
			for (int index = 0; index < this.commandHistory.length; index++)
			{
				this.commandHistory[index] = "";
			}
		}
	}

	/**
	 * Iterative method used to update the rendering of the display. This class implements Drawable2D, so the context
	 * needs to meet the requirements of that interface before this draw call is made.
	 *
	 * GL_DEPS:
	 * GL_TEXTURE_2D
	 * GL_BLEND
	 * GL_ALPHA
	 */
	public void draw()
	{
		//TODO implement end of line scrolling
		consoleFont.drawString(xOffset,
		                       getYPos(1),
		                       consoleHeader + new String(inputBuffer),
		                       Color.white);

		glPushMatrix();
		glDisable(GL_TEXTURE_2D);
		glBegin(GL_LINES);
		glLineWidth(2.0f);
		glVertex2f(xOffset,
		           windowManager.getHeight() - yOffset - consoleFont.getLineHeight() - buffer - 1);
		glVertex2f(windowManager.getWidth() - xOffset,
		           windowManager.getHeight() - yOffset - consoleFont.getLineHeight() - buffer - 1);
		glEnd();
		glPopMatrix();
		glEnable(GL_TEXTURE_2D);

		for (int index = 0; index < outputBuffer.length; index++)
		{
			consoleFont.drawString(xOffset,
			                       getYPos(index + 2),
			                       outputBuffer[index],
			                       Color.white);
		}
	}

	/*
	 * calculates the vertical position of the text rows
	 */
	private float getYPos(int count)
	{
		if (count <= 0)
		{
			return 0f;
		}

		return  this.windowManager.getHeight() -
				EngineConsole.yOffset -
				(this.consoleFont.getLineHeight() * count) -
				(EngineConsole.buffer * count);
	}
}
