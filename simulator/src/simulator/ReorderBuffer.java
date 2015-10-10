/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator;


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
    
    private int NR, NC, head;
    private ROBEntry[] ROB;
    private RegisterFile<Integer> intRegisters;
    private RegisterFile<Double> floatRegisters;
    private ReservationStations resStations;
    private Scoreboard scoreboard;
    
    public ReorderBuffer(int NR, int NC,  RegisterFile<Integer> intRegisters,  RegisterFile<Double> floatRegisters, ReservationStations resStations, Scoreboard scoreboard){
        this.NR = NR;
        this.NC = NC;
        this.intRegisters = intRegisters;
        this.floatRegisters = floatRegisters;
        this.resStations = resStations;
        this.scoreboard = scoreboard;
        head = 0;
        ROB = new ROBEntry[NR];
    }
    
    public void cycle(int numCommitted) {
        int current = head;
        ROBEntry entry = ROB[current];
        
        // Find the oldest instruction
        while(entry == null) {
            current = next(current);
            entry = ROB[current];
        }
        
        // Commit in order from oldest instructions, if values are ready, while bandwidth available on the CDB
        while(entry.hasValue && numCommitted < NC) {
            commit(current, entry);
            ROB[current] = null;
            numCommitted++;
            
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
        if(index >= 0 && index < NR) {
            ROB[index].hasValue = true;
            ROB[index].intValue = val;
        }
        return true;
    }
    
    public boolean write(int index, Double val) {
        if(index >= 0 && index < NR) {
            ROB[index].hasValue = true;
            ROB[index].floatValue = val;
        }
        return true;
    }
    
    private int next() {
        return (head + 1) % NR;
    }
    
    private int next(int index) {
        return (index + 1) % NR;
    }
}
