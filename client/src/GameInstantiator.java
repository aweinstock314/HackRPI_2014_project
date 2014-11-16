package client;
import javax.swing.*;
import java.awt.*;
import java.util.*;

public class GameInstantiator {

    public static void main(String[] args) {
        //instantiate game object later

        ThreadClient tc = new ThreadClient("129.161.91.206",51701);
        tc.getWriter().print("{}");

        GameObject go = new GameObject();
        CommandPusher cp = new CommandPusher(tc.getWriter());
        JFrame jf = new JFrame();
        jf.setSize(500,500);
        UIPanel panel = new UIPanel(cp);
        jf.setContentPane(panel);
        jf.pack();
        jf.setVisible(true);
        Drawer dr = new Drawer(go,panel);
        java.util.Timer timer = new java.util.Timer(true);
        timer.scheduleAtFixedRate(dr,50,25);

        ServerSyncer ss = new ServerSyncer(tc.getReader(),go);
        timer.schedule(ss,0);
    }
}

