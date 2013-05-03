package com.rx201.dx.translator;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import org.jf.baksmali.Adaptors.Format.InstructionMethodItem;
import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.CodeItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Analysis.AnalyzedInstruction;
import org.jf.dexlib.Code.Analysis.MethodAnalyzer;
import org.jf.dexlib.Util.AccessFlags;
import org.jf.util.IndentingWriter;

import com.android.dx.cf.code.ConcreteMethod;
import com.android.dx.cf.code.Ropper;
import com.android.dx.dex.DexOptions;
import com.android.dx.dex.cf.CodeStatistics;
import com.android.dx.dex.cf.OptimizerOptions;
import com.android.dx.dex.code.DalvCode;
import com.android.dx.dex.code.PositionList;
import com.android.dx.dex.code.RopTranslator;
import com.android.dx.rop.code.BasicBlock;
import com.android.dx.rop.code.BasicBlockList;
import com.android.dx.rop.code.DexTranslationAdvice;
import com.android.dx.rop.code.Insn;
import com.android.dx.rop.code.InsnList;
import com.android.dx.rop.code.LocalVariableExtractor;
import com.android.dx.rop.code.LocalVariableInfo;
import com.android.dx.rop.code.RopMethod;
import com.android.dx.rop.code.TranslationAdvice;
import com.android.dx.rop.cst.CstMethodRef;
import com.android.dx.ssa.Optimizer;
import com.android.dx.util.IntList;

public class Translator {

	public static RopMethod toRop(CodeItem method) {
		MethodAnalyzer analyzer = new MethodAnalyzer(method.getParent(), false, null);
		analyzer.analyze();
        HashMap<Instruction, AnalyzedInstruction> analysisResult = new HashMap<Instruction, AnalyzedInstruction>();
        for(AnalyzedInstruction analyzedInst : analyzer.getInstructions()) {
                analysisResult.put(analyzedInst.getInstruction(), analyzedInst);
        }
        
        // Build basic blocks
        ArrayList<ArrayList<AnalyzedInstruction>> basicBlocks = buildBasicBlocks(analyzer);
        
        // Convert to ROP's BasicBlockList form
        BasicBlockList ropBasicBlocks = new BasicBlockList(basicBlocks.size());
        int bbIndex = 0;
        
        Converter converter = new Converter(analyzer, method);
        
		IndentingWriter writer = new IndentingWriter(new OutputStreamWriter(System.out));
        for(ArrayList<AnalyzedInstruction> basicBlock : basicBlocks) {
        	
        	// Process instruction in the basic block as a whole, 
        	ArrayList<Insn> insnBlock = new ArrayList<Insn>();
        	ConvertedResult lastInsn = null;
        	for(AnalyzedInstruction inst : basicBlock) {
        		if (inst.getInstruction() != null) {
        			InstructionMethodItem<Instruction> x = new InstructionMethodItem<Instruction>(method, 0, inst.getInstruction()) ;
					try {
						x.writeTo(writer);
					} catch (IOException e) {
						e.printStackTrace();
					}
        		}
        		lastInsn = converter.convert(inst);
        		insnBlock.addAll(lastInsn.insns);
        		
				try {
					writer.write("\n    --> ");
					writer.write(lastInsn.insns.get(0).toHuman());
					if (lastInsn.insns.size() > 1)
						writer.write("...");
					writer.write("\n");
					writer.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        	
        	// then convert them to InsnList
        	InsnList insns = new InsnList(insnBlock.size());
        	for(int i=0 ;i<insnBlock.size(); i++)
        		insns.set(i, insnBlock.get(i));
        	insns.setImmutable();
        	
        	IntList successors = new IntList();
        	for(AnalyzedInstruction s : lastInsn.successors) 
        		successors.add(s.getInstructionIndex());
        	successors.setImmutable();
        	
        	int label = basicBlock.get(0).getInstructionIndex();
        	BasicBlock ropBasicBlock = new BasicBlock(label, insns, successors, lastInsn.primarySuccessor != null ? lastInsn.primarySuccessor.getInstructionIndex() : -1);
        	ropBasicBlocks.set(bbIndex++, ropBasicBlock);
        }

        return new SimpleRopMethod(ropBasicBlocks, analyzer.getStartOfMethod().getSuccesors().get(0).getInstructionIndex());
	}
	
	private static ArrayList<ArrayList<AnalyzedInstruction>> buildBasicBlocks(MethodAnalyzer analyzer) {
        ArrayList<ArrayList<AnalyzedInstruction>> basicBlocks = new ArrayList<ArrayList<AnalyzedInstruction>>();
        
        Stack<AnalyzedInstruction> leads = new Stack<AnalyzedInstruction>();
        assert analyzer.getStartOfMethod().getSuccesors().size() == 0;
        leads.push(analyzer.getStartOfMethod().getSuccesors().get(0));
        HashSet<Integer> visited = new HashSet<Integer>();
        
        while(!leads.empty()) {
        	AnalyzedInstruction first = leads.pop();
        	int id = first.getInstructionIndex();
        	if (visited.contains(id)) continue; // Already visited this basic block before.
        	visited.add(id);
        	
        	ArrayList<AnalyzedInstruction> block = new ArrayList<AnalyzedInstruction>(); 
        	// Extend this basic block as far as possible
        	AnalyzedInstruction current = first; // Always refer to latest-added instruction in the bb
        	block.add(current);
        	while(current.getSuccessorCount() == 1  && (!isBasicBlockBreaker(current))) { 
        		// Condition 1: current has only one successor
        		// Condition 2: next instruction has only one predecessor
        		// Condition 3: current cannot throw
        		AnalyzedInstruction next = current.getSuccesors().get(0);
        		if (next.getPredecessorCount() == 1) {
        			block.add(next);
        			current = next;
        		} else
        			break;
        	}
        	
        	// Add successors of current to the to-be-visit stack
        	for(AnalyzedInstruction i : current.getSuccesors())
        		leads.push(i);
        	
        	basicBlocks.add(block);
       }
        
        return basicBlocks;
	}

	
	private static boolean isBasicBlockBreaker(AnalyzedInstruction next) {
		return next.getInstruction().opcode.canThrow();
	}

	public static void translate(EncodedMethod method) {
		CodeItem code = method.codeItem;
		int paramSize = code.getInWords();
		boolean isStatic = (method.accessFlags & AccessFlags.STATIC.getValue()) != 0;
		DexOptions dexOptions = new DexOptions();
        dexOptions.targetApiLevel = 10;

		RopMethod rmeth = toRop(code);

        rmeth = Optimizer.optimize(rmeth, paramSize, isStatic, false, DexTranslationAdvice.THE_ONE);
		
        DalvCode dcode = RopTranslator.translate(rmeth, PositionList.NONE, null, paramSize, dexOptions);
        PrintWriter pw = new PrintWriter(System.out);
        dcode.getInsns().debugPrint(pw, "    ", true);
	}
	
}

