package rayburn.game.view;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
//import rayburn.engine.util.oldcrap._MathMatrixUtil;

import static rayburn.engine.util.MathUtil.*;

/**
 * @author Will Stuckey
 * @date 12/9/13
 * <p></p>
 */

public class View
{
	/*
	private _CameraOld cameraOld;
	private Projection projection;

	private Vector3f translation = new Vector3f();
	private Vector3f rotation = new Vector3f();
	private Vector3f scale = new Vector3f();

	public View()
	{
		//TODO implement default cameraOld and projection classes
	}

	public View(_CameraOld cameraOld, Projection projection)
	{
		this.cameraOld = cameraOld;
		this.projection = projection;
	}

	public Matrix4f getTransformation()
	{
		Matrix4f translationMatrix = new Matrix4f();
		Matrix4f.mul(translationMatrix, _MathMatrixUtil.getTranslationMatrix(translation), translationMatrix);
		Matrix4f rotationMatrix = new Matrix4f();
		Matrix4f.mul(translationMatrix, _MathMatrixUtil.getRotationMatrix(rotation), rotationMatrix);
		Matrix4f scaleMatrix = new Matrix4f();
		Matrix4f.mul(scaleMatrix, _MathMatrixUtil.getScaleMatrix(scale), scaleMatrix);

		Matrix4f.mul(rotationMatrix, scaleMatrix, rotationMatrix);
		Matrix4f.mul(translationMatrix, rotationMatrix, translationMatrix);

		return translationMatrix;
	}

	public Matrix4f getProjectedTransformation()
	{
		Matrix4f cameraRotation, cameraTranslation;
		Matrix4f transformationMatrix = getTransformation();
		Matrix4f projectionMatrix = projection.getProjectionMatrix();
		cameraRotation = createCameraMatrix(cameraOld.getForward(), cameraOld.getUp());
		cameraTranslation = _MathMatrixUtil.getTranslationMatrix(-cameraOld.getPosition().getX(),
				-cameraOld.getPosition().getY(),
				-cameraOld.getPosition().getZ());

		Matrix4f.mul(cameraTranslation, transformationMatrix, cameraTranslation);
		Matrix4f.mul(cameraRotation, cameraTranslation, cameraRotation);
		Matrix4f.mul(projectionMatrix, cameraRotation, projectionMatrix);

		return projectionMatrix;
	}

	public _CameraOld getCameraOld()
	{
		return cameraOld;
	}

	public synchronized void setCameraOld(_CameraOld cameraOld)
	{
		this.cameraOld = cameraOld;
	}

	public Projection getProjection()
	{
		return projection;
	}

	public synchronized void setProjection(Projection projection)
	{
		this.projection = projection;
	}
	*/
}