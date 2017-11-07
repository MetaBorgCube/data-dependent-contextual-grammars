package org.metaborg.sdf2table.deepconflicts;

import java.io.Serializable;

public class Context implements Serializable {

    private static final long serialVersionUID = -4581589940398341265L;

    // label of the production causing the conflict
    private final int context;
    private final ContextType type;
    // propagate shallow context only to leftmost or rightmost symbols
    private final ContextPosition position;

    public Context(int context, ContextType type, ContextPosition position) {
        this.context = context;
        this.type = type;
        this.position = position;
    }

    @Override
    public String toString() {
        if(getType() == ContextType.SHALLOW && getPosition() == ContextPosition.LEFTMOST) {
            return "SHALLOW-LEFT: " + getContext();
        } else if(getType() == ContextType.SHALLOW && getPosition() == ContextPosition.RIGHTMOST) {
            return "SHALLOW-RIGHT: " + getContext();
        }
        return "" + context;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + context;
        result = prime * result + ((position == null) ? 0 : position.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        Context other = (Context) obj;
        if(context != other.context)
            return false;
        if(position != other.position)
            return false;
        if(type != other.type)
            return false;
        return true;
    }

    public ContextType getType() {
        return type;
    }

    public ContextPosition getPosition() {
        return position;
    }

    public int getContext() {
        return context;
    }
}
