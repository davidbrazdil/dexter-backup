package uk.ac.cam.db538.dexter.analysis.cfg;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.utils.NoDuplicatesList;

public class ControlFlowGraph {

  @Getter private final DexCode code;

  @Getter private CfgStartBlock startBlock;
  @Getter private CfgExitBlock exitBlock;
  private List<CfgBasicBlock> basicBlocks;

  public ControlFlowGraph(DexCode code) {
    this.code = code;
    this.startBlock = new CfgStartBlock();
    this.exitBlock = new CfgExitBlock();
    this.basicBlocks = new LinkedList<CfgBasicBlock>();
    generateCFG();
  }

  private CfgBlock getBlockByFirstInsn(DexCodeElement firstInsn, HashMap<DexCodeElement, CfgBasicBlock> insnBlockMap) {
    val block = insnBlockMap.get(firstInsn);
    if (block == null)
      throw new CfgException("Successor of a block doesn't point to a different block");
    else
      return block;
  }

  private void generateCFG() {

    val insns = code.getInstructionList();

    // this is a map going from the first instruction
    // of a block to its reference; used later to create
    // edges between blocks
    val insnBlockMap = new HashMap<DexCodeElement, CfgBasicBlock>();

    // split instruction list into basic blocks
    NoDuplicatesList<DexCodeElement> currentBlock = new NoDuplicatesList<DexCodeElement>();
    for (val insn : insns) {
      if (insn.cfgStartsBasicBlock() && !currentBlock.isEmpty()) {
        val block = new CfgBasicBlock(currentBlock);
        basicBlocks.add(block);
        insnBlockMap.put(block.getFirstInstruction(), block);
        currentBlock = new NoDuplicatesList<DexCodeElement>();
      }

      currentBlock.add(insn);

      if ((insn.cfgEndsBasicBlock() || insn.cfgExitsMethod()) && !currentBlock.isEmpty()) {
        val block = new CfgBasicBlock(currentBlock);
        basicBlocks.add(block);
        insnBlockMap.put(block.getFirstInstruction(), block);
        currentBlock = new NoDuplicatesList<DexCodeElement>();
      }
    }
    if (!currentBlock.isEmpty()) {
      val block = new CfgBasicBlock(currentBlock);
      basicBlocks.add(block);
      insnBlockMap.put(block.getFirstInstruction(), block);
    }

    // connect blocks together

    if (basicBlocks.isEmpty()) {
      // no basic blocks => just connect START and EXIT
      CfgBlock.createEdge(startBlock, exitBlock);
    } else {
      // connect first block with START
      CfgBlock.createEdge(startBlock, basicBlocks.get(0));

      // connect blocks together using the list of successors
      // provided by each instruction
      for (val block : basicBlocks) {
        val lastInsn = block.getLastInstruction();
        val lastInsnSuccs = lastInsn.cfgGetSuccessors();

        for (val succ : lastInsnSuccs)
          CfgBlock.createEdge(block, getBlockByFirstInsn(succ, insnBlockMap));

        // if a block ends with a returning instruction connect it to EXIT
        if (lastInsn.cfgExitsMethod() || lastInsnSuccs.isEmpty())
          CfgBlock.createEdge(block, exitBlock);

        // if one of the instructions (pick any) is inside a try block,
        // connect the whole block to the catch blocks
        // (depends on the fact that try block starts and ends a basic block)
        for (val tryEnd : code.getTryBlocks()) {
          val tryStart = tryEnd.getBlockStart();
          if (code.isBetween(tryStart, tryEnd, lastInsn)) {
            if (tryStart.getCatchAllHandler() != null)
              CfgBlock.createEdge(block, getBlockByFirstInsn(tryStart.getCatchAllHandler(), insnBlockMap));
            for (val catchBlock : tryStart.getCatchHandlers())
              CfgBlock.createEdge(block, getBlockByFirstInsn(catchBlock, insnBlockMap));
          }
        }
      }
    }
  }

  public List<CfgBasicBlock> getBasicBlocks() {
    return Collections.unmodifiableList(basicBlocks);
  }
}
