/* SER 540/494 - Assignment 3: CAN Data Simulator and Curve Warnings
 * Carlos Franco & Jonathan Zerby
 * ASU Fall 2020
 * 
 * This program provides users with a simulator that displays the various values
 * CAN sensors provide. It reimplements the old CAN data reader from assignment
 * 2 and simulates receiving the data in real time, displaying the values 
 * via Java Swing. Additionally, this program contains a simulated curve
 * detection ADAS, where curves are detected through the yaw rate sensor and
 * then classified based on speed (fast or slow) and direction (left or right).
 */

import java.io.IOException;

public class CurveDetectionSimulator { //implements ActionListener {
   
    public static void main(String args[]) {
        //Initializing GUI.
        //new CurveDetectionSimulator();
        //Initializing CANDataReader.
        CANDataReader dataReader = new CANDataReader(); 
        //Retrieving the locations of the files to read from args.
        String gpsTrackFile = args[0];
        String canMessagesFile = args[1];

        try {
            //Reading in "GPS Track.htm".
            dataReader.readGpsCoords(gpsTrackFile);
            //Reading in "CANmessages.trc" and creating CANmessages.
            dataReader.readCanMessages(canMessagesFile);
            //Initializing simulator with CAN message data.
             Simulator sim = new Simulator(dataReader.getMessages());
            //Running simulation.
            sim.startSimulation();
        } catch (IOException e) {
            System.out.println("ERROR: Unable to find passed file(s).");
            e.printStackTrace();
        }
    }
    
}

