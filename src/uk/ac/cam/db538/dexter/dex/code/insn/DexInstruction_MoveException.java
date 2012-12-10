package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

public class DexInstruction_MoveException extends DexInstruction {

  @Getter private final DexRegister regTo;

  public DexInstruction_MoveException(DexCode methodCode, DexRegister to) {
    super(methodCode);

    regTo = to;
  }

  public DexInstruction_MoveException(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn instanceof Instruction11x && insn.opcode == Opcode.MOVE_EXCEPTION) {

      val insnMoveException = (Instruction11x) insn;
      regTo = parsingState.getRegister(insnMoveException.getRegisterA());

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }


  @Override
  public String getOriginalAssembly() {
    return "move-exception v" + regTo.getOriginalIndexString();
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    int rTo = state.getRegisterAllocation().get(regTo);

    if (fitsIntoBits_Unsigned(rTo, 8))
      return new Instruction[] {
               new Instruction11x(Opcode.MOVE_EXCEPTION, (short) rTo)
             };
    else
      return throwNoSuitableFormatFound();
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    val set = new HashSet<DexRegister>();
    set.add(regTo);
    return set;
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    val set = new HashSet<GcRangeConstraint>();
    set.add(new GcRangeConstraint(regTo, ColorRange.RANGE_8BIT));
    return set;
  }

}
