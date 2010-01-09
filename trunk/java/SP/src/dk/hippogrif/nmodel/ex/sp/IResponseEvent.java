package dk.hippogrif.nmodel.ex.sp;

/**
 * NModel test harness for mock SP example in NModel book chapter 16.3.
 *
 * @author Jesper Goertz
 */
public interface IResponseEvent {

    void respond(String cmd, int id, int credits, boolean completed);

}
