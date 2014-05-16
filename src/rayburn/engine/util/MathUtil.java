package rayburn.engine.util;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

/**
 * @author Will Stuckey
 * @date 12/19/13
 * @version 1.1
 * <p>Contains Rayburn utility constants, functions, and classes</p>
 */
public class MathUtil
{
	/**
	 * The method the class will use to evaluate mathematics. Using GL, column major ordering, or standard row major ordering.
	 */
	private static MathMode mathMode;

	/**
	 * The identity matrix
	 * @since 0.1
	 */
	public static final Matrix4f identityMatrix;

	/**
	 * xAxis
	 * @since 0.1
	 */
	public static final float[] xAxis = {1.0f, 0.0f, 0.0f};

	/**
	 * yAxis
	 * @since 0.1
	 */
	public static final float[] yAxis = {0.0f, 1.0f, 0.0f};

	/**
	 * zAxis
	 * @since 0.1
	 */
	public static final float[] zAxis = {0.0f, 0.0f, 1.0f};

	/**
	 * xAxis
	 * @since 0.3
	 */
	public static final Vector3f xVector = new Vector3f(1.0f, 0.0f, 0.0f);

	/**
	 * yAxis
	 * @since 0.3
	 */
	public static final Vector3f yVector = new Vector3f(0.0f, 1.0f, 0.0f);

	/**
	 * zAxis
	 * @since 0.3
	 */
	public static final Vector3f zVector = new Vector3f(0.0f, 0.0f, 1.0f);

	/**
	 * default scale vector
	 * @since 0.4
	 */
	public static final Vector3f defaultScale = new Vector3f(1.0f, 1.0f, 1.0f);

	/**
	 * default translation vector
	 * @since 0.4
	 */
	public static final Vector3f defaultTranslation = new Vector3f(0.0f, 0.0f, 0.0f);

	/**
	 * default rotation vector
	 * @since 0.4
	 */
	public static final Vector3f defaultRotation = new Vector3f(0.0f, 0.0f, 0.0f);

	/**
	 * default location vector
	 * @since 0.4
	 */
	public static final Vector3f defaultLocation = new Vector3f(0.0f, 0.0f, 0.0f);

	/**
	 * default forward vector
	 * @since 0.4
	 */
	public static final Vector3f defaultForward = new Vector3f(0.0f, 0.0f, 1.0f);

	/**
	 * default up vector
	 * @since 0.4
	 */
	public static final Vector3f defaultUp = new Vector3f(0.0f, 1.0f, 0.0f);

	/**
	 * default left vector
	 * @since 0.4
	 */
	public static final Vector3f defaultLeft = new Vector3f(-1.0f, 0.0f, 0.0f);

	/**
	 * default right vector
	 * @since 0.4
	 */
	public static final Vector3f defaultRight = new Vector3f(1.0f, 0.0f, 0.0f);


	/**
	 * The valid mathematical modes the class can operate on.
	 * @since 0.4
	 */
	public static enum MathMode
	{
		/**
		 * Standard mathematics. Synonymous with row major ordering.
		 */
		STD,

		/**
		 * OpenGL mathematics. Synonymous with column major ordering.
		 */
		GL,

		/**
		 * Column major matrix ordering. Synonymous with OpenGL mathematics.
		 */
		COLUMN_MAJOR_ORDERING,

		/**
		 * Row major matrix ordering. Synonymous with standard mathematics.
		 */
		ROW_MAJOR_ORDERING;
	}

	static
	{
		mathMode = MathMode.ROW_MAJOR_ORDERING;

		Matrix4f identity = new Matrix4f();

		identity.m00 = 1f;
		identity.m10 = 0f;
		identity.m20 = 0f;
		identity.m30 = 0f;

		identity.m10 = 0f;
		identity.m11 = 1f;
		identity.m12 = 0f;
		identity.m13 = 0f;

		identity.m20 = 0f;
		identity.m21 = 0f;
		identity.m22 = 1f;
		identity.m23 = 0f;

		identity.m30 = 0f;
		identity.m31 = 0f;
		identity.m32 = 0f;
		identity.m33 = 1f;

		identityMatrix = identity;
	}

	/**
	 * Prevent the class from being initialized
	 */
	private MathUtil() {}

	/**
	 * @return the current matrix math mode for the class
	 */
	public static MathMode getMathMode()
	{
		return mathMode;
	}

