package client;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedHashSet;
import java.util.Set;

public class KeyListenerSmoother implements KeyListener
{
    private static class DummyComponent extends Component {}
    private static Component DUMMY_COMPONENT = new DummyComponent();
    private Set<KeyListener> listeners = new LinkedHashSet<KeyListener>();
    private Set<Integer> heldKeys = new LinkedHashSet<Integer>();
    public void addKeyListener(KeyListener l) { listeners.add(l); }
    public void removeKeyListener(KeyListener l) { listeners.remove(l); }

    @Override
    public synchronized void keyPressed(KeyEvent e) { heldKeys.add(e.getKeyCode()); }
    @Override
    public synchronized void keyReleased(KeyEvent e) { heldKeys.remove(e.getKeyCode()); }
    @Override
    public void keyTyped(KeyEvent e) {}

    public KeyListenerSmoother(final long retransmitDelay)
    {
        new Thread(new Runnable() { @Override public void run() {
            retransmitLoop(retransmitDelay);
        }}).start();
    }

    private void retransmitLoop(final long millis)
    {
        // manual macroexpansion of with-min-ms-per-iteration
        long starttime = 0; long endtime = 0; long delta = 0;
        while(true)
        {
            starttime = System.currentTimeMillis();
            retransmitLoopBody();
            endtime = System.currentTimeMillis();
            delta = (long)Math.max(0, millis - (endtime - starttime));
            if(delta > 0) { try { Thread.sleep(delta); } catch(InterruptedException ie) {} }
        }
    }

    private synchronized void retransmitLoopBody()
    {
        for(Integer key : heldKeys)
        {
            KeyEvent e = new KeyEvent(DUMMY_COMPONENT, KeyEvent.KEY_PRESSED,
                                        System.currentTimeMillis(), 0,
                                        key, KeyEvent.CHAR_UNDEFINED);
            for(KeyListener l : listeners)
            {
                l.keyPressed(e);
            }
        }
    }
}
