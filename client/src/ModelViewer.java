package client;

import java.io.FileReader;
import java.net.Socket;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

public class ModelViewer extends AbstractGLWindow
{
    public JSONArray model = null;
    public CameraHandler cameraHandler = new CameraHandler();
    public SecondAttemptAtInput saai = null;

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
        gl2.glBegin(gl2.GL_TRIANGLES);
        if(model != null) for(int i=0; i<(model.size()/3); i+=3)
        {
            gl2.glColor3f((float)Math.random(), (float)Math.random(), (float)Math.random());
            gl2.glVertex3d(((Number)model.get(i)).floatValue(), ((Number)model.get(i+1)).floatValue(), ((Number)model.get(i+2)).floatValue());
        }
        gl2.glEnd();
    }
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}
    public void init(GLAutoDrawable drawable) {}
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
    public void dispose(GLAutoDrawable drawable) {}

    public ModelViewer(int w, int h)
    {
        try { saai = new SecondAttemptAtInput(new Socket("localhost", 51701).getOutputStream()); }
        catch(Exception e) { e.printStackTrace(); }
        GLCanvas glcanv = constructorAux(w, h, 5);
        glcanv.addKeyListener(saai);
        glcanv.requestFocus();
        try { model = (JSONArray)JSONValue.parse(new FileReader("unit_sphere.json")); }
        catch(Exception e) { e.printStackTrace(); }
    }
    public static void main(String[] args)
    {
        ModelViewer mv = new ModelViewer(WIDTH, HEIGHT);
        JFrame jf = do_main(mv);
        jf.addMouseWheelListener(mv.cameraHandler);
        jf.addKeyListener(mv.cameraHandler);
        jf.addKeyListener(mv.saai);
    }
}
