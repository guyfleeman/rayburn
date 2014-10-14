package com.rayburn;

import com.rayburn.engine.RayburnEngine;
import com.rayburn.engine.RayburnEngineBuilder;
import com.rayburn.engine.dew.DewException;
import com.rayburn.engine.dew.lang.DewLexer;
import com.rayburn.engine.dew.lang.DewParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Stack;

/**
 * @author Will Stuckey
 * @date 10/20/13
 * <p>The runner used for testing and output of project Rayburn</p>
 */
public class Rayburn
{
	public static void main(String[] args) throws Exception
	{
		//printProjectBuildSize();

		RayburnEngineBuilder.attachDefaultInitRoutine();
		RayburnEngine engine = RayburnEngineBuilder.createDefaultRayburnEngine();
		engine.run();
	}

	public static void scriptTest() throws DewException
	{
		DewLexer dewLexer = DewLexer.getInstance();
		DewParser dewParser = DewParser.getInstance();
		DewLexer.isPrintingTokenization = true;
		DewLexer.lexerLogger.isLogging = true;

		Stack tokenizedScript = dewLexer.tokenizeAcc(new File("C:\\Users\\Will Stuckey\\Desktop\\HelloWorld.dew"));
		Stack debug = (Stack) tokenizedScript.clone();
		dewParser.parse(tokenizedScript);
	}

	/*
	 * Project scope info finder. Just for fun ;)
	 */
	private static long fileCt, lineCt, charCt, maxDepth;
	public static void printProjectBuildSize()
	{
		getBuildSizeInfo(new File("/home/willstuckey/Dropbox/PROGRAMMING/Java/EngineDev/src"), 0L);
		System.out.println("RAYBURN SCOPE STATISTICS: numFiles=" + fileCt
				+ ", numLines=" + lineCt
				+ ", numChars=" + charCt
				+ ", level=" + maxDepth);
	}

	private static void getBuildSizeInfo(File dir, long depth)
	{
		if (dir == null)
			return;

		File[] files = dir.listFiles();
		if (files == null)
			return;

		for (File f : files)
		{
			if (f.isDirectory())
			{
				if (depth + 1 > maxDepth)
					maxDepth = depth + 1;

				System.out.println("Found sub level at depth " + depth + ". Moving to depth " +  (depth + 1));
				getBuildSizeInfo(f, depth + 1);
			}
			else if (f.isFile())
			{
				fileCt++;

				try
				{
					Scanner fileScanner = new Scanner(f);
					while (fileScanner.hasNextLine())
					{
						String line = fileScanner.nextLine();
						lineCt++;
						charCt += line.length();
					}
				}
				catch (FileNotFoundException e) {}

				System.out.println("AccLn: " + lineCt);
			}
		}
	}
}
