package org.spoofax.jsglr2.parseforest.datadependent;

import java.util.BitSet;

import org.metaborg.sdf2table.jsglrinterfaces.ISGLRProduction;
import org.metaborg.sdf2table.parsetable.ParseTableProduction;
import org.metaborg.sdf2table.parsetable.ProductionType;
import org.spoofax.jsglr2.parseforest.IDerivation;
import org.spoofax.jsglr2.parser.Parse;
import org.spoofax.jsglr2.parser.Position;

public class DataDependentRuleNode extends DataDependentParseForest implements IDerivation<DataDependentParseForest> {

    public final ISGLRProduction production;
    public final ProductionType productionType;
    public final DataDependentParseForest[] parseForests;
    public final BitSet leftContexts;
    public final BitSet rightContexts;

    public DataDependentRuleNode(int nodeNumber, Parse parse, Position startPosition, Position endPosition,
        ISGLRProduction production, ProductionType productionType, DataDependentParseForest[] parseForests) {
        super(nodeNumber, parse, startPosition, endPosition);
        this.production = production;
        this.productionType = productionType;
        this.parseForests = parseForests;
        rightContexts = new BitSet();
        leftContexts = new BitSet();

        if(parseForests.length > 0) {
            DataDependentParseForest leftmost = parseForests[0];
            if(leftmost instanceof DataDependentSymbolNode) {
                if(((DataDependentSymbolNode) leftmost).production instanceof ParseTableProduction) {
                    ParseTableProduction p = (ParseTableProduction) ((DataDependentSymbolNode) leftmost).production;
                    if(p.getLeftmostContextsMapping().containsKey(p.productionNumber())) {
                        leftContexts.set(p.getLeftmostContextsMapping().get(p.productionNumber()));
                    }
                }
                
                for(DataDependentRuleNode rn : ((DataDependentSymbolNode) leftmost).getDerivations()) {
                    leftContexts.or(rn.leftContexts);
                }
            }

            DataDependentParseForest rightmost = parseForests[parseForests.length - 1];
            if(rightmost instanceof DataDependentSymbolNode) {
                if(((DataDependentSymbolNode) rightmost).production instanceof ParseTableProduction) {
                    ParseTableProduction p = (ParseTableProduction) ((DataDependentSymbolNode) rightmost).production;
                    if(p.getRightmostContextsMapping().containsKey(p.productionNumber())) {
                        rightContexts.set(p.getRightmostContextsMapping().get(p.productionNumber()));
                    }
                }
                for(DataDependentRuleNode rn : ((DataDependentSymbolNode) rightmost).getDerivations()) {
                    rightContexts.or(rn.rightContexts);
                }
            }


        }
    }

    public String descriptor() {
        return production.descriptor();
    }

    public ISGLRProduction production() {
        return production;
    }

    public DataDependentParseForest[] parseForests() {
        return parseForests;
    }

}
