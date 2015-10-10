package simulator;

import java.util.*;

public class DecodeUnit {
	
	public FetchUnit fetchUnit;
	private int ND, NI;
	private ArrayList<Instruction> issueQueue;
	
	public DecodeUnit(int ND, int NI, FetchUnit fetchUnit) {
		this.ND = ND;
		this.NI = NI;
		this.fetchUnit = fetchUnit;
		issueQueue = new ArrayList<Instruction>();
	}
	
	public void cycle() {
		int numInst = Math.min(NI - issueQueue.size(), ND);
		ArrayList<String> dequeuedInstructions = fetchUnit.dequeue(numInst);
		for(String instString : dequeuedInstructions) {
			String[] tokens = instString.split(";");
			Instruction inst = new Instruction(tokens[0], Integer.parseInt(tokens[1]));
			if(tokens.length > 2) {
				inst.branchCondition = Boolean.parseBoolean(tokens[2]);
			}
			renameRegisters(inst);
			issueQueue.add(inst);
		}
	}
	
	private void renameRegisters(Instruction inst) {
		/***
			To be filled in when ROB and register files exist.
		***/
	}
	
	public Instruction peek() {
		if(issueQueue.size() > 0) {
			return issueQueue.get(0);
		} else {
			return null;
		}
	}
	
	public void dequeue() {
		issueQueue.remove(0);
	}
}