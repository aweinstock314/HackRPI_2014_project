package client;

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;


public abstract class Abstract3DKeyListener implements KeyListener
{
    protected float move_delta = (float)0.05;
    protected float rotate_delta = (float)0.05;

    public static final int k_moveforward = KeyEvent.VK_W;
    public static final int k_movebackward = KeyEvent.VK_S;
    public static final int k_moveup = KeyEvent.VK_E;
    public static final int k_movedown = KeyEvent.VK_Q;
    public static final int k_moveleft = KeyEvent.VK_A;
    public static final int k_moveright = KeyEvent.VK_D;
    public static final int k_lookup = KeyEvent.VK_I;
    public static final int k_lookdown = KeyEvent.VK_K;
    public static final int k_lookleft = KeyEvent.VK_J;
    public static final int k_lookright = KeyEvent.VK_L;
    public static final int k_shoot = KeyEvent.VK_SPACE;

    public void keyPressed(KeyEvent e) {
        //System.out.println(e.getKeyCode());
        switch(e.getKeyCode()) {
            case k_lookleft: lookLeft(); break;
            case k_lookright: lookRight(); break;
            case k_lookdown: lookDown(); break;
            case k_lookup: lookUp(); break;

            case k_moveforward: moveForward(); break;
            case k_movebackward: moveBackward(); break;
            case k_movedown: moveDown(); break;
            case k_moveup: moveUp(); break;
            case k_moveleft: moveLeft(); break;
            case k_moveright: moveRight(); break;
            case k_shoot: shoot(); break;
            default:
        }
    }
    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    abstract void lookLeft();
    abstract void lookRight();
    abstract void lookDown();
    abstract void lookUp();

    abstract void moveForward();
    abstract void moveBackward();
    abstract void moveDown();
    abstract void moveUp();
    abstract void moveLeft();
    abstract void moveRight();

    abstract void shoot();
}
