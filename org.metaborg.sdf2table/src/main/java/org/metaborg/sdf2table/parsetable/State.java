package org.metaborg.sdf2table.parsetable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;
import java.util.Set;

import org.metaborg.sdf2table.grammar.CharacterClass;
import org.metaborg.sdf2table.grammar.IProduction;
import org.metaborg.sdf2table.grammar.Symbol;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRAction;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRGoto;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRReduce;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRState;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

public class State implements ISGLRState, Comparable<State>, Serializable {

    private static final long serialVersionUID = 7118071460461287164L;

    ParseTable pt;

    private final int label;
    private Set<ISGLRGoto> gotos;
    private Map<Integer, ISGLRGoto> gotosMapping;
    private final Set<LRItem> kernel;
    private Set<LRItem> items;
    private SetMultimap<Symbol, LRItem> symbol_items;
    private SetMultimap<CharacterClass, ISGLRAction> lr_actions;

    private StateStatus status = StateStatus.VISIBLE;

    public Set<State> states = Sets.newHashSet();

    public State(IProduction p, ParseTable pt) {
        items = Sets.newHashSet();
        gotos = Sets.newHashSet();
        gotosMapping = Maps.newHashMap();
        kernel = Sets.newHashSet();
        symbol_items = HashMultimap.create();
        lr_actions = HashMultimap.create();

        this.pt = pt;
        label = this.pt.totalStates();
        this.pt.stateLabels().put(label, this);
        this.pt.incTotalStates();

        LRItem item = new LRItem(p, 0, pt);
        kernel.add(item);
        pt.kernelMap().put(kernel, this);
    }

    public State(Set<LRItem> kernel, ParseTable pt) {
        items = Sets.newHashSet();
        gotos = Sets.newHashSet();
        gotosMapping = Maps.newHashMap();
        symbol_items = HashMultimap.create();
        lr_actions = HashMultimap.create();

        this.kernel = Sets.newHashSet();
        this.kernel.addAll(kernel);
        pt.kernelMap().put(kernel, this);

        this.pt = pt;
        label = this.pt.totalStates();
        this.pt.stateLabels().put(label, this);
        this.pt.incTotalStates();
    }

    public void closure() {
        for(LRItem item : kernel) {
            // if(item.getDotPosition() < item.getProd().rightHand().size()) {
            // pt.symbolStatesMapping().put(item.getProd().rightHand().get(item.getDotPosition()), this);
            // }
            item.process(items, symbol_items, this);
        }
    }

    public void doShift() {
        for(Symbol s_at_dot : symbol_items.keySet()) {
            if(s_at_dot instanceof CharacterClass) {
                Set<LRItem> new_kernel = Sets.newHashSet();
                Set<GoTo> new_gotos = Sets.newHashSet();
                Set<Shift> new_shifts = Sets.newHashSet();
                for(LRItem item : symbol_items.get(s_at_dot)) {
                    Shift shift = new Shift((CharacterClass) s_at_dot);
                    new_kernel.add(item.shiftDot());
                    if(!(item.getProd().equals(pt.initialProduction()) && item.getDotPosition() == 1)) {
                        new_shifts.add(shift);
                    }
                    new_gotos.add(new GoTo((CharacterClass) s_at_dot, pt));
                }
                if(!new_kernel.isEmpty()) {
                    checkKernel(new_kernel, new_gotos, new_shifts);
                }
            } else {
                for(IProduction p : pt.normalizedGrammar().getSymbolProductionsMapping().get(s_at_dot)) {

                    // p might be a contextual production
                    if(pt.normalizedGrammar().getProdContextualProdMapping().get(p) != null) {
                        p = pt.normalizedGrammar().getProdContextualProdMapping().get(p);
                    }

                    Set<LRItem> new_kernel = Sets.newHashSet();
                    Set<GoTo> new_gotos = Sets.newHashSet();
                    Set<Shift> new_shifts = Sets.newHashSet();
                    for(LRItem item : symbol_items.get(s_at_dot)) {
                        // if item.prod does not conflict with p
                        if(!LRItem.isPriorityConflict(item, p, pt.normalizedGrammar().priorities())) {
                            new_kernel.add(item.shiftDot());
                            new_gotos.add(new GoTo(pt.productionLabels().get(p), pt));
                        }
                    }
                    if(!new_kernel.isEmpty()) {
                        checkKernel(new_kernel, new_gotos, new_shifts);
                    }
                }
            }
        }
    }

