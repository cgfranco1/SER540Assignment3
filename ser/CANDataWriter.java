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
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Map;

public class CANDataWriter {
    /*Hashmap used to contain all the messages in "CANmessages.trc" which have
      a frame ID that matches one of the IDs listed in "CAN Frames Info.txt".*/
    private static Map<Double, CANmessage> canMsgMap;
    //Array used to contain the GLatLng values obtained from "GPS Track.htm".
    private static ArrayList<String> gpsCoords;
    /*Int used to track what the time offset needs to be to insert the two
      CANmessages which will represent the GPS coordinates.*/
    private static int gpsTimeOffset;

    //CANframes used to query data.
    private static CANframe wheelAngle, displaySpeed, yawRate, latAcc, longAcc;

    //Buffered reader used to read in "CANmessages.trc" and "GPS Track.htm".
    private static BufferedReader br;

    public static void main(String args[]) throws IOException {
        //Initializing canMsgMap and gpsCoords.
        canMsgMap = new LinkedHashMap<Double, CANmessage>();
        gpsCoords = new ArrayList<>();
        /*GPS message insertions will need to be done every thousandth
          milisecond and so this int will be incremented by a 1000 after the
          insertion of lat and long messages.*/
        gpsTimeOffset = 1000;

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
        //String gpsTrackFile = args[0];
        String canMessagesFile = args[1];

        //Reading in "GPS Track.htm" and filling gpsCoords with relevant data.
        //readGpsCoords(gpsTrackFile);
        //Reading in "CANmessages.trc" and filling canMsgMap with relevant data.
        readCanMessages(canMessagesFile);

        //Printing all the messages in canMsgMap.
        //printData();
    }

    /* Method which reads the "CANmessages.trc" file line by line checking for
     * data entries which match one of the three relevant frame IDs. The method
     * will create CANmessage objects for every line that is found with a
     * correct ID and put it into canMsgMap.
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
            line = br.readLine();
            while (line.charAt(0) == ';') {
                    line = br.readLine();
            }

            //Reading the rest of the file.
            while ((line = br.readLine()) != null) {
                //Parsing file line into array.
                lineArr = line.trim().split("\\s+");

                //Checking if the frame ID matches one of the 3 relevant ones.
                //If frame ID equates to wheel angle.
                if(lineArr[3].equals(wheelAngle.getFrameID())) {
                    //Create wheel message and put it in the linked hashmap.
                    CANmessage wheelMessage = messageBuilder(lineArr, 
                                                             wheelAngle);
                    canMsgMap.put(wheelMessage.getTimeOffset(), wheelMessage);
                //If frame ID equates to speed.
                } else if (lineArr[3].equals(displaySpeed.getFrameID())) {
                    //Create speed message and put it in the linked hashmap.
                    CANmessage speedMessage = messageBuilder(lineArr, 
                                                             displaySpeed);
                    canMsgMap.put(speedMessage.getTimeOffset(), speedMessage);
                /*If frame ID equates to yaw rate, as well as longitudinal and 
                  lateral acceleration.*/
                } else if (lineArr[3].equals(yawRate.getFrameID())) {
                    //Create messages for all three.
                    CANmessage yawMessage = messageBuilder(lineArr, yawRate);
                    CANmessage longMessage = messageBuilder(lineArr, longAcc);
                    CANmessage latMessage = messageBuilder(lineArr, latAcc);
                    //Put all three into the linked hashmap.
                    canMsgMap.put(yawMessage.getTimeOffset(), yawMessage);
                    canMsgMap.put(longMessage.getTimeOffset(), yawMessage);
                    canMsgMap.put(latMessage.getTimeOffset(), yawMessage);
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
        if (message.getTimeOffset() > gpsTimeOffset){
            //Creating CANmessages for GPS lateral and longitudinal coordinates.
            CANmessage gLat = new CANmessage();
            CANmessage gLng = new CANmessage();
            //Setting the time offsets equal to the current gpsTimeOffset time.
            gLat.setTimeOffset(gpsTimeOffset);
            gLng.setTimeOffset(gpsTimeOffset);
            //Setting the data descriptions.
            gLat.setMessageDesc("Latitude");
            gLng.setMessageDesc("Longitude");
            //Temporarily setting the GPS values to -1.
            gLat.setDecodedVal(-1.0);
            gLng.setDecodedVal(-1.0);
            //Putting the GPS CANmessages into the linked hashmap.
            canMsgMap.put(gLat.getTimeOffset(), gLat);
            canMsgMap.put(gLng.getTimeOffset(), gLng);
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
        //Bit-shifting exctractedVall left to move bits back into place.
        extractedVal = extractedVal << dataLocation[3];

        //Now converting the extracted int value into the value it represents.
        double convertedVal = extractedVal * frame.getStepSize() + 
                              frame.getValRange()[0];

        return convertedVal;
    }

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
            String frameID = "frameId";
            if (e.getValue().getMessageDesc().equals(wheelAngle.getDataDesc())){
                frameID = wheelAngle.getFrameID();
            }
            else if (e.getValue().getMessageDesc().equals(displaySpeed.getDataDesc())){
                frameID = displaySpeed.getFrameID();
            }
            else if (e.getValue().getMessageDesc().equals(yawRate.getDataDesc()) || 
            e.getValue().getMessageDesc().equals(latAcc.getDataDesc()) || 
            e.getValue().getMessageDesc().equals(longAcc.getDataDesc())){
                frameID = yawRate.getFrameID();
            }
            else if (e.getValue().getMessageDesc().equals("Latitude")){
                frameID = "TEST";
            }
            else if (e.getValue().getMessageDesc().equals("Longitude")){
                frameID = "TEST";
            }
            
            System.out.printf("%-25.1f %-25s %-25.1f\n", e.getKey(),
                              frameID, e.getValue().getDecodedVal());
        }
    }
}