	/**
	 * Set the current matrix math mode for the class
	 * @param mm the math mode the class will use for matrix operations
	 */
	public static void setMathMode(MathMode mm)
	{
		if (mm == MathMode.COLUMN_MAJOR_ORDERING || mm == MathMode.GL)
			mathMode = MathMode.COLUMN_MAJOR_ORDERING;
		else
			mathMode = MathMode.ROW_MAJOR_ORDERING;
	}

	///////////////////////////////////
	//                               //
	//    BEGIN VECTOR OPERATIONS    //
	//                               //
	///////////////////////////////////

	/**
	 * Determines if a vector is zeroed
	 * @param vector a vector
	 * @return if vector is zeroed
	 */
	public static boolean isZero(Vector3f vector)
	{
		return ((vector.getZ() == 0f) && (vector.getY() == 0f) && (vector.getZ() == 0f));
	}

	/**
	 * Rotates an axis (vector), by an angle
	 * @param angle the angle of rotation, in degrees
	 * @param axis the axis (vector) being rotated
	 * @return a new vector, the product of the original axis and the rotation
	 */
	public static Vector3f getViewAxis(Vector3f axis, Vector3f view, float angle)
	{
        Quaternion qTemp = new Quaternion();
        Quaternion qView = new Quaternion();
        Quaternion qResult = new Quaternion();

        float sinHalf = (float)Math.sin(Math.toRadians(angle));
        float cosHalf = (float)Math.cos(Math.toRadians(angle));

        qTemp.setX(axis.getX() * sinHalf);
        qTemp.setY(axis.getY() * sinHalf);
        qTemp.setZ(axis.getZ() * sinHalf);
        qTemp.setW(cosHalf);

        qView.setX(view.getX());
        qView.setY(view.getY());
        qView.setZ(view.getZ());
        qView.setW(0f);

        qResult = multiplyQuaternion(multiplyQuaternion(qTemp, qView), negateQuaternion(qTemp));

        return new Vector3f(qResult.getX(), qResult.getY(), qResult.getZ());

        /*
		float sinHalf = (float)Math.sin(Math.toRadians(angle));
		float cosHalf = (float)Math.cos(Math.toRadians(angle));

		float rX = (axis.getX() * sinHalf);
		float rY = (axis.getY() * sinHalf);
		float rZ = (axis.getZ() * sinHalf);
		float rW = cosHalf;

		Quaternion rotation = new Quaternion(rX, rY, rZ, rW);

		//TODO TESTING ON THIS, esp NaN testing
		//Quaternion conjugate = new Quaternion();
		//Quaternion.negate(rotation, conjugate);
		//Quaternion w = new Quaternion();
		//Quaternion.mul(rotateQuaternionByVector(rotation, axis), conjugate, w);

		Quaternion w = rotateQuaternionByVector(rotation, axis);
        */

		//return new Vector3f(w.getX(), w.getY(), w.getZ());
	}

	/**
	 * Gets the cross product of two vectors
	 * @param left the left vector
	 * @param right the right vector
	 * @return the cross product
	 */
	public static Vector3f getCrossProduct(Vector3f left, Vector3f right)
	{
		Vector3f cp = new Vector3f();
		Vector3f.cross(left, right, cp);
		return cp;
	}

	/**
	 * Multiplies the components of two vectors
	 * @param left the left vector
	 * @param right the right vector
	 * @return the product
	 */
	public static Vector3f multiplyVectors(Vector3f left, Vector3f right)
	{
		return new Vector3f(left.getX() * right.getX(), left.getY() * right.getY(), left.getZ() * right.getZ());
	}

	/**
	 * Multiplies the components of two vectors
	 * @param left the left vector
	 * @param right the left vector
	 * @param dest the destination vector
	 */
	public static void multpilyVectors(Vector3f left, Vector3f right, Vector3f dest)
	{
		dest = multiplyVectors(left, right);
	}

    /**
     * Subtracts the components of two vectors
     * @param left the left vector
     * @param right the right vector
     * @return the difference
     */
    public static Vector3f subtractVectors(Vector3f left, Vector3f right)
    {
        return new Vector3f(left.getX() - right.getX(), left.getY() - right.getY(), left.getZ() - right.getZ());
    }

