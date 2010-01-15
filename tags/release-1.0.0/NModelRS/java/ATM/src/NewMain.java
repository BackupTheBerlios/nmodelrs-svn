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

import atm.NewATM;
import dk.hippogrif.nmodel.stepper.FileStepper;
import dk.hippogrif.nmodel.stepper.NModelException;
import dk.hippogrif.nmodel.stepper.Observer;
import dk.hippogrif.nmodel.stepper.RemoteStepper;
import dk.hippogrif.nmodel.stepper.Stepper;
import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;
import simulation.NewSimulation;

/**
 * NModel test harness for the java simulation of ATMExample.
 *
 * The <a href="http://nmodel.codeplex.com/">NModel</a> model of
 * the <a href="http://www.math-cs.gordon.edu/courses/cps211/ATMExample/index.html">ATMExample</a>
 * modelled in UML in <a href="http://www.cs.waikato.ac.nz/~marku/mbt/">Practical Model-Based Testing: A Tools Approach</a>
 * can be found at <a href="http://hippogrif.dk/sw/nmodel">NModelRS</a>.
 *
 * @author Jesper Goertz
 */
public class NewMain implements Runnable {
    static Logger logger = Logger.getLogger("dk.hippogrif.nmodel.atm");

    public void run() {
        ATMMain(new String[0]);
    }

    public static void pause(int i) {
        try
        {
            Thread.sleep(i * 1000);
        }
        catch(InterruptedException e)
        { }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 2 || !(args[0].equals("-f") || args[0].equals("-s"))) {
            System.out.println("NewMain -f path-to-testsuite msTimeout");
            System.out.println("NewMain -s port [host asyncport]");
            System.exit(1);
        }
        Thread thread = new Thread(new NewMain());
        thread.start();
        pause(2);
        Stepper stepper;
        if (args[0].equals("-f")) {
            int timeout = Integer.parseInt(args[2]);
            stepper = FileStepper.newInstance(args[1], timeout);
        }
        else {
            int port = Integer.parseInt(args[1]);
            stepper = RemoteStepper.newInstance(port);
            if (args.length > 3) {
                int aPort = Integer.parseInt(args[3]);
                Observer observer = Observer.newInstance(args[2], aPort);
                NewSimulation.getInstance().setObserver(observer);
            }
        }
        NewSimulation.getInstance().setStepper(stepper);
        try {
            while (!stepper.readAction().getName().equals("init")) {};
            while (true) {
                NewSimulation.getInstance().semaphore.acquire();
                NewSimulation.getInstance().doAction();
            }
        } catch (NModelException e) {
            if (e.reason == NModelException.Reason.EOF) {
                logger.warning("*** EOF TestSuite ***");
                System.exit(0);
            }
            logger.severe(e.stackTrace());
            System.exit(1);
        }
    }

    /**
     * main from ATMMain, but using NewATM and NewSimulation
     * @param args
     */
    public static void ATMMain(String[] args)
    {
        NewATM theATM = new NewATM(42, "Gordon College", "First National Bank of Podunk",
                             null /* We're not really talking to a bank! */);
        NewSimulation theSimulation = new NewSimulation(theATM);

        // Create the frame that will display the simulated ATM, and add the
        // GUI simulation to it

        Frame mainFrame = new Frame("ATM Simulation");
        mainFrame.add(theSimulation.getGUI());

        // Arrange for a file menu with a Quit option, plus quit on window close

        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem quitItem = new MenuItem("Quit", new MenuShortcut('Q'));
        quitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                logger.warning("*** ATM GUI Quit ***");
                System.exit(1);
            }
        });
        fileMenu.add(quitItem);
        menuBar.add(fileMenu);
        mainFrame.setMenuBar(menuBar);
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e)
            {
                logger.warning("*** ATM GUI Close ***");
                System.exit(1);
            }
        });

        // Start the Thread that runs the ATM

        new Thread(theATM).start();

        // Pack the GUI frame, show it, and off we go!

        mainFrame.setResizable(false);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

}
