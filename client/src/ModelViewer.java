package client;

import java.io.FileReader;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

public class ModelViewer extends AbstractGLWindow
{
    //public JSONArray model = null;
    public GameWorld world = new GameWorld();
    public CameraHandler cameraHandler = new CameraHandler();
    public SecondAttemptAtInput saai = null;
    public KeyListenerSmoother smoother = new KeyListenerSmoother(10);

    public void setProjection(GL2 gl2, float width, float height)
    {
        gl2.glMatrixMode(gl2.GL_PROJECTION);
        gl2.glLoadIdentity();
        gl2.glOrtho(-width, width, -height, height, 1, -4);
        cameraHandler.apply(gl2);
        //gl2.glRotatef(xrot, 1, 0, 0);
        //gl2.glRotatef(yrot, 0, 1, 0);
        //gl2.glRotatef(zrot, 0, 0, 1);
        //gl2.glTranslatef(-xpos, -ypos, -zpos);
    }

    public void setPerspectiveProjection(GL2 gl2)
    {
        gl2.glMatrixMode(gl2.GL_PROJECTION);
        gl2.glLoadIdentity();
        gl2.glFrustum(-1, 1, -1, 1, .5, 100);
        gl2.glMatrixMode(gl2.GL_MODELVIEW);
        gl2.glLoadIdentity();
        cameraHandler.apply(gl2);
    }
    public void enableDepth(GL2 gl2)
    {
        gl2.glClearColor(0,0,0,1);
        gl2.glShadeModel(gl2.GL_SMOOTH);
        gl2.glClearDepth(1.0f);
        gl2.glEnable(gl2.GL_DEPTH_TEST);
        gl2.glDepthFunc(gl2.GL_LEQUAL);

        gl2.glEnable(gl2.GL_ALPHA_TEST);
        gl2.glEnable(gl2.GL_BLEND);
        gl2.glBlendFunc(gl2.GL_SRC_ALPHA,gl2.GL_ONE_MINUS_SRC_ALPHA);
        gl2.glClear(gl2.GL_COLOR_BUFFER_BIT | gl2.GL_DEPTH_BUFFER_BIT);
    }

    public void display(GLAutoDrawable drawable)
    {
        GL2 gl2 = drawable.getGL().getGL2();
        enableDepth(gl2);
        //setProjection(gl2, cameraHandler.widthScale, cameraHandler.heightScale);
        setPerspectiveProjection(gl2);
        gl2.glClear(gl2.GL_COLOR_BUFFER_BIT);
        synchronized(world) {
            for(DrawObject d : world.actors.values()) {
                if(d != world.getPlayer()) {
                    d.draw(gl2);
                }
            }
        }
    }
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}
    public void init(GLAutoDrawable drawable) {}
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
    public void dispose(GLAutoDrawable drawable) {}

    public ModelViewer(int w, int h)
    {
        Socket connectionToServer;
        try
        {
            connectionToServer = new Socket("localhost", 51701);
            saai = new SecondAttemptAtInput(connectionToServer.getOutputStream());
            ServerSyncher synch = new ServerSyncher(connectionToServer, world, cameraHandler);
            new Thread(synch).start();
        }
        catch(Exception e) { e.printStackTrace(); }
        smoother.addKeyListener(saai);
        smoother.addKeyListener(cameraHandler);
        GLCanvas glcanv = constructorAux(w, h, 60);
        glcanv.addKeyListener(smoother);
        glcanv.requestFocus();
        JSONArray model = null;
        try { model = (JSONArray)JSONValue.parse(new FileReader("unit_sphere.json")); }
        //try { model = (JSONArray)JSONValue.parse(new FileReader("unit_cylinder.json")); }
        catch(Exception e) { e.printStackTrace(); }
        world.actors.put(-1L, new DrawObject(0, 0, 0, 0, 0, "sphere", model));
        world.actors.put(0L, new DrawObject(0, 5, 0, 0, 0, "sphere", model));
    }
    public static void main(String[] args)
    {
        ModelViewer mv = new ModelViewer(WIDTH, HEIGHT);
        JFrame jf = do_main(mv);
        jf.addMouseWheelListener(mv.cameraHandler);
        jf.addKeyListener(mv.smoother);
    }
}
