package com.normalizedinsanity.rayburn.engine.dew.lang;

import com.normalizedinsanity.rayburn.engine.dew.DewException;
import com.normalizedinsanity.rayburn.engine.util.Logger;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

import static com.normalizedinsanity.rayburn.engine.dew.lang.DewInterpreter.DewMathFunctionSet.add;
import static com.normalizedinsanity.rayburn.engine.dew.lang.DewInterpreter.DewMathFunctionSet.castToFloat;
import static com.normalizedinsanity.rayburn.engine.dew.lang.DewInterpreter.DewMathFunctionSet.castToInteger;
import static com.normalizedinsanity.rayburn.engine.dew.lang.DewInterpreter.DewMathFunctionSet.div;
import static com.normalizedinsanity.rayburn.engine.dew.lang.DewInterpreter.DewMathFunctionSet.mod;
import static com.normalizedinsanity.rayburn.engine.dew.lang.DewInterpreter.DewMathFunctionSet.mul;
import static com.normalizedinsanity.rayburn.engine.dew.lang.DewInterpreter.DewMathFunctionSet.sub;
import static com.normalizedinsanity.rayburn.engine.dew.lang.DewParser.AlgebraicExpressionBranch
		.canEvaluateNaturalMathToConstant;
import static com.normalizedinsanity.rayburn.engine.dew.lang.DewParser.AlgebraicExpressionBranch
		.convertNaturalMathToRPN;
import static com.normalizedinsanity.rayburn.engine.dew.lang.DewParser.AlgebraicExpressionBranch.evalRPN;

/**
 * @author guyfleeman
 * @date 03/03/14
 */
public class DewParser
{
	private static DewParser instance;
	private static Logger parserLogger = new Logger();

	/**
	 * Map linking syntax token to their precedence
	 */
	public static final HashMap<DewLexer.LexicalTokens.SyntaticToken.SyntaxElement, Integer> shuntingYardPrecedence;

	/**
	 * Regex to identify numbers.
	 */
	public static final Pattern genericNumberRegex = Pattern.compile("^-?\\d+([,\\.]\\d+)?([eE]-?\\d+)?$");

	static
	{
		shuntingYardPrecedence = new HashMap<DewLexer.LexicalTokens.SyntaticToken.SyntaxElement, Integer>();
		shuntingYardPrecedence.put(DewLexer.LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_MATH_MUL, 2);
		shuntingYardPrecedence.put(DewLexer.LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_MATH_DIV, 2);
		shuntingYardPrecedence.put(DewLexer.LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_MATH_MOD, 2);
		shuntingYardPrecedence.put(DewLexer.LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_MATH_ADD, 1);
		shuntingYardPrecedence.put(DewLexer.LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_MATH_SUB, 1);

		instance = new DewParser();
	}

	DewParser() {}

	public static DewParser getInstance()
	{
		return instance;
	}

	///////////////////////////////////
	//  end instance initialization  //
	///////////////////////////////////



	public SyntaxBranch parse(Stack tokenizedDewScript) throws DewException
	{
		DewLexer.LexicalTokens.SyntaticToken initialTriggerToken;
		SyntaxBranch primaryBranch;
		ArrayList<DewLexer.LexicalTokens.LexicalToken> scriptTokens = new ArrayList<DewLexer.LexicalTokens
				.LexicalToken>();
		ArrayList<ParseTokens.Variable> globalVariables = new ArrayList<ParseTokens.Variable>();
		HashMap<String, Short> globalPointers = new HashMap<String, Short>();
		MutableInt index = new MutableInt(0);
		short initialDepth = 0;

		for (Object genericToken : tokenizedDewScript)
			scriptTokens.add((DewLexer.LexicalTokens.LexicalToken) genericToken);

		parserLogger.log(Logger.LogLevel.INFO, "Evaluating token " + scriptTokens.get(0));

		if (!(scriptTokens.get(index.getValue()) instanceof DewLexer.LexicalTokens.SyntaticToken)
				|| !(((DewLexer.LexicalTokens.SyntaticToken) scriptTokens.get(index.getValue())).getElement()
					== DewLexer.LexicalTokens.SyntaticToken.SyntaxElement.START_OF_FILE))
		{
			parserLogger.log(Logger.LogLevel.FATAL, "Script entry point not found.");
			parserLogger.log(Logger.LogLevel.INFO, "Did DewParser successfully parse source?");
			throw new DewException("Start of file not found. Quitting.");
		}
		else
		{
			parserLogger.log(Logger.LogLevel.INFO, "Valid entry point found. Proceeding.");
			initialTriggerToken = (DewLexer.LexicalTokens.SyntaticToken) scriptTokens.get(index.getValue());
			scriptTokens.remove(index.getValue());
		}

		primaryBranch = createBranch(initialTriggerToken,
				0,
				scriptTokens,
				globalVariables,
				globalVariables,
				globalPointers,
				globalPointers,
				initialDepth,
				index);

		return primaryBranch;
	}

