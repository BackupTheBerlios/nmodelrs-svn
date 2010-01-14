package dk.hippogrif.nmodel.ex.sp;

import dk.hippogrif.nmodel.stepper.Action;
import dk.hippogrif.nmodel.stepper.NModelException;
import dk.hippogrif.nmodel.stepper.Observer;
import dk.hippogrif.nmodel.stepper.RemoteStepper;
import java.util.logging.Logger;

/**
 * NModel test harness for mock SP example in NModel book chapter 16.3.
 *
 * @author Jesper Goertz
 */
public class Stepper implements IResponseEvent {
    static Logger logger = Logger.getLogger("dk.hippogrif.nmodel.ex.sp");
    RemoteStepper rs;
    Observer obs;

    public Stepper(RemoteStepper rs, Observer obs) {
        this.rs = rs;
        this.obs = obs;
    }

    public void run() {
        while (true) {
            try {
                Action action = rs.readAction(false);
                if (action.getName().equals("ReqSetup")) {
                    Server.Request("Setup", action.getParam(1).getIntValue(), action.getParam(2).getIntValue());
                }
                else
                if (action.getName().equals("ReqWork")) {
                    Server.Request("Work", action.getParam(1).getIntValue(), action.getParam(2).getIntValue());
                }
                else {
                    rs.exception("Unknown action: "+action.getName());
                }
            }
            catch(NModelException e) {
                boolean busy = Server.IsBusy();
                if (e.reason != NModelException.Reason.RESET || busy) {
                    System.err.println("Server is busy: "+busy);
                    System.err.println(e);
                    System.exit(1);
                }
            }
            rs.reply();
        }
    }

    public void respond(String cmd, int id, int credits, boolean completed) {
        String action = "Res"+cmd+"("+id+","+credits+",Status(\""+(completed?"Completed":"Cancelled")+"\"))";
        obs.send(action);
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        int asyncPort = Integer.parseInt(args[2]);
        RemoteStepper rs = RemoteStepper.newInstance(port);
        Observer obs = Observer.newInstance(args[1], asyncPort);
        Stepper stepper = new Stepper(rs, obs);
        Server.responseEvent = stepper;
        stepper.run();
    }
}
