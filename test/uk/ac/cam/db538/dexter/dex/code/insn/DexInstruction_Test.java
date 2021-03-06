package uk.ac.cam.db538.dexter.dex.code.insn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.Set;

import lombok.val;

import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.Dex;
import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCatch;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCatchAll;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexTryBlockEnd;
import uk.ac.cam.db538.dexter.dex.code.elem.DexTryBlockStart;
import uk.ac.cam.db538.dexter.dex.method.DexDirectMethod;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;
import uk.ac.cam.db538.dexter.dex.type.DexVoid;

public class DexInstruction_Test {

  private static boolean execThrowingInsn_CanExitMethod(DexInstruction insn) {
    try {
      Method m = DexInstruction.class.getDeclaredMethod("throwingInsn_CanExitMethod");
      m.setAccessible(true);
      return (Boolean) m.invoke(insn);
    } catch (Exception e) {
      e.printStackTrace(System.err);
      fail("Couldn't execute method: " + e.getClass().getSimpleName());
      return false;
    }
  }

  private static DexCode createDexCode() {
    val dex = new Dex();
    val clazz = new DexClass(dex,
                             DexClassType.parse("Lcom/example/Clazz;", dex.getTypeCache()),
                             DexClassType.parse("Ljava/lang/Object;", dex.getTypeCache()),
                             null, null, null, null, null);

    val code = new DexCode();

    // method will automatically assign itself to DexCode
    new DexDirectMethod(clazz, "m", null,
                        new DexPrototype(DexVoid.parse("V", null), null),
                        code, null, null);

    return code;
  }

  @Test
  public void testThrowingInsn_CanExitMethod_NoTryBlocks() {
    // we pretend that NOP can throw an exception

    val code = createDexCode();

    val nop = new DexInstruction_Nop(code);
    code.add(nop);

    assertTrue(execThrowingInsn_CanExitMethod(nop));
  }

  @Test
  public void testThrowingInsn_CanExitMethod_NotInsideTryBlock() {
    // we pretend that NOP can throw an exception

    val code = createDexCode();
    val nop = new DexInstruction_Nop(code);
    val tryStart = new DexTryBlockStart(code);
    val tryEnd = new DexTryBlockEnd(code, tryStart);

    code.add(nop);
    code.add(tryStart);
    code.add(tryEnd);

    assertTrue(execThrowingInsn_CanExitMethod(nop));
  }

  @Test
  public void testThrowingInsn_CanExitMethod_InsideTryBlockWithoutCatchHandlers() {
    // we pretend that NOP can throw an exception

    val code = createDexCode();
    val nop = new DexInstruction_Nop(code);
    val tryStart = new DexTryBlockStart(code);
    val tryEnd = new DexTryBlockEnd(code, tryStart);

    code.add(tryStart);
    code.add(nop);
    code.add(tryEnd);

    assertTrue(execThrowingInsn_CanExitMethod(nop));
  }

  @Test
  public void testThrowingInsn_CanExitMethod_InsideTryBlockWithCatchAllHandler() {
    // we pretend that NOP can throw an exception

    val code = createDexCode();
    val nop = new DexInstruction_Nop(code);
    val tryStart = new DexTryBlockStart(code);
    val tryEnd = new DexTryBlockEnd(code, tryStart);
    val catchAll = new DexCatchAll(code);

    tryStart.setCatchAllHandler(catchAll);

    code.add(tryStart);
    code.add(nop);
    code.add(tryEnd);
    code.add(catchAll);
    code.add(new DexInstruction_Nop(code));

    assertFalse(execThrowingInsn_CanExitMethod(nop));
  }

