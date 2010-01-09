using System;
using System.Collections.Generic;
using System.Text;
using NModel;
using NModel.Attributes;
using NModel.Execution;

namespace ATM
{
    /// <summary/>
    public partial class Card : LabeledInstance<Card>
    {
        static Set<Card> allCards = Set<Card>.EmptySet;
        int id;
        internal int pin;

        internal static Card get(int id)
        {
            Card card = allCards.Select(e => e.id == id).Choose();
            if (card == null) { throw new Exception("no card for id: " + id); }
            return card;
        }

        [Action]
        static void createCard([Domain("new")] Card card, int id, int pin)
        {
            card.id = id;
            card.pin = pin;
            allCards = allCards.Add(card); 
        }
        static bool createCardEnabled() { return ATM.state == State.init; }

        internal static Set<int> CardIds()
        {
            Set<int> ids = new Set<int>(0); // 0 is unreadable card
            foreach (Card card in allCards) { ids = ids.Add(card.id); }
            return ids;
        }

        internal static Set<int> Pins()
        {
            Set<int> pins = new Set<int>(0); // 0 is bad pin
            if (ATM.card != null) { pins = pins.Add(ATM.card.pin); }
            return pins;
        }

        internal void remove()
        {
            allCards = allCards.Remove(this);
        }
    }
}
