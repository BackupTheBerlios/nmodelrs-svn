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

package atm;

import dk.hippogrif.nmodel.stepper.NModelException;
import java.net.InetAddress;
import java.util.logging.Logger;

/**
 * NModel test harness for ATMExample.
 *
 * @author Jesper Goertz
 */
public class NewATM extends ATM {
    static Logger logger = Logger.getLogger("dk.hippogrif.nmodel.atm");

    public NewATM(int id, String place, String bankName, InetAddress bankAddress) {
        super(id, place, bankName, bankAddress);
    }

    @Override
    public void run() {
        try {
            super.run();
        } catch (NModelException e) {
            if (e.reason == NModelException.Reason.EOF) {
                logger.warning("*** EOF TestSuite ***");
                System.exit(0);
            }
            logger.severe(e.stackTrace());
            System.exit(1);
        }
    }

}