    public void doReduces() {
        // for each item p_i : A = A0 ... AN .
        // add a reduce action reduce([0-256] / follow(A), p_i)
        for(LRItem item : items) {

            if(item.getDotPosition() == item.getProd().rightHand().size()) {
                int prod_label = pt.productionLabels().get(item.getProd());

                if(item.getProd().leftHand().followRestriction() == null
                    || item.getProd().leftHand().followRestriction().equals(CharacterClass.emptyCC)) {
                    addReduceAction(item.getProd(), prod_label, CharacterClass.getFullCharacterClass(), null);
                } else {
                    // Not based on first and follow sets thus, only considering the follow restrictions
                    CharacterClass final_range = CharacterClass.getFullCharacterClass()
                        .difference(item.getProd().leftHand().followRestriction());
                    for(CharacterClass[] s : item.getProd().leftHand().followRestrictionLookahead()) {
                        final_range.difference(s[0]);

                        // create reduce Lookahead actions
                        CharacterClass[] lookahead = Arrays.copyOfRange(s, 1, s.length);
                        addReduceAction(item.getProd(), prod_label, s[0], lookahead);
                    }
                    addReduceAction(item.getProd(), prod_label, final_range, null);
                }
            }
            // <Start> = <START> . EOF
            if(item.getProd().equals(pt.initialProduction()) && item.getDotPosition() == 1) {
                BitSet bs = new BitSet(257);
                bs.set(256);
                lr_actions.put(new CharacterClass(bs), new Accept(new CharacterClass(bs)));
            }
        }
    }


    private void addReduceAction(IProduction p, Integer label, CharacterClass cc, CharacterClass[] lookahead) {
        CharacterClass final_range = cc;
        ParseTableProduction prod = pt.productionsMapping().get(p);

        for(CharacterClass range : lr_actions.keySet()) {
            if(final_range.equals(CharacterClass.emptyCC)) {
                break;
            }
            CharacterClass intersection = CharacterClass.intersection(final_range, range);
            if(!intersection.equals(CharacterClass.emptyCC)) {
                if(intersection.equals(range)) {
                    if(lookahead != null) {
                        lr_actions.put(intersection, new ReduceLookahead(prod, label, intersection, lookahead));
                    } else {
                        lr_actions.put(intersection, new Reduce(prod, label, intersection));
                    }
                    final_range = final_range.difference(intersection);
                }
            }
        }

        if(!final_range.equals(CharacterClass.emptyCC)) {
            if(lookahead != null) {
                lr_actions.put(final_range, new ReduceLookahead(prod, label, final_range, lookahead));
            } else {
                lr_actions.put(final_range, new Reduce(prod, label, final_range));
            }
        }
    }

    private void checkKernel(Set<LRItem> new_kernel, Set<GoTo> new_gotos, Set<Shift> new_shifts) {
        if(pt.kernelMap().containsKey(new_kernel)) {
            int stateNumber = pt.kernelMap().get(new_kernel).getLabel();
            // set recently added shift and goto actions to new state
            for(Shift shift : new_shifts) {
                shift.setState(stateNumber);

                this.lr_actions.put(shift.cc, shift);
                // this.lr_actions.add(new LRAction(shift.cc, shift));
            }
            for(GoTo g : new_gotos) {
                g.setState(stateNumber);
                this.gotos.add(g);
                this.gotosMapping.put(g.label, g);
            }
        } else {
            State new_state = new State(new_kernel, pt);
            for(Shift shift : new_shifts) {
                shift.setState(new_state.getLabel());
                this.lr_actions.put(shift.cc, shift);
                // this.lr_actions.add(new LRAction(shift.cc, shift));
            }
            for(GoTo g : new_gotos) {
                g.setState(new_state.getLabel());
                this.gotos.add(g);
                this.gotosMapping.put(g.label, g);
            }
            pt.stateQueue().add(new_state);
        }
    }

