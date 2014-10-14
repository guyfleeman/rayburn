package com.rayburn.engine.dew.lang;

import com.rayburn.engine.dew.DewException;
import com.rayburn.engine.util.debug.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Stack;

/**
 * @author guyfleeman
 * @date 2/27/14
 * <b>This class is responsible for performing the lexcial analysis frontend of the lang interpreter.</b>
 */
public class DewLexer
{
	private static DewLexer instance;
	public static Logger lexerLogger = new Logger();

	/**
	 * Static iniitalizer creates the default instance of the lexer.
	 */
	static
	{
		instance = new DewLexer();
	}

	/**
	 * Private void constructor prevents the initialization of more than one instance of the lexer.
	 */
	private DewLexer() {}

	/**
	 * Get the instance of the lang lexer
	 * @return lang lexer instance
	 */
	public static DewLexer getInstance()
	{
		return instance;
	}

	///////////////////////////////////
	//  end instance initialization  //
	///////////////////////////////////

	/**
	 * Is the lexer going to print the tokenization of the file after lexical analysis is complete.
	 */
	public static boolean isPrintingTokenization = false;

	/**
	 * TO BE FULLY IMPLEMENTED. Will the lexer debug in realtime.
	 */
	public static boolean isRealtimeDebugging = false;

	/**
	 * Tokenizes a lang script. Tokenization is the interpreter frontend to the parser backend. This method will
	 * create tokens, each of which is a usable fraction of the script. These tokens can then be analyzed by the
	 * parser to assemble a syntax tree, which can be interpreted and run. This method is generally handled within
	 * the lang interpreter.
	 * @depricated
	 * @param file source
	 * @return the stack of tokens, flipped
	 */
	@Deprecated
	public Stack tokenize(File file) throws DewException
	{
		boolean isInStringLiteral = false;
		boolean passedAssertions = false;
		int lineNum = 0;
		int charInLineNum = 0;
		long scriptCharNum = 0;
		long tokensTokenized = 0;
		long startTime = 0;
		long endTime = 0;
		String line = "";
		StringBuilder characterSequence = new StringBuilder();
		Stack tokenizedScriptStack = new Stack();
		Scanner input = null;

		lexerLogger.isLogging = true;
		lexerLogger.isUsingTimeStamp = true;
		lexerLogger.log(Logger.LogLevel.INFO, "opening io stream to file");

		try
		{
			input = new Scanner(file);
			lexerLogger.log(Logger.LogLevel.INFO, "io stream to file opened @" + file);
		}
		catch (FileNotFoundException e)
		{
			lexerLogger.log(Logger.LogLevel.FATAL, "could not open IO stream to file @" + file);
			throw new DewException("File not found");
		}

		lexerLogger.log(Logger.LogLevel.INFO, "tokenizing");
		startTime = System.currentTimeMillis();

		tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(-1, -1, -1, LexicalTokens.SyntaticToken.SyntaxElement.START_OF_FILE));
		tokensTokenized++;

