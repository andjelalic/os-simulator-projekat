package assembler;

public enum OpCode {
    LOAD,
    STORE,
    ADD,
    SUB,
    PRINT,
    HALT;

    public int getCode() {
        switch (this) {
            case LOAD:
                return 0;
            case STORE:
                return 1;
            case ADD:
                return 2;
            case SUB:
                return 3;
            case PRINT:
                return 4;
            case HALT:
                return 5;
            default:
                throw new IllegalStateException("Nepoznat opcode: " + this);
        }
    }

    public static OpCode fromCode(int code) {
        switch (code) {
            case 0:
                return LOAD;
            case 1:
                return STORE;
            case 2:
                return ADD;
            case 3:
                return SUB;
            case 4:
                return PRINT;
            case 5:
                return HALT;
            default:
                throw new IllegalArgumentException("Nepoznat opcode broj: " + code);
        }
    }
}
