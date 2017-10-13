package org.spoofax.jsglr2.parseforest.datadependent;

import org.metaborg.sdf2table.jsglrinterfaces.ISGLRProduction;
import org.spoofax.jsglr2.tokenizer.Tokenizer;

public class DDTokenizer extends Tokenizer<DataDependentParseForest, DataDependentSymbolNode, DataDependentRuleNode> {

    protected ISGLRProduction parseNodeProduction(DataDependentSymbolNode symbolNode) {
        return symbolNode.production;
    }

    protected Iterable<DataDependentRuleNode> parseNodeDerivations(DataDependentSymbolNode symbolNode) {
        return symbolNode.getDerivations();
    }

}
