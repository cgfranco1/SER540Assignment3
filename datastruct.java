import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

class datastruct {
    //my basic data structure with all the variables to store the data with 

    public double speed=0;
    public double wheel_angle=0;
    public double yaw=0;            // basic inits beacuse not all the data has a variable from the can bus 
    public double toffset=0;
    public String gpslong=" ";
    public String gpslatit=" ";
    public double gpslataccl= 0;
    public double gpslongaccl= 0;
    public String ID = " ";

    public String getID() {//get id 
        return this.ID;
    }

    public void setID(String ID) { //set id 
        this.ID = ID;
    }

    public double getGpslataccl() { // get gpslataccl
        return this.gpslataccl;
    }

    public void setGpslataccl(double gpslataccl) { // set gpslataccl
        this.gpslataccl = gpslataccl;
    }

    public double getGpslongaccl() { //get gpslongaccl
        return this.gpslongaccl;
    }

    public void setGpslongaccl(double gpslongaccl) { //set gpslongaccl
        this.gpslongaccl = gpslongaccl;
    }


    public double getSpeed() { // get speed
        return this.speed;
    }

    public void setSpeed(double speed) { //set speed 
        this.speed = speed;
    }

    public double getWheel_angle() { // get wheel angle 
        return this.wheel_angle;
    }

    public void setWheel_angle(double wheel_angle) { //set wheel angle 
        this.wheel_angle = wheel_angle;
    }

    public double getYaw() {// get yaw 
        return this.yaw;
    }

    public void setYaw(double yaw) {//set yaw 
        this.yaw = yaw;
    }

    public double getToffset() { // get time offset 
        return this.toffset;
    }

    public void setToffset(double toffset) { //set  time offset 
        this.toffset = toffset;
    }

    public String getGpslong() {
        return this.gpslong;    // get gps longitude 
    }

    public void setGpslong(String gpslong) {
        this.gpslong = gpslong; // set gps longitude
    }

    public String getGpslatit() { // get gps latitude
        return this.gpslatit;
    }

    public void setGpslatit(String gpslatit) {  // set gps latitude 
        this.gpslatit = gpslatit;
    }

    
    public datastruct(String[] x) { // basic constuctor to init data set 

   setToffset(Double.parseDouble(x[2])); // time offset 

   if (x[4].contains("0003")){  // for wheel angle
        setID("0003");// set id
        String stemp = x[6]+x[7]; //first two hex bytes 
        String number = h2b(stemp); //convert to binary 
        String bits = number.substring(2, 15); // get right bits 
        setWheel_angle(Integer.parseInt(bits,2) * 0.5 - 2048); //set var

    }
    else if (x[4].contains("019F")){ // for speed
        setID("019F");// set id
        String stemp = x[6]+x[7];//first two hex bytes
        String number = h2b(stemp);//convert to binary 
        String bits = number.substring(4, 15);// get right bits 
        setSpeed(Integer.parseInt(bits,2) * 0.1); //set var
    }
    else if (x[4].contains("0245")){ //yaw rate 
        setID("0245");// set id
        String stemp = x[6]+x[7];//first two hex bytes
        String number = h2b(stemp);//convert to binary 
        String bits = number.substring(0, 15);// get right bits 
        setYaw(Integer.parseInt(bits,2)* 0.01 - 327.68);//set var

         stemp = x[10]; //first  hex byte
         number = h2b(stemp);//convert to binary 
         bits = number.substring(0, 7);// get right bits 
         setGpslongaccl(Integer.parseInt(bits,2)* 0.08 - 10.24);

         stemp = x[11];  //first  hex byte
         number = h2b(stemp);//convert to binary 
         bits = number.substring(0, 7);// get right bits 
         setGpslataccl(Integer.parseInt(bits,2)* 0.08 - 10.24);//set var
     }
     
}
public void update(String[] x){
    if (x[4].contains("0003")){  // for wheel angle
        setID("0003");// set id
        String stemp = x[6]+x[7]; //first two hex bytes 
        String number = h2b(stemp); //convert to binary 
        String bits = number.substring(2, 15); // get right bits 
        setWheel_angle(Integer.parseInt(bits,2) * 0.5 - 2048); //set var

    }
    else if (x[4].contains("019F")){ // for speed
        setID("019F");// set id
        String stemp = x[6]+x[7];//first two hex bytes
        String number = h2b(stemp);//convert to binary 
        String bits = number.substring(4, 15);// get right bits 
        setSpeed(Integer.parseInt(bits,2) * 0.1); //set var
    }
    else if (x[4].contains("0245")){ //yaw rate 
        setID("0245");// set id
        String stemp = x[6]+x[7];//first two hex bytes
        String number = h2b(stemp);//convert to binary 
        String bits = number.substring(0, 15);// get right bits 
        setYaw(Integer.parseInt(bits,2)* 0.01 - 327.68);//set var

         stemp = x[10]; //first  hex byte
         number = h2b(stemp);//convert to binary 
         bits = number.substring(0, 7);// get right bits 
         setGpslongaccl(Integer.parseInt(bits,2)* 0.08 - 10.24);

         stemp = x[11];  //first  hex byte
         number = h2b(stemp);//convert to binary 
         bits = number.substring(0, 7);// get right bits 
         setGpslataccl(Integer.parseInt(bits,2)* 0.08 - 10.24);//set var
     }
}
    public String toString() { 
    return "time offset- "+toffset+" | "+"ID-"+ID+" | "+"Speed- "+speed+" | "+"Wheel angle- "+wheel_angle+" | "+"yaw- "+yaw+" | "+ //to string 
    "Lat- "+gpslatit+" | "+"Long- "+gpslong+" | "+"lat_accel-"+gpslataccl+" | "+"long_accel-"+gpslongaccl+" |";
    }
    public static String h2b(String number) {  // hex to binary converter
        
        String c, tmp = "";  //tmp varable 
        for(int i = 0; i < number.length(); i++) {   ///basic switch to change hex to binary 
            c = number.substring(i, i + 1); 
            switch (c) {
                case "0": tmp += "0000"; break;
                case "1": tmp += "0001"; break;
                case "2": tmp += "0010"; break;
                case "3": tmp += "0011"; break;
                case "4": tmp += "0100"; break; // basically checking the hexadecimal digit and choosing the approiate conversion 
                case "5": tmp += "0101"; break;
                case "6": tmp += "0110"; break;
                case "7": tmp += "0111"; break;
                case "8": tmp += "1000"; break;
                case "9": tmp += "1001"; break;
                case "A": tmp += "1010"; break;
                case "B": tmp += "1011"; break;
                case "C": tmp += "1100"; break;
                case "D": tmp += "1101"; break;
                case "E": tmp += "1110"; break;
                case "F": tmp += "1111"; break;
                default:
                    break;
            }
        }
        return tmp; // returns binary 
    }
}