package uk.ac.cam.db538.dexter.dex.code.insn;

public enum Opcode_IfTestZero {
  eqz,
  nez,
  ltz,
  gez,
  gtz,
  lez;

  public static Opcode_IfTestZero convert(org.jf.dexlib.Code.Opcode opcode) {
    switch (opcode) {
    case IF_EQZ:
      return eqz;
    case IF_NEZ:
      return nez;
    case IF_LTZ:
      return ltz;
    case IF_GEZ:
      return gez;
    case IF_GTZ:
      return gtz;
    case IF_LEZ:
      return lez;
    default:
      return null;
    }
  }

  public static org.jf.dexlib.Code.Opcode convert(Opcode_IfTestZero opcode) {
    switch (opcode) {
    case eqz:
      return org.jf.dexlib.Code.Opcode.IF_EQZ;
    case nez:
      return org.jf.dexlib.Code.Opcode.IF_NEZ;
    case ltz:
      return org.jf.dexlib.Code.Opcode.IF_LTZ;
    case gez:
      return org.jf.dexlib.Code.Opcode.IF_GEZ;
    case gtz:
      return org.jf.dexlib.Code.Opcode.IF_GTZ;
    case lez:
      return org.jf.dexlib.Code.Opcode.IF_LEZ;
    default:
      return null;
    }
  }
}