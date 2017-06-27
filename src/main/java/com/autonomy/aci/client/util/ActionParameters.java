package com.autonomy.aci.client.util;

import com.autonomy.aci.client.services.AciConstants;
import com.autonomy.aci.client.transport.AciParameter;
import com.autonomy.aci.client.transport.ActionParameter;
import com.autonomy.aci.client.transport.InputStreamActionParameter;
import org.apache.commons.lang.Validate;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Utility class to make it easier to specify sets of {@link ActionParameter} objects for sending with ACI actions.
 *
 * For example:
 * <pre>
 *    ActionParameters parameters = new ActionParameters();
 *    parameters.add(&quot;action&quot;, &quot;query&quot;);
 *    parameters.add(&quot;text&quot;, &quot;XPath&quot;);
 *    parameters.add(&quot;combine&quot;, &quot;simple&quot;);
 *    ...
 * </pre>
 * or:
 * <pre>
 *    ActionParameters parameters = new ActionParameters(
 *            new AciParameter(&quot;action&quot;, &quot;query&quot;),
 *            new AciParameter(&quot;text&quot;, &quot;XPath&quot;),
 *            new AciParameter(&quot;combine&quot;, &quot;simple&quot;)
 *    );
 *    ...
 * </pre>
 * For those actions that don't generally take parameters, like <tt>GetStatus</tt> and <tt>GetVersion</tt>, you can do
 * the following:
 * <pre>
 *    ...
 *    aciService.executeAction(new ActionParameters(&quot;GetStatus&quot;), myProcessor);
 *    ...
 * </pre>
 */
public class ActionParameters implements Set<ActionParameter<?>> {

    private final Set<ActionParameter<?>> parameters = new LinkedHashSet<>();

    /**
     * Default empty constructor...
     */
    public ActionParameters() {}

    /**
     * Convenience constructor for those actions that don't normally take any parameters like <tt>GetStatus</tt> or
     * <tt>GetVersion</tt>. Essentially calls {@link #ActionParameters(ActionParameter[])}
     * after creating a new <tt>ActionParameters</tt> - <tt>new AciParameter(AciConstants.PARAM_ACTION, action)</tt>
     * @param action The value of the action parameter to create this instance with
     */
    public ActionParameters(final String action) {
        parameters.add(new AciParameter(AciConstants.PARAM_ACTION, action));
    }

    /**
     * Creates a new instance using the supplied collection of <tt>ActionParameter</tt>s...
     * @param parameters A <tt>Collection</tt> of <tt>ActionParameter</tt>s to create this instance with
     */
    public ActionParameters(final Collection<? extends ActionParameter<?>> parameters) {
        // Add all the parameters....
        addAll(parameters);
    }

    /**
     * Creates a new instance using the supplied <tt>ActionParameter</tt>s. This constructor is a shorthand way and
     * equivalent of doing:
     * <pre>
     *     new ActionParameters(Arrays.asList(
     *           new AciParameter("Action", "Query"),
     *           new AciParameter("Test", "this is some query text"),
     *           ...
     *     ));
     * </pre>
     * @param parameters A <tt>Collection</tt> of <tt>AciParameter</tt>s to create this instance with
     */
    public ActionParameters(final ActionParameter<?>... parameters) {
        // Add all the parameters....
        addAll(Arrays.asList(parameters));
    }

    /**
     * This is a convenience method to convert a collection of parameters into an <tt>ActionParameters</tt> object. The
     * returned object may or may not be backed by the collection provided.
     * @param parameters The collection of parameters to convert
     * @return An <tt>ActionParameters</tt> object containing all elements of <tt>parameters</tt>
     */
    public static ActionParameters convert(final Collection<? extends ActionParameter<?>> parameters) {
        return (parameters instanceof ActionParameters)
                ? (ActionParameters) parameters
                : new ActionParameters(parameters);
    }

    /**
     * Returns the value for the parameter specified by <tt>name</tt>. Returns <tt>null</tt> if no parameter exists with
     * <tt>name</tt> in the set. However, a return value of <tt>null</tt> doesn't <i>necessarily</i> indicate that no
     * parameter exists as <tt>null</tt> is an allowed parameter value. The <tt>contains</tt> operation may be used to
     * distinguish these two cases.
     * @param name The name of the parameter to get the value of
     * @return The value of the parameter. If the returned value is <tt>null</tt> it is not necessarily an indication
     * that the parameter doesn't exist as <tt>null</tt> is an allowed parameter value.
     */
    public Object get(final String name) {
        Object value = null;

        for (final ActionParameter<?> parameter : this) {
            if (parameter.getName().equalsIgnoreCase(name)) {
                value = parameter.getValue();
                break;
            }
        }

        return value;
    }

