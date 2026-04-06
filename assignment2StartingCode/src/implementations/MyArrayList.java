package implementations;

import java.lang.reflect.Array;
import java.util.NoSuchElementException;

import utilities.Iterator;
import utilities.ListADT;

/**
 * Array-backed implementation of the {@link ListADT} interface.
 *
 * <p>Elements are stored in a dynamically resizing internal array. When the
 * array reaches capacity it is automatically doubled, so the list has no fixed
 * upper bound. Random-access reads and writes run in O(1) time; insertions and
 * removals run in O(n) time because elements may need to be shifted.
 * {@link #add(Object)} runs in amortised O(1) time.</p>
 *
 * <p>This implementation does <b>not</b> permit {@code null} elements.</p>
 *
 * @param <E> the type of elements stored in this list.
 *
 * @author Aayan Karim
 * @version 1.0
 */
public class MyArrayList<E> implements ListADT<E>
{
    // Initial backing-array capacity before any resizing
    private static final int DEFAULT_CAPACITY = 10;

    // Internal array that holds the list's elements
    private E[] elements;

    // Number of elements currently in the list
    private int size;

    /**
     * Constructs an empty list backed by an array of the default capacity
     * ({@value #DEFAULT_CAPACITY}).
     */
    @SuppressWarnings( "unchecked" )
    public MyArrayList()
    {
        // The unchecked cast is safe: the array only ever holds objects of type E
        this.elements = (E[]) new Object[DEFAULT_CAPACITY];
        this.size = 0;
    }

    /**
     * Returns the number of elements currently stored in this list.
     *
     * @return the current element count.
     */
    @Override
    public int size()
    {
        return this.size;
    }

    /**
     * Removes all elements from this list. After this call, {@link #size()}
     * returns {@code 0}. Occupied slots are nulled to allow garbage
     * collection of the removed objects.
     */
    @Override
    public void clear()
    {
        // Null every live slot so the GC can reclaim the referenced objects
        for( int i = 0; i < this.size; i++ )
        {
            this.elements[i] = null;
        }
        this.size = 0;
    }

    /**
     * Inserts {@code toAdd} at position {@code index}, shifting all elements
     * at and beyond that position one place to the right. Grows the backing
     * array if it is currently full.
     *
     * @param index the 0-based insertion position (0 &le; index &le; size).
     * @param toAdd the element to insert; must not be {@code null}.
     * @return {@code true} if the element was added successfully.
     * @throws NullPointerException      if {@code toAdd} is {@code null}.
     * @throws IndexOutOfBoundsException if {@code index < 0 || index > size()}.
     */
    @Override
    public boolean add( int index, E toAdd )
            throws NullPointerException, IndexOutOfBoundsException
    {
        validateElement( toAdd );      // Reject null elements
        validateInsertIndex( index );  // Reject out-of-range indices
        ensureCapacity();              // Grow the backing array if needed

        // Shift elements right to open up the target slot
        for( int i = this.size; i > index; i-- )
        {
            this.elements[i] = this.elements[i - 1];
        }

        // Place the new element and update the size counter
        this.elements[index] = toAdd;
        this.size++;
        return true;
    }

    /**
     * Appends {@code toAdd} to the end of this list. Equivalent to calling
     * {@link #add(int, Object) add(size(), toAdd)}.
     *
     * @param toAdd the element to append; must not be {@code null}.
     * @return {@code true} if the element was added successfully.
     * @throws NullPointerException if {@code toAdd} is {@code null}.
     */
    @Override
    public boolean add( E toAdd ) throws NullPointerException
    {
        return add( this.size, toAdd );
    }

    /**
     * Appends all elements in {@code toAdd} to the end of this list, in the
     * order returned by the list's iterator.
     *
     * @param toAdd the source list; must not be {@code null}.
     * @return {@code true} if the operation completed successfully.
     * @throws NullPointerException if {@code toAdd} is {@code null}.
     */
    @Override
    public boolean addAll( ListADT<? extends E> toAdd ) throws NullPointerException
    {
        if( toAdd == null )
        {
            throw new NullPointerException( "Source list must not be null." );
        }

        // Iterate the source list and append each element in order
        Iterator<? extends E> it = toAdd.iterator();
        while( it.hasNext() )
        {
            add( it.next() );
        }
        return true;
    }

