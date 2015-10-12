/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator;

import java.util.*;

public class ReservationStations {
    
    private final int numStations = 19;
    private Station[] Stations;
    private Scoreboard scoreboard;
    private ReorderBuffer reorderBuffer;
    
    public ReservationStations(Scoreboard scoreboard){
        Stations = new Station[numStations];
        this.scoreboard = scoreboard;
        for(int i = 0; i < numStations; i++){
            Stations[i] = new Station();
            if(i<2)
                Stations[i].unit = "INT0";
            else if(i<4)
                Stations[i].unit = "INT1";
            else if(i<6)
                Stations[i].unit = "MULT";
            else if(i<9)
                Stations[i].unit = "Load";
            else if(i<12)
                Stations[i].unit = "Store";
            else if(i<17)
                Stations[i].unit = "FPU";
            else
                Stations[i].unit = "BU";
            
        }
    }
    
    public void reorderBufferInit(ReorderBuffer reorderBuffer){
        this.reorderBuffer = reorderBuffer;
    }
    
    public class Station{
        boolean isFree;
        boolean justFreed;
        boolean finishedExec;
        boolean isWaiting;
        boolean isExecuting;
        String unit;
        Instruction instruction;
        int srcName;
        int targetName;
        int destName;
        
        public Station() {
            isFree = true;
            justFreed = false;
            instruction = null;
            finishedExec = true;
            isWaiting = false;
            isExecuting = false;
        }
    }
    
    public void cycle(){
        for(int i=0;i<numStations;i++){
            if(Stations[i].justFreed){
                Stations[i].isFree = true;
                Stations[i].justFreed = false;
                }
            else if(Stations[i].finishedExec && !Stations[i].isFree){
                Stations[i].justFreed = true;
                Stations[i].instruction = null;
            }
        }
    }
    
    public int isFree(String Unit){
        switch(Unit){
            case "INT":
                for(int i=0;i<4;i++){
                    if(Stations[i].isFree)
                        return i;
                }
                break;
            case "MULT":
                for(int i=4;i<6;i++){
                    if(Stations[i].isFree)
                        return i;
                }
                break;
            case "Load":
                for(int i=6;i<9;i++){
                    if(Stations[i].isFree)
                        return i;
                }
                break;
            case "Store":
                for(int i=9;i<12;i++){
                    if(Stations[i].isFree)
                        return i;
                }
                break;
            case "FPU":
                for(int i=12;i<17;i++){
                    if(Stations[i].isFree)
                        return i;
                }
                break;
            case "BU":
                for(int i=17;i<19;i++){
                    if(Stations[i].isFree)
                        return i;
                }
                break;
        }
        return -1;
    }
    
    public Instruction getInstruction(int stNum){
        Stations[stNum].isExecuting = true;
        return Stations[stNum].instruction;
    }
    
    public Instruction checkInstruction(int stNum){
        return Stations[stNum].instruction;
    }
    
    public int isReady(String Unit){
        switch(Unit){
            case "INT0":
                for(int i=0;i<2;i++){
                    if(!Stations[i].isFree && !Stations[i].isWaiting && Stations[i].instruction!=null) {
                        return i;
                    }
                }
                break;
            case "INT1":
                for(int i=2;i<4;i++){
                    if(!Stations[i].isFree && !Stations[i].isWaiting && Stations[i].instruction!=null) {
                        return i;
                    }
                }
                break;
            case "MULT":
                for(int i=4;i<6;i++){
                    if(!Stations[i].isFree && !Stations[i].isWaiting && Stations[i].instruction!=null && !Stations[i].isExecuting)
                        return i;
                }
                break;
            case "Load/Store":
                int[] robSlots = new int[6];
                int[] stNums = new int[6];
                int readyCounter = 0;
                int earliest = -1;
                for(int i=6;i<12;i++){
                    if(!Stations[i].isFree && !Stations[i].isWaiting && Stations[i].instruction!=null){
                        robSlots[readyCounter] = Stations[i].instruction.robSlot;
                        stNums[readyCounter] = i;
                        readyCounter++;
                    }
                }
                if(readyCounter > 0)
                    earliest = stNums[0];
                for(int i = 1; i < readyCounter; i++){
                   //Check from rob which of the two slots is for earlier instruction 
                    if(reorderBuffer.isBefore(Stations[stNums[i]].instruction, Stations[earliest].instruction))
                        earliest = stNums[i];
                }
                return earliest;
            case "FPU":
                for(int i=12;i<17;i++){
                    if(!Stations[i].isFree && !Stations[i].isWaiting && Stations[i].instruction!=null && !Stations[i].isExecuting)
                        return i;
                }
                break;
            case "BU":
                for(int i=17;i<19;i++){
                    if(!Stations[i].isFree && !Stations[i].isWaiting && Stations[i].instruction!=null)
                        return i;
                }
                break;
        }
        return -1;
    }
    
