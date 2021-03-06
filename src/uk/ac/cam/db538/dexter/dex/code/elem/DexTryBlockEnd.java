package uk.ac.cam.db538.dexter.dex.code.elem;

import lombok.Getter;
import lombok.Setter;
import uk.ac.cam.db538.dexter.dex.code.DexCode;

public class DexTryBlockEnd extends DexCodeElement {

  @Getter @Setter private DexTryBlockStart blockStart;

  public DexTryBlockEnd(DexCode methodCode, DexTryBlockStart blockStart) {
    super(methodCode);

    this.blockStart = blockStart;
  }

  @Override
  public String getOriginalAssembly() {
    return "} // end TRY" + blockStart.getOriginalAbsoluteOffsetString();
  }

  @Override
  public boolean cfgEndsBasicBlock() {
    return true;
  }
}
