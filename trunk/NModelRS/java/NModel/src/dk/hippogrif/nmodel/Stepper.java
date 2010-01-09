/*
   Copyright (C) 2010 Jesper Goertz
   http://hippogrif.dk/sw/nmodel

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
   http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package dk.hippogrif.nmodel;

/**
 * Used  by test harness to read test actions and write replies.
 * <br>
 * Atomic actions must be acknowledged with a null reply.
 * A Start action (with suffix "_Start") must be followed by a Finish action
 * (with suffix "_Finish").
 * Stepper handles the suffixes in read and reply methods,
 * so only the proper action name should be used by the test harness.
 *
 * @author Jesper Goertz
 */
public abstract class Stepper {

    boolean needsReply;
    String startAction;

    abstract String readLine();

    abstract void writeLine(String line);

    Stepper() {
    }

    /**
     * Read action with autoreply.
     *
     * @return action
     */
    public Action readAction() {
        return readAction(true);
    }

    /**
     * Read action.
     *
     * @param autoReply true iff null reply for atomic actions
     * should be sent automatically.
     * @return action
     */
    public Action readAction(boolean autoReply) {
        if (needsReply) {
            throw new NModelException(NModelException.Reason.INPUT, "reply expected");
        }
        needsReply = true;
        String line = readLine();
        Action action = Action.parse(line);
        if (!isStartAction(action.getFirstName()) && autoReply) {
            replyLine("");
        }
        return action;
    }

    /**
     * Read named action with autoreply.
     *
     * @param name of expected action
     * @return action
     * @throws NModelException with Reason.INPUT if unexpected action read
     */
    public Action readAction(String name) {
        return readAction(name, true);
    }

    /**
     * Read named action.
     *
     * @param name of expected action
     * @param autoReply true iff null reply for atomic actions
     * should be sent automatically.
     * @return action
     * @throws NModelException with Reason.INPUT if unexpected action read
     */
    public Action readAction(String name, boolean autoReply) {
        Action action = readAction(autoReply);
        if (!action.getFirstName().equals(startAction==null?name:name+"_Start")) {
            throw new NModelException(NModelException.Reason.INPUT,
                    "expected: " + name + "\nreceived: " + action);
        }
        return action;
    }

    /**
     * Read required action incl parameters with autoreply.
     *
     * @param line contains serialized action
     * @throws NModelException with Reason.INPUT if unexpected action read
     */
    public void requireAction(String line) {
        requireAction(line, true);
    }

    /**
     * Read required action incl parameters.
     *
     * @param line contains serialized action
     * @param autoReply true iff null reply for atomic actions
     * should be sent automatically.
     * @throws NModelException with Reason.INPUT if unexpected action read
     */
    public void requireAction(String line, boolean autoReply) {
        requireAction(Action.parse(line), autoReply);
    }

    /**
     * Read required action incl parameters with autoreply.
     *
     * @param action required
     * @throws NModelException with Reason.INPUT if unexpected action read
     */
    public void requireAction(Action action) {
        requireAction(action, true);
    }

    /**
     * Read required action incl parameters.
     *
     * @param action required
     * @param autoReply true iff null reply for atomic actions
     * should be sent automatically.
     * @throws NModelException with Reason.INPUT if unexpected action read
     */
    public void requireAction(Action action, boolean autoReply) {
        String line = action.toString();
        Action a = readAction(autoReply);
        String s = a.toString();
        if (!line.equals(s)) {
            throw new NModelException(NModelException.Reason.INPUT,
                    "expected: " + line + "\nreceived: " + s);
        }
    }

    /**
     * Check whether Start action has been read and now
     * a reply is expected.
     *
     * @return true iff reply needs to be sent for Start action.
     */
    public boolean isStartAction() {
        return startAction != null;
    }

    boolean isStartAction(String name) {
        if (startAction != null) {
            throw new NModelException(NModelException.Reason.OUTPUT, "reply expected");
        }
        if (name.endsWith("_Start")) {
            startAction = name.substring(0, name.length()-6);
        }
        return startAction != null;
    }

    void replyLine(String line) {
        if (!needsReply) {
            throw new NModelException(NModelException.Reason.OUTPUT, "unexpected reply: "+line);
        }
        needsReply = false;
        writeLine(line);
    }

    /**
     * Make a null reply to an atomic action.
     *
     * @throws NModelException with Reason.OUTPUT if reply to a Start action is expected or reply is not expected
     */
    public void reply() {
        if (startAction != null) {
            throw new NModelException(NModelException.Reason.OUTPUT, "unexpected null reply");
        }
        replyLine("");
    }
    
    /**
     * Reply to a Start action.
     *
     * @param value contains serialized return value of the action
     * @throws NModelException with Reason.OUTPUT if reply not expected
     */
    public void reply(String value) {
        if (startAction == null) {
            throw new NModelException(NModelException.Reason.OUTPUT, "unexpected reply: " + value);
        }
        String s = startAction + "_Finish(" + value + ")";
        startAction = null;
        replyLine(s);
    }

    /**
     * Reply with exception.
     *
     * @param e is exception
     * @throws NModelException with Reason.OUTPUT if reply not expected
     */
    public void exception(Exception e) {
        exception(e.getMessage());
    }

    /**
     * Reply with exception.
     *
     * @param msg of exception
     * @throws NModelException with Reason.OUTPUT if reply not expected
     */
    public void exception(String msg) {
        replyLine("Exception: " + msg);
    }
}
