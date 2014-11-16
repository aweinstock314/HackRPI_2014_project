package client;
import java.io.*;
import java.awt.event.*;
import java.util.*;

public class CommandPusher extends java.util.TimerTask implements KeyListener,MouseMotionListener,MouseListener {
    public int velocity;
    
    PrintWriter out;

    private HashSet<Integer> pressedKeys;

    public CommandPusher(PrintWriter out) {
        this.out = out;
        pressedKeys = new HashSet<Integer>();
    }

    //send a shoot command
    public void shoot() {
        sendSignal("Shoot");
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
    public void sendData(String data, double value) {
        out.print(data);
    }

    public void sendSignal(String signalType) {

    }

    public void mouseMoved(MouseEvent e) {
        System.out.println("Moved mouse");
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
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
}
