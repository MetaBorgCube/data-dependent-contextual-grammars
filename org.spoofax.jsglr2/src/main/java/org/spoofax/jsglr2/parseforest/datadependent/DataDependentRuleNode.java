package org.spoofax.jsglr2.parseforest.datadependent;

import java.util.Set;

import org.metaborg.sdf2table.deepconflicts.ContextualProduction;
import org.metaborg.sdf2table.grammar.IProduction;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRProduction;
import org.metaborg.sdf2table.parsetable.ParseTableProduction;
import org.metaborg.sdf2table.parsetable.ProductionType;
import org.spoofax.jsglr2.parseforest.IDerivation;
import org.spoofax.jsglr2.parser.Parse;
import org.spoofax.jsglr2.parser.Position;

import com.google.common.collect.Sets;

public class DataDependentRuleNode extends DataDependentParseForest implements IDerivation<DataDependentParseForest> {

    public final ISGLRProduction production;
    public final ProductionType productionType;
    public final DataDependentParseForest[] parseForests;
    public final Set<IProduction> leftContexts;
    public final Set<IProduction> rightContexts;

    public DataDependentRuleNode(int nodeNumber, Parse parse, Position startPosition, Position endPosition,
        ISGLRProduction production, ProductionType productionType, DataDependentParseForest[] parseForests) {
        super(nodeNumber, parse, startPosition, endPosition);
        this.production = production;
        this.productionType = productionType;
        this.parseForests = parseForests;
        rightContexts = Sets.newHashSet();
        leftContexts = Sets.newHashSet();

        if(parseForests.length > 0) {
            DataDependentParseForest leftmost = parseForests[0];
            if(leftmost instanceof DataDependentSymbolNode) {
                if(((DataDependentSymbolNode) leftmost).production instanceof ParseTableProduction) {
                    ParseTableProduction p = (ParseTableProduction) ((DataDependentSymbolNode) leftmost).production;
                    if(p.constructor() != null) {
                        IProduction context = p.getProduction();
                        if(context instanceof ContextualProduction) {
                            context = ((ContextualProduction) context).getOrigProduction();                            
                        }
                        leftContexts.add(context);
                    }
                }
                
                for(DataDependentRuleNode rn : ((DataDependentSymbolNode) leftmost).getDerivations()) {
                    leftContexts.addAll(rn.leftContexts);
                }
            }

            DataDependentParseForest rightmost = parseForests[parseForests.length - 1];
            if(rightmost instanceof DataDependentSymbolNode) {
                if(((DataDependentSymbolNode) rightmost).production instanceof ParseTableProduction) {
                    ParseTableProduction p = (ParseTableProduction) ((DataDependentSymbolNode) rightmost).production;
                    if(p.constructor() != null) {
                        IProduction context = p.getProduction();
                        if(context instanceof ContextualProduction) {
                            context = ((ContextualProduction) context).getOrigProduction();                            
                        }
                        rightContexts.add(context);
                    }
                }
                for(DataDependentRuleNode rn : ((DataDependentSymbolNode) rightmost).getDerivations()) {
                    rightContexts.addAll(rn.rightContexts);
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
