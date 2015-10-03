/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator;


public class ReorderBuffer {
    
    private int NR;
    
    public ReorderBuffer(int NR){
        this.NR = NR;
    }
    
    /*To check if there s a free slot in the reorder buffer. If there is, returns the slot number*/
    public int isSlotFree(){
        return 0;
    }
    
    /*To reserve a slot in the reorder buffer with the given slot number*/
    public void reserveSlot(int slotNum, Instruction instruction){
        
    }
    
}
