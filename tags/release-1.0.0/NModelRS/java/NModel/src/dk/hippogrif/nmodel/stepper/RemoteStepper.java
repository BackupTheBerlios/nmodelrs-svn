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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Reads test actions and writes replies to remote stepper over TCP.
 * Action strings and possibly empty replies are transmitted in UTF-8 and terminated by nl char.
 *
 * @author Jesper Goertz
 */
public class RemoteStepper extends Stepper {
    static Logger logger = Logger.getLogger("dk.hippogrif.nmodel");

    int port;
    ServerSocket serverSocket;
    Socket socket;
    OutputStreamWriter out;
    BufferedReader in;

    /**
     * Returns new remote stepper ready for use.
     *
     * @param port where stepper will listen
     * @return remote stepper
     */
    public static RemoteStepper newInstance(int port) {
        RemoteStepper remoteStepper = new RemoteStepper(port);
        remoteStepper.open();
        return remoteStepper;
    }

    RemoteStepper(int port) {
        this.port = port;
    }

    void open() {
        try {
            logger.config("port "+port);
            serverSocket = new ServerSocket(port);
            socket = serverSocket.accept();
            out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            socket.close();
            out.close();
            in.close();
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized String readLine() {
        try {
            String line = in.readLine();
            logger.fine(line);
            if (line == null) {
                throw new NModelException(NModelException.Reason.EOF);
            }
            if ("RESET".equals(line)) {
                throw new NModelException(NModelException.Reason.RESET);
            }
            return line;
        }
        catch(IOException e) {
            throw new NModelException(NModelException.Reason.EOF, e.getMessage());
        }
    }

    synchronized void writeLine(String line) {
        try {
            logger.fine(line);
            out.write(line+'\n', 0, line.length()+1);
            out.flush();
        }
        catch(IOException e) {
            throw new NModelException(NModelException.Reason.EOF, e.getMessage());
        }
    }
}
