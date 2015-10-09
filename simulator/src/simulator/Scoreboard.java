package simulator;

import java.util.*;

public class Scoreboard {

    private final int numRegisters = 32;
    int[] intRegisterNames, floatRegisterNames;
    RegisterFile<Integer> intRegisters;
    RegisterFile<Double> floatRegisters;
    

    public Scoreboard(RegisterFile<Integer> intRegisters, RegisterFile<Double> floatRegisters) {
        this.intRegisters = intRegisters;
        this.floatRegisters = floatRegisters;
        intRegisterNames = new int[numRegisters];
        floatRegisterNames = new int[numRegisters];
        for(int i = 0; i < numRegisters; i++) {
            intRegisterNames[i] = -1; // Indicates that the register slot is available for reading
            floatRegisterNames[i] = -1; // Indicates that the register slot is available for reading
        }
    }
    
    public int getName(Instruction.Operand op) {
        if(op.registerType == null) {
            return -1;
        } else {
            if(op.registerType == Instruction.RegisterType.INT) {
                return intRegisterNames[op.registerIndex];
            } else {
                return floatRegisterNames[op.registerIndex];
            }
        }
    }
    
    // For the destination register, rename to the rob slot
    public void rename(Instruction.Operand dest, int robSlot) {
        if(dest.registerType == Instruction.RegisterType.INT) {
            intRegisterNames[dest.registerIndex] = robSlot;
        } else {
            floatRegisterNames[dest.registerIndex] = robSlot;
        }
    }
    
    // If the register is not renamed, read the value from the register into the operand
    public void loadOp(Instruction.Operand op) {
        if(op.registerType != null) {
            if(op.registerType == Instruction.RegisterType.INT) {
                if(intRegisterNames[op.registerIndex] == -1) {
                    op.intValue = intRegisters.read(op.registerIndex);
                }
            } else {
                if(floatRegisterNames[op.registerIndex] == -1) {
                    op.floatValue = floatRegisters.read(op.registerIndex);
                }
            }
        }
    }

    // Removes outdated names, only if there are no more pending writes to the register
    public void writeback(int robSlot) {
        for(int i = 0; i < numRegisters; i++) {
            if(intRegisterNames[i] == robSlot) {
                intRegisterNames[i] = -1;
                break;
            }
            if(floatRegisterNames[i] == robSlot) {
                floatRegisterNames[i] = -1;
                break;
            }
        }
    }
}