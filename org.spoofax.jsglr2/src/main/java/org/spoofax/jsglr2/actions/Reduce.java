package org.spoofax.jsglr2.actions;

import org.metaborg.sdf2table.jsglrinterfaces.ISGLRCharacters;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRProduction;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRReduce;
import org.metaborg.sdf2table.parsetable.ProductionType;

public class Reduce extends Action implements ISGLRReduce {

	private final ISGLRProduction production;
    private final ProductionType productionType;
	private final int arity;
	
	public Reduce(ISGLRCharacters characters, ISGLRProduction production, ProductionType productionType, int arity) {
	    super(characters);

        this.production = production;
        this.productionType = productionType;
        this.arity = arity;
	}
	
	public ISGLRProduction production() {
	    return production;
	}
	
	public ProductionType productionType() {
        return productionType;
    }
    
    public int arity() {
        return arity;
    }

    @Override public String toString() {
        return "Reduce [production=" + production + ", productionType=" + productionType + ", arity=" + arity + "]";
    }
}
