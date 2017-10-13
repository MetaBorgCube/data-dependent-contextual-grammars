package org.spoofax.jsglr2.parser;

import java.util.ArrayList;
import java.util.List;

import org.metaborg.sdf2table.jsglrinterfaces.ISGLRAction;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRParseTable;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRReduce;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRReduceLookahead;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRShift;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRState;
import org.spoofax.jsglr2.characters.Characters;
import org.spoofax.jsglr2.parseforest.AbstractParseForest;
import org.spoofax.jsglr2.parseforest.ParseForestManager;
import org.spoofax.jsglr2.stack.AbstractStackNode;
import org.spoofax.jsglr2.stack.StackManager;

public class Parser<StackNode extends AbstractStackNode<ParseForest>, ParseForest extends AbstractParseForest, ParseNode extends ParseForest, Derivation> implements IParser<StackNode, ParseForest> {

    private final ISGLRParseTable parseTable;
    private final StackManager<StackNode, ParseForest> stackManager;
    private final ParseForestManager<ParseForest, ParseNode, Derivation> parseForestManager;
    private final Reducer<StackNode, ParseForest, ParseNode, Derivation> reducer;
	private final List<IParserObserver<StackNode, ParseForest>> observers;
    
    public Parser(ISGLRParseTable parseTable, StackManager<StackNode, ParseForest> stackManager, ParseForestManager<ParseForest, ParseNode, Derivation> parseForestManager) {
        this.parseTable = parseTable;
        this.stackManager = stackManager;
        this.parseForestManager = parseForestManager;
        this.reducer = new Reducer<StackNode, ParseForest, ParseNode, Derivation>(parseTable, stackManager, parseForestManager);
        this.observers = new ArrayList<IParserObserver<StackNode, ParseForest>>();
    }
    
    public Parser(ISGLRParseTable parseTable, StackManager<StackNode, ParseForest> stackManager, ParseForestManager<ParseForest, ParseNode, Derivation> parseForestManager, Reducer<StackNode, ParseForest, ParseNode, Derivation> reducer) {
        this.parseTable = parseTable;
        this.stackManager = stackManager;
        this.parseForestManager = parseForestManager;
        this.reducer = reducer;
        this.observers = new ArrayList<IParserObserver<StackNode, ParseForest>>();
    }
	
	public ParseResult<ParseForest> parse(String inputString, String filename) {
		notify(observer -> observer.parseStart(inputString));
		
		Parse<StackNode, ParseForest> parse = new Parse<StackNode, ParseForest>(inputString, filename, observers);
        
		StackNode initialStackNode = stackManager.createInitialStackNode(parse, parseTable.startState());

        parse.activeStacks.add(initialStackNode);
		
		try {
			parseCharacter(parse);
			
			while (parse.hasNext() && !parse.activeStacks.isEmpty()) {
				parse.next();
				
				parseCharacter(parse);
			}
			
			ParseResult<ParseForest> result;
			
			if (parse.acceptingStack != null) {
				ParseSuccess<ParseForest> success = new ParseSuccess<ParseForest>(parse, stackManager.findDirectLink(parse.acceptingStack, initialStackNode).parseForest);
				
				notify(observer -> observer.success(success));
				
				result = success;
			} else {
				ParseFailure<ParseForest> failure = new ParseFailure<ParseForest>(parse, new ParseException("unknown parse fail (file: " + parse.filename + ", char: " + parse.currentChar + "/'" + Characters.charToString(parse.currentChar) + "', position: " + parse.currentPosition().coordinatesToString() + " [" + parse.currentPosition().offset + "/" + parse.inputLength + "])"));
				
				notify(observer -> observer.failure(failure));
				
				result = failure;
			}
			
			return result;
		} catch (ParseException parseException) {
			ParseFailure<ParseForest> failure = new ParseFailure<ParseForest>(parse, parseException);
			
			notify(observer -> observer.failure(failure));
			
			return failure;
		}
	}
	
