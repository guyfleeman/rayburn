package test.projectiontesting;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.vector.Matrix4f;
import com.normalizedinsanity.rayburn.engine.util.MathUtil;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.util.glu.GLU.gluPerspective;

/**
 * @author Will Stuckey
 * @date 11/11/13
 * <b>This class contains the display initialization and render loop code to make a "space" render. Points are stars.</b>
 */
public class VertexBufferEngine
{
    private final boolean fullscreen;
    private final boolean smoothAndDeferredAA;
    private final boolean vsyncEnabled = false;
    private final int frameTgt = 60;
	private final int dof = 10000;

    private final int width;
    private final int height;
    private final int numPoints;

    /**
     * @param fullscreen Should the display be stripped (e.g. no title and border)
     * @param numPoints The number of points (starts) to be initialized
     * Primary constructor. Initializes the display and points, and then begins the render loop (VBO & CBO).
     * The display is automatically initialized to the dimensions of the screen.
     */
    public VertexBufferEngine(boolean fullscreen, boolean smooth, int numPoints)
    {
        this.smoothAndDeferredAA = smooth;
        this.fullscreen = fullscreen;
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.width = (int)dim.getWidth();
        this.height = (int)dim.getHeight();
        this.numPoints = numPoints;
        systemInit();
    }

    /**
     * @param fullscreen Should the display be stripped (e.g. no title and border)
     * @param width Width of the display in pixels
     * @param height Height of the display in pixels
     * @param numPoints The number of points (starts) to be initialized
     * Secondary constructor. Initializes the display and points, and then begins the render loop (VBO & CBO)
     */
    public VertexBufferEngine(boolean fullscreen, boolean smooth, int width, int height, int numPoints)
    {
        this.smoothAndDeferredAA = smooth;
        this.fullscreen = fullscreen;
        this.width = width;
        this.height = height;
        this.numPoints = numPoints;
        systemInit();
    }

    private void systemInit()
    {
        System.out.println("-------------------------*-------------------------");

        System.out.print("Initializing the frame......................... ");
        try
        {
            if (fullscreen)
            {
                Mouse.setGrabbed(true);
                System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
            }

            Display.setDisplayMode(new DisplayMode(width, height));
            Display.setTitle("VBO RENDER");
            Display.create(new PixelFormat(8, 8, 8));
        }
        catch (LWJGLException e)
        {
            System.out.println("FAILED.");
            e.printStackTrace();
            Display.destroy();
            System.exit(-1000);
        }

        if (smoothAndDeferredAA)
        {
            glEnable(GL_POINT_SMOOTH);
            glEnable(GL_LINE_SMOOTH);
            glEnable(GL_POLYGON_SMOOTH);
            glEnable(GL_POINT_SMOOTH_HINT);
            glEnable(GL_LINE_SMOOTH_HINT);
            glEnable(GL_POLYGON_SMOOTH_HINT);
            glEnable(GL_PERSPECTIVE_CORRECTION_HINT);

            glHint(GL_NICEST, GL_POINT_SMOOTH_HINT);
            glHint(GL_NICEST, GL_LINE_SMOOTH_HINT);
            glHint(GL_NICEST, GL_POLYGON_SMOOTH_HINT);
            glHint(GL_NICEST, GL_PERSPECTIVE_CORRECTION_HINT);
        }

	    glEnable(GL_BLEND);
	    glEnable(GL_DEPTH_TEST);

        System.out.println("DONE. fullscreen->" + fullscreen + ", height->" + height + ", width->" + width);

        System.out.print("Initializing the 3D space...................... ");

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective((float) 30, 1000f / 800f, 0.001f, dof);
        glMatrixMode(GL_MODELVIEW);

        System.out.println("DONE. GLContext->GLU");

        System.out.print("Initializing point data........................ ");
        Random random = new Random();
        ArrayList<test.projectiontesting.Point> points = new ArrayList();
	     for (int i = 0; i < numPoints; i++)
		     points.add(new test.projectiontesting.Point(
				     (random.nextFloat() - 0.5f) * 500f,
				     (random.nextFloat() - 0.5f) * 500f,
				     (float)random.nextInt(dof) - dof,
				     random.nextFloat() - 0.25f,
				     random.nextFloat() - 0.25f,
				     random.nextFloat())
		     );

        System.out.println("DONE. num->" + numPoints);

        System.out.print("Stripping vertex data.......................... ");
        ArrayList<Float> vertCoord = new ArrayList<Float>();
        for (test.projectiontesting.Point p : points)
        {
            vertCoord.add(p.x);
            vertCoord.add(p.y);
            vertCoord.add(p.z);
        }

        if (vertCoord.size() != (points.size() * 3))
        {
            System.out.println("FAILED. Data Size Mismatch");
            System.exit(-1000);
        }

	    float[] vertCoordDataStripped = new float[vertCoord.size()];
        int index = 0;
	    for (Float f : vertCoord)
	        vertCoordDataStripped[index++] = (f != null ? f : Float.NaN);

        vertCoord = null;
        System.gc();
        System.out.println("DONE.");

		System.out.print("Stripping color data........................... ");
        ArrayList<Float> colorCoord = new ArrayList<Float>(numPoints * 3);
	    for (test.projectiontesting.Point p : points)
	    {
		    colorCoord.add(p.r);
		    colorCoord.add(p.g);
		    colorCoord.add(p.b);
	    }

	    if (colorCoord.size() != (points.size() * 3))
	    {
		    System.out.println("FAILED.");
		    System.exit(-1000);
	    }

	    float[] colorCoordDataStripped = new float[colorCoord.size()];
	    index = 0;
	    for (Float f : colorCoord)
	        colorCoordDataStripped[index++] = (f != null ? f : Float.NaN);

        colorCoord = null;
        System.gc();
        System.out.println("DONE.");

        System.out.print("DATA STRIPPED. Destroying point references..... ");
        for (test.projectiontesting.Point p : points)
            p = null;
        points = null;
        System.gc();
        System.out.println("DONE.");

        float[] cubeData =
                {
                    1.0f, 1.0f, 1.0f,
                    1.0f, -1.0f, 1.0f,
                    -1.0f, -1.0f, 1.0f,
                    -1.0f, 1.0f, 1.0f
                };

        System.out.print("Initializing the vertex float buffer........... ");
        FloatBuffer vertexData = BufferUtils.createFloatBuffer(numPoints * 3);
	    //vertexData.put(vertCoordDataStripped);
        vertexData.put(cubeData);
        System.out.print("flipping..... ");
        vertexData.flip();
        System.out.println("DONE. num->" + (numPoints * 3));

        System.out.print("Initializing the color float buffer............ ");
        FloatBuffer colorData = BufferUtils.createFloatBuffer(numPoints * 3);
	    colorData.put(colorCoordDataStripped);
        System.out.print("flipping..... ");
        colorData.flip();
        System.out.println("DONE. num->" + (numPoints * 3));

        System.out.print("Initializing the vertex handler................ ");
        int vboVertexHandler = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboVertexHandler);
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        System.out.println("DONE. vertexBuffer->bound, vertexBuffer->static");