		while (input.hasNextLine())
		{
			line = input.nextLine();
			lineNum++;
			charInLineNum = 0;

			if (!passedAssertions
					&& line.length() > 8
					&& line.substring(0, 8).equals("#assert ")
					&& (
					(tokenizedScriptStack.peek() instanceof LexicalTokens.SyntaticToken
							&& ((LexicalTokens.SyntaticToken) tokenizedScriptStack.peek()).getElement() == LexicalTokens.SyntaticToken.SyntaxElement.START_OF_FILE)
							|| tokenizedScriptStack.peek() instanceof LexicalTokens.InterpreterAssertionToken
							|| tokenizedScriptStack.peek() instanceof LexicalTokens.InterpreterAssertionLiteral))
			{
				tokenizedScriptStack.push(new LexicalTokens.InterpreterAssertionToken(lineNum, -1, -1));
				tokensTokenized++;
				tokenizedScriptStack.push(new LexicalTokens.InterpreterAssertionLiteral(lineNum, -1, -1, line.substring(8)));
				tokensTokenized++;
				continue;
			}
			else
			{
				passedAssertions = true;
			}

			for (int i = 0; i < line.length(); i++)
			{
				charInLineNum++;
				scriptCharNum++;

				if (isInStringLiteral)
				{
					if (i + 1 < line.length() && line.charAt(i) == '\\' && line.charAt(i + 1) == '\"')
					{
						characterSequence.append(line.charAt(i));
						characterSequence.append(line.charAt(i + 1));
						i++;
					}
					else if (line.charAt(i) == '\"')
					{
						tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
								characterSequence,
								true,
								tokensTokenized,
								lineNum,
								charInLineNum,
								scriptCharNum);
						tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
								charInLineNum,
								scriptCharNum,
								LexicalTokens.SyntaticToken.SyntaxElement.STRING_QUOTE));
						tokensTokenized++;
						isInStringLiteral = false;
					}
					else
					{
						characterSequence.append(line.charAt(i));
					}
				}
				else
				{
					if (line.charAt(i) == ';')
					{
						tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
								characterSequence,
								false,
								tokensTokenized,
								lineNum,
								charInLineNum,
								scriptCharNum);
						tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
								charInLineNum,
								scriptCharNum,
								LexicalTokens.SyntaticToken.SyntaxElement.END_OF_STATEMENT));
						tokensTokenized++;
					}
					else if (line.charAt(i) == '\"')
					{
						tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
								characterSequence,
								false,
								tokensTokenized,
								lineNum,
								charInLineNum,
								scriptCharNum);
						isInStringLiteral = !isInStringLiteral;
						tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
								charInLineNum,
								scriptCharNum,
								LexicalTokens.SyntaticToken.SyntaxElement.STRING_QUOTE));
						tokensTokenized++;
					}
					else if (line.charAt(i) == ' ')
					{
						tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
								characterSequence,
								false,
								tokensTokenized,
								lineNum,
								charInLineNum,
								scriptCharNum);
					}
					else if (line.charAt(i) == '(')
					{
						tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
								characterSequence,
								false,
								tokensTokenized,
								lineNum,
								charInLineNum,
								scriptCharNum);
						tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
								charInLineNum,
								scriptCharNum,
								LexicalTokens.SyntaticToken.SyntaxElement.DELIMITER_LEFT_PAREN));
						tokensTokenized++;
					}
					else if (line.charAt(i) == '{')
					{
						tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
								characterSequence,
								false,
								tokensTokenized,
								lineNum,
								charInLineNum,
								scriptCharNum);
						tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
								charInLineNum,
								scriptCharNum,
								LexicalTokens.SyntaticToken.SyntaxElement.DELIMITER_LEFT_BRACE));
						tokensTokenized++;
					}
					else if (line.charAt(i) == '[')
					{
						tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
								characterSequence,
								false,
								tokensTokenized,
								lineNum,
								charInLineNum,
								scriptCharNum);;
						tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
								charInLineNum,
								scriptCharNum,
								LexicalTokens.SyntaticToken.SyntaxElement.DELIMITER_LEFT_BRACK));
						tokensTokenized++;
					}
					else if (line.charAt(i) == ')')
					{
						tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
								characterSequence,
								false,
								tokensTokenized,
								lineNum,
								charInLineNum,
								scriptCharNum);
						tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
								charInLineNum,
								scriptCharNum,
								LexicalTokens.SyntaticToken.SyntaxElement.DELIMITER_RIGHT_PAREN));
						tokensTokenized++;
					}
					else if (line.charAt(i) == '}')
					{
						tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
								characterSequence,
								false,
								tokensTokenized,
								lineNum,
								charInLineNum,
								scriptCharNum);
						tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
								charInLineNum,
								scriptCharNum,
								LexicalTokens.SyntaticToken.SyntaxElement.DELIMITER_RIGHT_BRACE));
						tokensTokenized++;
					}
					else if (line.charAt(i) == ']')
					{
						tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
								characterSequence,
								false,
								tokensTokenized,
								lineNum,
								charInLineNum,
								scriptCharNum);
						tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
								charInLineNum,
								scriptCharNum,
								LexicalTokens.SyntaticToken.SyntaxElement.DELIMITER_RIGHT_BRACK));
						tokensTokenized++;
					}
					else if (line.charAt(i) == '=')
					{
						if (i + 1 < line.length() && line.charAt(i + 1) == '=')
						{
							tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
									characterSequence,
									false,
									tokensTokenized,
									lineNum,
									charInLineNum,
									scriptCharNum);
							tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_RELATIONAL_EQ));
							tokensTokenized++;
							i++;
						}
						else
						{
							tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
									characterSequence,
									false,
									tokensTokenized,
									lineNum,
									charInLineNum,
									scriptCharNum);
							tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_EQ));
							tokensTokenized++;
						}
					}
					else if (line.charAt(i) == '>')
					{
						if (i + 1 < line.length() && line.charAt(i + 1) == '=')
						{
							tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
									characterSequence,
									false,
									tokensTokenized,
									lineNum,
									charInLineNum,
									scriptCharNum);
							tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_RELATIONAL_GREATER_EQ));
							tokensTokenized++;
							i++;
						}
						else
						{
							tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
									characterSequence,
									false,
									tokensTokenized,
									lineNum,
									charInLineNum,
									scriptCharNum);
							tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_RELATIONAL_GREATER));
							tokensTokenized++;
						}
					}
					else if (line.charAt(i) == '<')
					{
						if (i + 1 < line.length() && line.charAt(i + 1) == '=')
						{
							tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
									characterSequence,
									false,
									tokensTokenized,
									lineNum,
									charInLineNum,
									scriptCharNum);
							tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_RELATIONAL_LESS_EQ));
							tokensTokenized++;
							i++;
						}
						else
						{
							tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
									characterSequence,
									false,
									tokensTokenized,
									lineNum,
									charInLineNum,
									scriptCharNum);
							tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_RELATIONAL_LESS));
							tokensTokenized++;
						}
					}
					else if (line.charAt(i) == '!')
					{
						if (i + 1 < line.length() && line.charAt(i + 1) == '=')
						{
							tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
									characterSequence,
									false,
									tokensTokenized,
									lineNum,
									charInLineNum,
									scriptCharNum);
							tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_RELATIONAL_NOT_EQ));
							tokensTokenized++;
							i++;
						}
						else
						{
							tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
									characterSequence,
									false,
									tokensTokenized,
									lineNum,
									charInLineNum,
									scriptCharNum);
							tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_LOGIC_NOT));
							tokensTokenized++;
						}
					}
					else if (line.charAt(i) == '&')
					{
						tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
								characterSequence,
								false,
								tokensTokenized,
								lineNum,
								charInLineNum,
								scriptCharNum);
						tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
								charInLineNum,
								scriptCharNum,
								LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_LOGIC_AND));
						tokensTokenized++;
					}
					else if (line.charAt(i) == '|')
					{
						tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
								characterSequence,
								false,
								tokensTokenized,
								lineNum,
								charInLineNum,
								scriptCharNum);
						tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
								charInLineNum,
								scriptCharNum,
								LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_LOGIC_OR));
						tokensTokenized++;
					}
					else if (line.charAt(i) == '+')
					{
						if (i + 1 < line.length() && line.charAt(i + 1) == '+')
						{
							tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
									characterSequence,
									false,
									tokensTokenized,
									lineNum,
									charInLineNum,
									scriptCharNum);
							tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									LexicalTokens.SyntaticToken.SyntaxElement.INCREMENT));
							tokensTokenized++;
							i++;
						}
						else if (i + 1 < line.length() && line.charAt(i + 1) == '=')
						{
							tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
									characterSequence,
									false,
									tokensTokenized,
									lineNum,
									charInLineNum,
									scriptCharNum);
							tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_MATH_ADD_EQ));
							tokensTokenized++;
							i++;
						}
						else
						{
							tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
									characterSequence,
									false,
									tokensTokenized,
									lineNum,
									charInLineNum,
									scriptCharNum);
							tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_MATH_ADD));
							tokensTokenized++;
						}
					}
					else if (line.charAt(i) == '-')
					{
						if (i + 1 < line.length() && line.charAt(i + 1) == '-')
						{
							tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
									characterSequence,
									false,
									tokensTokenized,
									lineNum,
									charInLineNum,
									scriptCharNum);
							tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									LexicalTokens.SyntaticToken.SyntaxElement.DECREMENT));
							tokensTokenized++;
							i++;
						}
						else if (i + 1 < line.length() && line.charAt(i + 1) == '=')
						{
							tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
									characterSequence,
									false,
									tokensTokenized,
									lineNum,
									charInLineNum,
									scriptCharNum);
							tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_MATH_SUB_EQ));
							tokensTokenized++;
							i++;
						}
						else
						{
							tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
									characterSequence,
									false,
									tokensTokenized,
									lineNum,
									charInLineNum,
									scriptCharNum);
							tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_MATH_SUB));
							tokensTokenized++;
						}
					}
					else if (line.charAt(i) == '*')
					{
						if (i + 1 < line.length() && line.charAt(i + 1) == '=')
						{
							tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
									characterSequence,
									false,
									tokensTokenized,
									lineNum,
									charInLineNum,
									scriptCharNum);
							tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_MATH_MUL_EQ));
							tokensTokenized++;
							i++;
						}
						else
						{
							tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
									characterSequence,
									false,
									tokensTokenized,
									lineNum,
									charInLineNum,
									scriptCharNum);
							tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_MATH_MUL));
							tokensTokenized++;
						}
					}
					else if (line.charAt(i) == '/')
					{
						if (i + 1 < line.length() && line.charAt(i + 1) == '/')
						{
							break;
						}
						else if (i + 1 < line.length() && line.charAt(i + 1) == '=')
						{
							tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
									characterSequence,
									false,
									tokensTokenized,
									lineNum,
									charInLineNum,
									scriptCharNum);
							tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_MATH_DIV_EQ));
							tokensTokenized++;
							i++;
						}
						else
						{
							tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
									characterSequence,
									false,
									tokensTokenized,
									lineNum,
									charInLineNum,
									scriptCharNum);
							tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_MATH_DIV));
							tokensTokenized++;
						}
					}
					else if (line.charAt(i) == '%')
					{
						if (i + 1 < line.length() && line.charAt(i + 1) == '=')
						{
							tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
									characterSequence,
									false,
									tokensTokenized,
									lineNum,
									charInLineNum,
									scriptCharNum);
							tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_MATH_MOD_EQ));
							tokensTokenized++;
							i++;
						}
						else
						{
							tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
									characterSequence,
									false,
									tokensTokenized,
									lineNum,
									charInLineNum,
									scriptCharNum);
							tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_MATH_MOD));
							tokensTokenized++;
						}
					}
					else if (line.charAt(i) == '^')
					{
						if (i + 1 < line.length() && line.charAt(i + 1) == '=')
						{
							tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
									characterSequence,
									false,
									tokensTokenized,
									lineNum,
									charInLineNum,
									scriptCharNum);
							tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_MATH_POW_EQ));
							tokensTokenized++;
							i++;
						}
						else
						{
							tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
									characterSequence,
									false,
									tokensTokenized,
									lineNum,
									charInLineNum,
									scriptCharNum);
							tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_MATH_POW));
							tokensTokenized++;
						}
					}
					else if (line.charAt(i) == '.')
					{
						if (i + 1 < line.length() && line.charAt(i + 1) == '=')
						{
							tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
									characterSequence,
									false,
									tokensTokenized,
									lineNum,
									charInLineNum,
									scriptCharNum);
							tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_APPEND_PROPERTY));
							tokensTokenized++;
							i++;
						}
						else
						{
							tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
									characterSequence,
									false,
									tokensTokenized,
									lineNum,
									charInLineNum,
									scriptCharNum);
							tokenizedScriptStack.push(new LexicalTokens.ResourceHierarchyIdentifier(lineNum,
									charInLineNum,
									scriptCharNum));
							tokensTokenized++;
						}
					}
					else
					{
						characterSequence.append(line.charAt(i));
					}
				}
			}
		}

		tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
				charInLineNum,
				scriptCharNum,
				LexicalTokens.SyntaticToken.SyntaxElement.END_OF_FILE));
		tokensTokenized++;

		endTime = System.currentTimeMillis();

		Stack reversedTokens = new Stack();
		for (Object o : tokenizedScriptStack)
			reversedTokens.push(o);

		if (isPrintingTokenization)
		{
			Stack tokenDebug = (Stack) reversedTokens.clone();
			for (Object o : tokenDebug)
				lexerLogger.log(Logger.LogLevel.INFO, "Found valid token " + o +
						"   @L" + ((LexicalTokens.LexicalToken) o).lineNum +
						" LC" + ((LexicalTokens.LexicalToken) o).charInLineNum +
						" SC" + ((LexicalTokens.LexicalToken) o).scriptCharNum);
		}

		lexerLogger.log(Logger.LogLevel.INFO, "Lexically analyzed " + lineNum + " lines.");
		lexerLogger.log(Logger.LogLevel.INFO, "Lexically analyzed " + scriptCharNum + " characters.");
		lexerLogger.log(Logger.LogLevel.INFO, "Found " + tokensTokenized + " valid tokens.");
		lexerLogger.log(Logger.LogLevel.INFO, "DONE. Took " + (endTime - startTime) + "ms");

		input.close();

		return reversedTokens;
	}

	/**
	 * Tokenizes a lang script. Tokenization is the interpreter frontend to the parser backend. This method will
	 * create tokens, each of which is a usable fraction of the script. These tokens can then be analyzed by the
	 * parser to assemble a syntax tree, which can be interpreted and run. This method is generally handled within
	 * the lang interpreter.
	 * @param file source
	 * @return the stack of tokens, flipped
	 */
	public Stack tokenizeAcc(File file) throws DewException
	{
		/*
		 * Is the lexer evaluation charcters as part of a literal rather than part of the source
		 */
		boolean isInStringLiteral = false;
		/*
		 * Has the header of the source been passed.
		 */
		boolean passedAssertions = false;
		/*
		 * Variables to describe the current location and progress of the lexer.
		 */
		int lineNum = 0;
		int charInLineNum = 0;
		long scriptCharNum = 0;
		long tokensTokenized = 0;
		long startTime = 0;
		long endTime = 0;
		/*
		 * The line of the source currently being analyzed.
		 */
		String line = "";
		/*
		 * The current sequqence of accumulated characters. Once a keyword or syntax element is found, these will be
		 * handled appropriately.
		 */
		StringBuilder characterSequence = new StringBuilder();
		/*
		 * The stack of lexial tokens representing the source.
		 */
		Stack tokenizedScriptStack = new Stack();
		/*
		 * The scanner to read the source file.
		 */
		Scanner input = null;

		//TODO remove after this method has been tested and is assumed to be stable. Then the end user can decide if he/she want to debug
		lexerLogger.isLogging = true;
		lexerLogger.isUsingTimeStamp = true;
		lexerLogger.log(Logger.LogLevel.INFO, "opening io stream to file");

		try
		{
			input = new Scanner(file);
			lexerLogger.log(Logger.LogLevel.INFO, "io stream to file opened @" + file);
		}
		catch (FileNotFoundException e)
		{
			lexerLogger.log(Logger.LogLevel.FATAL, "could not open IO stream to file @" + file);
			throw new DewException("File not found");
		}

		/*
		 * Mark the start of the file and record the start time.
		 */
		lexerLogger.log(Logger.LogLevel.INFO, "tokenizing");
		startTime = System.currentTimeMillis();

		/*
		 * Push the token marking the start of the script source
		 */
		tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(-1, -1, -1, LexicalTokens.SyntaticToken.SyntaxElement.START_OF_FILE));
		tokensTokenized++;

		/*
		 * This loop will iterate over every every line of the source file provided.
		 */
		while (input.hasNextLine())
		{
			/*
			 * Take the next line, then increment the lien coutner, then reset the lien char counter because a new
			 * line has been taken.
			 */
			line = input.nextLine();
			lineNum++;
			charInLineNum = 0;

			/*
			 * Check for interpreter assertions.
			 * <Assertion> := "#assert" + <string literal containing inteprreter directive>
			 */
			if (!passedAssertions
					&& line.length() > 8
					&& line.substring(0, 8).equals("#assert ")
					&& (
					(tokenizedScriptStack.peek() instanceof LexicalTokens.SyntaticToken
							&& ((LexicalTokens.SyntaticToken) tokenizedScriptStack.peek()).getElement() == LexicalTokens.SyntaticToken.SyntaxElement.START_OF_FILE)
							|| tokenizedScriptStack.peek() instanceof LexicalTokens.InterpreterAssertionToken
							|| tokenizedScriptStack.peek() instanceof LexicalTokens.InterpreterAssertionLiteral))
			{
				//log the assertions
				tokenizedScriptStack.push(new LexicalTokens.InterpreterAssertionToken(lineNum, -1, -1));
				tokensTokenized++;
				tokenizedScriptStack.push(new LexicalTokens.InterpreterAssertionLiteral(lineNum, -1, -1, line.substring(8)));
				tokensTokenized++;
				continue;
			}
			/*
			 * If an assertion is not found, mark the end of assertions. Asserting information to the interpreter
			 * after the source head is nto allowed. If an assertion is found later it will be read as a data
			 * literal, which will likely be caught by the parser as a syntax error.
			 */
			else
			{
				passedAssertions = true;
			}

			/*
			 * This loop interates over every character of every line. The code within this loop will perform the
			 * character by character analysis that generates tokens.
			 */
			for (int i = 0; i < line.length(); i++)
			{
				/*
				 * Increment the lien char counter and the total char counter.
				 */
				charInLineNum++;
				scriptCharNum++;

				/*
				 * Check if the lexer is currently lookint at a string literal. If so all keywords and symbols
				 * should be ignored.
				 */
				if (isInStringLiteral)
				{
					/*
					 * Check if a slash is followed by a doubel quute. If so, append the double qoute escpae
					 * sequence rather than breaking out of the string literal interpretation state.
					 */
					if (i + 1 < line.length() && line.charAt(i) == '\\' && line.charAt(i + 1) == '\"')
					{
						characterSequence.append(line.charAt(i));
						characterSequence.append(line.charAt(i + 1));
						i++;
					}
					/*
					 * Check if a double quote is present not preceeded by a slash. This ends the string literal and
					 * returns the lexer to its normal evlaualtion state.
					 */
					else if (line.charAt(i) == '\"')
					{
						tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
								characterSequence,
								true,
								tokensTokenized,
								lineNum,
								charInLineNum,
								scriptCharNum);
						tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
								charInLineNum,
								scriptCharNum,
								LexicalTokens.SyntaticToken.SyntaxElement.STRING_QUOTE));
						tokensTokenized++;
						isInStringLiteral = false;
					}
					/*
					 * If no quotes are detected, the character should simply be logged as part of the string
					 * literal.
					 */
					else
					{
						characterSequence.append(line.charAt(i));
					}
				}
				/*
				 * If the lexer is not in a string literal interpertation state, it should proceed normally
				 * detecting keywords, symbols, and structures.
				 */
				else
				{
					/*
					 * This SyntaxElement will be push to the stack. If no character marking ciritcla syntax is
					 * found, this variabel will remain null inidicating that the built character string needs to be
					 * checked for keywords and literals.
					 */
					LexicalTokens.SyntaticToken.SyntaxElement elementToPush = null;

					/*
					 * Cast the char to an int so a much cleaner switch block can be used. Switch with strings/chars
					 * is not supported by Java 5.1 and we are trying to keep the DewInterpreter complinat with 5.1.
					 */
					switch ((int) line.charAt(i))
					{
						/*
						 * Check for EOL and EOS char
						 */
						case (int) ';': elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.END_OF_STATEMENT; break;
						/*
						 * Check for a double quote char, moving the lexer to a string literal interpretation state.
						 */
						case (int) '\"':
							elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.STRING_QUOTE;
							isInStringLiteral = !isInStringLiteral;
							break;
						/*
						 * Check for a white space char.
						 */
						case (int) ' ':
							tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
									characterSequence,
									false,
									tokensTokenized,
									lineNum,
									charInLineNum,
									scriptCharNum);
							continue;
						/*
						 * Check for a white space char.
						 */
						case (int) '\t': break;
						/*
						 * Check for delimiter chars.
						 */
						case (int) '(': elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.DELIMITER_LEFT_PAREN; break;
						case (int) '{': elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.DELIMITER_LEFT_BRACE; break;
						case (int) '[': elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.DELIMITER_LEFT_BRACK; break;
						case (int) ')': elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.DELIMITER_RIGHT_PAREN; break;
						case (int) '}': elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.DELIMITER_RIGHT_BRACE; break;
						case (int) ']': elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.DELIMITER_RIGHT_BRACK; break;
						/*
						 * Check for logical and mathematic syntax chars and sequences. This includes =, ==,  >, >=,
						 * <, <=, !, !=, &, |, +, ++, +=, -, --, -=, *, *=, /, /=, %, and %=
						 */
						case (int) '=':
							if (i + 1 < line.length() && line.charAt(i + 1) == '=')
							{
								elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_RELATIONAL_EQ;
								i++;
							}
							else
							{
								elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_EQ;
							}
							break;
						case (int) '>':
							if (i + 1 < line.length() && line.charAt(i + 1) == '=')
							{
								elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_RELATIONAL_GREATER_EQ;
								i++;
							}
							else
							{
								elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_RELATIONAL_GREATER;
							}
							break;
						case (int) '<':
							if (i + 1 < line.length() && line.charAt(i + 1) == '=')
							{
								elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_RELATIONAL_LESS_EQ;
								i++;
							}
							else
							{
								elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_RELATIONAL_LESS;
							}
							break;
						case (int) '!':
							if (i + 1 < line.length() && line.charAt(i + 1) == '=')
							{
								elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_RELATIONAL_NOT_EQ;
								i++;
							}
							else
							{
								elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_LOGIC_NOT;
							}
							break;
						case (int) '&': elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_LOGIC_AND; break;
						case (int) '|': elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_LOGIC_OR; break;
						case (int) '+':
							if (i + 1 < line.length() && line.charAt(i + 1) == '+')
							{
								elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.INCREMENT;
								i++;
							}
							else if (i + 1 < line.length() && line.charAt(i + 1) == '=')
							{
								elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_MATH_ADD_EQ;
								i++;
							}
							else
							{
								elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_MATH_ADD;
							}
							break;
						case (int) '-':
							if (i + 1 < line.length() && line.charAt(i + 1) == '-')
							{
								elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.DECREMENT;
								i++;
							}
							else if (i + 1 < line.length() && line.charAt(i + 1) == '=')
							{
								elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_MATH_SUB_EQ;
								i++;
							}
							else
							{
								elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_MATH_SUB;
							}
							break;
						case (int) '*':
							if (i + 1 < line.length() && line.charAt(i + 1) == '=')
							{
								elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_MATH_MUL_EQ;
								i++;
							}
							else
							{
								elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_MATH_MUL;
							}
							break;
						case (int) '/':
							if (i + 1 < line.length() && line.charAt(i + 1) == '=')
							{
								elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_MATH_DIV_EQ;
								i++;
							}
							else
							{
								elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_MATH_DIV;
							}
							break;
						case (int) '%':
							if (i + 1 < line.length() && line.charAt(i + 1) == '=')
							{
								elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_MATH_MOD_EQ;
								i++;
							}
							else
							{
								elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_MATH_MOD;
							}
							break;
						/*
						 * Check for resource append and heiarchy identifiers.
						 */
						case (int) '.':
							// .=
							if (i + 1 < line.length() && line.charAt(i + 1) == '=')
							{
								elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_APPEND_PROPERTY;
								i++;
							}
							// .
							else
							{
								tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
										characterSequence,
										false,
										tokensTokenized,
										lineNum,
										charInLineNum,
										scriptCharNum);
								tokenizedScriptStack.push(new LexicalTokens.ResourceHierarchyIdentifier(lineNum,
										charInLineNum,
										scriptCharNum));
								tokensTokenized++;
							}
							break;
						case (int) '$': elementToPush = LexicalTokens.SyntaticToken.SyntaxElement.POINTER;

					}

					/*
					 * If a syntax element has been found, push it to the stack.
					 */
					if (elementToPush != null)
					{
						tokensTokenized = pushCharacterSequence(tokenizedScriptStack,
								characterSequence,
								false,
								tokensTokenized,
								lineNum,
								charInLineNum,
								scriptCharNum);

						tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
								charInLineNum,
								scriptCharNum,
								elementToPush));

						tokensTokenized++;
					}
					/*
					 * If a syntax element has not been found, add the character sequence and check for keywords.
					 */
					else
					{
						characterSequence.append(line.charAt(i));
					}
				}
			}
		}

		/*
		 * Mark the end of the file. Also marks the terminating token.
		 */
		tokenizedScriptStack.push(new LexicalTokens.SyntaticToken(lineNum,
				charInLineNum,
				scriptCharNum,
				LexicalTokens.SyntaticToken.SyntaxElement.END_OF_FILE));
		tokensTokenized++;

		//lexical analysis done, mark the end time
		endTime = System.currentTimeMillis();

		//flip the stack
		/*
		Stack reversedTokens = new Stack();
		while (!tokenizedScriptStack.isEmpty())
		{
			reversedTokens.push(tokenizedScriptStack.pop());
		}
		*/

		//if debugging is on, print the tokens and their locations.
		if (isPrintingTokenization)
		{
			Stack tokenDebug = (Stack) tokenizedScriptStack.clone();
			for (Object o : tokenDebug)
				lexerLogger.log(Logger.LogLevel.INFO, "Found valid token " + o +
						"   @L" + ((LexicalTokens.LexicalToken) o).lineNum +
						" LC" + ((LexicalTokens.LexicalToken) o).charInLineNum +
						" SC" + ((LexicalTokens.LexicalToken) o).scriptCharNum);
		}

		//print lexical analysis stats. Mostly for fun :)
		lexerLogger.log(Logger.LogLevel.INFO, "Lexically analyzed " + lineNum + " lines.");
		lexerLogger.log(Logger.LogLevel.INFO, "Lexically analyzed " + scriptCharNum + " characters.");
		lexerLogger.log(Logger.LogLevel.INFO, "Found " + tokensTokenized + " valid tokens.");
		lexerLogger.log(Logger.LogLevel.INFO, "DONE. Took " + (endTime - startTime) + "ms");

		/*
		 * Close resources
		 */
		input.close();

		//return
		return tokenizedScriptStack;
	}

	/**
	 * This method tokenizes character sequence literals after generic symbols and syntax have been tokenized
	 * @param stack the stack to push evaluated character sequences
	 * @param dataLiteral the character sequence to be analyzed
	 * @param isInStringLiteral is the character sequence a part of a denoted string literal
	 */
	private long pushCharacterSequence(Stack stack,
	                                   StringBuilder dataLiteral,
	                                   boolean isInStringLiteral,
	                                   long tokensTokenized,
	                                   int lineNum,
	                                   int charInLineNum,
	                                   long scriptCharNum)
	{
		if (isInStringLiteral)
		{
			stack.pop();
			tokensTokenized--;

			if (stack.peek() instanceof LexicalTokens.ResourceCallToken ||
					stack.peek() instanceof LexicalTokens.ResourceOperationToken)
			{
				stack.push(new LexicalTokens.SyntaticToken(lineNum,
						charInLineNum,
						scriptCharNum,
						LexicalTokens.SyntaticToken.SyntaxElement.STRING_QUOTE));
				tokensTokenized++;

				stack.push(new LexicalTokens.ResourcePointerToken(lineNum,
						charInLineNum,
						scriptCharNum,
						dataLiteral.toString()));
				tokensTokenized++;

				dataLiteral.delete(0, dataLiteral.length());
				return tokensTokenized;
			}
			else
			{
				stack.push(new LexicalTokens.SyntaticToken(lineNum,
						charInLineNum,
						scriptCharNum,
						LexicalTokens.SyntaticToken.SyntaxElement.STRING_QUOTE));
				tokensTokenized++;

				stack.push(new LexicalTokens.DataLiteralToken(lineNum,
						charInLineNum,
						scriptCharNum,
						dataLiteral.toString()));
				tokensTokenized++;
			}

			dataLiteral.delete(0, dataLiteral.length());
			return tokensTokenized;
		}

		//remove leading whitespace and tabs
		for (int i = 0; i < dataLiteral.length(); i++)
		{
			if (dataLiteral.charAt(i) == ' ' || dataLiteral.charAt(i) == '\t')
			{
				dataLiteral.deleteCharAt(i);
			}
			else
			{
				break;
			}
		}

		//preserve data format
		String data = dataLiteral.toString();

		//remove whitespace for analysis
		String dataNoWhiteSpace = data.replaceAll(" ", "");

		if (dataLiteral.length() != 0)
		{
			if (dataNoWhiteSpace.equals("return"))
			{
				stack.push(new LexicalTokens.SyntaticToken(lineNum,
						charInLineNum,
						scriptCharNum,
						LexicalTokens.SyntaticToken.SyntaxElement.END_OF_BLOCK));
				tokensTokenized++;
			}
			else if (dataNoWhiteSpace.equals("if"))
			{
				stack.push(new LexicalTokens.SyntaticToken(lineNum,
						charInLineNum,
						scriptCharNum,
						LexicalTokens.SyntaticToken.SyntaxElement.STRUCTURE_IF));
				tokensTokenized++;
			}
			else if (dataNoWhiteSpace.equals("elseif"))
			{
				stack.push(new LexicalTokens.SyntaticToken(lineNum,
						charInLineNum,
						scriptCharNum,
						LexicalTokens.SyntaticToken.SyntaxElement.STRUCTURE_ELSEIF));
				tokensTokenized++;
			}
			else if (dataNoWhiteSpace.equals("else"))
			{
				stack.push(new LexicalTokens.SyntaticToken(lineNum,
						charInLineNum,
						scriptCharNum,
						LexicalTokens.SyntaticToken.SyntaxElement.STRUCTURE_ELSE));
				tokensTokenized++;
			}
			else if (dataNoWhiteSpace.equals("while"))
			{
				stack.push(new LexicalTokens.SyntaticToken(lineNum,
						charInLineNum,
						scriptCharNum,
						LexicalTokens.SyntaticToken.SyntaxElement.STRUCTURE_WHILE));
				tokensTokenized++;
			}
			else if (dataNoWhiteSpace.equals("true"))
			{
				stack.push(new LexicalTokens.BooleanValueLiteralToken(lineNum,
						charInLineNum,
						scriptCharNum,
						LexicalTokens.BooleanValueLiteralToken.BoolValue.TRUE));
				tokensTokenized++;
			}
			else if (dataNoWhiteSpace.equals("false"))
			{
				stack.push(new LexicalTokens.BooleanValueLiteralToken(lineNum,
						charInLineNum,
						scriptCharNum,
						LexicalTokens.BooleanValueLiteralToken.BoolValue.FLASE));
				tokensTokenized++;
			}
			else if (dataNoWhiteSpace.equals("intern"))
			{
				stack.push(new LexicalTokens.ResourceCallToken(lineNum,
						charInLineNum,
						scriptCharNum,
						LexicalTokens.ResourceCallToken.ResourceParadigm.INTERNAL));
				tokensTokenized++;
			}
			else if (dataNoWhiteSpace.equals("extern"))
			{
				stack.push(new LexicalTokens.ResourceCallToken(lineNum,
						charInLineNum,
						scriptCharNum,
						LexicalTokens.ResourceCallToken.ResourceParadigm.EXTERNAL));
				tokensTokenized++;
			}
			else if (dataNoWhiteSpace.equals("load"))
			{
				if (stack.peek() instanceof LexicalTokens.ResourceCallToken
						&& ((LexicalTokens.ResourceCallToken) stack.peek()).getResourceParadigm()
						== LexicalTokens.ResourceCallToken.ResourceParadigm.EXTERNAL)
				{
					stack.push(new LexicalTokens.ResourceOperationToken(lineNum,
							charInLineNum,
							scriptCharNum,
							LexicalTokens.ResourceOperationToken.ResourceOperation.LOAD));
					tokensTokenized++;
				}
				else
				{
					stack.push(new LexicalTokens.DataLiteralToken(lineNum,
							charInLineNum,
							scriptCharNum,
							data));
					tokensTokenized++;
				}
			}
			else if (dataNoWhiteSpace.equals("exec"))
			{
				if (stack.peek() instanceof LexicalTokens.ResourceCallToken
						/* && ((ResourceCallToken) stack.peek()).getResourceParadigm()
						== ResourceParadigm.EXTERNAL*/)
				{
					stack.push(new LexicalTokens.ResourceOperationToken(lineNum,
							charInLineNum,
							scriptCharNum,
							LexicalTokens.ResourceOperationToken.ResourceOperation.EXEC));
					tokensTokenized++;
				}
				else
				{
					stack.push(new LexicalTokens.DataLiteralToken(lineNum,
							charInLineNum,
							scriptCharNum,
							data));
					tokensTokenized++;
				}
			}
			else if (dataNoWhiteSpace.equals("bool"))
			{
				stack.push(new LexicalTokens.VariableDeclarationToken(lineNum,
						charInLineNum,
						scriptCharNum,
						LexicalTokens.VariableDeclarationToken.VariableType.BOOL));
				tokensTokenized++;
			}
			else if (dataNoWhiteSpace.equals("int"))
			{
				stack.push(new LexicalTokens.VariableDeclarationToken(lineNum,
						charInLineNum,
						scriptCharNum,
						LexicalTokens.VariableDeclarationToken.VariableType.INT));
				tokensTokenized++;
			}
			else if (dataNoWhiteSpace.equals("float"))
			{
				stack.push(new LexicalTokens.VariableDeclarationToken(lineNum,
						charInLineNum,
						scriptCharNum,
						LexicalTokens.VariableDeclarationToken.VariableType.FLOAT));
				tokensTokenized++;
			}
			else if (dataNoWhiteSpace.equals("char"))
			{
				stack.push(new LexicalTokens.VariableDeclarationToken(lineNum,
						charInLineNum,
						scriptCharNum,
						LexicalTokens.VariableDeclarationToken.VariableType.CHAR));
				tokensTokenized++;
			}
			else if (dataNoWhiteSpace.equals("string"))
			{
				stack.push(new LexicalTokens.VariableDeclarationToken(lineNum,
						charInLineNum,
						scriptCharNum,
						LexicalTokens.VariableDeclarationToken.VariableType.STRING));
				tokensTokenized++;
			}
			else
			{
				if (stack.peek() instanceof LexicalTokens.ResourceCallToken ||
						stack.peek() instanceof LexicalTokens.ResourceOperationToken ||
						stack.peek() instanceof LexicalTokens.ResourceHierarchyIdentifier)
				{
					stack.push(new LexicalTokens.ResourcePointerToken(lineNum,
							charInLineNum,
							scriptCharNum,
							data));
					tokensTokenized++;
				}
				else if (stack.peek() instanceof LexicalTokens.VariableDeclarationToken)
				{
					stack.push(new LexicalTokens.VariableNameToken(lineNum,
							charInLineNum,
							scriptCharNum,
							data));
					tokensTokenized++;
				}
				else
				{
					stack.push(new LexicalTokens.DataLiteralToken(lineNum,
							charInLineNum,
							scriptCharNum,
							data));
					tokensTokenized++;
				}
			}
		}

		dataLiteral.delete(0, dataLiteral.length());
		return tokensTokenized;
	}

	////////////////////////////////
	//  token class declarations  //
	////////////////////////////////


	/**
	 * This class acts an internal container class for all Token classes critical to the lexical analysis process.
	 * @see LexicalTokens.LexicalToken
	 */
	public final static class LexicalTokens
	{
		/**
		 * Do not initialize this class. It is a container.
		 */
		private LexicalTokens() {}

		/**
		 * This is the generic super-class of all lexical tokens. It holds the location reference data so error
		 * can be debugged.
		 */
		public static abstract class LexicalToken extends DewInterpreter.Token
		{
			/**
			 * The line number of the token
			 */
			public final int lineNum;

			/**
			 * The character in the line of the token
			 */
			public final int charInLineNum;

			/**
			 * The total character location of the token
			 */
			public final long scriptCharNum;

			/**
			 * @param lineNum the line the token can be found on
			 * @param charInLineNum the character in the line the token can be found on
			 * @param scriptCharNum the character location of the token in the entire script
			 */
			public LexicalToken(int lineNum, int charInLineNum, long scriptCharNum)
			{
				this.lineNum = lineNum;
				this.charInLineNum = charInLineNum;
				this.scriptCharNum = scriptCharNum;
			}

			/**
			 * Prints the token's location in the script source.
			 * @return teh location of the token
			 */
			public String toString()
			{
				return "LEXICAL TOKEN@ LINE: " + lineNum + ", CHAR: " + charInLineNum;
			}
		}

		/**
		 * This class represents an interpreter assertion token. It indicates a literal is following on the same
		 * line that will describe the behavior of the interpreter before it begins evaluation the script. Similar
		 * to compiler notes in Java or C++
		 */
		public static class InterpreterAssertionToken extends LexicalTokens.LexicalToken
		{
			/**
			 * Default constructor.
			 * @param line the line number of the token
			 * @param charInLine the char position of the token in the line
			 * @param scriptChar the total character position of the token
			 */
			private InterpreterAssertionToken(int line, int charInLine, long scriptChar)
			{
				super(line, charInLine, scriptChar);
			}

			/**
			 * @return "ASSERTION TOKEN"
			 */
			public String toString()
			{
				return "ASSERTION TOKEN";
			}
		}

		/**
		 * This class holds the literal asserted by the asstertion token. The data stored will give the interpreter
		 * a pre-runtime directive such a gc options or rounding/casting options.
		 */
		public static class InterpreterAssertionLiteral extends LexicalTokens.LexicalToken
		{
			/**
			 * The literal asserted
			 */
			private String literal;

			/**
			 * Default constructor.
			 * @param line the line number of the token
			 * @param charInLine the char position of the token in the line
			 * @param scriptChar the total character position of the token
			 * @param literal the value of the asserted literal
			 */
			public InterpreterAssertionLiteral(int line, int charInLine, long scriptChar, String literal)
			{
				super(line, charInLine, scriptChar);
				this.literal = literal;
			}

			/**
			 * @return the literal
			 */
			public String getInterpreterAssertionLiteral()
			{
				return literal;
			}

			/**
			 * @return "AssertionLiteral: <value>"
			 */
			public String toString()
			{
				return "AssertionLiteral: " + literal;
			}
		}

		/**
		 * This class represents any syntactic symbol found during lexical analysis.
		 */
		public static class SyntaticToken extends LexicalTokens.LexicalToken
		{
			/**
			 * The syntax element value.
			 */
			private LexicalTokens.SyntaticToken.SyntaxElement element;

			/**
			 * The list of syntactic elements this class can represent.
			 */
			public enum SyntaxElement
			{
				//entrancy
				START_OF_BLOCK,
				START_OF_FILE,

				//delimiters
				DELIMITER_LEFT_PAREN,
				DELIMITER_LEFT_BRACE,
				DELIMITER_LEFT_BRACK,
				DELIMITER_RIGHT_PAREN,
				DELIMITER_RIGHT_BRACE,
				DELIMITER_RIGHT_BRACK,

				//mathematical operators
				OPERATOR_MATH_ADD,
				OPERATOR_MATH_SUB,
				OPERATOR_MATH_MUL,
				OPERATOR_MATH_DIV,
				OPERATOR_MATH_MOD,
				OPERATOR_MATH_POW,
				OPERATOR_MATH_ADD_EQ,
				OPERATOR_MATH_SUB_EQ,
				OPERATOR_MATH_MUL_EQ,
				OPERATOR_MATH_DIV_EQ,
				OPERATOR_MATH_MOD_EQ,
				OPERATOR_MATH_POW_EQ,
				OPERATOR_EQ,
				INCREMENT,
				DECREMENT,

				//internal operators
				OPERATOR_APPEND_PROPERTY,

				//logical operators
				OPERATOR_LOGIC_AND,
				OPERATOR_LOGIC_OR,
				OPERATOR_LOGIC_NOT,

				//relational operators
				OPERATOR_RELATIONAL_EQ,
				OPERATOR_RELATIONAL_GREATER,
				OPERATOR_RELATIONAL_LESS,
				OPERATOR_RELATIONAL_GREATER_EQ,
				OPERATOR_RELATIONAL_LESS_EQ,
				OPERATOR_RELATIONAL_NOT_EQ,

				//structures
				STRUCTURE_IF,
				STRUCTURE_ELSEIF,
				STRUCTURE_ELSE,
				STRUCTURE_WHILE,
				STRUCTURE_FOR,

				//pointers
				POINTER,

				//strings
				STRING_QUOTE,

				//termination
				END_OF_STATEMENT,
				END_OF_BLOCK,
				END_OF_FILE
			}

			/**
			 *
			 * @param line the line number of the token
			 * @param charInLine the char position of the token in the line
			 * @param scriptChar the total character position of the token
			 * @param element the syntactic element represented by an instance of this object
			 */
			public SyntaticToken(int line, int charInLine, long scriptChar, LexicalTokens.SyntaticToken.SyntaxElement
					element)
			{
				super(line, charInLine, scriptChar);
				this.element = element;
			}

			/**
			 * @return the syntactic element
			 */
			public LexicalTokens.SyntaticToken.SyntaxElement getElement()
			{
				return element;
			}

			/**
			 * @return "SyntaxToken: <element>"
			 */
			public String toString()
			{
				return "SyntaxToken: " + element.toString();
			}
		}

		/**
		 * This class represents the declaration of a variable.
		 */
		public static class VariableDeclarationToken extends LexicalTokens.LexicalToken
		{
			/**
			 * The variable type being initialized.
			 */
			private LexicalTokens.VariableDeclarationToken.VariableType variableType;

			/**
			 * The types of variable supported.
			 */
			public enum VariableType
			{
				BOOL,
				INT,
				FLOAT,
				CHAR,
				STRING
			}

			/**
			 * Default constructor.
			 * @param line the line number of the token
			 * @param charInLine the char position of the token in the line
			 * @param scriptChar the total character position of the token
			 * @param variableType the type of variable being declared
			 */
			public VariableDeclarationToken(int line, int charInLine, long scriptChar,
			                                LexicalTokens.VariableDeclarationToken.VariableType variableType)
			{
				super(line, charInLine, scriptChar);
				this.variableType = variableType;
			}

			/**
			 * @return the variable type
			 */
			public LexicalTokens.VariableDeclarationToken.VariableType getVariableType()
			{
				return variableType;
			}

			/**
			 * @return "VariableToken: <varType>"
			 */
			public String toString()
			{
				return "VariableToken: " + variableType.toString();
			}
		}

		/**
		 * This class represents the name a variable associated with its declaration.
		 */
		public static class VariableNameToken extends LexicalTokens.LexicalToken
		{
			/**
			 * The variable name
			 */
			private String variableName;

			/**
			 * Default constructor
			 * @param line the line number of the token
			 * @param charInLine the char position of the token in the line
			 * @param scriptChar the total character position of the token
			 * @param variableName the variable name
			 */
			public VariableNameToken(int line, int charInLine, long scriptChar, String variableName)
			{
				super(line, charInLine, scriptChar);
				this.variableName = variableName;
			}

			/**
			 * @return variable name
			 */
			public String getVariableName()
			{
				return variableName;
			}

			/**
			 * @return "VariableNameToken <varName>"
			 */
			public String toString()
			{
				return "VariableNameToken: " + variableName;
			}
		}

		/**
		 * This class represents a boolean literal.
		 */
		public static class BooleanValueLiteralToken extends LexicalTokens.LexicalToken
		{
			/**
			 * the boolean literal value
			 */
			private LexicalTokens.BooleanValueLiteralToken.BoolValue value;

			/**
			 * Boolean literal values
			 */
			public static enum BoolValue
			{
				TRUE,
				FLASE;
			}

			/**
			 *
			 * @param line the line number of the token
			 * @param charInLine the char position of the token in the line
			 * @param scriptChar the total character position of the token
			 * @param value the value of the boolean literal
			 */
			public BooleanValueLiteralToken(int line, int charInLine, long scriptChar,
			                                LexicalTokens.BooleanValueLiteralToken.BoolValue value)
			{
				super(line, charInLine, scriptChar);
				this.value = value;
			}

			/**
			 * @return the value of the boolean literal
			 */
			public LexicalTokens.BooleanValueLiteralToken.BoolValue getValue()
			{
				return value;
			}

			/**
			 * @return "ValueLiteral: <value>"
			 */
			public String toString()
			{
				return "ValueLiteral: " + value;
			}
		}

		/**
		 * This class represents an period indicating a hierarchical relationship between two potential elements in
		 * the object-property tree.
		 */
		public static class ResourceHierarchyIdentifier extends LexicalTokens.LexicalToken
		{
			/**
			 * Default constructor.
			 * @param line the line number of the token
			 * @param charInLine the char position of the token in the line
			 * @param scriptChar the total character position of the token
			 */
			public ResourceHierarchyIdentifier(int line, int charInLine, long scriptChar)
			{
				super(line, charInLine, scriptChar);
			}

			/**
			 * @return "ResourceHierarchyIdentifier"
			 */
			public String toString()
			{
				return "ResourceHierarchyIdentifier";
			}
		}

		/**
		 * This class represents a directive to a resource.
		 */
		public static class ResourceCallToken extends LexicalTokens.LexicalToken
		{
			/**
			 * The paradigm of the resource
			 */
			private LexicalTokens.ResourceCallToken.ResourceParadigm resourceParadigm;

			/**
			 * The types of resource paradigms. Internal resources are held within the application system porting
			 * DewScript. External resources are held outside the application system porting DewScript, for example
			 * files on users Desktop, or a program execute outside the runtime of the application or script.
			 */
			public enum ResourceParadigm
			{
				EXTERNAL,
				INTERNAL
			}

			/**
			 * Default-Constructor
			 * @param line the line number of the token
			 * @param charInLine the char position of the token in the line
			 * @param scriptChar the total character position of the token
			 * @param resourceParadigm the paradigm of the resource call
			 */
			public ResourceCallToken(int line, int charInLine, long scriptChar, LexicalTokens.ResourceCallToken
					.ResourceParadigm resourceParadigm)
			{
				super(line, charInLine, scriptChar);
				this.resourceParadigm = resourceParadigm;
			}

			/**
			 * @return the resource paradigm
			 */
			public LexicalTokens.ResourceCallToken.ResourceParadigm getResourceParadigm()
			{
				return resourceParadigm;
			}

			/**
			 * @return "ResourceCallToken: <parardigm>"
			 */
			public String toString()
			{
				return "ResourceCallToken: " + resourceParadigm.toString();
			}
		}

		/**
		 * This class represents an operation performed on a resource.
		 */
		public static class ResourceOperationToken extends LexicalTokens.LexicalToken
		{
			/**
			 * The operation performed on a resource
			 */
			private LexicalTokens.ResourceOperationToken.ResourceOperation resourceOperation;

			/**
			 * The types of operations that can be performed on a resource. Load will issue system calls to pull the
			 * resource into the application implementing DewScript; based on the user's DewScript language
			 * interface, the new resource will then be handled appropriately according to that interface. Exec will
			 * execute a resource outside the application, for example running a program.
			 */
			public enum ResourceOperation
			{
				LOAD,
				EXEC
			}

			/**
			 * Default constructor.
			 * @param line the line number of the token
			 * @param charInLine the char position of the token in the line
			 * @param scriptChar the total character position of the token
			 * @param resourceOperation the resource operation to be performed
			 */
			public ResourceOperationToken(int line, int charInLine, long scriptChar,
			                              LexicalTokens.ResourceOperationToken.ResourceOperation resourceOperation)
			{
				super(line, charInLine, scriptChar);
				this.resourceOperation = resourceOperation;
			}

			/**
			 * @return the resource operation
			 */
			public LexicalTokens.ResourceOperationToken.ResourceOperation getResourceOperation()
			{
				return resourceOperation;
			}

			/**
			 * @return "ResourceOperationToken: <operation>"
			 */
			public String toString()
			{
				return "ResourceOperationToken: " + resourceOperation.toString();
			}
		}

		/**
		 * This class represents the location of a resource in the object-property tree.
		 */
		public static class ResourcePointerToken extends LexicalTokens.LexicalToken
		{
			/**
			 * The resource directory.
			 */
			private String resourceDirectory;

			/**
			 * Default constructor.
			 * @param line the line number of the token
			 * @param charInLine the char position of the token in the line
			 * @param scriptChar the total character position of the token
			 * @param resourceDirectory The resource directory. For external resources this is simply a full or
			 *                          relative file directory. For internal resources, this is the tree directory.
			 *                          The object-property tree is made of AbstractDewEntityBindings and
			 *                          AbstractDewPropertyBindings. A valid directory might be
			 *                          objectName.subObjectName.propertyName.
			 */
			public ResourcePointerToken(int line, int charInLine, long scriptChar, String resourceDirectory)
			{
				super(line, charInLine, charInLine);
				this.resourceDirectory = resourceDirectory;
			}

			/**
			 * @return resource directory
			 */
			public String getResourceDirectory()
			{
				return resourceDirectory;
			}

			/**
			 * @return "ResourcePointerToken: <dir>"
			 */
			public String toString()
			{
				return "ResourcePointerToken: " + resourceDirectory;
			}
		}

		/**
		 * This class represents a generic literal. Generic literals could be more than one thing based on the
		 * lexical context. The parse will further evaluate the role of a data literal at the parse stage.
		 */
		public static class DataLiteralToken extends LexicalTokens.LexicalToken
		{
			/**
			 * The value of the data literal.
			 */
			private String dataLiteral;

			/**
			 * Default constructor.
			 * @param line the line number of the token
			 * @param charInLine the char position of the token in the line
			 * @param scriptChar the total character position of the token
			 * @param dataLiteral the value of the data literal
			 */
			public DataLiteralToken(int line, int charInLine, long scriptChar, String dataLiteral)
			{
				super(line, charInLine, scriptChar);
				this.dataLiteral = dataLiteral;
			}

			/**
			 * @return the value of the data literal
			 */
			public String getDataLiteral()
			{
				return dataLiteral;
			}

			/**
			 * @return "RawDataLiteral: <literalValue>"
			 */
			public String toString()
			{
				return "RawDataLiteral: " + dataLiteral;
			}
		}
	}
}
