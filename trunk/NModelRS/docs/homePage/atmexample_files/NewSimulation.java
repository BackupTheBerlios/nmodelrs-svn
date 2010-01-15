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

package simulation;

import atm.ATM;
import banking.Card;
import banking.Money;
import dk.hippogrif.nmodel.stepper.Action;
import dk.hippogrif.nmodel.stepper.Stepper;
import dk.hippogrif.nmodel.stepper.NModelException;
import dk.hippogrif.nmodel.stepper.Observer;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

/**
 * NModel test harness for ATMExample.
 *
 * @author Jesper Goertz
 */
public class NewSimulation extends Simulation {
    static Logger logger = Logger.getLogger("dk.hippogrif.nmodel.atm");

    private ATM atm;
    private Stepper stepper;
    private Observer observer;
    public Semaphore semaphore = new Semaphore(1);

    // 1. first display text starts with
    // 2. last display text starts with - or empty if only one text
    // 3. corresponding message
    static String[] message = new String[]{
        "Not currently available","","OFF_MSG",
        "Please insert your card","","IDLE_MSG",
        "Unable to read card","","UNREADABLE_MSG",
        "Please enter your PIN","","PIN_MSG",
        "Please choose transaction type","4) Balance","SESSION_MSG",
        "Account to withdraw from","3) Money Market","SELECT_FROM_ACCT",
        "Amount of cash to withdraw","5) $200","ENTER_AMOUNT",
        "Insufficient cash available","5) $200","INSUFFICIENT_CASH",
        "Account to transfer from","3) Money Market","SELECT_FROM_ACCT",
        "Account to transfer to","3) Money Market","SELECT_TO_ACCT",
        "Amount to transfer","","ENTER_AMOUNT",
        "Last transaction was cancelled","2) No","CANCEL_TRANS",
        "Would you like to do another transaction?","2) No","ANOTHER_TRANS",
        "PIN was incorrect","","INVALID_PIN",
        "Your card has been retained","","CARD_RETAINED",
        "Invalid account type","2) No","INVALID_FROM_ACCT",
        "Daily withdrawal limit exceeded","2) No","MAX_DAILY_WITHDRAWAL",
        "Insufficient available balance","2) No","INVALID_ACCT_BALANCE",
        "Invalid from account type","2) No","INVALID_FROM_ACCT",
        "Invalid to account type","2) No","INVALID_TO_ACCT",
        "Can't transfer money from","2) No","SAME_ACCOUNT"
    };

    public NewSimulation(ATM atm) {
        super(atm);
        this.atm = atm;
    }

    public ATM getAtm() {
        return atm;
    }

    public static void newSwitchChanged(boolean on) {
        logger.info(""+on);
        Simulation.getInstance().switchChanged(on);
    }

    @Override
    public Money getInitialCash() {
        int bills = 5;
        Action action = stepper.readAction("setCash");
        bills = action.getParam(1).getIntValue();
        logger.info("20*"+bills);
        return new Money(20 * bills);
    }

    public static void newCardInserted() {
        logger.info("");
        Simulation.getInstance().cardInserted();
    }

    int cardNo = 1;

    @Override
    public Card readCard()
    {
        logger.info(""+cardNo);
        operatorPanel.setEnabled(false);
        cardReader.animateInsertion();
        return cardNo > 0 ? new Card(cardNo) : null; // null means unreadable
    }

    @Override
    public void ejectCard() {
        stepper.readAction("ejectCard");
        super.ejectCard();
    }

    @Override
    public void retainCard() {
        stepper.readAction("retainCard");
        super.retainCard();
    }

    public static NewSimulation getInstance() {
        return (NewSimulation)Simulation.getInstance();
    }

    public void setStepper(Stepper stepper) throws IOException {
        this.stepper = stepper;
        operatorPanel = (SimOperatorPanel)getFieldObject(this, "operatorPanel");
        cardReader = (SimCardReader)getFieldObject(this, "cardReader");
    }

    public void setObserver(Observer observer) {
        this.observer = observer;
    }

    public void error(String msg, Action action) {
        throw new NModelException(NModelException.Reason.INPUT, msg + ": " + action.toString());
    }

    boolean off = true;

    public boolean getOff() {
        return off;
    }

