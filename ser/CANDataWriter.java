/* SER 540/494 - Assignment 3: CAN Data Simulator and Curve Warnings
 * Carlos Franco
 * ASU Fall 2020
 * 
 * This program is able to read in CAN data files denoted as "CANmessages.trc"
 * and an HTML file of a Google Maps tracing of GPS data, denoted as
 * "GPS Track.htm". The program takes in the directories of the two files as
 * arguements from the console and then prints out the queried data specified in
 * "CAN Frames Info.txt" in a neatly formatted manner.
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.event.*;
import java.io.OutputStream;
import java.io.PrintStream;

public class CANDataWriter implements ActionListener {
    /*Arraylist used to contain all the messages in "CANmessages.trc" which have
      a frame ID that matches one of the IDs listed in "CAN Frames Info.txt".*/
    private static ArrayList<CANmessage> canMsgList;
    //Array used to contain the GLatLng values obtained from "GPS Track.htm".
    private static ArrayList<String> gpsCoords;
    /*Int used to track what the time offset needs to be to insert the two
      CANmessages which will represent the GPS coordinates.*/
    private static int gpsTimeOffset;
    //Int used to keep track of the gpsCoords index.
    private static int gpsIndex;

    //CANframes used to query data.
    private static CANframe wheelAngle, displaySpeed, yawRate, latAcc, longAcc;

    //Buffered reader used to read in "CANmessages.trc" and "GPS Track.htm".
    private static BufferedReader br;

    public static void main(String args[]) throws IOException {

        new CANDataWriter(); //init Gui 
        //Initializing canMsgList and gpsCoords.
        canMsgList = new ArrayList<>();
        gpsCoords = new ArrayList<>();
        /*GPS message insertions will need to be done every thousandth
          milisecond and so this int will be incremented by a 1000 after the
          insertion of lat and long messages.*/
        gpsTimeOffset = 0;
        //Initializing gpsIndex as 0 for the first index.
        gpsIndex = 0;

        /*All values for the next 5 CANframe objects have been obtained from 
          the "CAN Frames Info.txt" file.*/
        //Constructing CANframe obj of the steering wheel angle.
        byte[] wheelByteRange = {1, 5, 2, 0};
        double[] wheelValRange = {-2048.0, 2047.0};
        wheelAngle = new CANframe("0003", wheelByteRange, (byte) 14, 
            "Steering wheel angle", 0x3FFF, "degrees", wheelValRange, 0.5);

        //Constructing CANframe obj of the displayed vehicle speed.
        byte[] speedByteRange = {1, 3, 2, 0};
        double[] speedValRange = {0.0, 409.4};
        displaySpeed = new CANframe("019F", speedByteRange, (byte) 12, 
            "Displayed vehicle speed", 0xFFF, " km/h", speedValRange, 0.1);

        //Constructing CANframe obj of the vehicle yaw rate.
        byte[] yawByteRange = {1, 7, 2, 0};
        double[] yawValRange = {-327.68, 327.66};
        yawRate = new CANframe("0245", yawByteRange, (byte) 16, 
            "Vehicle yaw rate", 0xFFFF, "degrees/s", yawValRange, 0.01);

        //Constructing CANframe obj of the vehicle lateral acceleration.
        byte[] latByteRange = {6, 7, 6, 0};
        double[] latValRange = {-10.24, 10.08};
        latAcc = new CANframe("0245", latByteRange, (byte) 8, 
            "Vehicle longitudinal acceleration", 0xFF, " m/s^2", latValRange, 
            0.08);
        
        //Constructing CANframe obj of the vehicle longitudinal acceleration.
        byte[] longByteRange = {5, 7, 5, 0};
        double[] longValRange = {-10.24, 10.08};
        longAcc = new CANframe("0245", longByteRange, (byte) 8, 
            "Vehicle longitudinal acceleration", 0xFF, " m/s^2", longValRange, 
            0.08);

        //Retrieving the locations of the files to read from args.
        String gpsTrackFile = args[0];
        String canMessagesFile = args[1];

        //Reading in "GPS Track.htm" and filling gpsCoords with relevant data.
        readGpsCoords(gpsTrackFile);
        //Reading in "CANmessages.trc" and filling canMsgList with relevant data.
        readCanMessages(canMessagesFile);

        //Printing all the messages in canMsgList.
        printData();
    }
    
    JTextArea textArea;
    JLabel Label1;
    CANDataWriter(){
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

    /* Method which reads the "GPS Track.htm" file to search for the array "t"
     * which contains the GLatLng objects needed to retrieve GPS data. Unlike
     * readCanMessages method, this method does not actually create any of
     * the CANmessage objects used to store data in canMsgList array list.
     * Instead, the CANmessages which represent latitude and longitude points
     * are generated in the messageBuilder method which will create a CANmessage
     * for both latitude and longitude every 1000 ms starting at 0 ms.
     */
    static void readGpsCoords(String gpsFile) throws IOException {
        try {
            //Creating new buffered reader.
            br = new BufferedReader(new FileReader(gpsFile));
            //String used to represent each line.
            String line = null;

            //Skipping lines until the array with the GPS coordinates is found.
            do {
                line = br.readLine();
            } while (!line.contains("var t"));

            //Reading the first line of the array.
            line = br.readLine();
            //Using string builder to construct the coordinate strings.
            StringBuilder sb = new StringBuilder();
            /*Indicates if current character being read is part of the sequence
              which represents one of the coordinate doubles.*/
            boolean isDouble = false;

            //Loop until the end of the array is found.
            while (!line.contains("];")) {
                //Iterates through each character of the line.
                for (int i = 0; i < line.length(); i++) {
                    //If the character corresponds to a coordinate point.
                    if (Character.isDigit(line.charAt(i)) || 
                        line.charAt(i) == '.') {
                            //Appending the character to the coordinate string.
                            sb.append(line.charAt(i));
                            //Setting double flag as true.
                            isDouble = true;
                    //If the coordinate string being read in is completed.
                    } else if (isDouble == true) {
                        /*Add the coordinate to gpsCoords. The values are
                          ordered such that they alternate between latitude and 
                          longitude values starting with latitude.*/
                        gpsCoords.add(sb.toString());
                        //Initialize new StringBuilder to read next coordinate.
                        sb = new StringBuilder();
                        //Indicating that the double has finished its sequence.
                        isDouble = false;
                    }
                }
                //Reading in next line.
                line = br.readLine();
            }

        } catch (FileNotFoundException e) {
            //When the given file directory cannot be found.
            System.out.println("Error: Unable to locate file: " + gpsFile);
        } finally {
            //Closing buffered reader.
            br.close();
        }
    }

    /* Method which reads the "CANmessages.trc" file line by line checking for
     * data entries which match one of the three relevant frame IDs. The method
     * will create CANmessage objects for every line that is found with a
     * correct ID and add it to canMsgList.
     */
    static void readCanMessages(String msgFile) throws IOException {
        try {
            //Creating new buffered reader.
            br = new BufferedReader(new FileReader(msgFile));
            //String used to represent each line.
            String line = null;
            //Array used to parse values from line.
            String[] lineArr;

            //Reading the first line and skipping over the commented header.
            do {
                line = br.readLine();
            } while (line.charAt(0) == ';');

            //Reading the rest of the file.
            while ((line = br.readLine()) != null) {
                //Parsing file line into array.
                lineArr = line.trim().split("\\s+");

                //Checking if the frame ID matches one of the 3 relevant ones.
                //If frame ID equates to wheel angle.
                if(lineArr[3].equals(wheelAngle.getFrameID())) {
                    //Create wheel message and add it in the array list.
                    CANmessage wheelMessage = messageBuilder(lineArr, 
                                                             wheelAngle);
                    canMsgList.add(wheelMessage);
                //If frame ID equates to speed.
                } else if (lineArr[3].equals(displaySpeed.getFrameID())) {
                    //Create speed message and add it in the array list.
                    CANmessage speedMessage = messageBuilder(lineArr, 
                                                             displaySpeed);
                    canMsgList.add(speedMessage);
                /*If frame ID equates to yaw rate, as well as longitudinal and 
                  lateral acceleration.*/
                } else if (lineArr[3].equals(yawRate.getFrameID())) {
                    //Create messages for all three.
                    CANmessage yawMessage = messageBuilder(lineArr, yawRate);
                    CANmessage longMessage = messageBuilder(lineArr, longAcc);
                    CANmessage latMessage = messageBuilder(lineArr, latAcc);
                    //Add all three to the array list.
                    canMsgList.add(yawMessage);
                    canMsgList.add(longMessage);
                    canMsgList.add(latMessage);
                }
            }

        } catch (FileNotFoundException e) {
            //When the given file directory cannot be found.
            System.out.println("Error: Unable to locate file: " + msgFile);
        } finally {
            //Closing buffered reader.
            br.close();
        }
    }

    /* Method which constructs message objects by parsing through the data and
     * obtaining the decoded values. 
     */
    static CANmessage messageBuilder(String[] lineArr, CANframe frame) {
        //Creating CANmessage object.
        CANmessage message = new CANmessage();
        //Adding the time offset and data description.
        message.setTimeOffset(Double.parseDouble(lineArr[1]));
        message.setMessageDesc(frame.getDataDesc());

        /*If the time offset is greater than gpsTimeOffset, then the GPS CAN
          messages are inserted first and gpsTimeOffset is incremented.*/
        if (message.getTimeOffset() > gpsTimeOffset && gpsIndex < gpsCoords.size()){
            //Creating CANmessages for GPS lateral and longitudinal coordinates.
            CANmessage gLat = new CANmessage();
            CANmessage gLng = new CANmessage();
            //Setting the time offsets equal to the current gpsTimeOffset time.
            gLat.setTimeOffset(gpsTimeOffset);
            gLng.setTimeOffset(gpsTimeOffset);
            //Setting the data descriptions.
            gLat.setMessageDesc("Latitude");
            gLng.setMessageDesc("Longitude");
            //Setting the decoded value to the ones held in gpsCoords.
            gLat.setDecodedVal(Double.parseDouble(gpsCoords.get(gpsIndex)));
            //Also incrementing gpsIndex to point to the next value in the list.
            gpsIndex += 1;
            gLng.setDecodedVal(Double.parseDouble(gpsCoords.get(gpsIndex)));
            gpsIndex += 1;
            //Adding the GPS CANmessages to the array list.
            canMsgList.add(gLat);
            canMsgList.add(gLng);
            //Incrementing gpsTimeOffset.
            gpsTimeOffset = gpsTimeOffset + 1000;
        }

        //Obtaining an array of all the byte data included in the line.
        int dataLength = Integer.parseInt(lineArr[4]);
        String[] data = Arrays.copyOfRange(lineArr, 5, 5 + dataLength);
        
        //Extracting the message value and setting it in the CANmessage.
        double decodedValue = decodeData(data, frame);
        message.setDecodedVal(decodedValue);

        return message;
    }

    /* Method which converts String data into int data which can be used for
     * obtaining the decoded values. 
     */
    static double decodeData(String[] rawData, CANframe frame) {
        //Getting the data location array from the CANframe.
        byte[] dataLocation = frame.getDataLocation();
        //Reduce start byte by 1 since bytes are counted from 1 rather than 0.
        int startByte = dataLocation[0] - 1;
        //Do not reduce end byte since copyOfRange is exclusive of last index.
        int endByte = dataLocation[2];
        //Creating new array which includes only targeted bytes.
        String[] targetData = Arrays.copyOfRange(rawData, startByte, endByte);

        //Using StringBuilder to merge strings from targetData.
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < targetData.length; i++) {
            sb.append(targetData[i]);
        }
        //Convert the StringBuilder string into an integer.
        int extractedVal = Integer.parseInt(sb.toString(), 16);

        /*Now that the data has been refined down to the target bytes, some bit
          manipulation is needed to further refine the data down to the target
          bits provided in dataLocation.*/
        //Creating a bit mask which sets all left bits to be excluded as 0.
        int leftBitMask = ~(-1 << (frame.getDataSize() + 1));
        //Masking extractedVal.
        extractedVal = extractedVal & leftBitMask;
        //Bit-shifting extractedVal right up to the the last bit to be included.
        extractedVal = extractedVal >>> dataLocation[3];
        //Bit-shifting exctractedVal left to move bits back into place.
        extractedVal = extractedVal << dataLocation[3];

        //Now converting the extracted int value into the value it represents.
        double convertedVal = extractedVal * frame.getStepSize() + 
                              frame.getValRange()[0];

        return convertedVal;
    }

    static void startSimulation() {
        double time = 0.0;
        StringBuilder sb = new StringBuilder();

        System.out.println("Current Time | Steering Angle | Yaw Rate |" + 
                           " Lateral Acc | Longitudinal Acc | GPS Lat, Long");

        for (int i = 0; i < 433000; i++) {
            time = i / 10;
            sb.append(String.format("%9.1f ms |", time));
           // sb.append(String.format("%11.2f deg |", sang));
        }

        
    }
    static int getcurve(Double x) {
        if(x > 13.0) { // low speed curve =1 left 1 right -1
            return 1;
        }
        if (x < -13.0) { 
            return -1;
        }
        if(x > 2 && x < 13.0){// high speed curve = 2 left 2 right -2
            return 2; 
        }
        if(x > -2 && x < -13.0) {
            return -2;
        }
         return 0;

        

    }

    /* Method which prints the table containing the values of the data.
     */
    static void printData() {
        StringBuilder sb = new StringBuilder();
        ArrayList<Curveobj>  curves= new ArrayList<Curveobj>();
        double tmpspeed=0.0;
        double tmplat=0.0;
        double tmplon=0.0;
        Boolean iscurve;
        double startspeed=0.0;
        double endspeed=0.0;
        double avgspeed=0.0;
        int counter =0;
        int tmp2 =0;
        //sb.append("Time offset             |");
        //sb.append("Frame ID               ");
        //sb.append("|Data\n");
        //sb.append("------------------------+");
        //sb.append("------------------------");
        //sb.append("+------------------------");
        //System.out.println(sb.toString());

        for(int i = 0; i < canMsgList.size(); i++) {
            iscurve=false;
            String frameID = "frameId";
            if (canMsgList.get(i).getMessageDesc().equals(wheelAngle.getDataDesc())){
                frameID = wheelAngle.getFrameID();
            }
            else if (canMsgList.get(i).getMessageDesc().equals(displaySpeed.getDataDesc())){
               tmpspeed=canMsgList.get(i).getDecodedVal();
                frameID = displaySpeed.getFrameID();
            }
            else if (canMsgList.get(i).getMessageDesc().equals(yawRate.getDataDesc()) || 
            canMsgList.get(i).getMessageDesc().equals(latAcc.getDataDesc()) || 
            canMsgList.get(i).getMessageDesc().equals(longAcc.getDataDesc())){
                frameID = yawRate.getFrameID();
            }
            else if (canMsgList.get(i).getMessageDesc().equals("Latitude")){
                tmplat= canMsgList.get(i).getDecodedVal();
                frameID = "GLAT";
            }
            else if (canMsgList.get(i).getMessageDesc().equals("Longitude")){
                tmplon= canMsgList.get(i).getDecodedVal();
                frameID = "GLNG";
            }
            
            if (canMsgList.get(i).getMessageDesc().equals(yawRate.getDataDesc())) {

                Double  tmp=canMsgList.get(i).getDecodedVal();
                tmp2= getcurve(tmp);
  
                if (tmp2==1|tmp2==-1|tmp2==2|tmp2==-2){
                    iscurve=true;
                }
                else if (tmp2 == 0){
                    iscurve = false;
                }
                
                
            }
            if(iscurve == true){
                startspeed = tmpspeed;
                counter++;

            }
            if (iscurve==false) {
               endspeed=tmpspeed ;
               avgspeed=(startspeed+endspeed)/2;
               curves.add(new Curveobj(tmplat, tmplon, tmp2 , avgspeed));
            }
            
            if (tmp2 == 1){
                System.out.println("curve Warning :low speed left curve" +" |lat-"+tmplat+" |long-"+tmplon+" |curve "+tmp2+" |speed "+avgspeed);
                
            }
            else if (tmp2 == -1 ){
                System.out.println("curve Warning :low speed right curve"+" |lat-"+tmplat+" |long "+tmplon+" |curve-"+tmp2+" |speed-"+avgspeed);
                
            }
            else if (tmp2 == 2) {
        
                System.out.println("curve Warning :High speed left curve"+" |lat-"+tmplat+" |long-"+tmplon+" |curve-"+tmp2+" |speed-"+avgspeed);
                
            }
            else if (tmp2 == -2) {
                
                System.out.println("curve Warning :High speed right curve"+" |lat"+tmplat+" |long"+tmplon+" |curve"+tmp2+" |speed"+avgspeed);
            }

            //System.out.printf("%-25.1f %-25s %-5.2f %s\n", canMsgList.get(i).getTimeOffset(),
               //               frameID, canMsgList.get(i).getDecodedVal(), canMsgList.get(i).getMessageDesc());
              
        }
        System.out.println(curves.size());
    }
}
