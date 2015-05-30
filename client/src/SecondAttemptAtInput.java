package client;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.OutputStream;
import java.io.PrintStream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class SecondAttemptAtInput extends Abstract3DKeyListener
{
    PrintStream ps = null;
    public SecondAttemptAtInput(OutputStream os)
    {
        ps = new PrintStream(os);
    }

    void lookLeft() { emitRotateCamera(-rotate_delta, 0); }
    void lookRight() { emitRotateCamera(rotate_delta, 0); }
    void lookDown() { emitRotateCamera(0, -rotate_delta); }
    void lookUp() { emitRotateCamera(0, rotate_delta); }

    void moveForward() { emitMoveForward(move_delta); }
    void moveBackward() { emitMoveForward(-move_delta); }
    void moveLeft() { emitMoveSideways(-move_delta); }
    void moveRight() { emitMoveSideways(move_delta); }
    void moveDown() { emitMoveUp(-move_delta); }
    void moveUp() { emitMoveUp(move_delta); }

    void shoot() { emitShoot(); }

    public void emitMoveForward(float delta) {
        JSONObject x = new JSONObject();
        x.put("variant", "MoveForward");
        JSONArray y = new JSONArray();
        y.add(delta);
        x.put("fields", y);
        ps.println(x);
    }
    public void emitMoveSideways(float delta) {
        JSONObject x = new JSONObject();
        x.put("variant", "MoveSideways");
        JSONArray y = new JSONArray();
        y.add(delta);
        x.put("fields", y);
        ps.println(x);
    }
    public void emitMoveUp(float delta) {
        JSONObject x = new JSONObject();
        x.put("variant", "MoveUp");
        JSONArray y = new JSONArray();
        y.add(delta);
        x.put("fields", y);
        ps.println(x);
    }
    public void emitShoot() {
        ps.println("\"Shoot\"");
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
    }
}