	/**
	 * This recursive method creates a syntax tree by processing a Stack containing tokenized DewScript scource. It
	 * handles expression evaluation and subbranching as necessary. It handles variable scope as necessary. It
	 * handles variable pointers as necessary.
	 *
	 * @param branchTriggerToken the token that flagged the method to make a recursive call
	 *                           and evaulate a subbranch.
	 * @param branchTriggerTokenIndex the index of the arrayList that flagged the method to make a recursive call
	 *                                and evaulate a subbranch.
	 * @param lexicalTokens an ArrayList containing the lexical tokens from the lexer to be parsed and assembled
	 *                      into a syntax tree or subbranch.
	 * @param globalVariableList the ArrayList containg the highest level branch's (the trunk, or starting branch of
	 *                           the tree) variables. Any variabel declared as public should added to this list to
	 *                           ensure public scope.
	 * @param localVariableList the ArrayList conaining private variables only relative to the current subbranch.
	 *                          Variables added to this list are not passed up the syntax tree as this list is
	 *                          cloned in subbraches to keep the security of variabel scope. Varibles added to this
	 *                          list will be passed to subbranches.
	 * @param globalVariablePointersMap the HashMap containig the highest level branch's (the trunk, or starting
	 *                                   branch of the tree) variable pointers. Any varible delcared as public
	 *                                   should have it's pointer added to this Map to ensure global scope.
	 * @param localVariablePointersMap the HashMap containg pointers to variables relative to the current subbranch.
	 *                                 Pointers added to this Map are not passed up the syntax tree as this list is
	 *                                 cloned in subbranches to keep the security of variable scope. Pointers added
	 *                                 to this list will be passed to subbranches.
	 * @param depth the depth of the subbranch, currently used for debugging purposes. Likely to be removed.
	 * @param branchLoopIndex the MutableInt represinting the looping index of the super branch. As tokens are used
	 *                        by the subbranch, the super branch no longer needs to evalutate them becuase they only
	 *                        belong to the subbranch. Evaluating them else where would cause the syntax tree to
	 *                        redundant and wrong evlauations. A MutableInt is used to keep a pointer to the looping
	 *                        index of the superbranch,incrementing it as the subbranch evlauated tokens. This
	 *                        ensures when the subbranch has been evaluated, the superbranch will where to pick up
	 *                        the evaluation process in teh ArrayList.
	 * @return the syntax branch to be added to the syntax tree for evaluation.
	 * @throws com.normalizedinsanity.rayburn.engine.dew.DewException if the syntax rules of DewScript are violated or if the parser experiences an internal
	 *                      error, a DewException will be thrown.
	 */
	private static SyntaxBranch createBranch(DewLexer.LexicalTokens.LexicalToken branchTriggerToken,
	                                         int branchTriggerTokenIndex,
	                                         ArrayList<DewLexer.LexicalTokens.LexicalToken> lexicalTokens,
	                                         ArrayList<ParseTokens.Variable> globalVariableList,
	                                         ArrayList<ParseTokens.Variable> localVariableList,
	                                         HashMap<String, Short> globalVariablePointersMap,
	                                         HashMap<String, Short> localVariablePointersMap,
	                                         short depth,
	                                         MutableInt branchLoopIndex) throws DewException
	{
		parserLogger.log(Logger.LogLevel.INFO, "Creating subbranch @L" + depth);

		/*
		 * Check if the syntax tree has exceeded 32,768 branching levels. I don't anticipate any code reacing this
		 * many sublevels, but this is primarily here to check the integrity DewParser and DewLexer algorithms. May
		 * be commented out in the future or the branch level may be increased to Integer.MAX_VALUE.
		 * This serves as an inital check for the validity of the branch.
		 */
		if (depth == Short.MAX_VALUE)
		{
			parserLogger.log(Logger.LogLevel.FATAL, "Syntax tree depth exceeded! (" + depth + ")");
			parserLogger.log(Logger.LogLevel.INFO, "Is syntax error creating recursive depth problem?");
			throw new DewException("Syntax tree depth exceeded. Quitting.");
		}
		depth++;

		//initial check passed, create the new branch.
		SyntaxBranch currentBranch = new SyntaxBranch();

		if (!(branchTriggerToken instanceof DewLexer.LexicalTokens.SyntaticToken))
		{
			parserLogger.log(Logger.LogLevel.FATAL, "Branch entrancy token not of type SyntaxToken.");
			throw new DewException("Invalid entrance token.");
		}

		/*
		 * Given the entrance token, determine the corresponding token that will signal the branch to break,
		 * ending the branch and stopping its execution.
		 */
		DewLexer.LexicalTokens.SyntaticToken.SyntaxElement endOfBranchElement = DewLexer.LexicalTokens.SyntaticToken
				.SyntaxElement.END_OF_STATEMENT;
		switch (((DewLexer.LexicalTokens.SyntaticToken) branchTriggerToken).getElement())
		{
			case START_OF_FILE:
				endOfBranchElement = DewLexer.LexicalTokens.SyntaticToken.SyntaxElement.END_OF_FILE;
				break;
			case STRUCTURE_IF:
			case STRUCTURE_ELSEIF:
			case STRUCTURE_ELSE:
			case STRUCTURE_WHILE:
			case STRUCTURE_FOR:
				endOfBranchElement = DewLexer.LexicalTokens.SyntaticToken.SyntaxElement.END_OF_STATEMENT;
				break;
			case DELIMITER_LEFT_BRACE:
				endOfBranchElement = DewLexer.LexicalTokens.SyntaticToken.SyntaxElement.DELIMITER_RIGHT_BRACE;
				break;
			case DELIMITER_LEFT_BRACK:
				endOfBranchElement = DewLexer.LexicalTokens.SyntaticToken.SyntaxElement.DELIMITER_RIGHT_BRACK;
				break;
			case DELIMITER_LEFT_PAREN:
				endOfBranchElement = DewLexer.LexicalTokens.SyntaticToken.SyntaxElement.DELIMITER_RIGHT_PAREN;
				break;
		}

		parserLogger.log(Logger.LogLevel.INFO, "Exit token determined: " + endOfBranchElement.toString());

		/*
		 * Level specfic copies of resources are made to ensure branch level cnages are not carried up the syntax
		 * tree, only down it. For example, if a local variable is created in a for loop, the scope of the variable
		 * is limited to the context of the loop or any subcontexts below it. The variable's data and pointer
		 * should not be accesssible to higher branches. Branch level copies are made to keep variable (and
		 * eventually function) scope in check.
		 */
		@SuppressWarnings("unchecked")
		ArrayList<DewLexer.LexicalTokens.LexicalToken> branchLexicalTokenSubset =
				(ArrayList<DewLexer.LexicalTokens.LexicalToken>) lexicalTokens.clone();
		@SuppressWarnings("unchecked")
		ArrayList<ParseTokens.Variable> branchVariableSubset =
				(ArrayList<ParseTokens.Variable>) localVariableList.clone();
		@SuppressWarnings("unchecked")
		HashMap<String, Short> branchVariablePointersSubset =
				(HashMap<String, Short>) localVariablePointersMap.clone();

		assert branchLexicalTokenSubset     != null;
		assert branchVariableSubset         != null;
		assert branchVariablePointersSubset != null;

		//branch loop label to stop branch creation once the end of branch sequence has been reached
		branchLoop:
		//TODO minus 1 check
		for (MutableInt index = branchLoopIndex; index.getValue() < lexicalTokens.size() - 1; index.increment())
		{
			branchLoopIndex.increment();

			parserLogger.log(Logger.LogLevel.INFO, "Evaluating token: "
					+ lexicalTokens.get(index.getValue()).toString());

			//if the end of the branch has been reached, break and return the branch
			if (lexicalTokens.get(index.getValue()) instanceof DewLexer.LexicalTokens.SyntaticToken
					&& ((DewLexer.LexicalTokens.SyntaticToken) lexicalTokens.get(index.getValue())).getElement()
					== endOfBranchElement)
			{
				parserLogger.log(Logger.LogLevel.INFO, "End of branch token found: "
						+ endOfBranchElement.toString());
				break;
			}

			//if the end of the block execution has been reached, break adn return the branch
			if (lexicalTokens.get(index.getValue()) instanceof DewLexer.LexicalTokens.SyntaticToken
					&& ((DewLexer.LexicalTokens.SyntaticToken) lexicalTokens.get(index.getValue())).getElement()
					== DewLexer.LexicalTokens.SyntaticToken.SyntaxElement.END_OF_BLOCK)
			{
				parserLogger.log(Logger.LogLevel.INFO, "End of block found.");
				break;
			}

			/*
			 * Check if a syntax element is present. If so, determine if its presence with the surroundings tokens
			 * requires the creation of a subbranch.
			 */
			if (lexicalTokens.get(index.getValue()) instanceof DewLexer.LexicalTokens.SyntaticToken)
			{
				DewLexer.LexicalTokens.SyntaticToken.SyntaxElement syntaxElement =
						((DewLexer.LexicalTokens.SyntaticToken) lexicalTokens.get(index.getValue())).getElement();

				//TODO check security of removing this block
				//if the end of the branch has been reached, break and return the branch
				if (syntaxElement == endOfBranchElement)
					break branchLoop;

				switch (syntaxElement)
				{
					case END_OF_FILE: break branchLoop;
					case END_OF_BLOCK: break branchLoop;
					/*
					 * A left brace signals a new scope and possible routine block. A new branch needs to be created
					 * to handle this.
					 */
					case DELIMITER_LEFT_BRACE:
					case DELIMITER_LEFT_BRACK:
					case DELIMITER_LEFT_PAREN:
						createBranch(lexicalTokens.get(index.getValue()),
								index.getValue(),
								lexicalTokens,
								globalVariableList,
								branchVariableSubset,
								globalVariablePointersMap,
								branchVariablePointersSubset,
								depth,
								index);
						break;


				}

			}

			/*
			 * Check for variable declaration.
			 */
			if (lexicalTokens.get(index.getValue()) instanceof DewLexer.LexicalTokens.VariableDeclarationToken)
			{
				/*
				 * Ensure a variable name follows the declaration token. If index + 1 (name token location) is
				 * greater than the size of the list or its not a VariableNameToken, throw a parse exception.
				 */
				if (index.getValue() + 1 > lexicalTokens.size()
						|| !(lexicalTokens.get(index.getValue() + 1) instanceof DewLexer.LexicalTokens.VariableNameToken))
				{

					parserLogger.log(Logger.LogLevel.FATAL, "Expected variable name: "
							+ lexicalTokens.get(index.getValue()).toString());
					throw new DewParseException("Expected variable name token following@" +
							lexicalTokens.get(index.getValue()).toString());
				}

				/*
				 * Ensure the maximum number of variables has not been exceeded. This is to ensure the pointer
				 * system can accurtely point to the variable. The current number of variables is limited to
				 * Short.MAX_VALUE and cannot be negative. Shorts are used to check the integrity of the pointer
				 * system while the Interpreter is being tested. Once the stability is the system is verified the
				 * number will be increased to Long.MAX_VALUE.
				 */
				if (branchVariableSubset.size() >= Short.MAX_VALUE)
					throw new DewParseException(
							"Max number of variables exceeded, cannot assign pointer to new variable@"
									+ lexicalTokens.get(index.getValue()).toString());

				DewLexer.LexicalTokens.VariableDeclarationToken decToken =
						((DewLexer.LexicalTokens.VariableDeclarationToken) lexicalTokens.get(index.getValue()));
				index.increment();
				DewLexer.LexicalTokens.VariableNameToken nameToken =
						((DewLexer.LexicalTokens.VariableNameToken) lexicalTokens.get(index.getValue()));
				index.increment();

				Set<String> keys = localVariablePointersMap.keySet();
				for (String key : keys)
					if (nameToken.getVariableName().equals(key))
						throw new DewParseException("Variable name already defined in scope@"
								+ nameToken.toString());

				keys = branchVariablePointersSubset.keySet();
				for (String key : keys)
					if (nameToken.getVariableName().equals(key))
						throw new DewParseException("Variable name already defined in scope@"
								+ nameToken.toString());

				keys = null;

				//TODO check for global definition when internal global declaration feature is added

				/*
				 * Add the variables pointer map to the local maps list.
				 */
				branchVariablePointersSubset.put(
						nameToken.getVariableName(), (short) branchVariableSubset.size());

				switch (decToken.getVariableType())
				{
					case BOOL:
						boolean boolValue = false;

						if (index.getValue() <= lexicalTokens.size()
								&& lexicalTokens.get(index.getValue()) instanceof DewLexer.LexicalTokens.SyntaticToken
								&& ((DewLexer.LexicalTokens.SyntaticToken) lexicalTokens.get(index.getValue())).getElement()
								== DewLexer.LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_EQ)
						{
							index.increment();

							ArrayList<DewLexer.LexicalTokens.LexicalToken> expressionTokens = new ArrayList<DewLexer.LexicalTokens.LexicalToken>();
							while (index.getValue() <= lexicalTokens.size()
									&& !(lexicalTokens.get(index.getValue()) instanceof DewLexer.LexicalTokens.SyntaticToken
											&& ((DewLexer.LexicalTokens.SyntaticToken) lexicalTokens.get(index.getValue())).getElement()
											== DewLexer.LexicalTokens.SyntaticToken.SyntaxElement.END_OF_STATEMENT))
							{
								expressionTokens.add(lexicalTokens.get(index.getValue()));
								index.increment();
							}
							index.increment(); //EOS increment

							//value = ;
						}

						/*
						 * Add a new boolean variable. Give it a pointer to the size of the local variable's list
						 * size. Give it a default value of false.
						 */
						branchVariableSubset.add(new ParseTokens.Variable<Boolean>(
								boolValue, Boolean.class, (short) branchVariableSubset.size()));
						continue branchLoop;
						//break;
					case INT:
						int intValue = 0;

						/*
						 * If an equals sign is the next token, calculate the value.
						 */
						if (index.getValue() <= lexicalTokens.size()
								&& lexicalTokens.get(index.getValue()) instanceof DewLexer.LexicalTokens.SyntaticToken
								&& ((DewLexer.LexicalTokens.SyntaticToken) lexicalTokens.get(index.getValue())).getElement()
								== DewLexer.LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_EQ)
						{
							index.increment();

							/*
							 * While the end of statement hasn't been reached, gather tokens as expression tokens.
							 */
							ArrayList<DewLexer.LexicalTokens.LexicalToken> expressionTokens = new ArrayList<DewLexer.LexicalTokens.LexicalToken>();
							while (index.getValue() <= lexicalTokens.size()
									&& !(lexicalTokens.get(index.getValue()) instanceof DewLexer.LexicalTokens.SyntaticToken
									&& ((DewLexer.LexicalTokens.SyntaticToken) lexicalTokens.get(index.getValue())).getElement()
									== DewLexer.LexicalTokens.SyntaticToken.SyntaxElement.END_OF_STATEMENT))
							{
								expressionTokens.add(lexicalTokens.get(index.getValue()));
								index.increment();
							}
							index.decrement();

							boolean canEvalIntExp = canEvaluateNaturalMathToConstant(expressionTokens,
									globalVariableList,
									localVariableList,
									globalVariablePointersMap,
									localVariablePointersMap,
									parserLogger);

							if(canEvalIntExp)
							{
								parserLogger.log(Logger.LogLevel.INFO, "Found RPN sequence that can be evaluated " +
										"as a constant...");
								LinkedList<DewInterpreter.Token> expressionQueue = convertNaturalMathToRPN(expressionTokens,
									globalVariableList,
									localVariableList,
									globalVariablePointersMap,
									localVariablePointersMap,
									parserLogger);
								intValue = Integer.parseInt(evalRPN(expressionQueue,
										globalVariableList,
										localVariableList,
										Integer.class,
										parserLogger).toString());
								parserLogger.log(Logger.LogLevel.INFO, "Added int variable with value " + intValue);

							}
						}

						/*
						 * Add a new int variable. Give it a pointer to the size of the local variable's list
						 * size. Give it a default value of 0.
						 */
						branchVariableSubset.add(new ParseTokens.Variable<Integer>(
								intValue,
								Integer.class,
								(short) branchVariableSubset.size()));

						continue branchLoop;
					case FLOAT:
						float floatValue = 0f;

						/*
						 * If an equals sign is the next token, calculate the value.
						 */
						if (index.getValue() <= lexicalTokens.size()
								&& lexicalTokens.get(index.getValue()) instanceof DewLexer.LexicalTokens.SyntaticToken
								&& ((DewLexer.LexicalTokens.SyntaticToken) lexicalTokens.get(index.getValue())).getElement()
								== DewLexer.LexicalTokens.SyntaticToken.SyntaxElement.OPERATOR_EQ)
						{
							index.increment();

							/*
							 * While the end of statement hasn't been reached, gather tokens as expression tokens.
							 */
							ArrayList<DewLexer.LexicalTokens.LexicalToken> expressionTokens = new ArrayList<DewLexer.LexicalTokens.LexicalToken>();
							while (index.getValue() <= lexicalTokens.size()
									&& !(lexicalTokens.get(index.getValue()) instanceof DewLexer.LexicalTokens.SyntaticToken
									&& ((DewLexer.LexicalTokens.SyntaticToken) lexicalTokens.get(index.getValue())).getElement()
									== DewLexer.LexicalTokens.SyntaticToken.SyntaxElement.END_OF_STATEMENT))
							{
								expressionTokens.add(lexicalTokens.get(index.getValue()));
								index.increment();
							}
							index.decrement();

							boolean canEvalFloatExp = canEvaluateNaturalMathToConstant(expressionTokens,
									globalVariableList,
									localVariableList,
									globalVariablePointersMap,
									localVariablePointersMap,
									parserLogger);

							if(canEvalFloatExp)
							{
								parserLogger.log(Logger.LogLevel.INFO, "Found RPN sequence that can be evaluated " +
										"as a constant...");
								LinkedList<DewInterpreter.Token> expressionQueue = convertNaturalMathToRPN(expressionTokens,
										globalVariableList,
										localVariableList,
										globalVariablePointersMap,
										localVariablePointersMap,
										parserLogger);
								floatValue = Float.parseFloat(evalRPN(expressionQueue,
										globalVariableList,
										localVariableList,
										Float.class,
										parserLogger).toString());
								parserLogger.log(Logger.LogLevel.INFO, "Added float variable with value " + floatValue);

							}
						}

						/*
						 * Add a new float variable. Give it a pointer to the size of the local variable's list
						 * size. Give it a default value of 0f.
						 */
						branchVariableSubset.add(new ParseTokens.Variable<Float>(
								floatValue,
								Float.class,
								(short) branchVariableSubset.size()));
						continue branchLoop;
						//break;
					case CHAR:
						/*
						 * Add a new char variable. Give it a pointer to the size of the local variable's list
						 * size. Give it a default value of \0.
						 */
						branchVariableSubset.add(new ParseTokens.Variable<Character>(
								'\0', Character.class, (short) branchVariableSubset.size()));
						continue branchLoop;
						//break;
					case STRING:
					default:
						/*
						 * Add a new String variable. Give it a pointer to the size of the local variable's list
						 * size. Give it a default value of "".
						 */
						branchVariableSubset.add(new ParseTokens.Variable<String>(
								"", String.class, (short) branchVariableSubset.size()));
						continue branchLoop;
						//break;
				}


			}

			//if ()

		}

		//return the branch
		return currentBranch;
	}

