package org.spoofax.jsglr2.parseforest;

import java.util.List;

import org.metaborg.sdf2table.jsglrinterfaces.ISGLRProduction;
import org.metaborg.sdf2table.parsetable.ProductionType;
import org.spoofax.jsglr2.parser.Parse;
import org.spoofax.jsglr2.parser.Position;

public abstract class ParseForestManager<ParseForest extends AbstractParseForest, ParseNode extends ParseForest, Derivation> {

    abstract public ParseForest createParseNode(Parse<?, ParseForest> parse, ISGLRProduction production, Derivation firstDerivation);
    
    abstract public Derivation createDerivation(Parse<?, ParseForest> parse, ISGLRProduction production, ProductionType productionType, List<ParseForest> parseForests);
    
    abstract public void addDerivation(Parse<?, ParseForest> parse, ParseNode parseNode, Derivation derivation);
    
    abstract public ParseForest createCharacterNode(Parse<?, ParseForest> parse);
    
    protected Cover getCover(Parse<?, ParseForest> parse, ParseForest[] parseNodes) {
        Position startPosition, endPosition;
        
        if (parseNodes.length == 0)
            startPosition = endPosition = parse.currentPosition();
        else {
            ParseForest firstParseNode = parseNodes[0];
            ParseForest lastParseNode = parseNodes[parseNodes.length - 1];

            startPosition = firstParseNode.startPosition;
            endPosition = lastParseNode.endPosition;
        }
        
        return new Cover(startPosition, endPosition);
    }
    
    // Represents the part of the input that a parse node covers
    protected class Cover {
        public Position startPosition, endPosition;
        
        public Cover(Position startPosition, Position endPosition) {
            this.startPosition = startPosition;
            this.endPosition = endPosition;
        }
    }
    
}
