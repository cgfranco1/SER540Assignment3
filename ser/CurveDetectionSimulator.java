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
import javax.swing.*;
import java.awt.event.*;
import java.io.OutputStream;
import java.io.PrintStream;

public class CurveDetectionSimulator implements ActionListener {
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

    JTextArea textArea;
    JLabel Label1;
    CurveDetectionSimulator(){
        JFrame f=new JFrame();//creating JFrame   
        JButton b=new JButton("Start/reset");//creating JButton 
        Label1=new JLabel();  // new Jlabel 
        Label1.setBounds(40,25,100,30); // bounds for the label 
        Label1.setText("Simulator"); // setting text for label 
        b.setBounds(150,550,120,30);//x axis, y axis, width, height  
        b.addActionListener(this);  // adding action listener   
        textArea = new JTextArea();  // new text area
        //textArea.setBounds(5,65,525,450);
        JScrollPane scrollbar=new JScrollPane(textArea);
        scrollbar.setBounds(5,65,575,450);
        scrollbar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        f.add(Label1);  // adding stuff to the frame
        f.add(b);
        f.getContentPane().add(scrollbar);
        f.setSize(600,700);//400 width and 500 height 
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // exit on close ability 
        f.setLayout(null);//using no layout managers  
        f.setVisible(true);//making the frame visible  
        PrintStream out = new PrintStream( new CustomOutputStream(textArea) ); // redirecting console output to gui to run simluation
        System.setOut(out); // system output
        //System.setErr(out); // error output
    } 
    public void actionPerformed(ActionEvent e){  
        System.out.println("hello Carlos Wink "); 
     
    }
}

