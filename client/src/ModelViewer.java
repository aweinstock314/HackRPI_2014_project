package client;

import org.json.simple.JSONValue;
import org.json.simple.JSONArray;
import java.io.FileReader;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

public class ModelViewer extends AbstractGLWindow
{
    public JSONArray model = null;
    public void display(GLAutoDrawable drawable)
    {
        GL2 gl2 = drawable.getGL().getGL2();
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
        constructorAux(w, h, 1);
        try { model = (JSONArray)JSONValue.parse(new FileReader("unit_sphere.json")); }
        catch(Exception e) { e.printStackTrace(); }
    }
    public static void main(String[] args) { do_main(new ModelViewer(WIDTH, HEIGHT)); }
}
