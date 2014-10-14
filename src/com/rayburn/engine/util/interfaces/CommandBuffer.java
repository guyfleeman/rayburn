package com.rayburn.engine.util.interfaces;

/**
 * @author willstuckey
 * @date 10/9/14 <p></p>
 */
public interface CommandBuffer
{
	public char[] getInputBuffer();

	public void clearInputBuffer();

	public String[] getOutputBuffer();

	public void clearOutputBuffer();

	public String[] getCommandHistory();

	public void clearCommandHistory();
}