	public AlgebraicExpressionBranch createExpressionBranch()
	{
		return null;
	}


	///////////////////////////////
	//  inner class declaration  //
	///////////////////////////////

	/**
	 * @author guyfleeman
	 * @date 03/03/14
	 * This class contains the syntactical branch for the interpreter to interpret
	 */
	public static class SyntaxBranch implements BranchElement
	{
		private ArrayList<BranchElement>                   branchElements;
		private ArrayList<ArrayList<ParseTokens.Variable>> variables;

		public void call()
		{
			for (BranchElement branchElement : branchElements)
				branchElement.call();
		}
	}

	/**
	 * This class represents a branch that can be attached to the abstract syntax tree, and be evaluated at runtime.
	 * This class represents an algebraic expression branch, converted from infix (natural language) to postfix
	 * (reverse polish notation) queue for computational evaluation at runtime. Computers can't evaluate infix, so
	 * infix is changed to postfix at the parse stage, and queue up for evaluation in an instance of this object.
	 */
	public static class AlgebraicExpressionBranch implements BranchElement
	{
		/**
		 * The expression queue to be evaluated at runtime.
		 */
		private final LinkedList<DewLexer.LexicalTokens.LexicalToken> expressionQueue;

		/**
		 * The global varables list.
		 */
		private final ArrayList<ParseTokens.Variable> globalVariableList;

