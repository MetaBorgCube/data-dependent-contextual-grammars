package org.spoofax.jsglr2.stack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.metaborg.sdf2table.jsglrinterfaces.ISGLRState;
import org.spoofax.jsglr2.parseforest.AbstractParseForest;
import org.spoofax.jsglr2.parser.Parse;

public abstract class StackManager<StackNode extends AbstractStackNode<ParseForest>, ParseForest extends AbstractParseForest> {
    
    public abstract StackNode createInitialStackNode(Parse<StackNode, ParseForest> parse, ISGLRState state);
    
    public abstract StackNode createStackNode(Parse<StackNode, ParseForest> parse, ISGLRState state);
    
    public abstract StackLink<StackNode, ParseForest> createStackLink(Parse<StackNode, ParseForest> parse, StackNode from, StackNode to, ParseForest parseNode);
    
    public void rejectStackLink(Parse<StackNode, ParseForest> parse, StackLink<StackNode, ParseForest> link) {
        link.reject();
        
        parse.notify(observer -> observer.rejectStackLink(link));
    }
    
    public StackNode findActiveStackWithState(Parse<StackNode, ParseForest> parse, ISGLRState state) {
        for (StackNode activeStack : parse.activeStacks) {
            if (activeStack.state.equals(state))
                return activeStack;
        }
        
        return null;
    }
    
    public StackLink<StackNode, ParseForest> findDirectLink(StackNode from, StackNode to) {
        for (StackLink<StackNode, ParseForest> link : stackLinksOut(from)) {
            if (link.to == to)
                return link;
        }
        
        return null;
    }
    
    public List<StackPath<StackNode, ParseForest>> findAllPathsOfLength(StackNode stack, int length) {
        if (length == 0)
            return Arrays.asList(new EmptyStackPath<StackNode, ParseForest>(stack));
        else {
            List<StackPath<StackNode, ParseForest>> paths = new ArrayList<StackPath<StackNode, ParseForest>>();
            
            for (StackLink<StackNode, ParseForest> ownLink : stackLinksOut(stack)) {          
                if (length == 1)
                    paths.add(new NonEmptyStackPath<StackNode, ParseForest>(ownLink));
                else {
                    List<StackPath<StackNode, ParseForest>> pathsAfterOwnLink = findAllPathsOfLength(ownLink.to, length - 1);
                    
                    for (StackPath<StackNode, ParseForest> pathAfterOwnLink : pathsAfterOwnLink)
                        paths.add(new NonEmptyStackPath<StackNode, ParseForest>(ownLink, pathAfterOwnLink));
                }
            }
            
            return paths;
        }
    }
    
    protected abstract Iterable<StackLink<StackNode, ParseForest>> stackLinksOut(StackNode stack);
    
}
