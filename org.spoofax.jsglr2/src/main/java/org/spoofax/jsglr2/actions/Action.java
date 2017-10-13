package org.spoofax.jsglr2.actions;

import org.metaborg.sdf2table.jsglrinterfaces.ISGLRAction;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRCharacters;

public abstract class Action implements ISGLRAction {

	private final ISGLRCharacters characters;
	
	public Action(ISGLRCharacters characters) {
		this.characters = characters;
	}
    
    public ISGLRCharacters characters() {
        return characters;
    }
	
}