package assembler;

import java.util.ArrayList;
import java.util.List;

public class Assembler {

    public static List<Instruction> parse(String sourceCode) {
        List<Instruction> instructions = new ArrayList<>();

        // razbijam kod na redove. -1 kao limit cuva i eventualne prazne redove
        // na kraju stringa, tako da brojevi redova ostaju tacni, indeks u nizu + 1
        String[] lines = sourceCode.split("\n", -1);

        for (int i = 0; i < lines.length; i++) {
            int lineNumber = i + 1;
            String line = lines[i].trim();

            // prazan red ili red koji poslie trima postane prazan se ignorise
            if (line.isEmpty()) {
                continue;
            }

            // red se dijeli na najvise dva dijela, opcode i opc. operand,
            // razdvojene bilo kojim brojem razmaka
            String[] parts = line.split("\\s+", 2);
            String opcodeText = parts[0];
            String operandText = parts.length > 1 ? parts[1].trim() : null;

            OpCode opcode;
            try {
                opcode = OpCode.valueOf(opcodeText.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Red " + lineNumber + ": nepoznata instrukcija '" + opcodeText + "'");
            }

            boolean requiresOperand = opcode == OpCode.LOAD || opcode == OpCode.STORE
                    || opcode == OpCode.ADD || opcode == OpCode.SUB;

            int operand = 0;
            if (requiresOperand) {
                if (operandText == null || operandText.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Red " + lineNumber + ": instrukcija '" + opcode + "' zahtijeva operand, a nije zadat");
                }
                try {
                    operand = Integer.parseInt(operandText);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Red " + lineNumber + ": operand '" + operandText + "' nije validan cijeli broj");
                }
            }

            instructions.add(new Instruction(opcode, operand));
        }

        return instructions;
    }

    // pretvara listu instrukcija u niz brojeva, za svaku instrukciju upisuje
    // opcode.getCode() pa operand, jedno za drugim
    public static int[] toBinary(List<Instruction> instructions) {
        int[] binary = new int[instructions.size() * 2];

        for (int i = 0; i < instructions.size(); i++) {
            Instruction instruction = instructions.get(i);
            binary[i * 2] = instruction.getOpcode().getCode();
            binary[i * 2 + 1] = instruction.getOperand();
        }

        return binary;
    }

    // obrnuto od toBinary, cita niz po dva broja (opcode, operand) i pravi
    // listu instrukcija
    public static List<Instruction> fromBinary(int[] binary) {
        if (binary.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "Neispravan binarni zapis: dužina niza (" + binary.length + ") nije parna");
        }

        List<Instruction> instructions = new ArrayList<>();
        for (int i = 0; i < binary.length; i += 2) {
            OpCode opcode = OpCode.fromCode(binary[i]);
            int operand = binary[i + 1];
            instructions.add(new Instruction(opcode, operand));
        }

        return instructions;
    }
}
