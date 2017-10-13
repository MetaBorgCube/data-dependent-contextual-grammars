package org.spoofax.jsglr2.stack;

import org.metaborg.sdf2table.jsglrinterfaces.ISGLRState;

public abstract class AbstractStackNode<ParseForest> {

	public final int stackNumber;
	public final ISGLRState state;
	
	public AbstractStackNode(int stackNumber, ISGLRState state) {
		this.stackNumber = stackNumber;
        this.state = state;
	}
    
    public abstract boolean allLinksRejected();
	
}
