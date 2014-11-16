package client;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.FileReader;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.JFrame;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

public class ModelViewer extends AbstractGLWindow implements MouseWheelListener
{
    public JSONArray model = null;
    float widthScale = 1;
    float heightScale = 1;

    public void setProjection(GL2 gl2, double width, double height)
    {
        gl2.glMatrixMode(gl2.GL_PROJECTION);
        gl2.glLoadIdentity();
        gl2.glOrtho(-width, width, -height, height, 1, -1);
    }

    public void display(GLAutoDrawable drawable)
    {
        GL2 gl2 = drawable.getGL().getGL2();
        setProjection(gl2, widthScale, heightScale);
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

    public void mouseWheelMoved(MouseWheelEvent ev)
    {
        float delta = (float)(.5 * ev.getWheelRotation());
        widthScale += delta;
        heightScale += delta;
        widthScale = (float)Math.max(.5, widthScale);
        heightScale = (float)Math.max(.5, heightScale);
        //System.out.printf("(%f, %f)\n", widthScale, heightScale);
    }


    public ModelViewer(int w, int h)
    {
        constructorAux(w, h, 1);
        try { model = (JSONArray)JSONValue.parse(new FileReader("unit_sphere.json")); }
        catch(Exception e) { e.printStackTrace(); }
    }
    public static void main(String[] args)
    {
        ModelViewer mv = new ModelViewer(WIDTH, HEIGHT);
        JFrame jf = do_main(mv);
        jf.addMouseWheelListener(mv);
    }
}
