package org.spoofax.jsglr2.parseforest;

import org.metaborg.sdf2table.jsglrinterfaces.ISGLRProduction;

public interface IDerivation<ParseForest> {

    ISGLRProduction production();
    
    ParseForest[] parseForests();
    
}
