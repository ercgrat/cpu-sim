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
        int NF = 4, NQ = 8, ND = 4, NI = 8, NW = 4, NR = 32, NC = 4, NB = 4;
        if(args.length < 1) {
            System.out.println("Too few arguments. Run with input file on command line:\n\njava simulator.Simulator <input_file>");
            return;
	}
        
        String filename = args[args.length-1];
        boolean dumpRegs = false;
        boolean dumpBT = false;
        boolean dumpMem = false;
        int memStart = 0;
        int memEnd = 0;
        if(args.length > 1){
            for(int a = 0; a < args.length-1; a++){
                if(args[a].equals("--dump_regs"))
                    dumpRegs = true;
                if(args[a].equals("--dump_branch"))
                    dumpBT = true;
                if(args[a].equals("--dump_mem")){
                    dumpMem = true;
                    memStart = Integer.valueOf(args[a+1]);
                    memEnd = Integer.valueOf(args[a+2]);
                }
                if(args[a].equals("--NC"))
                    NC = Integer.valueOf(args[a+1]);
                if(args[a].equals("--NR"))
                    NR = Integer.valueOf(args[a+1]);
                if(args[a].equals("--NW"))
                    NW = Integer.valueOf(args[a+1]);
                if(args[a].equals("--NQ"))
                    NQ = Integer.valueOf(args[a+1]);
                if(args[a].equals("--NI"))
                    NI = Integer.valueOf(args[a+1]);
                if(args[a].equals("--ND"))
                    ND = Integer.valueOf(args[a+1]);
                if(args[a].equals("--NF"))
                    NF = Integer.valueOf(args[a+1]);
                if(args[a].equals("--NB"))
                    NB = Integer.valueOf(args[a+1]);
            }
        }        
        
        
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
        ReorderBuffer reorderBuffer = new ReorderBuffer(NR, NB, NC, intRegisters, floatRegisters, reservationStations, scoreboard, memory);
        reservationStations.reorderBufferInit(reorderBuffer);
        IssueUnit issueUnit = new IssueUnit(NW, decodeUnit, reservationStations, reorderBuffer, scoreboard);
        
        IntUnits intUnits = new IntUnits(reservationStations, reorderBuffer);
        FPUnit fpUnit = new FPUnit(reservationStations, reorderBuffer);
        MULTUnit multUnit = new MULTUnit(reservationStations, reorderBuffer);
        LoadStoreUnit lsUnit = new LoadStoreUnit(reservationStations, reorderBuffer, memory);
        BranchUnit branchUnit = new BranchUnit(reservationStations, reorderBuffer);
        Instruction branchInstruction = null;
        int cycles = 0;
        int countdown = 1;
        int robStalls = 0;
        int resStalls = 0;
        int stallType = 0;
        int numCommits = 0;
        int numMispredictions = 0;
        int branches = 0;
        boolean readingInstructions = true;
        while(true) {
            cycles++;
            //System.out.println("----------Cycle " + cycles + "----------");
            
            
            // writeback & commit
            //System.out.println("*****Writeback & Commit");
            branches += reorderBuffer.cycle();
            
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
            numCommits += reorderBuffer.stageCommits(); // necessary for prioritizing writeback from execution units
            if(branchInstruction != null){
                //System.out.println("*****Branch - flush pipeline");
                if(!reorderBuffer.isSetToBeFlushed(branchInstruction)) {
                    reorderBuffer.flush(branchInstruction);
                    decodeUnit.flush();
                    fetchUnit.flush(branchInstruction);
                    numMispredictions += 1;
                }
            }
            //System.out.println("*****Reservation Stations");
            reservationStations.cycle();
            // issue
            //System.out.println("*****Issue");
            if(branchInstruction == null){
                stallType = issueUnit.cycle();
                if(stallType == 1)
                    resStalls += 1;
                if(stallType == 2)
                    robStalls += 1;
                // decode
                //System.out.println("*****Decode");
                decodeUnit.cycle();
            
                // fetch
                //System.out.println("*****Fetch");
                readingInstructions = fetchUnit.cycle();
            }
            scoreboard.cycle();
            if(readingInstructions && reorderBuffer.allFlush() && fetchUnit.isFetchQEmpty() && decodeUnit.isIssueQEmpty()) {
                
                    break;
            
            }
        }
        Float IPC = (float)numCommits/cycles;
        Float mispredictionRate = (float) numMispredictions/branches;
        System.out.println("Total number of cycles: " + cycles);
        System.out.println("IPC: " + IPC);
        System.out.println("Branch prediction miss rate is: " + mispredictionRate);
        System.out.println("Number of stalls due to reservation stations: " + resStalls);
        System.out.println("Number of stalls due to ROB: " + robStalls);
        if(dumpRegs){
            System.out.println("Int registers: " + intRegisters);
            System.out.println("Float registers: " + floatRegisters);
        }
        if(dumpMem){
            for(Map.Entry entry: memory.entrySet()){
                int memAdd = (int) entry.getKey();
                if(memAdd >= memStart && memAdd <= memEnd)
                    System.out.println("Mem("+entry.getKey()+") = "+entry.getValue());
            }
        }    
        if(dumpBT){
            System.out.println("Branch table contents are:");
            System.out.println("Instruction addresses   Target Addresses    Predictions");
            int[] instructionAddresses = fetchUnit.dumpInstructionAddresses();
            int[] targetAddresses = fetchUnit.dumpTargetAddresses();
            boolean[] outcomes = fetchUnit.dumpOutcomes();
            String taken;
            int convertedAdd = 0;
            for(int b =0 ;b<32;b++){
                if(targetAddresses[b]!=0)
                    convertedAdd = 1+(targetAddresses[b]-1000)/4;
                else
                    convertedAdd = 0;
                if(outcomes[b])
                    taken="Taken";
                else
                    taken="Not Taken";
                System.out.println(instructionAddresses[b]+"    "+convertedAdd+"  "+taken);
            }
        }
    }
        
    
}
