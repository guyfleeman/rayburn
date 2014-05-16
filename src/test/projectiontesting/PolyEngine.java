package test.projectiontesting;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import rayburn.engine.util.MathUtil;
import rayburn.game.util.geom.Cube;

import static org.lwjgl.util.glu.GLU.*;

import static rayburn.engine.util.MathUtil.*;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author StuckeyWilliamM
 * @date 12/16/13
 * <b></b>
 */
public class PolyEngine
{
    private boolean fullscreen = true;
    private final int height = 600;
    private final int width = 600;

    public PolyEngine()
    {
        main();
    }

    private void main()
    {
        try
        {
            if (fullscreen)
            {
                Mouse.setGrabbed(true);
                System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
            }

            Display.setDisplayMode(new DisplayMode(width, height));
            Display.setTitle("POLY RENDER");
            Display.create(new PixelFormat(8, 8, 8));
            Display.setVSyncEnabled(true);
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

        glMatrixMode(GL_PROJECTION);
        glMatrixMode(GL_MODELVIEW);

        System.out.print("Initializing controls.......................... ");
        ControlsThread controlsHandler = new ControlsThread();
        controlsHandler.start();
        ReferenceTimer rt = new ReferenceTimer();
        System.out.println("DONE. mouse.grabbed->" + fullscreen +", thread->started");

        System.out.print("Initializing context........................... ");
	    MathUtil.setMathMode(MathMode.COLUMN_MAJOR_ORDERING);
	    System.out.print(MathUtil.getMathMode() + ".....");
	    Matrix4f viewMatrix = getIdentityMatrix();
        FloatBuffer viewBuffer = BufferUtils.createFloatBuffer(16);
        System.out.println("DONE.");

        System.out.println("SYSTEM INIT COMPLETE. PROCEEDING TO RENDER LOOP.");
        System.out.println("-------------------------*-------------------------");

        float xMov = -2.0f;
        float cubeRot = 0.0f;
        while (!Display.isCloseRequested())
        {
            if (controlsHandler.isWindowCloseRequested())
                break;

            //Clear buffers to free cached junk
            glClear(GL_COLOR_BUFFER_BIT);
            glClear(GL_DEPTH_BUFFER_BIT);

	        glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
	        gluPerspective(60f, (float)height / (float)width, 0.1f, 100f);
	        glMatrixMode(GL_MODELVIEW);
	        glLoadIdentity();

            rt.update();
            Display.setTitle("VBO RENDER: fps=" + 1000 / (rt.getIntervalInt() + 1) + " tranlation=" + controlsHandler.getTranslationVector().getZ() + " yRot=" + controlsHandler.getRotationVector().getY());


            viewMatrix = MathUtil.getIdentityMatrix();

            /*
            viewBuffer.clear();
            //Matrix4f.scale(new Vector3f(1f, 1f, 1f), viewMatrix, viewMatrix);
            Matrix4f.translate(controlsHandler.getTranslationVector(), viewMatrix, viewMatrix);
            Matrix4f.rotate(controlsHandler.getRotationVector().getX(), MathUtil.xVector, viewMatrix, viewMatrix);
            Matrix4f.rotate(controlsHandler.getRotationVector().getY(), MathUtil.yVector, viewMatrix, viewMatrix);
            Matrix4f.rotate(controlsHandler.getRotationVector().getZ(), MathUtil.zVector, viewMatrix, viewMatrix);
            viewMatrix.store(viewBuffer);
            viewBuffer.flip();
            glMultMatrix(viewBuffer);
            */

            /*
            viewBuffer.clear();
            getTranslationMatrix(controlsHandler.getTranslationVector()).store(viewBuffer);
            viewBuffer.flip();
            glMultMatrix(viewBuffer);


	        viewBuffer.clear();
	        getRotationMatrix(controlsHandler.getRotationVector()).store(viewBuffer);
	        viewBuffer.flip();
	        glMultMatrix(viewBuffer);
	        */


            MathUtil.getViewAxis(controlsHandler.getRotationAxis(), controlsHandler.getViewAxis(), controlsHandler.getMouseDelta().getY());
            MathUtil.getViewAxis(controlsHandler.getRotationAxis(), controlsHandler.getUpAxis(), controlsHandler.getMouseDelta().getX());
            gluLookAt(

                    controlsHandler.getTranslationVector().getX(),
                    controlsHandler.getTranslationVector().getY(),
                    controlsHandler.getTranslationVector().getZ(),

                    /*
                    controlsHandler.getViewAxis().getX(),
                    controlsHandler.getViewAxis().getY(),
                    controlsHandler.getViewAxis().getZ(),
                    */

                    controlsHandler.getRotationVector().getX(),
                    controlsHandler.getRotationVector().getY(),
                    controlsHandler.getRotationVector().getZ(),



                    controlsHandler.getUpAxis().getX(),
                    controlsHandler.getUpAxis().getY(),
                    controlsHandler.getUpAxis().getZ());

	        /*
            viewMatrix = getIdentityMatrix();
            Matrix4f.mul(viewMatrix, getRotationMatrix(controlsHandler.getRotationVector()), viewMatrix);
            Matrix4f.mul(viewMatrix, getTranslationMatrix(controlsHandler.getTranslationVector()), viewMatrix);

	        viewBuffer.clear();
            viewMatrix.store(viewBuffer);
            viewBuffer.flip();
            glMultMatrix(viewBuffer);
            */

	        Cube c1 = new Cube();
	        c1.setLocation(new Vector3f(2f, 0f, 2f));
	        c1.draw();

	        Cube c2 = new Cube();
	        c2.setLocation(new Vector3f(-2f, 0f, 2f));
	        c2.draw();

	        Cube c3 = new Cube();
	        c3.setLocation(new Vector3f(2f, 0f, -2f));
	        c3.draw();

	        Cube c4 = new Cube();
	        c4.setLocation(new Vector3f(-2f, 0f, -2f));
	        c4.draw();

            glPushMatrix();
            glRotatef(cubeRot, 1.0f, 1.0f, 1.0f);
            cubeRot += 1.0f;

	        glBegin(GL_QUADS);
	        glColor3f(0.0f, 1.0f, 0.0f);          // Set The Color To Green
	        glVertex3f(0.1f, 0.1f, -0.1f);          // Top Right Of The Quad (Top)
	        glVertex3f(-0.1f, 0.1f, -0.1f);          // Top Left Of The Quad (Top)
	        glVertex3f(-0.1f, 0.1f, 0.1f);          // Bottom Left Of The Quad (Top)
	        glVertex3f(0.1f, 0.1f, 0.1f);          // Bottom Right Of The Quad (Top)

	        glColor3f(1.0f, 0.5f, 0.0f);          // Set The Color To Orange
	        glVertex3f(0.1f, -0.1f, 0.1f);          // Top Right Of The Quad (Bottom)
	        glVertex3f(-0.1f, -0.1f, 0.1f);          // Top Left Of The Quad (Bottom)
	        glVertex3f(-0.1f, -0.1f, -0.1f);          // Bottom Left Of The Quad (Bottom)
	        glVertex3f(0.1f, -0.1f, -0.1f);          // Bottom Right Of The Quad (Bottom)

	        glColor3f(1.0f, 0.0f, 0.0f);          // Set The Color To Red
	        glVertex3f(0.1f, 0.1f, 0.1f);          // Top Right Of The Quad (Front)
	        glVertex3f(-0.1f, 0.1f, 0.1f);          // Top Left Of The Quad (Front)
	        glVertex3f(-0.1f, -0.1f, 0.1f);          // Bottom Left Of The Quad (Front)
	        glVertex3f(0.1f, -0.1f, 0.1f);          // Bottom Right Of The Quad (Front)

	        glColor3f(1.0f, 1.0f, 0.0f);          // Set The Color To Yellow
	        glVertex3f(0.1f, -0.1f, -0.1f);          // Bottom Left Of The Quad (Back)
	        glVertex3f(-0.1f, -0.1f, -0.1f);          // Bottom Right Of The Quad (Back)
	        glVertex3f(-0.1f, 0.1f, -0.1f);          // Top Right Of The Quad (Back)
	        glVertex3f(0.1f, 0.1f, -0.1f);          // Top Left Of The Quad (Back)

	        glColor3f(0.0f, 0.0f, 1.0f);          // Set The Color To Blue
	        glVertex3f(-0.1f, 0.1f, 0.1f);          // Top Right Of The Quad (Left)
	        glVertex3f(-0.1f, 0.1f, -0.1f);          // Top Left Of The Quad (Left)
	        glVertex3f(-0.1f, -0.1f, -0.1f);          // Bottom Left Of The Quad (Left)
	        glVertex3f(-0.1f, -0.1f, 0.1f);          // Bottom Right Of The Quad (Left)

	        glColor3f(1.0f, 0.0f, 1.0f);          // Set The Color To Violet
	        glVertex3f(0.1f, 0.1f, -0.1f);          // Top Right Of The Quad (Right)
	        glVertex3f(0.1f, 0.1f, 0.1f);          // Top Left Of The Quad (Right)
	        glVertex3f(0.1f, -0.1f, 0.1f);          // Bottom Left Of The Quad (Right)
	        glVertex3f(0.1f, -0.1f, -0.1f);          // Bottom Right Of The Quad (Right)
	        glEnd();
            glPopMatrix();

            Display.update();
            Display.sync(60);
        }

        controlsHandler.terminate();

        try
        {
            Thread.sleep(100);
        }
        catch (InterruptedException e) {}

        Display.destroy();

    }
}
