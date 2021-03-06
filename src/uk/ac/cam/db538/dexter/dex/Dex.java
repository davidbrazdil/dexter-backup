package uk.ac.cam.db538.dexter.dex;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.DexFromMemory;
import org.jf.dexlib.Util.AccessFlags;
import org.jf.dexlib.Util.ByteArrayAnnotatedOutput;

import uk.ac.cam.db538.dexter.dex.DexInstrumentationCache.InstrumentationWarning;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ReturnVoid;
import uk.ac.cam.db538.dexter.dex.method.DexDirectMethod;
import uk.ac.cam.db538.dexter.dex.method.DexMethodWithCode;
import uk.ac.cam.db538.dexter.dex.type.ClassRenamer;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;
import uk.ac.cam.db538.dexter.dex.type.DexVoid;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

public class Dex {

  @Getter final RuntimeHierarchy hierarchy;
  @Getter final InputStream resAuxiliaryDex;

  private final Set<DexClass> _classes;
  @Getter private final Set<DexClass> classes;
  

  @Getter private DexClassType objectTaintStorage_Type;
  @Getter private DexDirectMethod objectTaintStorage_Get;
  @Getter private DexDirectMethod objectTaintStorage_Set;

  @Getter private DexClassType methodCallHelper_Type;
  @Getter private DexField methodCallHelper_Arg;
  @Getter private DexField methodCallHelper_Res;
  @Getter private DexField methodCallHelper_SArg;
  @Getter private DexField methodCallHelper_SRes;

  @Getter private DexClassType internalClassAnnotation_Type;
  @Getter private DexClassType internalMethodAnnotation_Type;

  @Getter private DexClassType taintConstants_Type;
  @Getter private DexDirectMethod taintConstants_QueryTaint;
  @Getter private DexDirectMethod taintConstants_ServiceTaint;
  @Getter private DexDirectMethod taintConstants_HasSourceAndSinkTaint;

  @Getter private DexClass externalStaticFieldTaint_Class;
  @Getter private DexMethodWithCode externalStaticFieldTaint_Clinit;

  public Dex(RuntimeHierarchy hierarchy, InputStream auxiliaryDex) {
    this.hierarchy = hierarchy;
    this.resAuxiliaryDex = auxiliaryDex;
    
    this._classes = new HashSet<DexClass>();
    this.classes = Collections.unmodifiableSet(this._classes);
  }

  public Dex(DexFile dex, RuntimeHierarchy hierarchy, InputStream auxiliaryDex) {
	this(hierarchy, auxiliaryDex);
	
    classes.addAll(parseAllClasses(dex));

    for (val clazz : classes)
      clazz.markMethodsOriginal();
  }

  public Dex() {
	  this(null, null);
  }

  public DexTypeCache getTypeCache() {
    return hierarchy.getTypeCache();
  }

  private List<DexClass> parseAllClasses(DexFile file) {
    val dexClsInfos = file.ClassDefsSection.getItems();
    val classList = new ArrayList<DexClass>(dexClsInfos.size());

    for (val dexClsInfo : dexClsInfos)
      classList.add(new DexClass(this, dexClsInfo));

    return classList;
  }

  /*
   * Needs to generate a short, but unique class name
   */
  private DexClassType generateClassType() {
	val typeCache = getTypeCache();
	
    String desc;
    long suffix = 0L;
    do {
      desc = "L$" + suffix + ";";
      suffix++;
    } while (typeCache.encounteredClassType(desc));

    return DexClassType.parse(desc, typeCache);
  }