	private void parseCharacter(Parse<StackNode, ParseForest> parse) {
		notify(observer -> observer.parseCharacter(parse.currentChar, parse.activeStacks)); 
		
		parse.forActor.clear();
		parse.forActor.addAll(parse.activeStacks);
		parse.forActorDelayed.clear();
		
		parse.forShifter.clear();
		
		notify(observer -> observer.forActorStacks(parse.forActor, parse.forActorDelayed));
		
		while (!parse.forActor.isEmpty() || !parse.forActorDelayed.isEmpty()) {
			if (parse.forActor.isEmpty())
			    parse.forActor.add(parse.forActorDelayed.remove());
			else
				while (!parse.forActor.isEmpty()) {
				    StackNode stack = parse.forActor.remove();
					
					if (!stack.allLinksRejected())
						actor(stack, parse);
					else
					    notify(observer -> observer.skipRejectedStack(stack));
				}
	        
	        notify(observer -> observer.forActorStacks(parse.forActor, parse.forActorDelayed));
		}
		
		shifter(parse);
	}
	
	private void actor(StackNode stack, Parse<StackNode, ParseForest> parse) {
		Iterable<ISGLRAction> applicableActions = stack.state.applicableActions(parse.currentChar);
				
		notify(observer -> observer.actor(stack, applicableActions));
		
		for (ISGLRAction action : applicableActions)
			switch (action.actionType()) {
			case SHIFT:
				ISGLRShift shiftAction = (ISGLRShift) action;
				ISGLRState shiftState = parseTable.getState(shiftAction.shiftState());

				addForShifter(parse, stack, shiftState);
				
				break;
            case REDUCE:
                ISGLRReduce reduceAction = (ISGLRReduce) action;
                reducer.doReductions(parse, stack, reduceAction);                
                break;
            case REDUCE_LOOKAHEAD:
                ISGLRReduceLookahead reduceLookaheadAction = (ISGLRReduceLookahead) action;
                
                if (reduceLookaheadAction.allowsLookahead(parse.getLookahead(reduceLookaheadAction.lookaheadSize()))) {
                    reducer.doReductions(parse, stack, reduceLookaheadAction);
                }
                
                break;
			case ACCEPT:
			    parse.acceptingStack = stack;
				
				notify(observer -> observer.accept(stack));
				
				break;
			}
	}
	
	private void shifter(Parse<StackNode, ParseForest> parse) {
	    parse.activeStacks.clear();
		
		ParseForest characterNode = parseForestManager.createCharacterNode(parse);
		
		notify(observer -> observer.shifter(characterNode, parse.forShifter));
		
		for (ForShifterElement<StackNode, ParseForest> forShifterElement : parse.forShifter) {
		    StackNode activeStackForState = stackManager.findActiveStackWithState(parse, forShifterElement.state);
			
			if (activeStackForState != null) {
			    stackManager.createStackLink(parse, activeStackForState, forShifterElement.stack, characterNode);
			} else {
			    StackNode newStack = stackManager.createStackNode(parse, forShifterElement.state);
				
				stackManager.createStackLink(parse, newStack, forShifterElement.stack, characterNode);
				
				parse.activeStacks.add(newStack);
			}
		}
	}
	
	private void addForShifter(Parse<StackNode, ParseForest> parse, StackNode stack, ISGLRState shiftState) {
		ForShifterElement<StackNode, ParseForest> forShifterElement = new ForShifterElement<StackNode, ParseForest>(stack, shiftState);
		
		notify(observer -> observer.addForShifter(forShifterElement));
		
		parse.forShifter.add(forShifterElement);
	}
	
	public void attachObserver(IParserObserver<StackNode, ParseForest> observer) {
		observers.add(observer);
	}
	
	private void notify(IParserNotification<StackNode, ParseForest> notification) {
		for (IParserObserver<StackNode, ParseForest> observer : observers)
			notification.notify(observer);
	}
	
}
