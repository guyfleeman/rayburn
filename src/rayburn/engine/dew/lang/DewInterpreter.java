package rayburn.engine.dew.lang;

import org.apache.commons.lang3.mutable.MutableInt;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

import rayburn.engine.dew.bindings.AbstractDewEntityBinding;
import rayburn.engine.util.Logger;

import rayburn.engine.dew.lang.DewInterpreter.DewLexer.LexicalTokens.*;
import rayburn.engine.dew.lang.DewInterpreter.DewLexer.LexicalTokens.SyntaticToken.SyntaxElement;
import rayburn.engine.dew.lang.DewInterpreter.DewLexer.LexicalTokens.VariableDeclarationToken.VariableType;
import rayburn.engine.dew.lang.DewInterpreter.DewLexer.LexicalTokens.ResourceCallToken.ResourceParadigm;
import rayburn.engine.dew.lang.DewInterpreter.DewLexer.LexicalTokens.ResourceOperationToken.ResourceOperation;
import rayburn.engine.dew.lang.DewInterpreter.DewParser.ParseTokens.*;

import static rayburn.engine.dew.lang.DewInterpreter.DewParser.AlgebraicExpressionBranch.*;
import static rayburn.engine.dew.lang.DewInterpreter.DewMathFunctionSet.*;

/**
 * @author guyfleeman
 * @date 3/1/14
 * @version 0.01
 * <p>Everything in this class is Java 5.0 compliant. If you are adding to this class please set the language level of
 * your IDE to Java 5.0 and code appropriately. Thanks.</p>
 */
public class DewInterpreter
{
	private static DewLexer interpreterLexer = DewLexer.getInstance();
	private static DewParser interpreterParser = DewParser.getInstance();
	private static DewInterpreter instance;


	static
	{
		instance = new DewInterpreter();
	}

	private DewInterpreter() {}

	public static DewInterpreter getInstance()
	{
		return instance;
	}

	///////////////////////////////////
	//  end instance initialization  //
	///////////////////////////////////

	public DewLanguageBindingInterface runtimeBinding = null;
	public ArrayList<AbstractDewEntityBinding> objectTree = null;

	public DewScript interpretSource(String name, File scriptSource) throws DewException
	{
		return new DewScript(name, interpreterParser.parse(interpreterLexer.tokenize(scriptSource)));
	}

	public void setRuntimeBinding(DewLanguageBindingInterface runtimeInterface)
	{
		this.runtimeBinding = runtimeInterface;
	}

	public DewLanguageBindingInterface getRuntimeBinding()
	{
		return runtimeBinding;
	}

	public void setRuntimeObjectTree(ArrayList<AbstractDewEntityBinding> objectTree)
	{
		this.objectTree = objectTree;
	}

	public ArrayList<AbstractDewEntityBinding> getRuntimeObjectTree()
	{
		return objectTree;
	}

	public void attachBranch(AbstractDewEntityBinding branch)
	{
		if (branch != null)
			objectTree.add(branch);
	}

	public void run(DewLanguageBindingInterface contextualLanguageBinding,
	                ArrayList<AbstractDewEntityBinding> objectTree,
	                DewScript script)
	{

	}

	public void run(DewScript script)
	{
		run(runtimeBinding, objectTree, script);
	}



	///////////////////////////////
	//  inner class declaration  //
	///////////////////////////////

	public static abstract class Token
	{
		Token() {}
	}

	/**
	 * This class contains the interpreters functions for math operations.
	 */
	public static class DewMathFunctionSet
	{
		public static boolean returnMinInsteadOfNull = false;
		public static boolean allowAddressCastOnMismatch = false;
		public static boolean roundOnIntFloatMismatch = false;

		/**
		 * Casts an object to a float.
		 * @param value value to cast
		 * @return result
		 */
		public static Float castToFloat(Object value)
		{
			/*
			 * If its a float return it.
			 */
			if (value.getClass() == Float.class)
			{
				return (Float) value;
			}
			/*
			 * If it's an int, cast it and return it.
			 */
			else if (value.getClass() == Integer.class)
			{
				return (float) ((Integer) value).intValue();
			}
			else
			{
				/*
				 * Try to parse a float of whatever object is passed
				 */
				try
				{
					return Float.parseFloat(value.toString());
				}
				catch (NumberFormatException e)
				{
					/*
					 * If a float can be parsed and the compiler note says to return MIN_VALUE do so.
					 */
					if (returnMinInsteadOfNull)
					{
						return Float.MIN_VALUE;
					}
					/*
					 * Running out of cast options at this point aren't we? Well if the user says its okay, hash the
					 * object and return that. I guess.
					 */
					else if (allowAddressCastOnMismatch)
					{
						return (float) value.hashCode();
					}
					/*
					 * Dang you ran out of options? I was so nice. Return null. Boo you.
					 */
					else
					{
						return null;
					}
				}
			}
		}

		/**
		 * Casts an object to an int
		 * @param value object to be cast
		 * @return result
		 */
		public static Integer castToInteger(Object value)
		{
			/*
			 * If it's an int return it
			 */
			if (value.getClass() == Integer.class)
			{
				return (Integer) value;
			}
			/*
			 * if it's a float
			 */
			else if (value.getClass() == Float.class)
			{
				/*
				 * If the interpreter is directed to auto-round, round the float and cast it, else just cast
				 */
				if (roundOnIntFloatMismatch)
				{
					float floatVal = (Float) value;

					if (floatVal < 0)
					{
						return (int) (floatVal - 0.5f);
					}
					else
					{
						return (int) (floatVal + 0.5f);
					}
				}
				else
				{
					return (int) ((Float) value).floatValue();
				}
			}
			else
			{
				/*
				 * If you want snarky comments about options see the float cast function
				 */
				try
				{
					return Integer.parseInt(value.toString());
				}
				catch (NumberFormatException e)
				{
					/*
					 * If an int can't be parsed and the compiler note says to return MIN_VALUE, do so.
					 */
					if (returnMinInsteadOfNull)
					{
						return Integer.MIN_VALUE;
					}
					/*
					 * if hashing is allowed, hash the object and return it
					 */
					else if (allowAddressCastOnMismatch)
					{
						return value.hashCode();
					}
					/*
					 * Return null
					 */
					else
					{
						return null;
					}
				}
			}
		}

		/**
		 * Adds two objects numerically. implicitly casts
		 * @param left left operand
		 * @param right right operand
		 * @param returnType return type, forced (int, float)
		 * @return sum
		 */
		public static Object add(Object left,
		                  Object right,
		                  Class returnType)
		{
			if (returnType == Integer.class)
			{
				float leftFloat = castToFloat(left);
				float rightFloat = castToFloat(right);
				return castToInteger(leftFloat + rightFloat);
			}
			else
			{
				return castToFloat(left) + castToFloat(right);
			}
		}

		/**
		 * Subtracts two objects numerically. implicitly casts
		 * @param left left operand
		 * @param right right operand
		 * @param returnType return type, forced (int, float)
		 * @return difference
		 */
		public static Object sub(Object left,
		                  Object right,
		                  Class returnType)
		{
			if (returnType == Integer.class)
			{
				float leftFloat = castToFloat(left);
				float rightFloat = castToFloat(right);
				return castToInteger(leftFloat - rightFloat);
			}
			else
			{
				return castToFloat(left) - castToFloat(right);
			}
		}