    /**
     * Subtracts the components of two vectors
     * @param left left the left vector
     * @param right right the right vector
     * @param dest the destination vector
     */
    public static void subtractVectors(Vector3f left, Vector3f right, Vector3f dest)
    {
        dest = subtractVectors(left, right);
    }

	/**
	 * Multiplies a vector by a constant.
	 * @param vector
	 * @param amount
	 * @return Multiplied vector
	 */
	public static Vector3f mulConst(Vector3f vector, float amount)
	{
		return new Vector3f(vector.getX() * amount, vector.getY() * amount, vector.getZ() * amount);
	}

	/**
	 * Divides a vector by a constant
	 * @param vector
	 * @param amount
	 * @return Divided vector
	 */
	public static Vector3f divConst(Vector3f vector, float amount)
	{
		if (amount != 0)
			return new Vector3f(vector.getX() / amount, vector.getY() / amount, vector.getZ() / amount);
		else return null;
	}

	/**
	 * Adds a constant to a vector
	 * @param vector
	 * @param amount
	 * @return Added vector
	 */
	public static Vector3f addConst(Vector3f vector, float amount)
	{
		return new Vector3f(vector.getX() + amount, vector.getY() + amount, vector.getZ() + amount);
	}

	/**
	 * Subtracts a constant from a vector
	 * @param vector
	 * @param amount
	 * @return Subtracted vector
	 */
	public static Vector3f subConst(Vector3f vector, float amount)
	{
		return new Vector3f(vector.getX() - amount, vector.getY() - amount, vector.getZ() - amount);
	}

	///////////////////////////////////////
	//                                   //
	//    BEGIN QUATERNION OPERATIONS    //
	//                                   //
	///////////////////////////////////////

	/**
	 * multiplies a quaternion by a vector representing a rotation
	 * @param quat the quaternion to be multiplied, the operand
	 * @param rotation the vector to multiply the quaternion by, the operator
	 * @return a new quaternion that is the product of quaternion, quat, and Vector, rotation
	 */
	public static Quaternion rotateQuaternionByVector(Quaternion quat, Vector3f rotation)
	{
		float x, y, z, w;

		x = (quat.getW() * rotation.getX()
				+ (quat.getY() * rotation.getZ())
				- (quat.getZ() * rotation.getY()));
		y = (quat.getW() * rotation.getY()
				+ (quat.getZ() * rotation.getX())
				- (quat.getX() * rotation.getZ()));
		z = (quat.getW() * rotation.getZ()
				+ (quat.getX() * rotation.getY())
				- (quat.getY() * rotation.getX()));
		w = (((-1.0f * quat.getX()) * rotation.getX())
				- (quat.getY() * rotation.getY())
				- (quat.getZ() * rotation.getZ()));

		return new Quaternion(x, y, z, w);
	}

    public static Quaternion multiplyQuaternion(Quaternion left, Quaternion right)
    {
        Quaternion result = new Quaternion();
        Quaternion.mul(left, right, result);
        return result;
    }

    public static Quaternion negateQuaternion(Quaternion left)
    {
        Quaternion result = new Quaternion();
        Quaternion.negate(left, result);
        return result;
    }

    public static Quaternion multiplyInverseQuaternion(Quaternion left, Quaternion right)
    {
        Quaternion result = new Quaternion();
        Quaternion.mulInverse(left, right, result);
        return result;
    }

	/**
	 * Gets the length of quaternion, quat
	 * @param quat
	 * @return the length
	 */
	public static float getLength(Quaternion quat)
	{
		return (float)Math.sqrt((quat.getX() * quat.getX())
				+ (quat.getY() * quat.getY())
				+ (quat.getZ() * quat.getZ())
				+ (quat.getW() * quat.getW()));
	}

	///////////////////////////////////
	//                               //
	//    BEGIN MATRIX OPERATIONS    //
	//                               //
	///////////////////////////////////

	/**
	 * @return an identity matrix
	 */
	public static Matrix4f getIdentityMatrix()
	{
		return identityMatrix;
	}

	/**
	 * Creates a matrix that represents a translation in the modelview stack
	 * @param x the x translation
	 * @param y the y translation
	 * @param z the z translation
	 * @return the matrix representing the translation
	 */
    public static Matrix4f getTranslationMatrix(float x, float y, float z)
    {
        Matrix4f translation = identityMatrix;

	    if(mathMode == MathMode.COLUMN_MAJOR_ORDERING)
	    {
            translation.m30 = x;
            translation.m31 = y;
            translation.m32 = z;
	    }
	    else
	    {
		    translation.m03 = x;
		    translation.m13 = y;
		    translation.m23 = z;
	    }

        return translation;
    }

