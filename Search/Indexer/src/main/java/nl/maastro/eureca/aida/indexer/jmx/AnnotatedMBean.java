// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer.jmx;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

/**
 * 
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 *
 */
public class AnnotatedMBean extends StandardMBean {
	private static class ClassHierarchy<T> {
		public interface Predicate<T> {
			boolean eval(Class<? super T> item);
		}
		
		private final Class<T> derived_class;
		private final LinkedList<Class<? super T>> search_order;

		public ClassHierarchy(Class<T> derived_class_) {
			this.derived_class = derived_class_;
			this.search_order = init_search_order(derived_class_);
		}

		public boolean contains (Class<?> element)
		{
			if (element.isAssignableFrom(derived_class)) {
				return search_order.contains(
						cast_to_super_of_derived(element));
			} else {
				return false;
			}
		}

		public Class<? super T> cast_to_super_of_derived(Class<?> class_obj) {
			if (class_obj.isAssignableFrom(derived_class)) {
				@SuppressWarnings("unchecked")
				Class<? super T> result = (Class<? super T>)class_obj;
				return result;
			} else {
				throw new ClassCastException(
						class_obj.getName() + " is not a superclass of " +
						derived_class.getName());
			}
		}

		public Class<? super T> find_if (Predicate<T> predicate) {
			for (Class<? super T> cl : search_order) {
				if (predicate.eval(cl)) {
					return cl;
				}
			}
			return null;
		}

		public LinkedList<Class<? super T>> filter (Predicate<T> predicate)
		{
			LinkedList<Class<? super T>> result = new LinkedList<>();
			for (Class<? super T> cl : search_order) {
				if (predicate.eval(cl)) {
					result.add(cl);
				}
			}
			return result;
		}

		public void move_to (Class<? super T> class_to_move, int position)
		{
			if (search_order.remove(class_to_move)) {
				search_order.listIterator(position).add(class_to_move);
			}
		}

		public Comparator<Class<? super T>> search_order_comparison()
		{
			return new Comparator<Class<? super T>>() {
				@Override
				public int compare(Class<? super T> o1, Class<? super T> o2) {
					return search_order.indexOf(o1) - search_order.indexOf(o2);
				}
			};
		}
		
		private static <T> LinkedList<Class<? super T>> init_search_order(
				Class<T> derivedClass)
		{
			LinkedList<Class<? super T>> result = new LinkedList<>();
			result.add(derivedClass);
			result.addAll(get_transitive_interfaces(derivedClass));
			result.addAll(get_transitive_super_classes(derivedClass));
			
			return result;
		}
		
		private static <T> List<Class<? super T>> get_transitive_super_classes(
				Class<T> derived_class)
		{
			return get_transitive_super_classes(new LinkedList<Class<? super T>>(), derived_class);
		}

		private static <T> List<Class<? super T>> get_transitive_super_classes(
				List<Class<? super T>> target,
				Class<?> derived_class)
		{
			Class<?> parent_obj = derived_class.getSuperclass();
			@SuppressWarnings("unchecked")
			Class<? super T> parent = (Class<? super T>)parent_obj;
			
			if (parent != null) {
				target.add((Class<? super T>)parent);
				return get_transitive_super_classes(target, parent);
			} else {
				return target;
			}
		}

		private static <T> LinkedHashSet<Class<? super T>> get_transitive_interfaces(
				Class<T> derived_class)
		{
			LinkedHashSet<Class<? super T>> result = new LinkedHashSet<>();
			Deque<Class<?>> to_check = new LinkedList<>();
			to_check.addAll(Arrays.asList(derived_class.getInterfaces()));

			while (!to_check.isEmpty()) {
				Class<?> item_obj = to_check.remove();
				@SuppressWarnings("unchecked")
				Class<? super T> item = (Class<? super T>)item_obj;

				if (!result.contains(item)) {
					result.add(item);
					to_check.addAll(Arrays.asList(item.getInterfaces()));
				}
			}

			return result;
		}
	}

	private static class MethodByName
	{
		private final Map<String, Set<Method>> methods;

