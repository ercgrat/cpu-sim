package simulator;

/** List of methods:
***
*** op(String inst)
*** unit(String inst)
*** isFloatType(String inst)
*** isImmediateType(String inst)
*** dest(String inst)
*** src(String inst)
*** target(String inst)
*** immediate(String inst)
*** isIntChar(char c)
***
**/

public class Instruction {
    
	private static final String delimiters = ", | |,"; // Split on comma space OR space or comma (order matters here; the largest delimiter needs to be first)
	
	// Returns the operation string
	public static String op(String instruction) {
		String[] tokens = instruction.split(delimiters);
        String op = tokens[0];
		return op;
	}
	
	// Returns a string representing the class of functional unit that the operation uses
	public static String unit(String instruction) {
        String op = op(instruction);
		String unit = "";
		switch(op) {
			case "DMUL":
				unit = "MULT";
				break;
			case "LD":
			case "L.D":
			case "SD":
			case "S.D":
				unit = "Load/Store";
				break;
			case "ADD.D":
			case "SUB.D":
			case "MUL.D":
			case "DIV.D":
				unit = "FPU";
				break;
			case "BEQZ":
			case "BNEZ":
			case "BEQ":
			case "BNE":
				unit = "BU";
				break;
			default:
				unit = "INT";
		}
		
		return unit;
    }
	
	// Returns a boolean indicating whether this instruction operates on floating point values
	public static boolean isFloatType(String instruction) {
        String op = op(instruction);
		if(op.substring(op.length()-2, op.length()).equals(".D")) {
			return true;
		}
		return false;
	}
	
	// Returns a boolean indicating whether this instruction operates on floating point values
	public static boolean isImmediateType(String instruction) {
        String op = op(instruction);
		if(op.charAt(op.length()-1) == 'I') {
			return true;
		}
		return false;
	}
	
	// Returns an integer representing the destination register (whether float or int type)
	public static int dest(String instruction) {
		String[] tokens = instruction.split(delimiters);
		String dest = tokens[1].substring(1,tokens[1].length());
		return Integer.parseInt(dest);
	}

	// Returns an integer representing the source register (whether float or int type) or -1 for branch instructions
	public static int src(String instruction) {
		String[] tokens = instruction.split(delimiters);
		String srcToken = tokens[2];
		
		if(srcToken.indexOf('R') >= 0 || srcToken.indexOf('F') >= 0) { // Instruction has a named register
			int regIndex = srcToken.indexOf('R'); // Get index of R
			if(regIndex == -1) {
				regIndex = srcToken.indexOf('F'); // Otherwise get index of F
			}
			
			String src = "" + srcToken.charAt(regIndex + 1); // Get first register number
			if(srcToken.length() > 2 && isIntChar(srcToken.charAt(regIndex + 2))) {
				src = src + srcToken.charAt(regIndex + 2); // Get second register number if present
			}
			
			return Integer.parseInt(src);
			
		} else {
			return -1;
		}
	}
	
	// Returns an integer representing the source offset for memory operations or null if not present
	public static Integer srcOffset(String instruction) {
		if(unit(instruction).equals("Load/Store")) {
			String[] tokens = instruction.split(delimiters);
			String srcToken = tokens[2];
			String srcOffset = "" + srcToken.charAt(0);
			int i = 1;
			while(true) {
				if(isIntChar(srcToken.charAt(i))) {
					srcOffset = srcOffset + srcToken.charAt(i);
				} else {
					break;
				}
				i++;
			}
			return Integer.parseInt(srcOffset);
		} else {
			return null;
		}
	}
	
	public static int target(String instruction) {
		String[] tokens = instruction.split(delimiters);
		if(tokens.length < 4) { // No target register
			return -1;
		}
		
		String targetToken = tokens[3];
		if(targetToken.charAt(0) == 'R' || targetToken.charAt(0) == 'F') {
			String target = tokens[3].substring(1, tokens[3].length());// Get first register number
			return Integer.parseInt(target);
		} else { // Is an immediate type instruction
			return -1;
		}
	}
	
	// Returns immediate value or null if not present
	public static Integer immediate(String instruction) {
		String[] tokens = instruction.split(delimiters);
		if(tokens.length < 4 || tokens[3].indexOf('R') >= 0 || tokens[3].indexOf('F') >= 0) {
			return null;
		} else {
			return Integer.parseInt(tokens[3]);
		}
	}
	
	// Returns true if the character is a digit from 0-9
	private static boolean isIntChar(char c) {
		return c > 47 && c < 58;
	}
}
