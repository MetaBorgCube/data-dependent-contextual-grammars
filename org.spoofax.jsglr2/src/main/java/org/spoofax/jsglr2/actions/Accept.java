package org.spoofax.jsglr2.actions;

import org.metaborg.sdf2table.jsglrinterfaces.ISGLRAccept;
import org.spoofax.jsglr2.characters.Characters;
import org.spoofax.jsglr2.characters.SingleCharacter;

public class Accept extends Action implements ISGLRAccept {

	public Accept() {
		super(new SingleCharacter(Characters.EOF));
	}
	
}
