package com.rayburn.engine;

import com.rayburn.Rayburn;
import com.rayburn.engine.dew.Console;

import com.rayburn.engine.util.debug.console.EngineConsole;
import com.rayburn.engine.util.debug.console.RayburnConsole;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class RayburnEngineBuilder
{
	protected static String id = "Engine";

	protected static Document initRoutine = null;
	protected static WindowManager windowManager = null;
	protected static EngineConsole engineConsole = null;

	public static void attachEngineName(String name)
	{
		RayburnEngineBuilder.id = name;
	}

	public static void attachDefaultWindowManager()
	{
		RayburnEngineBuilder.windowManager = new RayburnWindowManager();
	}

	public static void attachWindowManager(WindowManager windowManager)
	{
		RayburnEngineBuilder.windowManager = windowManager;
	}

	public static void attachDefaultInitRoutine() throws RayburnException
	{
		try
		{
			RayburnEngineBuilder.initRoutine = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
					Rayburn.class.getResourceAsStream("rsc/init/rayburn.init"));
		}
		catch (ParserConfigurationException | SAXException | IOException e)
		{
			throw new RayburnException(e);
		}
	}

	public static void attachInitRoutine(File file) throws RayburnException
	{
		try
		{
			RayburnEngineBuilder.initRoutine = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
		}
		catch (ParserConfigurationException | SAXException | IOException e)
		{
			throw new RayburnException(e);
		}
	}

	public static void attachDefaultCommandConsole()
	{
		RayburnEngineBuilder.engineConsole = new RayburnConsole(windowManager);
	}

	public static void attachCommandConsole(EngineConsole engineConsole)
	{
		RayburnEngineBuilder.engineConsole = engineConsole;
	}

	public static RayburnEngine createDefaultRayburnEngine()
	{
		attachDefaultWindowManager();
		attachDefaultCommandConsole();

		return createRayburnEngine();
	}

	public static RayburnEngine createRayburnEngine()
	{
		return new RayburnEngine(id, windowManager, engineConsole, initRoutine);
	}

	public static void clear()
	{
		initRoutine = null;
		windowManager = null;
		engineConsole = null;
	}
}