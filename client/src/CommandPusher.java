package client;
import java.io.*;
import java.awt.event.*;
import java.util.*;
import org.json.simple.*;
import java.awt.*;

public class CommandPusher extends java.util.TimerTask implements KeyListener,MouseMotionListener,MouseListener {

    public float velocity = .1f;
    
    private int cur_mouse_x;
    private int cur_mouse_y;
    
    private Robot rob;
    private boolean working_robot;
    private boolean robot_reset = true;

    PrintWriter out;

    private HashSet<Integer> pressedKeys;

    public CommandPusher(PrintWriter out) {
        this.out = out;
        try {
            rob = new Robot();
            working_robot = true;
        } catch (AWTException e) {
            working_robot = false;
        }
        
        pressedKeys = new HashSet<Integer>();
        Timer tim = new Timer();
        tim.schedule(this,0,20);
    }

    //send a shoot command
    public void shoot() {
        sendSignal("\"Shoot\"");
    }

    public void jump() {
        //sendSignal("Jump");
    }

    //send current pressed keys
    public void run() {
       if(pressedKeys.contains(KeyEvent.VK_W) && !pressedKeys.contains(KeyEvent.VK_S)) 
       {
           sendData("MoveForward",velocity);
       }
       if(pressedKeys.contains(KeyEvent.VK_S) && !pressedKeys.contains(KeyEvent.VK_W)) 
       {
           sendData("MoveForward",-velocity);
       }
       if(pressedKeys.contains(KeyEvent.VK_A) && !pressedKeys.contains(KeyEvent.VK_D)) 
       {
           sendData("MoveSideways",-velocity);
       }
       if(pressedKeys.contains(KeyEvent.VK_D) && !pressedKeys.contains(KeyEvent.VK_A)) 
       {
           sendData("MoveSideways",velocity);
       }
    }

    //establish protocol here
    public void sendData(String data, float value) {
        long timestamp = System.currentTimeMillis();
        JSONObject obj = new JSONObject();
        JSONObject cmd = new JSONObject();
        cmd.put("variant",data);
        JSONArray valueArray = new JSONArray();
        valueArray.add(value);
        cmd.put("fields",valueArray);
        obj.put("command",cmd);
        obj.put("timestamp",timestamp);
        out.println(cmd.toString());
    }

    public void sendSignal(String signalType) {
        System.out.println(signalType);
        out.println();
        out.println(signalType);
    }

    //fix sending of orientation...
    public void sendOrientation(float delth, float delph) {
        long timestamp = System.currentTimeMillis();
        JSONObject obj = new JSONObject();
        JSONObject cmd = new JSONObject();
        cmd.put("variant", "RotateCamera");
        JSONArray valueArray = new JSONArray();
        JSONObject valueObj = new JSONObject();
        valueObj.put("_field0",delth);
        valueObj.put("_field1",delph);
        valueArray.add(valueObj);
        cmd.put("fields",valueArray);
        obj.put("command",cmd);
        obj.put("timestamp",timestamp);
        out.println(cmd.toString());
    }

    public void mouseMoved(MouseEvent e) {
        if(!robot_reset) {
            sendOrientation(e.getX()-cur_mouse_x,e.getY()-cur_mouse_y);
            cur_mouse_x = e.getX();
            cur_mouse_y = e.getY();
        } else {
            robot_reset = false;
        }
    }
    
    public void mouseDragged(MouseEvent e) {}
    
    public void keyTyped(KeyEvent e) { }

    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_SPACE) {
            jump();
        } else {
            pressedKeys.add(e.getKeyCode());
        }
    }

    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
    }

    public void mouseClicked(MouseEvent e) { 
        shoot();
    }

    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {
        if(working_robot) {
            robot_reset = true;
            rob.mouseMove(500,500); //FIXME
            Point mousePoint = MouseInfo.getPointerInfo().getLocation();
            cur_mouse_x = mousePoint.x;
            cur_mouse_y = mousePoint.y;
        }
    }
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
}
