package org.spoofax.jsglr2.actions;

import org.metaborg.sdf2table.jsglrinterfaces.ISGLRCharacters;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRShift;

public class Shift extends Action implements ISGLRShift {

	private final int shiftState;
	
	public Shift(ISGLRCharacters characters, int shiftState) {
		super(characters);
		
		this.shiftState = shiftState;
	}
	
	public int shiftState() {
	    return shiftState;
	}
	
}