  private List<DexClass> parseExtraClasses() {
    val parsingCache = getTypeCache();

    // generate types
    val clsTaintConstants = generateClassType();
    val clsInternalClassAnnotation = generateClassType();
    val clsInternalMethodAnnotation = generateClassType();
    val clsObjTaint = generateClassType();
    val clsObjTaintEntry = generateClassType();
    val clsMethodCallHelper = generateClassType();

    // set descriptor replacements
    ClassRenamer renamer = parsingCache.getClassRenamer();
    renamer.addRule(CLASS_TAINTCONSTANTS, clsTaintConstants.getDescriptor());
    renamer.addRule(CLASS_INTERNALCLASS, clsInternalClassAnnotation.getDescriptor());
    renamer.addRule(CLASS_INTERNALMETHOD, clsInternalMethodAnnotation.getDescriptor());
    renamer.addRule(CLASS_OBJTAINT, clsObjTaint.getDescriptor());
    renamer.addRule(CLASS_OBJTAINTENTRY, clsObjTaintEntry.getDescriptor());
    renamer.addRule(CLASS_METHODCALLHELPER, clsMethodCallHelper.getDescriptor());

    // open the merge DEX file
    DexFile mergeDex;
    try {
      mergeDex = new DexFromMemory(resAuxiliaryDex);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // parse the classes
    val extraClasses = parseAllClasses(mergeDex);
    
    // remove renamer rules
    renamer.removeRule(CLASS_TAINTCONSTANTS);
    renamer.removeRule(CLASS_INTERNALCLASS);
    renamer.removeRule(CLASS_INTERNALMETHOD);
    renamer.removeRule(CLASS_OBJTAINT);
    renamer.removeRule(CLASS_OBJTAINTENTRY);
    renamer.removeRule(CLASS_METHODCALLHELPER);

    // store Object Taint Storage class type and method references
    // store MethodCallHelper class type & method and field references
    taintConstants_Type = clsTaintConstants;
    objectTaintStorage_Type = clsObjTaint;
    methodCallHelper_Type = clsMethodCallHelper;
    internalClassAnnotation_Type = clsInternalClassAnnotation;
    internalMethodAnnotation_Type = clsInternalMethodAnnotation;

    for (val clazz : extraClasses)
      if (clazz.getType() == objectTaintStorage_Type) {
        for (val method : clazz.getMethods())
          if (!(method instanceof DexDirectMethod))
            continue;
          else if (method.getName().equals("get"))
            objectTaintStorage_Get = (DexDirectMethod) method;
          else if (method.getName().equals("set"))
            objectTaintStorage_Set = (DexDirectMethod) method;

      } else if (clazz.getType() == methodCallHelper_Type) {
        for (val field : clazz.getFields())
          if (field.getName().equals("ARG"))
            methodCallHelper_Arg = field;
          else if (field.getName().equals("RES"))
            methodCallHelper_Res = field;
          else if (field.getName().equals("S_ARG"))
            methodCallHelper_SArg = field;
          else if (field.getName().equals("S_RES"))
            methodCallHelper_SRes = field;

      } else if (clazz.getType() == taintConstants_Type) {
        for (val method : clazz.getMethods())
          if (!(method instanceof DexDirectMethod))
            continue;
          else if (method.getName().equals("queryTaint"))
            taintConstants_QueryTaint = (DexDirectMethod) method;
          else if (method.getName().equals("serviceTaint"))
            taintConstants_ServiceTaint = (DexDirectMethod) method;
          else if (method.getName().equals("hasSourceAndSinkTaint"))
            taintConstants_HasSourceAndSinkTaint = (DexDirectMethod) method;
      }

    return extraClasses;
  }

  private List<DexClass> generateExtraClasses() {
    val parsingCache = getTypeCache();

    externalStaticFieldTaint_Class = new DexClass(
      this,
      generateClassType(),
      DexClassType.parse("Ljava/lang/Object;", parsingCache),
      EnumSet.of(AccessFlags.PUBLIC),
      null,
      null,
      null,
      null);

    val clinitCode = new DexCode();
    clinitCode.add(new DexInstruction_ReturnVoid(clinitCode));

    externalStaticFieldTaint_Clinit = new DexDirectMethod(
      externalStaticFieldTaint_Class,
      "<clinit>",
      EnumSet.of(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR),
      new DexPrototype(DexVoid.parse("V", parsingCache), null),
      clinitCode,
      null, null);
    externalStaticFieldTaint_Class.addMethod(externalStaticFieldTaint_Clinit);

    return Arrays.asList(new DexClass[] { externalStaticFieldTaint_Class });
  }

  public List<InstrumentationWarning> instrument(boolean debug) {
    val cache = new DexInstrumentationCache(this, debug);

    val extraClassesLinked = parseExtraClasses();
    val extraClassesGenerated = generateExtraClasses();

    for (val cls : classes)
      cls.instrument(cache);

    classes.addAll(extraClassesLinked);
    classes.addAll(extraClassesGenerated);

    return cache.getWarnings();
  }

  public byte[] writeToFile() {
    val parsingCache = getTypeCache();

    val outFile = new DexFile();
    val out = new ByteArrayAnnotatedOutput();

    val asmCache = new DexAssemblingCache(outFile, parsingCache);
    for (val cls : classes)
      cls.writeToFile(outFile, asmCache);

    // Apply jumbo-instruction fix requires ReferencedItem being 
    // placed first, after which the code needs to be placed again
    // because jumbo instruction is wider.
    // The second pass shoudn't change ReferencedItem's placement 
    // (because they are ordered deterministically by its content)
    // otherwise we'll be in trouble.
    outFile.place();
    fixInstructions(outFile);
    outFile.place();
    outFile.writeTo(out);

    val bytes = out.toByteArray();

    DexFile.calcSignature(bytes);
    DexFile.calcChecksum(bytes);

    return bytes;
  }

  private static final String CLASS_OBJTAINT = "Luk/ac/cam/db538/dexter/merge/ObjectTaintStorage;";
  private static final String CLASS_OBJTAINTENTRY = "Luk/ac/cam/db538/dexter/merge/ObjectTaintStorage$Entry;";
  private static final String CLASS_METHODCALLHELPER = "Luk/ac/cam/db538/dexter/merge/MethodCallHelper;";
  private static final String CLASS_INTERNALCLASS = "Luk/ac/cam/db538/dexter/merge/InternalClassAnnotation;";
  private static final String CLASS_INTERNALMETHOD = "Luk/ac/cam/db538/dexter/merge/InternalMethodAnnotation;";
  private static final String CLASS_TAINTCONSTANTS = "Luk/ac/cam/db538/dexter/merge/TaintConstants;";

  public void countInstructions() {
    val count = new HashMap<Class<?>, Integer>();
    for (val clazz : classes)
      clazz.countInstructions(count);
    for (val entry : count.entrySet())
      System.out.println(entry.getKey().getSimpleName() + "," + entry.getValue().toString());
  }
  
  private void fixInstructions(DexFile outFile) {
	  for (CodeItem codeItem : outFile.CodeItemsSection.getItems()) {
		  codeItem.fixInstructions(true, true);
	  }
  }

}
