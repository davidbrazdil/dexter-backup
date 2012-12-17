package uk.ac.cam.db538.dexter.dex.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.CodeItem;
import org.jf.dexlib.CodeItem.EncodedCatchHandler;
import org.jf.dexlib.CodeItem.TryItem;
import org.jf.dexlib.DebugInfoItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.analysis.coloring.GraphColoring;
import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.DexInstrumentationCache;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveWide;
import uk.ac.cam.db538.dexter.dex.code.insn.InstructionParsingException;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;
import uk.ac.cam.db538.dexter.utils.NoDuplicatesList;

public abstract class DexMethodWithCode extends DexMethod {

  @Getter protected DexCode code;
  @Getter private final boolean direct;

  private final NoDuplicatesList<DexRegister> parameterRegisters;
  private final Set<DexRegister> codeRegistersForParameters;
  private final DexCode parameterMoveInstructions;

  public DexMethodWithCode(DexClass parent, String name, Set<AccessFlags> accessFlags,
                           DexPrototype prototype, DexCode code,
                           boolean direct) {
    super(parent, name, accessFlags, prototype);
    this.code = code;
    this.direct = direct;
    this.parameterRegisters = this.getPrototype().generateParameterRegisters(this.isStatic());
    this.parameterMoveInstructions = new DexCode();
    this.codeRegistersForParameters = new HashSet<DexRegister>();
  }

  public DexMethodWithCode(DexClass parent, EncodedMethod methodInfo) throws UnknownTypeException, InstructionParsingException {
    super(parent, methodInfo);
    if (methodInfo.codeItem == null)
      code = new DexCode(this);
    else
      code = new DexCode(methodInfo.codeItem, this, parent.getParentFile().getParsingCache());
    direct = methodInfo.isDirect();
    parameterRegisters = this.getPrototype().generateParameterRegisters(this.isStatic());
    parameterMoveInstructions = new DexCode();
    this.codeRegistersForParameters = new HashSet<DexRegister>();

    val prototype = this.getPrototype();
    val isStatic = this.isStatic();
    val clazz = this.getParentClass();

    System.out.println("Method: " + this.getName());

    // create the parameter-register mappings
    val regCount = methodInfo.codeItem.getRegisterCount();
    val paramCount = prototype.getParameterCount(isStatic);
    for (int i = 0; i < paramCount; ++i) {
      val paramRegId = prototype.getParameterRegisterId(i, regCount, isStatic);
      val paramType = prototype.getParameterType(i, isStatic, clazz);
      System.out.println("  param #" + i + ": " + paramType.getPrettyName() + ", v" + paramRegId);
      switch (paramType.getTypeSize()) {
      case SINGLE:
        val regSingle = code.getRegisterByOriginalNumber(paramRegId);
        addParameterMapping_Single(i, regSingle);
        break;
      case WIDE:
        val regWide1 = code.getRegisterByOriginalNumber(paramRegId);
        val regWide2 = code.getRegisterByOriginalNumber(paramRegId + 1);
        addParameterMapping_Wide(i, regWide1, regWide2);
        break;
      }
    }
  }

  public void addParameterMapping_Single(int paramIndex, DexRegister codeReg) {
    if (!code.getUsedRegisters().contains(codeReg))
      return;

    val paramType = this.getPrototype().getParameterType(paramIndex, this.isStatic(), this.getParentClass());

    val regIndex = this.getPrototype().getFirstParameterRegisterIndex(paramIndex, isStatic());
    val paramReg = parameterRegisters.get(regIndex);

    parameterMoveInstructions.add(new DexInstruction_Move(code, codeReg, paramReg, paramType instanceof DexReferenceType));

    codeRegistersForParameters.add(codeReg);
  }

  public void addParameterMapping_Wide(int paramIndex, DexRegister codeReg1, DexRegister codeReg2) {
    if (!code.getUsedRegisters().contains(codeReg1) && !code.getUsedRegisters().contains(codeReg2))
      return;

    val firstRegIndex = this.getPrototype().getFirstParameterRegisterIndex(paramIndex, isStatic());

    val paramReg1 = parameterRegisters.get(firstRegIndex);
    val paramReg2 = parameterRegisters.get(firstRegIndex + 1);

    parameterMoveInstructions.add(new DexInstruction_MoveWide(code, codeReg1, codeReg2, paramReg1, paramReg2));

    codeRegistersForParameters.add(codeReg1);
    codeRegistersForParameters.add(codeReg2);
  }

  @Override
  public boolean isVirtual() {
    return !direct;
  }

  @Override
  public void instrument(DexInstrumentationCache cache) {
    code.instrument(cache);
  }

  @Override
  protected CodeItem generateCodeItem(DexFile outFile, DexAssemblingCache cache) {
    // do register allocation
    // note that this changes the code itself
    // (adds temporaries, inserts move instructions)
    val codeColoring = new GraphColoring(code); // changes the code itself (potentially)

    // add parameter registers to the register allocation
    val registerAllocation = new HashMap<DexRegister, Integer>(codeColoring.getColoring());
    int registerCount = codeColoring.getColorsUsed();
    val inWords = this.getPrototype().countParamWords(this.isStatic());
    if (registerCount >= inWords) {
      int startReg = registerCount - inWords;
      for (int i = 0; i < inWords; ++i)
        registerAllocation.put(parameterRegisters.get(i), startReg + i);
    } else {
      for (int i = 0; i < inWords; ++i)
        registerAllocation.put(parameterRegisters.get(i), i);
      registerCount = inWords;
    }

    // sometimes a register is not used in the code
    // and thus would not get allocated...
    // but if it's mapped to a parameter, assembling
    // the move instruction would fail...
    // so add these into the register allocation...
    // the color doesn't matter
    for (val reg : codeRegistersForParameters)
      if (!registerAllocation.containsKey(reg))
        registerAllocation.put(reg, 0);

    val assembledMoveInstructions = parameterMoveInstructions.assembleBytecode(registerAllocation, cache, 0);
    val assembledCode = code.assembleBytecode(registerAllocation, cache, assembledMoveInstructions.getTotalCodeLength());

    List<Instruction> instructions = new ArrayList<Instruction>();
    instructions.addAll(assembledMoveInstructions.getInstructions());
    instructions.addAll(assembledCode.getInstructions());

    List<TryItem> tries = new ArrayList<TryItem>();
    tries.addAll(assembledMoveInstructions.getTries());
    tries.addAll(assembledCode.getTries());

    List<EncodedCatchHandler> catchHandlers = new ArrayList<EncodedCatchHandler>();
    catchHandlers.addAll(assembledMoveInstructions.getCatchHandlers());
    catchHandlers.addAll(assembledCode.getCatchHandlers());

    int outWords = code.getOutWords();

    DebugInfoItem debugInfo = null;

    return CodeItem.internCodeItem(outFile, registerCount, inWords, outWords, debugInfo, instructions, tries, catchHandlers);
  }
}