		/**
		 * Multiplies two objects numerically. implicitly casts
		 * @param left left operand
		 * @param right right operand
		 * @param returnType return type (int, float)
		 * @return product
		 */
		public static Object mul(Object left,
		                  Object right,
		                  Class returnType)
		{
			if (returnType == Integer.class)
			{
				float leftFloat = castToFloat(left);
				float rightFloat = castToFloat(right);
				return castToInteger(leftFloat * rightFloat);
			}
			else
			{
				return castToFloat(left) * castToFloat(right);
			}
		}

		/**
		 * Divides two objects numerically, implicitly casts
		 * @param left left operand
		 * @param right right operand
		 * @param returnType return type (int, float)
		 * @return quotient
		 */
		public static Object div(Object left,
		                  Object right,
		                  Class returnType)
		{
			if (returnType == Integer.class)
			{
				float leftFloat = castToFloat(left);
				float rightFloat = castToFloat(right);
				return castToInteger(leftFloat / rightFloat);
			}
			else
			{
				return castToFloat(left) / castToFloat(right);
			}
		}

		/**
		 * Modulo's two objects numerically
		 * @param left left operand
		 * @param right right operand
		 * @param returnType return type (int, float)
		 * @return remainder
		 */
		public static Object mod(Object left,
		                  Object right,
		                  Class returnType)
		{
			if (returnType == Integer.class)
			{
				float leftFloat = castToFloat(left);
				float rightFloat = castToFloat(right);
				return castToInteger(leftFloat % rightFloat);
			}
			else
			{
				return castToFloat(left) % castToFloat(right);
			}
		}
	}

	/*
	public static class DewMathRoundingFunctionSet
	{
		private DewMathRoundingFunctionSet() {}

		public static int add(int a, int b)
		{
			return a + b;
		}

		public static float add(float a, float b)
		{
			return a + b;
		}

		public static int add(int a, float b)
		{
			if (b > 0)
				return a + (int)(b + 0.5f);
			else
				return a + (int)(b - 0.5f);
		}

		public static int add(float a, int b)
		{
			if (a > 0)
				return (int)(a + 0.5f) + b;
			else
				return (int)(a - 0.5f) + b;
		}

		public static int sub(int a, int b)
		{
			return a - b;
		}

		public static float sub(float a, float b)
		{
			return a - b;
		}

		public static int sub(int a, float b)
		{
			if (b > 0)
				return a - (int)(b + 0.5f);
			else
				return a - (int)(b - 0.5f);
		}

		public static int sub(float a, int b)
		{
			if (a > 0)
				return (int)(a + 0.5f) - b;
			else
				return (int)(a - 0.5f) - b;
		}

		public static int mul(int a, int b)
		{
			return a * b;
		}

		public static float mul(float a, float b)
		{
			return a * b;
		}

		public static int mul(int a, float b)
		{
			return (int)((float)a * b);
		}

		public static int mul(float a, int b)
		{
			return (int)(a * (float)b);
		}

		public static int div(int a, int b)
		{
			return a / b;
		}

		public static float div(float a, float b)
		{
			return a / b;
		}

		public static int div(int a, float b)
		{
			return (int)((float)a / b);
		}

		public static int div(float a, int b)
		{
			return (int)(a / (float)b);
		}

		public static int mod(int a, int b)
		{
			return a % b;
		}

		public static float mod(float a, float b)
		{
			return a % b;
		}

		public static int mod(int a, float b)
		{
			return (int)((float)a % b);
		}

		public static int mod(float a, int b)
		{
			return (int)(a % (float)b);
		}

		public static int pow(int a, int b)
		{
			return (int)Math.pow(a, b);
		}

		public static float pow(float a, float b)
		{
			return (float)Math.pow(a, b);
		}

		public static int pow(int a, float b)
		{
			return (int)Math.pow(a, b);
		}

		public static int pow(float a, int b)
		{
			return (int)Math.pow(a, b);
		}

		public static int inc(int a)
		{
			return ++a;
		}

		public static float inc(float a)
		{
			return ++a;
		}

		public static int dec(int a)
		{
			return --a;
		}

		public static float dec(float a)
		{
			return --a;
		}
	}
	*/

	public static class DewStringAndCharFunctionSet
	{
		private DewStringAndCharFunctionSet() {}

		public static String concat(String a, String b)
		{
			return a + b;
		}

		public static String concat(char a, char b)
		{
			return "" + a + b;
		}

		public static String concat(String a, char b)
		{
			return a + b;
		}

		public static String concat(char a, String b)
		{
			return "" + a + b;
		}
	}

	public static class DewRelationalOperatorFunctionSet
	{
		private DewRelationalOperatorFunctionSet() {}

		public static boolean eq(boolean a, boolean b)
		{
			return a == b;
		}

		public static boolean eq(float a, float b)
		{
			return a == b;
		}

		public static boolean eq(String a, String b)
		{
			return a.equalsIgnoreCase(b);
		}

		public static boolean gr(float a, float b)
		{
			return a > b;
		}

		public static boolean ls(float a, float b)
		{
			return a < b;
		}

		public static boolean ge(float a, float b)
		{
			return a >= b;
		}

		public static boolean le(float a, float b)
		{
			return a <= b;
		}

		public static boolean ne(boolean a, boolean b)
		{
			return a != b;
		}

		public static boolean ne(float a, float b)
		{
			return a != b;
		}

		public static boolean ne(String a, String b)
		{
			return !a.equalsIgnoreCase(b);
		}
	}

	public static class DewLogicalOperatorFunctionSet
	{
		private DewLogicalOperatorFunctionSet() {}

		public static boolean and(boolean a, boolean b)
		{
			return a && b;
		}

		public static boolean or(boolean a, boolean b)
		{
			return a || b;
		}

		public static boolean not(boolean a)
		{
			return !a;
		}
	}

	/**
	 * @author guyfleeman
	 * @date 03/03/14
	 */
	public static class DewParser
	{
		private static DewParser instance;
		private static Logger parserLogger = new Logger();

		/**
		 * Map linking syntax token to their precedence
		 */
		public static final HashMap<SyntaticToken.SyntaxElement, Integer> shuntingYardPrecedence;

		/**
		 * Regex to identify numbers.
		 */
		public static final Pattern genericNumberRegex = Pattern.compile("^-?\\d+([,\\.]\\d+)?([eE]-?\\d+)?$");

		static
		{
			shuntingYardPrecedence = new HashMap<SyntaticToken.SyntaxElement, Integer>();
			shuntingYardPrecedence.put(SyntaticToken.SyntaxElement.OPERATOR_MATH_MUL, 2);
			shuntingYardPrecedence.put(SyntaticToken.SyntaxElement.OPERATOR_MATH_DIV, 2);
			shuntingYardPrecedence.put(SyntaticToken.SyntaxElement.OPERATOR_MATH_MOD, 2);
			shuntingYardPrecedence.put(SyntaticToken.SyntaxElement.OPERATOR_MATH_ADD, 1);
			shuntingYardPrecedence.put(SyntaticToken.SyntaxElement.OPERATOR_MATH_SUB, 1);

			instance = new DewParser();
		}

		private DewParser() {}

		public static DewParser getInstance()
		{
			return instance;
		}

		///////////////////////////////////
		//  end instance initialization  //
		///////////////////////////////////






