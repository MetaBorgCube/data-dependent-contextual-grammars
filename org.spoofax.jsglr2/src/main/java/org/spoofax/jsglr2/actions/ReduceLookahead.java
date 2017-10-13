package org.spoofax.jsglr2.actions;

import org.metaborg.sdf2table.jsglrinterfaces.ISGLRCharacters;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRProduction;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRReduceLookahead;
import org.metaborg.sdf2table.parsetable.ProductionType;
import org.spoofax.jsglr2.parser.Parse;

public class ReduceLookahead extends Reduce implements ISGLRReduceLookahead {

	private final ISGLRCharacters[] followRestriction;
	
	public ReduceLookahead(ISGLRCharacters characters, ISGLRProduction production, ProductionType productionType, int arity, ISGLRCharacters[] followRestriction) {
		super(characters, production, productionType, arity);

		this.followRestriction = followRestriction;
	}
    
    public boolean allowsLookahead(String lookahead) {
        if (lookahead.length() != followRestriction.length)
            return true;
        
        for (int i = 0; i < followRestriction.length; i++) {
            if (!followRestriction[i].containsCharacter(lookahead.charAt(i)))
                return true;
        }
        
        return false;
    }
    
    public boolean allowsLookahead(Parse parse) {
        String lookahead = parse.getLookahead(lookaheadSize());
        
        return allowsLookahead(lookahead);
    }

    @Override public int lookaheadSize() {
        return followRestriction.length;
    }
	
}
