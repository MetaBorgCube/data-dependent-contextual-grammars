package org.spoofax.jsglr2.tokenizer.hybrid;

import org.metaborg.sdf2table.jsglrinterfaces.ISGLRProduction;
import org.spoofax.jsglr2.parseforest.hybrid.Derivation;
import org.spoofax.jsglr2.parseforest.hybrid.HParseForest;
import org.spoofax.jsglr2.parseforest.hybrid.ParseNode;
import org.spoofax.jsglr2.tokenizer.Tokenizer;

public class HTokenizer extends Tokenizer<HParseForest, ParseNode, Derivation> {

    protected ISGLRProduction parseNodeProduction(ParseNode parseNode) {
        return parseNode.production;
    }

    protected Iterable<Derivation> parseNodeDerivations(ParseNode parseNode) {
        return parseNode.getDerivations();
    }

}
