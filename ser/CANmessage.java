/* This class is used as an object containing the various fields of a single
 * CAN message which will be used for the final print out of data.
 */

public class CANmessage {
    private double timeOffset; //Time offset in ms of the message.
    private String frameID; //Frame ID of the message.
    private String[] rawData; //Hex data bytes as a String array.
    private int[] decodedVal; //The int values of what is decoded from rawData.
    private String[] dataString; /* The decodedVal as strings with their
                                * corresponding value and measurement unit.*/
    private double[] latLong; //The GPS coordinates obtained from "GPS Track.htm"

    //Setter and getter for timeOffset.
    public void setTimeOffset(double timeOffset){
        this.timeOffset = timeOffset;
    }
    public double getTimeOffset(){
        return timeOffset;
    }

    //Setter and getter for frameID.
    public void setFrameID(String frameID){
        this.frameID = frameID;
    }
    public String getFrameID(){
        return frameID;
    }

    //Setter and getter for rawData.
    public void setRawData(String[] rawData){
        this.rawData = rawData;
    }
    public String[] getRawData(){
        return rawData;
    }

    //Setter and getter for decodedVal.
    public void setDecodedVal(int[] decodedVal){
        this.decodedVal = decodedVal;
    }
    public int[] getDecodedVal(){
        return decodedVal;
    }

    //Setter and getter for dataString.
    public void setDataString(String[] dataString){
        this.dataString = dataString;
    }
    public String[] getDataString(){
        return dataString;
    }

    //Setter and getter for latLong.
    public void setLatLong(double[] latLong){
        this.latLong = latLong;
    }
    public double[] getLatLong(){
        return latLong;
    }
}

