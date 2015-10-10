package simulator;

public class BranchUnit {

    private Unit BU;
    ReorderBuffer reorderBuffer;
    ReservationStations reservationStations;
    
    public BranchUnit(ReservationStations reservationStations, ReorderBuffer reorderBuffer){
        this.reorderBuffer = reorderBuffer;
        this.reservationStations = reservationStations;
        this.BU.curCycle = 0;
        this.BU.instruction = null;
        this.BU.isWaiting = false;
    }
    
    private class Unit{
        Instruction instruction;
        int curCycle;
        boolean isWaiting;
    }
    
    /*Returns the branch instruction if the original prediction was fasle, null otherwise*/
    public Instruction cycle(){
        int stNum;
        Instruction returnInstruction = null;
        if(BU.curCycle == 0){
            stNum = -1;
            stNum = reservationStations.isReady("BU");
            if(stNum != -1){
                BU.instruction = reservationStations.getInstruction(stNum);
                BU.curCycle++;
            }
        }
        else if(!BU.isWaiting){
            boolean branchOutcome = BU.instruction.branchCondition;
            switch(BU.instruction.op){
                case "BEQZ":
                    branchOutcome = (BU.instruction.dest.intValue == 0);
                    break;
                case "BNEZ":
                    branchOutcome = (BU.instruction.dest.intValue != 0);
                    break;
                case "BEQ":
                    branchOutcome = (BU.instruction.src.intValue == BU.instruction.dest.intValue);
                    break;
                case "BNE":
                    branchOutcome = (BU.instruction.src.intValue != BU.instruction.dest.intValue);
                    break;
            }
            if(branchOutcome != BU.instruction.branchCondition){
                BU.instruction.branchCondition = branchOutcome;
                returnInstruction = BU.instruction;
            }
            if(/*ROB function to write branch here*/true){
                BU.curCycle = 0;
                reservationStations.finishedExecution(BU.instruction.stNum);
            }
            else{
                BU.isWaiting = true;
            }
        }
        else{
            if(/*ROB function to write branch here*/true){
                BU.curCycle = 0;
                BU.isWaiting = false;
                reservationStations.finishedExecution(BU.instruction.stNum);
            }
        }
        return returnInstruction;
    }
        
}