    /**
     * Returns the element at the specified 0-based position without removing
     * it.
     *
     * @param index the position of the desired element.
     * @return the element at {@code index}.
     * @throws IndexOutOfBoundsException if {@code index < 0 || index >= size()}.
     */
    @Override
    public E get( int index ) throws IndexOutOfBoundsException
    {
        validateAccessIndex( index );
        return this.elements[index];
    }

    /**
     * Removes and returns the element at the specified position. Elements
     * after the removed position are shifted one place to the left.
     *
     * @param index the 0-based position of the element to remove.
     * @return the removed element.
     * @throws IndexOutOfBoundsException if {@code index < 0 || index >= size()}.
     */
    @Override
    public E remove( int index ) throws IndexOutOfBoundsException
    {
        validateAccessIndex( index );

        E removed = this.elements[index]; // Save the element to return it

        // Shift elements left to fill the gap
        for( int i = index; i < this.size - 1; i++ )
        {
            this.elements[i] = this.elements[i + 1];
        }

        // Null the dangling last slot so the GC can reclaim the object
        this.elements[this.size - 1] = null;
        this.size--;
        return removed;
    }

    /**
     * Removes the first occurrence of {@code toRemove} from this list.
     * If the element is not present, the list is unchanged and {@code null}
     * is returned.
     *
     * @param toRemove the element to search for and remove; must not be
     *                 {@code null}.
     * @return the removed element, or {@code null} if not found.
     * @throws NullPointerException if {@code toRemove} is {@code null}.
     */
    @Override
    public E remove( E toRemove ) throws NullPointerException
    {
        validateElement( toRemove );

        // Linear scan for the first matching element
        for( int i = 0; i < this.size; i++ )
        {
            if( this.elements[i].equals( toRemove ) )
            {
                return remove( i ); // Delegate to index-based removal
            }
        }
        return null; // Element not found
    }

    /**
     * Replaces the element at the specified position with {@code toChange} and
     * returns the element previously stored there.
     *
     * @param index    the 0-based position of the element to replace.
     * @param toChange the replacement element; must not be {@code null}.
     * @return the element previously at {@code index}.
     * @throws NullPointerException      if {@code toChange} is {@code null}.
     * @throws IndexOutOfBoundsException if {@code index < 0 || index >= size()}.
     */
    @Override
    public E set( int index, E toChange )
            throws NullPointerException, IndexOutOfBoundsException
    {
        validateElement( toChange );
        validateAccessIndex( index );

        E previous = this.elements[index]; // Remember the old value
        this.elements[index] = toChange;
        return previous;
    }

    /**
     * Returns {@code true} if this list contains no elements.
     *
     * @return {@code true} when {@link #size()} is zero.
     */
    @Override
    public boolean isEmpty()
    {
        return this.size == 0;
    }

