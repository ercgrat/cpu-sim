import java.util.*;

public class RegisterFile<T> {
    
    private final int NR = 32;
    private ArrayList<T> registers;
    
    public RegisterFile(T filler) {
        registers = new ArrayList<T>();
        for(int i = 0; i < NR; i++) {
            registers.add(filler);
        }
    }
    
    public void write(int index, T val) {
        if(index >= 0 && index < NR) {
            registers.set(index, val);
        }
    }
    
    public T read(int index) {
        if(index >= 0 && index < NR) {
            return registers.get(index);
        } else {
            return null;
        }
    }
    
    public String toSubmissionString(String type) {
        String result = "";
        for(int i = 0; i < registers.size(); i++) {
            result += type + "[" + i + "]=" + registers.get(i) + "\n";
        }
        return result;
    }
    
    public String toString() {
        return registers.toString();
    }
}