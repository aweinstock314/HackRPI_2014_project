package client;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.media.opengl.GL2;

public class CameraHandler implements MouseWheelListener, KeyListener
{
    float move_delta = (float)1.0;
    float rotate_delta = (float)0.1;

    float widthScale = 1;
    float heightScale = 1;
    public int moveforward = KeyEvent.VK_W;
    public int movebackward = KeyEvent.VK_S;
    public int moveleft = KeyEvent.VK_A;
    public int moveright = KeyEvent.VK_D;
    public int lookup = KeyEvent.VK_I;
    public int lookdown = KeyEvent.VK_K;
    public int lookleft = KeyEvent.VK_J;
    public int lookright = KeyEvent.VK_L;
    float x, y, z;
    float theta, phi;
    public void apply(GL2 gl2)
    {
        gl2.glRotatef(rad2deg(phi), 1, 0, 0);
        gl2.glRotatef(rad2deg(theta), 0, 1, 0);
        gl2.glTranslatef(-x, -y, -z);
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

    public void keyPressed(KeyEvent e) {
        //System.out.println(e.getKeyCode());
        switch(e.getKeyCode()) {
            case KeyEvent.VK_Q: theta -= rotate_delta; break;
            case KeyEvent.VK_W: theta += rotate_delta; break;
            case KeyEvent.VK_A: phi -= rotate_delta; break;
            case KeyEvent.VK_S: phi += rotate_delta; break;

            case KeyEvent.VK_E: x -= move_delta; break;
            case KeyEvent.VK_R: x += move_delta; break;
            case KeyEvent.VK_D: y -= move_delta; break;
            case KeyEvent.VK_F: y += move_delta; break;
            case KeyEvent.VK_C: z -= move_delta; break;
            case KeyEvent.VK_V: z += move_delta; break;
            default:
        }
    }

    float rad2deg(float x) { return (float)((x * 180)/Math.PI); }

    public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {} 
}
