public class MULTUnit {

    private Unit MULT;
    ReorderBuffer reorderBuffer;
    ReservationStations reservationStations;
    
    private class Unit{
        Instruction[] stages;
        boolean isWaiting;
        
        public Unit() {
            stages = new Instruction[4];
            isWaiting = false;
        }
    }
    
    public MULTUnit(ReservationStations reservationStations, ReorderBuffer reorderBuffer){
        this.reorderBuffer = reorderBuffer;
        this.reservationStations = reservationStations;
        this.MULT = new Unit();
    }
    
    public void cycle(){
        int stNum = reservationStations.isReady("MULT");
        if(MULT.stages[3] != null){
            if(!MULT.isWaiting)
                MULT.stages[3].dest.intValue = MULT.stages[3].src.intValue * MULT.stages[3].target.intValue;
            if(reorderBuffer.write(MULT.stages[3].robSlot, MULT.stages[3].dest.intValue)){
                MULT.isWaiting = false;
                reservationStations.finishedExecution(MULT.stages[3].stNum);
                MULT.stages[3] = null;
            }
            else{
                MULT.isWaiting = true;
            }
        }
        for(int i = 3; i > 0; i--){
            if(MULT.stages[i] == null)
                if(MULT.stages[i-1] != null){
                    MULT.stages[i] = MULT.stages[i-1];
                    MULT.stages[i-1] = null;
            }
        }
        if(MULT.stages[0] == null && stNum != -1){
            MULT.stages[0] = reservationStations.getInstruction(stNum);
        }
    }
}
