using System;
using System.Collections.Generic;
using System.Text;
using NModel;
using NModel.Attributes;
using NModel.Execution;

namespace ATM
{
    internal enum TRANS_TYPE { TRANSFER, WITHDRAWAL, NONE }
    internal enum ACCT_TYPE { CHECKING, SAVINGS, MONEYMARKET, EMPTY }
    internal enum SessionState { initTrans, doTrans, waitingForAnotherTrans, retryPin, cardRetain, dispenseCash, printReceipt, message }

    /// <summary/>
    public static class Session
    {
        const int NAN = 0;

        internal static SessionState state = SessionState.initTrans;
        internal static int maxPinRetries = 3;
        static int pinRetries = 0;
        internal static TRANS_TYPE transType = TRANS_TYPE.NONE;
        static ACCT_TYPE fromAccount = ACCT_TYPE.EMPTY;
        static ACCT_TYPE toAccount = ACCT_TYPE.EMPTY;
        static int transAmount = NAN;

        static readonly Set<TRANS_TYPE> TransTypes = new Set<TRANS_TYPE>(TRANS_TYPE.TRANSFER, TRANS_TYPE.WITHDRAWAL);
        static readonly Set<ACCT_TYPE> AcctTypes = new Set<ACCT_TYPE>(ACCT_TYPE.CHECKING, ACCT_TYPE.MONEYMARKET, ACCT_TYPE.SAVINGS);
        static readonly Set<int> Dollars = new Set<int>(20, 40, 60, 100, 200);
        static Set<int> DispensedDollars() { return new Set<int>(transAmount); }
        static Set<int> Pins() { return Card.Pins(); }
        internal static Set<MSG> messages = Set<MSG>.EmptySet;

        internal static void addMessage(MSG msg) { messages = messages.Add(msg); state = SessionState.message; }
        internal static bool hasMessages() { return !messages.IsEmpty; }
        internal static bool hasMessage(MSG msg) { return messages.Contains(msg); }

        [Action]
        static void message(MSG msg) 
        { 
            state = 
                msg == MSG.SESSION_MSG ? SessionState.initTrans : 
                msg == MSG.INSUFFICIENT_CASH ? SessionState.initTrans : 
                msg == MSG.IDLE_MSG ? SessionState.initTrans :
                msg == MSG.INVALID_PIN ? SessionState.retryPin :
                    SessionState.waitingForAnotherTrans;
            messages = Set<MSG>.EmptySet;
            if (msg == MSG.IDLE_MSG) { ATM.state = State.idle; }
        }
        static bool messageEnabled(MSG msg) { return messages.Contains(msg) && state == SessionState.message; }

        [Action]
        static void custCancel() { pinRetries = 0; }
        static bool custCancelEnabled() { return transType == TRANS_TYPE.NONE && state == SessionState.initTrans; }

        [Action]
        static MSG custCancelTrans() { resetTransaction(); state = SessionState.waitingForAnotherTrans; return MSG.CANCEL_TRANS; }
        static bool custCancelTransEnabled()
        {
            return ATM.state == State.inSession && transType != TRANS_TYPE.NONE &&
                (state == SessionState.initTrans || state == SessionState.retryPin);
        }

        [Action]
        static MSG custSelectTrans([Domain("TransTypes")] TRANS_TYPE transactionType) { transType = transactionType; return MSG.SELECT_FROM_ACCT; }
        static bool custSelectTransEnabled()
        {
            return ATM.state == State.inSession && state == SessionState.initTrans && transType == TRANS_TYPE.NONE;
        }

