package org.spoofax.jsglr2.parser;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Set;

import org.metaborg.sdf2table.deepconflicts.Context;
import org.metaborg.sdf2table.deepconflicts.ContextPosition;
import org.metaborg.sdf2table.deepconflicts.ContextType;
import org.metaborg.sdf2table.deepconflicts.ContextualProduction;
import org.metaborg.sdf2table.deepconflicts.ContextualSymbol;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRGoto;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRParseTable;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRProduction;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRReduce;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRState;
import org.metaborg.sdf2table.parsetable.ParseTableProduction;
import org.spoofax.jsglr2.parseforest.AbstractParseForest;
import org.spoofax.jsglr2.parseforest.ParseForestManager;
import org.spoofax.jsglr2.parseforest.datadependent.DataDependentRuleNode;
import org.spoofax.jsglr2.parseforest.datadependent.DataDependentSymbolNode;
import org.spoofax.jsglr2.stack.AbstractStackNode;
import org.spoofax.jsglr2.stack.StackLink;
import org.spoofax.jsglr2.stack.StackManager;
import org.spoofax.jsglr2.stack.StackPath;
import org.spoofax.jsglr2.stack.standard.StandardStackNode;

public class DataDependentReducer<StackNode extends AbstractStackNode<ParseForest>, ParseForest extends AbstractParseForest, ParseNode extends ParseForest, Derivation>
    extends Reducer<StandardStackNode<ParseForest>, ParseForest, ParseNode, Derivation> {

    public DataDependentReducer(ISGLRParseTable parseTable,
        StackManager<StandardStackNode<ParseForest>, ParseForest> stackManager,
        ParseForestManager<ParseForest, ParseNode, Derivation> parseForestManager) {
        super(parseTable, stackManager, parseForestManager);
    }

    @Override protected void reducePath(Parse<StandardStackNode<ParseForest>, ParseForest> parse,
        StackPath<StandardStackNode<ParseForest>, ParseForest> path, ISGLRReduce reduce) {
        StandardStackNode<ParseForest> pathEnd = path.lastStackNode();

        List<ParseForest> parseNodes = path.getParseForests();

        ISGLRProduction prod = reduce.production();
        if(prod instanceof ParseTableProduction) {
            if(((ParseTableProduction) prod).getProduction() instanceof ContextualProduction) {
                ContextualProduction p = (ContextualProduction) ((ParseTableProduction) prod).getProduction();
                for(int i = 0; i < p.rightHand().size(); i++) {
                    if(p.rightHand().get(i) instanceof ContextualSymbol) {
                        if(checkRightMostContext(parseNodes.get(i),
                            ((ContextualSymbol) p.rightHand().get(i)).getContexts())
                            || checkLeftMostContexts(parseNodes.get(i),
                                ((ContextualSymbol) p.rightHand().get(i)).getContexts())) {
                            return;
                        }
                    }
                }
            }
        }

        ISGLRGoto gotoAction = pathEnd.state.getGoto(reduce.production().productionNumber());
        ISGLRState gotoState = parseTable.getState(gotoAction.gotoState());

        reducer(parse, pathEnd, gotoState, reduce, parseNodes);
    }

    @Override public void doReductions(Parse<StandardStackNode<ParseForest>, ParseForest> parse,
        StandardStackNode<ParseForest> stack, ISGLRReduce reduce) {
        if(reduce.production().isCompletionOrRecovery())
            return;

        for(StackPath<StandardStackNode<ParseForest>, ParseForest> path : stackManager.findAllPathsOfLength(stack,
            reduce.arity()))
            reducePath(parse, path, reduce);
    }

    private void reducer(Parse<StandardStackNode<ParseForest>, ParseForest> parse, StandardStackNode<ParseForest> stack,
        ISGLRState gotoState, ISGLRReduce reduce, List<ParseForest> parseForests) {
        StandardStackNode<ParseForest> activeStackWithGotoState =
            stackManager.findActiveStackWithState(parse, gotoState);

        parse.notify(observer -> observer.reduce(reduce, parseForests, activeStackWithGotoState));

        Derivation derivation =
            parseForestManager.createDerivation(parse, reduce.production(), reduce.productionType(), parseForests);

        if(activeStackWithGotoState != null) {
            StackLink<StandardStackNode<ParseForest>, ParseForest> directLink =
                stackManager.findDirectLink(activeStackWithGotoState, stack);

            parse.notify(observer -> observer.directLinkFound(directLink));

            if(directLink != null) {
                @SuppressWarnings("unchecked") ParseNode parseNode = (ParseNode) directLink.parseForest;

                parseForestManager.addDerivation(parse, parseNode, derivation);

                if(reduce.isRejectProduction())
                    stackManager.rejectStackLink(parse, directLink);
            } else {
                ParseForest parseNode = parseForestManager.createParseNode(parse, reduce.production(), derivation);

                StackLink<StandardStackNode<ParseForest>, ParseForest> link =
                    stackManager.createStackLink(parse, activeStackWithGotoState, stack, parseNode);

                if(reduce.isRejectProduction())
                    stackManager.rejectStackLink(parse, link);

                // Copy the currently active stacks since the reductions performed below could add new ones (resulting
                // into java.util.ConcurrentModificationException)
                ArrayDeque<StandardStackNode<ParseForest>> activeStacksCopy =
                    new ArrayDeque<StandardStackNode<ParseForest>>(parse.activeStacks);

                for(StandardStackNode<ParseForest> activeStack : activeStacksCopy) {
                    if(!activeStack.allLinksRejected() && !parse.forActor.contains(activeStack)
                        && !parse.forActorDelayed.contains(activeStack))
                        for(ISGLRReduce reduceAction : activeStack.state.applicableReduceActions(parse.currentChar))
                            doLimitedRedutions(parse, activeStack, reduceAction, link);
                }
            }
        } else {
            StandardStackNode<ParseForest> newStack = stackManager.createStackNode(parse, gotoState);
            ParseForest parseNode = parseForestManager.createParseNode(parse, reduce.production(), derivation);

            StackLink<StandardStackNode<ParseForest>, ParseForest> link =
                stackManager.createStackLink(parse, newStack, stack, parseNode);

            parse.activeStacks.add(newStack);

            if(newStack.state.isRejectable())
                parse.forActorDelayed.add(newStack);
            else
                parse.forActor.add(newStack);

            if(reduce.isRejectProduction())
                stackManager.rejectStackLink(parse, link);
        }
    }

    private void doLimitedRedutions(Parse<StandardStackNode<ParseForest>, ParseForest> parse,
        StandardStackNode<ParseForest> stack, ISGLRReduce reduce,
        StackLink<StandardStackNode<ParseForest>, ParseForest> link) {
        if(reduce.production().isCompletionOrRecovery())
            return;

        for(StackPath<StandardStackNode<ParseForest>, ParseForest> path : stackManager.findAllPathsOfLength(stack,
            reduce.arity())) {
            if(path.contains(link))
                reducePath(parse, path, reduce);
        }
    }

    private boolean checkLeftMostContexts(ParseForest pf, Set<Context> contexts) {
        if(pf instanceof DataDependentSymbolNode) {
            for(DataDependentRuleNode rn : ((DataDependentSymbolNode) pf).getDerivations()) {
                for(Integer lmContext : rn.leftContexts) {
                    if(contexts.contains(new Context(lmContext, ContextType.DEEP, ContextPosition.LEFTMOST))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkRightMostContext(ParseForest pf, Set<Context> contexts) {

        if(pf instanceof DataDependentSymbolNode) {
            for(DataDependentRuleNode rn : ((DataDependentSymbolNode) pf).getDerivations()) {
                for(Integer rmContext : rn.rightContexts) {
                    if(contexts.contains(new Context(rmContext, ContextType.DEEP, ContextPosition.RIGHTMOST))) {
                        return true;
                    }
                }
            }
        }

        return false;


    }



}