  @Test
  public void testThrowingInsn_CanExitMethod_InsideTryBlockWithCatchThrowableHandler() {
    // we pretend that NOP can throw an exception

    val code = createDexCode();
    val cache = code.getParentFile().getTypeCache();

    val nop = new DexInstruction_Nop(code);
    val tryStart = new DexTryBlockStart(code);
    val tryEnd = new DexTryBlockEnd(code, tryStart);
    val catchThrowable = new DexCatch(code, DexClassType.parse("Ljava/lang/Throwable;", cache));

    tryStart.addCatchHandler(catchThrowable);

    code.add(tryStart);
    code.add(nop);
    code.add(tryEnd);
    code.add(catchThrowable);
    code.add(new DexInstruction_Nop(code));

    assertFalse(execThrowingInsn_CanExitMethod(nop));
  }

//  @Test
//  public void testThrowingInsn_CanExitMethod_InsideTryBlockWithCatchExceptionHandler() {
//    // we pretend that NOP can throw an exception
//
//    val code = createDexCode();
//    val cache = code.getParentMethod().getParentClass().getParentFile().getParsingCache();
//
//    val nop = new DexInstruction_Nop(code);
//    val tryStart = new DexTryBlockStart(code);
//    val tryEnd = new DexTryBlockEnd(code, tryStart);
//    val catchException = new DexCatch(code, DexClassType.parse("Ljava/lang/Exception;", cache));
//
//    tryStart.addCatchHandler(catchException);
//
//    code.add(tryStart);
//    code.add(nop);
//    code.add(tryEnd);
//    code.add(catchException);
//    code.add(new DexInstruction_Nop(code));
//
//    // Exception catch handler doesn't catch RuntimeExceptions and Errors
//    // thus the instruction could exit the method
//    assertTrue(execThrowingInsn_CanExitMethod(nop));
//  }

  @SuppressWarnings("unchecked")
  private static Set<DexCodeElement> execThrowingInsn_CatchHandlers(DexInstruction insn) {
    try {
      Method m = DexInstruction.class.getDeclaredMethod("throwingInsn_CatchHandlers");
      m.setAccessible(true);
      return (Set<DexCodeElement>) m.invoke(insn);
    } catch (Exception e) {
      e.printStackTrace(System.err);
      fail("Couldn't execute method: " + e.getClass().getSimpleName());
      return null;
    }
  }

  @Test
  public void testThrowingInsn_CatchHandlers_NotInsideTryBlock() {
    // we pretend that NOP can throw an exception

    val code = createDexCode();
    val nop = new DexInstruction_Nop(code);
    val tryStart = new DexTryBlockStart(code);
    val tryEnd = new DexTryBlockEnd(code, tryStart);

    code.add(nop);
    code.add(tryStart);
    code.add(tryEnd);

    assertTrue(execThrowingInsn_CatchHandlers(nop).isEmpty());
  }

  @Test
  public void testThrowingInsn_CatchHandlers_InsideTryBlockWithoutCatchHandlers() {
    // we pretend that NOP can throw an exception

    val code = createDexCode();
    val nop = new DexInstruction_Nop(code);
    val tryStart = new DexTryBlockStart(code);
    val tryEnd = new DexTryBlockEnd(code, tryStart);

    code.add(tryStart);
    code.add(nop);
    code.add(tryEnd);

    assertTrue(execThrowingInsn_CatchHandlers(nop).isEmpty());
  }

  @Test
  public void testThrowingInsn_CatchHandlers_InsideTryBlockWithCatchAllHandler() {
    // we pretend that NOP can throw an exception

    val code = createDexCode();
    val nop = new DexInstruction_Nop(code);
    val tryStart = new DexTryBlockStart(code);
    val tryEnd = new DexTryBlockEnd(code, tryStart);
    val catchAll = new DexCatchAll(code);

    tryStart.setCatchAllHandler(catchAll);

    code.add(tryStart);
    code.add(nop);
    code.add(tryEnd);
    code.add(catchAll);
    code.add(new DexInstruction_Nop(code));

    val succ = execThrowingInsn_CatchHandlers(nop);
    assertEquals(1, succ.size());
    assertTrue(succ.contains(catchAll));
  }

  @Test
  public void testThrowingInsn_CatchHandlers_InsideTryBlockWithCatchHandler() {
    // we pretend that NOP can throw an exception

    val code = createDexCode();
    val nop = new DexInstruction_Nop(code);
    val tryStart = new DexTryBlockStart(code);
    val tryEnd = new DexTryBlockEnd(code, tryStart);
    val catchThrowable = new DexCatch(code, DexClassType.parse("Ljava/lang/Throwable;", new DexTypeCache()));

    tryStart.addCatchHandler(catchThrowable);

    code.add(tryStart);
    code.add(nop);
    code.add(tryEnd);
    code.add(catchThrowable);
    code.add(new DexInstruction_Nop(code));

    val succ = execThrowingInsn_CatchHandlers(nop);
    assertEquals(1, succ.size());
    assertTrue(succ.contains(catchThrowable));
  }


