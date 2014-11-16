package client;

import com.jogamp.opengl.util.FPSAnimator;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

public class ModelViewer extends AbstractGLWindow
{
    public void display(GLAutoDrawable drawable) {}
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}
    public void init(GLAutoDrawable drawable) {}
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
    public void dispose(GLAutoDrawable drawable) {}

    public ModelViewer(int w, int h) { constructorAux(w, h); }
    public static void main(String[] args) { do_main(new ModelViewer(WIDTH, HEIGHT)); }
}
