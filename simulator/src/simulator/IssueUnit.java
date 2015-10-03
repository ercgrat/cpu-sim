/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator;

/**
 *
 * @author zaeem
 */
public class IssueUnit {
    private int NW;
    private DecodeUnit decodeUnit;
    private ReservationStations reservationStations;
    private ReorderBuffer reorderBuffer;
    
    public IssueUnit(int NW, DecodeUnit decodeUnit, ReservationStations reservationStations, ReorderBuffer reorderBuffer){
        this.NW = NW;
        this.decodeUnit = decodeUnit;
        this.reservationStations = reservationStations;
        this.reorderBuffer = reorderBuffer;
    }
    
    public void cycle(){
        for(int i=0;i<NW;i++){
            Instruction nextInst = decodeUnit.peek();
            if(nextInst==null)
                break;
            else{
                if(nextInst.unit=="Load/Store"){
                    if(nextInst.op=="LD" || nextInst.op=="L.D")
                        nextInst.unit = "Load";
                    else
                        nextInst.unit = "Store";
                }
                int stNum = reservationStations.isFree(nextInst.unit);
                if(stNum!=0){
                    int slotNum = reorderBuffer.isSlotFree();
                    if(slotNum != 0){
                        reservationStations.reserveStation(stNum, nextInst);
                        reorderBuffer.reserveSlot(slotNum, nextInst);
                        decodeUnit.dequeue();
                    }
                    else
                        break;
                }
                else
                    break;
            }
        }
    }
}
