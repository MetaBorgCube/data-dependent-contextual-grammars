package org.spoofax.jsglr2.parsetable;

import java.util.Set;

import org.metaborg.sdf2table.jsglrinterfaces.ISGLRAction;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRGoto;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRReduce;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRState;
import org.metaborg.sdf2table.parsetable.ActionType;

import com.google.common.collect.Sets;

public class State implements ISGLRState {
	
	private final int stateNumber;
	private final Set<ISGLRGoto> gotos;
	private final Set<ISGLRAction> actions;
	private boolean rejectable;
	
	public State(int stateNumber, ISGLRGoto[] gotos, ISGLRAction[] actions) {
		this.stateNumber = stateNumber;
		this.gotos = Sets.newHashSet(gotos);
        this.actions = Sets.newHashSet(actions);
        this.rejectable = false;
	}
	
	public int stateNumber() {
	    return stateNumber;
	}
	
	public Set<ISGLRGoto> gotos() {
	    return gotos;
	}
    
	public Set<ISGLRAction> actions() {
	    return actions;
	}
    
    public boolean isRejectable() {
        return rejectable;
    }
    
    public void markRejectable() {
        this.rejectable = true;
    }
	
	public Set<ISGLRAction> applicableActions(int character) {
	    Set<ISGLRAction> res = Sets.newHashSet();
		
		for (ISGLRAction action : actions) {
			if (action.appliesTo(character))
				res.add(action);
		}
		
		return res;
	}
	
	public Set<ISGLRReduce> applicableReduceActions(int character) {
		Set<ISGLRReduce> res = Sets.newHashSet();
		
		for (ISGLRAction action : actions) {
			if (action.actionType() == ActionType.REDUCE && action.appliesTo(character)) // TODO: handle reduces with lookahead
				res.add((ISGLRReduce) action);
		}
		
		return res;
	}
	
	public ISGLRGoto getGoto(int production) {
		for (ISGLRGoto gotoAction : gotos) {
			for (int otherProduction : gotoAction.productions())
				if (production == otherProduction)
					return gotoAction;
		}
		
		return null;
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof State))
			return false;
		
		State otherState = (State) obj;
		
		return this.stateNumber == otherState.stateNumber;
	}

}
