package assembler;

public class Instruction {

    private OpCode opcode;
    private int operand;

    public Instruction(OpCode opcode, int operand) {
        this.opcode = opcode;
        this.operand = operand;
    }

    public OpCode getOpcode() {
        return opcode;
    }

    public int getOperand() {
        return operand;
    }

    @Override
    public String toString() {
        // PRINT i HALT nemaju operand pa se ispisuju samo sa imenom instrukcije
        if (opcode == OpCode.PRINT || opcode == OpCode.HALT) {
            return opcode.name();
        }
        return opcode.name() + " " + operand;
    }
}
