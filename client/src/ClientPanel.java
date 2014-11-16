package client;

import java.io.FileReader;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.JFrame;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

public class ClientPanel extends AbstractGLWindow
{
    public JSONArray playerModel = null;
    public JSONArray bulletModel = null;
    private GameObject go;

    public CameraHandler cameraHandler;

    public void setProjection(GL2 gl2, float width, float height)
    {
        gl2.glMatrixMode(gl2.GL_PROJECTION);
        gl2.glLoadIdentity();
        gl2.glOrtho(-width, width, -height, height, 1, -1);
        cameraHandler.apply(gl2);
        //gl2.glRotatef(xrot, 1, 0, 0);
        //gl2.glRotatef(yrot, 0, 1, 0);
        //gl2.glRotatef(zrot, 0, 0, 1);
        //gl2.glTranslatef(-xpos, -ypos, -zpos);
    }

    public void display(GLAutoDrawable drawable)
    {
        GL2 gl2 = drawable.getGL().getGL2();
        setProjection(gl2, cameraHandler.widthScale, cameraHandler.heightScale);
        gl2.glClear(gl2.GL_COLOR_BUFFER_BIT);
        for(DrawObject dO : go.actors.values()) {
            if(dO.type == "Player") {
                dO.draw(gl2,playerModel);
            } else if(dO.type == "Bullet") {
                dO.draw(gl2,bulletModel);
            }
        }
    }
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}
    public void init(GLAutoDrawable drawable) {}
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
    public void dispose(GLAutoDrawable drawable) {}

    public ClientPanel(int w, int h, GameObject go, CommandPusher cp, CameraHandler ch)
    {
        constructorAux(w, h, 5);
        this.go = go;
        cameraHandler = ch;
        addMouseMotionListener(cp);
        addKeyListener(cp);
        addMouseListener(cp);
        try { bulletModel = (JSONArray)JSONValue.parse(new FileReader("unit_sphere.json")); }
        catch(Exception e) { e.printStackTrace(); }
        try { playerModel = (JSONArray)JSONValue.parse(new FileReader("unit_cylinder.json")); }
        catch(Exception e) { e.printStackTrace(); }
    }
    public static void main(String[] args)
    {
        ModelViewer mv = new ModelViewer(WIDTH, HEIGHT);
        JFrame jf = do_main(mv);
        jf.addMouseWheelListener(mv.cameraHandler);
        jf.addKeyListener(mv.cameraHandler);
    }
}
