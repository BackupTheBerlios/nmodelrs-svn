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

using System.IO;
using System.Reflection;
using System.Xml.Linq;

namespace RemoteStepper
{
    /**
     * <summary>
     * Configures remote stepper from XML file "RemoteStepper.dll.conf" in same
     * directory as RemoteStepper.dll. 
     * <para>If the conf file (shown below) is not present the following default values are used</para>
     * <code>
     * <configuration>
     *   <port>4711</port>
     *   <host>127.0.0.1</host>
     *   <asyncPort>4712</asyncPort>
     *   <bufferSize>1024</bufferSize>
     *   <delayReset>True</delayReset>
     *   <logEnabled>False</logEnabled>
     * </configuration>
     * </code>
     * <list>
     * <description><see cref="Stepper"/>opens a TCP connection to host:port.</description>
     * <description><see cref="AsyncStepper"/>listens on asyncPort.</description>
     * <description>bufferSize must be able to hold any received message or datagram.</description>
     * <description>Reset after test case may be delayed until start of next test case.</description>
     * <description>activity is logged on console iff logEnabled is True.</description>
     * </list>
     * </summary>
     * <remarks>
     * RemoteStepper is a <a href="http://nmodel.codeplex.com/">NModel</a> stepper 
     * supporting remote test harness in other languages,
     * e.g., <a href="dk.hippogrif/sw/nmodel">Java</a>.
     * </remarks>
     */
    static class Config
    {
        internal static bool initialized = false;
        internal static int port = 4711;
        internal static string host = "127.0.0.1"; // localhost
        internal static int asyncPort = 4712;
        internal static int bufferSize = 1024;
        internal static bool delayReset = true;
        internal static bool logEnabled = false;

        public static void init()
        {
            if (initialized) { return; }
            readConfig();
            initialized = true;
        }

        public static bool readConfig()
        {
            Assembly assembly = typeof(Config).Assembly;
            string configFile = assembly.Location + ".conf";
            if (!File.Exists(configFile)) return false;
            XDocument doc = XDocument.Load(@configFile);
            foreach (XElement elem in doc.Root.Elements())
            {
                switch (elem.Name.ToString())
                {
                    case "port":
                        port = int.Parse(elem.Value);
                        break;
                    case "host":
                        host = elem.Value;
                        break;
                    case "asyncPort":
                        asyncPort = int.Parse(elem.Value);
                        break;
                    case "bufferSize":
                        bufferSize = int.Parse(elem.Value);
                        break;
                    case "delayReset":
                        delayReset = bool.Parse(elem.Value);
                        break;
                    case "logEnabled":
                        logEnabled = bool.Parse(elem.Value);
                        break;
                }
            }
            return true;
        }
    }
}
