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

/**
 * Action is a tree of terms representing function symbols and parameters.
 * Top head is action name, other heads names data types.
 * E.g., the action in the text line A(5,Set<string>("c","b")) is
 * parsed to the tree ("A","5",("Set<string>","c","b")).
 *
 * @author Jesper Goertz
 */
public class Action extends Term {

    public static Action parse(String line) {
        Action action = new Action();
        action.terms = Parser.parse(line).terms;
        if (action.terms == null) {
            throw new NModelException(NModelException.Reason.PARSER, line);
        }
        return action;
    };

    public String getName() {
        String name = getFirstName();
        if (name.endsWith("_Start")) {
            return name.substring(0, name.length()-6);
        }
        return name;
    }

}