    public void finishedExecution(int stNum) {
        System.out.println("Freed reservation station.");
        Stations[stNum].isExecuting = false;
        Stations[stNum].finishedExec = true;
    }
    
    public void writeback(int robSlot, Integer val) {
        for(int i = 0; i < numStations; i++) {
            if(Stations[i].isWaiting) {
                if(Stations[i].srcName >= 0  && Stations[i].destName < 0) {
                    if(Stations[i].targetName >= 0) { // both source and target are waiting
                        if(Stations[i].srcName == robSlot) { // if source matches writeback slot, read value
                            Stations[i].instruction.src.intValue = val;
                            Stations[i].srcName = -1;
                        }
                        if(Stations[i].targetName == robSlot) { // if target matches writeback slot, read value
                            Stations[i].instruction.target.intValue = val;
                            Stations[i].targetName = -1;
                        }
                        // done waiting if both operands match the writeback slot
                        if(Stations[i].srcName == robSlot && Stations[i].targetName == robSlot) {
                            Stations[i].isWaiting = false;
                        }
                    } else { // just source is waiting
                        if(Stations[i].srcName == robSlot) { // source matches the writeback slot, done waiting
                            Stations[i].instruction.src.intValue = val;
                            Stations[i].isWaiting = false;
                        }
                    }
                } else if(Stations[i].destName < 0){ // just target is waiting
                    if(Stations[i].targetName == robSlot) { // target matches the writeback slot, done waiting
                        Stations[i].instruction.target.intValue = val;
                        Stations[i].isWaiting = false;
                    }
                } else if(Stations[i].srcName >= 0  && Stations[i].destName >= 0){
                    if(Stations[i].srcName == robSlot) { // if source matches writeback slot, read value
                        Stations[i].instruction.src.intValue = val;
                        Stations[i].srcName = -1;
                    }
                    if(Stations[i].destName == robSlot) {
                        Stations[i].instruction.dest.intValue = val;
                        Stations[i].destName = -1;
                    }
                    // done waiting if both operands match the writeback slot
                    if(Stations[i].srcName == robSlot && Stations[i].destName == robSlot) {
                        Stations[i].isWaiting = false;
                    }
                } else{
                    if(Stations[i].destName == robSlot) { // Dest matches the writeback slot, done waiting
                        Stations[i].instruction.dest.intValue = val;
                        Stations[i].isWaiting = false;
                    }
                }
            }
        }
    }
    
    public void writeback(int robSlot, Float val) {
        for(int i = 0; i < numStations; i++) {
            if(Stations[i].isWaiting) {
                if(Stations[i].srcName >= 0) {
                    if(Stations[i].targetName >= 0) { // both source and target are waiting
                        if(Stations[i].srcName == robSlot) { // if source matches writeback slot, read value
                            Stations[i].instruction.src.floatValue = val;
                            Stations[i].srcName = -1;
                        }
                        if(Stations[i].targetName == robSlot) { // if target matches writeback slot, read value
                            Stations[i].instruction.target.floatValue = val;
                            Stations[i].targetName = -1;
                        }
                        // done waiting if both operands match the writeback slot
                        if(Stations[i].srcName == robSlot && Stations[i].targetName == robSlot) {
                            Stations[i].isWaiting = false;
                        }
                    } else { // just source is waiting
                        if(Stations[i].srcName == robSlot) { // source matches the writeback slot, done waiting
                            Stations[i].instruction.src.floatValue = val;
                            Stations[i].isWaiting = false;
                        }
                    }
                } else { // just target is waiting
                    if(Stations[i].targetName == robSlot) { // target matches the writeback slot, done waiting
                        Stations[i].instruction.target.floatValue = val;
                        Stations[i].isWaiting = false;
                    }
                }
            }
        }
    }
    
    public void reserveStation(int i, Instruction instruction, int robSlot){
        Stations[i].isFree = false;
        Stations[i].finishedExec = false;
        Stations[i].instruction = instruction;
        Stations[i].instruction.robSlot = robSlot;
        Stations[i].instruction.stNum = i;
        // Gets register values if available
        scoreboard.loadOp(instruction.src);
        scoreboard.loadOp(instruction.target);
        Stations[i].destName = -1;
        if(instruction.op.startsWith("B", 0)){
            scoreboard.loadOp(instruction.dest);
            Stations[i].destName = scoreboard.getName(instruction.dest);
            if(Stations[i].destName != -1)
                Stations[i].isWaiting = true;
        }
            
        Stations[i].srcName = scoreboard.getName(instruction.src);
        Stations[i].targetName = scoreboard.getName(instruction.target);
        if(Stations[i].srcName != -1 || Stations[i].targetName != -1) {
            Stations[i].isWaiting = true;
        }
    }
    
}
