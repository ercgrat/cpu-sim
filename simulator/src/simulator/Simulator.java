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
        
        int NF = 4, NQ = 8, ND = 4, NI = 8, NW = 4, NR = 4, NC = 4;
        
		Emulator emulator = new Emulator();
		ArrayList<String> instructions = emulator.readInstructions(filename);
		Map<Integer,Float> memory = emulator.readData(filename);
        
        RegisterFile<Integer> intRegisters = new RegisterFile<Integer>(0);
        RegisterFile<Float> floatRegisters = new RegisterFile<Float>((float)0);
        Scoreboard scoreboard = new Scoreboard(intRegisters, floatRegisters);
		
        FetchUnit fetchUnit = new FetchUnit(NF, NQ, instructions);
        DecodeUnit decodeUnit = new DecodeUnit(ND, NI, fetchUnit);
        
        ReservationStations reservationStations = new ReservationStations(scoreboard);
        ReorderBuffer reorderBuffer = new ReorderBuffer(NR, NC, intRegisters, floatRegisters, reservationStations, scoreboard, memory);
        reservationStations.reorderBufferInit(reorderBuffer);
        IssueUnit issueUnit = new IssueUnit(NW, decodeUnit, reservationStations, reorderBuffer, scoreboard);
        
        IntUnits intUnits = new IntUnits(reservationStations, reorderBuffer);
        FPUnit fpUnit = new FPUnit(reservationStations, reorderBuffer);
        MULTUnit multUnit = new MULTUnit(reservationStations, reorderBuffer);
        LoadStoreUnit lsUnit = new LoadStoreUnit(reservationStations, reorderBuffer, memory);
        BranchUnit branchUnit = new BranchUnit(reservationStations, reorderBuffer);
        
        int cycles = 0;
        while(true) {
            cycles++;
            System.out.println("----------Cycle " + cycles + "----------");
            
            
            // writeback & commit
            System.out.println("*****Writeback & Commit");
            reorderBuffer.cycle();
            
            // execution
            System.out.println("*****Execution");
            intUnits.cycle();
            System.out.println("*****Execution");
            fpUnit.cycle();
            System.out.println("*****Execution");
            multUnit.cycle();
            System.out.println("*****Execution");
            lsUnit.cycle();
            System.out.println("*****Execution");
            branchUnit.cycle();
            System.out.println("*****Execution");
            reorderBuffer.stageCommits(); // necessary for prioritizing writeback from execution units
            
            // issue
            System.out.println("*****Issue");
            issueUnit.cycle();
            
            // decode
            System.out.println("*****Decode");
            decodeUnit.cycle();
            
            // fetch
            System.out.println("*****Fetch");
            fetchUnit.cycle();
            
            if(cycles == 1000) {
                break;
            }
        }
	}
}
