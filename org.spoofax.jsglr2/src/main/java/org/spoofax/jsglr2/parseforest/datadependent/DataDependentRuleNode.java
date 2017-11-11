package org.spoofax.jsglr2.parseforest.datadependent;

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

    private long contextBitmap = 0L;

    public DataDependentRuleNode(int nodeNumber, Parse parse, Position startPosition, Position endPosition,
        ISGLRProduction production, ProductionType productionType, DataDependentParseForest[] parseForests) {
        super(nodeNumber, parse, startPosition, endPosition);
        this.production = production;
        this.productionType = productionType;
        this.parseForests = parseForests;

        if (parseForests.length > 0) {
            if (parseForests.length == 1) {
                if (parseForests[0] instanceof DataDependentSymbolNode) {
                    final DataDependentSymbolNode parseForest = (DataDependentSymbolNode) parseForests[0];
                    final ParseTableProduction onlyProduction = (ParseTableProduction) parseForest.production;

                    // introduction of contextual token
                    contextBitmap |= onlyProduction.contextL();
                    contextBitmap |= onlyProduction.contextR();

                    // aggregation of recursive contextual tokens
                    for (DataDependentRuleNode ruleNode : parseForest.getDerivations()) {
                        contextBitmap |= (ruleNode.getContextBitmap());
                    }
                }
            } else {
                if (parseForests[0] instanceof DataDependentSymbolNode) {
                    final DataDependentSymbolNode parseForest = (DataDependentSymbolNode) parseForests[0];
                    final ParseTableProduction leftmostProduction = (ParseTableProduction) parseForest.production;

                    // introduction of contextual token
                    contextBitmap |= leftmostProduction.contextL();

                    // aggregation of recursive contextual tokens
                    for (DataDependentRuleNode ruleNode : parseForest.getDerivations()) {
                        contextBitmap |= (ruleNode.getContextBitmap());
                    }
                }

                if (parseForests[parseForests.length - 1] instanceof DataDependentSymbolNode) {
                    final DataDependentSymbolNode parseForest = (DataDependentSymbolNode) parseForests[parseForests.length - 1];
                    final ParseTableProduction rightmostProduction = (ParseTableProduction) parseForest.production;

                    // introduction of contextual token
                    contextBitmap |= rightmostProduction.contextR();

                    // aggregation of recursive contextual tokens
                    for (DataDependentRuleNode ruleNode : parseForest.getDerivations()) {
                        contextBitmap |= (ruleNode.getContextBitmap());
                    }
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

    public final long getContextBitmap() {
        return contextBitmap;
    }

}
