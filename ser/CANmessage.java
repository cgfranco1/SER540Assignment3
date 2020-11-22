/* This class is used as an object containing the various fields of a single
 * CAN message which will be used for the final print out of data.
 */

public class CANmessage {
    private double timeOffset; //Time offset in ms of the message.
    private String messageDesc; //Description of the message.
    private double decodedVal; //The int values of what is decoded from rawData.

    //Setter and getter for timeOffset.
    public void setTimeOffset(double timeOffset){
        this.timeOffset = timeOffset;
    }
    public double getTimeOffset(){
        return timeOffset;
    }

    //Setter and getter for messageDesc.
    public void setMessageDesc(String messageDesc){
        this.messageDesc = messageDesc;
    }
    public String getMessageDesc(){
        return messageDesc;
    }

    //Setter and getter for decodedVal.
    public void setDecodedVal(double decodedVal){
        this.decodedVal = decodedVal;
    }
    public double getDecodedVal(){
        return decodedVal;
    }
}

