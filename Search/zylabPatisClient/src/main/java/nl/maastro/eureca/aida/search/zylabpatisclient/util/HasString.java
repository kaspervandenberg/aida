// © Maastro Clinic
package nl.maastro.eureca.aida.search.zylabpatisclient.util;

import java.util.Objects;

/**
 * Thin wrapper arround String to create 'strings' of a specific type; e.g 
 * {@link nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber},
 * {@link nl.maastro.eureca.aida.search.zylabpatisclient.DocumentId}, and
 * {@link nl.maastro.eureca.aida.search.zylabpatisclient.Snippet}.
 * 
 * Some objects are nothing more than strings, but should be of different 
 * classes; using a {@link nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber}
 * where a {@link nl.maastro.eureca.aida.search.zylabpatisclient.Snippet} is
 * expected is wrong.  Deriving {@code PatisNumber} and {@code Snippet} from
 * {@link java.lang.String} would be a reasonnable (definitely not good) design.
 * However Java's {@code String} is final (for good reasons), therefore deriving
 * classes from it is impossible.  These classes can derive from {@code HasString}.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public abstract class HasString implements Comparable<HasString> {
	protected final String value;

	protected HasString(final String value_) {
		this.value = value_;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 41 * hash + Objects.hashCode(this.value);
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
		final HasString other = (HasString) obj;
		if (!Objects.equals(this.value, other.value)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return value; 
	}

	@Override
	public int compareTo(HasString o) {
		if(o == null) {
			throw new NullPointerException("Cannot compare to null");
		}
		if(Objects.equals(this, o)) {
			return 0;
		}
		if(this.getClass() != o.getClass()) {
			return this.getClass().getCanonicalName().compareTo(
					o.getClass().getCanonicalName());
		}
		if(this.value == null || o.value == null) {
			throw new NullPointerException("Cannot compare HasString-objects that have null as value");
		}
		return this.value.compareTo(o.value);
	}

	public String getValue() {
		return value;
	}
}
