/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author zaeem
 */
public class Simulator {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        emulator Emulator=new emulator();
        ArrayList<String> instructions=Emulator.readInstructions("prog.dat");
        Map<Integer,Float> memory=Emulator.readData("prog.dat");
        System.out.println(String.valueOf(memory.get(200)));
        
    }
}