	/**
	 * Creates a matrix the represents a translation in the modelview stack
	 * @param translation the translation vector
	 * @return the matrix representing the translation
	 */
    public static Matrix4f getTranslationMatrix(Vector3f translation)
    {
        return getTranslationMatrix(translation.getX(), translation.getY(), translation.getZ());
    }

	/**
	 * Translates a matrix based on the modelview translation matrix, and returns the new one
	 * @param matrix the matrix to be translated
	 * @param x the x translation
	 * @param y the y translation
	 * @param z the z translation
	 * @return the translated matrix
	 */
    public static Matrix4f getTranslatedMatrix(Matrix4f matrix, float x, float y, float z)
    {
	    Matrix4f translatedMatrix = getIdentityMatrix();
        Matrix4f.mul(matrix, getTranslationMatrix(x, y, z), translatedMatrix);
        return translatedMatrix;
    }

	/**
	 * Translates a matrix based on the modelview translation matrix, and returns the new one
	 * @param matrix the matrix to be translated
	 * @param translation the translation vector
	 * @return the translated matrix
	 */
    public static Matrix4f getTranslatedMatrix(Matrix4f matrix, Vector3f translation)
    {
		return getTranslatedMatrix(matrix, translation.getX(), translation.getY(), translation.getZ());
    }

	/**
	 * Translates a matrix based on the modelview translation matrix
	 * @param matrix the matrix to be translated
	 * @param x the x translation
	 * @param y the y translation
	 * @param z the z translation
	 */
	public static void translateMatrix(Matrix4f matrix, float x, float y, float z)
	{
		Matrix4f.mul(matrix, getTranslationMatrix(x, y, z), matrix);
	}

	/**
	 * Translates a matrix based on the modelview translation matrix
	 * @param matrix the matrix to be translated
	 * @param translation the translation vector
	 */
	public static void translateMatrix(Matrix4f matrix, Vector3f translation)
	{
		translateMatrix(matrix, translation.getX(), translation.getY(), translation.getZ());
	}

	/**
	 * Creates a matrix that represents rotation in the modelview stack
	 * @param x the x rotation, in degrees
	 * @param y the y rotation, in degrees
	 * @param z the z rotation, in degrees
	 * @return the matrix representing the rotation
	 */
	public static Matrix4f getRotationMatrix(float x, float y, float z)
	{
		Matrix4f finalRotation = getIdentityMatrix();
		Matrix4f xRotation = getIdentityMatrix();
		Matrix4f yRotation = getIdentityMatrix();
		Matrix4f zRotation = getIdentityMatrix();


		x = (float)Math.toRadians(x);
		y = (float)Math.toRadians(y);
		z = (float)Math.toRadians(z);

		if (mathMode == MathMode.COLUMN_MAJOR_ORDERING)
		{
			//X rotation
			xRotation.m11 = (float)Math.cos(x);
			xRotation.m12 = -1f * (float)Math.sin(x);
			xRotation.m21 = (float)Math.sin(x);
			xRotation.m22 = (float)Math.cos(x);

			//y rotation
			yRotation.m00 = (float)Math.cos(y);
			yRotation.m02 = (float)Math.sin(y);
			yRotation.m20 = -1f * (float)Math.sin(y);
			yRotation.m22 = (float)Math.cos(y);

			//z rotation
			zRotation.m00 = (float)Math.cos(z);
			zRotation.m01 = -1f * (float)Math.sin(z);
			zRotation.m10 = (float)Math.sin(z);
			zRotation.m11 = (float)Math.cos(z);
		}
		else
		{
			//X rotation
			xRotation.m11 = (float)Math.cos(x);
			xRotation.m21 = -1f * (float)Math.sin(x);
			xRotation.m12 = (float)Math.sin(x);
			xRotation.m22 = (float)Math.cos(x);

			//y rotation
			yRotation.m00 = (float)Math.cos(y);
			yRotation.m20 = (float)Math.sin(y);
			yRotation.m02 = -1f * (float)Math.sin(y);
			yRotation.m22 = (float)Math.cos(y);

			//z rotation
			zRotation.m00 = (float)Math.cos(z);
			zRotation.m10 = -1f * (float)Math.sin(z);
			zRotation.m01 = (float)Math.sin(z);
			zRotation.m11 = (float)Math.cos(z);
		}

		Matrix4f.mul(yRotation, xRotation, yRotation);
		Matrix4f.mul(zRotation, yRotation, zRotation);
		Matrix4f.mul(finalRotation, zRotation, finalRotation);

		return finalRotation;
	}

