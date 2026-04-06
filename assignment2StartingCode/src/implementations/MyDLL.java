package implementations;

import java.lang.reflect.Array;
import java.util.NoSuchElementException;

import utilities.Iterator;
import utilities.ListADT;

/**
 * Doubly Linked List (DLL) implementation of the {@link ListADT} interface.
 *
 * <p>Each element is wrapped in a private {@link Node} that holds references
 * to both its predecessor and successor, enabling O(1) insertion and removal
 * at either end of the list.  Access by index requires traversal and therefore
 * runs in O(n) time; however, the implementation chooses to traverse from the
 * nearer end (head or tail) to halve the average traversal length.</p>
 *
 * <p>This implementation does <b>not</b> permit {@code null} elements.</p>
 *
 * @param <E> the type of elements stored in this list.
 *
 * @author Jonah Gile
 * @version 1.0
 */
public class MyDLL<E> implements ListADT<E>
{
    // Reference to the first node in the list; null when the list is empty
    private Node<E> head;

    // Reference to the last node in the list; null when the list is empty
    private Node<E> tail;

    // Number of elements currently stored in the list
    private int size;

    /**
     * Constructs an empty doubly linked list with no elements.
     */
    public MyDLL()
    {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    /**
     * Returns the number of elements currently in this list.
     *
     * @return the current element count.
     */
    @Override
    public int size()
    {
        return this.size;
    }

    /**
     * Removes all elements from this list. Node references are nulled to
     * allow the garbage collector to reclaim memory. After this call,
     * {@link #size()} returns {@code 0}.
     */
    @Override
    public void clear()
    {
        // Walk the chain and sever every inter-node link
        Node<E> current = this.head;
        while( current != null )
        {
            Node<E> next = current.next;
            current.previous = null; // Help GC by removing back-reference
            current.next     = null; // Help GC by removing forward-reference
            current.value    = null; // Release the stored element
            current          = next;
        }

        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    /**
     * Inserts {@code toAdd} at the specified 0-based position. All nodes at
     * and beyond that position are logically shifted one position to the right.
     *
     * @param index the position at which to insert (0 &le; index &le; size).
     * @param toAdd the element to insert; must not be {@code null}.
     * @return {@code true} if the element was added successfully.
     * @throws NullPointerException      if {@code toAdd} is {@code null}.
     * @throws IndexOutOfBoundsException if {@code index < 0 || index > size()}.
     */
    @Override
    public boolean add( int index, E toAdd )
            throws NullPointerException, IndexOutOfBoundsException
    {
        validateElement( toAdd );
        validateInsertIndex( index );

        Node<E> newNode = new Node<>( toAdd );

        if( this.size == 0 )
        {
            // Inserting into an empty list — new node is both head and tail
            this.head = newNode;
            this.tail = newNode;
        }
        else if( index == 0 )
        {
            // Prepend — new node becomes the new head
            newNode.next      = this.head;
            this.head.previous = newNode;
            this.head          = newNode;
        }
        else if( index == this.size )
        {
            // Append — new node becomes the new tail
            newNode.previous = this.tail;
            this.tail.next   = newNode;
            this.tail        = newNode;
        }
        else
        {
            // Mid-list insertion — splice newNode between current-1 and current
            Node<E> current  = getNode( index );
            Node<E> previous = current.previous;

            newNode.previous  = previous;
            newNode.next      = current;
            previous.next     = newNode;
            current.previous  = newNode;
        }

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
     * Appends all elements of {@code toAdd} to the end of this list in the
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

        // Iterate and append each element in order
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
        // getNode handles bounds validation
        return getNode( index ).value;
    }

    /**
     * Removes and returns the element at the specified position. Adjacent
     * nodes are re-linked to close the gap.
     *
     * @param index the 0-based position of the element to remove.
     * @return the removed element.
     * @throws IndexOutOfBoundsException if {@code index < 0 || index >= size()}.
     */
    @Override
    public E remove( int index ) throws IndexOutOfBoundsException
    {
        Node<E> target = getNode( index );
        return unlink( target );
    }

    /**
     * Removes the first occurrence of {@code toRemove} in this list. If the
     * element is not present, the list is unchanged and {@code null} is
     * returned.
     *
     * @param toRemove the element to remove; must not be {@code null}.
     * @return the removed element, or {@code null} if not found.
     * @throws NullPointerException if {@code toRemove} is {@code null}.
     */
    @Override
    public E remove( E toRemove ) throws NullPointerException
    {
        validateElement( toRemove );

        // Walk the chain looking for the first match
        Node<E> current = this.head;
        while( current != null )
        {
            if( current.value.equals( toRemove ) )
            {
                return unlink( current ); // Found — remove and return
            }
            current = current.next;
        }
        return null; // Not found
    }

    /**
     * Replaces the element at {@code index} with {@code toChange} and returns
     * the element that was previously there.
     *
     * @param index    the 0-based position of the element to replace.
     * @param toChange the replacement element; must not be {@code null}.
     * @return the element that was at {@code index} before the replacement.
     * @throws NullPointerException      if {@code toChange} is {@code null}.
     * @throws IndexOutOfBoundsException if {@code index < 0 || index >= size()}.
     */
    @Override
    public E set( int index, E toChange )
            throws NullPointerException, IndexOutOfBoundsException
    {
        validateElement( toChange );

        Node<E> current = getNode( index );
        E previous      = current.value;
        current.value   = toChange;
        return previous;
    }

    /**
     * Returns {@code true} if this list contains no elements.
     *
     * @return {@code true} when {@link #size()} equals zero.
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

        // Walk the chain — return true on the first match
        Node<E> current = this.head;
        while( current != null )
        {
            if( current.value.equals( toFind ) )
            {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    /**
     * Copies all elements into {@code toHold} (or a new array of the same
     * runtime type if it is too small) and returns the result. Elements appear
     * in list order starting at index 0.
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

        // Walk the chain and copy each value in order
        Node<E> current = this.head;
        int index = 0;
        while( current != null )
        {
            result[index++] = current.value;
            current = current.next;
        }

        // Null-terminate if the array has extra capacity
        if( result.length > this.size )
        {
            result[this.size] = null;
        }

        return result;
    }

    /**
     * Returns a new {@code Object[]} containing all elements of this list in
     * head-to-tail order.
     *
     * @return an {@code Object[]} of length {@link #size()}.
     */
    @Override
    public Object[] toArray()
    {
        Object[] result = new Object[this.size];

        // Walk the chain and copy each value in order
        Node<E> current = this.head;
        int index = 0;
        while( current != null )
        {
            result[index++] = current.value;
            current = current.next;
        }
        return result;
    }

    /**
     * Returns a forward iterator that walks the list from head to tail.
     *
     * @return a new {@link Iterator} positioned before the first element.
     */
    @Override
    public Iterator<E> iterator()
    {
        return new DLLIterator();
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
     * Throws {@link IndexOutOfBoundsException} if {@code index} is outside
     * the valid insertion range {@code [0, size]}.
     *
     * @param index the index to validate.
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
     * Returns the {@link Node} at the given 0-based position. Traversal
     * starts from whichever end (head or tail) is closer to {@code index}.
     *
     * @param index the 0-based position of the desired node.
     * @return the node at {@code index}.
     * @throws IndexOutOfBoundsException if {@code index < 0 || index >= size}.
     */
    private Node<E> getNode( int index )
    {
        if( index < 0 || index >= this.size )
        {
            throw new IndexOutOfBoundsException(
                    "Access index " + index + " out of bounds for size " + this.size );
        }

        if( index < this.size / 2 )
        {
            // Closer to the head — traverse forward
            Node<E> current = this.head;
            for( int i = 0; i < index; i++ )
            {
                current = current.next;
            }
            return current;
        }
        else
        {
            // Closer to the tail — traverse backward
            Node<E> current = this.tail;
            for( int i = this.size - 1; i > index; i-- )
            {
                current = current.previous;
            }
            return current;
        }
    }

    /**
     * Removes {@code target} from the chain by re-linking its neighbours and
     * returns its stored value. Updates {@code head} and {@code tail} as needed.
     *
     * @param target the node to remove; must not be {@code null}.
     * @return the value that was held by {@code target}.
     */
    private E unlink( Node<E> target )
    {
        Node<E> previous = target.previous;
        Node<E> next     = target.next;

        if( previous == null )
        {
            // target was the head — promote its successor
            this.head = next;
        }
        else
        {
            previous.next = next; // Skip over target in the forward direction
        }

        if( next == null )
        {
            // target was the tail — promote its predecessor
            this.tail = previous;
        }
        else
        {
            next.previous = previous; // Skip over target in the backward direction
        }

        // Sever target's links to aid garbage collection
        target.previous = null;
        target.next     = null;
        E removed       = target.value;
        target.value    = null;
        this.size--;
        return removed;
    }

    // -----------------------------------------------------------------------
    // Inner Node class
    // -----------------------------------------------------------------------

    /**
     * Doubly linked node that wraps a single list element and holds references
     * to both its predecessor and successor nodes.
     *
     * @param <E> the type of value held by this node.
     */
    private static class Node<E>
    {
        /** The element stored in this node. */
        private E value;

        /** Reference to the previous node in the list, or {@code null} at the head. */
        private Node<E> previous;

        /** Reference to the next node in the list, or {@code null} at the tail. */
        private Node<E> next;

        /**
         * Constructs a node holding the given value with no neighbours linked.
         *
         * @param value the element to store; should not be {@code null}.
         */
        private Node( E value )
        {
            this.value = value;
        }
    }

    // -----------------------------------------------------------------------
    // Inner iterator class
    // -----------------------------------------------------------------------

    /**
     * Forward-only iterator that walks the list from head to tail.
     */
    private class DLLIterator implements Iterator<E>
    {
        // The next node to visit; starts at the head of the list
        private Node<E> current = head;

        /**
         * Returns {@code true} if the iterator has not yet passed the tail.
         *
         * @return {@code true} when there is at least one more element to visit.
         */
        @Override
        public boolean hasNext()
        {
            return this.current != null;
        }

        /**
         * Returns the value of the current node and advances the iterator to
         * the next node.
         *
         * @return the value stored in the current node.
         * @throws NoSuchElementException if {@link #hasNext()} is {@code false}.
         */
        @Override
        public E next() throws NoSuchElementException
        {
            if( !hasNext() )
            {
                throw new NoSuchElementException( "No more elements to iterate." );
            }

            E value      = this.current.value; // Capture the value before advancing
            this.current = this.current.next;  // Move to the next node
            return value;
        }
    }
}
