// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer.jmx;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describe a MBean, -operation, or -property to an application administrator.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
@Retention (RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Target({
	ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD,
	ElementType.FIELD, ElementType.PARAMETER})
public @interface Description {
	String value();
}
