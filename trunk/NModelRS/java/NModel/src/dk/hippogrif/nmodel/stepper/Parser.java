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

import java.util.ArrayList;

/**
 * Parse action to Term tree.
 * The grammar looks like this
 * <code>
 * <br>  term ::= value | quotedValue | invocation
 * <br>  invocation ::= name "(" [paramlist] ")"
 * <br>  paramlist ::= term  ["," term]*
 * </code>
 * @author Jesper Goertz
 */
public class Parser {
    String s;
    int pos = -1;

    public Parser(String s) {
        this.s = s;
        next();
    }

    public static Term parse(String s) {
        Parser parser = new Parser(s);
        Term term = parser.parse();
        if (parser.c > 0) {
            parser.error("more to read");
        }
        return term;
    }

    /**
     * entry with pos at first char of a term
     * @return a term with pos at char after
     */
    public Term parse() {
        Term term = new Term();
        int startPos = pos;
        for (;;) {
            switch (c) {
                case '(':
                    if (pos-startPos<1) error("no name");
                    term.terms = new ArrayList<Term>();
                    term.terms.add(new Term(s.substring(startPos, pos).trim()));
                    if (nextc() != ')') {
                        do {
                            term.terms.add(parse());
                            if (c == ',') { nextc(); }
                        }
                        while (c != ')');
                    }
                    next();
                    return term;
                case ')':
                case ',':
                    if (pos-startPos<1) error("no value");
                    term.value = s.substring(startPos, pos).trim();
                    return term;
                case '"':
                    if (pos!=startPos) error("misplaced quote");
                    startPos++;
                    while (nextc() != '"') {};
                    term.value = s.substring(startPos, pos);
                    term.isQuoted = true;
                    nextc();
                    return term;
                default:
            }
            nextc();
        }
    }

    void error(String msg) {
        throw new NModelException(NModelException.Reason.PARSER,
                msg+" at pos "+pos+" char "+c+" in "+s);
    }

    char c;

    char nextc() {
        if (next() == 0) error("end of string");
        return c;
    }

    char next() {
        pos = pos+1;
        c = pos < s.length() ? s.charAt(pos) : 0;
        return c;
    }
}