    /**
     * Associates the specified value with the parameter specified by <tt>name</tt>. If no parameter exists, then a new
     * one is created. If a parameter exists, its old value is replaced by the specified value.
     * @param name  The name of the parameter
     * @param value The value of the parameter
     * @return Previous parameter value or <tt>null</tt> if the parameter didn't exist. A <tt>null</tt> can also
     * indicate that the previous parameter value was <tt>null</tt> as <tt>null</tt> is an allowed parameter
     * value.
     * @throws IllegalArgumentException if <tt>name</tt> is <tt>null</tt> or blank.
     */
    public Object put(final String name, final Object value) {
        final Object oldValue = get(name);
        remove(name);
        add(name, value);
        return oldValue;
    }

    /**
     * Puts the specified <tt>parameter</tt> into this collection. If a parameter with the same name already exists in
     * this collection, it is replaced by the specified <tt>parameter</tt>.
     * @param parameter The parameter to put into this collection
     * @return Previous parameter or <tt>null</tt> if the parameter didn't exist
     * @throws IllegalArgumentException If <tt>parameter</tt> is <tt>null</tt>
     */
    public ActionParameter<?> put(final ActionParameter<?> parameter) {
        Validate.notNull(parameter, "The parameter must not be null.");

        // Find the existing parameter...
        ActionParameter<?> oldParameter = null;
        for (final ActionParameter<?> param : parameters) {
            if (param.equals(parameter)) {
                oldParameter = param;
                break;
            }
        }

        // Remove the old parameter if there was one...
        if (oldParameter != null) {
            parameters.remove(oldParameter);
        }

        // Put in the new parameter...
        parameters.add(parameter);

        // Return what ever the old parameter was...
        return oldParameter;
    }

    /**
     * Puts the collection of <tt>parameters</tt> into this collection. If any of the specified parameters already
     * exists in this collection, they are replaced. Does nothing if <tt>parameters</tt> is <tt>null</tt>.
     * @param parameters The parameters to put into this collection
     * @throws IllegalArgumentException if any of the <tt>parameters</tt> are <tt>null</tt>
     */
    public void putAll(final Collection<? extends ActionParameter<?>> parameters) {
        if (parameters != null) {
            parameters.forEach(this::put);
        }
    }

    /**
     * Puts the array of <tt>parameters</tt> into this collection. If any of the specified parameters already exists in
     * this collection, they are replaced. Does nothing if <tt>parameters</tt> is <tt>null</tt>.
     * @param parameters The parameters to put into this collection
     * @throws IllegalArgumentException If any of the <tt>parameters</tt> are <tt>null</tt>.
     */
    public void putAll(final ActionParameter<?>... parameters) {
        if (parameters != null) {
            for (final ActionParameter<?> parameter : parameters) {
                put(parameter);
            }
        }
    }

    /**
     * Adds a new parameter to the current set if one of the same names doesn't already exist.
     * @param name  The name of the parameter
     * @param value The value of the parameter
     * @return <tt>true</tt> if the set did not already contain the specified element.
     * @throws java.lang.IllegalArgumentException if <tt>name</tt> is <tt>null</tt> or blank.
     */
    public final boolean add(final String name, final Object value) {
        return value instanceof InputStream ? add(new InputStreamActionParameter(name, (InputStream) value)) : add(new AciParameter(name, value));
    }

    /**
     * <p>Adds the specified element to this set if it is not already present.  More formally, adds the specified
     * element <tt>e</tt> to this set if the set contains no element <tt>e2</tt> such that
     * <tt>(e==null&nbsp;?&nbsp;e2==null&nbsp;:&nbsp;e.equals(e2))</tt>. If this set already contains the element, the
     * call leaves the set unchanged and returns <tt>false</tt>.  In combination with the restriction on constructors,
     * this ensures that sets never contain duplicate elements.
     * @param parameter Element to be added to this set
     * @return <tt>true</tt> if this set did not already contain the specified element
     * @throws UnsupportedOperationException If the <tt>add</tt> operation is not supported by this set
     * @throws NullPointerException          If the specified element is null and this set does not permit null elements
     * @throws IllegalArgumentException      If some property of the specified element prevents it from being added to
     *                                       this set
     */
    @Override
    public boolean add(final ActionParameter<?> parameter) {
        return parameters.add(parameter);
    }

