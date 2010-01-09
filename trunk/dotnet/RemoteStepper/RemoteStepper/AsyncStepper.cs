﻿/*
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
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using NModel.Conformance;
using NModel.Terms;

namespace RemoteStepper
{
    /**
     * <summary>
     * Transmits synchronous steps over TCP to a remote stepper <see cref="Stepper"/>
     * and receives asynchronous atomic actions as UDP datagrams. 
     * <para>Datagrams are strings in UTF-8 (without nl char termination).</para>
     * <para>Actions are transmitted in their string representation.</para>
     * <para>For configuration see <see cref="Config"/>.</para>
     * </summary>
     * <remarks>
     * <para>
     * RemoteStepper is a <a href="http://nmodel.codeplex.com/">NModel</a> stepper 
     * supporting remote test harness in other languages,
     * e.g., <a href="dk.hippogrif/sw/nmodel">Java</a>.
     * </para>
     * <para>Configure NModel ct with /r:RemoteStepper.dll /iut:RemoteStepper.AsyncStepper.Create</para>
     * </remarks>
     */
    class AsyncStepper : Stepper, IAsyncStepper
    {
        ObserverDelegate observer;
        Thread svthread;

        public void SetObserver(ObserverDelegate observer)
        {
            this.observer = observer;
        }

        new public CompoundTerm DoAction(CompoundTerm action)
        {
            if (svthread == null) { startServer(); }
            return base.DoAction(action);
        }

        void startServer()
        {
            Config.init();
            svthread = new Thread(new ThreadStart(server));
            svthread.IsBackground = true;
            svthread.Start(); 
        }

        void server()
        {
            if (Config.logEnabled) { Logger.log("start UDP server on port " + Config.asyncPort); }
            IPEndPoint ipep = new IPEndPoint(IPAddress.Any, Config.asyncPort);  
            Socket newsock = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
            newsock.Bind(ipep);
            IPEndPoint sender = new IPEndPoint(IPAddress.Any, 0);
            EndPoint Remote = (EndPoint)(sender);
            while(true)
            {
                byte[] data = new byte[Config.bufferSize];
                int recv = newsock.ReceiveFrom(data, ref Remote);
                string s = Encoding.UTF8.GetString(data, 0, recv);
                if (Config.logEnabled) { Logger.log("obsv " + s); }
                if (observer != null) { observer(CompoundTerm.Parse(s)); }
            }
        }

        new public static IAsyncStepper Create()
        {
            return new AsyncStepper();
        }
    }
}