    @Override
    public String toString() {
        String buf = "";
        int i = 0;
        buf += "State " + getLabel();
        if(!gotos.isEmpty()) {
            buf += "\nGotos: ";
        }
        for(ISGLRGoto g : gotos) {
            if(i != 0)
                buf += "\n     , ";
            buf += g;
            i++;
        }
        if(!lr_actions.isEmpty()) {
            buf += "\nActions: ";
        }
        i = 0;
        for(CharacterClass cc : lr_actions.keySet()) {
            if(i != 0)
                buf += "\n       , ";
            buf += cc + ": ";
            int j = 0;
            for(ISGLRAction a : lr_actions.get(cc)) {
                if(j != 0)
                    buf += ", ";
                buf += a;
                j++;
            }
            i++;
        }
        if(!items.isEmpty()) {
            buf += "\nItems: ";

            i = 0;
            for(LRItem it : items) {
                if(i != 0)
                    buf += "\n       ";
                buf += it.toString();
                i++;
            }
        } else {
            buf += "\nItems: ";

            i = 0;
            for(LRItem it : kernel) {
                if(i != 0)
                    buf += "\n       ";
                buf += it.toString();
                i++;
            }
        }

        return buf;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((kernel == null) ? 0 : kernel.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        State other = (State) obj;
        if(kernel == null) {
            if(other.kernel != null)
                return false;
        } else if(!kernel.equals(other.kernel))
            return false;
        return true;
    }

    @Override
    public int compareTo(State o) {
        return this.getLabel() - o.getLabel();
    }

    public int getLabel() {
        return label;
    }

    public Set<LRItem> getItems() {
        return items;
    }

    public StateStatus status() {
        return status;
    }

    public void setStatus(StateStatus status) {
        this.status = status;
    }

    public void markDirty() {
        this.items.clear();
        this.symbol_items.clear();
        this.lr_actions.clear();
        this.setStatus(StateStatus.DIRTY);
    }

    @Override
    public int stateNumber() {
        return label;
    }

    @Override
    public Set<ISGLRGoto> gotos() {
        return gotos;
    }

    @Override
    public Iterable<ISGLRAction> actions() {
        Set<ISGLRAction> actions = Sets.newHashSet(lr_actions.values());
        return actions;
    }

    public SetMultimap<CharacterClass, ISGLRAction> actionsMapping() {
        return lr_actions;
    }

    @Override
    public boolean isRejectable() {
        // TODO implement this
        return false;
    }

    @Override
    public Iterable<ISGLRAction> applicableActions(int character) {
        Set<ISGLRAction> applicableActions = Sets.newHashSet();
        for(CharacterClass cc : lr_actions.keySet()) {
            if(cc.containsCharacter(character)) {
                applicableActions.addAll(lr_actions.get(cc));
            }
        }
        return applicableActions;
    }

    @Override
    public Iterable<ISGLRReduce> applicableReduceActions(int character) {
        Set<ISGLRReduce> reduceActions = Sets.newHashSet();
        for(CharacterClass cc : lr_actions.keySet()) {
            if(cc.containsCharacter(character)) {
                for(ISGLRAction action : lr_actions.get(cc)) {
                    if(action instanceof ISGLRReduce) {
                        reduceActions.add((ISGLRReduce) action);
                    }
                }

            }
        }
        return reduceActions;
    }

    @Override
    public ISGLRGoto getGoto(int productionNumber) {
        return gotosMapping.get(productionNumber);
    }


}