		/**
		 * The local variables list.
		 */
		private final ArrayList<ParseTokens.Variable> localVariableList;

		/**
		 * The global pointers map.
		 */
		private final HashMap<String, Short> globalVariablePointersMap;

		/**
		 * The local pointers map.
		 */
		private final HashMap<String, Short> localVariablePointersMap;

		/**
		 * The value of the expression. Will be set at the earliest possible opportunity.
		 */
		private Object value = null;

		/**
		 *
		 * @param expressionQueue the expression queue
		 * @param globalVariableList the global variables list
		 * @param localVariableList the local variables list
		 * @param globalVariablePointersMap the global pointers map
		 * @param localVariablePointersMap the local pointers map
		 */
		public AlgebraicExpressionBranch(LinkedList<DewLexer.LexicalTokens.LexicalToken> expressionQueue,
		                                 ArrayList<ParseTokens.Variable> globalVariableList,
		                                 ArrayList<ParseTokens.Variable> localVariableList,
		                                 HashMap<String, Short> globalVariablePointersMap,
		                                 HashMap<String, Short> localVariablePointersMap)
		{
			this.expressionQueue = expressionQueue;
			this.globalVariableList = globalVariableList;
			this.localVariableList = localVariableList;
			this.globalVariablePointersMap = globalVariablePointersMap;
			this.localVariablePointersMap = localVariablePointersMap;
		}

