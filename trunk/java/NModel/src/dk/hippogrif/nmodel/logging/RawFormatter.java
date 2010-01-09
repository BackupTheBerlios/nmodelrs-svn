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

package dk.hippogrif.nmodel.logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Simple format of log records with name of method and message.
 *
 * Configure it in logging.properties with, e.g.,
 * <code>
 * java.util.logging.ConsoleHandler.formatter = dk.hippogrif.nmodel.logging.RawFormatter
 * </code>
 *
 * @author Jesper Goertz
 */
public class RawFormatter extends Formatter {

    @Override
    public synchronized String format(LogRecord record) {
        return record.getSourceMethodName()+": "+record.getMessage()+"\n";
    }

}
