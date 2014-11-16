package client;
import javax.swing.*;
import java.awt.*;

public class UIPanel extends JPanel {

    public UIPanel(CommandPusher cp) {
        addKeyListener(cp);
        addMouseMotionListener(cp);
        addMouseListener(cp);
    }
}
