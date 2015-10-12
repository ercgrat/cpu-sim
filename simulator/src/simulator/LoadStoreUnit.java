package simulator;

public class LoadStoreUnit {

    private Unit LSU;
    ReorderBuffer reorderBuffer;
    ReservationStations reservationStations;
    
    private class Unit {
        Instruction instruction;
        int curCycle;
        boolean isWaiting;
        
        public Unit() {
            curCycle = 0;
            isWaiting = false;
        }
    }
    
    public LoadStoreUnit(ReservationStations reservationStations, ReorderBuffer reorderBuffer){
        this.reorderBuffer = reorderBuffer;
        this.reservationStations = reservationStations;
        this.LSU = new Unit();
    }
    
    public void cycle(){
        int stNum;
        if(LSU.curCycle == 0){
            stNum = -1;
            stNum = reservationStations.isReady("Load/Store");
            if(stNum != -1){
                    LSU.instruction = reservationStations.getInstruction(stNum);
                    LSU.curCycle++;
            }
        }
        else if(!LSU.isWaiting){
            LSU.instruction.memoryAddress = LSU.instruction.src.intValue + LSU.instruction.target.intValue;
            if(true/*Rob method to write memory address and value*/){
                LSU.curCycle = 0;
                reservationStations.finishedExecution(LSU.instruction.stNum);
            }
            else{
                LSU.isWaiting = true;
            }
        }
        else{
            if(true/*Rob method to write memory address and value*/){
                LSU.curCycle = 0;
                LSU.isWaiting = false;
                reservationStations.finishedExecution(LSU.instruction.stNum);
            }
        }
    }
}
