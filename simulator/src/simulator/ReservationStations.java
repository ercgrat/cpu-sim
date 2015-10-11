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
    
    public ReservationStations(Scoreboard scoreboard){
        Stations = new Station[numStations];
        for(int i = 0; i < numStations; i++){
            this.Stations[i].isFree = true;
            this.Stations[i].justFreed = false;
            this.Stations[i].instruction = null;
            this.Stations[i].finishedExec = true;
            this.Stations[i].isWaiting = false;
            if(i<2)
                this.Stations[i].unit = "INT0";
            else if(i<4)
                this.Stations[i].unit = "INT1";
            else if(i<6)
                this.Stations[i].unit = "MULT";
            else if(i<9)
                this.Stations[i].unit = "Load";
            else if(i<12)
                this.Stations[i].unit = "Store";
            else if(i<17)
                this.Stations[i].unit = "FPU";
            else
                this.Stations[i].unit = "BU";
            
        }
    }
    
    public class Station{
        boolean isFree;
        boolean justFreed;
        boolean finishedExec;
        boolean isWaiting;
        String unit;
        Instruction instruction;
        int srcName;
        int targetName;
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
        return 0;
    }
    
    public int isReady(String Unit){
        switch(Unit){
            case "INT0":
                for(int i=0;i<2;i++){
                    if(!Stations[i].isFree && !Stations[i].isWaiting && Stations[i].instruction!=null)
                        return i;
                }
                break;
            case "INT1":
                for(int i=2;i<4;i++){
                    if(!Stations[i].isFree && !Stations[i].isWaiting && Stations[i].instruction!=null)
                        return i;
                }
                break;
            case "MULT":
                for(int i=4;i<6;i++){
                    if(!Stations[i].isFree && !Stations[i].isWaiting && Stations[i].instruction!=null)
                        return i;
                }
                break;
            case "Load/Store":
                for(int i=6;i<12;i++){
                    if(!Stations[i].isFree && !Stations[i].isWaiting && Stations[i].instruction!=null)
                        return i;
                }
                break;
            case "FPU":
                for(int i=12;i<17;i++){
                    if(!Stations[i].isFree && !Stations[i].isWaiting && Stations[i].instruction!=null)
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
        return 0;
    }
    
    public void finishedExecution(int stNum){
        Stations[stNum].finishedExec = true;
    }
    
    public void writeback(int robSlot, Integer val) {
        for(int i = 0; i < numStations; i++) {
            if(Stations[i].isWaiting) {
                if(Stations[i].srcName >= 0) {
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
                } else { // just target is waiting
                    if(Stations[i].targetName == robSlot) { // target matches the writeback slot, done waiting
                        Stations[i].instruction.target.intValue = val;
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
    
    public void reserveStation(int i, Instruction instruction){
        Stations[i].isFree = false;
        Stations[i].finishedExec = false;
        Stations[i].instruction = instruction;
        
        // Gets register values if available
        scoreboard.loadOp(instruction.src);
        scoreboard.loadOp(instruction.target);
        
        Stations[i].srcName = scoreboard.getName(instruction.src);
        Stations[i].targetName = scoreboard.getName(instruction.target);
        if(Stations[i].srcName != -1 || Stations[i].targetName != -1) {
            Stations[i].isWaiting = true;
        }
    }
    
}
