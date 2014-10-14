package com.rayburn.engine;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.vector.Vector3f;
import com.rayburn.engine.ingestion.ResourceParser;
import com.rayburn.engine.entity.geom.OBJModel;
import test.projectiontesting.ReferenceTimer;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.FloatBuffer;

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
import static org.lwjgl.opengl.GL11.glShadeModel;
import static org.lwjgl.opengl.GL11.glTranslatef;

import static org.lwjgl.util.glu.GLU.gluPerspective;




/**
 * Created by Lizzy McPherson on 5/16/2014.
 */
public class ExoplanetEngine
{
    /*
     * Screen does not automatically start in full screen, sets dimensions
     */
    private boolean fullscreen = false;
    private int height = 600;
    private int width = 600;

    private File exoplanetFile = null;
    private File sunFile = null;

    /**
     *
     * @param fullscreen determines whether or not program starts in full screen
     * @param sunFile OBJ file for Sun
     * @param exoplanetFile OBJ file for Exoplanet
     */
    public ExoplanetEngine(boolean fullscreen, File sunFile, File exoplanetFile)
    {
        this.fullscreen = fullscreen;
        this.exoplanetFile = exoplanetFile;
        this.sunFile = sunFile;

        start();
    }

    private void start()
    {
        try
        {
            if(fullscreen == true)
            {
                /*
                 * hide the mouse, hide the frame
                 */
                Mouse.setGrabbed(true);
                System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");

                /*
                 * auto-sizes the screen
                 */
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                Dimension screen = toolkit.getScreenSize();
                width = (int)screen.getWidth();
                height = (int)screen.getHeight();
            }

            /*
             * creates screen display
             */
            Display.setDisplayMode(new DisplayMode(width, height));
            Display.setTitle("exoplanet demo");
            Display.create();
            Display.setVSyncEnabled(true);
            Display.update();
        }
        catch(LWJGLException e)
        {
            e.printStackTrace();
            Display.destroy();
            System.exit(0);
        }


        //////////////////////
        //  BEGIN LIGHTING  //
        //////////////////////

        /*
         * creates lights, lighting patterns and positions
         */


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

        glMaterialf(GL_FRONT, GL_SHININESS, 50f);
        glMaterial(GL_FRONT, GL_SPECULAR, specular);

        ////////////////////
        //  END LIGHTING  //
        ////////////////////

        /*
         * handles camera and camera rotation
         */
        Camera exoplanetCamera = new Camera();

        /*
         * synchronizes animations
         */
        ReferenceTimer timer = new ReferenceTimer();

        OBJModel sun = new OBJModel();
        OBJModel exoplanet = new OBJModel();

       /*
        * processes OBJ files into a model
        */
        try
        {
            sun = ResourceParser.genModelFromWavefrontOBJ(sunFile);
            exoplanet = ResourceParser.genModelFromWavefrontOBJ(exoplanetFile);
        }
        catch (FileNotFoundException e)
        {
            System.out.println(e.toString());
            System.exit(-1500);
        }

        /*
         * sets up objects to be rendered
         */
        sun.constructBuffers();
        exoplanet.constructBuffers();

        /*
         * makes exoplanet smaller than the sun
         */
        exoplanet.setScale(new Vector3f(0.1f, 0.1f, 0.1f));

        /*
         * entering the main render loop
         */
        while (!Display.isCloseRequested())
        {
            if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
                break;

            glClear(GL_COLOR_BUFFER_BIT);
            glClear(GL_DEPTH_BUFFER_BIT);

            /*
             * sets up the perspective
             */
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            gluPerspective(60f, (float)width / (float)height, 0.1f, 100f);

            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();

            timer.update();

            /*
             * receives mouse movements, updates camera
             */
            float mouseDX = (float) Mouse.getDX() / 5f;
            float mouseDY = (float) -Mouse.getDY() / 5f;
            exoplanetCamera.updateRotation(new Vector3f(0f, mouseDX, mouseDY));
            exoplanetCamera.align();

           /*
            * when certain key is pressed, moves camera
            */
            if (Keyboard.isKeyDown(Keyboard.KEY_W))
                exoplanetCamera.move(0.3f);

            if (Keyboard.isKeyDown(Keyboard.KEY_S))
                exoplanetCamera.move(-0.3f);

            if (Keyboard.isKeyDown(Keyboard.KEY_A))
                exoplanetCamera.strafe(-0.3f);

            if (Keyboard.isKeyDown(Keyboard.KEY_D))
                exoplanetCamera.strafe(0.3f);

            if(Keyboard.isKeyDown(Keyboard.KEY_R))
                exoplanetCamera.updateTranslation(new Vector3f(0f, -0.3f, 0f));

            if(Keyboard.isKeyDown(Keyboard.KEY_F))
                exoplanetCamera.updateTranslation(new Vector3f(0f, 0.3f, 0f));

           /*
            * rotates and translates the scene (camera movement)
            */
            glMultMatrix(exoplanetCamera.getCameraLocationBuffer());

            glTranslatef(exoplanetCamera.getTranslation().getX(),
                    exoplanetCamera.getTranslation().getY(),
                    exoplanetCamera.getTranslation().getZ());

           /*
            * draws lighting
            */
            glLight(GL_LIGHT0, GL_POSITION, lightLocOne);
            glLight(GL_LIGHT1, GL_POSITION, lightLocTwo);

            /*
             * draws the sun
             */
            sun.bindBuffers();
            sun.drawBuffers();

            /*
             * updates position and draws the exoplanet
             */
            exoplanet.setTranslation(getExoplanetPosition(timer.getIntervalInt(), 0.04f, 5f));
            exoplanet.bindBuffers();
            exoplanet.drawBuffers();

            /*
             * updates the frame
             */
            Display.update();
            Display.sync(60);
        }

        /*
         * garbage collection
         */
        sun.destructBuffers();
        exoplanet.destructBuffers();

        glDisableClientState(GL_NORMAL_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);

        Display.destroy();
    }

   /*
    * exoplanet postiton using vectors
    */
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