    public void doAction() {
        Action action = stepper.readAction();
        String name = action.getName();
        if ("custInsertCard".equals(name)) {
            cardNo = action.getParam(1).getIntValue();
            newCardInserted();
        }
        else if ("switchOff".equals(name)) {
            newSwitchChanged(false);
            off = true;
        }
        else if ("switchOn".equals(name)) {
            newSwitchChanged(true);
            off = false;
        }
        else error("doAction", action);
    }

    @Override
    public void display(String text) {
        super.display(text);
        if (text.isEmpty()) return;
        logger.info(text);
        String msg = findMessage(text);
        if (msg == null || stepper == null) return;
        if (stepper.isStartAction()) {
            stepper.reply("MSG(\""+msg+"\")");
        }
        else {
            String s = "message(MSG(\""+msg+"\"))";
            if (observer != null) {
                observer.send(s);
            }
            else {
                stepper.requireAction(s);
            }
        }
        if (msg.equals(message[2]) || msg.equals(message[5])) semaphore.release();
    }

    int msgNo = -1;
    boolean msgDone = true;

    String findMessage(String text) {
        if (!msgDone) {
            if (text.startsWith(message[msgNo+1])) {
                msgDone = true;
                return message[msgNo+2];
            }
            else return null;
        }
        for (int i=0; i<message.length; i=i+3) {
            if (text.startsWith(message[i])) {
                if (message[i+1].length() == 0) {
                    return message[i+2];
                }
                msgNo = i;
                msgDone = false;
                return null;
            }
        }
        throw new RuntimeException("unknown display text: "+text);
    }

    @Override
    public String readInput(int mode, int maxValue) {
        Action action = stepper.readAction();
        String name = action.getName();
        pause(2);
        if ("custCancel".equals(name) || "custCancelTrans".equals(name)) {
            return null;
        }
        if (mode == PIN_MODE &&
                ("custEnterPin".equals(name) || "custRetryPin".equals(name))) {
            return action.getParam(1).getValue();
        }
        if (mode == MENU_MODE) {
            if ("custSelectTrans".equals(name)) {
                String s = action.getParam(1).getParam(1).getValue();
                String i = "WITHDRAWAL".equals(s) ? "1" : "TRANSFER".equals(s) ? "3" : "";
                if (i.length()>0) return i;
            }
            else if ("custSelectAcct".equals(name)) {
                String s = action.getParam(1).getParam(1).getValue();
                String i = "CHECKING".equals(s) ? "1" :
                    "SAVINGS".equals(s) ? "2" : "MONEYMARKET".equals(s) ? "3" : "";
                if (i.length()>0) return i;
            }
            else if ("custEnterAmountW".equals(name)) {
                String s = action.getParam(1).getValue();
                String i = "20".equals(s) ? "1" : "40".equals(s) ? "2" :
                    "60".equals(s) ? "3" : "100".equals(s) ? "4" : "200".equals(s) ? "5" : "";
                if (i.length()>0) {
                    stepper.readAction("doTransaction");
                    return i;
                }
            }
            else if ("custAnotherTrans".equals(name)) {
                String s = action.getParam(1).getValue();
                String i = "true".equals(s) ? "1" : "false".equals(s) ? "2" : "";
                if (i.length()>0) return i;
            }
        }
        if (mode == AMOUNT_MODE && "custEnterAmountT".equals(name)) {
            String s = action.getParam(1).getValue();
            stepper.readAction("doTransaction");
            return s;
        }
        error("readInput("+mode+","+maxValue+")", action);
        return null;
    }

    @Override
    public void dispenseCash(Money amount)
    {
        String s = amount.toString();
        stepper.requireAction("dispenseCash("+s.substring(1, s.length()-3)+")");
        super.dispenseCash(amount);
    }

    @Override
    public void printReceiptLine(String text)
    {
        if (text.startsWith("ATM #")) {
            stepper.readAction("printReceipt");
        }
        super.printReceiptLine(text);
    }

    public static void pause(int i) {
        try
        {
            Thread.sleep(i * 1000);
        }
        catch(InterruptedException e)
        { }
    }

    SimOperatorPanel operatorPanel;
    SimCardReader cardReader;

    public static Object getFieldObject(Object object, String name) {
        try {
            Field f = object.getClass().getSuperclass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(object);
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
