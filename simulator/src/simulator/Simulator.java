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
        
        int NF = 4, NQ = 8, ND = 4, NI = 8, NW = 4, NR = 32, NC = 4;
        
		Emulator emulator = new Emulator();
		ArrayList<String> instructions = emulator.readInstructions(filename);
		Map<Integer,Float> memory = emulator.readData(filename);
        /*for(Map.Entry entry: memory.entrySet())
            System.out.println("Mem("+entry.getKey()+") = "+entry.getValue());*/
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
        Instruction branchInstruction = null;
        int cycles = 0;
        int countdown = 50;
        boolean readingInstructions = true;
        while(true) {
            cycles++;
            //System.out.println("----------Cycle " + cycles + "----------");
            
            
            // writeback & commit
            //System.out.println("*****Writeback & Commit");
            reorderBuffer.cycle();
            
            // execution
            //System.out.println("*****Execution INT");
            intUnits.cycle();
            //System.out.println("*****Execution FPU");
            fpUnit.cycle();
            //System.out.println("*****Execution MULT");
            multUnit.cycle();
            //System.out.println("*****Execution LOAD/STORE");
            lsUnit.cycle();
            //System.out.println("*****Execution BRANCH");
            branchInstruction = branchUnit.cycle();
            //System.out.println("Branch outcome is:" + branchInstruction);
            //System.out.println("*****Stage Commits");
            reorderBuffer.stageCommits(); // necessary for prioritizing writeback from execution units
            if(branchInstruction != null){
                //System.out.println("*****Branch - flush pipeline");
                if(!reorderBuffer.isSetToBeFlushed(branchInstruction)) {
                    reorderBuffer.flush(branchInstruction);
                    decodeUnit.flush();
                    fetchUnit.flush(branchInstruction);
                }
            }
            //System.out.println("*****Reservation Stations");
            reservationStations.cycle();
            // issue
            //System.out.println("*****Issue");
            if(branchInstruction == null){
                issueUnit.cycle();
            
                // decode
                //System.out.println("*****Decode");
                decodeUnit.cycle();
            
                // fetch
                //System.out.println("*****Fetch");
                readingInstructions = fetchUnit.cycle();
            }
            scoreboard.cycle();
            if(readingInstructions) {
                countdown--;
                if(countdown == 0) {
                    break;
                }
            }
        }
        System.out.println("Total number of cycles: " + cycles);
        System.out.println("Int registers: " + intRegisters);
	System.out.println("Float registers: " + floatRegisters);
        for(Map.Entry entry: memory.entrySet())
            System.out.println("Mem("+entry.getKey()+") = "+entry.getValue());
	}
}
