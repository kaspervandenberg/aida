/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.util.Objects;

/**
 *
 * @author kasper
 */
public class Snippet {
	public final String value;

	public Snippet(String value_) {
		this.value = value_;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 89 * hash + Objects.hashCode(this.value);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Snippet other = (Snippet) obj;
		if (!Objects.equals(this.value, other.value)) {
			return false;
		}
		return true;
	}
	
}
