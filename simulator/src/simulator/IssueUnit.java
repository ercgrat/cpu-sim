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
    private Scoreboard scoreboard;
    
    public IssueUnit(int NW, DecodeUnit decodeUnit, ReservationStations reservationStations, ReorderBuffer reorderBuffer, Scoreboard scoreboard) {
        this.NW = NW;
        this.decodeUnit = decodeUnit;
        this.reservationStations = reservationStations;
        this.reorderBuffer = reorderBuffer;
        this.scoreboard = scoreboard;
    }
    
    public void cycle(){
        for(int i=0;i<NW;i++){
            Instruction nextInst = decodeUnit.peek();
            if(nextInst==null)
                break;
            else{
                int stNum = reservationStations.isFree(nextInst.unit);
                if(stNum != -1 && reorderBuffer.hasSlot()){
                    int robSlot = reorderBuffer.reserveSlot(nextInst);
                    scoreboard.rename(nextInst.dest, robSlot);
                    reservationStations.reserveStation(stNum, nextInst, robSlot);
                    decodeUnit.dequeue();
                    System.out.println("Issued: " + nextInst);
                }
                else
                    break;
            }
        }
    }
}
