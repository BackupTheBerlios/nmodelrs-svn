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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Logger;

/**
 * Sends serialized actions as datagrams to asynchronous stepper.
 * Action strings are transmitted in UTF-8 without nl char termination.
 *
 * @author Jesper Goertz
 */
public class Observer {
    static Logger logger = Logger.getLogger("dk.hippogrif.nmodel");

    String host;
    int port;
    InetAddress address;
    DatagramSocket socket;

    /**
     * Make new instance of Observer bound to an asynchronous stepper at host:port.
     * 
     * @param host name
     * @param port
     * @return observer
     */
    public static Observer newInstance(String host, int port) {
        Observer observer = new Observer(host, port);
        observer.open();
        return observer;
    }

    Observer(String host, int port) {
        logger.config("datagrams sent to "+host+":"+port);
        this.host = host;
        this.port = port;
    }

    void open() {
        try {
            address = InetAddress.getByName(host);
            socket = new DatagramSocket();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        socket.close();
    }

    /**
     * Send serialized action to asynchronous stepper.
     *
     * @param action string
     */
    public void send(String action) {
        try {
            logger.fine(action);
            byte[] buf = action.getBytes("UTF-8");
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
            socket.send(packet);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
