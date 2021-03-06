package uk.ac.cam.db538.dexter.dex.code.insn.invoke;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.utils.InstructionList;
import uk.ac.cam.db538.dexter.utils.Pair;

public abstract class ExternalCallInstrumentor {

  public abstract boolean canBeApplied(DexPseudoinstruction_Invoke insn);
  public abstract Pair<InstructionList, InstructionList> generateInstrumentation(DexPseudoinstruction_Invoke insn, DexCode_InstrumentationState state);

  public static List<ExternalCallInstrumentor> getInstrumentors() {
    val instrumentors = new ArrayList<ExternalCallInstrumentor>();
    instrumentors.add(new Source_ContentResolver());
    instrumentors.add(new Source_SystemService());
    instrumentors.add(new Source_Browser());
    instrumentors.add(new Sink_IPC());
    instrumentors.add(new Sink_HttpClient());
    instrumentors.add(new Sink_Log());
    instrumentors.add(new Sink_IO());
    instrumentors.add(new Taint_File());
    instrumentors.add(new Taint_Socket());
    return instrumentors;
  }
}
