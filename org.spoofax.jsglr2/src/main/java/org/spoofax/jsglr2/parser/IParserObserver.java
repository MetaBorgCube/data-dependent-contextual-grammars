package org.spoofax.jsglr2.parser;

import java.util.List;
import java.util.Queue;

import org.metaborg.sdf2table.jsglrinterfaces.ISGLRAction;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRProduction;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRReduce;
import org.spoofax.jsglr2.parseforest.AbstractParseForest;
import org.spoofax.jsglr2.stack.AbstractStackNode;
import org.spoofax.jsglr2.stack.StackLink;

public interface IParserObserver<StackNode extends AbstractStackNode<ParseForest>, ParseForest extends AbstractParseForest> {

	public void parseStart(String inputString);
	
	public void parseCharacter(int character, Queue<StackNode> activeStacks);
	
	public void createStackNode(StackNode stack);
	
	public void createStackLink(int linkNumber, StackNode from, StackNode to, ParseForest parseNode);
	
	public void rejectStackLink(StackLink<StackNode, ParseForest> link);
    
    public void forActorStacks(Queue<StackNode> forActor, Queue<StackNode> forActorDelayed);
    
    public void actor(StackNode stack, Iterable<ISGLRAction> applicableActions);
	
	public void skipRejectedStack(StackNode stack);
	
	public void addForShifter(ForShifterElement<StackNode, ParseForest> forShifterElement);
	
	public void reduce(ISGLRReduce reduce, List<? extends AbstractParseForest> parseNodes, StackNode activeStackWithGotoState);
	
	public void directLinkFound(StackLink<StackNode, ParseForest> directLink);
	
	public void accept(StackNode acceptingStack);
	
	public void createParseNode(AbstractParseForest parseNode, ISGLRProduction production);
	
	public void createDerivation(AbstractParseForest[] parseNodes);
	
	public void createCharacterNode(AbstractParseForest characterNode, int character);
	
	public void addDerivation(AbstractParseForest parseNode);
	
	public void shifter(AbstractParseForest termNode, Queue<ForShifterElement<StackNode, ParseForest>> forShifter);
	
	public void remark(String remark);
	
	public void success(ParseSuccess<ParseForest> success);
	
	public void failure(ParseFailure<ParseForest> failure);
	
}