		/**
		 * Calls the branch to update value
		 */
		public void call()
		{
			value = getValue();
		}

		/**
		 * @return the value of the branch at runtime
		 */
		public Object getValue()
		{
			return null;
		}

		/**
		 * Check to see if a natural math expression can be evaluated as a constant (only constants and final vars)
		 * @param expressionTokens the tokens the describe the algebraic expression
		 * @param globalVariables global variables
		 * @param localVariables local variables
		 * @param globalPointers global pointers
		 * @param localPointers local pointers
		 * @return if the expression can be evaluated as a constant
		 */
		public static boolean canEvaluateNaturalMathToConstant(ArrayList<DewLexer.LexicalTokens.LexicalToken>
				                                                       expressionTokens,
		                                                       ArrayList<ParseTokens.Variable> globalVariables,
		                                                       ArrayList<ParseTokens.Variable> localVariables,
		                                                       HashMap<String, Short> globalPointers,
		                                                       HashMap<String, Short> localPointers,
		                                                       Logger logger)
		{
			/*
			 * If any necessary resources are null, return false.
			 */
			if (globalVariables == null
					|| localVariables == null
					|| globalPointers == null
					|| localPointers == null)
			{
				logger.log(Logger.LogLevel.WARNING, "Found essential null paramaters in RPN constant eval check. " +
						"Returning false");
				return false;
			}

			expressionLoop:
			for (DewLexer.LexicalTokens.LexicalToken token : expressionTokens)
			{
				/*
				 * If the token is SyntacticToken and does not represent a valid expression syntactic element,
				 * return false.
				 */
				if (token instanceof DewLexer.LexicalTokens.SyntaticToken)
				{
					DewLexer.LexicalTokens.SyntaticToken syntacticToken = (DewLexer.LexicalTokens.SyntaticToken) token;

					if (syntacticToken.getElement() != DewLexer.LexicalTokens.SyntaticToken.SyntaxElement
							.OPERATOR_MATH_MUL
							&& syntacticToken.getElement() != DewLexer.LexicalTokens.SyntaticToken.SyntaxElement
							.OPERATOR_MATH_DIV
							&& syntacticToken.getElement() != DewLexer.LexicalTokens.SyntaticToken.SyntaxElement
							.OPERATOR_MATH_MOD
							&& syntacticToken.getElement() != DewLexer.LexicalTokens.SyntaticToken.SyntaxElement
							.OPERATOR_MATH_ADD
							&& syntacticToken.getElement() != DewLexer.LexicalTokens.SyntaticToken.SyntaxElement
							.OPERATOR_MATH_SUB
							&& syntacticToken.getElement() != DewLexer.LexicalTokens.SyntaticToken.SyntaxElement
							.DELIMITER_LEFT_PAREN
							&& syntacticToken.getElement() != DewLexer.LexicalTokens.SyntaticToken.SyntaxElement
							.DELIMITER_RIGHT_PAREN)
					{
						logger.log(Logger.LogLevel.INFO, "Found invalid syntax element in RPN constant eval " +
								"check. Returning false");
						return false;
					}
				}
				else if (token instanceof DewLexer.LexicalTokens.DataLiteralToken)
				{
					DewLexer.LexicalTokens.DataLiteralToken dataToken = (DewLexer.LexicalTokens.DataLiteralToken)
							token;

					/*
					 * If the token is a number, its okay. Else do further analysis.
					 */
					if (isNumber(dataToken))
					{
						logger.log(Logger.LogLevel.INFO, "Found numeric literal in expression. Continuing.");
					}
					else
					{
						logger.log(Logger.LogLevel.INFO, "Found non numeric literal in expression. Continuing " +
								"evaluation.");

						/*
						 * If the pointers map is empty, this literal cant be a valid variable, so the expression
						 * cannot be evaluated to a constant.
						 */
						if (globalPointers.isEmpty() && localPointers.isEmpty())
						{
							logger.log(Logger.LogLevel.WARNING, "Found empty pointer maps. Cannot map literal. " +
									"Returning false");
							return false;
						}

						/*
						 * If there is no match for the literal, it can't be valid and can't be evaluated to a
						 * constant. Return false;
						 */
						if (localPointers.get(dataToken.getDataLiteral()) == null
								&& globalPointers.get(dataToken.getDataLiteral()) == null)
						{
							logger.log(Logger.LogLevel.INFO, "Found no pointer match. Returning false");
							return false;
						}

						/*
						 * check local variables for a pointer match
						 */
						if (!localPointers.isEmpty() && localPointers.get(dataToken.getDataLiteral()) != null)
						{
							for (ParseTokens.Variable v : localVariables)
							{
								/*
								 * If the variable is found and it's final, its good.
								 */
								if (v.pointer == localPointers.get(dataToken.getDataLiteral()) && v.isFinal)
								{
									logger.log(Logger.LogLevel.INFO, "Found final variable; can evaluate. " +
											"Continuing.");
									continue expressionLoop;
								}
							}
						}

						logger.log(Logger.LogLevel.INFO, "Found no local pointer match. Checking " +
								"global.");

						/*
						 * check global variables for a pointer match
						 */
						if (!globalPointers.isEmpty() && globalPointers.get(dataToken.getDataLiteral()) != null)
						{
							for (ParseTokens.Variable v : globalVariables)
							{
								/*
								 * If the variable is found and it's final, its good.
								 */
								if (v.pointer == globalPointers.get(dataToken.getDataLiteral()) && v.isFinal)
								{
									logger.log(Logger.LogLevel.INFO, "Found final variable; can evaluate. " +
											"Continuing.");
									continue expressionLoop;
								}
							}
						}

						logger.log(Logger.LogLevel.INFO, "Found no pointer match. Returning false");

						/*
						 * Final variable not found in local or global scope. Its value can be gaurenteed so this
						 * expression can't be evaluated as a constant. return false
						 */
						return false;
					}
				}
			}

			logger.log(Logger.LogLevel.INFO, "Found no immediate expression or variable transgressions. Returning " +
					"true");

			/*
			 * If the loop never return false, it's go to be evaluated as a constant;
			 */
			return true;
		}

