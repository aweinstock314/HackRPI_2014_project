package client;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.media.opengl.GL2;

public class CameraHandler implements MouseWheelListener, KeyListener
{
    float move_delta = (float)0.1;
    float rotate_delta = (float)0.1;

    float widthScale = 1;
    float heightScale = 1;
    public static final int moveforward = KeyEvent.VK_W;
    public static final int movebackward = KeyEvent.VK_S;
    public static final int moveup = KeyEvent.VK_E;
    public static final int movedown = KeyEvent.VK_Q;
    public static final int moveleft = KeyEvent.VK_A;
    public static final int moveright = KeyEvent.VK_D;
    public static final int lookup = KeyEvent.VK_I;
    public static final int lookdown = KeyEvent.VK_K;
    public static final int lookleft = KeyEvent.VK_J;
    public static final int lookright = KeyEvent.VK_L;
    public float x, y, z;
    public float theta, phi;
    public void apply(GL2 gl2)
    {
        gl2.glRotatef(rad2deg(phi), -1, 0, 0);
        gl2.glRotatef(rad2deg(theta), 0, 1, 0);
        gl2.glTranslatef(-x, -y, z);
    }
    public CameraHandler()
    {
        x = y = z = theta = phi = 0;
    }

    public void mouseWheelMoved(MouseWheelEvent ev)
    {
        float delta = (float)(.5 * ev.getWheelRotation());
        widthScale += delta;
        heightScale += delta;
        widthScale = (float)Math.max(.5, widthScale);
        heightScale = (float)Math.max(.5, heightScale);
        //System.out.printf("(%f, %f)\n", widthScale, heightScale);
    }

    public void do_polar_movement(float m, double t)
    {
        float rot_theta = theta + (float)(2*Math.PI * t);
        x += m*Math.cos(rot_theta);
        z += m*Math.sin(rot_theta);
    }

    public void keyPressed(KeyEvent e) {
        //System.out.println(e.getKeyCode());
        switch(e.getKeyCode()) {
            case lookleft: theta -= rotate_delta; break;
            case lookright: theta += rotate_delta; break;
            case lookdown: phi -= rotate_delta; break;
            case lookup: phi += rotate_delta; break;

            case moveforward: do_polar_movement(move_delta, .25); break;
            case movebackward: do_polar_movement(move_delta, .75); break;
            case movedown: y -= move_delta; break;
            case moveup: y += move_delta; break;
            case moveleft: do_polar_movement(move_delta, .5); break;
            case moveright: do_polar_movement(move_delta, 0.0); break;
            default:
        }
    }

    float rad2deg(float x) { return (float)((x * 180)/Math.PI); }

    public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {} 
}
