package test.gimballock;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import com.normalizedinsanity.rayburn.engine.util.MathUtil;

import java.nio.FloatBuffer;

/**
 * @author Will Stuckey
 * @date 2/20/14
 * <p></p>
 */
public class Camera extends LocationEntity
{
	public Vector3f direction = new Vector3f();

	private Quaternion pitch = new Quaternion();
	private Quaternion yaw = new Quaternion();
	//private Quaternion roll = new Quaternion();
	private Quaternion rotation = new Quaternion();

	private FloatBuffer cameraLocationBuffer = BufferUtils.createFloatBuffer(16);

	public Camera()
	{
		super();
	}

	public Camera(Vector3f initialTranslation, Vector3f initialRotation, Vector3f initialScale)
	{
		super(initialTranslation, initialRotation, initialScale);
	}

	public void move(float distance)
	{
		updateTranslation(new Vector3f(
				-direction.getX() * distance,
				-direction.getY() * distance,
				direction.getZ() * distance
		));
	}

	public void strafe(float distance)
	{
		Vector3f up = new Vector3f(0f, 1f, 0f);
		Vector3f left = Vector3f.cross(direction, up, null);

		updateTranslation(new Vector3f(
				left.getX() * distance,
				left.getY() * distance,
				-left.getZ() * distance
		));
	}

	public void align()
	{
		cameraLocationBuffer.clear();

		yaw.setFromAxisAngle(
				new Vector4f(
						MathUtil.yVector.getX(),
						MathUtil.yVector.getY(),
						MathUtil.yVector.getZ(),
						getRotation().getY() * ((float) Math.PI / 180f)
				)
		);

		pitch.setFromAxisAngle(
				new Vector4f(
						MathUtil.xVector.getX(),
						MathUtil.xVector.getY(),
						MathUtil.xVector.getZ(),
						getRotation().getZ() * ((float) Math.PI / 180f)
				)
		);

		/*
		roll.setFromAxisAngle(
				new Vector4f(
						MathUtil.defaultForward.getX(),
						MathUtil.defaultForward.getY(),
						MathUtil.defaultForward.getZ(),
						getRotation().getX() * ((float) Math.PI / 180f)
				)
		);
		*/

		//load matrix
	  	Quaternion.mul(pitch, yaw, rotation);
		Matrix4f rotationMatrix = convertQuaternionToMatrix4f(rotation);
		rotationMatrix.store(cameraLocationBuffer);
		cameraLocationBuffer.rewind();

		//readjust direction
		Matrix4f pitchMatrix = convertQuaternionToMatrix4f(pitch);
		Quaternion temp = Quaternion.mul(yaw, pitch, null);
		rotationMatrix = convertQuaternionToMatrix4f(temp);
		direction.setX(rotationMatrix.m20);
		direction.setY(pitchMatrix.m21);
		direction.setZ(rotationMatrix.m22);
	}

	public FloatBuffer getCameraLocationBuffer()
	{
		return cameraLocationBuffer;
	}

	private static Matrix4f convertQuaternionToMatrix4f(Quaternion q)
	{
		Matrix4f matrix = MathUtil.getIdentityMatrix();
		matrix.m00 = 1.0f - 2.0f * (q.getY() * q.getY() + q.getZ() * q.getZ());
		matrix.m01 =        2.0f * (q.getX() * q.getY() + q.getZ() * q.getW());
		matrix.m02 =        2.0f * (q.getX() * q.getZ() - q.getY() * q.getW());

		matrix.m10 =        2.0f * (q.getX() * q.getY() - q.getZ() * q.getW());
		matrix.m11 = 1.0f - 2.0f * (q.getX() * q.getX() + q.getZ() * q.getZ());
		matrix.m12 =        2.0f * (q.getZ() * q.getY() + q.getX() * q.getW());

		matrix.m20 =        2.0f * (q.getX() * q.getZ() + q.getY() * q.getW());
		matrix.m21 =        2.0f * (q.getY() * q.getZ() - q.getX() * q.getW());
		matrix.m22 = 1.0f - 2.0f * (q.getX() * q.getX() + q.getY() * q.getY());

		return matrix;
	}
}
