package com.rayburn.engine.util.debug.console;

import com.rayburn.engine.util.interfaces.Activity;
import com.rayburn.engine.util.interfaces.CommandBuffer;
import com.rayburn.engine.util.interfaces.Triggerable;
import org.apache.commons.lang3.ArrayUtils;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.lwjgl.input.Keyboard.*;

/**
 * @author willstuckey
 * @date 10/9/14 <p>This class comprehensively handles input for an in-render console. International keyboards can be
 * supported by overriding the Runnable field entitled keyHandler.</p>
 */
public class RayburnConsoleInputRegister<E extends Triggerable & CommandBuffer> implements Activity
{
	private boolean running          = false;
	private boolean capsLock         = false;
	private boolean shiftDown        = false;
	private int     cmdRefernceIndex = -1;
	private int     commandsInQueue  = 0;
	private long    invocationTime   = System.nanoTime();

	private       E                        conTarget         = null;
	private       ScheduledFuture<?>       keyListenerHandle = null;
	private final ScheduledExecutorService scheduler         = Executors.newScheduledThreadPool(1);

	/**
	 * Override this to create support for international keyboards
	 */
	protected final Runnable keyHandler = new Runnable()
	{
		public void run()
		{
			/*
			 * while the keyboard has keys to process (and an external kil request is not pending)
			 */
			while (next() && running)
			{
				/*
				 * if the key is presses and was pressed after the runnable was actually invoked
				 */
				if (getEventKeyState() && getEventNanoseconds() > invocationTime)
				{
					/*
					 * set default key to null
					 */
					char addition = '\0';
					switch (getEventKey())
					{
						/*
						 * Event keys
						 */
						/*
						 * enter key, reset command history reference index, and process the command by triggering the
						 * handler in the provided console class
						 */
						case KEY_RETURN:
							/*
							 *
							 */
							if (conTarget.getCommandHistory() != null)
							{
								if (commandsInQueue < conTarget.getCommandHistory().length)
								{
									commandsInQueue++;
								}
								cmdRefernceIndex = -1;
							}

							conTarget.trigger();
							break;
						/*
						 * backspace, removes the last non null character in the input buffer
						 */
						case KEY_BACK:
							if (ArrayUtils.contains(conTarget.getInputBuffer(), '\0'))
							{
								int nullIndex = ArrayUtils.indexOf(conTarget.getInputBuffer(), '\0');
								if (nullIndex > 0)
								{
									conTarget.getInputBuffer()[nullIndex - 1] = '\0';
								}
							}
							break;
						/*
						 * shifts
						 */
						case KEY_LSHIFT:
						case KEY_RSHIFT:
							shiftDown = true;
							break;
						/*
						 * caps toggle
						 */
						case KEY_CAPITAL:
							capsLock = !capsLock;
							break;
						/*
						 * up key, needs to access command history indices
						 */
						case KEY_UP:
							/*
							 * if the reference can legally be incremented
							 */
							if (cmdRefernceIndex < Integer.MAX_VALUE && cmdRefernceIndex < commandsInQueue - 1)
							{
								/*
								 * if command history is available (not disabled by non-default constructor in console
								 * class)
								 */
								if (conTarget.getCommandHistory() != null)
								{
									/*
									 * increment the index and copy the reference's characters into the input buffer,
									 * while preserving the max length
									 */
									cmdRefernceIndex++;
									System.arraycopy(conTarget.getCommandHistory()[cmdRefernceIndex].toCharArray(),
									                 0,
									                 conTarget.getInputBuffer(),
									                 0,
									                 conTarget.getCommandHistory()[cmdRefernceIndex]
											                 .toCharArray().length >
											                 conTarget.getInputBuffer().length
									                 ? conTarget.getInputBuffer().length
									                 : conTarget.getCommandHistory()[cmdRefernceIndex]
											                 .toCharArray().length);
								}
							}
							break;
						/*
						 * down key, needs to access command history indices and clear the line if command history index
						 * cannot be decremented
						 */
						case KEY_DOWN:
							/*
							 * if the index can be decremented, look through command history
							 */
							if (cmdRefernceIndex > 0)
							{
								if (conTarget.getCommandHistory() != null)
								{
									cmdRefernceIndex--;
									System.arraycopy(conTarget.getCommandHistory()[cmdRefernceIndex].toCharArray(),
									                 0,
									                 conTarget.getInputBuffer(),
									                 0,
									                 conTarget.getCommandHistory()[cmdRefernceIndex]
											                 .toCharArray().length >
											                 conTarget.getInputBuffer().length
									                 ? conTarget.getInputBuffer().length
									                 : conTarget.getCommandHistory()[cmdRefernceIndex]
											                 .toCharArray().length);
								}
							}
							/*
							 * if the index can not be decremented, clear the line
							 */
							else if (cmdRefernceIndex <= 0)
							{
								cmdRefernceIndex = -1;
								conTarget.clearInputBuffer();
							}
							break;

						/*
						 * Alphas
						 */
						case KEY_A:
							addition = handleKey('a', isCap());
							break;
						case KEY_B:
							addition = handleKey('b', isCap());
							break;
						case KEY_C:
							addition = handleKey('c', isCap());
							break;
						case KEY_D:
							addition = handleKey('d', isCap());
							break;
						case KEY_E:
							addition = handleKey('e', isCap());
							break;
						case KEY_F:
							addition = handleKey('f', isCap());
							break;
						case KEY_G:
							addition = handleKey('g', isCap());
							break;
						case KEY_H:
							addition = handleKey('h', isCap());
							break;
						case KEY_I:
							addition = handleKey('i', isCap());
							break;
						case KEY_J:
							addition = handleKey('j', isCap());
							break;
						case KEY_K:
							addition = handleKey('k', isCap());
							break;
						case KEY_L:
							addition = handleKey('l', isCap());
							break;
						case KEY_M:
							addition = handleKey('m', isCap());
							break;
						case KEY_N:
							addition = handleKey('n', isCap());
							break;
						case KEY_O:
							addition = handleKey('o', isCap());
							break;
						case KEY_P:
							addition = handleKey('p', isCap());
							break;
						case KEY_Q:
							addition = handleKey('q', isCap());
							break;
						case KEY_R:
							addition = handleKey('r', isCap());
							break;
						case KEY_S:
							addition = handleKey('s', isCap());
							break;
						case KEY_T:
							addition = handleKey('t', isCap());
							break;
						case KEY_U:
							addition = handleKey('u', isCap());
							break;
						case KEY_V:
							addition = handleKey('v', isCap());
							break;
						case KEY_W:
							addition = handleKey('w', isCap());
							break;
						case KEY_X:
							addition = handleKey('x', isCap());
							break;
						case KEY_Y:
							addition = handleKey('y', isCap());
							break;
						case KEY_Z:
							addition = handleKey('z', isCap());
							break;

						/*
						 * numbers
						 */
						case KEY_9:
							addition = handleKey('9', isCap());
							break;
						case KEY_NUMPAD9:
							addition = '9';
							break;
						case KEY_8:
							addition = handleKey('8', isCap());
							break;
						case KEY_NUMPAD8:
							addition = '8';
							break;
						case KEY_7:
							addition = handleKey('7', isCap());
							break;
						case KEY_NUMPAD7:
							addition = '7';
							break;
						case KEY_6:
							addition = handleKey('6', isCap());
							break;
						case KEY_NUMPAD6:
							addition = '6';
							break;
						case KEY_5:
							addition = handleKey('5', isCap());
							break;
						case KEY_NUMPAD5:
							addition = '5';
							break;
						case KEY_4:
							addition = handleKey('4', isCap());
							break;
						case KEY_NUMPAD4:
							addition = '4';
							break;
						case KEY_3:
							addition = handleKey('3', isCap());
							break;
						case KEY_NUMPAD3:
							addition = '3';
							break;
						case KEY_2:
							addition = handleKey('2', isCap());
							break;
						case KEY_NUMPAD2:
							addition = '2';
							break;
						case KEY_1:
							addition = handleKey('1', isCap());
							break;
						case KEY_NUMPAD1:
							addition = '1';
							break;
						case KEY_0:
							addition = handleKey('0', isCap());
							break;
						case KEY_NUMPAD0:
							addition = '0';
							break;

						/*
						 * Symbols
						 */
						case KEY_SPACE:
							addition = ' ';
							break;
						case KEY_COLON:
							addition = ':';
							break;
						case KEY_SEMICOLON:
							addition = ';';
							break;
						case KEY_CIRCUMFLEX:
							addition = '^';
							break;
						case KEY_AT:
							addition = '@';
							break;
						case KEY_ADD:
							addition = '+';
							break;
						case KEY_SUBTRACT:
						case KEY_MINUS:
							addition = '-';
							break;
						case KEY_MULTIPLY:
							addition = '*';
							break;
						case KEY_DIVIDE:
							addition = '/';
							break;
						case KEY_UNDERLINE:
							addition = '_';
							break;
						case KEY_EQUALS:
							addition = handleKey('=', isCap());
							break;
						case KEY_NUMPADEQUALS:
							addition = '=';
							break;
						case KEY_LBRACKET:
							addition = handleKey('[', isCap());
							break;
						case KEY_RBRACKET:
							addition = handleKey(']', isCap());
							break;
						case KEY_BACKSLASH:
							addition = handleKey('\\', isCap());
							break;
						case KEY_NONE:
							addition = handleKey('\\', isCap());
							break;
						case KEY_APOSTROPHE:
							addition = handleKey('\'', isCap());
							break;
						case KEY_COMMA:
							addition = handleKey(',', isCap());
							break;
						case KEY_NUMPADCOMMA:
							addition = ',';
							break;
						case KEY_DECIMAL:
							addition = '.';
							break;
						case KEY_PERIOD:
							addition = handleKey('.', isCap());
							break;
						case KEY_SLASH:
							addition = handleKey('/', isCap());
							break;
					}

					/*
					 * add the key if its valid
					 */
					if (addition != '\0' && ArrayUtils.contains(conTarget.getInputBuffer(), '\0'))
					{
						conTarget.getInputBuffer()[ArrayUtils.indexOf(conTarget.getInputBuffer(), '\0')] = addition;
					}
				}
				/*
				 * key releases
				 */
				else
				{
					switch (getEventKey())
					{
						/*
						 * shifts
						 */
						case KEY_LSHIFT:
						case KEY_RSHIFT:
							shiftDown = false;
							break;
					}
				}
			}
		}
	};

