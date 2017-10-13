package org.spoofax.jsglr2.parseforest.hybrid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.metaborg.sdf2table.jsglrinterfaces.ISGLRProduction;
import org.spoofax.jsglr2.parser.Parse;
import org.spoofax.jsglr2.parser.Position;
import org.spoofax.jsglr2.util.iterators.SingleElementIterable;
import org.spoofax.jsglr2.util.iterators.SingleElementWithListIterable;

public class ParseNode extends HParseForest {

	public final ISGLRProduction production;
    private final Derivation firstDerivation;
    private List<Derivation> otherDerivations;
	
	public ParseNode(int nodeNumber, Parse parse, Position startPosition, Position endPosition, ISGLRProduction production, Derivation firstDerivation) {
		super(nodeNumber, parse, startPosition, endPosition);
        this.production = production;
        this.firstDerivation = firstDerivation;
        this.otherDerivations = null;
	}
	
	public void addDerivation(Derivation derivation) {
	    if (otherDerivations == null)
	        otherDerivations = new ArrayList<Derivation>();
	    
	    otherDerivations.add(derivation);
	}
	
	public Iterable<Derivation> getDerivations() {
	    if (otherDerivations == null)
	        return new SingleElementIterable<Derivation>(firstDerivation); // Michael: JDK contains Collections/Arrays.something for single element iterable
	    else
	        return new SingleElementWithListIterable<Derivation>(firstDerivation, otherDerivations);
	}
    
    public List<Derivation> getPreferredAvoidedDerivations() {
        if (!isAmbiguous())
            return Arrays.asList(firstDerivation);
        else {
            List<Derivation> preferred = null, avoided = null, other = null;
            
            for (Derivation derivation : getDerivations()) {
                switch (derivation.productionType) {
                    case PREFER:
                        if (preferred == null)
                            preferred = new ArrayList<Derivation>();
                        
                        preferred.add(derivation);
                        break;
                    case AVOID:
                        if (avoided == null)
                            avoided = new ArrayList<Derivation>();
                        
                        avoided.add(derivation);
                        break;
                    default:
                        if (other == null)
                            other = new ArrayList<Derivation>();
                        
                        other.add(derivation);
                }
            }
            
            if (preferred != null && !preferred.isEmpty())
                return preferred;
            else if (other != null && !other.isEmpty())
                return other;
            else
                return avoided;
        }
    }
	
	public Derivation getOnlyDerivation() {
		return firstDerivation;
	}
	
	public boolean isAmbiguous() {
		return otherDerivations != null;
	}
	
	public String descriptor() {
		return production.descriptor();
	}
	
}
