// © Maastro Clinic, 2014
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

/**
 * Interface for converting {@link org.openrdf.model}-objects to string ids
 * that {@link org.apache.velocity.context.Context} can understand.
 *
 * @param <T> type of model object for which this translator can generate IDs.
 * 		{@code <T>} is expected to be one of:
 * 		<ul><li>{@link org.openrdf.model.Statement},</li>
 * 			<li>{@link org.openrdf.model.Value},</li>
 * 			<li>{@link org.openrdf.model.Resource},</li>
 * 			<li>{@link org.openrdf.model.URI},</li>
 * 			<li>{@link org.openrdf.model.BNode}, or</li>
 * 			<li>{@link org.openrdf.model.Literal}.</li></ul>
 * 
 * <p>The generated identifier must be interpreted within the context of
 * a {@link org.openrdf.model.Model}:</p>
 * <ul><li>{@link #getId(java.lang.Object)} is permitted to generate a 
 * 			different identifier for the same resource in a different model;
 * 			and</li>
 * 		<li>the same identifier might {@linkplain #matches(java.lang.Object,
 * 			java.lang.String) match} a different {@link org.openrdf.model.Value}
 * 			in a different {@code Model}.</li></ul>
 * 
 * @author Kasper van den Berg &lt;kasper.vandenberg@maastro.nl&gt; &lt;kasper@kaspervandenberg.net&gt;
 */
interface Translator<T> {
	/**
	 * Check whether {@code id} follows the supported N3/Turtle syntax for
	 * {@code <T>}-objects.
	 * 
	 * <p><em>NOTE: A {@code Translator} is allowed to support a subset of
	 * N3/Turtle syntax.  This implies that {@code isWellFormed(…)} can return 
	 * {@code false} for a string that follows N3 syntax.</em></p> 
	 * 
	 * @param id	String to check 
	 * @return 	Whether {@code id} is follows the syntactic rules for id's 
	 * 		for {@code <T>}:
	 * 		<ul><li>{@code true}: {@code id} adheres to the N3 syntax for 
	 * 			{@code <T>} objects; or</li>
	 * 		<li>{@code false}: {@code id} does not follow the <em>supported</em>
	 * 			syntax for {@code <T>}.</li></ul>
	 */
	public boolean isWellFormed(final String id);

	
	/**
	 * Generate an identifier for {@code val}.
	 * 
	 * <p>The generated identifier is {@link #isWellFormed(java.lang.String) 
	 * wellformed}.</p>
	 * See test:
	 * <ul><li>TranslatorTest#testAnyGeneratedIsWellformed.</li></ul>
	 * 
	 * <p>The generated identifier will be 
	 * {@link Object#equals(java.lang.Object) equal} to any other identifier
	 * this {@code Translator} generates for the same {@code val}, given that
	 * the {@link org.openrdf.model.Model} was not changed.<br> 
	 * ∀(<var>val<sub>1</sub></var>, <var>val<sub>2</sub></var> ∈ {@code T}):
	 * (<var>val<sub>1</sub></var> = <var>val<sub>2</sub></var>) 
	 * ↔ ({@code this.getId(}<var>val<sub>1</sub></var>{@code )}
	 * {@code .equals(this.getId(}<var>val<sub>2</sub></var>{@code ))}</p>
	 * See test:
	 * <ul><li>TranslatorTest#testGeneratedIdIsFunctional; and</li>
	 * 		<li>TranslatorTest#testGeneratedIdIsInjective.</li></ul>
	 * 
	 * @param val	the value from the {@link org.openrdf.model.Model} for which 
	 * 		to generate an identifier.
	 * 
	 * @return 	an identifier for {@code val}
	 */
	public String getId(final T val);


	/**
	 * Check whether {@code id} is an identifier for {@code val}.
	 * 
	 * <p>While {@link #getId} will generate a single unique identifier
	 * for {@code val}, it is possible that multiple identifiers match
	 * the value.</p>
	 * Mathematically:
	 * <ul><li>
	 * ∀(<var>val</var> ∈ {@code <T>}): {@code this.matches(}<var>val</var>,
	 * {@code this.getId(}<var>val</var>{@code ))}</li>
	 * <li>∀(<var>val<sub>1</sub></var>, <var>val<sub>2</sub></var> ∈ {@code <T>},
	 * <var>id<sub>1</sub></var> = 
	 * {@code this.getId(}<var>val<sub>1</sub></var>{@code )}): 
	 * {@code this.matches(}<var>val<sub>2</sub></var>, 
	 * <var>id<sub>1</sub></var>{@code )} ⊕ (<var>val<sub>1</sub></var> ≠
	 * <var>val<sub>2</sub></var>)</li></ul>
	 * See tests:
	 * <ul><li>TranslatorTest#testGeneratedMatches; and</li>
	 * 		<li>TranslatorTest#testMatchingAnIdEquivalentEqual.</li></ul>
	 * 
	 * @param val	the	{@link org.openrdf.model.Value} or 
	 * 		{@link org.openrdf.model.Statement} to check
	 * @param id	the identifier to check
	 * 
	 * @return <ul><li>{@code true}: {@code id} is an identifier for
	 * 			{@code value}; or</li>
	 * 		<li>{@code false}: {@code id} does not match {@code value}.</li>
	 * 		</ul>	
	 */
	public boolean matches(final T val, final String id);
	
}

/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

