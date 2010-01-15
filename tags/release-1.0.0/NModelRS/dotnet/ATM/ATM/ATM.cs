using System;
using System.Collections.Generic;
using System.Text;
using NModel;
using NModel.Attributes;
using NModel.Execution;

namespace ATM
{
    internal enum MSG
    {
        // ATM messages
        OFF_MSG,
        IDLE_MSG,
        UNREADABLE_MSG,
        PIN_MSG,
        SESSION_MSG,
        // Session messages
        CANCEL_TRANS,
        ANOTHER_TRANS,
        SELECT_FROM_ACCT,
        SELECT_TO_ACCT,
        ENTER_AMOUNT,
        INSUFFICIENT_CASH,
        MAX_DAILY_WITHDRAWAL,
        INVALID_PIN,
        INVALID_FROM_ACCT,
        INVALID_TO_ACCT,
        SAME_ACCOUNT,
        INVALID_ACCT_BALANCE,
        CARD_RETAINED
    }
    internal enum State { init, off, waitingForCash, idle, waitingUserPin, inSession, unreadableCard, cardEjection }

    /**
     * <summary>
     * An <a href="http://nmodel.codeplex.com/">NModel</a> model of 
     * the <a href="http://www.math-cs.gordon.edu/courses/cps211/ATMExample/index.html">ATMExample</a>
     * modelled in UML in <a href="http://www.cs.waikato.ac.nz/~marku/mbt/">Practical Model-Based Testing: A Tools Approach</a>.
     * The test harness for the Java ATM simulation can be found at <a href="http://hippogrif.dk/sw/nmodel">NModelRS</a>.
     * </summary>
     */
    public static class ATM
    {
        internal static State state = State.init;
        internal static int cashOnHand = 0;
        internal static Card card;
        internal static int pin = 0;

        static Set<int> CardIds() { return Card.CardIds(); }
        static Set<int> Pins() { return Card.Pins(); } 

        [AcceptingStateCondition]
        static bool offOrIdle() { return state == State.off || state == State.idle; }

        [Action]
        static void init() { state = State.off; }
        static bool initEnabled() { return state == State.init; }

        [Action]
        static void switchOn() { state = State.waitingForCash; }
        static bool switchOnEnabled() { return state == State.off; }

        [Action]
        static MSG switchOff() { state = State.off; cashOnHand = 0; return MSG.OFF_MSG; }
        static bool switchOffEnabled() { return state == State.idle; }

        [Action]
        static MSG setCash(int bills) { cashOnHand = bills * 20; state = State.idle; return MSG.IDLE_MSG; }
        static bool setCashEnabled() { return state == State.waitingForCash; }

        [Action]
        static MSG custInsertCard([Domain("CardIds")] int cardNo) 
        {
            if (cardNo > 0) { card = Card.get(cardNo); state = State.waitingUserPin; return MSG.PIN_MSG; }
            else { state = State.unreadableCard; return MSG.UNREADABLE_MSG; }
        }
        static bool custInsertCardEnabled() { return state == State.idle; }

        [Action]
        static MSG ejectCard() { closeCard(); state = State.idle; return MSG.IDLE_MSG; }
        static bool ejectCardEnabled() { return state == State.cardEjection || state == State.unreadableCard; }

        [Action]
        static MSG custEnterPin([Domain("Pins")] int pin) { ATM.pin = pin; state = State.inSession; return MSG.SESSION_MSG; }
        static bool custEnterPinEnabled() { return state == State.waitingUserPin; }

        [Action]
        internal static void custCancel() { closeCard(); state = State.cardEjection; }
        static bool custCancelEnabled() { return state == State.waitingUserPin || state == State.inSession; }

        internal static void closeCard()
        {
            card = null;
            pin = 0;
        }
    }

}
