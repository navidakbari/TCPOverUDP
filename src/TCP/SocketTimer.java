import jdk.nashorn.internal.ir.annotations.Reference;

import java.util.Timer;
import java.util.TimerTask;

public class SocketTimer extends TimerTask {
    private static final int DELAY = 150;
    private static final int PERIOD = 150;

    private Timer timer;
    private Window window;

    public SocketTimer(Window window) {
        this.window = window;
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

        }
    }
}
