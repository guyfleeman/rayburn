package com.rayburn.engine.util;

/**
 * @author willstuckey
 * @date 10/6/14 <p></p>
 */
public class ToggleKey
{
	private boolean lastState = false;
	private boolean isKeyActive = false;

	public ToggleKey() {}

	public void updateKeyStatus(boolean state)
	{
		if (state && !this.lastState)
		{
			this.isKeyActive = !this.isKeyActive;
		}

		this.lastState = state;
	}

	public boolean isKeyActive()
	{
		return isKeyActive;
	}
}
