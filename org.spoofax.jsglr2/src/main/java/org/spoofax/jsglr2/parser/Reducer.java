package org.spoofax.jsglr2.parser;

import java.util.ArrayDeque;
import java.util.List;

import org.metaborg.sdf2table.jsglrinterfaces.ISGLRGoto;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRParseTable;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRReduce;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRState;
import org.spoofax.jsglr2.parseforest.AbstractParseForest;
import org.spoofax.jsglr2.parseforest.ParseForestManager;
import org.spoofax.jsglr2.stack.AbstractStackNode;
import org.spoofax.jsglr2.stack.StackLink;
import org.spoofax.jsglr2.stack.StackManager;
import org.spoofax.jsglr2.stack.StackPath;

public class Reducer<StackNode extends AbstractStackNode<ParseForest>, ParseForest extends AbstractParseForest, ParseNode extends ParseForest, Derivation> {

    protected final ISGLRParseTable parseTable;
    protected final StackManager<StackNode, ParseForest> stackManager;
    protected final ParseForestManager<ParseForest, ParseNode, Derivation> parseForestManager;
    
    public Reducer(ISGLRParseTable parseTable, StackManager<StackNode, ParseForest> stackManager, ParseForestManager<ParseForest, ParseNode, Derivation> parseForestManager) {
        this.parseTable = parseTable;
        this.stackManager = stackManager;
        this.parseForestManager = parseForestManager;
    }
    
    public void doReductions(Parse<StackNode, ParseForest> parse, StackNode stack, ISGLRReduce reduce) {
        if (reduce.production().isCompletionOrRecovery())
            return;
        
        for (StackPath<StackNode, ParseForest> path : stackManager.findAllPathsOfLength(stack, reduce.arity()))
            reducePath(parse, path, reduce);
    }
    
    private void doLimitedRedutions(Parse<StackNode, ParseForest> parse, StackNode stack, ISGLRReduce reduce, StackLink<StackNode, ParseForest> link) {
        if (reduce.production().isCompletionOrRecovery())
            return;
        
        for (StackPath<StackNode, ParseForest> path : stackManager.findAllPathsOfLength(stack, reduce.arity())) {
            if (path.contains(link))
                reducePath(parse, path, reduce);
        }
    }
    
    protected void reducePath(Parse<StackNode, ParseForest> parse, StackPath<StackNode, ParseForest> path, ISGLRReduce reduce) {
        StackNode pathEnd = path.lastStackNode(); 
        
        List<ParseForest> parseNodes = path.getParseForests();
    
        ISGLRGoto gotoAction = pathEnd.state.getGoto(reduce.production().productionNumber());
        ISGLRState gotoState = parseTable.getState(gotoAction.gotoState());
        
        reducer(parse, pathEnd, gotoState, reduce, parseNodes);
    }
    
    private void reducer(Parse<StackNode, ParseForest> parse, StackNode stack, ISGLRState gotoState, ISGLRReduce reduce, List<ParseForest> parseForests) {
        StackNode activeStackWithGotoState = stackManager.findActiveStackWithState(parse, gotoState);
        
        parse.notify(observer -> observer.reduce(reduce, parseForests, activeStackWithGotoState));
        
        Derivation derivation = parseForestManager.createDerivation(parse, reduce.production(), reduce.productionType(), parseForests);
        
        if (activeStackWithGotoState != null) {
            StackLink<StackNode, ParseForest> directLink = stackManager.findDirectLink(activeStackWithGotoState, stack);
            
            parse.notify(observer -> observer.directLinkFound(directLink));
            
            if (directLink != null) {
                @SuppressWarnings("unchecked")
                ParseNode parseNode = (ParseNode) directLink.parseForest;
                
                parseForestManager.addDerivation(parse, parseNode, derivation);
                
                if (reduce.isRejectProduction())
                    stackManager.rejectStackLink(parse, directLink);
            } else {
                ParseForest parseNode = parseForestManager.createParseNode(parse, reduce.production(), derivation);
                
                StackLink<StackNode, ParseForest> link = stackManager.createStackLink(parse, activeStackWithGotoState, stack, parseNode);
                
                if (reduce.isRejectProduction())
                    stackManager.rejectStackLink(parse, link);
                
                // Copy the currently active stacks since the reductions performed below could add new ones (resulting into java.util.ConcurrentModificationException)
                ArrayDeque<StackNode> activeStacksCopy = new ArrayDeque<StackNode>(parse.activeStacks);
                
                for (StackNode activeStack : activeStacksCopy) {
                    if (!activeStack.allLinksRejected() && !parse.forActor.contains(activeStack) && !parse.forActorDelayed.contains(activeStack))
                        for (ISGLRReduce reduceAction : activeStack.state.applicableReduceActions(parse.currentChar))
                            doLimitedRedutions(parse, activeStack, reduceAction, link);
                }
            }
        } else {
            StackNode newStack = stackManager.createStackNode(parse, gotoState);
            ParseForest parseNode = parseForestManager.createParseNode(parse, reduce.production(), derivation);
            
            StackLink<StackNode, ParseForest> link = stackManager.createStackLink(parse, newStack, stack, parseNode);
            
            parse.activeStacks.add(newStack);
            
            if (newStack.state.isRejectable())
                parse.forActorDelayed.add(newStack);
            else
                parse.forActor.add(newStack);
            
            if (reduce.isRejectProduction())
                stackManager.rejectStackLink(parse, link);
        }
    }

}
