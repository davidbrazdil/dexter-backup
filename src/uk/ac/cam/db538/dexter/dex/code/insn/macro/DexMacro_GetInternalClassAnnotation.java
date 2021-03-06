package uk.ac.cam.db538.dexter.dex.code.insn.macro;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstructionVisitor;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstClass;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;

public class DexMacro_GetInternalClassAnnotation extends DexMacro {

  // need to test the result for null (returns Annotation object)

  @Getter private final DexRegister regTo;
  @Getter private final DexRegister regClassName;

  public DexMacro_GetInternalClassAnnotation(DexCode methodCode, DexRegister regTo, DexRegister regClassName) {
    super(methodCode);
    this.regTo = regTo;
    this.regClassName = regClassName;
  }

  @Override
  public List<? extends DexCodeElement> unwrap() {
    val code = getMethodCode();
    val dex = getParentFile();
    val parsingCache = dex.getTypeCache();

    val typeInternalClassAnnotation = dex.getInternalClassAnnotation_Type();
    val typeClass = DexClassType.parse("Ljava/lang/Class;", parsingCache);
    val typeString = DexClassType.parse("Ljava/lang/String;", parsingCache);
    val typeAnnotation = DexClassType.parse("Ljava/lang/annotation/Annotation;", parsingCache);

    val regInspectedClass = new DexRegister();
    val regAnnotationClass = new DexRegister();

    return Arrays.asList(new DexCodeElement[] {
                           // regInspectedClass = Class.forName(regClassName)
                           new DexInstruction_Invoke(
                             code,
                             typeClass,
                             "forName",
                             new DexPrototype(typeClass, createList((DexRegisterType) typeString)),
                             createList(regClassName),
                             Opcode_Invoke.Static),
                           new DexInstruction_MoveResult(
                             code,
                             regInspectedClass,
                             true),
                           // regAnnotationClass = InternalClassAnnotation
                           new DexInstruction_ConstClass(
                             code,
                             regAnnotationClass,
                             typeInternalClassAnnotation),
                           // regTo = regInspectedClass.getAnnotation(regAnnotationClass)
                           new DexInstruction_Invoke(
                             code,
                             typeClass,
                             "getAnnotation",
                             new DexPrototype(typeAnnotation, createList((DexRegisterType) typeClass)),
                             createList(regInspectedClass, regAnnotationClass),
                             Opcode_Invoke.Virtual),
                           new DexInstruction_MoveResult(
                             code,
                             regTo,
                             true),
                         });
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
