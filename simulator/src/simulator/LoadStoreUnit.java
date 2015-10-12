package simulator;

import java.util.Map;

public class LoadStoreUnit {

    private Unit LSU;
    ReorderBuffer reorderBuffer;
    ReservationStations reservationStations;
    Instruction memoryInst;
    boolean isMemInstWaiting;
    private Map<Integer, Float> memory;
    
    private class Unit {
        Instruction instruction;
        int curCycle;
        boolean isWaiting;
        
        public Unit() {
            curCycle = 0;
            isWaiting = false;
        }
    }
    
    public LoadStoreUnit(ReservationStations reservationStations, ReorderBuffer reorderBuffer, Map<Integer, Float> memory){
        this.reorderBuffer = reorderBuffer;
        this.memory = memory;
        this.reservationStations = reservationStations;
        this.LSU = new Unit();
        this.memoryInst = null;
        this.isMemInstWaiting = false;
    }
    
    public void cycle(){
        memoryAccess();
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
            if("Store".equals(LSU.instruction.unit)){
                if("S.D".equals(LSU.instruction.op) && reorderBuffer.write(LSU.instruction.robSlot, LSU.instruction.dest.floatValue))
                    LSU.curCycle = 0;
                else if("SD".equals(LSU.instruction.op) && reorderBuffer.write(LSU.instruction.robSlot, LSU.instruction.dest.intValue))
                    LSU.curCycle = 0;
                else
                    LSU.isWaiting = true;
            }
            else{
                if(memoryInst != null){
                    memoryInst = LSU.instruction;
                    LSU.curCycle = 0;
                }
                else
                    LSU.isWaiting = true;
            }
        }
        else{
            if("Store".equals(LSU.instruction.unit)){
                if("S.D".equals(LSU.instruction.op) && reorderBuffer.write(LSU.instruction.robSlot, LSU.instruction.dest.floatValue)){
                    LSU.curCycle = 0;
                    LSU.isWaiting = false;
                }
                else if("SD".equals(LSU.instruction.op) && reorderBuffer.write(LSU.instruction.robSlot, LSU.instruction.dest.intValue)){
                    LSU.curCycle = 0;
                    LSU.isWaiting = false;
                }
            }
            else{
                if(memoryInst != null){
                    memoryInst = LSU.instruction;
                    LSU.curCycle = 0;
                    LSU.isWaiting = false;
                }
            }
        }
    }
    
    private void memoryAccess(){
        if(memoryInst != null && !isMemInstWaiting){
            if(true/*reorderBuffer.isMemoryAddressInROB(memoryInst.memoryAddress)*/){
                if("L.D".equals(memoryInst.op)){
                    /*memoryInst.dest.floatValue=reorderBuffer.getValueOfGivenAddress(memoryInst.memoryAddress);*/
                    if(reorderBuffer.write(memoryInst.robSlot, memoryInst.dest.floatValue))
                        memoryInst = null;
                    else
                        isMemInstWaiting = true;
                }
                else{
                    /*memoryInst.dest.intValue=Integer.valueOf(reorderBuffer.getValueOfGivenAddress(memoryInst.memoryAddress));*/
                    if(reorderBuffer.write(memoryInst.robSlot, memoryInst.dest.intValue))
                        memoryInst = null;
                    else
                        isMemInstWaiting = true;
                }
            }
            else{
                if("L.D".equals(memoryInst.op)){
                    memoryInst.dest.floatValue=memory.get(memoryInst.memoryAddress);
                    if(reorderBuffer.write(memoryInst.robSlot, memoryInst.dest.floatValue))
                        memoryInst = null;
                    else
                        isMemInstWaiting = true;
                }
                else{
                    memoryInst.dest.intValue= Math.round(memory.get(memoryInst.memoryAddress));
                    if(reorderBuffer.write(memoryInst.robSlot, memoryInst.dest.intValue))
                        memoryInst = null;
                    else
                        isMemInstWaiting = true;
                }
            }
        }
        else if(memoryInst != null && isMemInstWaiting){
            if("L.D".equals(memoryInst.op)){
                if(reorderBuffer.write(memoryInst.robSlot, memoryInst.dest.floatValue)){
                    memoryInst = null;
                    isMemInstWaiting = false;
                }   
            }
            else{
                if(reorderBuffer.write(memoryInst.robSlot, memoryInst.dest.intValue)){
                    memoryInst = null;
                    isMemInstWaiting = false;
                }
            }
        }
    }
}