    /**
     * Removes the specified element from this set if it is present.  More formally, removes an element <tt>e</tt> such
     * that <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>, if this set contains such an element.
     * Returns <tt>true</tt> if this set contained the element (or equivalently, if this set changed as a result of the
     * call). (This set will not contain the element once the call returns.)
     * @param o Object to be removed from this set, if present
     * @return <tt>true</tt> if this set contained the specified element
     * @throws NullPointerException If the specified element is null and this set does not permit null elements
     */
    @Override
    public boolean remove(final Object o) {
        return parameters.remove(o);
    }

    /**
     * Removes a parameter from the set.
     * @param name The name of the parameter to remove
     * @return <tt>true</tt> if the set contained the specified element.
     */
    public boolean remove(final String name) {
        return remove(new AciParameter(name, ""));
    }

    /**
     * Returns <tt>true</tt> if this set contains all of the elements of the specified collection.  If the specified
     * collection is also a set, this method returns <tt>true</tt> if it is a <i>subset</i> of this set.
     * @param collection collection to be checked for containment in this set
     * @return <tt>true</tt> if this set contains all of the elements of the specified collection
     * @throws NullPointerException if the specified collection contains one or more null elements and this set does not
     *                              permit null elements (optional), or if the specified collection is null
     * @see #contains(Object)
     */
    @Override
    public boolean containsAll(final Collection<?> collection) {
        return parameters.containsAll(collection);
    }

    /**
     * Adds all of the elements in the specified collection to this set if they're not already present.  If the
     * specified collection is also a set, the <tt>addAll</tt> operation effectively modifies this set so that its value
     * is the <i>union</i> of the two sets.  The behavior of this operation is undefined if the specified collection is
     * modified while the operation is in progress.
     * @param collection collection containing elements to be added to this set
     * @return <tt>true</tt> If this set changed as a result of the call
     * @throws ClassCastException       If the class of an element of the specified collection prevents it from being added to
     *                                  this set
     * @throws NullPointerException     If the specified collection contains one or more null elements and this set does not
     *                                  permit null elements, or if the specified collection is null
     * @throws IllegalArgumentException If some property of an element of the specified collection prevents it from
     *                                  being added to this set
     * @see #add(Object)
     */
    @Override
    public boolean addAll(final Collection<? extends ActionParameter<?>> collection) {
        return parameters.addAll(collection);
    }

    /**
     * Retains only the elements in this set that are contained in the specified collection.  In other words, removes
     * from this set all of its elements that are not contained in the specified collection.  If the specified
     * collection is also a set, this operation effectively modifies this set so that its value is the
     * <i>intersection</i> of the two sets.
     * @param collection collection containing elements to be retained in this set
     * @return <tt>true</tt> If this set changed as a result of the call
     * @throws ClassCastException   if the class of an element of this set is incompatible with the specified collection
     * @throws NullPointerException if this set contains a null element and the specified collection does not permit
     *                              null elements (optional), or if the specified collection is null
     * @see #remove(Object)
     */
    @Override
    public boolean retainAll(final Collection<?> collection) {
        return parameters.retainAll(collection);
    }

    /**
     * Removes from this set all of its elements that are contained in the specified collection.  If the specified
     * collection is also a set, this operation effectively modifies this set so that its value is the
     * <i>asymmetric set difference</i> of the two sets.
     * @param collection collection containing elements to be removed from this set
     * @return <tt>true</tt> If this set changed as a result of the call
     * @throws ClassCastException   If the class of an element of this set is incompatible with the specified collection
     * @throws NullPointerException If this set contains a null element and the specified collection does not permit
     *                              null elements (optional), or if the specified collection is null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    @Override
    public boolean removeAll(final Collection<?> collection) {
        return parameters.removeAll(collection);
    }

    /**
     * Removes all of the elements from this set. The set will be empty after this call returns.
     */
    @Override
    public void clear() {
        parameters.clear();
    }

    /**
     * Returns the number of elements in this set (its cardinality).  If this set contains more than
     * <tt>Integer.MAX_VALUE</tt> elements, returns <tt>Integer.MAX_VALUE</tt>.
     * @return The number of elements in this set (its cardinality)
     */
    @Override
    public int size() {
        return parameters.size();
    }

