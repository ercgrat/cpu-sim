/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator;

import java.util.*;

public class ReorderBuffer {
    
    private class ROBEntry {
        Instruction inst;
        boolean hasValue;
        Integer intValue;
        Double floatValue;
        
        public ROBEntry(Instruction inst) {
            this.inst = inst;
            hasValue = false;
        }
    }
    
    private int NR, NC, head, writtenThisCycle;
    private ROBEntry[] ROB;
    private RegisterFile<Integer> intRegisters;
    private RegisterFile<Double> floatRegisters;
    private ReservationStations resStations;
    private Scoreboard scoreboard;
	private ArrayList<Integer> intIndexWriteQueue;
	private ArrayList<Integer> floatIndexWriteQueue;
	private ArrayList<Integer> intValueWriteQueue;
    private ArrayList<Double> floatValueWriteQueue;
	private ArrayList<Integer> indexCommitQueue;
	private ArrayList<ROBEntry> entryCommitQueue;
	
    public ReorderBuffer(int NR, int NC,  RegisterFile<Integer> intRegisters,  RegisterFile<Double> floatRegisters, ReservationStations resStations, Scoreboard scoreboard){
        this.NR = NR;
        this.NC = NC;
        this.intRegisters = intRegisters;
        this.floatRegisters = floatRegisters;
        this.resStations = resStations;
        this.scoreboard = scoreboard;
        head = 0;
        ROB = new ROBEntry[NR];
		
		writtenThisCycle = 0;
		intIndexWriteQueue = new ArrayList<Integer>();
		floatIndexWriteQueue = new ArrayList<Integer>();
		intValueWriteQueue = new ArrayList<Integer>();
		floatValueWriteQueue = new ArrayList<Double>();
		indexCommitQueue = new ArrayList<Integer>();
		entryCommitQueue = new ArrayList<ROBEntry>();
    }
    
    public void cycle() {
        
		// Write back what came from the execution stage last cycle and empty the writeback queues
		for(int i = 0; i < intIndexWriteQueue.size(); i++) {
			int robSlot = intIndexWriteQueue.get(i);
			ROB[robSlot].hasValue = true;
            ROB[robSlot].intValue = intValueWriteQueue.get(i);
			resStations.writeback(robSlot, ROB[robSlot].intValue);
			scoreboard.writeback(robSlot);
			writtenThisCycle++;
		}
		intIndexWriteQueue.clear();
		intValueWriteQueue.clear();
		for(int i = 0; i < floatIndexWriteQueue.size(); i++) {
			int robSlot = floatIndexWriteQueue.get(i);
			ROB[robSlot].hasValue = true;
            ROB[robSlot].floatValue = floatValueWriteQueue.get(i);
			resStations.writeback(robSlot, ROB[robSlot].floatValue);
			scoreboard.writeback(robSlot);
			writtenThisCycle++;
		}
        floatIndexWriteQueue.clear();
		floatValueWriteQueue.clear();
		
		// Write back what was committed last cycle and empty the commit queues
		for(int i = 0; i < indexCommitQueue.size(); i++) {
			int current = indexCommitQueue.get(i);
			ROBEntry entry = entryCommitQueue.get(i);
			commit(current, entry);
		}
		indexCommitQueue.clear();
		entryCommitQueue.clear();
		
		writtenThisCycle = 0;
    }
    
	public void commit() {
		int current = head;
        ROBEntry entry = ROB[current];
		
		// Find the oldest instruction
        while(entry == null) {
            current = next(current);
            entry = ROB[current];
        }
		
		// Commit in order from oldest instructions, if values are ready, while bandwidth available on the CDB
        while(entry.hasValue && writtenThisCycle < NC) {
			indexCommitQueue.add(current);
			entryCommitQueue.add(entry);
            ROB[current] = null;
            writtenThisCycle++;
            
            // Go to next oldest entry
            current = next(current);
            entry = ROB[current];
        }
	}
	
    private void commit(int robSlot, ROBEntry entry) {
        if(entry.intValue != null) {
            intRegisters.write(entry.inst.dest.registerIndex, entry.intValue);
            resStations.writeback(robSlot, entry.intValue);
        } else {
            floatRegisters.write(entry.inst.dest.registerIndex, entry.floatValue);
            resStations.writeback(robSlot, entry.floatValue);
        }
        scoreboard.writeback(robSlot);
    }
    
    public boolean hasSlot() {
        if(ROB[head] == null) {
            return true;
        } else {
            return false;
        }
    }
    
    /*To reserve a slot in the reorder buffer with the given slot number*/
    public int reserveSlot(Instruction instruction) {
        int index = head;
        head = next();
        ROB[index] = new ROBEntry(instruction);
        return index;
    }
    
    public boolean write(int index, Integer val) {
        if(writtenThisCycle < NC && index >= 0 && index < NR) {
			intIndexWriteQueue.add(index);
			intValueWriteQueue.add(val);
			writtenThisCycle++;
			return true;
        } else {
			return false;
		}
    }
    
    public boolean write(int index, Double val) {
        if(writtenThisCycle < NC && index >= 0 && index < NR) {
			floatIndexWriteQueue.add(index);
			floatValueWriteQueue.add(val);
			writtenThisCycle++;
			return true;
        } else {
			return false;
		}
    }
    
    private int next() {
        return (head + 1) % NR;
    }
    
    private int next(int index) {
        return (index + 1) % NR;
    }
}
