package com.in;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConvertE2A {
	
	InputStreamReader reader;
	StringBuilder builder;

	public static void main(String[] args) {
		

	}
	
	String readFile(){
		try {
	        reader = new InputStreamReader(new FileInputStream("C:\\TI3\\Legacy Systemen\\Week 3\\Oefening 3\\inputfile.dat"),
	               java.nio.charset.Charset.forName("ibm500") );
	        builder = new StringBuilder();
	        int theInt;
	        int counter =0 ;
		    while((theInt = reader.read()) != -1){
		        char theChar = (char) theInt;
		        builder.append(theChar);
		        
		        if(counter >= 100){
		        	break;
		        }
		        counter++;
		        
		        
		    }
		    reader.close();		    
	    } catch(FileNotFoundException e){
	        e.printStackTrace();
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	    
	    
	    return builder.toString();
	}

}