        [Action]
        static MSG custSelectAcct([Domain("AcctTypes")] ACCT_TYPE accountType) 
        {
            if (fromAccount == ACCT_TYPE.EMPTY) 
            { 
                fromAccount = accountType; 
                return transType == TRANS_TYPE.WITHDRAWAL ? MSG.ENTER_AMOUNT : MSG.SELECT_TO_ACCT; 
            }
            else { toAccount = accountType; return MSG.ENTER_AMOUNT; }
        }
        static bool custSelectAcctEnabled()
        {
            return state == SessionState.initTrans && 
            ((transType == TRANS_TYPE.TRANSFER && toAccount == ACCT_TYPE.EMPTY) ||
            (transType == TRANS_TYPE.WITHDRAWAL && fromAccount == ACCT_TYPE.EMPTY));
        }

        [Action]
        static void custEnterAmountW([Domain("Dollars")] int dollars) { transAmount = dollars; state = SessionState.doTrans; }
        static bool custEnterAmountWEnabled()
        {
            return state == SessionState.initTrans && transAmount == NAN && 
                transType == TRANS_TYPE.WITHDRAWAL && fromAccount != ACCT_TYPE.EMPTY;
        }

        [Action]
        static void custEnterAmountT(int cents) { transAmount = cents; state = SessionState.doTrans; }
        static bool custEnterAmountTEnabled()
        {
            return state == SessionState.initTrans && transAmount == NAN && 
                transType == TRANS_TYPE.TRANSFER && toAccount != ACCT_TYPE.EMPTY;
        }

        [Action]
        static void custAnotherTrans(bool yes) 
        {
            if (yes) { addMessage(MSG.SESSION_MSG); }
            else { pinRetries = 0; ATM.custCancel(); state = SessionState.initTrans; }
        }
        static bool custAnotherTransEnabled() { return state == SessionState.waitingForAnotherTrans; }

        [Action]
        static void doTransaction()
        {
            if (transType == TRANS_TYPE.WITHDRAWAL && transAmount > ATM.cashOnHand)
            {
                transAmount = NAN;
                addMessage(MSG.INSUFFICIENT_CASH);
                return;
            }
            if (ATM.pin == ATM.card.pin)
            {
                if (transType == TRANS_TYPE.WITHDRAWAL)
                {                    
                    if (ATM.card.withdrawal(fromAccount, transAmount)) { ATM.cashOnHand = ATM.cashOnHand - transAmount; }
                }
                else { ATM.card.transfer(fromAccount, toAccount, transAmount); }
                if (hasMessages()) { resetTransaction(); }
                else { state = transType == TRANS_TYPE.WITHDRAWAL ? SessionState.dispenseCash : SessionState.printReceipt; }
            }
            else
                if (pinRetries < maxPinRetries) { addMessage(MSG.INVALID_PIN); }
                else 
                { 
                    resetTransaction();
                    ATM.card.remove();
                    closeCard();
                    state = SessionState.cardRetain;
                }
        }
        static bool doTransactionEnabled() { return state == SessionState.doTrans; }

        [Action]
        static void dispenseCash([Domain("DispensedDollars")] int dollars) { state = SessionState.printReceipt; }
        static bool dispenseCashEnabled() { return state == SessionState.dispenseCash; }

        [Action]
        static void printReceipt() { resetTransaction(); addMessage(MSG.ANOTHER_TRANS); }
        static bool printReceiptEnabled() { return state == SessionState.printReceipt; }

        [Action]
        static void custRetryPin([Domain("Pins")] int pin) 
        { 
            ATM.pin = pin; 
            pinRetries++; 
            state = SessionState.doTrans;
            doTransaction();
        }
        static bool custRetryPinEnabled() { return state == SessionState.retryPin; }

        [Action]
        static MSG retainCard() 
        { 
            addMessage(MSG.IDLE_MSG); 
            return MSG.CARD_RETAINED; 
        }
        static bool retainCardEnabled() { return state == SessionState.cardRetain; }

        static void closeCard()
        {
            ATM.closeCard();
        }

        static void resetTransaction()
        {
            transType = TRANS_TYPE.NONE;
            fromAccount = ACCT_TYPE.EMPTY;
            toAccount = ACCT_TYPE.EMPTY;
            transAmount = NAN;
            pinRetries = 0;
        }
    }
}
