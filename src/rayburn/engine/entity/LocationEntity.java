package rayburn.engine.entity;

import org.lwjgl.util.vector.Vector3f;

/**
* @author Will Stuckey
* @date 2/20/14
* <p></p>
*/
public abstract class LocationEntity
{
	private Vector3f translation;
	private Vector3f rotation;
	private Vector3f scale;

	public LocationEntity()
	{
		this(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 0f, 0f), new Vector3f(1f, 1f, 1f));
	}

	public LocationEntity(Vector3f translation, Vector3f rotation, Vector3f scale)
	{
		this.translation = translation;
		this.rotation = rotation;
		this.scale = scale;
	}

	public void setLocation(Vector3f translation, Vector3f rotation, Vector3f scale)
	{
		this.translation = translation;
		this.rotation = rotation;
		this.scale = scale;
	}

	public void updateLocation(Vector3f translation, Vector3f rotation, Vector3f scale)
	{
		updateTranslation(translation);
		updateRotation(rotation);
		updateScale(scale);
	}

	public Vector3f getTranslation()
	{
		return translation;
	}

	public void setTranslation(Vector3f translation)
	{
		this.translation = translation;
	}

	public void updateTranslation(Vector3f translation)
	{
		this.translation.setX(this.translation.getX() + translation.getX());
		this.translation.setY(this.translation.getY() + translation.getY());
		this.translation.setZ(this.translation.getZ() + translation.getZ());
	}

	public Vector3f getRotation()
	{
		return rotation;
	}

	public void setRotation(Vector3f rotation)
	{
		this.rotation = rotation;
	}

	public void updateRotation(Vector3f rotation)
	{
		this.rotation.setX(this.rotation.getX() + rotation.getX());
		this.rotation.setY(this.rotation.getY() + rotation.getY());
		this.rotation.setZ(this.rotation.getZ() + rotation.getZ());
	}

	public Vector3f getScale()
	{
		return scale;
	}

	public void setScale(Vector3f scale)
	{
		this.scale = scale;
	}

	public void updateScale(Vector3f scale)
	{
		this.scale.setX(this.scale.getX() * scale.getX());
		this.scale.setY(this.scale.getY() * scale.getY());
		this.scale.setZ(this.scale.getZ() * scale.getZ());
	}
}
