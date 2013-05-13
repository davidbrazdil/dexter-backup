package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction12x;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;

public class DexInstruction_UnaryOpWide extends DexInstruction {

  @Getter private final DexRegister regTo1;
  @Getter private final DexRegister regTo2;
  @Getter private final DexRegister regFrom1;
  @Getter private final DexRegister regFrom2;
  @Getter private final Opcode_UnaryOpWide insnOpcode;

  public DexInstruction_UnaryOpWide(DexCode methodCode, DexRegister to1, DexRegister to2, DexRegister from1, DexRegister from2, Opcode_UnaryOpWide opcode) {
    super(methodCode);
    regTo1 = to1;
    regTo2 = to2;
    regFrom1 = from1;
    regFrom2 = from2;
    insnOpcode = opcode;
  }

  public DexInstruction_UnaryOpWide(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) {
    super(methodCode);
    if (insn instanceof Instruction12x && Opcode_UnaryOpWide.convert(insn.opcode) != null) {

      val insnUnaryOp = (Instruction12x) insn;
      regTo1 = parsingState.getRegister(insnUnaryOp.getRegisterA());
      regTo2 = parsingState.getRegister(insnUnaryOp.getRegisterA() + 1);
      regFrom1 = parsingState.getRegister(insnUnaryOp.getRegisterB());
      regFrom2 = parsingState.getRegister(insnUnaryOp.getRegisterB() + 1);
      insnOpcode = Opcode_UnaryOpWide.convert(insn.opcode);

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return insnOpcode.getAssemblyName() + " " + regTo1.getOriginalIndexString() + "|" + regTo2.getOriginalIndexString()
           + ", " + regFrom1.getOriginalIndexString() + "|" + regFrom2.getOriginalIndexString();
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping, boolean toRefs, boolean toDefs) {
    val newTo1 = (toDefs) ? mapping.get(regTo1) : regTo1;
    val newTo2 = (toDefs) ? mapping.get(regTo2) : regTo2;
    val newFrom1 = (toRefs) ? mapping.get(regFrom1) : regFrom1;
    val newFrom2 = (toRefs) ? mapping.get(regFrom2) : regFrom2;
    return new DexInstruction_UnaryOpWide(getMethodCode(), newTo1, newTo2, newFrom1, newFrom2, insnOpcode);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    val code = getMethodCode();
    code.replace(this, new DexCodeElement[] {
                   this,
                   new DexInstruction_Move(code, state.getTaintRegister(regTo1), state.getTaintRegister(regFrom1), false)
                 });
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    val regAlloc = state.getRegisterAllocation();
    int rTo1 = regAlloc.get(regTo1);
    int rTo2 = regAlloc.get(regTo2);
    int rFrom1 = regAlloc.get(regFrom1);
    int rFrom2 = regAlloc.get(regFrom2);

    if (!formWideRegister(rTo1, rTo2) || !formWideRegister(rFrom1, rFrom2))
      return throwWideRegistersExpected();

    if (fitsIntoBits_Unsigned(rTo1, 4) && fitsIntoBits_Unsigned(rFrom1, 4))
      return new Instruction[] { new Instruction12x(Opcode_UnaryOpWide.convert(insnOpcode), (byte) rTo1, (byte) rFrom1) };
    else
      return throwNoSuitableFormatFound();
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    return createSet(
             new GcRangeConstraint(regTo1, ColorRange.RANGE_4BIT),
             new GcRangeConstraint(regFrom1, ColorRange.RANGE_4BIT));
  }

  @Override
  public Set<GcFollowConstraint> gcFollowConstraints() {
    return createSet(
             new GcFollowConstraint(regTo1, regTo2),
             new GcFollowConstraint(regFrom1, regFrom2));
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTo1, regTo2);
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regFrom1, regFrom2);
  }

  @Override
  public gcRegType gcReferencedRegisterType(DexRegister reg) {
    if (reg.equals(regFrom1))
      return gcRegType.PrimitiveWide_High;
    else if (reg.equals(regFrom2))
      return gcRegType.PrimitiveWide_Low;
    else
      return super.gcReferencedRegisterType(reg);
  }

  @Override
  public gcRegType gcDefinedRegisterType(DexRegister reg) {
    if (reg.equals(regTo1))
      return gcRegType.PrimitiveWide_High;
    else if (reg.equals(regTo2))
      return gcRegType.PrimitiveWide_Low;
    else
      return super.gcDefinedRegisterType(reg);
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
