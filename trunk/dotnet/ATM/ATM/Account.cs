using System;
using System.Collections.Generic;
using System.Text;
using NModel;
using NModel.Attributes;
using NModel.Execution;

namespace ATM
{
    /// <summary/>
    public class Account : LabeledInstance<Account>
    {
        internal int id;
        internal ACCT_TYPE acctType;
        internal int balance;

        internal static Set<Account> allAccounts = Set<Account>.EmptySet;

        /// <remarks/>
        public override void Initialize()
        {
            id = 0;
            acctType = ACCT_TYPE.EMPTY;
            balance = 0;
        }

        internal static Account get(int id)
        {
            Account account = allAccounts.Select(e => e.id == id).Choose();
            if (account == null) { throw new Exception("no account for id: " + id); }
            return account;
        }

        [Action]
        static void createAccount([Domain("new")] Account account, int id, ACCT_TYPE acctType, int balance)
        {
            account.id = id;
            account.acctType = acctType;
            account.balance = balance;
            allAccounts = allAccounts.Add(account); 
        }
        static bool createAccountEnabled() { return ATM.state == State.init; }
    }
}
