package simulator;

public class Instruction {
	
	private static final String delimiters = ", | |,"; // Split on comma space OR space or comma (order matters here; the largest delimiter needs to be first)
	
        int stNum;
        int robSlot;
        int memoryAddress;
	int address;
	boolean branchCondition;
	String op, unit;
	Operand dest, src, target;
	
	public enum RegisterType {
		INT, FLOAT
	}
	
	public class Operand {
		Integer intValue;
        Float floatValue;
		Integer registerIndex;
		RegisterType registerType;
		
        public Operand() {
        }
        
		public Operand(Integer value) {
			this.intValue = value;
		}

        public Operand(Integer registerIndex, RegisterType registerType) {
            this.registerIndex = registerIndex;
			this.registerType = registerType;
        }
		
		public String toString() {
			return "{ intValue: " + intValue + ", floatValue: " + floatValue + ", RegIndex: " + registerIndex + ", RegType: " + registerType + " }";
		}
	}
	
	public Instruction(String instruction, int address) {
		this.op = op(instruction);
		this.unit = unit(instruction);
		this.dest = dest(instruction);
		this.src = src(instruction);
		this.target = target(instruction);
		this.address = address;
	}
    
	// Returns the operation string
	private String op(String instruction) {
		String[] tokens = instruction.split(delimiters);
        String op = tokens[0];
		return op;
	}
	
	// Returns a string representing the class of functional unit that the operation uses
	private String unit(String instruction) {
        String op = op(instruction);
		String unit = "";
		switch(op) {
			case "DMUL":
				unit = "MULT";
				break;
			case "LD":
			case "L.D":
                unit = "Load";
                break;
			case "SD":
			case "S.D":
				unit = "Store";
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
	
	private Operand dest(String instruction) {
		String[] tokens = instruction.split(delimiters);
		Integer destReg = Integer.parseInt(tokens[1].substring(1,tokens[1].length()));
		char destTypeChar = tokens[1].charAt(0);
		RegisterType destType = RegisterType.INT;
		if(destTypeChar == 'F') {
			destType = RegisterType.FLOAT;
		}
		return new Operand(destReg, destType);
	}

	private Operand src(String instruction) {
		String[] tokens = instruction.split(delimiters);
		String srcToken = tokens[2];
		
		if(srcToken.indexOf('R') >= 0 || srcToken.indexOf('F') >= 0) { // Instruction has a named register
			int regIndex = srcToken.indexOf('R'); // Get index of R
			RegisterType srcType = RegisterType.INT;
			if(regIndex == -1) {
				regIndex = srcToken.indexOf('F'); // Otherwise get index of F
				srcType = RegisterType.FLOAT;
			}
			
			String srcString = "" + srcToken.charAt(regIndex + 1); // Get first register number
			if(srcToken.length() > 2 && isIntChar(srcToken.charAt(regIndex + 2))) {
				srcString = srcString + srcToken.charAt(regIndex + 2); // Get second register number if present
			}
			Integer srcReg = Integer.parseInt(srcString);
			return new Operand(srcReg, srcType);
			
		} else {
			int branchTarget = Integer.parseInt(srcToken);
			return new Operand(branchTarget);
		}
	}
	
	private Operand target(String instruction) {
		String[] tokens = instruction.split(delimiters);
		if(tokens.length < 4) { // No target register, but will store the source offset immediate as the target if present
			String srcToken = tokens[2];
			if(srcToken.indexOf('R') > 1 || srcToken.indexOf('F') > 1) { // token has format like 0(R0)
				String srcOffset = "" + srcToken.charAt(0);
				int i = 1;
				while(true) {
					char nextChar = srcToken.charAt(i);
					if(isIntChar(nextChar)) { // nextChar is a digit 0-9
						srcOffset = srcOffset + nextChar;
					} else {
						break;
					}
					i++;
				}
				Integer target = Integer.parseInt(srcOffset);
				return new Operand(target);
			} else {
				return null;
			}
		}
		
		// Normal target parsing
		String targetToken = tokens[3];
		if(targetToken.charAt(0) == 'R' || targetToken.charAt(0) == 'F') {
			String targetString = tokens[3].substring(1, tokens[3].length());// Get first register number
			Integer targetReg = Integer.parseInt(targetString);
			RegisterType targetType = RegisterType.INT;
			if(targetToken.charAt(0) == 'F') {
				targetType = RegisterType.FLOAT;
			}
			return new Operand(targetReg, targetType);
		} else { // Is an immediate type instruction
			Integer target = Integer.parseInt(tokens[3]);
			return new Operand(target);
		}
	}
	
	private boolean isIntChar(char c) {
		return c > 47 && c < 58; // c is a digit 0-9
	}
	
	public String toString() {
		return "{ op: " + op + ", unit: " + unit + ", dest: " + dest + ", src: " + src + ", target: " + target + ", address: " + address + "}";
	}
}
