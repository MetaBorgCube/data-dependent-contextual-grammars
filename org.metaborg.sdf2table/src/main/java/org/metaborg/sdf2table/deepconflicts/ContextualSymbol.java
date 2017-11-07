package org.metaborg.sdf2table.deepconflicts;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.metaborg.sdf2table.grammar.CharacterClass;
import org.metaborg.sdf2table.grammar.Symbol;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.common.collect.Sets;

public class ContextualSymbol extends Symbol {

    private static final long serialVersionUID = -2886358954796970390L;

    private final Symbol s;
    private final Set<Context> contexts;
    private final BitSet lefmostDeepContexts;
    private final BitSet rightmostDeepContexts;

    public ContextualSymbol(Symbol s, Set<Context> context, BitSet leftMostDeepContexts, BitSet rightMostDeepContexts) {
        this.s = s;
        this.contexts = context;
        this.lefmostDeepContexts = leftMostDeepContexts;
        this.rightmostDeepContexts = rightMostDeepContexts;
    }

    public ContextualSymbol(Symbol s, Context context, BitSet leftMostDeepContexts, BitSet rightMostDeepContexts) {
        this.s = s;
        Set<Context> contexts = Sets.newHashSet(context);
        this.contexts = contexts;
        this.lefmostDeepContexts = leftMostDeepContexts;
        this.rightmostDeepContexts = rightMostDeepContexts;
    }

    public ContextualSymbol(Symbol s, Set<Context> context, Map<Integer, Integer> leftContextsBitSetMapping,
        Map<Integer, Integer> rightContextsBitSetMapping) {
        this.s = s;
        this.contexts = context;
        lefmostDeepContexts = new BitSet();
        rightmostDeepContexts = new BitSet();

        for(Context c : contexts) {
            if(c.getType().equals(ContextType.DEEP)) {
                if(c.getPosition().equals(ContextPosition.LEFTMOST)) {
                    lefmostDeepContexts.set(leftContextsBitSetMapping.get(c.getContext()));
                } else if(c.getPosition().equals(ContextPosition.RIGHTMOST)) {
                    rightmostDeepContexts.set(rightContextsBitSetMapping.get(c.getContext()));
                }
            }
        }
    }

    public ContextualSymbol(Symbol s, Context context, Map<Integer, Integer> leftContextsBitSetMapping,
        Map<Integer, Integer> rightContextsBitSetMapping) {
        this.s = s;
        Set<Context> contexts = Sets.newHashSet(context);
        this.contexts = contexts;
        lefmostDeepContexts = new BitSet();
        rightmostDeepContexts = new BitSet();

        for(Context c : contexts) {
            if(c.getType().equals(ContextType.DEEP)) {
                if(c.getPosition().equals(ContextPosition.LEFTMOST)) {
                    lefmostDeepContexts.set(leftContextsBitSetMapping.get(c.getContext()));
                } else if(c.getPosition().equals(ContextPosition.RIGHTMOST)) {
                    rightmostDeepContexts.set(rightContextsBitSetMapping.get(c.getContext()));
                }
            }
        }
    }

    @Override public String name() {
        String buf = "";
        if(!getContexts().isEmpty()) {
            boolean hasLeftContext = false;
            for(Context p : getContexts()) {
                if(p.getPosition() == ContextPosition.LEFTMOST) {
                    hasLeftContext = true;
                    break;
                }
            }
            if(hasLeftContext) {
                int i = 0;
                buf += "{";
                for(Context p : getContexts()) {
                    if(p.getPosition() == ContextPosition.RIGHTMOST)
                        continue;
                    if(i != 0)
                        buf += ", ";
                    buf += p;
                    i++;
                }
                buf += "}";
            }
        }
        buf += getOrigSymbol().name();
        if(!getContexts().isEmpty()) {
            boolean hasRightContext = false;
            for(Context p : getContexts()) {
                if(p.getPosition() == ContextPosition.RIGHTMOST) {
                    hasRightContext = true;
                    break;
                }
            }
            if(hasRightContext) {
                int i = 0;
                buf += "{";
                for(Context p : getContexts()) {
                    if(p.getPosition() == ContextPosition.LEFTMOST)
                        continue;
                    if(i != 0)
                        buf += ", ";
                    buf += p;
                    i++;
                }
                buf += "}";
            }
        }
        return buf;
    }

