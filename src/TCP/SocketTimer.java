import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class SocketTimer {
    private static final int DELAY = 10;
    private static final int PERIOD = 100;

    public static int startNum = 0;
    public static Timer timer;
    private Window window;
    private EnhancedDatagramSocket udp;

    public SocketTimer(Window window,EnhancedDatagramSocket udp) {
        this.window = window;
        this.udp = udp;
    }

    public void start() {
        SocketTimer.timer = new Timer();
        SocketTimer.timer.schedule(new SocketTimerTask(window, udp), DELAY, PERIOD);
        startNum ++;
    }

    public void stop() {
        if(timer != null) {
            System.out.println(startNum);
            timer.cancel();
            startNum --;
            timer.purge();
            timer = null;
        }
    }

    public void finish() {
        this.stop();
    }


    public void restart() {
        System.out.println("timer restart");
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
        window.sshtresh = window.cwnd/2;
        window.cwnd = 1;
        window.dupAckCount = 0;
        window.congestionState = Window.congestionStates.SLOW_START;
//        for( int i = window.base ; i < window.nextSeqNum ; i++)
//        {
            try {
                this.udp.send(window.packets.get(window.base).getUDPPacket());
                System.out.println("timeout packet send : " + (window.base));
            } catch (IOException e) {
                e.printStackTrace();
            }
//        }
    }
}
