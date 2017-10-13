package org.spoofax.jsglr2.parseforest.datadependent;

import java.util.List;

import org.metaborg.sdf2table.jsglrinterfaces.ISGLRProduction;
import org.spoofax.jsglr2.imploder.StrategoTermImploder;

public class DDStrategoImploder extends StrategoTermImploder<DataDependentParseForest, DataDependentSymbolNode, DataDependentRuleNode> {
	
    public DDStrategoImploder() {
        super(new DDTokenizer());
    }

    protected ISGLRProduction parseNodeProduction(DataDependentSymbolNode symbolNode) {
        return symbolNode.production;
    }

    protected DataDependentRuleNode parseNodeOnlyDerivation(DataDependentSymbolNode symbolNode) {
        return symbolNode.getOnlyDerivation();
    }

    protected List<DataDependentRuleNode> parseNodePreferredAvoidedDerivations(DataDependentSymbolNode symbolNode) {
        return symbolNode.getPreferredAvoidedDerivations();
    }

}
