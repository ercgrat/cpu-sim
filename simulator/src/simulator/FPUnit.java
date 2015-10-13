package simulator;

public class FPUnit {

    private Unit FPU;
    ReorderBuffer reorderBuffer;
    ReservationStations reservationStations;
    
    private class Unit{
        Instruction[] stages;
        boolean isWaiting;
        boolean isDividing;
        
        public Unit() {
            stages = new Instruction[4];
            isWaiting = false;
            isDividing = false;
        }
    }
    
    public FPUnit(ReservationStations reservationStations, ReorderBuffer reorderBuffer){
        this.reorderBuffer = reorderBuffer;
        this.reservationStations = reservationStations;
        this.FPU = new Unit();
    }
    
    public void cycle(){
        int stNum = reservationStations.isReady("FPU");
        System.out.println("FPU first stage instruction is: "+FPU.stages[0]);
        System.out.println("FPU second stage instruction is: "+FPU.stages[1]);
        System.out.println("FPU third stage instruction is: "+FPU.stages[2]);
        System.out.println("FPU last stage instruction is: "+FPU.stages[3]);
        if(FPU.stages[3] != null){
            if(!FPU.isWaiting){
                switch(FPU.stages[3].op){
                    case "ADD.D":
                        FPU.stages[3].dest.floatValue = FPU.stages[3].src.floatValue + FPU.stages[3].target.floatValue;
                        break;
                    case "SUB.D":
                        FPU.stages[3].dest.floatValue = FPU.stages[3].src.floatValue - FPU.stages[3].target.floatValue;
                        break;
                    case "MUL.D":
                        FPU.stages[3].dest.floatValue = FPU.stages[3].src.floatValue * FPU.stages[3].target.floatValue;
                        break;
                    case "DIV.D":
                        FPU.stages[3].dest.floatValue = FPU.stages[3].src.floatValue/FPU.stages[3].target.floatValue;
                        break;
                } 
            }
            if(reorderBuffer.write(FPU.stages[3].robSlot, FPU.stages[3].dest.floatValue)){
                FPU.isWaiting = false;
                reservationStations.finishedExecution(FPU.stages[3].stNum);
                FPU.isDividing = false;
                FPU.stages[3] = null;
            }
            else{
                FPU.isWaiting = true;
            }
        }
        for(int i = 3; i > 0; i--){
            if(FPU.stages[i] == null)
                if(FPU.stages[i-1] != null){
                    FPU.stages[i] = FPU.stages[i-1];
                    FPU.stages[i-1] = null;
            }
        }
        if(!FPU.isDividing){
            if(stNum != -1){
                Instruction potentialInst = reservationStations.checkInstruction(stNum);
                if("DIV.D".equals(potentialInst.op)){
                    boolean emptyFlag = true;
                    for(int i = 0; i < 4; i++){
                        if(FPU.stages[i] != null){
                            emptyFlag = false;
                            break;
                        }
                    }
                    if(emptyFlag){
                        FPU.stages[0] = reservationStations.getInstruction(stNum);
                        FPU.isDividing = true;
                    }
                }
                else if(FPU.stages[0] == null)
                    FPU.stages[0] = reservationStations.getInstruction(stNum);
            }
                
        }
    }
}
