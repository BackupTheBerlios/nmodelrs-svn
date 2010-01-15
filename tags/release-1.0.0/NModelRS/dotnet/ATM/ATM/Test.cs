using System;
using System.Collections.Generic;
using System.Text;
using NModel;
using NModel.Attributes;
using NModel.Execution;

namespace ATM
{
    [Feature]
    static class NoTransactions
    {
        [Action]
        static MSG custSelectTrans(TRANS_TYPE transactionType) { return MSG.SELECT_FROM_ACCT; }
        static bool custSelectTransEnabled() { return false; }
    }

    [Feature]
    static class PinTries1
    {
        [Action]
        static void init() { Session.maxPinRetries = 1; }
    }

    [Feature]
    static class PinOk
    {
        [Action]
        static MSG custEnterPin(int pin) { return MSG.SESSION_MSG; }
        static bool custEnterPinEnabled(int pin) { return pin > 0; }

        [Action]
        static void custRetryPin(int pin) { }
        static bool custRetryPinEnabled(int pin) { return pin > 0; }
    }

    [Feature]
    static class Deterministic // transaction check order as in simulated ATM
    {
        static readonly Set<MSG> transMsg = new Set<MSG>() { MSG.MAX_DAILY_WITHDRAWAL, MSG.INVALID_FROM_ACCT, MSG.INVALID_TO_ACCT, MSG.SAME_ACCOUNT, MSG.INVALID_ACCT_BALANCE };

        [Action]
        static void message(MSG msg) { }
        static bool messageEnabled(MSG msg) 
        {
            // WITHDRAWAL: INVALID_FROM_ACCT MAX_DAILY_WITHDRAWAL INVALID_ACCT_BALANCE
            // TRANSFER: INVALID_FROM_ACCT INVALID_TO_ACCT SAME_ACCOUNT INVALID_ACCT_BALANCE
            return
                msg != MSG.MAX_DAILY_WITHDRAWAL && msg != MSG.INVALID_FROM_ACCT && msg != MSG.INVALID_TO_ACCT && msg != MSG.SAME_ACCOUNT && msg != MSG.INVALID_ACCT_BALANCE ||
                msg == MSG.INVALID_FROM_ACCT ||
                msg == MSG.MAX_DAILY_WITHDRAWAL && !Session.hasMessage(MSG.INVALID_FROM_ACCT) ||
                msg == MSG.INVALID_TO_ACCT && !Session.hasMessage(MSG.INVALID_FROM_ACCT) ||
                msg == MSG.SAME_ACCOUNT && !Session.hasMessage(MSG.INVALID_FROM_ACCT) && !Session.hasMessage(MSG.INVALID_TO_ACCT) ||
                msg == MSG.INVALID_ACCT_BALANCE && !Session.hasMessage(MSG.MAX_DAILY_WITHDRAWAL) && !Session.hasMessage(MSG.INVALID_TO_ACCT) && !Session.hasMessage(MSG.SAME_ACCOUNT);
        }
    }
}
