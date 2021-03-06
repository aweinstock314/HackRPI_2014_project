package client;

import com.jogamp.opengl.util.FPSAnimator;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.io.PrintWriter;

public abstract class AbstractGLWindow extends JPanel implements GLEventListener
{
    protected static final int WIDTH = 640;
    protected static final int HEIGHT = 480;

    abstract public void display(GLAutoDrawable drawable);
    abstract public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged);
    abstract public void init(GLAutoDrawable drawable);
    abstract public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height);
    abstract public void dispose(GLAutoDrawable drawable);

    protected GLCanvas constructorAux(int w, int h, int fps)
    {
        setLayout(null);
        GLCanvas glcanv = new GLCanvas();
        add(glcanv);
        glcanv.addGLEventListener(this);
        glcanv.setBounds(0, 0, w, h);
        //CommandPusher cp = new CommandPusher(new PrintWriter(System.out, true));
        //glcanv.addKeyListener(cp);
        //glcanv.addMouseMotionListener(cp);
        //glcanv.requestFocus();
        new FPSAnimator(glcanv, fps).start();
        return glcanv;
    }
    public static JFrame do_main(AbstractGLWindow aglw)
    {
        JFrame jf = new JFrame();
        jf.setSize(WIDTH, HEIGHT);
        jf.setResizable(false);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setLayout(null);
        jf.add(aglw);
        jf.setVisible(true);
        return jf;
    }
}
