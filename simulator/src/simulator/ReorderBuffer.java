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
        Float floatValue;
        boolean flushFlag;
        
        public ROBEntry(Instruction inst) {
            this.inst = inst;
            hasValue = false;
            flushFlag = false;
        }
    }
    
    private int NR, NC, head, writtenThisCycle;
    private ROBEntry[] ROB;
    private RegisterFile<Integer> intRegisters;
    private RegisterFile<Float> floatRegisters;
    private ReservationStations resStations;
    private Scoreboard scoreboard;
	private ArrayList<Integer> intIndexWriteQueue;
	private ArrayList<Integer> floatIndexWriteQueue;
	private ArrayList<Integer> intValueWriteQueue;
    private ArrayList<Float> floatValueWriteQueue;
	private ArrayList<Integer> indexCommitQueue;
	private ArrayList<ROBEntry> entryCommitQueue;
    private Map<Integer, Float> memory;
	
    public ReorderBuffer(int NR, int NC,  RegisterFile<Integer> intRegisters,  RegisterFile<Float> floatRegisters, ReservationStations resStations, Scoreboard scoreboard, Map<Integer, Float> memory){
        this.NR = NR;
        this.NC = NC;
        this.intRegisters = intRegisters;
        this.floatRegisters = floatRegisters;
        this.resStations = resStations;
        this.scoreboard = scoreboard;
        this.memory = memory;
        head = 0;
        ROB = new ROBEntry[NR];
		
		writtenThisCycle = 0;
		intIndexWriteQueue = new ArrayList<Integer>();
		floatIndexWriteQueue = new ArrayList<Integer>();
		intValueWriteQueue = new ArrayList<Integer>();
		floatValueWriteQueue = new ArrayList<Float>();
		indexCommitQueue = new ArrayList<Integer>();
		entryCommitQueue = new ArrayList<ROBEntry>();
    }
    
    public void cycle() {
        
		// Write back what came from the execution stage last cycle and empty the writeback queues
		for(int i = 0; i < intIndexWriteQueue.size(); i++) {
			int robSlot = intIndexWriteQueue.get(i);
			ROB[robSlot].hasValue = true;
            ROB[robSlot].intValue = intValueWriteQueue.get(i);
            if(ROB[robSlot].flushFlag == false || ROB[robSlot].inst.unit.equals("Store")) { // Ignore instruction if set to be flushed
                resStations.writeback(robSlot, ROB[robSlot].intValue);
                scoreboard.writeback(robSlot);
                writtenThisCycle++;
            }
		}
		intIndexWriteQueue.clear();
		intValueWriteQueue.clear();
        
		for(int i = 0; i < floatIndexWriteQueue.size(); i++) {
			int robSlot = floatIndexWriteQueue.get(i);
			ROB[robSlot].hasValue = true;
            ROB[robSlot].floatValue = floatValueWriteQueue.get(i);
            if(ROB[robSlot].flushFlag == false || ROB[robSlot].inst.unit.equals("Store")) { // Ignore instruction if set to be flushed
                resStations.writeback(robSlot, ROB[robSlot].floatValue);
                scoreboard.writeback(robSlot);
                writtenThisCycle++;
            }
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
    
	public void stageCommits() { // To be called as a second "cycle()" method, after execution stations finish
		int current = head;
        ROBEntry entry = ROB[current];
		
		// Find the oldest instruction
        while(entry == null) {
            current = next(current);
            entry = ROB[current];
        }
		
		// Commit in order from oldest instructions, if values are ready, while bandwidth available on the CDB
        while(entry.hasValue && writtenThisCycle < NC) {
            if(entry.flushFlag == false) { // Not marked for flushing
                indexCommitQueue.add(current);
                entryCommitQueue.add(entry);
                writtenThisCycle++;
            }
            ROB[current] = null; // Flush whether committed or flushed
            
            // Go to next oldest entry
            current = next(current);
            entry = ROB[current];
        }
	}
	
    private void commit(int robSlot, ROBEntry entry) {
        if(entry.inst.unit.equals("Store")) { // For stores, write to memory
            if(entry.intValue != null) {
                Float floatVal = (float)entry.intValue;
                memory.put(entry.inst.memoryAddress, floatVal);
            } else {
                memory.put(entry.inst.memoryAddress, entry.floatValue);
            }
        } else {
            if(entry.intValue != null) {
                intRegisters.write(entry.inst.dest.registerIndex, entry.intValue);
                resStations.writeback(robSlot, entry.intValue);
            } else {
                floatRegisters.write(entry.inst.dest.registerIndex, entry.floatValue);
                resStations.writeback(robSlot, entry.floatValue);
            }
        }
        scoreboard.writeback(robSlot);
        
        if(entry.inst.unit.equals("Store") || entry.inst.unit.equals("Load")) { // Load/store res stations are locked until commit
            resStations.finishedExecution(entry.inst.stNum);
        }
    }
    
    public void flush(Instruction branch) {
        int current = previous();
        ROBEntry entry = ROB[current];
        while(true) {
            if(entry.inst == branch) {
                int flushIndex = next(current);
                while(flushIndex != head) {
                    ROB[flushIndex].flushFlag = true;
                    flushIndex = next(flushIndex);
                }
                break;
            }
        }
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
    
    public boolean write(int index, Float val) {
        if(writtenThisCycle < NC && index >= 0 && index < NR) {
			floatIndexWriteQueue.add(index);
			floatValueWriteQueue.add(val);
			writtenThisCycle++;
			return true;
        } else {
			return false;
		}
    }
    
    public boolean isBefore(Instruction i, Instruction j) {
        int current = previous();
        ROBEntry entry = ROB[current];
        while(current != head) {
            if(entry.inst == i) { // Compares pointers
                return false;
            } else if(entry.inst == j) { // Compares pointers
                return true;
            }
            current = previous(current);
            entry = ROB[current];
        }
        return false;
    }
    
    private int next() {
        return (head + 1) % NR;
    }
    
    private int next(int index) {
        return (index + 1) % NR;
    }
    
    private int previous() {
        return (head - 1) % NR;
    }
    
    private int previous(int index) {
        return (index - 1) % NR;
    }
}
