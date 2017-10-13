package org.spoofax.jsglr2.parseforest.datadependent;

import org.spoofax.jsglr2.characters.Characters;
import org.spoofax.jsglr2.parser.Parse;
import org.spoofax.jsglr2.parser.Position;

public class DataDependentTermNode extends DataDependentParseForest {

	public final int character;
	
	public DataDependentTermNode(int nodeNumber, Parse parse, Position position, int character) {
		super(nodeNumber, parse, position, Characters.isNewLine(character) ? position.nextLine() : position.nextColumn());
		this.character = character;
	}
	
	public String descriptor() {
		return "'" + Characters.charToString(this.character) + "'";
	}
	
}