    /**
     * Returns {@code true} if this list contains at least one element equal to
     * {@code toFind} according to {@link Object#equals(Object)}.
     *
     * @param toFind the element to search for; must not be {@code null}.
     * @return {@code true} if a matching element is found.
     * @throws NullPointerException if {@code toFind} is {@code null}.
     */
    @Override
    public boolean contains( E toFind ) throws NullPointerException
    {
        validateElement( toFind );

        // Linear scan — return true on the first match
        for( int i = 0; i < this.size; i++ )
        {
            if( this.elements[i].equals( toFind ) )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Copies all elements of this list into {@code toHold} (or a new array of
     * the same runtime type if {@code toHold} is too small) and returns the
     * result. Elements appear in list order starting at index 0.
     *
     * @param toHold the target array; must not be {@code null}.
     * @return an array containing all elements of this list.
     * @throws NullPointerException if {@code toHold} is {@code null}.
     */
    @Override
    @SuppressWarnings( "unchecked" )
    public E[] toArray( E[] toHold ) throws NullPointerException
    {
        if( toHold == null )
        {
            throw new NullPointerException( "Destination array must not be null." );
        }

        E[] result = toHold;

        // Allocate a larger array of the same component type if needed
        if( toHold.length < this.size )
        {
            result = (E[]) Array.newInstance(
                    toHold.getClass().getComponentType(), this.size );
        }

        // Copy all elements into the result array
        for( int i = 0; i < this.size; i++ )
        {
            result[i] = this.elements[i];
        }

        // Null-terminate the array if it has extra space
        if( result.length > this.size )
        {
            result[this.size] = null;
        }

        return result;
    }

    /**
     * Returns a new {@code Object[]} containing all elements of this list in
     * order.
     *
     * @return an {@code Object[]} of length {@link #size()}.
     */
    @Override
    public Object[] toArray()
    {
        Object[] result = new Object[this.size];

        // Copy each element into the result array
        for( int i = 0; i < this.size; i++ )
        {
            result[i] = this.elements[i];
        }
        return result;
    }

    /**
     * Returns a forward iterator over the elements of this list, from index
     * {@code 0} to {@code size - 1}.
     *
     * @return a new {@link Iterator} positioned before the first element.
     */
    @Override
    public Iterator<E> iterator()
    {
        return new ArrayListIterator();
    }

    // -----------------------------------------------------------------------
    // Private helper methods
    // -----------------------------------------------------------------------

    /**
     * Throws {@link NullPointerException} if {@code element} is {@code null}.
     *
     * @param element the value to validate.
     */
    private void validateElement( E element )
    {
        if( element == null )
        {
            throw new NullPointerException( "Null elements are not permitted." );
        }
    }

    /**
     * Throws {@link IndexOutOfBoundsException} if {@code index} is outside the
     * valid insertion range {@code [0, size]}.
     *
     * @param index the index to validate for an insert operation.
     */
    private void validateInsertIndex( int index )
    {
        if( index < 0 || index > this.size )
        {
            throw new IndexOutOfBoundsException(
                    "Insert index " + index + " out of bounds for size " + this.size );
        }
    }

    /**
     * Throws {@link IndexOutOfBoundsException} if {@code index} is outside the
     * valid access range {@code [0, size)}.
     *
     * @param index the index to validate for a get, set, or remove operation.
     */
    private void validateAccessIndex( int index )
    {
        if( index < 0 || index >= this.size )
        {
            throw new IndexOutOfBoundsException(
                    "Access index " + index + " out of bounds for size " + this.size );
        }
    }

    /**
     * Doubles the capacity of the backing array when it is full.
     * All existing elements are copied into the new, larger array.
     */
    @SuppressWarnings( "unchecked" )
    private void ensureCapacity()
    {
        // No-op if there is still room in the current array
        if( this.size < this.elements.length )
        {
            return;
        }

        // Double the array and copy all elements across
        E[] expanded = (E[]) new Object[this.elements.length * 2];
        for( int i = 0; i < this.size; i++ )
        {
            expanded[i] = this.elements[i];
        }
        this.elements = expanded;
    }

    // -----------------------------------------------------------------------
    // Inner iterator class
    // -----------------------------------------------------------------------

    /**
     * Forward-only iterator that walks the backing array from index 0 to
     * {@code size - 1}.
     */
    private class ArrayListIterator implements Iterator<E>
    {
        // Index of the next element to return
        private int currentIndex;

        /**
         * Returns {@code true} if the iterator has not yet reached the end of
         * the list.
         *
         * @return {@code true} if there are more elements to iterate over.
         */
        @Override
        public boolean hasNext()
        {
            return this.currentIndex < size;
        }

        /**
         * Returns the next element in the list and advances the iterator.
         *
         * @return the element at the current iterator position.
         * @throws NoSuchElementException if {@link #hasNext()} is
         *                                {@code false}.
         */
        @Override
        public E next() throws NoSuchElementException
        {
            if( !hasNext() )
            {
                throw new NoSuchElementException( "No more elements to iterate." );
            }
            // Return the current element and advance the index
            return elements[this.currentIndex++];
        }
    }
}
