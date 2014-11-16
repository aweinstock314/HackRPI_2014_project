package client;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

public class ModelViewer extends AbstractGLWindow
{
    public void display(GLAutoDrawable drawable)
    {
        GL2 gl2 = drawable.getGL().getGL2();
        gl2.glClear(gl2.GL_COLOR_BUFFER_BIT);
        gl2.glBegin(gl2.GL_TRIANGLES);
        gl2.glColor3f((float)Math.random(), (float)Math.random(), (float)Math.random());
        gl2.glVertex2d(0, 0);
        gl2.glVertex2d(1, 0);
        gl2.glVertex2d(0, 1);
        gl2.glEnd();
    }
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}
    public void init(GLAutoDrawable drawable) {}
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
    public void dispose(GLAutoDrawable drawable) {}

    public ModelViewer(int w, int h) { constructorAux(w, h, 1); }
    public static void main(String[] args) { do_main(new ModelViewer(WIDTH, HEIGHT)); }
}
