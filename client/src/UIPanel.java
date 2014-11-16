package client;
import javax.swing.*;
import java.awt.*;

public class UIPanel extends JPanel {

    public UIPanel(GameObject go, CommandPusher cp) {
        setPreferredSize(new Dimension(500,500));
        Drawer dr = new Drawer(go,this);
        addKeyListener(cp);
        addMouseMotionListener(cp);
        addMouseListener(cp);
    }
}
