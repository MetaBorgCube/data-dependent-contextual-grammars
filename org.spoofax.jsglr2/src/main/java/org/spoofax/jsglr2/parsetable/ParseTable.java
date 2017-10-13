package org.spoofax.jsglr2.parsetable;

import java.util.Set;

import org.metaborg.sdf2table.jsglrinterfaces.ISGLRParseTable;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRProduction;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRState;

import com.google.common.collect.Sets;

public class ParseTable implements ISGLRParseTable {

    final private Set<ISGLRProduction> productions;
    final private ISGLRState[] states;
    final private int startStateNumber;

    public ParseTable(ISGLRProduction[] productions, ISGLRState[] states, int startStateNumber) {
        this.productions = Sets.newHashSet(productions);
        this.states = states;
        this.startStateNumber = startStateNumber;
    }

    public Set<ISGLRProduction> productions() {
        return productions;
    }

    public ISGLRState startState() {
        return states[startStateNumber];
    }

    public ISGLRState getState(int stateNumber) {
        return states[stateNumber];
    }

}
