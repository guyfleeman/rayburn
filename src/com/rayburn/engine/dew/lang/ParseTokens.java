package com.rayburn.engine.dew.lang;

import com.rayburn.engine.dew.DewException;

import java.util.ArrayList;

/**
 * This class is a container for all token used and passed on to the interpreter by the parser.
 */
public class ParseTokens
{
	ParseTokens() {}

	public static class ParseToken extends DewInterpreter.Token
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
		 * @throws com.rayburn.engine.dew.DewException If a value is assigned to a final
		 * variable, an exception is thrown.
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
