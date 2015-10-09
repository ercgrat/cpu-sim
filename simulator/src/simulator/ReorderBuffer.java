/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator;


public class ReorderBuffer {
    
    private class ROBEntry {
        Instruction inst;
        int resStationNum;
        boolean hasValue;
        Integer intValue;
        Double floatValue;
        
        public ROBEntry(Instruction inst, int resStationNum) {
            this.inst = inst;
            this.resStationNum = resStationNum;
            hasValue = false;
        }
    }
    
    private int NR, NC, head;
    private ROBEntry[] ROB;
    
    public ReorderBuffer(int NR, int NC){
        this.NR = NR;
        this.NC = NC;
        head = 0;
        ROB = new ROBEntry[NR];
    }
    
    public void cycle() {
        int current = head;
        ROBEntry entry = ROB[current];
        
        // Find the oldest instruction
        while(entry == null) {
            current = next(current);
            entry = ROB[current];
        }
        
        // Commit in order from oldest instructions if values are available
        int numCommitted = 0;
        while(entry.hasValue && numCommitted < NC) {
            /***
            *** Commit the value available in entry
            ***/
            ROB[current] = null;
            numCommitted++;
            
            // Go to next oldest entry
            current = next(current);
            entry = ROB[current];
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
    public int reserveSlot(Instruction instruction, int resStationNum) {
        int index = head;
        head = next();
        ROB[index] = new ROBEntry(instruction, resStationNum);
        return index;
    }
    
    private int next() {
        return (head + 1) % NR;
    }
    
    private int next(int index) {
        return (index + 1) % NR;
    }
}