	/**
	 * Creates a matrix that represents rotation in the modelview stack
	 * @param rotation the rotation vector, all values in degrees
	 * @return the matrix representing the rotation
	 */
	public static Matrix4f getRotationMatrix(Vector3f rotation)
	{
		return getRotationMatrix(rotation.getX(), rotation.getY(), rotation.getZ());
	}

	/**
	 * Rotates a matrix based on the modelview rotation matrix, and returns the new one
	 * @param matrix the matrix to be rotated
	 * @param x the x rotation, in degrees
	 * @param y the y rotation, in degrees
	 * @param z the z rotation, in degrees
	 * @return the rotated matrix
	 */
	public static Matrix4f getRotatedMatrix(Matrix4f matrix, float x, float y, float z)
	{
		Matrix4f rotatedMatrix = getIdentityMatrix();
		Matrix4f.mul(matrix, getRotationMatrix(x, y, z), rotatedMatrix);
		return rotatedMatrix;
	}

	/**
	 * Rotates a matrix based on the modelview rotation matrix, and returns the new one
	 * @param matrix the matrix to be rotated
	 * @param rotation the rotation vector, all values in degrees
	 * @return the rotated matrix
	 */
	public static Matrix4f getRotatedMatrix(Matrix4f matrix, Vector3f rotation)
	{
		return getRotatedMatrix(matrix, rotation.getX(), rotation.getY(), rotation.getZ());
	}

	/**
	 * Rotates a matrix based on the modelview rotation matrix
	 * @param matrix the matrix to be rotated
	 * @param x the x rotation, in degrees
	 * @param y the y rotation, in degrees
	 * @param z the z rotation, in degrees
	 */
	public static void rotateMatrix(Matrix4f matrix, float x, float y, float z)
	{
		Matrix4f.mul(matrix, getRotationMatrix(x, y, z), matrix);
	}

	/**
	 * Rotates a matrix based on the modelview rotation matrix
	 * @param matrix the matrix to be rotated
	 * @param rotation the rotation vector, all values in degrees
	 */
	public static void rotateMatrix(Matrix4f matrix, Vector3f rotation)
	{
		rotateMatrix(matrix, rotation.getX(), rotation.getY(), rotation.getZ());
	}

	/**
	 * Creates a matrix that represents a scale operation in the modelview stack
	 * @param x the x scale
	 * @param y the y scale
	 * @param z the z scale
	 * @return the matrix representing the scale operation
	 */
	public static Matrix4f getScaleMatrix(float x, float y, float z)
	{
		Matrix4f scaleMatrix = getIdentityMatrix();

		if (mathMode == MathMode.COLUMN_MAJOR_ORDERING)
		{
			scaleMatrix.m03 = x;
			scaleMatrix.m13 = y;
			scaleMatrix.m23 = z;
		}
		else
		{
			scaleMatrix.m30 = x;
			scaleMatrix.m31 = y;
			scaleMatrix.m32 = z;
		}

		return scaleMatrix;
	}

	/**
	 * Creates a matrix that represents a scale operation in the modelview stack
	 * @param scale the scale vector
	 * @return the matrix representing the scale operation
	 */
	public static Matrix4f getScaleMatrix(Vector3f scale)
	{
		return getScaleMatrix(scale.getX(), scale.getY(), scale.getZ());
	}

	/**
	 * Scales a matrix based on the modelview scale matrix, and returns the new one
	 * @param matrix the matrix to be scaled
	 * @param x the x scale
	 * @param y the y scale
	 * @param z the z scale
	 * @return the scaled matrix
	 */
	public static Matrix4f getScaledMatrix(Matrix4f matrix, float x, float y, float z)
	{
		Matrix4f scaledMatrix = getIdentityMatrix();
		Matrix4f.mul(matrix, getScaleMatrix(x, y, z), scaledMatrix);
		return scaledMatrix;
	}

