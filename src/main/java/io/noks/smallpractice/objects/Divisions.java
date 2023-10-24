package io.noks.smallpractice.objects;

import io.noks.smallpractice.enums.DivisionsEnum;
import io.noks.smallpractice.enums.DivisionsTiers;

public class Divisions {
	private DivisionsEnum division;
	private DivisionsTiers tier;
	
	public Divisions(final int elo) {
		
	}
	
	public boolean hasChangedDivision(final int newElo) {
		return false; // TODO;
	}
	
	public void updateDivision(final int elo) {
		
	}
}