		public SyntaxBranch parse(Stack tokenizedDewScript) throws DewException
		{
			SyntaticToken initialTriggerToken;
			SyntaxBranch primaryBranch;
			ArrayList<LexicalToken> scriptTokens = new ArrayList<LexicalToken>();
			ArrayList<Variable> globalVariables = new ArrayList<Variable>();
			HashMap<String, Short> globalPointers = new HashMap<String, Short>();
			MutableInt index = new MutableInt(0);
			short initialDepth = 0;

			for (Object genericToken : tokenizedDewScript)
				scriptTokens.add((LexicalToken) genericToken);

			parserLogger.log(Logger.LogLevel.INFO, "Evaluating token " + scriptTokens.get(0));

			if (!(scriptTokens.get(index.getValue()) instanceof SyntaticToken)
					|| !(((SyntaticToken) scriptTokens.get(index.getValue())).getElement()
						== SyntaxElement.START_OF_FILE))
			{
				parserLogger.log(Logger.LogLevel.FATAL, "Script entry point not found.");
				parserLogger.log(Logger.LogLevel.INFO, "Did DewParser successfully parse source?");
				throw new DewException("Start of file not found. Quitting.");
			}
			else
			{
				parserLogger.log(Logger.LogLevel.INFO, "Valid entry point found. Proceeding.");
				initialTriggerToken = (SyntaticToken) scriptTokens.get(index.getValue());
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
		 * @throws DewException if the syntax rules of DewScript are violated or if the parser experiences an internal
		 *                      error, a DewException will be thrown.
		 */
		private static SyntaxBranch createBranch(LexicalToken branchTriggerToken,
		                                         int branchTriggerTokenIndex,
		                                         ArrayList<LexicalToken> lexicalTokens,
		                                         ArrayList<Variable> globalVariableList,
		                                         ArrayList<Variable> localVariableList,
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

			if (!(branchTriggerToken instanceof SyntaticToken))
			{
				parserLogger.log(Logger.LogLevel.FATAL, "Branch entrancy token not of type SyntaxToken.");
				throw new DewException("Invalid entrance token.");
			}

			/*
			 * Given the entrance token, determine the corresponding token that will signal the branch to break,
			 * ending the branch and stopping its execution.
			 */
			SyntaxElement endOfBranchElement = SyntaxElement.END_OF_STATEMENT;
			switch (((SyntaticToken) branchTriggerToken).getElement())
			{
				case START_OF_FILE: endOfBranchElement = SyntaxElement.END_OF_FILE; break;
				case STRUCTURE_IF:
				case STRUCTURE_ELSEIF:
				case STRUCTURE_ELSE:
				case STRUCTURE_WHILE:
				case STRUCTURE_FOR: endOfBranchElement = SyntaxElement.END_OF_STATEMENT; break;
				case DELIMITER_LEFT_BRACE: endOfBranchElement = SyntaxElement.DELIMITER_RIGHT_BRACE; break;
				case DELIMITER_LEFT_BRACK: endOfBranchElement = SyntaxElement.DELIMITER_RIGHT_BRACK; break;
				case DELIMITER_LEFT_PAREN: endOfBranchElement = SyntaxElement.DELIMITER_RIGHT_PAREN; break;
			}

			parserLogger.log(Logger.LogLevel.INFO, "Exit token determined: " + endOfBranchElement.toString());

			/*
			 * Level specfic copies of resources are made to ensure branch level cnages are not carried up the syntax
			 * tree, only down it. For example, if a local variable is created in a for loop, the scope of the variable
			 * is limited to the context of the loop or any subcontexts below it. The variable's data and pointer
			 * should not be accesssible to higher branches. Branch level copies are made to keep variable (and
			 * eventually function) scope in check.
			 */
			ArrayList<LexicalToken> branchLexicalTokenSubset =
					(ArrayList<LexicalToken>) lexicalTokens.clone();
			ArrayList<Variable> branchVariableSubset =
					(ArrayList<Variable>) localVariableList.clone();
			HashMap<String, Short> branchVariablePointersSubset =
					(HashMap<String, Short>) localVariablePointersMap.clone();

			assert branchLexicalTokenSubset != null;
			assert branchVariableSubset != null;
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
				if (lexicalTokens.get(index.getValue()) instanceof SyntaticToken
						&& ((SyntaticToken) lexicalTokens.get(index.getValue())).getElement()
						== endOfBranchElement)
				{
					parserLogger.log(Logger.LogLevel.INFO, "End of branch token found: "
							+ endOfBranchElement.toString());
					break;
				}

				//if the end of the block execution has been reached, break adn return the branch
				if (lexicalTokens.get(index.getValue()) instanceof SyntaticToken
						&& ((SyntaticToken) lexicalTokens.get(index.getValue())).getElement()
						== SyntaxElement.END_OF_BLOCK)
				{
					parserLogger.log(Logger.LogLevel.INFO, "End of block found.");
					break;
				}

				/*
				 * Check if a syntax element is present. If so, determine if its presence with the surroundings tokens
				 * requres the creation of a subbranch.
				 */
				if (lexicalTokens.get(index.getValue()) instanceof SyntaticToken)
				{
					SyntaxElement syntaxElement = ((SyntaticToken) lexicalTokens.get(index.getValue())).getElement();

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
				if (lexicalTokens.get(index.getValue()) instanceof VariableDeclarationToken)
				{
					/*
					 * Ensure a variable name follows the declaration token. If index + 1 (name token location) is
					 * greater than the size of the list or its not a VariableNameToken, throw a parse exception.
					 */
					if (index.getValue() + 1 > lexicalTokens.size()
							|| !(lexicalTokens.get(index.getValue() + 1) instanceof VariableNameToken))
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

					VariableDeclarationToken decToken =
							((VariableDeclarationToken) lexicalTokens.get(index.getValue()));
					index.increment();
					VariableNameToken nameToken =
							((VariableNameToken) lexicalTokens.get(index.getValue()));
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
									&& lexicalTokens.get(index.getValue()) instanceof SyntaticToken
									&& ((SyntaticToken) lexicalTokens.get(index.getValue())).getElement()
									== SyntaxElement.OPERATOR_EQ)
							{
								index.increment();

								ArrayList<LexicalToken> expressionTokens = new ArrayList<LexicalToken>();
								while (index.getValue() <= lexicalTokens.size()
										&& !(lexicalTokens.get(index.getValue()) instanceof SyntaticToken
												&& ((SyntaticToken) lexicalTokens.get(index.getValue())).getElement()
												== SyntaxElement.END_OF_STATEMENT))
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
							branchVariableSubset.add(new Variable<Boolean>(
									boolValue, Boolean.class, (short) branchVariableSubset.size()));
							continue branchLoop;
							//break;
						case INT:
							int intValue = 0;

							/*
							 * If an equals sign is the next token, calculate the value.
							 */
							if (index.getValue() <= lexicalTokens.size()
									&& lexicalTokens.get(index.getValue()) instanceof SyntaticToken
									&& ((SyntaticToken) lexicalTokens.get(index.getValue())).getElement()
									== SyntaxElement.OPERATOR_EQ)
							{
								index.increment();

								/*
								 * While the end of statement hasn't been reached, gather tokens as expression tokens.
								 */
								ArrayList<LexicalToken> expressionTokens = new ArrayList<LexicalToken>();
								while (index.getValue() <= lexicalTokens.size()
										&& !(lexicalTokens.get(index.getValue()) instanceof SyntaticToken
										&& ((SyntaticToken) lexicalTokens.get(index.getValue())).getElement()
										== SyntaxElement.END_OF_STATEMENT))
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
									LinkedList<Token> expressionQueue = convertNaturalMathToRPN(expressionTokens,
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
							branchVariableSubset.add(new Variable<Integer>(
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
									&& lexicalTokens.get(index.getValue()) instanceof SyntaticToken
									&& ((SyntaticToken) lexicalTokens.get(index.getValue())).getElement()
									== SyntaxElement.OPERATOR_EQ)
							{
								index.increment();

								/*
								 * While the end of statement hasn't been reached, gather tokens as expression tokens.
								 */
								ArrayList<LexicalToken> expressionTokens = new ArrayList<LexicalToken>();
								while (index.getValue() <= lexicalTokens.size()
										&& !(lexicalTokens.get(index.getValue()) instanceof SyntaticToken
										&& ((SyntaticToken) lexicalTokens.get(index.getValue())).getElement()
										== SyntaxElement.END_OF_STATEMENT))
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
									LinkedList<Token> expressionQueue = convertNaturalMathToRPN(expressionTokens,
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
							branchVariableSubset.add(new Variable<Float>(
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
							branchVariableSubset.add(new Variable<Character>(
									'\0', Character.class, (short) branchVariableSubset.size()));
							continue branchLoop;
							//break;
						case STRING:
						default:
							/*
							 * Add a new String variable. Give it a pointer to the size of the local variable's list
							 * size. Give it a default value of "".
							 */
							branchVariableSubset.add(new Variable<String>(
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
			private ArrayList<BranchElement> branchElements;
			private ArrayList<ArrayList<Variable>> variables;

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
			private final LinkedList<LexicalToken> expressionQueue;

			/**
			 * The global varables list.
			 */
			private final ArrayList<Variable> globalVariableList;

			/**
			 * The local variables list.
			 */
			private final ArrayList<Variable> localVariableList;

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
			public AlgebraicExpressionBranch(LinkedList<LexicalToken> expressionQueue,
			                                 ArrayList<Variable> globalVariableList,
			                                 ArrayList<Variable> localVariableList,
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
			public static boolean canEvaluateNaturalMathToConstant(ArrayList<LexicalToken> expressionTokens,
			                                                       ArrayList<Variable> globalVariables,
			                                                       ArrayList<Variable> localVariables,
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
				for (LexicalToken token : expressionTokens)
				{
					/*
					 * If the token is SyntacticToken and does not represent a valid expression syntactic element,
					 * return false.
					 */
					if (token instanceof SyntaticToken)
					{
						SyntaticToken syntacticToken = (SyntaticToken) token;

						if (syntacticToken.getElement() != SyntaxElement.OPERATOR_MATH_MUL
								&& syntacticToken.getElement() != SyntaxElement.OPERATOR_MATH_DIV
								&& syntacticToken.getElement() != SyntaxElement.OPERATOR_MATH_MOD
								&& syntacticToken.getElement() != SyntaxElement.OPERATOR_MATH_ADD
								&& syntacticToken.getElement() != SyntaxElement.OPERATOR_MATH_SUB
								&& syntacticToken.getElement() != SyntaxElement.DELIMITER_LEFT_PAREN
								&& syntacticToken.getElement() != SyntaxElement.DELIMITER_RIGHT_PAREN)
						{
							logger.log(Logger.LogLevel.INFO, "Found invalid syntax element in RPN constant eval " +
									"check. Returning false");
							return false;
						}
					}
					else if (token instanceof DataLiteralToken)
					{
						DataLiteralToken dataToken = (DataLiteralToken) token;

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
								for (Variable v : localVariables)
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
								for (Variable v : globalVariables)
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
			 * @throws DewParseException if a token is passed that is not a valid expression token, or the syntax rules of
			 * the expression are violated, a parse time exception will be thrown
			 */
			public static LinkedList<Token> convertNaturalMathToRPN(ArrayList<LexicalToken> expressionTokens,
			                                                               ArrayList<Variable> globalVariables,
			                                                               ArrayList<Variable> localVariables,
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
				LinkedList<Token> outputQueue = new LinkedList<Token>();
				boolean evalAsString = false;

				/*
				 * Loop tag
				 */
				expressionLoop:
				for (int i = 0; i < expressionTokens.size(); i++)
				{
					LexicalToken expressionToken = expressionTokens.get(i);

					/*
					 * Logic for non-operator literals in the expression (numeric or non-numeric).
					 */
					if (expressionToken instanceof DataLiteralToken)
					{
						DataLiteralToken token = (DataLiteralToken) expressionToken;

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
										"has no corresponding pointer map because the map is empty. Have any variables " +
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
										"has no corresponding pointer map. Has the variable been declared?");
							}

							/*
							 * check local variables for a pointer match
							 */
							if (!localPointers.isEmpty() && localPointers.get(token.getDataLiteral()) != null)
							{
								for (Variable v : localVariables)
								{
									if (v.pointer == localPointers.get(token.getDataLiteral()))
									{
										/*
										 * If the variable is final, push a literal not a pointer
										 */
										if (v.isFinal)
										{
											outputQueue.add(new DataLiteralToken(-1, -1, -1L, v.getValue().toString()));
										}
										/*
										 * If the variable isn't final, push the variable's pointer
										 */
										else
										{
											outputQueue.add(new VariablePointer(v.pointer));
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
								for (Variable v : globalVariables)
								{
									if (v.pointer == globalPointers.get(token.getDataLiteral()))
									{
										/*
										 * If the variable is final, push a literal not a pointer
										 */
										if (v.isFinal)
										{
											outputQueue.add(new DataLiteralToken(-1, -1, -1L, v.getValue().toString()));
										}
										/*
										 * If the variable isn't final, push the variable's pointer
										 */
										else
										{
											outputQueue.add(new VariablePointer(v.pointer));
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
									"has no corresponding pointer map. Has the variable been declared?");
						}

					}
					else if (expressionToken instanceof SyntaticToken)
					{
						/*
						 * If a string is found, all added elements must be read as a string. E.G. "Hello" + 3 + 2 + "."
						 * evaluates to Hello32.
						 */
						//TODO implement string in expression parsing
						if (evalAsString){}
						/*
						 * Not evaluating as a string.
						 */
						else
						{
							SyntaticToken syntaticToken = (SyntaticToken) expressionToken;

							/*
							 * If a string quote is found, throw an exception; these aren't supported yet.
							 */
							if (syntaticToken.getElement() == SyntaticToken.SyntaxElement.STRING_QUOTE)
							{
								throw new DewParseException("Strings are not yet supported at the expression level.");
							}

							/*
							 * If the syntatic token is not valid for an expression.
							 */
							if (syntaticToken.getElement() != SyntaxElement.OPERATOR_MATH_MUL
									&& syntaticToken.getElement() != SyntaxElement.OPERATOR_MATH_DIV
									&& syntaticToken.getElement() != SyntaxElement.OPERATOR_MATH_MOD
									&& syntaticToken.getElement() != SyntaxElement.OPERATOR_MATH_ADD
									&& syntaticToken.getElement() != SyntaxElement.OPERATOR_MATH_SUB
									&& syntaticToken.getElement() != SyntaxElement.DELIMITER_LEFT_PAREN
									&& syntaticToken.getElement() != SyntaxElement.DELIMITER_RIGHT_PAREN)
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
									while (((SyntaticToken) operatorStack.peek()).getElement()
											!= SyntaxElement.DELIMITER_LEFT_PAREN)
									{
										if (operatorStack.isEmpty())
										{
											throw new DewParseException("Unexpected end of expression.");
										}

										outputQueue.add((LexicalToken) operatorStack.pop());
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
											&& ((SyntaticToken) operatorStack.peek()).getElement()
												== SyntaxElement.DELIMITER_LEFT_PAREN)
									{
										operatorStack.push(syntaticToken);
										break;
									}

									while (!operatorStack.isEmpty()
											&& shuntingYardPrecedence.get(((SyntaticToken) operatorStack.peek()).getElement())
											> shuntingYardPrecedence.get(syntaticToken.getElement()))
									{
										if (operatorStack.isEmpty())
										{
											throw new DewParseException("Unexpected end of expression.");
										}

										outputQueue.add((SyntaticToken) operatorStack.pop());
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
					outputQueue.add((SyntaticToken) operatorStack.pop());
				}

				if (logger.isLogging)
				{
					LinkedList<Token> dbg = (LinkedList) outputQueue.clone();

					for (Token t : dbg)
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
			 * @throws DewParseException
			 */
			public static Object evalRPN(LinkedList<Token> outputQueue,
			                             ArrayList<Variable> globalVariables,
			                             ArrayList<Variable> localVariables,
			                             Class evalType,
			                             Logger logger) throws DewParseException
			{
				if (evalType != Integer.class && evalType != Float.class)
				{
					logger.log(Logger.LogLevel.SEVERE, "Algebric RPN cannot eval to given type: " + evalType);
					return null;
				}



				Stack evalStack = new Stack();

				while (outputQueue.size() > 0)
				{
					Token token = outputQueue.remove();
					if (token instanceof DataLiteralToken)
					{
						DataLiteralToken dataToken = (DataLiteralToken) token;

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
					else if (token instanceof SyntaticToken)
					{
						SyntaxElement element = ((SyntaticToken) token).getElement();

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
			public static boolean isNumber(DataLiteralToken token)
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

		/**
		 * This class is a container for all token used and passed on to the interpreter by the parser.
		 */
		public static class ParseTokens
		{
			private ParseTokens() {}

			public static class ParseToken extends Token
			{
				ParseToken() {}
			}

			public static interface ExpressionValueContainer
			{
				public Object getValue();
			}

			/**
			 * This class represents a declared and initialized variable.
			 * @param <E> The class type of the variable
			 */
			public static class Variable<E> implements ExpressionValueContainer
			{
				/**
				 * The value of the variable
				 */
				private E value;

				/**
				 * The class type of the variable. Allows for type checking.
				 */
				public final Class<E> type;

				/**
				 * The pointer to this variable.
				 */
				public final short pointer;

				/**
				 * Is the variable final.
				 */
				public final boolean isFinal;

				/**
				 * Instantiates a non final variable.
				 * @param value the value of the new variable
				 * @param type the type of variable being declared
				 * @param pointer the pointer to the variable
				 */
				public Variable(E value, Class<E> type, short pointer)
				{
					this(value, type, pointer, false);
				}

				/**
				 * Instantiates a non final variable.
				 * @param value the value of the new variable
				 * @param type the type of variable being declared
				 * @param pointer the pointer to the variable
				 * @param isFinal is the variable's value final
				 */
				public Variable(E value, Class<E> type, short pointer, boolean isFinal)
				{
					this.value = value;
					this.type = type;
					this.pointer = pointer;
					this.isFinal = isFinal;
				}

				/**
				 * Assigns a new value to the variable
				 * @param value the new value
				 * @throws DewException If a value is assigned to a final variable, an exception is thrown.
				 */
				public void setValue(E value) throws DewException
				{
					if (isFinal)
						throw new DewException("Cannot assign value to final variable.");

					this.value = value;
				}

				/**
				 * @return the variable's value
				 */
				public E getValue()
				{
					return value;
				}

				/**
				 * @return the variable's class type
				 */
				public Class getType()
				{
					return type;
				}

				/**
				 * @return the variable's pointer
				 */
				public short getPointer()
				{
					return pointer;
				}
			}

			/**
			 * This class hold a reference (pointer) to a variable. It created in the parse stage to replace String
			 * literals representing variable name to save on runtime memory and allow users to implement basic
			 * inter-variable pointer arithmetic.
			 */
			public static class VariablePointer extends ParseToken
			{
				/**
				 * The pointer to the variable (value) an instance of this class represents.
				 */
				private short pointer;

				/**
				 * Default constructor.
				 * @param pointer variable pointer
				 */
				public VariablePointer(short pointer)
				{
					this.pointer = pointer;
				}

				/**
				 * @return the pointer of the variable the instance of this class represents
				 */
				public short getPointer()
				{
					return pointer;
				}

				/**
				 * Sets a new pointer, used when pointer arithmetic repoints an instance of this class to a new variable
				 * (value).
				 * @param pointer the new pointer
				 */
				public void setPointer(short pointer)
				{
					this.pointer = pointer;
				}

				/**
				 * Gets the variable this pointer points to so its value can be retrieved.
				 * @param variables the local variable list
				 * @return the variable pointed to by an instance of this class
				 * @throws DewException throws an exception if no variable is found for the internal pointer. The
				 * reference should be secure at the time of initialization.
				 */
				public Variable getVariable(ArrayList<Variable> variables) throws DewException
				{
					return getVariable(variables, null);
				}

				/**
				 * Gets the variable this pointer points to so its value can be retrieved.
				 * @param localVariables the local variable list
				 * @param globalVariables the global variable list
				 * @return the variable pointed to by an instance of this class
				 * @throws DewException throws an exception if no variable is found for the internal pointer. The
				 * reference should be secure at the time of initialization.
				 */
				public Variable getVariable(ArrayList<Variable> localVariables, ArrayList<Variable> globalVariables)
						throws DewException
				{
					/*
					 * Check local variables first. If the Variable being pointed to locally is found, return it.
					 */
					if (localVariables != null)
						for (Variable v : localVariables)
							if (v.pointer == this.pointer)
								return v;

					/*
					 * If the Variable isn't found locally, search global.
					 */
					if (globalVariables != null)
						for (Variable v : globalVariables)
							if (v.pointer == this.pointer)
								return v;

					/*
					 * If a Variable can't be found, throw an exception, the reference should be secure.
					 */
					throw new DewException("Pointer to secure reference not found. Check manual GC! Parser bug?");
				}
			}

			/**
			 * This class represents a token that will make a system call at runtime.
			 */
			public static class SystemCall
			{

			}
		}
	}

	/**
	 * @author guyfleeman
	 * @date 2/27/14
	 * <b>This class is responsible for performing the lexcial analysis frontend of the lang interpreter.</b>
	 */
	public static class DewLexer
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

			tokenizedScriptStack.push(new SyntaticToken(-1, -1, -1, SyntaxElement.START_OF_FILE));
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
						(tokenizedScriptStack.peek() instanceof SyntaticToken
								&& ((SyntaticToken) tokenizedScriptStack.peek()).getElement() == SyntaxElement.START_OF_FILE)
								|| tokenizedScriptStack.peek() instanceof InterpreterAssertionToken
								|| tokenizedScriptStack.peek() instanceof InterpreterAssertionLiteral))
				{
					tokenizedScriptStack.push(new InterpreterAssertionToken(lineNum, -1, -1));
					tokensTokenized++;
					tokenizedScriptStack.push(new InterpreterAssertionLiteral(lineNum, -1, -1, line.substring(8)));
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
							tokenizedScriptStack.push(new SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									SyntaxElement.STRING_QUOTE));
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
							tokenizedScriptStack.push(new SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									SyntaxElement.END_OF_STATEMENT));
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
							tokenizedScriptStack.push(new SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									SyntaxElement.STRING_QUOTE));
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
							tokenizedScriptStack.push(new SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									SyntaxElement.DELIMITER_LEFT_PAREN));
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
							tokenizedScriptStack.push(new SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									SyntaxElement.DELIMITER_LEFT_BRACE));
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
							tokenizedScriptStack.push(new SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									SyntaxElement.DELIMITER_LEFT_BRACK));
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
							tokenizedScriptStack.push(new SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									SyntaxElement.DELIMITER_RIGHT_PAREN));
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
							tokenizedScriptStack.push(new SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									SyntaxElement.DELIMITER_RIGHT_BRACE));
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
							tokenizedScriptStack.push(new SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									SyntaxElement.DELIMITER_RIGHT_BRACK));
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
								tokenizedScriptStack.push(new SyntaticToken(lineNum,
										charInLineNum,
										scriptCharNum,
										SyntaxElement.OPERATOR_RELATIONAL_EQ));
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
								tokenizedScriptStack.push(new SyntaticToken(lineNum,
										charInLineNum,
										scriptCharNum,
										SyntaxElement.OPERATOR_EQ));
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
								tokenizedScriptStack.push(new SyntaticToken(lineNum,
										charInLineNum,
										scriptCharNum,
										SyntaxElement.OPERATOR_RELATIONAL_GREATER_EQ));
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
								tokenizedScriptStack.push(new SyntaticToken(lineNum,
										charInLineNum,
										scriptCharNum,
										SyntaxElement.OPERATOR_RELATIONAL_GREATER));
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
								tokenizedScriptStack.push(new SyntaticToken(lineNum,
										charInLineNum,
										scriptCharNum,
										SyntaxElement.OPERATOR_RELATIONAL_LESS_EQ));
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
								tokenizedScriptStack.push(new SyntaticToken(lineNum,
										charInLineNum,
										scriptCharNum,
										SyntaxElement.OPERATOR_RELATIONAL_LESS));
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
								tokenizedScriptStack.push(new SyntaticToken(lineNum,
										charInLineNum,
										scriptCharNum,
										SyntaxElement.OPERATOR_RELATIONAL_NOT_EQ));
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
								tokenizedScriptStack.push(new SyntaticToken(lineNum,
										charInLineNum,
										scriptCharNum,
										SyntaxElement.OPERATOR_LOGIC_NOT));
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
							tokenizedScriptStack.push(new SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									SyntaxElement.OPERATOR_LOGIC_AND));
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
							tokenizedScriptStack.push(new SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									SyntaxElement.OPERATOR_LOGIC_OR));
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
								tokenizedScriptStack.push(new SyntaticToken(lineNum,
										charInLineNum,
										scriptCharNum,
										SyntaxElement.INCREMENT));
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
								tokenizedScriptStack.push(new SyntaticToken(lineNum,
										charInLineNum,
										scriptCharNum,
										SyntaxElement.OPERATOR_MATH_ADD_EQ));
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
								tokenizedScriptStack.push(new SyntaticToken(lineNum,
										charInLineNum,
										scriptCharNum,
										SyntaxElement.OPERATOR_MATH_ADD));
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
								tokenizedScriptStack.push(new SyntaticToken(lineNum,
										charInLineNum,
										scriptCharNum,
										SyntaxElement.DECREMENT));
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
								tokenizedScriptStack.push(new SyntaticToken(lineNum,
										charInLineNum,
										scriptCharNum,
										SyntaxElement.OPERATOR_MATH_SUB_EQ));
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
								tokenizedScriptStack.push(new SyntaticToken(lineNum,
										charInLineNum,
										scriptCharNum,
										SyntaxElement.OPERATOR_MATH_SUB));
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
								tokenizedScriptStack.push(new SyntaticToken(lineNum,
										charInLineNum,
										scriptCharNum,
										SyntaxElement.OPERATOR_MATH_MUL_EQ));
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
								tokenizedScriptStack.push(new SyntaticToken(lineNum,
										charInLineNum,
										scriptCharNum,
										SyntaxElement.OPERATOR_MATH_MUL));
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
								tokenizedScriptStack.push(new SyntaticToken(lineNum,
										charInLineNum,
										scriptCharNum,
										SyntaxElement.OPERATOR_MATH_DIV_EQ));
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
								tokenizedScriptStack.push(new SyntaticToken(lineNum,
										charInLineNum,
										scriptCharNum,
										SyntaxElement.OPERATOR_MATH_DIV));
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
								tokenizedScriptStack.push(new SyntaticToken(lineNum,
										charInLineNum,
										scriptCharNum,
										SyntaxElement.OPERATOR_MATH_MOD_EQ));
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
								tokenizedScriptStack.push(new SyntaticToken(lineNum,
										charInLineNum,
										scriptCharNum,
										SyntaxElement.OPERATOR_MATH_MOD));
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
								tokenizedScriptStack.push(new SyntaticToken(lineNum,
										charInLineNum,
										scriptCharNum,
										SyntaxElement.OPERATOR_MATH_POW_EQ));
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
								tokenizedScriptStack.push(new SyntaticToken(lineNum,
										charInLineNum,
										scriptCharNum,
										SyntaxElement.OPERATOR_MATH_POW));
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
								tokenizedScriptStack.push(new SyntaticToken(lineNum,
										charInLineNum,
										scriptCharNum,
										SyntaxElement.OPERATOR_APPEND_PROPERTY));
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
								tokenizedScriptStack.push(new ResourceHierarchyIdentifier(lineNum,
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

			tokenizedScriptStack.push(new SyntaticToken(lineNum,
					charInLineNum,
					scriptCharNum,
					SyntaxElement.END_OF_FILE));
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
							"   @L" + ((LexicalToken) o).lineNum +
							" LC" + ((LexicalToken) o).charInLineNum +
							" SC" + ((LexicalToken) o).scriptCharNum);
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
			tokenizedScriptStack.push(new SyntaticToken(-1, -1, -1, SyntaxElement.START_OF_FILE));
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
						(tokenizedScriptStack.peek() instanceof SyntaticToken
								&& ((SyntaticToken) tokenizedScriptStack.peek()).getElement() == SyntaxElement.START_OF_FILE)
								|| tokenizedScriptStack.peek() instanceof InterpreterAssertionToken
								|| tokenizedScriptStack.peek() instanceof InterpreterAssertionLiteral))
				{
					//log the assertions
					tokenizedScriptStack.push(new InterpreterAssertionToken(lineNum, -1, -1));
					tokensTokenized++;
					tokenizedScriptStack.push(new InterpreterAssertionLiteral(lineNum, -1, -1, line.substring(8)));
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
							tokenizedScriptStack.push(new SyntaticToken(lineNum,
									charInLineNum,
									scriptCharNum,
									SyntaxElement.STRING_QUOTE));
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
						SyntaxElement elementToPush = null;

						/*
						 * Cast the char to an int so a much cleaner switch block can be used. Switch with strings/chars
						 * is not supported by Java 5.1 and we are trying to keep the DewInterpreter complinat with 5.1.
						 */
						switch ((int) line.charAt(i))
						{
							/*
							 * Check for EOL and EOS char
							 */
							case (int) ';': elementToPush = SyntaxElement.END_OF_STATEMENT; break;
							/*
							 * Check for a double quote char, moving the lexer to a string literal interpretation state.
							 */
							case (int) '\"':
								elementToPush = SyntaxElement.STRING_QUOTE;
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
							case (int) '(': elementToPush = SyntaxElement.DELIMITER_LEFT_PAREN; break;
							case (int) '{': elementToPush = SyntaxElement.DELIMITER_LEFT_BRACE; break;
							case (int) '[': elementToPush = SyntaxElement.DELIMITER_LEFT_BRACK; break;
							case (int) ')': elementToPush = SyntaxElement.DELIMITER_RIGHT_PAREN; break;
							case (int) '}': elementToPush = SyntaxElement.DELIMITER_RIGHT_BRACE; break;
							case (int) ']': elementToPush = SyntaxElement.DELIMITER_RIGHT_BRACK; break;
							/*
							 * Check for logical and mathematic syntax chars and sequences. This includes =, ==,  >, >=,
							 * <, <=, !, !=, &, |, +, ++, +=, -, --, -=, *, *=, /, /=, %, and %=
							 */
							case (int) '=':
								if (i + 1 < line.length() && line.charAt(i + 1) == '=')
								{
									elementToPush = SyntaxElement.OPERATOR_RELATIONAL_EQ;
									i++;
								}
								else
								{
									elementToPush = SyntaxElement.OPERATOR_EQ;
								}
								break;
							case (int) '>':
								if (i + 1 < line.length() && line.charAt(i + 1) == '=')
								{
									elementToPush = SyntaxElement.OPERATOR_RELATIONAL_GREATER_EQ;
									i++;
								}
								else
								{
									elementToPush = SyntaxElement.OPERATOR_RELATIONAL_GREATER;
								}
								break;
							case (int) '<':
								if (i + 1 < line.length() && line.charAt(i + 1) == '=')
								{
									elementToPush = SyntaxElement.OPERATOR_RELATIONAL_LESS_EQ;
									i++;
								}
								else
								{
									elementToPush = SyntaxElement.OPERATOR_RELATIONAL_LESS;
								}
								break;
							case (int) '!':
								if (i + 1 < line.length() && line.charAt(i + 1) == '=')
								{
									elementToPush = SyntaxElement.OPERATOR_RELATIONAL_NOT_EQ;
									i++;
								}
								else
								{
									elementToPush = SyntaxElement.OPERATOR_LOGIC_NOT;
								}
								break;
							case (int) '&': elementToPush = SyntaxElement.OPERATOR_LOGIC_AND; break;
							case (int) '|': elementToPush = SyntaxElement.OPERATOR_LOGIC_OR; break;
							case (int) '+':
								if (i + 1 < line.length() && line.charAt(i + 1) == '+')
								{
									elementToPush = SyntaxElement.INCREMENT;
									i++;
								}
								else if (i + 1 < line.length() && line.charAt(i + 1) == '=')
								{
									elementToPush = SyntaxElement.OPERATOR_MATH_ADD_EQ;
									i++;
								}
								else
								{
									elementToPush = SyntaxElement.OPERATOR_MATH_ADD;
								}
								break;
							case (int) '-':
								if (i + 1 < line.length() && line.charAt(i + 1) == '-')
								{
									elementToPush = SyntaxElement.DECREMENT;
									i++;
								}
								else if (i + 1 < line.length() && line.charAt(i + 1) == '=')
								{
									elementToPush = SyntaxElement.OPERATOR_MATH_SUB_EQ;
									i++;
								}
								else
								{
									elementToPush = SyntaxElement.OPERATOR_MATH_SUB;
								}
								break;
							case (int) '*':
								if (i + 1 < line.length() && line.charAt(i + 1) == '=')
								{
									elementToPush = SyntaxElement.OPERATOR_MATH_MUL_EQ;
									i++;
								}
								else
								{
									elementToPush = SyntaxElement.OPERATOR_MATH_MUL;
								}
								break;
							case (int) '/':
								if (i + 1 < line.length() && line.charAt(i + 1) == '=')
								{
									elementToPush = SyntaxElement.OPERATOR_MATH_DIV_EQ;
									i++;
								}
								else
								{
									elementToPush = SyntaxElement.OPERATOR_MATH_DIV;
								}
								break;
							case (int) '%':
								if (i + 1 < line.length() && line.charAt(i + 1) == '=')
								{
									elementToPush = SyntaxElement.OPERATOR_MATH_MOD_EQ;
									i++;
								}
								else
								{
									elementToPush = SyntaxElement.OPERATOR_MATH_MOD;
								}
								break;
							/*
							 * Check for resource append and heiarchy identifiers.
							 */
							case (int) '.':
								// .=
								if (i + 1 < line.length() && line.charAt(i + 1) == '=')
								{
									elementToPush = SyntaxElement.OPERATOR_APPEND_PROPERTY;
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
									tokenizedScriptStack.push(new ResourceHierarchyIdentifier(lineNum,
											charInLineNum,
											scriptCharNum));
									tokensTokenized++;
								}
								break;
							case (int) '$': elementToPush = SyntaxElement.POINTER;

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

							tokenizedScriptStack.push(new SyntaticToken(lineNum,
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
			tokenizedScriptStack.push(new SyntaticToken(lineNum,
					charInLineNum,
					scriptCharNum,
					SyntaxElement.END_OF_FILE));
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
							"   @L" + ((LexicalToken) o).lineNum +
							" LC" + ((LexicalToken) o).charInLineNum +
							" SC" + ((LexicalToken) o).scriptCharNum);
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

				if (stack.peek() instanceof ResourceCallToken ||
						stack.peek() instanceof ResourceOperationToken)
				{
					stack.push(new SyntaticToken(lineNum,
							charInLineNum,
							scriptCharNum,
							SyntaxElement.STRING_QUOTE));
					tokensTokenized++;

					stack.push(new ResourcePointerToken(lineNum,
							charInLineNum,
							scriptCharNum,
							dataLiteral.toString()));
					tokensTokenized++;

					dataLiteral.delete(0, dataLiteral.length());
					return tokensTokenized;
				}
				else
				{
					stack.push(new SyntaticToken(lineNum,
							charInLineNum,
							scriptCharNum,
							SyntaxElement.STRING_QUOTE));
					tokensTokenized++;

					stack.push(new DataLiteralToken(lineNum,
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
					stack.push(new SyntaticToken(lineNum,
							charInLineNum,
							scriptCharNum,
							SyntaxElement.END_OF_BLOCK));
					tokensTokenized++;
				}
				else if (dataNoWhiteSpace.equals("if"))
				{
					stack.push(new SyntaticToken(lineNum,
							charInLineNum,
							scriptCharNum,
							SyntaxElement.STRUCTURE_IF));
					tokensTokenized++;
				}
				else if (dataNoWhiteSpace.equals("elseif"))
				{
					stack.push(new SyntaticToken(lineNum,
							charInLineNum,
							scriptCharNum,
							SyntaxElement.STRUCTURE_ELSEIF));
					tokensTokenized++;
				}
				else if (dataNoWhiteSpace.equals("else"))
				{
					stack.push(new SyntaticToken(lineNum,
							charInLineNum,
							scriptCharNum,
							SyntaxElement.STRUCTURE_ELSE));
					tokensTokenized++;
				}
				else if (dataNoWhiteSpace.equals("while"))
				{
					stack.push(new SyntaticToken(lineNum,
							charInLineNum,
							scriptCharNum,
							SyntaxElement.STRUCTURE_WHILE));
					tokensTokenized++;
				}
				else if (dataNoWhiteSpace.equals("true"))
				{
					stack.push(new BooleanValueLiteralToken(lineNum,
							charInLineNum,
							scriptCharNum,
							BooleanValueLiteralToken.BoolValue.TRUE));
					tokensTokenized++;
				}
				else if (dataNoWhiteSpace.equals("false"))
				{
					stack.push(new BooleanValueLiteralToken(lineNum,
							charInLineNum,
							scriptCharNum,
							BooleanValueLiteralToken.BoolValue.FLASE));
					tokensTokenized++;
				}
				else if (dataNoWhiteSpace.equals("intern"))
				{
					stack.push(new ResourceCallToken(lineNum,
							charInLineNum,
							scriptCharNum,
							ResourceParadigm.INTERNAL));
					tokensTokenized++;
				}
				else if (dataNoWhiteSpace.equals("extern"))
				{
					stack.push(new ResourceCallToken(lineNum,
							charInLineNum,
							scriptCharNum,
							ResourceParadigm.EXTERNAL));
					tokensTokenized++;
				}
				else if (dataNoWhiteSpace.equals("load"))
				{
					if (stack.peek() instanceof ResourceCallToken
							&& ((ResourceCallToken) stack.peek()).getResourceParadigm()
							== ResourceParadigm.EXTERNAL)
					{
						stack.push(new ResourceOperationToken(lineNum,
								charInLineNum,
								scriptCharNum,
								ResourceOperation.LOAD));
						tokensTokenized++;
					}
					else
					{
						stack.push(new DataLiteralToken(lineNum,
								charInLineNum,
								scriptCharNum,
								data));
						tokensTokenized++;
					}
				}
				else if (dataNoWhiteSpace.equals("exec"))
				{
					if (stack.peek() instanceof ResourceCallToken
							/* && ((ResourceCallToken) stack.peek()).getResourceParadigm()
							== ResourceParadigm.EXTERNAL*/)
					{
						stack.push(new ResourceOperationToken(lineNum,
								charInLineNum,
								scriptCharNum,
								ResourceOperation.EXEC));
						tokensTokenized++;
					}
					else
					{
						stack.push(new DataLiteralToken(lineNum,
								charInLineNum,
								scriptCharNum,
								data));
						tokensTokenized++;
					}
				}
				else if (dataNoWhiteSpace.equals("bool"))
				{
					stack.push(new VariableDeclarationToken(lineNum,
							charInLineNum,
							scriptCharNum,
							VariableType.BOOL));
					tokensTokenized++;
				}
				else if (dataNoWhiteSpace.equals("int"))
				{
					stack.push(new VariableDeclarationToken(lineNum,
							charInLineNum,
							scriptCharNum,
							VariableType.INT));
					tokensTokenized++;
				}
				else if (dataNoWhiteSpace.equals("float"))
				{
					stack.push(new VariableDeclarationToken(lineNum,
							charInLineNum,
							scriptCharNum,
							VariableType.FLOAT));
					tokensTokenized++;
				}
				else if (dataNoWhiteSpace.equals("char"))
				{
					stack.push(new VariableDeclarationToken(lineNum,
							charInLineNum,
							scriptCharNum,
							VariableType.CHAR));
					tokensTokenized++;
				}
				else if (dataNoWhiteSpace.equals("string"))
				{
					stack.push(new VariableDeclarationToken(lineNum,
							charInLineNum,
							scriptCharNum,
							VariableType.STRING));
					tokensTokenized++;
				}
				else
				{
					if (stack.peek() instanceof ResourceCallToken ||
							stack.peek() instanceof ResourceOperationToken ||
							stack.peek() instanceof ResourceHierarchyIdentifier)
					{
						stack.push(new ResourcePointerToken(lineNum,
								charInLineNum,
								scriptCharNum,
								data));
						tokensTokenized++;
					}
					else if (stack.peek() instanceof VariableDeclarationToken)
					{
						stack.push(new VariableNameToken(lineNum,
								charInLineNum,
								scriptCharNum,
								data));
						tokensTokenized++;
					}
					else
					{
						stack.push(new DataLiteralToken(lineNum,
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
		 * @see LexicalToken
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
			public static abstract class LexicalToken extends Token
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
			public static class InterpreterAssertionToken extends LexicalToken
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
			public static class InterpreterAssertionLiteral extends LexicalToken
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
			public static class SyntaticToken extends LexicalToken
			{
				/**
				 * The syntax element value.
				 */
				private SyntaxElement element;

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
				public SyntaticToken(int line, int charInLine, long scriptChar, SyntaxElement element)
				{
					super(line, charInLine, scriptChar);
					this.element = element;
				}

				/**
				 * @return the syntactic element
				 */
				public SyntaxElement getElement()
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
			public static class VariableDeclarationToken extends LexicalToken
			{
				/**
				 * The variable type being initialized.
				 */
				private VariableType variableType;

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
				public VariableDeclarationToken(int line, int charInLine, long scriptChar, VariableType variableType)
				{
					super(line, charInLine, scriptChar);
					this.variableType = variableType;
				}

				/**
				 * @return the variable type
				 */
				public VariableType getVariableType()
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
			public static class VariableNameToken extends LexicalToken
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
			public static class BooleanValueLiteralToken extends LexicalToken
			{
				/**
				 * the boolean literal value
				 */
				private BoolValue value;

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
				public BooleanValueLiteralToken(int line, int charInLine, long scriptChar, BoolValue value)
				{
					super(line, charInLine, scriptChar);
					this.value = value;
				}

				/**
				 * @return the value of the boolean literal
				 */
				public BoolValue getValue()
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
			public static class ResourceHierarchyIdentifier extends LexicalToken
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
			public static class ResourceCallToken extends LexicalToken
			{
				/**
				 * The paradigm of the resource
				 */
				private ResourceParadigm resourceParadigm;

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
				public ResourceCallToken(int line, int charInLine, long scriptChar, ResourceParadigm resourceParadigm)
				{
					super(line, charInLine, scriptChar);
					this.resourceParadigm = resourceParadigm;
				}

				/**
				 * @return the resource paradigm
				 */
				public ResourceParadigm getResourceParadigm()
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
			public static class ResourceOperationToken extends LexicalToken
			{
				/**
				 * The operation performed on a resource
				 */
				private ResourceOperation resourceOperation;

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
				public ResourceOperationToken(int line, int charInLine, long scriptChar, ResourceOperation resourceOperation)
				{
					super(line, charInLine, scriptChar);
					this.resourceOperation = resourceOperation;
				}

				/**
				 * @return the resource operation
				 */
				public ResourceOperation getResourceOperation()
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
			public static class ResourcePointerToken extends LexicalToken
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
			public static class DataLiteralToken extends LexicalToken
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

	/**
	 * Generic exception class for the lang interpreter.
	 */
	public static class DewException extends Exception
	{
		//TODO add exception line print info for debugging purposes

		public DewException() {}

		/**
		 * @param message
		 */
		public DewException(String message)
		{
			super(message);
		}

		/**
		 * @param cause
		 */
		public DewException(Throwable cause)
		{
			super(cause);
		}

		/**
		 * @param message
		 * @param cause
		 */
		public DewException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public String toString()
		{
			return "DEW EXCEPTION!" + getMessage();
		}
	}

	public static class DewParseException extends DewException
	{
		/**
		 * @param message
		 */
		DewParseException(String message)
		{
			super(message);
		}
	}

	public static class DewRuntimeException extends DewException
	{
		public DewRuntimeException() {}

		/**
		 * @param message
		 */
		public DewRuntimeException(String message)
		{
			super(message);
		}

		/**
		 * @param cause
		 */
		public DewRuntimeException(Throwable cause)
		{
			super(cause);
		}

		/**
		 * @param message
		 * @param cause
		 */
		public DewRuntimeException(String message, Throwable cause)
		{
			super(message, cause);
		}
	}
}