	/**
	 * Scales a matrix based on the modelview scale matrix, and returns the new one
	 * @param matrix the matrix to be scaled
	 * @param scale the scale vector
	 * @return the scaled matrix
	 */
	public static Matrix4f getScaledMatrix(Matrix4f matrix, Vector3f scale)
	{
		return getScaledMatrix(matrix, scale.getX(), scale.getY(), scale.getZ());
	}

	/**
	 * Scales a matrix based on the modelview scale matrix
	 * @param matrix the matrix to be scaled
	 * @param x the x scale
	 * @param y the y scale
	 * @param z the z scale
	 */
	public static void scaleMatrix(Matrix4f matrix, float x, float y, float z)
	{
		Matrix4f.mul(matrix, getScaleMatrix(x, y, z), matrix);
	}

	/**
	 * Scales a matrix based on the modelview scale matrix
	 * @param matrix the matrix to be scaled
	 * @param scale the scale vector
	 */
	public static void scaleMatrix(Matrix4f matrix, Vector3f scale)
	{
		scaleMatrix(matrix, scale.getX(), scale.getY(), scale.getZ());
	}

	/**
	 * Creates a projection matrix
	 * @DEPRICATED this functionality is taken care of by glu, a part of lwjgl
	 * @param width width of the view, in pixels
	 * @param height height of the view, in pixels
	 * @param fieldOfView angle between the camera's relative z axis and the outermost edge of view
	 * @param zNear distance between the camera (camera origin) and the near view plane
	 * @param zFar distance between the camera (camera origin) and the far view plane
	 * @return the projection matrix
	 */
	public static Matrix4f createProjectionMatrix(float width, float height, float fieldOfView, float zNear, float zFar)
	{
		Matrix4f projectionMatrix = getIdentityMatrix();

		float aspectRatio = width / height;
		float fovScale = (float)Math.tan(Math.toRadians(fieldOfView / 2.0f));
		float frustum = zNear - zFar;

		if (mathMode == MathMode.COLUMN_MAJOR_ORDERING)
		{
			projectionMatrix.m00 = (1.0f / (fovScale * aspectRatio));
			projectionMatrix.m11 = (1.0f / fovScale);
			projectionMatrix.m22 = (-1.0f * ((zFar + zNear) / frustum));
			projectionMatrix.m32 = (2.0f * zFar * zNear / frustum);
		}
		else
		{
			projectionMatrix.m00 = (1.0f / (fovScale * aspectRatio));
			projectionMatrix.m11 = (1.0f / fovScale);
			projectionMatrix.m22 = (-1.0f * ((zFar + zNear) / frustum));
			projectionMatrix.m23 = (2.0f * zFar * zNear / frustum);
		}

		return projectionMatrix;
	}

	/**
	 * Generates an inital camera matrix
	 * @param forward the vector describing forward in the 3D space
	 * @param up the vector describing up in the 3D space
	 * @return the camera matrix
	 */
	public static Matrix4f createCameraMatrix(Vector3f forward, Vector3f up)
	{
		if (!isZero(forward))
			forward.normalise();

		if (!isZero(up))
			up.normalise();

		Vector3f right = new Vector3f();
		Matrix4f cameraMatrix = getIdentityMatrix();

		Vector3f.cross(forward, up, up);
		Vector3f.cross(forward, up, right);

		if (mathMode == MathMode.COLUMN_MAJOR_ORDERING)
		{
			//set relative x axes
			cameraMatrix.m00 = right.getX();
			cameraMatrix.m01 = up.getX();
			cameraMatrix.m02 = forward.getX();

			//set relative y axes
			cameraMatrix.m10 = right.getY();
			cameraMatrix.m11 = up.getY();
			cameraMatrix.m12 = forward.getY();

			//set relative z axes
			cameraMatrix.m20 = right.getZ();
			cameraMatrix.m21 = up.getZ();
			cameraMatrix.m22 = forward.getZ();
		}
		else
		{
			//set relative right vector
			cameraMatrix.m00 = right.getX();
			cameraMatrix.m01 = right.getY();
			cameraMatrix.m02 = right.getZ();

			//set relative up vector
			cameraMatrix.m10 = up.getX();
			cameraMatrix.m11 = up.getY();
			cameraMatrix.m12 = up.getZ();

			//set relative forward vector
			cameraMatrix.m20 = forward.getX();
			cameraMatrix.m21 = forward.getY();
			cameraMatrix.m22 = forward.getZ();
		}

		return cameraMatrix;
	}
}