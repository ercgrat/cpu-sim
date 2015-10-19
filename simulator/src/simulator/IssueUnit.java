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
    
    public int cycle(){
        int numStalls = 0;
        for(int i=0;i<NW;i++){
            Instruction nextInst = decodeUnit.peek();
            if(nextInst==null)
                break;
            else{
                int stNum = reservationStations.isFree(nextInst.unit);
                if(stNum != -1 && reorderBuffer.hasSlot()){
                    int robSlot = reorderBuffer.reserveSlot(nextInst);
                    reservationStations.reserveStation(stNum, nextInst, robSlot);
					if(!(nextInst.unit.equals("BU") || nextInst.unit.equals("Store"))) {
						scoreboard.rename(nextInst.dest, robSlot);
					}
                    decodeUnit.dequeue();
                    //System.out.println("Issued: " + nextInst);
                }
                else{
                    if(stNum == -1)
                        numStalls = 1;
                    else
                        numStalls = 2;
                    break;
                }
            }
        }
        return numStalls;
    }
}
