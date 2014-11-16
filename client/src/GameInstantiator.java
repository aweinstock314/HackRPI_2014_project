package client;
import javax.swing.*;
import java.awt.*;
import java.util.*;

public class GameInstantiator {

    public static void main(String[] args) {
        //instantiate game object later

        ThreadClient tc = new ThreadClient("localhost"/*"129.161.91.206"*/,51701);
        tc.getWriter().print("{}");

        GameObject go = new GameObject();
        CommandPusher cp = new CommandPusher(tc.getWriter());
        JFrame jf = new JFrame();
        ClientPanel panel = new ClientPanel(500,500, go, cp);
        jf.setContentPane(panel);
        jf.pack();
        jf.setVisible(true);
        java.util.Timer timer = new java.util.Timer(true);

        ServerSyncer ss = new ServerSyncer(tc.getReader(),go);
        timer.schedule(ss,0);
    }
}

