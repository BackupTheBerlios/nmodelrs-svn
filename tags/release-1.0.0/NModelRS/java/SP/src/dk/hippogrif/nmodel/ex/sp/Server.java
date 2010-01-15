package dk.hippogrif.nmodel.ex.sp;

import java.util.logging.Logger;

/**
 * NModel test harness for mock SP example in NModel book chapter 16.3.
 *
 * @author Jesper Goertz
 */
public class Server implements Runnable {
    static Logger logger = Logger.getLogger("dk.hippogrif.nmodel.ex.sp");
    static int activeRequests = 0;
    static public boolean IsBusy() { return activeRequests > 0; }
    static IResponseEvent responseEvent;

    String cmd;
    int id;
    int credits;

    public static void Request(String cmd, int id, int credits) {
        activeRequests++;
        logger.fine("activeRequests "+activeRequests);
        Thread thread = new Thread(new Server(cmd, id, credits));
        thread.start();
    }

    public Server(String cmd, int id, int credits) {
        this.cmd = cmd;
        this.id = id;
        this.credits = credits;
    }

    public void run() {
        try {
            Thread.sleep((long) (nextRandom(100)));
            if (responseEvent != null) {
                responseEvent.respond(cmd, id, nextRandom(credits), true);
            }
            activeRequests--;
            logger.fine("activeRequests "+activeRequests);
        }        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    static int nextRandom(int max) {
        return (int)(Math.random()*max);
    }
}