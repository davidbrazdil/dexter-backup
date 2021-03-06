package com.rx201.dx.translator;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.rx201.dx.translator.util.DexRegisterHelper;

import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;
import uk.ac.cam.db538.dexter.utils.Pair;

public class AnalyzedDexInstruction {

	    /**
	     * The actual instruction
	     */
	    protected final DexInstruction instruction;
	    protected DexCodeElement auxillaryElement;

	    /**
	     * Instructions that can pass on execution to this one during normal execution
	     */
	    protected final LinkedList<AnalyzedDexInstruction> predecessors = new LinkedList<AnalyzedDexInstruction>();
	    protected final Set<AnalyzedDexInstruction> exceptionPredecessors = new HashSet<AnalyzedDexInstruction>();
	    /**
	     * Instructions that can execution could pass on to next during normal execution
	     */
	    protected final LinkedList<AnalyzedDexInstruction> successors = new LinkedList<AnalyzedDexInstruction>();

	    /**
	     * This contains the register types *before* the instruction has executed
	     */
	    protected final HashSet<DexRegister> usedRegisters;
	    protected final HashMap<Integer, TypeSolver> usedRegisterMap;

	    /**
	     * This contains the register types *after* the instruction has executed
	     */
	    protected final HashSet<DexRegister> definedRegisters;
	    protected final HashMap<Integer, TypeSolver> definedRegisterMap;


	    protected final HashMap<Integer, Pair<Integer, TypeSolver.CascadeType>> constrainedRegisters;
	    
	    public final int instructionIndex;
	    
		protected HashMap<Integer, Pair<RopType, Boolean>> useSet;
		protected HashMap<Integer, Pair<RopType, Boolean>> defSet;
		// <regSource, regDestination>
		protected HashMap<Integer, Integer> moveSet;
		public static RuntimeHierarchy hierarchy;
	    
	    
	    public AnalyzedDexInstruction(int index, DexInstruction instruction) {
	        this.instruction = instruction;
	        this.usedRegisterMap = new HashMap<Integer, TypeSolver>();
	        this.definedRegisterMap = new HashMap<Integer, TypeSolver>();
	        this.constrainedRegisters = new HashMap<Integer, Pair<Integer, TypeSolver.CascadeType>>();
	        this.instructionIndex = index;
	        this.auxillaryElement = null;
	        
			useSet = new HashMap<Integer, Pair<RopType, Boolean>>();
			defSet = new HashMap<Integer, Pair<RopType, Boolean>>();
			moveSet = new HashMap<Integer, Integer>();
			usedRegisters = new HashSet<DexRegister>();
			definedRegisters = new HashSet<DexRegister>();
	        
	    }

	    public AnalyzedDexInstruction(int index, DexInstruction instruction, DexCodeElement element) {
	    	this(index, instruction);
	    	this.auxillaryElement = element;
	    }

	    public int getPredecessorCount() {
	        return predecessors.size();
	    }

	    public List<AnalyzedDexInstruction> getPredecessors() {
	        return Collections.unmodifiableList(predecessors);
	    }

	    protected void linkToSuccessor(AnalyzedDexInstruction successor, boolean exceptionPath) {
	    	successors.add(successor);
	    	successor.predecessors.add(this);
	    	if (exceptionPath) 
	    		successor.exceptionPredecessors.add(this);
	    }

	    public boolean isExceptionPredecessor(AnalyzedDexInstruction predecessor) {
	    	return exceptionPredecessors.contains(predecessor);
	    }
	    
	    public int getSuccessorCount() {
	        return successors.size();
	    }

	    public List<AnalyzedDexInstruction> getSuccesors() {
	        return Collections.unmodifiableList(successors);
	    }

	    public AnalyzedDexInstruction getOnlySuccesor() {
	    	assert successors.size() == 1;
	    	return successors.get(0);
	    }

		public DexInstruction getInstruction() {
	        return instruction;
	    }

	    public DexCodeElement getCodeElement() {
	    	if (instruction != null) {
	    		assert auxillaryElement == null;
	    		return instruction;
	    	} else if (auxillaryElement != null) {
	    		return auxillaryElement;
	    	} else {
	    		throw new RuntimeException("bad AnalyzedDexInstruction structure");
	    	}
	    }
		public Set<DexRegister> getUsedRegisters() {
			return usedRegisters;
		}
		
