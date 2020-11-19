/* SER 540/494 - Assignment 2: Interpreting CAN Data
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
import java.util.HashMap;
import java.util.Map;

public class CANDataWriter {
    /* Hashmap used to contain all the messages in "CANmessages.trc" which have
     * a frame ID that matches one of the IDs listed in "CAN Frames Info.txt".
     */
    private static HashMap<Double, CANmessage> canMsgMap;
    //Buffered reader used to read in "CANmessages.trc" and "GPS Track.htm".
    private static BufferedReader br;
    //CANframes used to query data.
    private static CANframe wheelAngle, displaySpeed, yawRate, longAcc, latAcc;
    public static void main(String args[]) throws IOException {
        //Initializing canMsgMap.
        canMsgMap = new HashMap<Double, CANmessage>();

        /* All values for the next 5 CANframe objects have been obtained from 
         * the "CAN Frames Info.txt" file.
         */
        //Constructing CANframe obj of the steering wheel angle.
        byte[] wheelByteRange = {1, 5, 2, 0};
        double[] wheelValRange = {-2048.0, 2047.0};
        wheelAngle = new CANframe("0003", wheelByteRange, (byte) 14, 
            "Steering wheel angle", 0x3FFF, "°",wheelValRange, 0.5);

        //Constructing CANframe obj of the displayed vehicle speed.
        byte[] speedByteRange = {1, 3, 2, 0};
        double[] speedValRange = {0.0, 409.4};
        displaySpeed = new CANframe("019F", speedByteRange, (byte) 12, 
            "Displayed vehicle speed", 0xFFF, " km/h", speedValRange, 0.1);

        //Constructing CANframe obj of the vehicle yaw rate.
        byte[] yawByteRange = {1, 7, 2, 0};
        double[] yawValRange = {-327.68, 327.66};
        yawRate = new CANframe("0245", yawByteRange, (byte) 16, 
            "Vehicle yaw rate", 0xFFFF, "°/s", yawValRange, 0.01);

        //Constructing CANframe obj of the vehicle longitudinal acceleration.
        byte[] longByteRange = {5, 7, 5, 0};
        double[] longValRange = {-10.24, 10.08};
        longAcc = new CANframe("0245", longByteRange, (byte) 8, 
            "Vehicle longitudinal acceleration", 0xFF, " m/s²", longValRange, 0.08);

        //Constructing CANframe obj of the vehicle lateral acceleration.
        byte[] latByteRange = {6, 7, 6, 0};
        double[] latValRange = {-10.24, 10.08};
        latAcc = new CANframe("0245", latByteRange, (byte) 8, 
            "Vehicle longitudinal acceleration", 0xFF, " m/s²", latValRange, 0.08);

        //Retrieving the locations of the files to read from args.
        String canMessagesFile = args[0];
        //String gpsTrackFile = args[1];

        //Reading in CANmessages.trc and filling canMsgMap with relevant data.
        readCanMessages(canMessagesFile);

        //Printing all the messages in canMsgMap.
        printData();
    }

    static void readCanMessages(String msgFile) throws IOException {
        try {
            br = new BufferedReader(new FileReader(msgFile));
            String line = null; //String used to represent each line.
            String[] lineArr; //Array used to parse values from line.

            line = br.readLine();
            while (line.charAt(0) == ';') {
                    line = br.readLine();
            }

            while ((line = br.readLine()) != null) {
                lineArr = line.trim().split("\\s+"); //Parsing file line into array.
                //Checking if the frame ID matches one of the 3 relevant ones.
                if(lineArr[3].equals(wheelAngle.getFrameID())) { //Frame ID equates to wheel angle.
                    CANmessage wheelMessage = messageBuilder(lineArr, wheelAngle); //Create wheel message.
                    canMsgMap.put(wheelMessage.getTimeOffset(), wheelMessage); //Put message in hashmap.
                } else if (lineArr[3].equals(displaySpeed.getFrameID())) { //Same as before but for speed.
                    CANmessage speedMessage = messageBuilder(lineArr, displaySpeed);
                    canMsgMap.put(speedMessage.getTimeOffset(), speedMessage);
                } else if (lineArr[3].equals(yawRate.getFrameID())) { //Same as before but for the remaining 3.
                    CANmessage coordMessage = messageBuilder(lineArr, yawRate);
                    canMsgMap.put(coordMessage.getTimeOffset(), coordMessage);
                }
            }

        } catch (FileNotFoundException e) { //Handles filenotfoundexception if the given file directory cannot be found.
            System.out.println("Error: Unable to locate file: " + msgFile);
        } finally {
            br.close(); //Closing buffered reader.
        }
    }

    /* Method which constructs message objects by parsing through the data and
     * obtaining the decoded values. 
     */
    static CANmessage messageBuilder(String[] lineArr, CANframe frame) {
        CANmessage message = new CANmessage();
        message.setTimeOffset(Double.parseDouble(lineArr[1]));
        message.setFrameID(lineArr[3]);

        int dataLength = Integer.parseInt(lineArr[4]);
        String[] data = Arrays.copyOfRange(lineArr, 5, 5 + dataLength);
        message.setRawData(data);

        int[] decodedValues;
        String[] dataStr;
        if (frame.getFrameID().equals("0245")) {
            //Ordered as yaw rate, long acceleration and lat acceleration.
            decodedValues = new int[3];
            decodedValues[0] = convertRawToInt(data, yawRate.getDataLocation());
            decodedValues[1] = convertRawToInt(data, longAcc.getDataLocation());
            decodedValues[2] = convertRawToInt(data, latAcc.getDataLocation());

            dataStr = new String[3]; 
            dataStr[0] = convertIntToString(decodedValues[0], yawRate);
            dataStr[1] = convertIntToString(decodedValues[1], longAcc);
            dataStr[2] = convertIntToString(decodedValues[2], latAcc);
        } else {
            decodedValues = new int[1];
            decodedValues[0] = convertRawToInt(data, frame.getDataLocation());

            dataStr = new String[1]; 
            dataStr[0] = convertIntToString(decodedValues[0], frame);
        }
        message.setDecodedVal(decodedValues);
        message.setDataString(dataStr);

        return message;
    }

    /* Method which converts String data into int data which can be used for
     * obtaining the decoded values. 
     */
    static int convertRawToInt(String[] rawData, byte[] dataLocation) {
        int startByte = dataLocation[0] - 1;
        int endByte = dataLocation[2];
        rawData = Arrays.copyOfRange(rawData, startByte, endByte);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rawData.length; i++) {
            sb.append(rawData[i]);
        }
        int targetData = Integer.parseInt(sb.toString(), 16);
        int leftBitMask = ~(-1 << (dataLocation[1] + 1));
        targetData = targetData & leftBitMask;
        targetData = targetData >>> dataLocation[3];
        targetData = targetData << dataLocation[3];
        return targetData;
    }

    /* Method which converts int data values into readable string data with the
     * corresponding units of measurement. 
     */
    static String convertIntToString(int value, CANframe frame) {
        double realVal = value * frame.getStepSize() + frame.getValRange()[0];
        String msg = frame.getDataDesc() + ": " + 
                     String.format("%.2f", realVal) + 
                     frame.getUnitType();
        return msg;
    }

    //Unfinished method.
    //static void readGpsTrack(String gpsFile) {
    //}

    /* Method which prints the table containing the values of the data.
     */
    static void printData() {
        StringBuilder sb = new StringBuilder();
        sb.append("Time offset             |");
        sb.append("Frame ID               ");
        sb.append("|Data\n");
        sb.append("------------------------+");
        sb.append("------------------------");
        sb.append("+------------------------");
        System.out.println(sb.toString());

        for(Map.Entry<Double, CANmessage> e : canMsgMap.entrySet()) {
            String[] dataArr = e.getValue().getDataString();
            String data = "";
            if (dataArr.length > 1) {
                data = String.join(",     ", dataArr);
            } else {
                data = dataArr[0];
            }
            System.out.printf("%-25.1f %-25s %-25s\n", e.getKey(),
                              e.getValue().getFrameID(), data);
        }
    }
}
