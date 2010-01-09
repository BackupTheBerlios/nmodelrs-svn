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

using System.Net;
using System.Net.Sockets;
using System.Threading;
using System.Text; 

namespace RemoteStepper
{
    class Client
    {
        Socket socket;
        bool connected; 

        byte[] receiveBuf;

        public bool isConnected()
        {
            return connected;
        }

        public void Socket()
        {
            socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
        }

        public void Connect(string ipAddr, int port, int bufferSize)
        {
            receiveBuf = new byte[bufferSize];
            socket.Connect(new IPEndPoint(IPAddress.Parse(ipAddr), port));
            connected = true;
        }

        public void Send(string command)
        {
            byte[] sendBuf = Encoding.UTF8.GetBytes(command);
            socket.Send(sendBuf);
        }

        public string Receive()
        {
            int nbytes = socket.Receive(receiveBuf);
            return Encoding.UTF8.GetString(receiveBuf, 0, nbytes);
        }

        public void Close()
        {
            socket.Close();
            connected = false;
        }

        public void Sleep(int seconds)
        {
            Thread.Sleep(1000 * seconds);
        }
    }
}
