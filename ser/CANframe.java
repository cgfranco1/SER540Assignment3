/* This class is used to represent the five types of CAN frames that are
 * specified in "CAN Frames Info.txt".
 */

public class CANframe {
    private String frameID; //The hex frame ID stored in an int.
    private byte[] dataLocation; //The start and end bytes/bits stored in an
                                 //array formatted as: [By1, Bi5, By2, Bi0]
    private byte dataSize; //The bit length of the data to be interpreted.
    private String dataDesc; //The written description of what the data means.
    private int maxVal; //The maximum hex value the data can have in an int.
    private String unitType; //The measurement unit that the decoded data uses.
    private double[] valRange; //The range of values for the decoded data.
    private double stepSize; //Stepsize value used to decode data.

    /*Constructor takes in all values such that no set methods are ever used.
    I realize that conventionally this is a bloated constructor but I believe
    that for the purposes of simply creating 5 static objects there is no need
    to have setters.
    */
    public CANframe(String frameID, byte[] dataLocation, byte dataSize, 
                    String dataDesc, int maxVal, String unitType, 
                    double[] valRange, double stepSize) {
        this.frameID = frameID;
        this.dataLocation = dataLocation;
        this.dataSize = dataSize;
        this.dataDesc = dataDesc;
        this.maxVal = maxVal;
        this.unitType = unitType;
        this.valRange = valRange;
        this.stepSize = stepSize;
    }

    //Getter for frameID.
    public String getFrameID() {
        return frameID;
    }

    //Getter for dataLocation.
    public byte[] getDataLocation() {
        return dataLocation;
    }

    //Getter for dataSize.
    public byte getDataSize() {
        return dataSize;
    }

    //Getter for dataDesc.
    public String getDataDesc() {
        return dataDesc;
    }

    //Getter for maxVal.
    public int getMaxVal() {
        return maxVal;
    }

    //Getter for unitType.
    public String getUnitType() {
        return unitType;
    }

    //Getter for valRange.
    public double[] getValRange() {
        return valRange;
    }

    //Getter for stepSize.
    public double getStepSize() {
        return stepSize;
    }
}