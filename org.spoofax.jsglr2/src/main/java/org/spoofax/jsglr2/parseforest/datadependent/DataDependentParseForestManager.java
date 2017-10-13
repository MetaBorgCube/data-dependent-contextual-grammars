package org.spoofax.jsglr2.parseforest.datadependent;

import java.util.List;

import org.metaborg.sdf2table.jsglrinterfaces.ISGLRProduction;
import org.metaborg.sdf2table.parsetable.ProductionType;
import org.spoofax.jsglr2.parseforest.ParseForestManager;
import org.spoofax.jsglr2.parser.Parse;

public class DataDependentParseForestManager extends ParseForestManager<DataDependentParseForest, DataDependentSymbolNode, DataDependentRuleNode> {

    public DataDependentSymbolNode createParseNode(Parse<?, DataDependentParseForest> parse, ISGLRProduction production, DataDependentRuleNode firstDerivation) {
        DataDependentSymbolNode symbolNode = new DataDependentSymbolNode(parse.parseNodeCount++, parse, firstDerivation.startPosition, firstDerivation.endPosition, production);
        
        parse.notify(observer -> observer.createParseNode(symbolNode, production));
        
        addDerivation(parse, symbolNode, firstDerivation);
                
        return symbolNode;
    }
    
    public DataDependentRuleNode createDerivation(Parse<?, DataDependentParseForest> parse, ISGLRProduction production, ProductionType productionType, List<DataDependentParseForest> parseForests) {
        DataDependentParseForest[] parseForestArray = parseForests.toArray(new DataDependentParseForest[parseForests.size()]);
        
        Cover cover = getCover(parse, parseForestArray);
        
        DataDependentRuleNode ruleNode = new DataDependentRuleNode(parse.parseNodeCount++, parse, cover.startPosition, cover.endPosition, production, productionType, parseForestArray);
        
        parse.notify(observer -> observer.createDerivation(parseForestArray));
                
        return ruleNode;
    }
    
    public void addDerivation(Parse<?, DataDependentParseForest> parse, DataDependentSymbolNode symbolNode, DataDependentRuleNode ruleNode) {
        parse.notify(observer -> observer.addDerivation(symbolNode));
        
        symbolNode.addDerivation(ruleNode);
    }
    
    public DataDependentTermNode createCharacterNode(Parse<?, DataDependentParseForest> parse) {
        DataDependentTermNode termNode = new DataDependentTermNode(parse.parseNodeCount++, parse, parse.currentPosition(), parse.currentChar);
        
        parse.notify(observer -> observer.createCharacterNode(termNode, termNode.character));
        
        return termNode;
    }
   
}
