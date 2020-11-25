import java.util.ArrayList;
import java.util.Arrays;

/* This class implements the simulation portion of the project, in which once
 * all of the data has been read in, this class will simulate reading the data
 * in at real time. 
 */
public class Simulator {

    //Contains all of the CAN messages collected from CANDataReader.
    private CANmessage[] msgArr;
    /*Used to indicate the CANmessage from msgArr to be read on any given 
      iteration.*/
    private int pointer;
    /*Contains strings of the most recent values obtained from the msgArr for 
      each CAN sensor.*/
    private String[] canValues;
    //Contains all of the curves detected from the first run of the simulator.
    private ArrayList<Curve> curveList;

    public Simulator(CANmessage[] msgArr) {
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
        curveList = new ArrayList<>();
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

        //Used to keep track of most recent curve.
        Curve currentCurve = new Curve();
        //Represents if the data currently being read is during a curve.
        boolean isCurving = false;
        //Represents the last set of coordinates.
        double[] gpsCoords = new double[2];
        /*Keeps track of the number of times speed was recorded during the curve
          so as to calculate the average speed at the end of the curve.*/
        int numOfSpeeds = 0;


        //Loop until the end of CAN values is reached.
        while (pointer < msgArr.length) {
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

                        /*If the absolute value of the yaw rate is greater than 
                          or equal to 2, then the car is curving.*/
                        if (tempVal > 2 || tempVal < -2) {
                            //Set isCurving to true.
                            isCurving = true;
                            //If this is the first time the car is curving.
                            if (!(currentCurve.curveStarted())) {
                                //Add the starting point of the curve.
                                currentCurve.setGpsStart(gpsCoords);
                                //Set curve speed to fast.
                                currentCurve.setCurveSpeed("Fast");
                                //If yaw rate is positive.
                                if (tempVal > 0) {
                                    //Then curve is going left.
                                    currentCurve.setCurveDirection("left");
                                } else {
                                    //Then curve is going right.
                                    currentCurve.setCurveDirection("right");
                                }
                            }
                            /*If the absolute value of the yaw rate is greater
                              than or equal to 13, then the curve is slow.*/
                            if (currentCurve.getCurveSpeed().equals("Fast") &&
                                (tempVal > 13 || tempVal < -13)) {
                                    currentCurve.setCurveSpeed("Slow");
                                }
                        } else {
                            isCurving = false;
                        }
                        
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
                        //Storing latitude in gpsCoords.
                        gpsCoords[0] = tempVal;
                        //Getting value for GPS longitude.
                        tempVal = msgArr[pointer].getDecodedVal();
                        //Adding GPS longitude value as string.
                        updateCanValues(6, 6, tempVal);
                        //Storing longitude in gpsCoords.
                        gpsCoords[1] = tempVal;
                    }

                //Else if time offset equates to steering angle.
                } else if (tempMsg.getMessageDesc().equals("Steering wheel angle")) {
                    updateCanValues(0, 2, tempVal);

                //Else time offset equates to vehicle speed.
                } else {
                    updateCanValues(1, 2, tempVal);
                    if (isCurving) {
                        double newAvg = currentCurve.getAvgSpeed() + tempVal;
                        currentCurve.setAvgSpeed(newAvg);
                        numOfSpeeds++;
                    }
                }
            }

            //If currently on curve.
            if (isCurving) {
                sb.append("Processing curve...\n");
            //If curve has ended.
            } else if (currentCurve.curveStarted()) {
                //Calculate the average speed of the curve.
                double avgSpeed = currentCurve.getAvgSpeed() / numOfSpeeds;
                //Set curve's average speed.
                currentCurve.setAvgSpeed(avgSpeed);
                //Set the end point of the curve.
                currentCurve.setGpsEnd(gpsCoords);
                //Print out values for the curve.
                sb.append("Curve recorded! Speed: " + 
                          currentCurve.getCurveSpeed() + ", Direction: " + 
                          currentCurve.getCurveDirection() +
                          ", Average speed: " + currentCurve.getAvgSpeed() + 
                          ", Start point: " + 
                          Arrays.toString(currentCurve.getGpsStart()) + 
                          ", End point: " + 
                          Arrays.toString(currentCurve.getGpsEnd()) + "\n\n");
                //Add currentCurve to the curveList.
                curveList.add(currentCurve);
                //Create new curve to represent the currentCurve.
                currentCurve = new Curve();

            //If not on curve.
            } else {
                sb.append("No curve detected.\n");
            }

            //Appending the header to the StringBuilder.
            sb.append("Current Time | Steering Angle | Vehicle Speed |"); 
            sb.append("    Yaw Rate    |  Lateral Acc  |");
            sb.append(" Longitudinal Acc | GPS Lat, Long\n");
            //Appending all CAN values to the StringBuilder.
            sb.append(String.format("%9.1f ms |", timeOffset));
            sb.append(String.format("%11s deg |", canValues[0]));
            sb.append(String.format("%9s km/h |", canValues[1]));
            sb.append(String.format("%9s deg/s |", canValues[2]));
            sb.append(String.format("%8s m/s^2 |", canValues[3]));
            sb.append(String.format("%11s m/s^2 |", canValues[4]));
            sb.append(String.format(" (%s, %s)", canValues[5], canValues[6]));
            sb.append("\r");
            //Printing out the CAN values.
            System.out.print(sb.toString());

            //Getting current system time.
            currentTime = System.nanoTime();
            //Setting new time offset, rounded up to a single decimal.
            timeOffset = Math.round((currentTime - startTime) / 100000.0) / 10.0;
        }
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
