package uk.ac.cam.db538.dexter.dex.code.insn;

import lombok.Getter;

public enum Opcode_BinaryOpWide {
  AddLong("add-long"),
  SubLong("sub-long"),
  MulLong("mul-long"),
  DivLong("div-long"),
  RemLong("rem-long"),
  AndLong("and-long"),
  OrLong("or-long"),
  XorLong("xor-long"),
  ShlLong("shl-long"),
  ShrLong("shr-long"),
  UshrLong("ushr-long"),
  AddDouble("add-double"),
  SubDouble("sub-double"),
  MulDouble("mul-double"),
  DivDouble("div-double"),
  RemDouble("rem-double");

  @Getter private final String AssemblyName;

  private Opcode_BinaryOpWide(String assemblyName) {
    AssemblyName = assemblyName;
  }

  public static Opcode_BinaryOpWide convert(org.jf.dexlib.Code.Opcode opcode) {
    switch (opcode) {
    case ADD_LONG:
    case ADD_LONG_2ADDR:
      return AddLong;
    case SUB_LONG:
    case SUB_LONG_2ADDR:
      return SubLong;
    case MUL_LONG:
    case MUL_LONG_2ADDR:
      return MulLong;
    case DIV_LONG:
    case DIV_LONG_2ADDR:
      return DivLong;
    case REM_LONG:
    case REM_LONG_2ADDR:
      return RemLong;
    case AND_LONG:
    case AND_LONG_2ADDR:
      return AndLong;
    case OR_LONG:
    case OR_LONG_2ADDR:
      return OrLong;
    case XOR_LONG:
    case XOR_LONG_2ADDR:
      return XorLong;
    case SHL_LONG:
    case SHL_LONG_2ADDR:
      return ShlLong;
    case SHR_LONG:
    case SHR_LONG_2ADDR:
      return ShrLong;
    case USHR_LONG:
    case USHR_LONG_2ADDR:
      return UshrLong;
    case ADD_DOUBLE:
    case ADD_DOUBLE_2ADDR:
      return AddDouble;
    case SUB_DOUBLE:
    case SUB_DOUBLE_2ADDR:
      return SubDouble;
    case MUL_DOUBLE:
    case MUL_DOUBLE_2ADDR:
      return MulDouble;
    case DIV_DOUBLE:
    case DIV_DOUBLE_2ADDR:
      return DivDouble;
    case REM_DOUBLE:
    case REM_DOUBLE_2ADDR:
      return RemDouble;
    default:
      return null;
    }
  }

  public static org.jf.dexlib.Code.Opcode convert(Opcode_BinaryOpWide opcode) {
    switch (opcode) {
    case AddLong:
      return org.jf.dexlib.Code.Opcode.ADD_LONG;
    case SubLong:
      return org.jf.dexlib.Code.Opcode.SUB_LONG;
    case MulLong:
      return org.jf.dexlib.Code.Opcode.MUL_LONG;
    case DivLong:
      return org.jf.dexlib.Code.Opcode.DIV_LONG;
    case RemLong:
      return org.jf.dexlib.Code.Opcode.REM_LONG;
    case AndLong:
      return org.jf.dexlib.Code.Opcode.AND_LONG;
    case OrLong:
      return org.jf.dexlib.Code.Opcode.OR_LONG;
    case XorLong:
      return org.jf.dexlib.Code.Opcode.XOR_LONG;
    case ShlLong:
      return org.jf.dexlib.Code.Opcode.SHL_LONG;
    case ShrLong:
      return org.jf.dexlib.Code.Opcode.SHR_LONG;
    case UshrLong:
      return org.jf.dexlib.Code.Opcode.USHR_LONG;
    case AddDouble:
      return org.jf.dexlib.Code.Opcode.ADD_DOUBLE;
    case SubDouble:
      return org.jf.dexlib.Code.Opcode.SUB_DOUBLE;
    case MulDouble:
      return org.jf.dexlib.Code.Opcode.MUL_DOUBLE;
    case DivDouble:
      return org.jf.dexlib.Code.Opcode.DIV_DOUBLE;
    case RemDouble:
      return org.jf.dexlib.Code.Opcode.REM_DOUBLE;
    default:
      return null;
    }
  }

  public static org.jf.dexlib.Code.Opcode convert2addr(Opcode_BinaryOpWide opcode) {
    switch (opcode) {
    case AddLong:
      return org.jf.dexlib.Code.Opcode.ADD_LONG_2ADDR;
    case SubLong:
      return org.jf.dexlib.Code.Opcode.SUB_LONG_2ADDR;
    case MulLong:
      return org.jf.dexlib.Code.Opcode.MUL_LONG_2ADDR;
    case DivLong:
      return org.jf.dexlib.Code.Opcode.DIV_LONG_2ADDR;
    case RemLong:
      return org.jf.dexlib.Code.Opcode.REM_LONG_2ADDR;
    case AndLong:
      return org.jf.dexlib.Code.Opcode.AND_LONG_2ADDR;
    case OrLong:
      return org.jf.dexlib.Code.Opcode.OR_LONG_2ADDR;
    case XorLong:
      return org.jf.dexlib.Code.Opcode.XOR_LONG_2ADDR;
    case ShlLong:
      return org.jf.dexlib.Code.Opcode.SHL_LONG_2ADDR;
    case ShrLong:
      return org.jf.dexlib.Code.Opcode.SHR_LONG_2ADDR;
    case UshrLong:
      return org.jf.dexlib.Code.Opcode.USHR_LONG_2ADDR;
    case AddDouble:
      return org.jf.dexlib.Code.Opcode.ADD_DOUBLE_2ADDR;
    case SubDouble:
      return org.jf.dexlib.Code.Opcode.SUB_DOUBLE_2ADDR;
    case MulDouble:
      return org.jf.dexlib.Code.Opcode.MUL_DOUBLE_2ADDR;
    case DivDouble:
      return org.jf.dexlib.Code.Opcode.DIV_DOUBLE_2ADDR;
    case RemDouble:
      return org.jf.dexlib.Code.Opcode.REM_DOUBLE_2ADDR;
    default:
      return null;
    }
  }
}