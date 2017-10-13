package org.spoofax.jsglr2.parseforest.symbolrule;

import org.metaborg.sdf2table.jsglrinterfaces.ISGLRProduction;
import org.metaborg.sdf2table.parsetable.ProductionType;
import org.spoofax.jsglr2.parseforest.IDerivation;
import org.spoofax.jsglr2.parser.Parse;
import org.spoofax.jsglr2.parser.Position;

public class RuleNode extends SRParseForest implements IDerivation<SRParseForest> {
	
	public final ISGLRProduction production;
    public final ProductionType productionType;
	public final SRParseForest[] parseForests;
	
	public RuleNode(int nodeNumber, Parse parse, Position startPosition, Position endPosition, ISGLRProduction production, ProductionType productionType, SRParseForest[] parseForests) {
		super(nodeNumber, parse, startPosition, endPosition);
		this.production = production;
        this.productionType = productionType;
		this.parseForests = parseForests;
	}
	
	public String descriptor() {
		return production.descriptor();
	}

    public ISGLRProduction production() {
        return production;
    }
    
    public SRParseForest[] parseForests() {
        return parseForests;
    }

}
