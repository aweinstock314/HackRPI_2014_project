package client;
import javax.swing.*;
import java.awt.*;
import java.util.*;

public class GameInstantiator {

    public static void main(String[] args) {
        //instantiate game object later

        ThreadClient tc = new ThreadClient("localhost"/*"129.161.91.206"*/,51701);
        tc.getWriter().print("{}");

        GameWorld go = new GameWorld();
        CommandPusher cp = new CommandPusher(tc.getWriter());
        JFrame jf = new JFrame();
        cp.shoot();
        CameraHandler ch = new CameraHandler();
        ClientPanel panel = new ClientPanel(500,500, go, cp,ch);
        jf.setSize(500,500);
        jf.setResizable(false);
        jf.setLayout(null);
        jf.add(panel);
        jf.setVisible(true);
        java.util.Timer timer = new java.util.Timer(true);

        ServerSyncher ss = new ServerSyncher(tc.getReader(),go,ch);
        new Thread(ss).start();
    }
}