		/**
		 * This method parses a natural linear expression and converts it to reverse polish notation (RPN) by using the
		 * shunting yard algorithm. Once the natural function has been converted to RPN, it can be evaluated at runtime
		 * by the interpreter.
		 * @param expressionTokens the tokens that describe the expression. Uncleaned for syntax error.
		 * @param globalVariables the ArrayList of variables the expression has access to
		 * @param globalPointers the Map of pointers the expression has access to
		 * @return the output queue
		 * @throws com.normalizedinsanity.rayburn.engine.dew.lang.DewParseException if a token is passed that is not a
		 * valid expression token, or the syntax rules of
		 * the expression are violated, a parse time exception will be thrown
		 */
		public static LinkedList<DewInterpreter.Token> convertNaturalMathToRPN(ArrayList<DewLexer.LexicalTokens
																			   .LexicalToken> expressionTokens,
		                                                                       ArrayList<ParseTokens.Variable>
				                                                                       globalVariables,
		                                                                       ArrayList<ParseTokens.Variable>
				                                                                       localVariables,
		                                                                       HashMap<String, Short> globalPointers,
		                                                                       HashMap<String, Short> localPointers,
		                                                                       Logger logger)
				throws DewParseException
		{
			/*
			 * Shunting yard operator stack
			 */
			Stack operatorStack = new Stack();

			/*
			 * Shunting yard outputQueue
			 */
			LinkedList<DewInterpreter.Token> outputQueue = new LinkedList<DewInterpreter.Token>();
			boolean evalAsString = false;

			/*
			 * Loop tag
			 */
			expressionLoop:
			for (int i = 0; i < expressionTokens.size(); i++)
			{
				DewLexer.LexicalTokens.LexicalToken expressionToken = expressionTokens.get(i);

				/*
				 * Logic for non-operator literals in the expression (numeric or non-numeric).
				 */
				if (expressionToken instanceof DewLexer.LexicalTokens.DataLiteralToken)
				{
					DewLexer.LexicalTokens.DataLiteralToken token = (DewLexer.LexicalTokens.DataLiteralToken)
							expressionToken;

					/*
					 * Is the token a number? If so add it to the output queue.
					 */
					if (isNumber(token))
					{
						outputQueue.add(token);
					}
					/*
					 * Is it a non-number? (Variable name or string literal, String literals will be a future
					 * implementation)
					 */
					else
					{
						/*
						 * If the pointers map is empty, no variables have been declared. This token is invalid a and
						 * cannot be evaluated at runtime. Throw a DewParseException.
						 */
						if (globalPointers.isEmpty() && localPointers.isEmpty())
						{
							throw new DewParseException("DataLiteral found in expression that is a non-number and " +
									                            "has no corresponding pointer map because the map is" +
									                            " " +
									                            "empty. Have any variables " +
									                            "been declared?");
						}

						/*
						 * If there is no match for the literal, the variable has not been declaration, or the
						 * declaration has a spelling error. This token is invalid and cannot be evaluated at runtime.
						 * Throw a DewParseException.
						 */
						if (localPointers.get(token.getDataLiteral()) == null
								&& globalPointers.get(token.getDataLiteral()) == null)
						{
							throw new DewParseException("DataLiteral found in expression this is a non-number and " +
									                            "has no corresponding pointer map. Has the variable " +
									                            "been declared?");
						}

						/*
						 * check local variables for a pointer match
						 */
						if (!localPointers.isEmpty() && localPointers.get(token.getDataLiteral()) != null)
						{
							for (ParseTokens.Variable v : localVariables)
							{
								if (v.pointer == localPointers.get(token.getDataLiteral()))
								{
									/*
									 * If the variable is final, push a literal not a pointer
									 */
									if (v.isFinal)
									{
										outputQueue.add(new DewLexer.LexicalTokens.DataLiteralToken(-1, -1, -1L,
										                                                            v.getValue()
										                                                             .toString()));
									}
									/*
									 * If the variable isn't final, push the variable's pointer
									 */
									else
									{
										outputQueue.add(new ParseTokens.VariablePointer(v.pointer));
									}

									continue expressionLoop;
								}
							}
						}

						/*
						 * check global variables for a pointer match
						 */
						if (!globalPointers.isEmpty() && globalPointers.get(token.getDataLiteral()) != null)
						{
							for (ParseTokens.Variable v : globalVariables)
							{
								if (v.pointer == globalPointers.get(token.getDataLiteral()))
								{
									/*
									 * If the variable is final, push a literal not a pointer
									 */
									if (v.isFinal)
									{
										outputQueue.add(new DewLexer.LexicalTokens.DataLiteralToken(-1, -1, -1L,
										                                                            v.getValue()
										                                                             .toString()));
									}
									/*
									 * If the variable isn't final, push the variable's pointer
									 */
									else
									{
										outputQueue.add(new ParseTokens.VariablePointer(v.pointer));
									}

									continue expressionLoop;
								}
							}
						}

						/*
						 * If there is no match for the literal, the variable has not been declaration, or the
						 * declaration has a spelling error. This token is invalid and cannot be evaluated at runtime.
						 * Throw a DewParseException.
						 */
						throw new DewParseException("DataLiteral found in expression this is a non-number and " +
								                            "has no corresponding pointer map. Has the variable been" +
								                            " " +
								                            "declared?");
					}

				}
				else if (expressionToken instanceof DewLexer.LexicalTokens.SyntaticToken)
				{
					/*
					 * If a string is found, all added elements must be read as a string. E.G. "Hello" + 3 + 2 + "."
					 * evaluates to Hello32.
					 */
					//TODO implement string in expression parsing
					if (evalAsString)
					{
					}
					/*
					 * Not evaluating as a string.
					 */
					else
					{
						DewLexer.LexicalTokens.SyntaticToken syntaticToken = (DewLexer.LexicalTokens.SyntaticToken)
								expressionToken;

						/*
						 * If a string quote is found, throw an exception; these aren't supported yet.
						 */
						if (syntaticToken.getElement() == DewLexer.LexicalTokens.SyntaticToken.SyntaxElement
								.STRING_QUOTE)
						{
							throw new DewParseException("Strings are not yet supported at the expression level.");
						}

						/*
						 * If the syntatic token is not valid for an expression.
						 */
						if (syntaticToken.getElement() != DewLexer.LexicalTokens.SyntaticToken.SyntaxElement
								.OPERATOR_MATH_MUL
								&& syntaticToken.getElement() != DewLexer.LexicalTokens.SyntaticToken.SyntaxElement
								.OPERATOR_MATH_DIV
								&& syntaticToken.getElement() != DewLexer.LexicalTokens.SyntaticToken.SyntaxElement
								.OPERATOR_MATH_MOD
								&& syntaticToken.getElement() != DewLexer.LexicalTokens.SyntaticToken.SyntaxElement
								.OPERATOR_MATH_ADD
								&& syntaticToken.getElement() != DewLexer.LexicalTokens.SyntaticToken.SyntaxElement
								.OPERATOR_MATH_SUB
								&& syntaticToken.getElement() != DewLexer.LexicalTokens.SyntaticToken.SyntaxElement
								.DELIMITER_LEFT_PAREN
								&& syntaticToken.getElement() != DewLexer.LexicalTokens.SyntaticToken.SyntaxElement
								.DELIMITER_RIGHT_PAREN)
						{
							throw new DewParseException("Illegal syntax token in expression");
						}

						/*
						 * Shunting yard operator algorithm
						 */
						switch (syntaticToken.getElement())
						{
							/*
							 * Left parens always get pushed to the operator stack
							 */
							case DELIMITER_LEFT_PAREN:
								operatorStack.push(syntaticToken);
								break;
							/*
							 * If a right paren is found all, everything on the operator stack gets put in the queue
							 * until the corresponding left paren is found. Once the left paren is found, the set is
							 * discarded.
							 */
							case DELIMITER_RIGHT_PAREN:
								while (((DewLexer.LexicalTokens.SyntaticToken) operatorStack.peek()).getElement()
										!= DewLexer.LexicalTokens.SyntaticToken.SyntaxElement.DELIMITER_LEFT_PAREN)
								{
									if (operatorStack.isEmpty())
									{
										throw new DewParseException("Unexpected end of expression.");
									}

									outputQueue.add((DewLexer.LexicalTokens.LexicalToken) operatorStack.pop());
								}

								/*
								 * discard the left paren
								 */
								operatorStack.pop();

								break;
							/*
							 * While operators on the stack have a higher precedence, put the operators in the queue.
							 * Then push the operator at hand onto the stack.
							 */
							case OPERATOR_MATH_MUL:
							case OPERATOR_MATH_DIV:
							case OPERATOR_MATH_MOD:
							case OPERATOR_MATH_ADD:
							case OPERATOR_MATH_SUB:
								if (operatorStack.isEmpty())
								{
									operatorStack.push(syntaticToken);
									break;
								}

								/*
								 * If a left paren is present, always push the new operator.
								 */
								if (!operatorStack.isEmpty()
										&& ((DewLexer.LexicalTokens.SyntaticToken) operatorStack.peek()).getElement()
										== DewLexer.LexicalTokens.SyntaticToken.SyntaxElement.DELIMITER_LEFT_PAREN)
								{
									operatorStack.push(syntaticToken);
									break;
								}

								while (!operatorStack.isEmpty()
										&& shuntingYardPrecedence.get(((DewLexer.LexicalTokens.SyntaticToken)
										operatorStack.peek()).getElement())
										> shuntingYardPrecedence.get(syntaticToken.getElement()))
								{
									if (operatorStack.isEmpty())
									{
										throw new DewParseException("Unexpected end of expression.");
									}

									outputQueue.add((DewLexer.LexicalTokens.SyntaticToken) operatorStack.pop());
								}

								operatorStack.push(syntaticToken);

								break;
							default:
								throw new DewParseException("Unexpected break or termination in expression");
						}
					}
				}
			}

			/*
			 * Put all remaining operators onto the queue
			 */
			/*
			for (Object synToken : operatorStack)
			{
				outputQueue.add((SyntaticToken) synToken);
			}
			*/
			while (!operatorStack.isEmpty())
			{
				outputQueue.add((DewLexer.LexicalTokens.SyntaticToken) operatorStack.pop());
			}

			if (logger.isLogging)
			{
				LinkedList<DewInterpreter.Token> dbg = (LinkedList) outputQueue.clone();

				for (DewInterpreter.Token t : dbg)
					logger.log(Logger.LogLevel.INFO, "RPN Token: " + t);
			}

			/*
			 * Return the queue
			 */
			return outputQueue;
		}