  @Test
  public void testThrowingInsn_CatchHandlers_InsideTryBlockWithMultipleCatchHandlers() {
    // we pretend that NOP can throw an exception

    val parseCache = new DexTypeCache();

    val code = createDexCode();
    val nop = new DexInstruction_Nop(code);
    val tryStart = new DexTryBlockStart(code);
    val tryEnd = new DexTryBlockEnd(code, tryStart);
    val catchAll = new DexCatchAll(code);
    val catchThrowable = new DexCatch(code, DexClassType.parse("Ljava/lang/Throwable;", parseCache));
    val catchRuntimeException = new DexCatch(code, DexClassType.parse("Ljava/lang/RuntimeException;", parseCache));
    val catchError = new DexCatch(code, DexClassType.parse("Ljava/lang/Error;", parseCache));

    tryStart.setCatchAllHandler(catchAll);
    tryStart.addCatchHandler(catchThrowable);
    tryStart.addCatchHandler(catchRuntimeException);
    tryStart.addCatchHandler(catchError);

    code.add(tryStart);
    code.add(nop);
    code.add(tryEnd);
    code.add(catchAll);
    code.add(new DexInstruction_Nop(code));
    code.add(catchThrowable);
    code.add(new DexInstruction_Nop(code));
    code.add(catchRuntimeException);
    code.add(new DexInstruction_Nop(code));
    code.add(catchError);
    code.add(new DexInstruction_Nop(code));

    val succ = execThrowingInsn_CatchHandlers(nop);
    assertEquals(4, succ.size());
    assertTrue(succ.contains(catchAll));
    assertTrue(succ.contains(catchThrowable));
    assertTrue(succ.contains(catchRuntimeException));
    assertTrue(succ.contains(catchError));
  }

