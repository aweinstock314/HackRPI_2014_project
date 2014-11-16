package client;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.OutputStream;
import java.io.PrintStream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class SecondAttemptAtInput implements KeyListener
{
    PrintStream ps = null;
    public SecondAttemptAtInput(OutputStream os)
    {
        ps = new PrintStream(os);
    }
    float move_delta = (float)0.1;
    float rotate_delta = (float)0.1;
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
    public static final int shoot = KeyEvent.VK_SPACE;

    public void keyPressed(KeyEvent e) {
        //System.out.println(e.getKeyCode());
        switch(e.getKeyCode()) {
            case lookleft: emitRotateCamera(-rotate_delta, 0); break;
            case lookright: emitRotateCamera(rotate_delta, 0); break;
            case lookdown: emitRotateCamera(0, -rotate_delta); break;
            case lookup: emitRotateCamera(0, rotate_delta); break;

            case moveforward: emitMoveForward(move_delta); break;
            case movebackward: emitMoveForward(-move_delta); break;
            case movedown: break;
            case moveup: break;
            case moveleft: emitMoveSideways(-move_delta); break;
            case moveright: emitMoveSideways(move_delta); break;
            case shoot: emitShoot(); break;
            default:
        }
    }
    public void emitMoveForward(float delta) {
        JSONObject x = new JSONObject();
        x.put("variant", "MoveForward");
        JSONArray y = new JSONArray();
        y.add(delta);
        x.put("fields", y);
        ps.println(x);
        System.out.println(x);
    }
    public void emitMoveSideways(float delta) {
        JSONObject x = new JSONObject();
        x.put("variant", "MoveSideways");
        JSONArray y = new JSONArray();
        y.add(delta);
        x.put("fields", y);
        ps.println(x);
        System.out.println(x);
    }
    public void emitShoot() {
        ps.println("\"Shoot\"");
        System.out.println("\"Shoot\"");
    }
    public void emitRotateCamera(float theta, float phi) {
        JSONObject x = new JSONObject();
        x.put("variant", "RotateCamera");
        JSONArray y = new JSONArray();
        JSONObject z = new JSONObject();
        z.put("_field0", theta);
        z.put("_field1", phi);
        y.add(z);
        x.put("fields", y);
        ps.println(x);
        System.out.println(x);
    }
    public void keyReleased(KeyEvent e) {} 
    public void keyTyped(KeyEvent e) {}
    
}