		public <T> Comparator<Method> in_hierarchy_comparison(
				final Class<T> derivedClass,
				final ClassHierarchy<T> hierarchy)
		{
			return new Comparator<Method>() {
				private final Comparator<Class<? super T>> class_comparator =
						hierarchy.search_order_comparison();
				@Override
				public int compare(Method o1, Method o2) {
					Class<?> o1_declaringClass_obj = o1.getDeclaringClass();
					Class<?> o2_declaringClass_obj = o2.getDeclaringClass();
					
					if (is_comparable(o1_declaringClass_obj, o2_declaringClass_obj)) {
						Class<? super T> o1_declaringClass = cast_to_super_of_T(o1_declaringClass_obj);
						Class<? super T> o2_declaringClass = cast_to_super_of_T(o2_declaringClass_obj);
						return compare(o1_declaringClass, o2_declaringClass);
					} else {
						return fallback_compare(o1, o2);
					}
				}

				private Class<? super T> cast_to_super_of_T(Class<?> cl)
				{
					if (cl.isAssignableFrom(derivedClass)) {
						@SuppressWarnings("unchecked")
						Class<? super T> result = (Class<? super T>)cl;
						return result;
					} else {
						throw new ClassCastException(
								cl.getName() +
								"is not a super class of " +
								derivedClass.getName());
					}
				}
				
				private boolean is_comparable(Class<?> cl1, Class<?> cl2)
				{
					return hierarchy.contains(cl1) && hierarchy.contains(cl2);
				}
				
				private int compare(Class<? super T> cl1, Class<? super T> cl2)
				{
					return class_comparator.compare(cl1, cl2);
				}
				
				private int fallback_compare (Method m1, Method m2) {
					int hashDiff = m1.hashCode() - m2.hashCode();
					if (hashDiff != 0 || m1.equals(m2)) {
						return hashDiff;
					} else {
						throw new Error("Cannot compare " + m1 + " and " + m2);
					}
				}
			};
		}

		private void add_all_public_methods (Collection<Class<?>> col)
		{
			for (Class<?> c : col) {
				add_all_public_methods(c);
			}
		}
		
		private void add_all_public_methods (Class<?> from)
		{
			for (Method m : from.getMethods()) {
				add_entry(m);
			}
		}

		private void add_entry (Method to_add)
		{
			String method_name = to_add.getName();

			Set<Method> target;
			if (!methods.containsKey(method_name)) {
				target = new LinkedHashSet<>();
				methods.put(method_name, target);
			} else {
				target = methods.get(method_name);
			}
			target.add(to_add);
		}
	}


	private Map<String, List<Method>> methods_by_name = null; // Lazy initialisation
	
	
	public AnnotatedMBean(Class<?> mbeanInterface) throws NotCompliantMBeanException {
		super(mbeanInterface);
	}

	public AnnotatedMBean(Class<?> mbeanInterface, boolean isMXBean) {
		super(mbeanInterface, isMXBean);
	}

	public <T extends Object> AnnotatedMBean(T implementation, Class<T> mbeanInterface) throws NotCompliantMBeanException {
		super(implementation, mbeanInterface);
	}

	public <T extends Object> AnnotatedMBean(T implementation, Class<T> mbeanInterface, boolean isMXBean) {
		super(implementation, mbeanInterface, isMXBean);
	}

	@Override
	protected String getDescription(MBeanInfo info) {
		return super.getDescription(info); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	protected String getDescription(MBeanAttributeInfo info) {
		return super.getDescription(info); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	protected String getDescription(MBeanConstructorInfo info) {
		return super.getDescription(info); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	protected String getDescription(MBeanConstructorInfo ctor, MBeanParameterInfo param, int sequence) {
		return super.getDescription(ctor, param, sequence); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	protected String getDescription(MBeanOperationInfo info) {
		return super.getDescription(info); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	protected String getDescription(MBeanOperationInfo op, MBeanParameterInfo param, int sequence) {
		return super.getDescription(op, param, sequence); //To change body of generated methods, choose Tools | Templates.
	}

	private <TAnnotation extends Annotation> TAnnotation find_class_annotation(
			Class<TAnnotation> annotationType)
	{
		for (Class<?> c : get_search_order()) {
			if (c.isAnnotationPresent(annotationType)) {
				return c.getAnnotation(annotationType);
			}
		}
		
		return null;
	}

	private Annotation find_class_annotation(
			Set<Class<? extends Annotation>> annotation_types_to_look_for)
	{
		for (Class<?> c : get_search_order()) {
			for (Class<? extends Annotation> at : annotation_types_to_look_for) {
				if (c.isAnnotationPresent(at)) {
					return c.getAnnotation(at);
				}
			}
		}

		return null;
	}

	private <TAnnotation extends Annotation> TAnnotation find_property_annotation(
			Class<TAnnotation> annotationType, String name)
	{
		Class<?> c;
		c.get
	}
	
	private List<Class<?>> get_search_order() {
		if (searched_classes == null) {
			searched_classes = init_search_order();
		}
		return Collections.unmodifiableList(searched_classes);
	}
	
	@SuppressWarnings("serial")
	private List<Class<?>> init_search_order() {
		return new ArrayList<Class<?>>() {{
			Class<?> implementationClass = AnnotatedMBean.this.getImplementationClass();
			add (implementationClass);
			add	(AnnotatedMBean.this.getMBeanInterface());
			addAll(get_transitive_super_classes(implementationClass));
			addAll(get_transitive_interfaces(implementationClass));
		}};
	}	


	private static 
}
