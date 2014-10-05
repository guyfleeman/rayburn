package com.normalizedinsanity.rayburn.engine.entity.geom;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.util.vector.Vector3f;

/**
 * @author StuckeyWilliamM
 * @date 12/18/13
 * <b></b>
 */
public class Cube
{
    private Vector3f location = new Vector3f();
    private Vector3f rotation = new Vector3f();
    private Vector3f rotationIncrement = new Vector3f();
    private Vector3f scale = new Vector3f();

    private float[] color1 = new float[3];
    private float[] color2 = new float[3];
    private float[] color3 = new float[3];
    private float[] color4 = new float[3];
    private float[] color5 = new float[3];
    private float[] color6 = new float[3];

    public Cube()
    {
        this(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.0f, 0.0f));
    }

    public Cube(Vector3f location, Vector3f scale, Vector3f rotation)
    {
        this.location = location;
        this.scale = scale;
        this.rotation = rotation;

        float[] white = {1.0f, 1.0f, 1.0f};
        color1 = white;
        color2 = white;
        color3 = white;
        color4 = white;
        color5 = white;
        color6 = white;
    }

    public Cube(Vector3f location,
                Vector3f scale,
                Vector3f rotation,
                float[] color1,
                float[] color2,
                float[] color3,
                float[] color4,
                float[] color5,
                float[] color6)
    {
        this(location, scale, rotation);
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
        this.color4 = color4;
        this.color5 = color5;
        this.color6 = color6;
    }

    public void setLocation(Vector3f location)
    {
        this.location = location;
    }

    public void setTranslation(Vector3f translation)
    {
        location = translation;
    }

    public void setScale(Vector3f scale)
    {
        this.scale = scale;
    }

    public void setRotation(Vector3f rotation)
    {
        this.rotation = rotation;
    }

    public void setIncrementRotation(Vector3f rotationIncrement)
    {
        this.rotationIncrement = rotationIncrement;
    }

	public void setColor(float[] color)
	{
		color1 = color;
		color2 = color;
		color3 = color;
		color4 = color;
		color5 = color;
		color6 = color;
	}

    public void draw()
    {
        glPushMatrix();

        glRotatef(rotation.getX(), 1.0f, 0.0f, 0.0f);
        glRotatef(rotation.getY(), 0.0f, 1.0f, 0.0f);
        glRotatef(rotation.getZ(), 0.0f, 0.0f, 1.0f);
        glTranslatef(location.getX(), location.getY(), location.getZ());
        glScalef(scale.getX(), scale.getY(), scale.getZ());

        rotation.setX(rotation.getX() + rotationIncrement.getX());
        rotation.setY(rotation.getY() + rotationIncrement.getY());
        rotation.setZ(rotation.getZ() + rotationIncrement.getZ());

        glBegin(GL_QUADS);

        glColor3f(color1[0], color1[1], color1[2]);
        glVertex3f(0.5f, 0.5f, -0.5f);          // Top Right Of The Quad (Top)
        glVertex3f(-0.5f, 0.5f, -0.5f);          // Top Left Of The Quad (Top)
        glVertex3f(-0.5f, 0.5f, 0.5f);          // Bottom Left Of The Quad (Top)
        glVertex3f(0.5f, 0.5f, 0.5f);          // Bottom Right Of The Quad (Top)

        glColor3f(color2[0], color2[1], color2[2]);
        glVertex3f(0.5f, -0.5f, 0.5f);          // Top Right Of The Quad (Bottom)
        glVertex3f(-0.5f, -0.5f, 0.5f);          // Top Left Of The Quad (Bottom)
        glVertex3f(-0.5f, -0.5f, -0.5f);          // Bottom Left Of The Quad (Bottom)
        glVertex3f(0.5f, -0.5f, -0.5f);          // Bottom Right Of The Quad (Bottom)

        glColor3f(color3[0], color3[1], color3[2]);
        glVertex3f(0.5f, 0.5f, 0.5f);          // Top Right Of The Quad (Front)
        glVertex3f(-0.5f, 0.5f, 0.5f);          // Top Left Of The Quad (Front)
        glVertex3f(-0.5f, -0.5f, 0.5f);          // Bottom Left Of The Quad (Front)
        glVertex3f(0.5f, -0.5f, 0.5f);          // Bottom Right Of The Quad (Front)

        glColor3f(color4[0], color4[1], color4[2]);
        glVertex3f(0.5f, -0.5f, -0.5f);          // Bottom Left Of The Quad (Back)
        glVertex3f(-0.5f, -0.5f, -0.5f);          // Bottom Right Of The Quad (Back)
        glVertex3f(-0.5f, 0.5f, -0.5f);          // Top Right Of The Quad (Back)
        glVertex3f(0.5f, 0.5f, -0.5f);          // Top Left Of The Quad (Back)

        glColor3f(color5[0], color5[1], color5[2]);
        glVertex3f(-0.5f, 0.5f, 0.5f);          // Top Right Of The Quad (Left)
        glVertex3f(-0.5f, 0.5f, -0.5f);          // Top Left Of The Quad (Left)
        glVertex3f(-0.5f, -0.5f, -0.5f);          // Bottom Left Of The Quad (Left)
        glVertex3f(-0.5f, -0.5f, 0.5f);          // Bottom Right Of The Quad (Left)

        glColor3f(color6[0], color6[1], color6[2]);
        glVertex3f(0.5f, 0.5f, -0.5f);          // Top Right Of The Quad (Right)
        glVertex3f(0.5f, 0.5f, 0.5f);          // Top Left Of The Quad (Right)
        glVertex3f(0.5f, -0.5f, 0.5f);          // Bottom Left Of The Quad (Right)
        glVertex3f(0.5f, -0.5f, -0.5f);          // Bottom Right Of The Quad (Right)

        glEnd();

        glPopMatrix();
    }
}