    /**
     * Returns <tt>true</tt> if this set contains no elements.
     * @return <tt>true</tt> if this set contains no elements
     */
    @Override
    public boolean isEmpty() {
        return parameters.isEmpty();
    }

    /**
     * Returns <tt>true</tt> if this set contains the specified element.
     * @param o whose presence in this set is to be tested
     * @return <tt>true</tt> if this set contains the specified element
     */
    @Override
    public boolean contains(final Object o) {
        return parameters.contains(o);
    }

    /**
     * Returns <tt>true</tt> if this set contains the specified element.
     * @param name The name of the parameter to check for
     * @return <tt>true</tt> if this set contains the specified element
     */
    public boolean contains(final String name) {
        return parameters.contains(new AciParameter(name, ""));
    }

    /**
     * Returns an iterator over the <tt>ActionParameter</tt>s in this set.
     * @return an iterator over the elements in this set
     */
    @Override
    public Iterator<ActionParameter<?>> iterator() {
        return parameters.iterator();
    }

    /**
     * <p>Returns an array containing all of the elements in this set in iteration order.
     * <p>The returned array will be "safe" in that no references to it are maintained by this set.  (In other words,
     * this method must allocate a new array even if this set is backed by an array). The caller is thus free to modify
     * the returned array.
     * <p>This method acts as bridge between array-based and collection-based APIs.
     * @return an array containing all the elements in this set
     */
    @Override
    public ActionParameter<?>[] toArray() {
        return parameters.toArray(new ActionParameter<?>[parameters.size()]);
    }

    /**
     * <p>Returns an array containing all of the elements in this set in iteration order; the runtime type of the
     * returned array is that of the specified array. If the set fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the specified array and the size of this set.
     * <p>If this set fits in the specified array with room to spare (i.e., the array has more elements than this set),
     * the element in the array immediately following the end of the set is set to <tt>null</tt>.  (This is useful in
     * determining the length of this set <i>only</i> if the caller knows that this set does not contain any null
     * elements.)
     * <p>Like the {@link #toArray()} method, this method acts as bridge between array-based and collection-based APIs.
     * Further, this method allows precise control over the runtime type of the output array, and may, under certain
     * circumstances, be used to save allocation costs.
     * <p>Suppose <tt>x</tt> is a set known to contain only strings. The following code can be used to dump the set into
     * a newly allocated array of <tt>String</tt>:
     * <pre>
     *     String[] y = x.toArray(new String[0]);
     * </pre>
     *
     * <p>Note that <tt>toArray(new Object[0])</tt> is identical in function to <tt>toArray()</tt>.
     * @param array The array into which the elements of this set are to be stored, if it is big enough; otherwise, a
     *              new array of the same runtime type is allocated for this purpose.
     * @return An array containing all the elements in this set
     * @throws ArrayStoreException  If the runtime type of the specified array is not a supertype of the runtime type of
     *                              every element in this set
     * @throws NullPointerException If the specified array is null
     */
    @Override
    public <T> T[] toArray(final T[] array) {
        return parameters.toArray(array);
    }

    /**
     * Compares the specified object with this set for equality. Returns <tt>true</tt> if the specified object is also
     * a set, the two sets have the same size, and every member of the specified set is contained in this set (or
     * equivalently, every member of this set is contained in the specified set). This definition ensures that the
     * equals method works properly across different implementations of the set interface.
     * @param obj Object to be compared for equality with this set.
     * @return <tt>true</tt> if the specified Object is equal to this set.
     */
    @Override
    public boolean equals(final Object obj) {
        return parameters.equals(obj);
    }

    /**
     * Returns the hash code value for this set. The hash code of a set is defined to be the sum of the hash codes of
     * the elements in the set, where the hashcode of a <tt>null</tt> element is defined to be zero. This ensures that
     * {@code s1.equals(s2)} implies that <code>s1.hashCode()==s2.hashCode()</code> for any two sets <code>s1
     * </code> and {@code s2}, as required by the general contract of the <tt>Object.hashCode</tt> method.
     * @return the hash code value for this set.
     * @see Object#hashCode()
     * @see Object#equals(Object)
     * @see Set#equals(Object)
     */
    @Override
    public int hashCode() {
        return parameters.hashCode();
    }
}
