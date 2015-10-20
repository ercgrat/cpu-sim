package simulator;

import java.util.*;

public class FetchUnit {

	private final int baseAddr = 1000;
	private int PC, NF, NQ;
	private ArrayList<String> cacheLine;
	private ArrayList<String> instructionQueue;
	private ArrayList<String> instructionMemory;
	BranchTable branchTable;
	
	private class BranchTable {
		private final int branchTableSize = 32;
		int[] instructions;
		int[] targetAddresses;
		boolean[] branchConditions;
		
		public BranchTable() {
			instructions = new int[branchTableSize];
			targetAddresses = new int[branchTableSize];
			branchConditions = new boolean[branchTableSize];
		}
		
		void makeEntry(int address, boolean branchCondition, Integer targetAddress) {
			int index = address % branchTableSize;
			instructions[index] = address;
			branchConditions[index] = branchCondition;
			if(branchCondition && targetAddress != null) {
				targetAddresses[index] = targetAddress;
			}
		}
		
		boolean hasEntry(int instructionIndex) {
			int index = instructionIndex % branchTableSize;
			if(instructions[index] == instructionIndex) {
				return true;
			}
			return false;
		}
		
		int targetAddress(int instructionIndex) {
			int index = instructionIndex % branchTableSize;
			return targetAddresses[index];
		}
		
		boolean branchCondition(int instructionIndex) {
			int index = instructionIndex % branchTableSize;
			return branchConditions[index];
		}
	}
	
        public int[] dumpInstructionAddresses(){
            return branchTable.instructions;
        }
        
        public int[] dumpTargetAddresses(){
            return branchTable.targetAddresses;
        } 
        
        public boolean[] dumpOutcomes(){
            return branchTable.branchConditions;
        } 
	public FetchUnit(int NF, int NQ, ArrayList<String> instructionMemory) {
		this.NF = NF;
		this.NQ = NQ;
		this.instructionMemory = instructionMemory;
		PC = baseAddr;
		cacheLine = new ArrayList<String>();
		instructionQueue = new ArrayList<String>();
		branchTable = new BranchTable();
	}
	
	public boolean cycle() {
		return enqueue();
	}
	
	// Fetch a cache line and add to the instruction queue, checking for branch predictions/incrementing the PC
	private boolean enqueue() {
		int instIndex = (PC - baseAddr)/4;
		int cacheLinePotential = 4;
		if(cacheLine.size() > 0) { // Instructions are remaining on the cache line
			cacheLinePotential = cacheLine.size();
		} else { // Read 4 onto the cache line
            for(int i = 0; i < 4; i++) {
                if(instIndex + i < instructionMemory.size()) {
                    cacheLine.add(instructionMemory.get(instIndex + i));
                } else {
                    break;
                }
            }
		}
        
        boolean readingInstructions = (cacheLine.size() == 0);
		int numInsts = Math.min(Math.min(NQ - instructionQueue.size(), cacheLinePotential), cacheLine.size());
		
		// Move instructions from cache line to queue
		for(int i = 0; i < numInsts; i++) {
			String inst = cacheLine.get(0);
			cacheLine.remove(0);
			PC = PC + 4;
			int PCIndex = (PC - baseAddr)/4;
			
			if(branchTable.hasEntry(PCIndex)) {
				boolean branchCondition = branchTable.branchCondition(PCIndex);
                //System.out.println("Fetched branch: " + inst + ";" + PCIndex + ";" + branchCondition);
				instructionQueue.add(inst + ";" + PCIndex + ";" + branchCondition);
				if(branchCondition == true) {
					// Do branch stuff here
					PC = branchTable.targetAddress(PCIndex);
					PCIndex = (PC - baseAddr)/4;
					cacheLine.clear();
					cacheLine.add(instructionMemory.get(PCIndex));
					cacheLine.add(instructionMemory.get(PCIndex + 1));
					cacheLine.add(instructionMemory.get(PCIndex + 2));
					cacheLine.add(instructionMemory.get(PCIndex + 3));
					break;
				}
			} else {
				instructionQueue.add(inst + ";" + PCIndex);
			}
		}
        
        return readingInstructions;
	}
	
	public ArrayList<String> dequeue(int numRequested) {
		ArrayList<String> dequeuedInstructions = new ArrayList<String>();
		if(numRequested <= 0) {
			return dequeuedInstructions;
		}
		int numInsts = Math.min(Math.min(numRequested, NF), instructionQueue.size());
		for(int i = 0; i < numInsts; i++) {
			dequeuedInstructions.add(instructionQueue.get(0));
			instructionQueue.remove(0);
		}
		return dequeuedInstructions;
	}
	
        public boolean isFetchQEmpty(){
            if(instructionQueue.size() > 0)
                return false;
            return true;
        }
        
	public void flush(Instruction branch){
		int targetAddress;
		if(branch.target == null) {
			targetAddress = branch.src.intValue;
		} else {
			targetAddress = branch.target.intValue;
		}
		targetAddress = targetAddress * 4 + baseAddr;
		branchTable.makeEntry(branch.instructionAddress, branch.branchCondition, targetAddress);
		
		instructionQueue.clear();
        cacheLine.clear();
		if(branch.branchCondition)
			PC = targetAddress;
		else
			PC = (branch.instructionAddress)*4 + baseAddr;
	}
}