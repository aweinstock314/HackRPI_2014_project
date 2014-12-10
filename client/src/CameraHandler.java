package client;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.media.opengl.GL2;

public class CameraHandler extends Abstract3DKeyListener implements MouseWheelListener
{
    float widthScale = 1;
    float heightScale = 1;
    public float x, y, z;
    public float theta, phi;
    public void apply(GL2 gl2)
    {
        gl2.glRotatef(rad2deg(phi), -1, 0, 0);
        gl2.glRotatef(rad2deg(theta), 0, -1, 0);
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

    void lookLeft() { theta += rotate_delta; }
    void lookRight() { theta -= rotate_delta; }
    void lookDown() { phi -= rotate_delta; }
    void lookUp() { phi += rotate_delta; }
    void moveForward() { do_polar_movement(move_delta, .25); }
    void moveBackward() { do_polar_movement(move_delta, .75); }
    void moveDown() { y -= move_delta; }
    void moveUp() { y += move_delta; }
    void moveLeft() { do_polar_movement(move_delta, .5); }
    void moveRight() { do_polar_movement(move_delta, 0.0); }

    void shoot() {}

    //System.out.printf("Current position: (%f, %f, %f)\n", x, y, z);
    //System.out.printf("Current orientation: (%f, %f)\n", rad2deg(theta), rad2deg(phi));

    float rad2deg(float x) { return (float)((x * 180)/Math.PI); }
}
