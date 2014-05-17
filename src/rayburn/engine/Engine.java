package rayburn.engine;

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
import rayburn.game.util.geom.OBJModel;
import test.projectiontesting.ReferenceTimer;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.FloatBuffer;
import java.util.Scanner;

import static org.lwjgl.opengl.GL11.GL_AMBIENT;
import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_DIFFUSE;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_LIGHT0;
import static org.lwjgl.opengl.GL11.GL_LIGHT1;
import static org.lwjgl.opengl.GL11.GL_LIGHT2;
import static org.lwjgl.opengl.GL11.GL_LIGHT3;
import static org.lwjgl.opengl.GL11.GL_LIGHT4;
import static org.lwjgl.opengl.GL11.GL_LIGHT5;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_LIGHT_MODEL_AMBIENT;
import static org.lwjgl.opengl.GL11.GL_LIGHT_MODEL_TWO_SIDE;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_NORMAL_ARRAY;
import static org.lwjgl.opengl.GL11.GL_POSITION;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_SHININESS;
import static org.lwjgl.opengl.GL11.GL_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_SPECULAR;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glDisableClientState;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glLight;
import static org.lwjgl.opengl.GL11.glLightModel;
import static org.lwjgl.opengl.GL11.glLightModeli;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMaterial;
import static org.lwjgl.opengl.GL11.glMaterialf;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glMultMatrix;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glShadeModel;
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
	private boolean controller = false;
    private int height = 800;
	private int width = 800;

    public Engine(boolean fullscreen, boolean controller)
    {
	    this.fullscreen = fullscreen;
	    this.controller = controller;
        main();
    }

    private void main()
    {
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

	    FloatBuffer lightColorSun = BufferUtils.createFloatBuffer(4);
	    lightColorSun.put(1f).put(1f).put(0.2f).put(100f).flip();

	    FloatBuffer lightColorTest = BufferUtils.createFloatBuffer(4);
	    lightColorTest.put(0f).put(1f).put(0f).put(10f).flip();

	    float size = 4f;

	    FloatBuffer lightLocOne = BufferUtils.createFloatBuffer(4);
	    lightLocOne.put(0f).put(size).put(0f).put(1f).flip();

	    FloatBuffer lightLocTwo = BufferUtils.createFloatBuffer(4);
	    lightLocTwo.put(0f).put(-size).put(0f).put(1f).flip();

	    FloatBuffer lightLocThree = BufferUtils.createFloatBuffer(4);
	    lightLocOne.put(size).put(0f).put(0f).put(1f).flip();

	    FloatBuffer lightLocFour = BufferUtils.createFloatBuffer(4);
	    lightLocTwo.put(-size).put(0f).put(0f).put(1f).flip();

	    FloatBuffer lightLocFive = BufferUtils.createFloatBuffer(4);
	    lightLocOne.put(0f).put(0f).put(size).put(1f).flip();

	    FloatBuffer lightLocSix = BufferUtils.createFloatBuffer(4);
	    lightLocTwo.put(0f).put(0f).put(-size).put(1f).flip();

	    /*
	    FloatBuffer lightLocOne = BufferUtils.createFloatBuffer(4);
	    lightLocOne.put(size).put(-size).put(-size).put(1f).flip();

	    FloatBuffer lightLocTwo = BufferUtils.createFloatBuffer(4);
	    lightLocTwo.put(size).put(-size).put(size).put(1f).flip();

	    FloatBuffer lightLocThree = BufferUtils.createFloatBuffer(4);
	    lightLocOne.put(-size).put(-size).put(size).put(1f).flip();

	    FloatBuffer lightLocFour = BufferUtils.createFloatBuffer(4);
	    lightLocTwo.put(-size).put(-size).put(-size).put(1f).flip();

	    FloatBuffer lightLocFive = BufferUtils.createFloatBuffer(4);
	    lightLocOne.put(size).put(size).put(-size).put(1f).flip();

	    FloatBuffer lightLocSix = BufferUtils.createFloatBuffer(4);
	    lightLocTwo.put(size).put(size).put(size).put(1f).flip();

	    FloatBuffer lightLocSeven = BufferUtils.createFloatBuffer(4);
	    lightLocOne.put(-size).put(size).put(size).put(1f).flip();

	    FloatBuffer lightLocEight = BufferUtils.createFloatBuffer(4);
	    lightLocTwo.put(-size).put(size).put(-size).put(1f).flip();
	    */

	    FloatBuffer ambient = BufferUtils.createFloatBuffer(4);
	    ambient.put(1f).put(1f).put(1f).put(1f).flip();

	    FloatBuffer diffuse = BufferUtils.createFloatBuffer(4);
	    diffuse.put(1f).put(1f).put(1f).put(1f).flip();

	    FloatBuffer specular = BufferUtils.createFloatBuffer(4);
	    specular.put(1f).put(1f).put(1f).put(1f).flip();

        glEnable(GL_BLEND);
	    glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
	    glEnable(GL_LIGHTING);
	    glEnable(GL_LIGHT0);
	    glEnable(GL_LIGHT1);
	    glCullFace(GL_BACK);

	    glShadeModel(GL_SMOOTH);
	    glLightModel(GL_LIGHT_MODEL_AMBIENT, lightColorSun);
	    glLightModeli(GL_LIGHT_MODEL_TWO_SIDE, GL_TRUE);
	    glLight(GL_LIGHT0, GL_COLOR, lightColorSun);
	    glLight(GL_LIGHT0, GL_POSITION, lightLocOne);

	    glLight(GL_LIGHT1, GL_COLOR, lightColorSun);
	    glLight(GL_LIGHT1, GL_AMBIENT, ambient);
	    glLight(GL_LIGHT1, GL_DIFFUSE, diffuse);
	    glLight(GL_LIGHT1, GL_SPECULAR, specular);
	    glLight(GL_LIGHT1, GL_POSITION, lightLocTwo);

	    glLight(GL_LIGHT2, GL_COLOR, lightColorSun);
	    glLight(GL_LIGHT2, GL_AMBIENT, ambient);
	    glLight(GL_LIGHT2, GL_DIFFUSE, diffuse);
	    glLight(GL_LIGHT2, GL_SPECULAR, specular);
	    glLight(GL_LIGHT2, GL_POSITION, lightLocThree);

	    glLight(GL_LIGHT3, GL_COLOR, lightColorSun);
	    glLight(GL_LIGHT3, GL_AMBIENT, ambient);
	    glLight(GL_LIGHT3, GL_DIFFUSE, diffuse);
	    glLight(GL_LIGHT3, GL_SPECULAR, specular);
	    glLight(GL_LIGHT3, GL_POSITION, lightLocFour);

	    glLight(GL_LIGHT4, GL_COLOR, lightColorSun);
	    glLight(GL_LIGHT4, GL_AMBIENT, ambient);
	    glLight(GL_LIGHT4, GL_DIFFUSE, diffuse);
	    glLight(GL_LIGHT4, GL_SPECULAR, specular);
	    glLight(GL_LIGHT4, GL_POSITION, lightLocFive);

	    glLight(GL_LIGHT5, GL_COLOR, lightColorSun);
	    glLight(GL_LIGHT5, GL_AMBIENT, ambient);
	    glLight(GL_LIGHT5, GL_DIFFUSE, diffuse);
	    glLight(GL_LIGHT5, GL_SPECULAR, specular);
	    glLight(GL_LIGHT5, GL_POSITION, lightLocSix);

	    /*
	    glLight(GL_LIGHT6, GL_COLOR, lightColorSun);
	    glLight(GL_LIGHT6, GL_AMBIENT, ambient);
	    glLight(GL_LIGHT6, GL_DIFFUSE, diffuse);
	    glLight(GL_LIGHT6, GL_SPECULAR, specular);
	    glLight(GL_LIGHT6, GL_POSITION, lightLocSeven);

	    glLight(GL_LIGHT7, GL_COLOR, lightColorSun);
	    glLight(GL_LIGHT7, GL_AMBIENT, ambient);
	    glLight(GL_LIGHT7, GL_DIFFUSE, diffuse);
	    glLight(GL_LIGHT7, GL_SPECULAR, specular);
	    glLight(GL_LIGHT7, GL_POSITION, lightLocEight);
	    */

		glMaterialf(GL_FRONT, GL_SHININESS, 50f);
	    glMaterial(GL_FRONT, GL_SPECULAR, specular);

	    float[] color = { 1f, 1f, 1f };

	    /*
	     * Create quat camera
	     */
	    Camera camera = new Camera();
	    camera.setTranslation(new Vector3f(0f, 0f, 0f));

	    ReferenceTimer rt = new ReferenceTimer();

	    OBJModel sun = new OBJModel();
	    OBJModel exoplanet = new OBJModel();
	    try
	    {
		    sun = ResourceParser.genModelFromWavefrontOBJ(
				    new File("C:\\Users\\Will Stuckey\\Desktop\\sun.obj"));

		    exoplanet = ResourceParser.genModelFromWavefrontOBJ(
				    new File("C:\\Users\\Will Stuckey\\Desktop\\planet.obj"));
	    }
	    catch (FileNotFoundException e)
	    {
		    System.out.println(e.toString());
		    System.exit(-1500);
	    }

	    sun.constructBuffers();
	    exoplanet.constructBuffers();

	    //geom.setScale(new Vector3f(5f, 5f, 5f));
	    exoplanet.setScale(new Vector3f(0.1f, 0.1f, 0.1f));

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


	        if (myController != null)
	        {
		        if (myController.isButtonPressed(8) || myController.isButtonPressed(9))
		        {
			        camera.setTranslation(new Vector3f(0f, -500f, 0f));
			        camera.setRotation(new Vector3f(0f, 0f, 0f));
			        camera.setScale(new Vector3f(1f, 1f, 1f));
		        }

		        float deltaX = myController.getXAxisValue();
		        float deltaY = -myController.getYAxisValue();
		        float deltaZ = myController.getZAxisValue();
		        float deltaRX = myController.getRXAxisValue();
		        float deltaRY = myController.getRYAxisValue();

		        if (deltaZ > 0.1)
			        camera.updateTranslation(new Vector3f(0f, 1.0f, 0f));
		        else if (deltaZ < -0.1)
			        camera.updateTranslation(new Vector3f(0f, -1.0f, 0f));

		        if (Math.abs(deltaRX) > 0.2)
		            camera.updateRotation(new Vector3f(0f, deltaRX * 5f, 0f));

		        if (Math.abs(deltaRY) > 0.2)
		            camera.updateRotation(new Vector3f(0f, 0f, deltaRY * 5f));

		        camera.align();

		        if (Math.abs(deltaX) > 0.2)
			        camera.strafe(deltaX / 0.5f);

		        if (Math.abs(deltaY) > 0.2)
		            camera.move(deltaY / 0.5f);
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

	        glLight(GL_LIGHT0, GL_POSITION, lightLocOne);
	        glLight(GL_LIGHT1, GL_POSITION, lightLocTwo);

	        /*
	         * Draw loaded obj
	         */
	        sun.bindBuffers();
	        sun.drawBuffers();

	        exoplanet.setTranslation(getExoplanetPosition(rt.getIntervalInt(), 0.04f, 5f));
	        exoplanet.bindBuffers();
	        exoplanet.drawBuffers();

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
		position.setY(0f);
		position.setX(radius * (float) Math.cos(Math.toRadians(angle)));
		position.setZ(radius * (float) Math.sin(Math.toRadians(angle)));

		return position;
	}
}