		/**
		 * Evaluates an algebraic queue in reverse polish notation to an answer.
		 * @param outputQueue RPN token queue
		 * @param globalVariables global variables
		 * @param localVariables local variables
		 * @param evalType eval type (Integer, Float)
		 * @param logger logger for the function
		 * @return answer
		 * @throws com.normalizedinsanity.rayburn.engine.dew.lang.DewParseException
		 */
		public static Object evalRPN(LinkedList<DewInterpreter.Token> outputQueue,
		                             ArrayList<ParseTokens.Variable> globalVariables,
		                             ArrayList<ParseTokens.Variable> localVariables,
		                             Class evalType,
		                             Logger logger) throws DewParseException
		{
			if (evalType != Integer.class && evalType != Float.class)
			{
				logger.log(Logger.LogLevel.SEVERE, "Algebraic RPN cannot eval to given type: " + evalType);
				return null;
			}



			Stack evalStack = new Stack();

			while (outputQueue.size() > 0)
			{
				DewInterpreter.Token token = outputQueue.remove();
				if (token instanceof DewLexer.LexicalTokens.DataLiteralToken)
				{
					DewLexer.LexicalTokens.DataLiteralToken dataToken = (DewLexer.LexicalTokens.DataLiteralToken)
							token;

					if (isInt(dataToken.getDataLiteral()))
					{
						evalStack.push(Integer.parseInt(dataToken.getDataLiteral()));
					}
					else if (isFloat(dataToken.getDataLiteral()))
					{
						evalStack.push(Float.parseFloat(dataToken.getDataLiteral()));
					}
					else
					{
						throw new DewParseException("Invalid numeric literal");
					}
				}
				else if (token instanceof DewLexer.LexicalTokens.SyntaticToken)
				{
					DewLexer.LexicalTokens.SyntaticToken.SyntaxElement element = ((DewLexer.LexicalTokens
							.SyntaticToken) token).getElement();

					if (evalStack.size() < 2)
					{
						throw new DewParseException("Invalid expression structure: " + token.toString());
					}

					switch (element)
					{
						case OPERATOR_MATH_MUL:
							evalStack.push(mul(evalStack.pop(), evalStack.pop(), Float.class));
							break;
						case OPERATOR_MATH_DIV:
							Object rightDiv = evalStack.pop();
							Object leftDiv = evalStack.pop();

							evalStack.push(div(leftDiv, rightDiv, Float.class));
							break;
						case OPERATOR_MATH_MOD:
							Object rightMod = evalStack.pop();
							Object leftMod = evalStack.pop();

							evalStack.push(mod(leftMod, rightMod, Float.class));
							break;
						case OPERATOR_MATH_ADD:
							evalStack.push(add(evalStack.pop(), evalStack.pop(), Float.class));
							break;
						case OPERATOR_MATH_SUB:
							Object rightSub = evalStack.pop();
							Object leftSub = evalStack.pop();

							evalStack.push(sub(leftSub, rightSub, Float.class));
							break;
					}
				}
			}

			if (evalType == Float.class)
			{
				return castToFloat(evalStack.pop());
			}
			else
			{
				return castToInteger(evalStack.pop());
			}
		}


		/**
		 * Uses the regex from the field genericNumberRegex to determine if a dataLiteralToken is a number.
		 * @param token
		 * @return if the token is a number
		 */
		public static boolean isNumber(DewLexer.LexicalTokens.DataLiteralToken token)
		{
			return isNumber(token.getDataLiteral());
		}

		/**
		 * Uses the regex from the field genericNumberRegex to determine if a String is a number.
		 * @param string
		 * @return if the String is a number
		 */
		public static boolean isNumber(String string)
		{
			return genericNumberRegex.matcher(string).matches();
		}

		/**
		 * @param value
		 * @return is value is an int
		 */
		public static boolean isInt(String value)
		{
			try
			{
				Integer.parseInt(value);
				return true;
			}
			catch (NumberFormatException e)
			{
				return false;
			}
		}

		/**
		 * @param value
		 * @return if value is a float
		 */
		public static boolean isFloat(String value)
		{
			try
			{
				Float.parseFloat(value);
				return true;
			}
			catch (NumberFormatException e)
			{
				return false;
			}
		}
	}

	/**
	 * Branch element interface. Any object that can be attached to a branch must implement this interface.
	 */
	public static interface BranchElement
	{
		public abstract void call();
	}

}
