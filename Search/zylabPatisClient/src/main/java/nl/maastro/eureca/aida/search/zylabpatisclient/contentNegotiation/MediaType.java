// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.contentNegotiation;

import checkers.nullness.quals.Nullable;
import dataflow.quals.Pure;
import dataflow.quals.SideEffectFree;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;

/**
 * MediaType used in {@link javax.servlet.http.HttpServlet} content negotiation.
 * 
 * Note that {@link nl.maastro.eureca.aida.search.zylabpatisclient.contentNegotiation.MediaType}
 * is similar to {@link org.apache.tika.mime.MediaType} with the extension that
 * wildcard types are supported.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class MediaType {
	/**
	 * Value of {@link #type} and {@link #subtype} of {@link #getAny()} and 
	 * {@link #getAny(java.lang.String)}.
	 */
	public static final String WILDCARD = "*";

	protected final String type;
	protected final String subtype;
	protected final Map<String, String> parameters;
	private static final transient MediaType any =
			new MediaType(WILDCARD, WILDCARD, Collections.<String, String>emptyMap()) {
				@Override
				public boolean isMoreSpecific(MediaType other) {
					return false;
				}

				@Override
				public boolean matches(MediaType other) {
					return true;
				} };
	private static final transient WeakHashMap<String, MediaType> anySubtype =
			new WeakHashMap<>();

	private MediaType(final String type_, final String subtype_, final Map<String, String> parameters_) {
		type = type_;
		subtype = subtype_;
		parameters = parameters_;
	}

	/**
	 * @return 	the wildcard mediatype {@code * / *}
	 */
	public static MediaType getAny() {
		return any;
	}

	/**
	 * @param type_	the {@link #type} part of the media type
	 * @return 	the subtype wildcard {@code type / * }
	 */
	public static MediaType getAny(final String type_) {
		String lc_type = type_.toLowerCase();
		MediaType result = anySubtype.get(lc_type);
		if (result == null) {
			result = new MediaType(lc_type, WILDCARD, Collections.<String, String>emptyMap()) {
				@Override
				public boolean isMoreSpecific(MediaType other) {
					if(other.equals(getAny())) {
						return true;
					}
					return false;
				}

				@Override
				public boolean matches(MediaType other) {
					if(other.equals(getAny())) {
						return true;
					}
					return this.type.equalsIgnoreCase(other.type);
				}
			};
			anySubtype.put(lc_type, result);
		}
		return result;
	}

	/**
	 * Parse a String of form
	 * <code><pre>{type} "/" {subtype} (";" {paramName} "=" {paramValue})*</code></pre>
	 * into a {@link MediaType}.
	 *
	 * @param value		a String formatted as described to parse into a
	 * 		{@code MediaType}.
	 * @return	the parsed {@link MediaType}
	 */
	public static MediaType parse(final String value) throws IllegalArgumentException {
		try {
			String[] typeParams = Separators.PARAMETER.split(value);
			String[] typeSubtype = Separators.TYPE_SUBTYPE.split(typeParams[0]);
			String type = typeSubtype[0];
			String subtype = typeSubtype[1];
			if (subtype.equalsIgnoreCase(WILDCARD)) {
				if (type.equalsIgnoreCase(WILDCARD)) {
					return getAny();
				} else {
					return getAny(type);
				}
			}
			Map<String, String> parameters = new HashMap<>(typeParams.length - 1);
			List<String> l_param = Arrays.asList(typeParams).subList(1, typeParams.length);
			for (String p : l_param) {
				String[] paramValue = Separators.PARAM_VALUE.split(p);
				parameters.put(paramValue[0], paramValue[1]);
			}
			return new MediaType(type, subtype, parameters);
		} catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException(String.format("Cannot parse \"%s\" to a MediaType.", value), ex);
		}
	}

	@Pure
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + Objects.hashCode(this.type);
		hash = 97 * hash + Objects.hashCode(this.subtype);
		hash = 97 * hash + Objects.hashCode(this.parameters);
		return hash;
	}

	@Pure
	@Override
	public boolean equals(@Nullable Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final MediaType other = (MediaType) obj;
		if (!Objects.equals(this.type, other.type)) {
			return false;
		}
		if (!Objects.equals(this.subtype, other.subtype)) {
			return false;
		}
		if (!Objects.equals(this.parameters, other.parameters)) {
			return false;
		}
		return true;
	}

	@Pure
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(type)
				.append(Separators.TYPE_SUBTYPE.getSeparator())
				.append(subtype);
		for (Map.Entry<String, String> param : parameters.entrySet()) {
			result.append(Separators.PARAMETER)
					.append(param.getKey())
					.append(Separators.PARAM_VALUE.getSeparator())
					.append(param.getValue());
		}
		
		return result.toString();
	}

	public boolean matches(MediaType other) {
		if (this == getAny() || other == getAny()) {
			return true;
		} else if (this.type.equalsIgnoreCase(other.type)) {
			if (this == getAny(this.type) || other == getAny(this.type)) {
				return true;
			} else if (this.subtype.equalsIgnoreCase(other.subtype)) {
				return allParametersMatch(other);
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public boolean isMoreSpecific(final MediaType other) {
		if (other.equals(getAny())) {
			return true;
		}
		if(this.type.equalsIgnoreCase(other.type)) {
			if(other.equals(getAny(this.type))) {
				return true;
			}
			if(this.subtype.equalsIgnoreCase(other.subtype)) {
				if (other.parameters.keySet().containsAll(this.parameters.keySet()) &&
						!this.parameters.keySet().containsAll(other.parameters.keySet())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Iterate from this {@link MediaType} to more general variants:
	 * <ol><li>first, omit parameters (if any);</li>
	 * <li>second, replace {@link MediaType#subtype} with 
	 * 		{@link MediaType#getAny(java.lang.String)}; and</li>
	 * <li>finally, replace {@link MediaType#type} with 
	 * 		{@link MediaType#getAny()}.</li></ol>
	 * 
	 * @return	a {@link Iterable} that returns an {@link Iterator} from 
	 * 		{@code this} {@code MediaType} to {@link MediaType#getAny()}.
	 */
	public Iterable<MediaType> generality() {
		return new Iterable<MediaType>() {
			@Override
			public Iterator<MediaType> iterator() {
				return new Iterator<MediaType>() {
					@Nullable MediaType nextItem = MediaType.this;
					
					@Override
					public boolean hasNext() {
						return nextItem != null;
					}

					@Override
					public MediaType next() {
						if(nextItem == null) {
							throw new NoSuchElementException("Iteration at most generic MediaType.");
						}
						
						MediaType tmp = nextItem;
						if(nextItem.equals(getAny())) {
							nextItem = null;
						} else if(nextItem.equals(getAny(nextItem.type))) {
							nextItem = getAny();
						} else if(nextItem.parameters.isEmpty()) {
							nextItem = getAny(nextItem.type);
						} else {
							return new MediaType(
									nextItem.type,
									nextItem.subtype,
									Collections.<String, String>emptyMap());
						}
						
						return tmp;
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException("Not supported.");
					}
				};
			}
		};
	}

	/**
	 * Order {@link MediaType}: more specific {@code MediaType}s come before
	 * more general types.
	 * 
	 * @return	a {@link Comperator} that orders {@link MediaType}s.  The
	 * 		{@code Comperator} sorts the {@code MediaTypes}:
	 * 		<ol><li>first, (lexicographically) by {@link MediaType#type} —with 
	 * 			{@link MediaType#getAny()} as last element—;</li>
	 * 		<li>next, (lexicographically) by {@link MediaType#subtype} —with 
	 * 			{@link MediaType#getAny(java.lang.String)} as last element—; 
	 * 			and</li>
	 * 		<li>finally, by {@link MediaType#parameters} —ordered by inverse 
	 * 			cardinality, lexicographically on parameter name, and 
	 * 			lexicographically on parameter value—.</li>
	 * 		</ol>
	 */
	public static Comparator<MediaType> specificallityOrdening() {
		return new Comparator<MediaType>() {
			static final int O1_LESS_THAN_O2 = -1;
			static final int O1_EQUAL_TO_O2 = 0;
			static final int O1_GREATER_THAN_O2 = +1;
			
			@Override
			public int compare(MediaType o1, MediaType o2) {
				if(Objects.equals(o1, o2)) {
					return O1_EQUAL_TO_O2;
				}
				
				int result = compareTypes(o1, o2);
				if(result == O1_EQUAL_TO_O2) {
					String commonType = o1.type;
					result = compareSubtypes(o1, o2, commonType);
					if(result == O1_EQUAL_TO_O2) {
						result = compareParameters(o1, o2);
						if(result == O1_EQUAL_TO_O2) {
							throw new Error(new IllegalArgumentException(
									"o1 not equal to o2, but both MediaTypes' \'type\', "
									+ "\'subtype\', and \'parameters\' are equal."
									));
						}
					}
				}
				return result;
			}
			
			private int compareTypes(MediaType o1, MediaType o2) {
				if(o1.equals(getAny())) {
					return O1_GREATER_THAN_O2;	// wildcards last in order
				}
				if(o2.equals(getAny())) {
					return O1_LESS_THAN_O2;	// wildcards last in order

				}
				return o1.type.compareTo(o2.type);
			}

			private int compareSubtypes(MediaType o1, MediaType o2, String commonType) {
				MediaType wildcardSubtype = getAny(commonType);
				if(o1.equals(wildcardSubtype)) {
					return O1_GREATER_THAN_O2;	// wildcards last in order
				}
				if(o2.equals(wildcardSubtype)) {
					return O1_LESS_THAN_O2;	// wildcards last in order
				}

				return o1.subtype.compareTo(o2.subtype);
			}

			private int compareParameters(MediaType o1, MediaType o2) {
				int result = o2.parameters.size() - o1.parameters.size();	
							// Order on inverted parameter count, i.e. 
							// (#o1.parameters > #o2.parameters) → O1_LESS_THAN_O2
							// and
							// (#o1.parameters < #o2.parameters) → O1_GREATER_THAN_O2
				
				if (result == O1_EQUAL_TO_O2) {
					TreeMap<String, String> o1paramSorted = new TreeMap<>(o1.parameters);
					TreeMap<String, String> o2paramSorted = new TreeMap<>(o2.parameters);
					Iterator<Map.Entry<String, String>> i1 = o1paramSorted.entrySet().iterator();
					Iterator<Map.Entry<String, String>> i2 = o2paramSorted.entrySet().iterator();

					while(result == O1_EQUAL_TO_O2 && i1.hasNext() && i2.hasNext()) {
						Map.Entry<String, String> e1 = i1.next();
						Map.Entry<String, String> e2 = i2.next();
						
						result = e1.getKey().compareTo(e2.getKey());
						if(result == O1_EQUAL_TO_O2) {
							result = e1.getValue().compareTo(e2.getValue());
						}
					}
				}
				return result;
			}
		};

	}

	public static <TValue> boolean mapContainsMatching(
			Map<MediaType, TValue> map,
			MediaType target) {
		for (MediaType m : target.generality()) {
			if(map.containsKey(m)) {
				return true;
			}
		}
		return false;
	}

	public static <TValue> TValue mapGetMatching(
			Map<MediaType, TValue> map,
			MediaType target) {
		for (MediaType m : target.generality()) {
			if(map.containsKey(m)) {
				return map.get(m);
			}
		}
		throw new NoSuchElementException(String.format(
				"Mediatype %s not found in map %s", target, map));
	}

	protected boolean allParametersMatch(MediaType other) {
		if (other.getClass().isAssignableFrom(this.getClass())) {
			if (!this.parameters.keySet().containsAll(other.parameters.keySet()) && !other.parameters.keySet().containsAll(this.parameters.keySet())) {
				throw new IllegalArgumentException("Parameter key sets do not overlap");
			}
			Set<String> commonParameters = new HashSet<>(this.parameters.keySet());
			commonParameters.retainAll(other.parameters.keySet());
			boolean result = true;
			for (String paramName : commonParameters) {
				result &= paramValueMatches(paramName, other);
			}
			return result;
		} else if (this.getClass().isAssignableFrom(other.getClass())) {
			return other.allParametersMatch(this);
		} else {
			throw new IllegalArgumentException("Both 'this' and 'other' have disjuctive types (both " + "extend MediaType in a different direction), no " + "comperator that can handle them both exists.");
		}
	}

	protected boolean paramValueMatches(final String paramName, MediaType other) {
		if (other.getClass().isAssignableFrom(this.getClass())) {
			@Nullable String thisVal = this.parameters.get(paramName);
			@Nullable String otherVal = this.parameters.get(paramName);
			if (thisVal == null || otherVal == null) {
				return true;
			}
			return thisVal.equals(otherVal);
		} else if (this.getClass().isAssignableFrom(other.getClass())) {
			return other.paramValueMatches(paramName, this);
		} else {
			throw new IllegalArgumentException("Both 'this' and 'other' have disjuctive types (both " + "extend MediaType in a different direction), no " + "comperator that can handle them both exists.");
		}
	}
	
}