	/**
	 * Default constructor
	 * @param conTarget, the console object the input register will use. Needs to implement CommandBuffer for writing
	 *                      and triggerable for triggering command responses
	 */
	public RayburnConsoleInputRegister(E conTarget)
	{
		this.conTarget = conTarget;
	}

	/**
	 * Activates this input register. While active, this class will register input with the provided console every
	 * millisecond. This is not a threaded loop so nothing needs to be locked.
	 */
	public void setActive()
	{
		if (keyListenerHandle != null)
		{
			keyListenerHandle.cancel(true);
		}

		enableRepeatEvents(true);
		invocationTime = System.nanoTime();
		running = true;
		keyListenerHandle = scheduler.scheduleAtFixedRate(keyHandler, 0, 1, TimeUnit.MILLISECONDS);
	}

	/**
	 * Deactivates this input register. While inactive, this class will not register input but will wait to be set
	 * active again.
	 */
	public void setInactive()
	{
		enableRepeatEvents(false);
		running = false;

		if (keyListenerHandle != null)
		{
			keyListenerHandle.cancel(true);
			keyListenerHandle = null;
		}
	}

	/**
	 * @return if the keyboard will currently produce a capital
	 */
	public boolean isCap()
	{
		return capsLock ^ shiftDown;
	}

