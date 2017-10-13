package org.spoofax.jsglr2.parser;

import java.util.List;

import org.metaborg.sdf2table.jsglrinterfaces.ISGLRGoto;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRParseTable;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRReduce;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRState;
import org.spoofax.jsglr2.parseforest.AbstractParseForest;
import org.spoofax.jsglr2.parseforest.ParseForestManager;
import org.spoofax.jsglr2.stack.StackLink;
import org.spoofax.jsglr2.stack.StackManager;
import org.spoofax.jsglr2.stack.StackPath;
import org.spoofax.jsglr2.stack.elkhound.ElkhoundStackNode;

public class ReducerElkhound<ParseForest extends AbstractParseForest, ParseNode extends ParseForest, Derivation> extends Reducer<ElkhoundStackNode<ParseForest>, ParseForest, ParseNode, Derivation> {

    public ReducerElkhound(ISGLRParseTable parseTable, StackManager<ElkhoundStackNode<ParseForest>, ParseForest> stackManager, ParseForestManager<ParseForest, ParseNode, Derivation> parseForestManager) {
        super(parseTable, stackManager, parseForestManager);
    }
    
    @Override
    public void doReductions(Parse<ElkhoundStackNode<ParseForest>, ParseForest> parse, ElkhoundStackNode<ParseForest> stack, ISGLRReduce reduce) {
        if (reduce.production().isCompletionOrRecovery())
            return;       
        
        if (stack.deterministicDepth >= reduce.arity()) {
            StackPath<ElkhoundStackNode<ParseForest>, ParseForest> deterministicPath = stack.findDeterministicPathOfLength(reduce.arity());
            
            if (parse.activeStacks.size() == 1)
                reduceElkhoundPath(parse, deterministicPath, reduce); // Do standard LR if there is only 1 active stack
            else
                reducePath(parse, deterministicPath, reduce); // Benefit from faster path retrieval, but still do extra checks since there are other active stacks
        } else {
            // Fall back to regular GLR
            for (StackPath<ElkhoundStackNode<ParseForest>, ParseForest> path : stackManager.findAllPathsOfLength(stack, reduce.arity()))
                reducePath(parse, path, reduce);
        }
    }
    
    private void reduceElkhoundPath(Parse<ElkhoundStackNode<ParseForest>, ParseForest> parse, StackPath<ElkhoundStackNode<ParseForest>, ParseForest> path, ISGLRReduce reduce) {
        ElkhoundStackNode<ParseForest> pathEnd = path.lastStackNode(); 
        
        List<ParseForest> parseNodes = path.getParseForests();
    
        ISGLRGoto gotoAction = pathEnd.state.getGoto(reduce.production().productionNumber());
        ISGLRState gotoState = parseTable.getState(gotoAction.gotoState());
        
        reducerElkhound(parse, pathEnd, gotoState, reduce, parseNodes);
    }
    
    private void reducerElkhound(Parse<ElkhoundStackNode<ParseForest>, ParseForest> parse, ElkhoundStackNode<ParseForest> stack, ISGLRState gotoState, ISGLRReduce reduce, List<ParseForest> parseForests) {
        Derivation derivation = parseForestManager.createDerivation(parse, reduce.production(), reduce.productionType(), parseForests);
        ParseForest parseNode = parseForestManager.createParseNode(parse, reduce.production(), derivation);
        
        ElkhoundStackNode<ParseForest> newStack = stackManager.createStackNode(parse, gotoState);
        StackLink<ElkhoundStackNode<ParseForest>, ParseForest> link = stackManager.createStackLink(parse, newStack, stack, parseNode);
        
        parse.activeStacks.add(newStack);
        parse.forActor.add(newStack);
        
        if (reduce.isRejectProduction())
            stackManager.rejectStackLink(parse, link);
    }

}