	    public RopType getUsedRegisterType(int reg) {
	    	return usedRegisterMap.get(reg).getType();
	    }
	    
	    public RopType getUsedRegisterType(DexRegister dexRegister) {
	    	int registerNumber = DexRegisterHelper.normalize(dexRegister);
	    	return getUsedRegisterType(registerNumber);
	    }

	    public void associateDefinitionSite(DexRegister usedReg, TypeSolver definition) {
	    	int registerNumber = DexRegisterHelper.normalize(usedReg);
	    	assert useSet.containsKey(registerNumber);
	    	usedRegisterMap.put(registerNumber, definition);
	    }

	    public TypeSolver getUsedRegisterTypeSolver(DexRegister usedReg) {
	    	int registerNumber = DexRegisterHelper.normalize(usedReg);
	    	return usedRegisterMap.get(registerNumber);
	    }

		public Set<DexRegister> getDefinedRegisters() {
			return definedRegisters;
		}

		public RopType getDefinedRegisterType(int reg) {
	    	return definedRegisterMap.get(reg).getType();
	    }

	    public RopType getDefinedRegisterType(DexRegister dexRegister) {
	    	int registerNumber = DexRegisterHelper.normalize(dexRegister);
	    	return getDefinedRegisterType(registerNumber);
	    }
	    
	    public TypeSolver getDefinedRegisterSolver(DexRegister dexRegister) {
	    	int registerNumber = DexRegisterHelper.normalize(dexRegister);
	    	if (definedRegisterMap.containsKey(registerNumber))
	    		return definedRegisterMap.get(registerNumber);
	    	else
	    		return null;
	    	
	    }

	    public int getInstructionIndex() {
	    	return instructionIndex;
	    }
	    
		public void defineRegister(DexRegister regTo, RopType registerType, boolean freezed) {
			int registerNumber = DexRegisterHelper.normalize(regTo);
			definedRegisters.add(regTo);
			definedRegisterMap.put(registerNumber, new TypeSolver(this));
			defSet.put(registerNumber, new Pair<RopType, Boolean>(registerType, freezed));
		}
		
		public void useRegister(DexRegister regFrom, RopType registerType, boolean freezed) {
			usedRegisters.add(regFrom);
			useSet.put(DexRegisterHelper.normalize(regFrom), new Pair<RopType, Boolean>(registerType, freezed));
		}
		
		public void addRegisterConstraint(DexRegister regTo, DexRegister regFrom, TypeSolver.CascadeType type) {
			constrainedRegisters.put(DexRegisterHelper.normalize(regTo), 
					new Pair<Integer, TypeSolver.CascadeType>(DexRegisterHelper.normalize(regFrom), type));
		}

		public void createConstraintEdges() {
			for(Entry<Integer, Pair<Integer, TypeSolver.CascadeType>> constraint : constrainedRegisters.entrySet()) {
				TypeSolver target = definedRegisterMap.get(constraint.getKey());
				//TODO: Hack for now, should add another addRegisterConstraint() interface
				if (target == null)
					target = usedRegisterMap.get(constraint.getKey());
				TypeSolver source = usedRegisterMap.get(constraint.getValue().getValA());
				target.addDependingTS(source, constraint.getValue().getValB());
			}
		}
		
		public void initDefinitionConstraints() {
			for(Entry<Integer, Pair<RopType, Boolean>> constraint : defSet.entrySet()) {
				TypeSolver target = definedRegisterMap.get(constraint.getKey());
				RopType type = constraint.getValue().getValA();
				boolean freezed = constraint.getValue().getValB();
				target.addConstraint(type, freezed, hierarchy);
			}
		}

		public void propagateUsageConstraints() {
			for(Entry<Integer, Pair<RopType, Boolean>> constraint : useSet.entrySet()) {
				TypeSolver target = usedRegisterMap.get(constraint.getKey());
				RopType type = constraint.getValue().getValA();
				boolean freezed = constraint.getValue().getValB();
				target.addConstraint(type, freezed, hierarchy);
			}
		}

		@Override
		public String toString() {
			if (auxillaryElement != null)
				return auxillaryElement.getOriginalAssembly();
			else if (instruction != null)
				return instruction.getOriginalAssembly();
			else
				return "null:" + instructionIndex;
		}

}