	/**
	 * Handles any key that has a capital output differing from is uppercase output that does not have a LWJGL key
	 * definition.
	 * @param key the key character
	 * @param caps if caps is current
	 * @return the corrected key
	 */
	public char handleKey(char key, boolean caps)
	{
		/*
		 * if caps not requested
		 */
		if (!caps)
		{
			/*
			 * if alpha lower case
			 */
			if (key >= 97 && key <= 122)
			{
				return key;
			}
			/*
			 * if alpha upper case
			 */
			else if (key >= 65 && key <= 90)
			{
				return (char) ((int) key + 32);
			}

		}
		/*
		 * if caps requested
		 */
		else
		{
			/*
			 * if alpha lower case
			 */
			if (key >= 97 && key <= 122)
			{
				return (char) ((int) key - 32);
			}
			/*
			 * if alpha upper case
			 */
			else if (key >= 65 && key <= 90)
			{
				return key;
			}
			/*
			 * numbers
			 */
			else if (key == '9')
			{
				return '(';
			}
			else if (key == '8')
			{
				return '*';
			}
			else if (key == '7')
			{
				return '&';
			}
			else if (key == '6')
			{
				return '^';
			}
			else if (key == '5')
			{
				return '%';
			}
			else if (key == '4')
			{
				return '$';
			}
			else if (key == '3')
			{
				return '#';
			}
			else if (key == '2')
			{
				return '@';
			}
			else if (key == '1')
			{
				return '!';
			}
			else if (key == '0')
			{
				return ')';
			}
			/*
			 * symbols
			 */
			else if (key == '-')
			{
				return '_';
			}
			else if (key == '=')
			{
				return '+';
			}
			else if (key == '[')
			{
				return '{';
			}
			else if (key == ']')
			{
				return '}';
			}
			else if (key == '\\')
			{
				return '|';
			}
			else if (key == '\'')
			{
				return '"';
			}
			else if (key == ',')
			{
				return '<';
			}
			else if (key == '.')
			{
				return '>';
			}
			else if (key == '/')
			{
				return '?';
			}
		}

		return key;
	}
}
