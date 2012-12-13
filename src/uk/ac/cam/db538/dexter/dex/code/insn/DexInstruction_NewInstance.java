package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21c;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexInstruction_NewInstance extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final DexClassType value;

  public DexInstruction_NewInstance(DexCode methodCode, DexRegister to, DexClassType value) {
    super(methodCode);

    this.regTo = to;
    this.value = value;
  }

  public DexInstruction_NewInstance(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction21c && insn.opcode == Opcode.NEW_INSTANCE) {

      val insnNewInstance = (Instruction21c) insn;
      regTo = parsingState.getRegister(insnNewInstance.getRegisterA());
      value = DexClassType.parse(
                ((TypeIdItem) insnNewInstance.getReferencedItem()).getTypeDescriptor(),
                parsingState.getCache());

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "new-instance v" + regTo.getOriginalIndexString() + ", " + value.getDescriptor();
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTo);
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    return createSet(new GcRangeConstraint(regTo, ColorRange.RANGE_8BIT));
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return new DexInstruction_NewInstance(getMethodCode(), mapping.get(regTo), value);
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    int rTo = state.getRegisterAllocation().get(regTo);

    if (fitsIntoBits_Unsigned(rTo, 8)) {
      return new Instruction[] {
               new Instruction21c(Opcode.NEW_INSTANCE, (short) rTo, state.getCache().getType(value))
             };
    } else
      return throwCannotAssembleException("No suitable instruction format found");
  }

  @Override
  public DexCodeElement[] instrument(DexCode_InstrumentationState state) {
    return new DexCodeElement[] {
             this,
             new DexInstruction_NewInstance(
               this.getMethodCode(),
               state.getTaintRegister(regTo),
               value)
           };
  }
}
