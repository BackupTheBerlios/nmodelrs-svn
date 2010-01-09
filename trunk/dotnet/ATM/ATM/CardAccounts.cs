using System;
using System.Collections.Generic;
using System.Text;
using NModel;
using NModel.Attributes;
using NModel.Execution;

namespace ATM
{
    public partial class Card : LabeledInstance<Card>
    {
        const int maxWithdrawal = 300;

        int todaysWithdrawal;
        Map<ACCT_TYPE, Account> accounts;

        /// <remarks/>
        public override void Initialize() 
        {
            id = 0;
            pin = 0;
            todaysWithdrawal = 0;
            accounts = Map<ACCT_TYPE, Account>.EmptyMap;
        }

        [Action]
        static void linkCardAccount(int cardId, int accountId)
        {
            get(cardId).linkAccount(accountId);
        }
        static bool linkCardAccountEnabled() { return ATM.state == State.init; }

        void linkAccount(int id)
        {
            Account account = Account.get(id);
            accounts = accounts.Add(account.acctType, account);
        }

        internal bool withdrawal(ACCT_TYPE fromType, int dollars)
        {
            if (todaysWithdrawal + dollars > maxWithdrawal) { Session.addMessage(MSG.MAX_DAILY_WITHDRAWAL); }
            if (!accounts.ContainsKey(fromType)) { Session.addMessage(MSG.INVALID_FROM_ACCT); return false; }
            Account fromAccount = accounts[fromType];
            if (fromAccount.balance < dollars * 100) { Session.addMessage(MSG.INVALID_ACCT_BALANCE); }
            if ( Session.hasMessages() ) { return false; }
            fromAccount.balance = fromAccount.balance - dollars * 100;
            todaysWithdrawal = todaysWithdrawal + dollars;
            return true;
        }

        internal bool transfer(ACCT_TYPE fromType, ACCT_TYPE toType, int cents)
        {
            Account fromAccount = null;
            if (!accounts.ContainsKey(fromType)) { Session.addMessage(MSG.INVALID_FROM_ACCT); }
            else
            {
                fromAccount = accounts[fromType];
                if (fromAccount.balance < cents) { Session.addMessage(MSG.INVALID_ACCT_BALANCE); }
            }
            if (!accounts.ContainsKey(toType)) { Session.addMessage(MSG.INVALID_TO_ACCT); }
            if (fromType == toType) { Session.addMessage(MSG.SAME_ACCOUNT); }
            if (Session.hasMessages()) { return false; }
            Account toAccount = accounts[toType];
            fromAccount.balance = fromAccount.balance - cents;
            toAccount.balance = toAccount.balance + cents;
            return true;
        }
    }
}
