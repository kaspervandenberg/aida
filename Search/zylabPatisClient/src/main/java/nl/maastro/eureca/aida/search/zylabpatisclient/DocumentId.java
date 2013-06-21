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
public class DocumentId {
	public final String value;

	public DocumentId(String value_) {
		value = value_;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 61 * hash + Objects.hashCode(this.value);
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
		final DocumentId other = (DocumentId) obj;
		if (!Objects.equals(this.value, other.value)) {
			return false;
		}
		return true;
	}
	
}
