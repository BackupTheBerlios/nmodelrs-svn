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

using System;
using NModel.Conformance;
using NModel.Terms;

namespace RemoteStepperProxy
{
    /**
     * <summary>
     * Transmits synchronous steps over TCP to a remote stepper. 
     * <list type="bullet">
     * <item>Messages are strings in UTF-8 terminated by a nl char.</item>
     * <item>Actions are transmitted in their string representation.</item>
     * <item>An empty reply consisting of a single nl char is interpreted as a null reply.</item>
     * <item>A reply starting with "<c>Exception: </c>" is interpreted to be followed by a text
     * to be thrown in an exception.</item>
     * <item>Reset sends the message "<c>RESET\n</c>", possibly delayed until next action, and expects an empty reply.</item>
     * </list>
     * <para>For configuration see <see cref="Config"/>.</para>
     * </summary>
     * <remarks>
     * <para>
     * RemoteStepperProxy is a <a href="http://nmodel.codeplex.com/">NModel</a> stepper 
     * supporting remote test harness in other languages,
     * e.g., Java, see <a href="http://hippogrif.dk/sw/nmodel">NModelRS</a>.
     * </para>
     * <para>Configure NModel ct with <c>/r:RemoteStepperProxy.dll /iut:RemoteStepperProxy.Stepper.Create</c></para>
     * </remarks>
     * <seealso cref="AsyncStepper"/>
     */
    public class Stepper: IStepper
    {
        Client c = new Client();
        bool resetDelayed = false;

        string receive()
        {
            string s = c.Receive();
            if (!s.EndsWith("\n"))
            {
                throw new Exception("remote input not ending in nl: " + s);
            }
            if (s.StartsWith("Exception: "))
            {
                throw new Exception(s.Substring(11,s.Length-12));
            }
            return s.Remove(s.Length - 1);
        }

        /// <summary/>
        public CompoundTerm DoAction(CompoundTerm action)
	    {
            Config.init();
            string s = action.ToString();
            if (!c.isConnected())
            {
                c.Socket();
                if (Config.logEnabled) { Logger.log("connect to " + Config.host + ":" + Config.port); }
                c.Connect(Config.host, Config.port, Config.bufferSize);
            }
            if (resetDelayed)
            {
                if (Config.logEnabled) { Logger.log("delayed Reset on " + s); }
                reset();
            }
            if (Config.logEnabled) { Logger.log("send " + s); }
            c.Send(s + "\n");
            s = receive();
            if (Config.logEnabled) { Logger.log("rcvd " + s); }
            if (s.Length == 0)
            {
                return null; 
            }
            else
            {
                return CompoundTerm.Parse(s);
            }
        }

        /// <summary/>
        public void Reset()
        {
            if (Config.delayReset)
            {
                if (Config.logEnabled) { Logger.log("delay Reset"); }
                resetDelayed = true;
            }
            else
            {
                reset();
            }
        }

        private void reset()
        {
            if (resetDelayed)
            {
                resetDelayed = false;
            }
            if (c.isConnected())
            {
                if (Config.logEnabled) { Logger.log("Reset"); }
                c.Send("RESET\n");
                String s = receive();
                if (s.Length > 0)
                {
                    throw new Exception("Reset: " + s);
                }
            }
        }

        /// <summary/>
        public static IStepper Create()
        {
            return new Stepper();
        }
    }
}
