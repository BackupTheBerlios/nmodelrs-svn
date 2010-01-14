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

package dk.hippogrif.nmodel.stepper;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Jesper Goertz
 */
public class NModelException extends RuntimeException {

    public enum Reason {
        EOF("*** END OF FILE ***"),
        INPUT("*** UNEXPECTED INPUT ***"),
        OUTPUT("*** UNEXPECTED OUTPUT ***"),
        RESET("*** RESET NOT SUPPORTED ***"),
        PARSER("*** ERROR PARSING ACTION ***"),
        NOTIMPL("*** NOT IMPLEMENTED ***");

        public final String msg;

        Reason(String msg) {
            this.msg = msg;
        }
    }

    public final Reason reason;

    /**
     * Constructs an instance of <code>NModelException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public NModelException(Reason reason, String msg) {
        super(message(reason, msg));
        this.reason = reason;
    }

    public NModelException(Reason reason) {
        this(reason, null);
    }

    static String message(Reason reason, String msg) {
        if (msg != null) {
            return reason.msg + "\n" + msg;
        }
        return reason.msg;
    }

    public String stackTrace() {
        StringWriter sw = new StringWriter();
        printStackTrace(new PrintWriter(sw));
        return sw.getBuffer().toString();
    }
}
