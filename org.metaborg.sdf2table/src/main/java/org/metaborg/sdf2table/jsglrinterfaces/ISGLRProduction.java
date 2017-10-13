package org.metaborg.sdf2table.jsglrinterfaces;

public interface ISGLRProduction {

    int productionNumber();
    
    String sort();
    
    String constructor();
    
    String descriptor();
    
    boolean isContextFree();
    
    boolean isLayout();
    
    boolean isLiteral();
    
    boolean isLexical();
    
    boolean isLexicalRhs(); // Whether the right hand side only contains character classes
    
    boolean isList();
    
    boolean isOptional();
    
    boolean isCompletionOrRecovery();
    
    // The methods below are for tokenization / syntax highlighting
    boolean isStringLiteral();

    boolean isNumberLiteral();
    
    boolean isOperator();
    
}
