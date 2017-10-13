package org.spoofax.jsglr2.parsetable;

import org.metaborg.sdf2table.jsglrinterfaces.ISGLRProduction;
import org.metaborg.sdf2table.parsetable.ProductionType;

public class Production implements ISGLRProduction {

	private final int productionNumber;
	private final String sort;
	private final boolean isContextFree;
	private final boolean isLayout;
	private final boolean isLiteral;
	private final boolean isLexical;
	private final boolean isLexicalRhs;
	private final boolean isList;
	private final boolean isOptional;
	private final boolean isStringLiteral;
	private final boolean isNumberLiteral;
	private final boolean isOperator;
	private final ProductionAttributes attributes;
	
	public Production(int productionNumber, String sort, Boolean isContextFree, Boolean isLayout, Boolean isLiteral, Boolean isLexical, Boolean isLexicalRhs, Boolean isList, Boolean isOptional, Boolean isStringLiteral, Boolean isNumberLiteral, Boolean isOperator, ProductionAttributes attributes) {
		this.productionNumber = productionNumber;
		this.sort = sort;
		this.isContextFree = isContextFree;
		this.isLayout = isLayout;
		this.isLiteral = isLiteral;
		this.isLexical = isLexical;
		this.isLexicalRhs = isLexicalRhs;
		this.isList = isList;
		this.isOptional = isOptional;
		this.isStringLiteral = isStringLiteral;
		this.isNumberLiteral = isNumberLiteral;
		this.isOperator = isOperator;
		this.attributes = attributes;
	}
	
	public int productionNumber() {
	    return productionNumber;
	}
	
	public static ProductionType typeFromInt(int productionType) {
		switch (productionType) {
			case 1:		return ProductionType.REJECT;
			case 2:		return ProductionType.PREFER;
			case 3:		return ProductionType.BRACKET;
			case 4:		return ProductionType.AVOID;
			case 5:		return ProductionType.LEFT_ASSOCIATIVE;
			case 6:		return ProductionType.RIGHT_ASSOCIATIVE;
			default:		return ProductionType.NO_TYPE; 
		}
	}
    
    public String sort() {
        return sort;
    }
    
    public String constructor() {
        return attributes.constructor;
    }
    
    public String descriptor() {
        return "";
    }
    
    public boolean isContextFree() {
        return isContextFree;
    }
    
    public boolean isLayout() {
        return isLayout;
    }
    
    public boolean isLiteral() {
        return isLiteral;
    }
    
    public boolean isLexical() {
        return isLexical;
    }
    
    public boolean isLexicalRhs() {
        return isLexicalRhs;
    }
    
    public boolean isList() {
        return isList;
    }
    
    public boolean isOptional() {
        return isOptional;
    }
	
    public boolean isStringLiteral() {
		return isStringLiteral;
	}
	
    public boolean isNumberLiteral() {
		return isNumberLiteral;
	}
	
    public boolean isOperator() {
		return isOperator;
	}
    
    public boolean isCompletionOrRecovery() {
        return attributes.isCompletionOrRecovery();
    }

    @Override public String toString() {
        return productionNumber + " " + sort;
    }
    
    
	
}
