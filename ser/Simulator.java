/* This class implements the simulation portion of the project, in which once
 * all of the data has been read in, this class will simulate reading the data
 * in at real time. 
 */
import java.io.IOException;
import javax.swing.*;

import java.awt.event.*;
import java.io.OutputStream;
import java.io.PrintStream;

public class Simulator implements ActionListener{

    private CANmessage[] msgArr;
    private int pointer;
    private String[] canValues;
    public JTextArea textArea;
    JLabel Label1;
    public Simulator(CANmessage[] msgArr) {
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
        scrollbar.setBounds(5,65,650,450);
        scrollbar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        f.add(Label1);  // adding stuff to the frame
        f.add(b);
        f.getContentPane().add(scrollbar);
        f.setSize(700,700);//400 width and 500 height 
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // exit on close ability 
        f.setLayout(null);//using no layout managers  
        f.setVisible(true);//making the frame visible  
        //PrintStream out = new PrintStream( new CustomOutputStream(textArea) ); // redirecting console output to gui to run simluation
        //System.setOut(out); // system output
        //System.setErr(out); // error output
        //timeOffset = 0.0;
        this.msgArr = msgArr;
        pointer = 0;
        canValues = new String [7];
        //Represents steering angle.
        canValues[0] = "-";
        //Represents vehicle speed.
        canValues[1] = "-";
        //Represents yaw rate.
        canValues[2] = "-";
        //Represents lateral acceleration.
        canValues[3] = "-";
        //Represents longitudinal acceleration.
        canValues[4] = "-";
        //Represents GPS latitude.
        canValues[5] = "-";
        //Represents GPS longitude.
        canValues[6] = "-";
        
    }
    
    public void actionPerformed(ActionEvent e){  
        pointer = 0;
        canValues = new String [7];
        //Represents steering angle.
        canValues[0] = "-";
        //Represents vehicle speed.
        canValues[1] = "-";
        //Represents yaw rate.
        canValues[2] = "-";
        //Represents lateral acceleration.
        canValues[3] = "-";
        //Represents longitudinal acceleration.
        canValues[4] = "-";
        //Represents GPS latitude.
        canValues[5] = "-";
        //Represents GPS longitude.
        canValues[6] = "-";
    }

    public void startSimulation() {
       

        //Represents the starting time of the simulation.
        long startTime = System.nanoTime();
        //Represents the current time of the system.
        long currentTime;
        //Represents the difference between currentTime and startTime in ms.
        double timeOffset = 0.0;
        //Stringbuilder used to write out the CAN values.
        StringBuilder sb;

        
        //Loop until the end of CAN values is reached.
        while (pointer < msgArr.length) {
            //textArea.setText(null);
            //Creating new StringBuilder.
            sb = new StringBuilder();

            //Used to hold CANmessage that is currently being pointed at.
            CANmessage tempMsg = msgArr[pointer];
            //Used to hold the value of the CANmessage being pointed at.
            double tempVal = tempMsg.getDecodedVal();

            //If the time offset of the current CANmessage matches the time.
            if(tempMsg.getTimeOffset() <= timeOffset) {

                //If the next CANmessage's time offset matches the time.
                if ((pointer + 1 < msgArr.length) && (msgArr[pointer + 1].getTimeOffset() <= timeOffset)) {

                    /*If the time offset of the next two CANmessages match the 
                      time in addition to the current CANmessage, then the 
                      CANmessages equates to yaw rate, lat acc and long acc.*/
                    if ((pointer + 2 < msgArr.length) && (msgArr[pointer + 2].getTimeOffset() <= timeOffset)) {
                        //Adding yaw rate value as string.
                        updateCanValues(2, 2, tempVal);
                        
                        //Getting value for lateral acceleration.
                        tempVal = msgArr[pointer].getDecodedVal();
                        //Adding lateral acceleration value as string.
                        updateCanValues(3, 2, tempVal);

                        //Getting value for longitudinal acceleration.
                        tempVal = msgArr[pointer].getDecodedVal();
                        //Adding longitudinal acceleration value as string.
                        updateCanValues(4, 2, tempVal);
                        
                    /*Else if the time offset of only the next CANmessage and
                      current CANmessage match the time, then the CANmessages 
                      equate to GPS lat and GPS long.*/
                    } else {
                        //Adding GPS latitude value as string.
                        updateCanValues(5, 6, tempVal);
                        //Getting value for GPS longitude.
                        tempVal = msgArr[pointer].getDecodedVal();
                        //Adding GPS longitude value as string.
                        updateCanValues(6, 6, tempVal);
                    }

                //Else if time offset equates to steering angle.
                } else if (tempMsg.getMessageDesc().equals("Steering wheel angle")) {
                    updateCanValues(0, 2, tempVal);

                //Else time offset equates to vehicle speed.
                } else {
                    updateCanValues(1, 2, tempVal);
                }
            }

            //Appending all CAN values to the StringBuilder.
            sb.append("Current Time | Steering Angle | Vehicle Speed |" + 
                           "    Yaw Rate    |  Lateral Acc  |" +
                           " Longitudinal Acc | GPS Lat, Long\n");
            sb.append(String.format("%9.1f ms |", timeOffset));
            sb.append(String.format("%11s deg |", canValues[0]));
            sb.append(String.format("%9s km/h |", canValues[1]));
            sb.append(String.format("%9s deg/s |", canValues[2]));
            sb.append(String.format("%8s m/s^2 |", canValues[3]));
            sb.append(String.format("%11s m/s^2 |", canValues[4]));
            sb.append(String.format(" (%s, %s)", canValues[5], canValues[6]));
            //sb.append("\r");
            //Printing out the CAN values.
            
            textArea.setText(sb.toString());

            //Getting current system time.
            currentTime = System.nanoTime();
            //Setting new time offset, rounded up to a single decimal.
            timeOffset = Math.round((currentTime - startTime) / 100000.0) / 10.0;
        }

        //System.out.println("\nLast time offset: " + msgArr[pointer - 1].getTimeOffset());
        //System.out.println(msgArr[pointer - 1].getMessageDesc() + ": " + msgArr[pointer - 1].getDecodedVal());
    }

    private void updateCanValues(int index, int decimals, double canVal) {
        //Creating string to represent format of value.
        String format = "%." + decimals + "f";
        //Adding value to canValues as a formatted string.
        canValues[index] = String.format(format, canVal);
        //Incrementing pointer to point to next CANmessage.
        pointer++;
    }

}
