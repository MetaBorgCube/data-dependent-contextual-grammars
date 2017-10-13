package org.spoofax.jsglr2.parseforest.hybrid;

import org.metaborg.sdf2table.jsglrinterfaces.ISGLRProduction;
import org.metaborg.sdf2table.parsetable.ProductionType;
import org.spoofax.jsglr2.parseforest.IDerivation;

public class Derivation implements IDerivation<HParseForest> {

    public final ISGLRProduction production;
    public final ProductionType productionType;
    public final HParseForest[] parseForests;
    
    public Derivation(ISGLRProduction production, ProductionType productionType, HParseForest[] parseForests) {
        this.production = production;
        this.productionType = productionType;
        this.parseForests = parseForests;
    }

    public ISGLRProduction production() {
        return production;
    }
    
    public HParseForest[] parseForests() {
        return parseForests;
    }
    
}
