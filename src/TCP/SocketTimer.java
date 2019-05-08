import jdk.nashorn.internal.ir.annotations.Reference;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class SocketTimer extends TimerTask {
    private static final int DELAY = 150;
    private static final int PERIOD = 150;

    private Timer timer;
    private Window window;
    private EnhancedDatagramSocket udp;

    public SocketTimer(Window window,EnhancedDatagramSocket udp) {
        this.window = window;
        this.udp = udp;
    }

    public void start() {
        timer = new Timer();
        timer.schedule(this, DELAY, PERIOD);
    }

    public void stop() {
        timer.cancel();
    }

    public void restart() {
        stop();
        start();
    }
    public void run()
    {
        for( int i = 0 ; i < window.nextSeqNum - window.base - 1; i++)
        {
            try {
                this.udp.send(window.packets[i].getUDPPacket());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
