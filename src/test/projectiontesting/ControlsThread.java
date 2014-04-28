package test.projectiontesting;

import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import rayburn.engine.util.MathUtil;

import static org.lwjgl.input.Keyboard.*;

/**
 * @author Will Stuckey
 * @date 12/9/13
 * <p></p>
 */
public class ControlsThread extends Thread
{
	private boolean running = true;

    protected boolean windowCloseRequested;

	protected Vector2f mouseDelta = new Vector2f(0.0f, 0.0f);
	protected Vector2f mouseSensitivity = new Vector2f(0.01f, 0.01f);
    protected Vector3f translationSensitivity = new Vector3f(0.1f, 0.1f, 0.1f);

    protected Vector3f translationVector = new Vector3f(0.0f, 0.0f, 0.0f);
    protected Vector3f rotationVector = new Vector3f(0.0f, 0.0f, 0.0f);
    protected Vector3f scaleVector = new Vector3f(1.0f, 1.0f, 1.0f);

    protected Vector3f up = MathUtil.defaultUp;
    protected Vector3f view = MathUtil.defaultForward;
    protected Vector3f rotationAxis = new Vector3f();
    protected Quaternion rotationQuaternion = new Quaternion();

	public ControlsThread()
	{
		resetContext();
	}

	public void run()
	{
		while (running)
		{
			if (isKeyDown(KEY_ESCAPE))
				windowCloseRequested = true;

            rotationAxis = (MathUtil.getCrossProduct(MathUtil.subtractVectors(view, translationVector), up));
            rotationAxis.normalise();

            mouseDelta.setX(Mouse.getDX());
            mouseDelta.setY(-Mouse.getDY());

			if (isKeyDown(KEY_SPACE))
				resetContext();

			if (isKeyDown(KEY_W))
				translationVector.setZ(translationVector.getZ() + translationSensitivity.getZ());

			if (isKeyDown(KEY_S))
				translationVector.setZ(translationVector.getZ() - translationSensitivity.getZ());

            if (isKeyDown(KEY_A))
                translationVector.setX(translationVector.getX() + translationSensitivity.getX());

            if (isKeyDown(KEY_D))
                translationVector.setX(translationVector.getX() - translationSensitivity.getX());

			rotationVector.setY(rotationVector.getY() - (mouseDelta.getY() * mouseSensitivity.getY()));
			rotationVector.setX(rotationVector.getX() - (mouseDelta.getX() * mouseSensitivity.getX()));

			try
			{
				Thread.sleep(5);
			}
			catch (InterruptedException e) {}

		}
	}

	public void terminate()
	{
		running = false;
	}

	public boolean isWindowCloseRequested()
	{
		return windowCloseRequested;
	}

	public synchronized void resetContext()
	{
		translationVector = new Vector3f(0.0f, 0.0f, -1.0f);
		rotationVector = new Vector3f(0.0f, 0.0f,0.0f);
		scaleVector = new Vector3f(1.0f, 1.0f, 1.0f);
	}

	public  Vector3f getTranslationVector()
	{
		return translationVector;
	}

    public Vector3f getRotationVector()
    {
        return rotationVector;
    }

    public Vector3f getUpAxis() { return up; }

    public Vector3f getViewAxis() { return view; }

    public Vector2f getMouseDelta() { return mouseDelta; }

    public Vector3f getRotationAxis()
    {
        return rotationAxis;
    }
}
