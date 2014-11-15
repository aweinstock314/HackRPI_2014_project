package client;
import javax.swing.*;
import java.awt.*;
import java.util.*;

public class GameInstantiator {

    public static void main(String[] args) {
        //instantiate game object later

        ThreadClient tc = new ThreadClient("128.113.222.153",51701);
        tc.getWriter().print("{}");

        GameObject go = new GameObject();
        CommandPusher cp = new CommandPusher(tc.getWriter());
        JFrame jf = new JFrame();
        UIPanel panel = new UIPanel(go,cp);
        jf.setContentPane(panel);
        jf.setVisible(true);
        Drawer dr = new Drawer(go,panel);
        java.util.Timer timer = new java.util.Timer(true);
        timer.scheduleAtFixedRate(dr,50,25);
    }
}

