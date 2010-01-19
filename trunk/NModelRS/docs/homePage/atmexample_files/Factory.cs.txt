using System.Collections.Generic;
using NModel;
using NModel.Execution;
using NModel.Attributes;

namespace ATM
{
    /// <summary/>
    public static class Factory
    {
        /// <remarks/>
        public static ModelProgram Create()
        {
            return new LibraryModelProgram(typeof(ATM).Assembly,
              "ATM", new Set<string>());
        }

        /// <remarks/>
        public static ModelProgram CreateNoTransactions()
        {
            return new LibraryModelProgram(typeof(ATM).Assembly,
              "ATM", new Set<string>("NoTransactions"));
        }

        /// <remarks/>
        public static ModelProgram CreatePinTries1()
        {
            return new LibraryModelProgram(typeof(ATM).Assembly,
              "ATM", new Set<string>("PinTries1"));
        }

        /// <remarks/>
        public static ModelProgram CreatePinOk()
        {
            return new LibraryModelProgram(typeof(ATM).Assembly,
              "ATM", new Set<string>("PinOk"));
        }

        /// <remarks/>
        public static ModelProgram CreateDeterministic()
        {
            return new LibraryModelProgram(typeof(ATM).Assembly,
              "ATM", new Set<string>("Deterministic"));
        }

        /// <remarks/>
        public static ModelProgram CreatePinOkDeterministic()
        {
            return new LibraryModelProgram(typeof(ATM).Assembly,
              "ATM", new Set<string>("PinOk", "Deterministic"));
        }
    }
}
