import java.util.*;

public class FetchUnit {

	private final int baseAddr = 1000;
	private int PC, NF;
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
	
	public FetchUnit(int NF, ArrayList<String> instructionMemory) {
		this.NF = NF;
		this.instructionMemory = instructionMemory;
		PC = baseAddr;
		cacheLine = new ArrayList<String>();
		instructionQueue = new ArrayList<String>();
		branchTable = new BranchTable();
	}
	
	public void cycle() {
		enqueue();
	}
	
	// Fetch a cache line and add to the instruction queue, checking for branch predictions/incrementing the PC
	private void enqueue() {
		int instIndex = (PC - baseAddr)/4;
		int cacheLinePotential = 4;
		if(cacheLine.size() > 0) { // Instructions are remaining on the cache line
			cacheLinePotential = cacheLine.size();
		} else { // Read 4 onto the cache line
			cacheLine.add(instructionMemory.get(instIndex));
			cacheLine.add(instructionMemory.get(instIndex + 1));
			cacheLine.add(instructionMemory.get(instIndex + 2));
			cacheLine.add(instructionMemory.get(instIndex + 3));
		}
		int numInsts = Math.min(NF - instructionQueue.size(), cacheLinePotential);
		
		// Move instructions from cache line to queue
		for(int i = 0; i < numInsts; i++) {
			String inst = cacheLine.get(0);
			cacheLine.remove(0);
			PC = PC + 4;
			int PCIndex = (PC - baseAddr)/4;
			
			if(branchTable.hasEntry(PCIndex) && branchTable.branchCondition(PCIndex)) {
				// Do branch stuff here
				PC = branchTable.targetAddress(PCIndex);
				cacheLine.clear();
				cacheLine.add(instructionMemory.get(PCIndex));
				cacheLine.add(instructionMemory.get(PCIndex + 1));
				cacheLine.add(instructionMemory.get(PCIndex + 2));
				cacheLine.add(instructionMemory.get(PCIndex + 3));
				break;
			}
			instructionQueue.add(inst);
		}
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
	
	public void setBranchPrediction(int address, boolean branchCondition, Integer targetAddress) {
		branchTable.makeEntry(address, branchCondition, targetAddress);
	}
}