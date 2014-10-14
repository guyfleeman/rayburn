package com.rayburn.engine.dew.lang;

import java.io.File;
import java.util.ArrayList;

import com.rayburn.engine.dew.DewException;
import com.rayburn.engine.dew.binding.AbstractDewEntityBinding;

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
					 * Dang you ran out of options? I was so nice. Return null. Boo you whore.
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

}