  //  private static DexCodeElement compare(Instruction insn, String output) {
//    DexCode code;
//    try {
//      code = new DexCode(new Instruction[] { insn }, new DexParsingCache());
//    } catch (UnknownTypeException | InstructionParsingException e) {
//      fail(e.getClass().getName() + ": " + e.getMessage());
//      return null;
//    }
//    val insnList = code.getInstructionList();
//    assertEquals(1, insnList.size());
//    val insnInsn = insnList.get(0);
//    assertEquals(output, insnInsn.getOriginalAssembly());
//    return insnInsn;
//  }
//
//  private static void compareList(Instruction[] insns, String[] output) throws InstructionParsingException {
//    DexCode code;
//    try {
//      code = new DexCode(insns, new DexParsingCache());
//    } catch (UnknownTypeException e) {
//      fail(e.getClass().getName() + ": " + e.getMessage());
//      return;
//    }
//
//    val insnList = code.getInstructionList();
//    assertEquals(output.length, insnList.size());
//    for (int i = 0; i < output.length; ++i)
//      assertEquals(output[i], insnList.get(i).getOriginalAssembly());
//  }
//
//  @Test
//  public void testGetRegister_ReuseRegisters() {
//    val insn = (DexInstruction_Move)
//               compare(
//                 new Instruction12x(Opcode.MOVE, (byte) 3, (byte) 3),
//                 "move v3, v3");
//    assertTrue(insn.getRegTo() == insn.getRegFrom());
//  }
//
//
//  @Test
//  public void testMoveWide() {
//    val insn = (DexInstruction_MoveWide)
//               compare(
//                 new Instruction12x(Opcode.MOVE_WIDE, (byte) 8, (byte) 5),
//                 "move-wide v8, v5");
//    assertEquals(Integer.valueOf(8), insn.getRegTo1().getId());
//    assertEquals(Integer.valueOf(9), insn.getRegTo2().getId());
//    assertEquals(Integer.valueOf(5), insn.getRegFrom1().getId());
//    assertEquals(Integer.valueOf(6), insn.getRegFrom2().getId());
//  }
//
//  @Test
//  public void testMoveWideFrom16() {
//    val insn = (DexInstruction_MoveWide)
//               compare(
//                 new Instruction22x(Opcode.MOVE_WIDE_FROM16, (short) 253, 62435),
//                 "move-wide v253, v62435");
//    assertEquals(Integer.valueOf(253), insn.getRegTo1().getId());
//    assertEquals(Integer.valueOf(254), insn.getRegTo2().getId());
//    assertEquals(Integer.valueOf(62435), insn.getRegFrom1().getId());
//    assertEquals(Integer.valueOf(62436), insn.getRegFrom2().getId());
//  }
//
//  @Test
//  public void testMoveWide16() {
//    val insn = (DexInstruction_MoveWide)
//               compare(
//                 new Instruction32x(Opcode.MOVE_WIDE_16, 60123, 62435),
//                 "move-wide v60123, v62435");
//    assertEquals(Integer.valueOf(60123), insn.getRegTo1().getId());
//    assertEquals(Integer.valueOf(60124), insn.getRegTo2().getId());
//    assertEquals(Integer.valueOf(62435), insn.getRegFrom1().getId());
//    assertEquals(Integer.valueOf(62436), insn.getRegFrom2().getId());
//  }
//
//  @Test
//  public void testMoveResult() {
//    compare(new Instruction11x(Opcode.MOVE_RESULT, (short) 234),
//            "move-result v234");
//  }
//
//  @Test
//  public void testMoveResultObject() {
//    compare(new Instruction11x(Opcode.MOVE_RESULT_OBJECT, (short) 234),
//            "move-result-object v234");
//  }
//
//  @Test
//  public void testMoveResultWide() {
//    val insn = (DexInstruction_MoveResultWide)
//               compare(
//                 new Instruction11x(Opcode.MOVE_RESULT_WIDE, (short) 233),
//                 "move-result-wide v233");
//    assertEquals(Integer.valueOf(233), insn.getRegTo1().getId());
//    assertEquals(Integer.valueOf(234), insn.getRegTo2().getId());
//  }
//
//  @Test
//  public void testMoveException() {
//    compare(new Instruction11x(Opcode.MOVE_EXCEPTION, (short) 231),
//            "move-exception v231");
//  }
//
//  @Test
//  public void testReturn() {
//    compare(new Instruction11x(Opcode.RETURN, (short) 231),
//            "return v231");
//  }
//
//  @Test
//  public void testReturnObject() {
//    compare(new Instruction11x(Opcode.RETURN_OBJECT, (short) 230),
//            "return-object v230");
//  }
//
//  @Test
//  public void testReturnWide() {
//    val insn = (DexInstruction_ReturnWide)
//               compare(
//                 new Instruction11x(Opcode.RETURN_WIDE, (short) 235),
//                 "return-wide v235");
//    assertEquals(Integer.valueOf(235), insn.getRegFrom1().getId());
//    assertEquals(Integer.valueOf(236), insn.getRegFrom2().getId());
//  }
//
//  @Test
//  public void testConstWide16() {
//    compare(new Instruction21s(Opcode.CONST_WIDE_16, (short) 236, (short) 32082),
//            "const-wide v236, #32082");
//    val insn = (DexInstruction_ConstWide)
//               compare(new Instruction21s(Opcode.CONST_WIDE_16, (short) 236, (short) -32082),
//                       "const-wide v236, #-32082");
//    assertEquals(Integer.valueOf(236), insn.getRegTo1().getId());
//    assertEquals(Integer.valueOf(237), insn.getRegTo2().getId());
//  }
//
//  @Test
//  public void testConstWide32() {
//    compare(new Instruction31i(Opcode.CONST_WIDE_32, (short) 236, 0x01ABCDEF),
//            "const-wide v236, #28036591");
//    val insn = (DexInstruction_ConstWide)
//               compare(new Instruction31i(Opcode.CONST_WIDE_32, (short) 236, 0xABCDEF01),
//                       "const-wide v236, #-1412567295");
//    assertEquals(Integer.valueOf(236), insn.getRegTo1().getId());
//    assertEquals(Integer.valueOf(237), insn.getRegTo2().getId());
//  }
//
//  @Test
//  public void testConstWide() {
//    compare(new Instruction51l(Opcode.CONST_WIDE, (short) 236, 0x0102030405060708L),
//            "const-wide v236, #72623859790382856");
//    val insn = (DexInstruction_ConstWide)
//               compare(new Instruction51l(Opcode.CONST_WIDE, (short) 236, 0xFFFFFFFFFFFFFFFEL),
//                       "const-wide v236, #-2");
//    assertEquals(Integer.valueOf(236), insn.getRegTo1().getId());
//    assertEquals(Integer.valueOf(237), insn.getRegTo2().getId());
//  }
//
//  @Test
//  public void testConstWideHigh16() {
//    compare(new Instruction21h(Opcode.CONST_WIDE_HIGH16, (short) 236, (short) 0x1234),
//            "const-wide v236, #1311673391471656960");
//    val insn = (DexInstruction_ConstWide)
//               compare(new Instruction21h(Opcode.CONST_WIDE_HIGH16, (short) 236, (short) 0xFEDC),
//                       "const-wide v236, #-82190693199511552");
//    assertEquals(Integer.valueOf(236), insn.getRegTo1().getId());
//    assertEquals(Integer.valueOf(237), insn.getRegTo2().getId());
//  }
//
//  private static StringIdItem getStringItem(String str) {
//    return StringIdItem.internStringIdItem(new DexFile(), str);
//  }
//
//  @Test
//  public void testConstString() {
//    compare(new Instruction21c(Opcode.CONST_STRING, (short) 236, getStringItem("Hello, world!")),
//            "const-string v236, \"Hello, world!\"");
//    // escaping characters
//    compare(new Instruction21c(Opcode.CONST_STRING, (short) 236, getStringItem("Hello, \"world!")),
//            "const-string v236, \"Hello, \\\"world!\"");
//    // cutting off after 15 characters
//    compare(new Instruction21c(Opcode.CONST_STRING, (short) 236, getStringItem("123456789012345")),
//            "const-string v236, \"123456789012345\"");
//    compare(new Instruction21c(Opcode.CONST_STRING, (short) 236, getStringItem("1234567890123456")),
//            "const-string v236, \"123456789012345...\"");
//    compare(new Instruction21c(Opcode.CONST_STRING, (short) 236, getStringItem("12345678901234\"")),
//            "const-string v236, \"12345678901234\\...\"");
//  }
//
//  @Test
//  public void testConstStringJumbo() {
//    compare(new Instruction31c(Opcode.CONST_STRING_JUMBO, (short) 236, getStringItem("Hello, world!")),
//            "const-string v236, \"Hello, world!\"");
//  }
//
//  private static TypeIdItem getTypeItem(String desc) {
//    return TypeIdItem.internTypeIdItem(new DexFile(), desc);
//  }
//
//  @Test
//  public void testConstClass() {
//    compare(new Instruction21c(Opcode.CONST_CLASS, (short) 236, getTypeItem("Ljava.lang.String;")),
//            "const-class v236, Ljava.lang.String;");
//    compare(new Instruction21c(Opcode.CONST_CLASS, (short) 236, getTypeItem("[Ljava.lang.String;")),
//            "const-class v236, [Ljava.lang.String;");
//  }
//
//  @Test
//  public void testMonitorEnter() {
//    compare(new Instruction11x(Opcode.MONITOR_ENTER, (short) 244),
//            "monitor-enter v244");
//  }
//
//  @Test
//  public void testMonitorExit() {
//    compare(new Instruction11x(Opcode.MONITOR_EXIT, (short) 245),
//            "monitor-exit v245");
//  }
//
//  @Test
//  public void testCheckCast() {
//    compare(new Instruction21c(Opcode.CHECK_CAST, (short) 236, getTypeItem("Ljava.lang.String;")),
//            "check-cast v236, Ljava.lang.String;");
//    compare(new Instruction21c(Opcode.CHECK_CAST, (short) 236, getTypeItem("[Ljava.lang.String;")),
//            "check-cast v236, [Ljava.lang.String;");
//  }
//
//  @Test
//  public void testInstanceOf() {
//    compare(new Instruction22c(Opcode.INSTANCE_OF, (byte) 4, (byte) 5, getTypeItem("Ljava.lang.String;")),
//            "instance-of v4, v5, Ljava.lang.String;");
//    compare(new Instruction22c(Opcode.INSTANCE_OF, (byte) 4, (byte) 5, getTypeItem("[Ljava.lang.String;")),
//            "instance-of v4, v5, [Ljava.lang.String;");
//  }
//
//  @Test
//  public void testNewInstance() {
//    compare(new Instruction21c(Opcode.NEW_INSTANCE, (short) 236, getTypeItem("Ljava.lang.String;")),
//            "new-instance v236, Ljava.lang.String;");
//  }
//
//  @Test
//  public void testNewArray() {
//    compare(new Instruction22c(Opcode.NEW_ARRAY, (byte) 4, (byte) 5, getTypeItem("[Ljava.lang.String;")),
//            "new-array v4, v5, [Ljava.lang.String;");
//    compare(new Instruction22c(Opcode.NEW_ARRAY, (byte) 4, (byte) 5, getTypeItem("[I")),
//            "new-array v4, v5, [I");
//    compare(new Instruction22c(Opcode.NEW_ARRAY, (byte) 4, (byte) 12, getTypeItem("[[[I")),
//            "new-array v4, v12, [[[I");
//  }
//
//  @Test
//  public void testThrow() {
//    compare(new Instruction11x(Opcode.THROW, (byte) 243),
//            "throw v243");
//  }
//
//  @Test
//  public void testGoto() throws InstructionParsingException {
//    compareList(
//      new Instruction[] {
//        new Instruction10x(Opcode.NOP),
//        new Instruction10t(Opcode.GOTO, -1),
//        new Instruction10x(Opcode.NOP)
//      }, new String[] {
//        "L0:",
//        "nop",
//        "goto L0",
//        "nop"
//      });
//    compareList(
//      new Instruction[] {
//        new Instruction32x(Opcode.MOVE_16, 12345, 23456),
//        new Instruction10t(Opcode.GOTO, 1),
//        new Instruction10x(Opcode.NOP)
//      }, new String[] {
//        "move v12345, v23456",
//        "goto L4",
//        "L4:",
//        "nop"
//      });
//  }
//
//  @Test(expected=InstructionParsingException.class)
//  public void testLabels_InvalidOffset_Positive() throws InstructionParsingException {
//    compareList(
//      new Instruction[] {
//        new Instruction10t(Opcode.GOTO, 2),
//        new Instruction10x(Opcode.NOP)
//      }, null);
//  }
//
//  @Test(expected=InstructionParsingException.class)
//  public void testLabels_InvalidOffset_Negative() throws InstructionParsingException {
//    compareList(
//      new Instruction[] {
//        new Instruction10x(Opcode.NOP),
//        new Instruction10t(Opcode.GOTO, -2)
//      }, null);
//  }
//
//  @Test
//  public void testGoto16() throws InstructionParsingException {
//    compareList(
//      new Instruction[] {
//        new Instruction10x(Opcode.NOP),
//        new Instruction20t(Opcode.GOTO_16, -1),
//        new Instruction10x(Opcode.NOP)
//      }, new String[] {
//        "L0:",
//        "nop",
//        "goto L0",
//        "nop"
//      });
//    compareList(
//      new Instruction[] {
//        new Instruction32x(Opcode.MOVE_16, 12345, 23456),
//        new Instruction20t(Opcode.GOTO_16, 2),
//        new Instruction10x(Opcode.NOP)
//      }, new String[] {
//        "move v12345, v23456",
//        "goto L5",
//        "L5:",
//        "nop"
//      });
//  }
//
//  @Test
//  public void testGoto32() throws InstructionParsingException {
//    compareList(
//      new Instruction[] {
//        new Instruction10x(Opcode.NOP),
//        new Instruction30t(Opcode.GOTO_32, -1),
//        new Instruction10x(Opcode.NOP)
//      }, new String[] {
//        "L0:",
//        "nop",
//        "goto L0",
//        "nop"
//      });
//    compareList(
//      new Instruction[] {
//        new Instruction32x(Opcode.MOVE_16, 12345, 23456),
//        new Instruction30t(Opcode.GOTO_32, 3),
//        new Instruction10x(Opcode.NOP)
//      }, new String[] {
//        "move v12345, v23456",
//        "goto L6",
//        "L6:",
//        "nop"
//      });
//  }
//
//  @Test
//  public void testIfTest() throws InstructionParsingException {
//    compareList(
//      new Instruction[] {
//        new Instruction10x(Opcode.NOP),
//        new Instruction22t(Opcode.IF_EQ, (byte) 0, (byte) 1, (short) -1),
//        new Instruction22t(Opcode.IF_NE, (byte) 2, (byte) 3, (short) -3),
//        new Instruction22t(Opcode.IF_LT, (byte) 4, (byte) 5, (short) -5),
//        new Instruction22t(Opcode.IF_GE, (byte) 6, (byte) 7, (short) -7),
//        new Instruction22t(Opcode.IF_GT, (byte) 8, (byte) 9, (short) -9),
//        new Instruction22t(Opcode.IF_LE, (byte) 10, (byte) 11, (short) -10)
//      }, new String[] {
//        "L0:",
//        "nop",
//        "L1:",
//        "if-eq v0, v1, L0",
//        "if-ne v2, v3, L0",
//        "if-lt v4, v5, L0",
//        "if-ge v6, v7, L0",
//        "if-gt v8, v9, L0",
//        "if-le v10, v11, L1"
//      });
//  }
//
//  @Test
//  public void testIfTestZero() throws InstructionParsingException {
//    compareList(
//      new Instruction[] {
//        new Instruction10x(Opcode.NOP),
//        new Instruction21t(Opcode.IF_EQZ, (short) 130, (short) -1),
//        new Instruction21t(Opcode.IF_NEZ, (short) 140, (short) -3),
//        new Instruction21t(Opcode.IF_LTZ, (short) 150, (short) -5),
//        new Instruction21t(Opcode.IF_GEZ, (short) 160, (short) -7),
//        new Instruction21t(Opcode.IF_GTZ, (short) 170, (short) -9),
//        new Instruction21t(Opcode.IF_LEZ, (short) 180, (short) -10)
//      }, new String[] {
//        "L0:",
//        "nop",
//        "L1:",
//        "if-eqz v130, L0",
//        "if-nez v140, L0",
//        "if-ltz v150, L0",
//        "if-gez v160, L0",
//        "if-gtz v170, L0",
//        "if-lez v180, L1"
//      });
//  }
//
//  @Test
//  public void testUnaryOp() throws InstructionParsingException {
//    compareList(
//      new Instruction[] {
//        new Instruction12x(Opcode.NEG_INT, (byte) 0, (byte) 1),
//        new Instruction12x(Opcode.NOT_INT, (byte) 2, (byte) 3),
//        new Instruction12x(Opcode.NEG_FLOAT, (byte) 4, (byte) 5)
//      }, new String[] {
//        "neg-int v0, v1",
//        "not-int v2, v3",
//        "neg-float v4, v5"
//      });
//  }
//
//  @Test
//  public void testUnaryOpWide() throws InstructionParsingException {
//    DexInstruction_UnaryOpWide insn;
//
//    insn = (DexInstruction_UnaryOpWide)
//           compare(new Instruction12x(Opcode.NEG_LONG, (byte) 0, (byte) 2),
//                   "neg-long v0, v2");
//    assertEquals(Integer.valueOf(0), insn.getRegTo1().getId());
//    assertEquals(Integer.valueOf(1), insn.getRegTo2().getId());
//    assertEquals(Integer.valueOf(2), insn.getRegFrom1().getId());
//    assertEquals(Integer.valueOf(3), insn.getRegFrom2().getId());
//
//    insn = (DexInstruction_UnaryOpWide)
//           compare(new Instruction12x(Opcode.NOT_LONG, (byte) 4, (byte) 6),
//                   "not-long v4, v6");
//    assertEquals(Integer.valueOf(4), insn.getRegTo1().getId());
//    assertEquals(Integer.valueOf(5), insn.getRegTo2().getId());
//    assertEquals(Integer.valueOf(6), insn.getRegFrom1().getId());
//    assertEquals(Integer.valueOf(7), insn.getRegFrom2().getId());
//
//    insn = (DexInstruction_UnaryOpWide)
//           compare(new Instruction12x(Opcode.NEG_DOUBLE, (byte) 8, (byte) 10),
//                   "neg-double v8, v10");
//    assertEquals(Integer.valueOf(8), insn.getRegTo1().getId());
//    assertEquals(Integer.valueOf(9), insn.getRegTo2().getId());
//    assertEquals(Integer.valueOf(10), insn.getRegFrom1().getId());
//    assertEquals(Integer.valueOf(11), insn.getRegFrom2().getId());
//  }
//
//  @Test
//  public void testConvert() throws InstructionParsingException {
//    compareList(
//      new Instruction[] {
//        new Instruction12x(Opcode.INT_TO_FLOAT, (byte) 0, (byte) 1),
//        new Instruction12x(Opcode.FLOAT_TO_INT, (byte) 2, (byte) 3),
//        new Instruction12x(Opcode.INT_TO_BYTE, (byte) 4, (byte) 5),
//        new Instruction12x(Opcode.INT_TO_CHAR, (byte) 6, (byte) 7),
//        new Instruction12x(Opcode.INT_TO_SHORT, (byte) 8, (byte) 9)
//      }, new String[] {
//        "int-to-float v0, v1",
//        "float-to-int v2, v3",
//        "int-to-byte v4, v5",
//        "int-to-char v6, v7",
//        "int-to-short v8, v9"
//      });
//  }
//
//  @Test
//  public void testConvertToWide() throws InstructionParsingException {
//    DexInstruction_ConvertToWide insn;
//
//    insn = (DexInstruction_ConvertToWide)
//           compare(new Instruction12x(Opcode.INT_TO_LONG, (byte) 0, (byte) 2),
//                   "int-to-long v0, v2");
//    assertEquals(Integer.valueOf(0), insn.getRegTo1().getId());
//    assertEquals(Integer.valueOf(1), insn.getRegTo2().getId());
//    assertEquals(Integer.valueOf(2), insn.getRegFrom().getId());
//
//    insn = (DexInstruction_ConvertToWide)
//           compare(new Instruction12x(Opcode.INT_TO_DOUBLE, (byte) 4, (byte) 6),
//                   "int-to-double v4, v6");
//    assertEquals(Integer.valueOf(4), insn.getRegTo1().getId());
//    assertEquals(Integer.valueOf(5), insn.getRegTo2().getId());
//    assertEquals(Integer.valueOf(6), insn.getRegFrom().getId());
//
//    insn = (DexInstruction_ConvertToWide)
//           compare(new Instruction12x(Opcode.FLOAT_TO_LONG, (byte) 8, (byte) 10),
//                   "float-to-long v8, v10");
//    assertEquals(Integer.valueOf(8), insn.getRegTo1().getId());
//    assertEquals(Integer.valueOf(9), insn.getRegTo2().getId());
//    assertEquals(Integer.valueOf(10), insn.getRegFrom().getId());
//
//    insn = (DexInstruction_ConvertToWide)
//           compare(new Instruction12x(Opcode.FLOAT_TO_DOUBLE, (byte) 12, (byte) 14),
//                   "float-to-double v12, v14");
//    assertEquals(Integer.valueOf(12), insn.getRegTo1().getId());
//    assertEquals(Integer.valueOf(13), insn.getRegTo2().getId());
//    assertEquals(Integer.valueOf(14), insn.getRegFrom().getId());
//  }
//
//  @Test
//  public void testConvertFromWide() throws InstructionParsingException {
//    DexInstruction_ConvertFromWide insn;
//
//    insn = (DexInstruction_ConvertFromWide)
//           compare(new Instruction12x(Opcode.LONG_TO_INT, (byte) 0, (byte) 2),
//                   "long-to-int v0, v2");
//    assertEquals(Integer.valueOf(0), insn.getRegTo().getId());
//    assertEquals(Integer.valueOf(2), insn.getRegFrom1().getId());
//    assertEquals(Integer.valueOf(3), insn.getRegFrom2().getId());
//
//    insn = (DexInstruction_ConvertFromWide)
//           compare(new Instruction12x(Opcode.LONG_TO_FLOAT, (byte) 8, (byte) 10),
//                   "long-to-float v8, v10");
//    assertEquals(Integer.valueOf(8), insn.getRegTo().getId());
//    assertEquals(Integer.valueOf(10), insn.getRegFrom1().getId());
//    assertEquals(Integer.valueOf(11), insn.getRegFrom2().getId());
//
//    insn = (DexInstruction_ConvertFromWide)
//           compare(new Instruction12x(Opcode.DOUBLE_TO_INT, (byte) 4, (byte) 6),
//                   "double-to-int v4, v6");
//    assertEquals(Integer.valueOf(4), insn.getRegTo().getId());
//    assertEquals(Integer.valueOf(6), insn.getRegFrom1().getId());
//    assertEquals(Integer.valueOf(7), insn.getRegFrom2().getId());
//
//    insn = (DexInstruction_ConvertFromWide)
//           compare(new Instruction12x(Opcode.DOUBLE_TO_FLOAT, (byte) 12, (byte) 14),
//                   "double-to-float v12, v14");
//    assertEquals(Integer.valueOf(12), insn.getRegTo().getId());
//    assertEquals(Integer.valueOf(14), insn.getRegFrom1().getId());
//    assertEquals(Integer.valueOf(15), insn.getRegFrom2().getId());
//  }
//
//  @Test
//  public void testConvertWide() throws InstructionParsingException {
//    DexInstruction_ConvertWide insn;
//
//    insn = (DexInstruction_ConvertWide)
//           compare(new Instruction12x(Opcode.LONG_TO_DOUBLE, (byte) 0, (byte) 2),
//                   "long-to-double v0, v2");
//    assertEquals(Integer.valueOf(0), insn.getRegTo1().getId());
//    assertEquals(Integer.valueOf(1), insn.getRegTo2().getId());
//    assertEquals(Integer.valueOf(2), insn.getRegFrom1().getId());
//    assertEquals(Integer.valueOf(3), insn.getRegFrom2().getId());
//
//    insn = (DexInstruction_ConvertWide)
//           compare(new Instruction12x(Opcode.DOUBLE_TO_LONG, (byte) 8, (byte) 10),
//                   "double-to-long v8, v10");
//    assertEquals(Integer.valueOf(8), insn.getRegTo1().getId());
//    assertEquals(Integer.valueOf(9), insn.getRegTo2().getId());
//    assertEquals(Integer.valueOf(10), insn.getRegFrom1().getId());
//    assertEquals(Integer.valueOf(11), insn.getRegFrom2().getId());
//  }

  @Test
  public void testNothing() {

  }
}
