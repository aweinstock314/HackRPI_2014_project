package client;
import javax.swing.*;

public class UIPanel extends JPanel {

    public UIPanel(GameObject go, CommandPusher cp) {
        Drawer dr = new Drawer(go,this);
        addKeyListener(cp);
        addMouseMotionListener(cp);
    }
}
