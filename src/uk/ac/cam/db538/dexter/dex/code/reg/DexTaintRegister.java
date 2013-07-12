package uk.ac.cam.db538.dexter.dex.code.reg;

import lombok.Getter;

public class DexTaintRegister extends DexRegister {

	@Getter private final DexStandardRegister originalRegister;

	// Only to be called by Dex{Single,Wide}OriginalRegister constructors
	DexTaintRegister(DexStandardRegister origReg) {
		this.originalRegister = origReg;
	}

	@Override
	public String toString() {
		return "t" + originalRegister.getPlainId();
	}

	@Override
	public RegisterWidth getWidth() {
		return RegisterWidth.SINGLE;
	}

	@Override
	public boolean storesType(RegisterType type) {
		return type == RegisterType.SINGLE_PRIMITIVE;
	}
}
