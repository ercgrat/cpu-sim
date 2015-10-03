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
		if(args.length < 1) {
			System.out.println("Too few arguments. Run with input file on command line:\n\njava simulator.Simulator <input_file>");
			return;
		}
		String filename = args[0];
		emulator Emulator = new emulator();
		ArrayList<String> instructions = Emulator.readInstructions(filename);
		Map<Integer,Float> memory = Emulator.readData(filename);     
		
		// Just demonstrating the Instruction class
		for(String inst : instructions) {
			Instruction test = new Instruction(inst);
			System.out.println(test);
		}
	}
}
