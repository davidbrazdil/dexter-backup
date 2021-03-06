package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction23x;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_ArrayPutWide_Test {

  @Test
  public void testParse_ArrayPutWide() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction23x(Opcode.APUT_WIDE, (short) 200, (short) 201, (short) 202),
        new Instruction23x(Opcode.APUT_WIDE, (short) 203, (short) 204, (short) 205)
      }, new String[] {
        "aput-wide v200, {v201}[v202]",
        "aput-wide v203, {v204}[v205]",
      });
  }
}