        System.out.print("Initializing the color handler................. ");
        int vboColorHandler = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboColorHandler);
        glBufferData(GL_ARRAY_BUFFER, colorData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        System.out.println("DONE. colorBuffer->bound, colorBuffer->static");

	    System.out.print("Initializing controls.......................... ");
	    ControlsThread controlsHandler = new ControlsThread();
	    controlsHandler.start();
	    ReferenceTimer rt = new ReferenceTimer();
	    System.out.println("DONE. mouse.grabbed->" + fullscreen +", thread->started");

	    System.out.print("Initializing context........................... ");
	    Matrix4f viewMatrix = MathUtil.getIdentityMatrix();
	    FloatBuffer viewBuffer = BufferUtils.createFloatBuffer(16);
	    System.out.println("DONE.");

        System.out.println("SYSTEM INIT COMPLETE. PROCEEDING TO RENDER LOOP.");
	    System.out.println("-------------------------*-------------------------");

        float translation = 0.0f;
	    while (!Display.isCloseRequested())
	    {
			if (controlsHandler.isWindowCloseRequested())
				break;

		    //Clear buffers to free cached junk
		    glClear(GL_COLOR_BUFFER_BIT);
		    glClear(GL_DEPTH_BUFFER_BIT);

            glLoadIdentity();

		    rt.update();
		    Display.setTitle("VBO RENDER: fps=" + 1000 / rt.getIntervalInt() + " tranlation=" + controlsHandler.getTranslationVector().getZ());

            /*
            Matrix4f.scale(new Vector3f(1f, 1f, 1f), viewMatrix, viewMatrix);
            Matrix4f.translate(controlsHandler.getTranslationVector(), viewMatrix, viewMatrix);
            Matrix4f.rotate(controlsHandler.getRotationVector().getX(), MathUtil.xVector, viewMatrix, viewMatrix);
            Matrix4f.rotate(controlsHandler.getRotationVector().getY(), MathUtil.yVector, viewMatrix, viewMatrix);
            Matrix4f.rotate(controlsHandler.getRotationVector().getZ(), MathUtil.zVector, viewMatrix, viewMatrix);
            viewMatrix.store(viewBuffer);
            viewBuffer.flip();
            */

            /*
            viewMatrix = _MathMatrixUtil.getIdentity();
		    Matrix4f.mul(_MathMatrixUtil.getRotationMatrix(controlsHandler.getRotationVector()),
				    _MathMatrixUtil.getTranslationMatrix(controlsHandler.getTranslationVector()),
				    viewMatrix);
            Matrix4f.mul(new Projection(width, height).getProjectionMatrix(), viewMatrix, viewMatrix);
		    viewMatrix.store(viewBuffer);
		    */

            //glMatrixMode(GL_PROJECTION_MATRIX);
		    //viewBuffer.flip();
		    //glMultMatrix(viewBuffer);
		    //glMatrixMode(GL_MODELVIEW);

		    glBindBuffer(GL_ARRAY_BUFFER, vboVertexHandler);
		    glVertexPointer(3, GL_FLOAT, 0, 0L);

		    glBindBuffer(GL_ARRAY_BUFFER, vboColorHandler);
		    glColorPointer(3, GL_FLOAT, 0, 0L);

		    glEnableClientState(GL_VERTEX_ARRAY);
		    glEnableClientState(GL_COLOR_ARRAY);

		    glDrawArrays(GL_QUADS, 0, 12);

		    glDisableClientState(GL_COLOR_ARRAY);
		    glDisableClientState(GL_VERTEX_ARRAY);

            Display.update();
            Display.sync(frameTgt);
	    }

	    glDeleteBuffers(vboVertexHandler);
	    glDeleteBuffers(vboColorHandler);

	    controlsHandler.terminate();

        //Ensure the controls thread is destroyed before the GLContext is destroyed.
        //This prevents an "Illegal State Exception" for the keyboard when the context is absent after destruction but,
        //the controls thread is still running.
        try
        {
            Thread.sleep(100);
        }
        catch (InterruptedException e) {}

	    Display.destroy();
	    System.exit(0);
    }
}