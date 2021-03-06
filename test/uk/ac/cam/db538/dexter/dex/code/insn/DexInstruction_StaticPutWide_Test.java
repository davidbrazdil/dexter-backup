package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21c;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_StaticPutWide_Test {

  @Test
  public void testParse_StaticPutWide() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction21c(Opcode.SPUT_WIDE, (short) 240, Utils.getFieldItem("Lcom/example/MyClass5;", "J", "TestField5")),
        new Instruction21c(Opcode.SPUT_WIDE, (short) 241, Utils.getFieldItem("Lcom/example/MyClass6;", "D", "TestField6"))
      }, new String[] {
        "sput-wide v240, com.example.MyClass5.TestField5",
        "sput-wide v241, com.example.MyClass6.TestField6",
      });
  }

  @Test(expected=InstructionArgumentException.class)
  public void testParse_StaticPutWide_WrongType() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction21c(Opcode.SPUT_WIDE, (short) 236, Utils.getFieldItem("Lcom/example/MyClass1;", "I", "TestField1")),
      "");
  }
}
