package com.rayburn.engine;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.vector.Vector3f;
import com.rayburn.engine.ingestion.ResourceParser;
import com.rayburn.engine.entity.geom.OBJModel;
import test.projectiontesting.ReferenceTimer;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LIGHT0;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_NORMAL_ARRAY;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDisableClientState;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glMultMatrix;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.util.glu.GLU.gluPerspective;

/**
 * @author Kurt Shaffer
 * @author Will Stuckey
 * @date 12/16/13
 * <b></b>
 */
public class Engine
{
    private boolean fullscreen = false;
    private int height = 600;
	private int width = 800;

    public Engine(boolean fullscreen)
    {
	    this.fullscreen = fullscreen;
        main();
    }

    private void main()
    {
	    /*
	     * Create the display
	     */
	    try
	    {
		    System.out.println("IM HERE");
		    Controllers.create();

		    if (fullscreen)
		    {


			    /*
			     * Grab the mouse and remove the window border
			     */
			    System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");

			    /*
			     * Get the dimensions of the screen
			     */
			    Toolkit toolkit = Toolkit.getDefaultToolkit();
			    Dimension screen = toolkit.getScreenSize();
			    width = (int)screen.getWidth();
			    height = (int)screen.getHeight();
		    }

		    /*
		     * Initialize the screen to the proper dimensions and enable vsync
		     */
		    Display.setDisplayMode(new DisplayMode(width, height));
		    Display.setTitle("POLY RENDER");
		    Display.create(new PixelFormat(8, 8, 8));
		    Display.setInitialBackground(1, 1, 1);
		    Display.setVSyncEnabled(true);
		    Display.update();
	    }
	    catch (LWJGLException e)
	    {
		    System.out.println("FAILED.");
		    e.printStackTrace();
		    Display.destroy();
		    System.exit(-1000);
	    }

	    Mouse.setGrabbed(true);

	    float[] color = { 1f, 1f, 1f };

	    FloatBuffer initLightLoc = BufferUtils.createFloatBuffer(4);
	    initLightLoc.put(0f).put(0f).put(0f).put(1f).flip();

	    FloatBuffer lightColor = BufferUtils.createFloatBuffer(4);
	    lightColor.put(1f).put(1f).put(1f).put(1f).flip();

	    glEnable(GL_BLEND);
	    glEnable(GL_DEPTH_TEST);
	    glEnable(GL_CULL_FACE);
	    glEnable(GL_LIGHTING);
	    glEnable(GL_LIGHT0);

	    /*
	     * Create quat camera
	     */
	    Camera camera = new Camera();
	    camera.setTranslation(new Vector3f(0f, -200f, 0f));

	    ReferenceTimer rt = new ReferenceTimer();

	    OBJModel base = new OBJModel();
	    OBJModel sphere = new OBJModel();
	    try
	    {
		    base = ResourceParser.genModelFromWavefrontOBJ(
				    new File("/home/willstuckey/Dropbox/PROGRAMMING/Java/EngineDev/parisReExp.obj"));

		    sphere = ResourceParser.genModelFromWavefrontOBJ(
				    new File("/home/willstuckey/Desktop/sphere.obj"));
	    }
	    catch (FileNotFoundException e)
	    {
		    System.out.println(e.toString());
		    System.exit(-1500);
	    }

	    base.constructBuffers();
	    sphere.constructBuffers();

	    //sphere.setTranslation(new Vector3f(0f, -1000f, 0f));
	    //sphere.setScale(new Vector3f(15f, 15f, 15f));

        while (!Display.isCloseRequested())
        {
	        /*
	         * If escape is pressed, quit the render loop
	         */
            if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
                break;

	        /*
	         * Clear buffers
	         */
            glClear(GL_COLOR_BUFFER_BIT);
            glClear(GL_DEPTH_BUFFER_BIT);
	        glClear(GL_STENCIL_BUFFER_BIT);

	        Display.setTitle("VBO RENDER: fps=" + (1000 / rt.getIntervalInt()));

	        /*
	         * Load the projection matrix with a glu projection algorithm
	         */
	        glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
	        gluPerspective(60f, (float)width / (float)height, 0.1f, 3000f);

	        /*
	         * Switch back to modelview matrix for geometry rendering
	         */
	        glMatrixMode(GL_MODELVIEW);
	        glLoadIdentity();

	        /*
	         * Sync the render timer
	         */
	        rt.update();

	        /*
	         * Update the camera based on mouse movements
	         */
	        float mouseDX = (float) Mouse.getDX() / 5f;
	        float mouseDY = (float) -Mouse.getDY() / 5f;
	        camera.updateRotation(new Vector3f(0f, mouseDX, mouseDY));
	        camera.align();

	        /*
	         * Camera forward and side movement
	         */
	        if (Keyboard.isKeyDown(Keyboard.KEY_W))
		        camera.move(1.1f);

	        if (Keyboard.isKeyDown(Keyboard.KEY_S))
		        camera.move(-1.1f);

	        if (Keyboard.isKeyDown(Keyboard.KEY_A))
		        camera.strafe(-0.1f);

	        if (Keyboard.isKeyDown(Keyboard.KEY_D))
		        camera.strafe(0.1f);

	        glMultMatrix(camera.getCameraLocationBuffer());

	        glTranslatef(camera.getTranslation().getX(),
			        camera.getTranslation().getY(),
			        camera.getTranslation().getZ());

	        glScalef(camera.getScale().getX(),
			        camera.getScale().getY(),
			        camera.getScale().getZ());

	        System.out.print(" MV MAT: " + camera.getCameraLocationBuffer().toString());
	        System.out.print("   DIR: " + camera.direction);
	        System.out.print("   TRANS: " + camera.getTranslation());
	        System.out.println("   ROT: " + camera.getRotation());

	        /*
	         * Draw loaded obj
	         */
	        base.bindBuffers();
	        base.drawBuffers();

	        sphere.setTranslation(getExoplanetPosition(rt.getIntervalInt(), 0.08f, 350f));
	        sphere.setScale(new Vector3f(15f, 15f, 15f));
	        sphere.bindBuffers();
	        sphere.drawBuffers();

            Display.update();
            Display.sync(60);
        }

	    glDisableClientState(GL_NORMAL_ARRAY);
	    glDisableClientState(GL_VERTEX_ARRAY);

        Display.destroy();

    }

	private static float angle = 0f;
	public static Vector3f getExoplanetPosition(int timerResolution, float speed, float radius)
	{
		angle = angle + (timerResolution * speed);
		angle = angle % 360;

		Vector3f position = new Vector3f();
		position.setY(200f);
		position.setX(radius * (float) Math.cos(Math.toRadians(angle)));
		position.setZ(radius * (float) Math.sin(Math.toRadians(angle)));

		return position;
	}
}
