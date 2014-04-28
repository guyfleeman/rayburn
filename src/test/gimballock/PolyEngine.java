package test.gimballock;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.vector.Vector3f;
import rayburn.game.util.ResourceParser;
import rayburn.game.util.geom.Cube;
import test.projectiontesting.ReferenceTimer;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Scanner;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_ARRAY;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_NORMAL_ARRAY;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_QUAD_STRIP;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glColorPointer;
import static org.lwjgl.opengl.GL11.glDisableClientState;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnableClientState;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glMultMatrix;
import static org.lwjgl.opengl.GL11.glNormalPointer;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertexPointer;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.util.glu.GLU.gluPerspective;

/**
 * @author StuckeyWilliamM
 * @date 12/16/13
 * <b></b>
 */
public class PolyEngine
{
    private boolean fullscreen = false;
	private boolean controller = false;
    private int height = 800;
    private int width = 800;

    public PolyEngine(boolean fullscreen, boolean controller)
    {
	    this.fullscreen = fullscreen;
	    this.controller = controller;
        main();
    }

    private void main()
    {
	    System.out.println("# CONTROLS #");
	    System.out.println("Move: L-Stick");
	    System.out.println("Look: R-Stick");
	    System.out.println("Elevation: L-Trigger AND R-Trigger");
	    System.out.println("Reset Context: L-Click OR R-Click");
	    System.out.println("Change Block Color: a OR b OR x OR y");
	    System.out.println("Reset Block Color: L-Bumper OR R-Bumper");
	    System.out.println("# END CONTROLS #\n");

	    /*
	     * Get controllers
	     */
	    try
	    {
		    Controllers.create();
	    }
	    catch (LWJGLException e)
	    {
		    e.printStackTrace();
		    System.out.println("Controller Error");
	    }

	    /*
	     * If using an external controller, bind it
	     */
	    Controller myController = null;
	    if (controller)
		{
		    for (int i = 0; i < Controllers.getControllerCount(); i++)
			    System.out.println("Controller " + (i + 1) + ": " + Controllers.getController(i).getName());

		    Scanner input = new Scanner(System.in);
		    System.out.print("LOCK CONTROLLER ID> ");
		    myController = Controllers.getController(input.nextInt() - 1);
	    }

	    /*
	     * Create the display
	     */
	    try
	    {
		    if (fullscreen)
		    {
			    /*
			     * Grab the mouse and remove the window border
			     */
			    Mouse.setGrabbed(true);
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


        glEnable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);

	    float[] color = { 1f, 1f, 1f };

	    int vboVertexHandler = 0;
	    int vboNormalHandler = 0;
	    int vboColorHandler = 0;
	    float[] vertexData;
	    float[] normalData;
	    float[] colorData;
	    int vertLen = 0;
	    int normLen = 0;
	    try
	    {
		    File objFile = new File("C:\\Users\\Will Stuckey\\Desktop\\cubeNormTri.obj");
		    ArrayList<Float> vertices = ResourceParser.parseOBJVerticies(objFile);
		    ArrayList<Float> normals = ResourceParser.parseOBJNormals(objFile);
		    vertLen = vertices.size();
		    normLen = normals.size();
		    vertexData = new float[vertLen];
		    normalData = new float[normLen];
		    colorData = new float[vertLen];
		    for (int i = 0; i < vertLen; i++)
		    {
			    vertexData[i] = vertices.get(i);
			    colorData[i] = 1f;
		    }

		    for (int i = 0; i < normLen; i++)
		    {
			    normalData[i] = normals.get(i);
		    }

		    System.out.print("Initializing the vertex float buffer........... ");
		    FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertLen);
		    vertexBuffer.put(vertexData);
		    System.out.print("flipping..... ");
		    vertexBuffer.rewind();
		    System.out.println("DONE. num->" + (vertLen));

		    FloatBuffer normalBuffer = BufferUtils.createFloatBuffer(normLen);
		    normalBuffer.put(normalData);
		    normalBuffer.rewind();

		    System.out.print("Initializing the color float buffer............ ");
		    FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(vertLen);
		    colorBuffer.put(colorData);
		    System.out.print("flipping..... ");
		    colorBuffer.rewind();
		    System.out.println("DONE. num->" + (vertLen));

		    System.out.print("Initializing the vertex handler................ ");
		    vboVertexHandler = glGenBuffers();
		    glBindBuffer(GL_ARRAY_BUFFER, vboVertexHandler);
		    glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
		    glBindBuffer(GL_ARRAY_BUFFER, 0);
		    System.out.println("DONE. vertexBuffer->bound, vertexBuffer->static");

		    vboNormalHandler = glGenBuffers();
		    glBindBuffer(GL_ARRAY_BUFFER, vboNormalHandler);
		    glBufferData(GL_ARRAY_BUFFER, normalBuffer, GL_STATIC_DRAW);
		    glBindBuffer(GL_ARRAY_BUFFER, 0);

		    System.out.print("Initializing the color handler................. ");
		    vboColorHandler = glGenBuffers();
		    glBindBuffer(GL_ARRAY_BUFFER, vboColorHandler);
		    glBufferData(GL_ARRAY_BUFFER, colorBuffer, GL_STATIC_DRAW);
		    glBindBuffer(GL_ARRAY_BUFFER, 0);
		    System.out.println("DONE. colorBuffer->bound, colorBuffer->static");
	    }
	    catch (FileNotFoundException e)
	    {
		    e.printStackTrace();
	    }




	    /*
	     * Create quat camera
	     */
	    Camera camera = new Camera();
	    camera.setTranslation(new Vector3f(0f, 0f, 0f));

	    Cube movingCube = new Cube();
	    ReferenceTimer rt = new ReferenceTimer();


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

	        /*
	         * Load the projection matrix with a glu projection algorithm
	         */
	        glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
	        gluPerspective(60f, (float)width / (float)height, 0.1f, 100f);

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
		        camera.move(0.1f);

	        if (Keyboard.isKeyDown(Keyboard.KEY_S))
		        camera.move(-0.1f);

	        if (Keyboard.isKeyDown(Keyboard.KEY_A))
		        camera.strafe(-0.1f);

	        if (Keyboard.isKeyDown(Keyboard.KEY_D))
		        camera.strafe(0.1f);


	        if (myController != null)
	        {
		        if (myController.isButtonPressed(8) || myController.isButtonPressed(9))
		        {
			        camera.setTranslation(new Vector3f(0f, 0f, -8f));
			        camera.setRotation(new Vector3f(0f, 0f, 0f));
			        camera.setScale(new Vector3f(1f, 1f, 1f));
		        }

		        float deltaX = myController.getXAxisValue();
		        float deltaY = -myController.getYAxisValue();
		        float deltaZ = myController.getZAxisValue();
		        float deltaRX = myController.getRXAxisValue();
		        float deltaRY = myController.getRYAxisValue();

		        if (deltaZ > 0.1)
			        camera.updateTranslation(new Vector3f(0f, 0.1f, 0f));
		        else if (deltaZ < -0.1)
			        camera.updateTranslation(new Vector3f(0f, -0.1f, 0f));

		        if (Math.abs(deltaRX) > 0.2)
		            camera.updateRotation(new Vector3f(0f, deltaRX * 5f, 0f));

		        if (Math.abs(deltaRY) > 0.2)
		            camera.updateRotation(new Vector3f(0f, 0f, deltaRY * 5f));

		        camera.align();

		        if (Math.abs(deltaX) > 0.2)
			        camera.strafe(deltaX / 10f);

		        if (Math.abs(deltaY) > 0.2)
		            camera.move(deltaY / 10f);
	        }

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

	        if (myController != null)
	        {
		        //a button/green button
		        if (myController.isButtonPressed(0))
		        {
			        color[0] = 0f   / 256f;
			        color[1] = 153f / 256f;
			        color[2] = 76f  / 256f;
		        }

		        //b button/red button
		        if (myController.isButtonPressed(1))
		        {
			        color[0] = 153f / 256f;
			        color[1] = 0f   / 256f;
			        color[2] = 0f   / 256f;
		        }

		        //x button/blue button
		        if (myController.isButtonPressed(2))
		        {
			        color[0] = 0f   / 256f;
			        color[1] = 204f / 256f;
			        color[2] = 204f / 256f;
		        }

		        //y button/yellow button
		        if (myController.isButtonPressed(3))
		        {
			        color[0] = 153f / 256f;
			        color[1] = 153f / 256f;
			        color[2] = 0f   / 256f;
		        }

		        //all buttons/white
		        if (myController.isButtonPressed(4) || myController.isButtonPressed(5))
		        {
			        color[0] = 1f;
			        color[1] = 1f;
			        color[2] = 1f;
		        }
	        }

	        /*
	        for (float i = -6; i <= 6; i += 4)
		        for (float j = -6; j <= 6; j += 4)
			        for (float k = -6; k <= 6; k += 4)
			        {
				        Cube c = new Cube();
				        c.setLocation(new Vector3f(i, j, k));
				        c.setColor(color);
				        c.draw();
			        }
			*/

	        glBindBuffer(GL_ARRAY_BUFFER, vboVertexHandler);
	        glVertexPointer(3, GL_FLOAT, 0, 0L);

	        glBindBuffer(GL_ARRAY_BUFFER, vboNormalHandler);
	        glNormalPointer(GL_FLOAT, 3, 0L);

	        glBindBuffer(GL_ARRAY_BUFFER, vboColorHandler);
	        glColorPointer(3, GL_FLOAT, 0, 0L);

	        glEnableClientState(GL_VERTEX_ARRAY);
	        glEnableClientState(GL_NORMAL_ARRAY);
	        glEnableClientState(GL_COLOR_ARRAY);

	        glDrawArrays(GL_TRIANGLE_STRIP, 0, vertLen);

	        glDisableClientState(GL_COLOR_ARRAY);
	        glDisableClientState(GL_NORMAL_ARRAY);
	        glDisableClientState(GL_VERTEX_ARRAY);

	        /*
	        Cube sun = new Cube();
	        sun.setColor(new float[]{1f, 1f, 0f});
	        sun.draw();
	        */

	        movingCube.setTranslation(getExoplanetPosition(rt.getIntervalInt(), 0.1f, 5f));
	        movingCube.setColor(new float[]{1f, 0f, 0f});
	        glPushMatrix();
	        movingCube.draw();
	        glPopMatrix();

            Display.update();
            Display.sync(60);
        }

        Display.destroy();

    }

	private static float angle = 0f;
	public static Vector3f getExoplanetPosition(int timerResolution, float speed, float radius)
	{
		angle = angle + (timerResolution * speed);
		angle = angle % 360;

		Vector3f position = new Vector3f();
		position.setY(0f);
		position.setX(radius * (float) Math.cos(Math.toRadians(angle)));
		position.setZ(radius * (float) Math.sin(Math.toRadians(angle)));

		return position;
	}
}
