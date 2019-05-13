import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class SocketTimer {
    private static final int DELAY = 50;
    private static final int PERIOD = 100;

    private int startNum = 0;
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
        startNum ++;
    }

    public void stop() {
        timer.cancel();
        timer.purge();
    }

    public void finish() {
        this.stop();
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
        window.sshtresh = window.cwnd/2;
        window.cwnd = 1;
        window.dupAckCount = 0;
        window.congestionState = Window.congestionStates.SLOW_START;
        for( int i = window.base ; i < window.nextSeqNum ; i++)
        {
            try {
                this.udp.send(window.packets.get(i).getUDPPacket());
                System.out.println("timeout packet send : " + (i));
                System.out.println("timeout packet : " + (window.packets.get(i).getUDPPacket().getAddress()));
                System.out.println("timeout packet : " + (window.packets.get(i).getUDPPacket().getPort()));
                System.out.println("timeout packet: " + window.packets.get(i).getSynFlag() + " " + window.packets.get(i).getAckFlag() );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
