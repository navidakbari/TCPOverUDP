import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class SocketTimer {
    private static final int DELAY = 2000;
    private static final int PERIOD = 2000;

    private Timer timer;
    private Window window;
    private EnhancedDatagramSocket udp;

    public SocketTimer(Window window,EnhancedDatagramSocket udp) {
        this.window = window;
        this.udp = udp;
    }

    public void start() {
        timer = new Timer();
        timer.schedule(new SocketTimerTask(window, udp), DELAY, PERIOD);
    }

    public void stop() {
        timer.cancel();
    }

    public void restart() {
        stop();
        start();
    }

}
class SocketTimerTask extends TimerTask{
    private Window window;
    private EnhancedDatagramSocket udp;
    SocketTimerTask(Window win, EnhancedDatagramSocket udp)
    {
        this.window = win;
        this.udp = udp;
    }
    public void run()
    {
        for( int i = 0 ; i < window.nextSeqNum - window.base - 1; i++)
        {
            try {
                this.udp.send(window.packets[i].getUDPPacket());
                System.out.println("timeout packet send : " + (window.base + i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
