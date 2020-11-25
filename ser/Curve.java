public class Curve {
    private double[] gpsStart; //Represents the starting point of the curve.
    private double[] gpsEnd; //Represents the end point of the curve.
    private String curveDirection; //Represents curve direction (left or right).
    private String curveSpeed; //Represents curve speed (fast or slow).
    private double avgSpeed; //Represents the average speed of the curve.
  
    //Getter and setter for gpsStart.
    public void setGpsStart(double[] gpsStart) {
        this.gpsStart = gpsStart;
    }
    public double[] getGpsStart() {
        return gpsStart;
    }
    //Checks if gpsStart is null to know if the curve has started.
    public boolean curveStarted() {
        boolean curveStarted = false;
        if (gpsStart != null && gpsStart.length != 0) {
            curveStarted = true;
        }
        return curveStarted;
    }

    //Getter and setter for gpsEnd.
    public void setGpsEnd(double[] gpsEnd) {
        this.gpsEnd = gpsEnd;
    }
    public double[] getGpsEnd() {
        return gpsEnd;
    }

    //Getter and setter for curveDirection.
    public void setCurveDirection(String curveDirection) {
        this.curveDirection = curveDirection;
    }
    public String getCurveDirection() {
        return curveDirection;
    }

    //Getter and setter for curveSpeed.
    public void setCurveSpeed(String curveSpeed) {
        this.curveSpeed = curveSpeed;
    }
    public String getCurveSpeed() {
        return curveSpeed;
    }

    //Getter and setter for avgSpeed.
    public void setAvgSpeed(double avgSpeed) {
        this.avgSpeed = avgSpeed;
    }
    public double getAvgSpeed() {
        return avgSpeed;
    }
}