    @Override public CharacterClass followRestriction() {
        return getOrigSymbol().followRestriction();
    }

    @Override public List<CharacterClass[]> followRestrictionLookahead() {
        return getOrigSymbol().followRestrictionLookahead();
    }

    public Set<Context> getContexts() {
        return contexts;
    }

    public BitSet getLefmostDeepContexts() {
        return lefmostDeepContexts;
    }

    public BitSet getRightmostDeepContexts() {
        return rightmostDeepContexts;
    }

    public Symbol getOrigSymbol() {
        return s;
    }

    public ContextualSymbol addContext(Context new_context, Map<Integer, Integer> leftContextsBitSetMapping,
        Map<Integer, Integer> rightContextsBitSetMapping) {
        Set<Context> new_contexts = Sets.newHashSet();
        new_contexts.addAll(getContexts());
        new_contexts.add(new_context);
        BitSet newLeftMostDeepContexts = (BitSet) lefmostDeepContexts.clone();
        BitSet newRightMostDeepContexts = (BitSet) rightmostDeepContexts.clone();

        if(new_context.getType().equals(ContextType.DEEP)) {
            if(new_context.getPosition().equals(ContextPosition.LEFTMOST)) {
                newLeftMostDeepContexts.set(leftContextsBitSetMapping.get(new_context.getContext()));
            } else if(new_context.getPosition().equals(ContextPosition.RIGHTMOST)) {
                newRightMostDeepContexts.set(rightContextsBitSetMapping.get(new_context.getContext()));
            }
        }

        return new ContextualSymbol(getOrigSymbol(), new_contexts, newLeftMostDeepContexts, newRightMostDeepContexts);
    }

    public ContextualSymbol addContexts(Set<Context> contexts, Map<Integer, Integer> leftContextsBitSetMapping,
        Map<Integer, Integer> rightContextsBitSetMapping) {
        Set<Context> new_contexts = Sets.newHashSet();
        new_contexts.addAll(contexts);
        new_contexts.addAll(this.getContexts());
        BitSet newLeftMostDeepContexts = (BitSet) lefmostDeepContexts.clone();
        BitSet newRightMostDeepContexts = (BitSet) rightmostDeepContexts.clone();

        for(Context c : contexts) {
            if(c.getType().equals(ContextType.DEEP)) {
                if(c.getPosition().equals(ContextPosition.LEFTMOST)) {
                    newLeftMostDeepContexts.set(leftContextsBitSetMapping.get(c.getContext()));
                } else if(c.getPosition().equals(ContextPosition.RIGHTMOST)) {
                    newRightMostDeepContexts.set(rightContextsBitSetMapping.get(c.getContext()));
                }
            }
        }

        return new ContextualSymbol(getOrigSymbol(), new_contexts, newLeftMostDeepContexts, newRightMostDeepContexts);
    }

    @Override public IStrategoTerm toAterm(ITermFactory tf) {
        return getOrigSymbol().toAterm(tf);
    }

    @Override public IStrategoTerm toSDF3Aterm(ITermFactory tf, Map<Set<Context>, Integer> ctx_vals, Integer ctx_val) {
        return getOrigSymbol().toSDF3Aterm(tf, ctx_vals, ctx_vals.get(getContexts()));
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((contexts == null) ? 0 : contexts.hashCode());
        result = prime * result + ((s == null) ? 0 : s.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        ContextualSymbol other = (ContextualSymbol) obj;
        if(contexts == null) {
            if(other.contexts != null)
                return false;
        } else if(!contexts.equals(other.contexts))
            return false;
        if(s == null) {
            if(other.s != null)
                return false;
        } else if(!s.equals(other.s))
            return false;
        return true;
    }

}
