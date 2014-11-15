package client;
import javax.swing.*;
import java.awt.*;

public class Drawer extends java.util.TimerTask {

    GameObject go;
    JPanel panel;

    public Drawer(GameObject go, JPanel panel) {
        this.go = go;
        this.panel = panel;
    }

    //Draw current game state using OpenGL
    public void run() {
    }
}
