package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

public class DexInstruction_MoveResultWide extends DexInstruction {

  @Getter private final DexRegister regTo1;
  @Getter private final DexRegister regTo2;

  public DexInstruction_MoveResultWide(DexCode methodCode, DexRegister to1, DexRegister to2) {
    super(methodCode);

    regTo1 = to1;
    regTo2 = to2;
  }

  public DexInstruction_MoveResultWide(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn instanceof Instruction11x && insn.opcode == Opcode.MOVE_RESULT_WIDE) {

      val insnMove = (Instruction11x) insn;
      regTo1 = parsingState.getRegister(insnMove.getRegisterA());
      regTo2 = parsingState.getRegister(insnMove.getRegisterA() + 1);

    } else
      throw FORMAT_EXCEPTION;
  }

  public DexInstruction_MoveResultWide(DexInstruction_MoveResultWide toClone) {
    this(toClone.getMethodCode(),
         toClone.regTo1,
         toClone.regTo2);

    this.setOriginalElement(toClone.isOriginalElement());
  }

  @Override
  public String getOriginalAssembly() {
    return "move-result-wide " + regTo1.getOriginalIndexString() + "|" + regTo2.getOriginalIndexString();
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTo1, regTo2);
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
