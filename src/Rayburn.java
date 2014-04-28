import org.apache.commons.lang3.mutable.MutableInt;
import rayburn.engine.dew.lang.DewInterpreter.*;
import rayburn.engine.dew.lang.DewInterpreter.DewLexer.*;
import rayburn.engine.dew.lang.DewInterpreter.DewLexer.LexicalTokens.*;
import rayburn.engine.dew.lang.DewInterpreter.DewParser.*;
import rayburn.engine.dew.lang.DewInterpreter.DewParser.ParseTokens.*;
import test.gimballock.PolyEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Stack;
import java.util.Queue;
import java.util.regex.Pattern;

import static rayburn.engine.dew.lang.DewInterpreter.DewParser.AlgebraicExpressionBranch.*;

/**
 * @author Will Stuckey
 * @date 10/20/13
 * <p>The runner used for testing and output of project Rayburn.</p>
 */
public class Rayburn
{
	public static void main(String[] args) throws Exception
	{
		//System.out.println(Integer.class);

		new PolyEngine(true, false);

		/*
		String myString = "1+2-4";
		List<String> split = Arrays.asList(myString.split("(\\+)|(\\-)"));
		split.forEach(System.out::println);
		*/

		//System.out.println(AlgebraicExpressionBranch.isNumber("0.25"));

        //scriptTest();

		//printProjectBuildSize();
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
		getBuildSizeInfo(new File("C:\\Users\\Will Stuckey\\Dropbox\\PROGRAMMING\\Java\\EngineDev\\src"), 0L);
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