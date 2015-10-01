/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



/**
 *
 * @author zaeem
 */
public class emulator {
    
    public ArrayList<String> readInstructions(String fileName) throws IOException{
        ArrayList<String> instructions=new ArrayList<String>();
        try{
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            int instructionCounter=0;
            Map<String, Integer> branchLabels=new HashMap<String, Integer>();
            String line = br.readLine();
            while(!line.isEmpty() && !line.equals("DATA")){
                if(line.contains(":")){
                    branchLabels.put(line.substring(0,line.indexOf(":")),instructionCounter);
                    instructions.add(line.replaceAll(".*?:", ""));
                }
                else{
                    instructions.add(line);
                }
                instructionCounter++;
                line = br.readLine();
            } 
            for(int i=0;i<instructions.size();i++){
                instructions.set(i, instructions.get(i).trim());
                if(instructions.get(i).startsWith("B")){
                    String[] subInsts=instructions.get(i).split(",");
                    int targetAddr=branchLabels.get(subInsts[subInsts.length-1]);
                    String tmpInst="";
                    for(int j=0;j<(subInsts.length-1);j++){
                        tmpInst+=subInsts[j]+",";
                    }
                    tmpInst+=targetAddr;
                    instructions.set(i,tmpInst);
                }
            }
        }catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	}
        return instructions;
        
    }
    
    public Map<Integer,Float> readData(String fileName) throws IOException{
        Map<Integer,Float> memory=new HashMap<Integer,Float>();
        try{
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            int instructionCounter=0;
            String line = br.readLine();
            while(!line.equals("DATA")){
                line = br.readLine();
            } 
            line = br.readLine();
            
            while(line!=null){
                String addressString=line.substring(line.indexOf("(")+1, line.indexOf(")"));
                int address=Integer.parseInt(addressString);
                String[] splitStrings=line.split("=");
                float value=Float.parseFloat(splitStrings[splitStrings.length-1]);
                memory.put(address, value);
                line = br.readLine();
            }
        }catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	}
        return memory;
        
    }
    
}
