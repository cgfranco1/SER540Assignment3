import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;  //imports that I used 

public class ser540_assign2 {

    public static Hashtable<Double, datastruct> mydata = new Hashtable<Double, datastruct>(); // my data set oraginzed by time offset 
    // easy lookup by time offset 

    public static void main(String arg[]) {

        BufferedReader streamin1; //  2 buffered readers to read in file can messages
        BufferedReader streamin2;
        String [] temp = new String [15];  // temp array when i have a important line to look through 
        try { // try block
            streamin2 = new BufferedReader(new FileReader("c:/Users/Jon/Downloads/19 GPS Track.htm")); 
            // path to file to read 
                                                                                                                   
            streamin1 = new BufferedReader(new FileReader("c:/Users/Jon/Downloads/19 CANmessages.trc"));
            //path to other file to read 
            
            String current = streamin1.readLine(); // read line
            

            while (current!=null) { // reading file
                    temp = current.split("\\s+"); // split the line into an array 

                if (current.contains("0003") | current.contains("019F") | current.contains(" 0245")){
                    datastruct x= new datastruct(temp); // this is if the data for the offset doesent exist yet
                    mydata.put(x.toffset, x);
                }
                current = streamin1.readLine(); // reading next line  
            }
            
        }catch (IOException e) { // catch blick to catch error in reading file
            e.printStackTrace();
        }
        Iterator<Double> it = mydata.keySet().iterator();  // prints out the hashtable using keys and using my to sting to format for easy reading 
        while(it.hasNext()){
            System.out.println(mydata.get(it.next()).toString());
        }  //end of main 
    }
}

