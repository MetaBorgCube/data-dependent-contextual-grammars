package org.spoofax.jsglr2.parser;

import org.metaborg.sdf2table.jsglrinterfaces.ISGLRState;
import org.spoofax.jsglr2.stack.AbstractStackNode;

public class ForShifterElement<StackNode extends AbstractStackNode<ParseForest>, ParseForest> {

	public final StackNode stack;
	public final ISGLRState state;
	
	public ForShifterElement(StackNode stack, ISGLRState state) {
		this.stack = stack;
		this.state = state;
	}
	
}
