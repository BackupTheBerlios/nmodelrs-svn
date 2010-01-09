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

package dk.hippogrif.nmodel;

import java.util.ArrayList;

/**
 * Term is either a value or a list of terms.
 *
 * @author Jesper Goertz
 */
public class Term {
    String value;
    boolean isQuoted;
    ArrayList<Term> terms;

    public Term() {
    }

    public Term(String value) {
        this.value = value;
    }

    public boolean isQuoted() {
        return isQuoted;
    }
    public String getValue() {
        return value;
    }

    public int getIntValue() {
        return Integer.parseInt(value);
    }

    public String getFirstName() {
        return terms.get(0).value;
    }

    public Term getParam(int i) {
        return terms.get(i);
    }

   /**
    * Serialize term.
    *
    * @return serialized term
    */
    @Override
    public String toString() {
        if (value != null) {
            if (isQuoted) {
                return "\""+value+"\"";
            }
            return value;
        }
        if (terms != null) {
            StringBuffer sb = new StringBuffer();
            for (int i=0; i<terms.size(); i++) {
                sb.append(terms.get(i).toString());
                if (i==0) sb.append('(');
                else if (i<terms.size()-1) sb.append(',');
            }
            sb.append(')');
            return sb.toString();
        }
        return "NULL";
    }

}
