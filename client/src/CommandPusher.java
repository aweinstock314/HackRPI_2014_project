package client;
import java.io.*;
import java.awt.event.*;

public class CommandPusher implements KeyListener,MouseMotionListener {
    
    PrintWriter out;

    public CommandPusher(PrintWriter out) {
        this.out = out;
    }

    //establish protocol here
    public void sendData(String data) {
        out.print(data);
    }

    public void mouseMoved(MouseEvent e) {}
    
    public void mouseDragged(MouseEvent e) {}
    
    public void keyTyped(KeyEvent e) {}

    public void keyPressed(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {}
}
