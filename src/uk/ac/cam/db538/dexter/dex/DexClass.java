package uk.ac.cam.db538.dexter.dex;

import java.util.HashSet;
import java.util.Set;

import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.code.DexInstructionParsingException;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

import lombok.Getter;
import lombok.val;

public class DexClass {

  @Getter private final Dex ParentFile;
  @Getter private final DexClassType Type;
  @Getter private final DexClassType SuperType;
  @Getter private final Set<AccessFlags> AccessFlagSet;
  @Getter private final Set<DexField> Fields;
  @Getter private final Set<DexMethod> Methods;

  public DexClass(Dex parent, DexClassType type, DexClassType superType, Set<AccessFlags> accessFlags, Set<DexField> fields, Set<DexMethod> methods) {
    ParentFile = parent;
    Type = type;
    SuperType = superType;
    AccessFlagSet = Utils.getNonNullAccessFlagSet(accessFlags);
    Fields = (fields == null) ? new HashSet<DexField>() : fields;
    Methods = (methods == null) ? new HashSet<DexMethod>() : methods;
  }

  public DexClass(Dex parent, ClassDefItem clsInfo) throws UnknownTypeException, DexInstructionParsingException {
    this(parent,
         DexClassType.parse(clsInfo.getClassType().getTypeDescriptor() , parent.getParsingCache()),
         DexClassType.parse(clsInfo.getSuperclass().getTypeDescriptor() , parent.getParsingCache()),
         Utils.getAccessFlagSet(AccessFlags.getAccessFlagsForClass(clsInfo.getAccessFlags())),
         null,
         null);

    val clsData = clsInfo.getClassData();
    if (clsData != null) {
      for (val staticFieldInfo : clsData.getStaticFields())
        Fields.add(new DexField(this, staticFieldInfo));
      for (val instanceFieldInfo : clsData.getInstanceFields())
        Fields.add(new DexField(this, instanceFieldInfo));

      for (val directMethodInfo : clsData.getDirectMethods())
        Methods.add(new DexMethodWithCode(this, directMethodInfo));
      for (val virtualMethodInfo : clsData.getVirtualMethods()) {
    	  if (virtualMethodInfo.codeItem == null)
    		  Methods.add(new DexPurelyVirtualMethod(this, virtualMethodInfo));
    	  else
    		  Methods.add(new DexMethodWithCode(this, virtualMethodInfo));
      }
    }
  }

  public boolean isAbstract() {
    return AccessFlagSet.contains(AccessFlags.ABSTRACT);
  }

  public boolean isAnnotation() {
    return AccessFlagSet.contains(AccessFlags.ANNOTATION);
  }

  public boolean isEnum() {
    return AccessFlagSet.contains(AccessFlags.ENUM);
  }

  public boolean isInterface() {
    return AccessFlagSet.contains(AccessFlags.INTERFACE);
  }

  public void addField(DexField f) {
    if (f.getParentClass() != null)
      f.getParentClass().removeField(f);

    Fields.add(f);
  }

  public void removeField(DexField f) {
    if (f.getParentClass() == this) {
      Fields.remove(f);
      f.setParentClass(null);
    }
  }

  public void addMethod(DexMethod m) {
    if (m.getParentClass() != null)
      m.getParentClass().removeMethod(m);

    Methods.add(m);
  }

  public void removeMethod(DexMethod m) {
    if (m.getParentClass() == this) {
      Methods.remove(m);
      m.setParentClass(null);
    }
  }
}
