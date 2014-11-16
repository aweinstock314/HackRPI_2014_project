package client;

import com.jogamp.opengl.util.FPSAnimator;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ModelViewer extends JPanel implements GLEventListener
{
    private static final int WIDTH = 640;
    private static final int HEIGHT = 480;

    public void display(GLAutoDrawable drawable){}
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged){}
    public void init(GLAutoDrawable drawable){}
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height){}
    public void dispose(GLAutoDrawable drawable) {}
    public ModelViewer(int w, int h)
    {
        setLayout(null);
        GLCanvas glcanv = new GLCanvas();
        add(glcanv);
        glcanv.setBounds(0, 0, w, h);
    }
    public static void main(String[] args)
    {
        JFrame jf = new JFrame();
        ModelViewer mv = new ModelViewer(WIDTH, HEIGHT);
        jf.setSize(WIDTH, HEIGHT);
        jf.setResizable(false);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setLayout(null);
        jf.add(mv);
        jf.setVisible(true);
    }
}
