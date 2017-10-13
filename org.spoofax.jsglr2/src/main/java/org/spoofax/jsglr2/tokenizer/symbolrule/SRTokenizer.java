package org.spoofax.jsglr2.tokenizer.symbolrule;

import org.metaborg.sdf2table.jsglrinterfaces.ISGLRProduction;
import org.spoofax.jsglr2.parseforest.symbolrule.RuleNode;
import org.spoofax.jsglr2.parseforest.symbolrule.SRParseForest;
import org.spoofax.jsglr2.parseforest.symbolrule.SymbolNode;
import org.spoofax.jsglr2.tokenizer.Tokenizer;

public class SRTokenizer extends Tokenizer<SRParseForest, SymbolNode, RuleNode> {

    protected ISGLRProduction parseNodeProduction(SymbolNode symbolNode) {
        return symbolNode.production;
    }

    protected Iterable<RuleNode> parseNodeDerivations(SymbolNode symbolNode) {
        return symbolNode.getDerivations();
    }

}
