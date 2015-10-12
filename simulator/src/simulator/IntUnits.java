package simulator;

public class IntUnits {

    private Unit[] INTS;
    ReorderBuffer reorderBuffer;
    ReservationStations reservationStations;
    
    public IntUnits(ReservationStations reservationStations, ReorderBuffer reorderBuffer){
        this.reorderBuffer = reorderBuffer;
        this.reservationStations = reservationStations;
        this.INTS = new Unit[2];
        INTS[0] = new Unit();
        INTS[1] = new Unit();
    }
    
    private class Unit {
        Instruction instruction;
        int curCycle;
        boolean isWaiting;
        
        public Unit() {
            instruction = null;
            curCycle = 0;
            isWaiting = false;
        }
    }
    
    public void cycle(){
        int stNum;
        for(int i = 0; i < 2; i++){
            if(INTS[i].curCycle == 0){
                stNum = -1;
                if(i == 0)
                    stNum = reservationStations.isReady("INT0");
                else
                    stNum = reservationStations.isReady("INT1");
                if(stNum != -1){
                    INTS[i].instruction = reservationStations.getInstruction(stNum);
                    INTS[i].curCycle++;
                }
            }
            else if(!INTS[i].isWaiting){
                switch(INTS[i].instruction.op){
                    case "AND":
                    case "ANDI":
                        INTS[i].instruction.dest.intValue = INTS[i].instruction.src.intValue & INTS[i].instruction.target.intValue;
                        break;
                    case "OR":
                    case "ORI":
                        INTS[i].instruction.dest.intValue = INTS[i].instruction.src.intValue ^ INTS[i].instruction.target.intValue;
                        break;
                    case "SLT":
                    case "SLTI":
                        INTS[i].instruction.dest.intValue = (INTS[i].instruction.src.intValue < INTS[i].instruction.target.intValue) ? 1:0;
                        break;
                    case "DADD":
                    case "DADDI":
                        INTS[i].instruction.dest.intValue = INTS[i].instruction.src.intValue + INTS[i].instruction.target.intValue;
                        break;
                    case "DSUB":
                        INTS[i].instruction.dest.intValue = INTS[i].instruction.src.intValue - INTS[i].instruction.target.intValue;
                        break;
                }
                if(reorderBuffer.write(INTS[i].instruction.robSlot, INTS[i].instruction.dest.intValue)){
                    INTS[i].curCycle = 0;
                    reservationStations.finishedExecution(INTS[i].instruction.stNum);
                }
                else{
                    INTS[i].isWaiting = true;
                }
            }
            else{
                if(reorderBuffer.write(INTS[i].instruction.robSlot, INTS[i].instruction.dest.intValue)){
                    INTS[i].curCycle = 0;
                    INTS[i].isWaiting = false;
                    reservationStations.finishedExecution(INTS[i].instruction.stNum);
                }
            }
        }
        
    }
}
