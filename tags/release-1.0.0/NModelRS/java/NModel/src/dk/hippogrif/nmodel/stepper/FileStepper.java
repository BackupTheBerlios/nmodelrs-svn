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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

/**
 * Reads testsuite from file.
 * Stepper replies are checked against "_Finish" actions in file.
 *
 * @author Jesper Goertz
 */
public class FileStepper extends Stepper implements Runnable {
    static Logger logger = Logger.getLogger("dk.hippogrif.nmodel");
    BufferedReader in;
    boolean testCase;
    boolean firstTestCase = true;
    String filename;
    int timeout;
    Thread timeoutThread;

    /**
     * Returns an open file stepper ready for use.
     *
     * @param filename of file holding the testsuie
     * @param msTimeout max time in ms between reads
     * @return file stepper
     */
    public static FileStepper newInstance(String filename, int msTimeout) {
        FileStepper fileStepper = new FileStepper(filename, msTimeout);
        fileStepper.open();
        return fileStepper;
    }

    FileStepper(String filename, int msTimeout) {
        this.filename = filename;
        timeout = msTimeout;
    }

    void open() {
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
        logger.config("using TestSuite in "+filename);
        logger.config("using timeout value in ms of "+timeout);
        if (timeout > 0) {
            timeoutThread = new Thread(null, this, "test-timeout");
            timeoutThread.start();
        }
    }

    /**
     * Close the file and stop the timout thread.
     */
    public void close() {
        try {
            in.close();
            if (timeoutThread != null) {
                timeoutThread.interrupt();
            }
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void run() {
        while (true) {
            try {
                Thread.sleep(timeout);
                logger.severe("*** TIMEOUT ***");
                System.exit(1);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * throws ResetException on start of TestCase except the first
     * throws EOFException on end of testsuite
     * @return
     */
    synchronized String readLine() {
        if (timeout > 0) {
            timeoutThread.interrupt();
        }
        String line = read();
        logger.fine(line);
        return line;
    }

    String read() {
        try {
            String line = in.readLine();
            if (line == null) {
                throw new NModelException(NModelException.Reason.EOF);
            }
            if (line.startsWith("#")) {
                line = read();
            }
            line = line.trim();
            if (line.isEmpty()) {
                line = read();
            }
            if (line.endsWith(",")) {
                line = line.substring(0, line.length()-1);
            }
            if ("TestSuite(".equals(line)) {
                logger.info("*** TestSuite Start ***");
                line = read();
            }
            else if ("TestCase(".equals(line)) {
                logger.info("*** TestCase Start ***");
                testCase = true;
                if (!firstTestCase) {
                    throw new NModelException(NModelException.Reason.RESET);
                }
                line = read();
            }
            else if (")".equals(line)) {
                if (testCase) {
                    testCase = false;
                    if (firstTestCase) {
                        firstTestCase = false;
                    }
                    logger.fine("*** TestCase End ***");
                }
                else {
                    logger.info("*** TestSuite End ***");
                    throw new NModelException(NModelException.Reason.EOF);
                }
                line = read();
            }
            return line;
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    void writeLine(String line) {
        if (line.length() > 0) {
            requireAction(line);
        }
    }
}
