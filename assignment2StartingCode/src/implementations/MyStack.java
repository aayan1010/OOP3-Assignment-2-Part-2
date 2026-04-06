package implementations;

import java.util.EmptyStackException;

import utilities.Iterator;
import utilities.StackADT;

/**
 * LIFO (Last-In-First-Out) Stack implementation backed strictly by
 * {@link MyArrayList}.
 *
 * <p>The top of the stack is stored at index 0 of the underlying list, so
 * {@link #push(Object)} inserts at index 0 and {@link #pop()} removes from
 * index 0. This means all elements returned by {@link #iterator()} are visited
 * from top to bottom.</p>
 *
 * <p>This implementation does <b>not</b> permit {@code null} elements and has
 * no fixed capacity (i.e. {@link #stackOverflow()} always returns
 * {@code false}).</p>
 *
 * @param <E> the type of elements stored in this stack.
 *
 * @author Aayan Karim
 * @version 1.0
 */
public class MyStack<E> implements StackADT<E>
{
    // The underlying dynamic array that provides all storage
    private final MyArrayList<E> list;

    /**
     * Constructs an empty stack with no elements.
     */
    public MyStack()
    {
        this.list = new MyArrayList<>();
    }

    /**
     * Pushes {@code toAdd} onto the top of this stack. The element is inserted
     * at index 0 of the backing list so that it becomes the new top.
     *
     * @param toAdd the element to push; must not be {@code null}.
     * @throws NullPointerException if {@code toAdd} is {@code null}.
     */
    @Override
    public void push( E toAdd ) throws NullPointerException
    {
        // Insert at position 0 so the new element becomes the top
        this.list.add( 0, toAdd );
    }

    /**
     * Removes and returns the element at the top of this stack.
     *
     * @return the element popped from the top.
     * @throws EmptyStackException if the stack contains no elements.
     */
    @Override
    public E pop() throws EmptyStackException
    {
        if( isEmpty() )
        {
            throw new EmptyStackException();
        }
        // Top is always at index 0
        return this.list.remove( 0 );
    }

    /**
     * Returns, without removing, the element at the top of this stack.
     *
     * @return the top element.
     * @throws EmptyStackException if the stack contains no elements.
     */
    @Override
    public E peek() throws EmptyStackException
    {
        if( isEmpty() )
        {
            throw new EmptyStackException();
        }
        // Top is always at index 0
        return this.list.get( 0 );
    }

    /**
     * Removes all elements from this stack. After this call, {@link #size()}
     * returns {@code 0}.
     */
    @Override
    public void clear()
    {
        this.list.clear();
    }

    /**
     * Returns {@code true} if this stack contains no elements.
     *
     * @return {@code true} when {@link #size()} is zero.
     */
    @Override
    public boolean isEmpty()
    {
        return this.list.isEmpty();
    }

    /**
     * Returns an {@code Object[]} containing all elements from top to bottom.
     *
     * @return an array of all stack elements in top-to-bottom order.
     */
    @Override
    public Object[] toArray()
    {
        return this.list.toArray();
    }

    /**
     * Returns an array containing all elements of this stack from top to
     * bottom. If {@code holder} is large enough, elements are placed into it;
     * otherwise a new array of the same runtime type is allocated.
     *
     * @param holder the target array; must not be {@code null}.
     * @return an array containing all stack elements in top-to-bottom order.
     * @throws NullPointerException if {@code holder} is {@code null}.
     */
    @Override
    public E[] toArray( E[] holder ) throws NullPointerException
    {
        return this.list.toArray( holder );
    }

    /**
     * Returns {@code true} if this stack contains an element equal to
     * {@code toFind} according to {@link Object#equals(Object)}.
     *
     * @param toFind the element to search for; must not be {@code null}.
     * @return {@code true} if the element is found.
     * @throws NullPointerException if {@code toFind} is {@code null}.
     */
    @Override
    public boolean contains( E toFind ) throws NullPointerException
    {
        return this.list.contains( toFind );
    }

    /**
     * Returns the 1-based position of {@code toFind} from the top of this
     * stack. The topmost element is at position 1. Returns {@code -1} if the
     * element is not present.
     *
     * @param toFind the element to search for.
     * @return the 1-based distance from the top, or {@code -1} if not found.
     */
    @Override
    public int search( E toFind )
    {
        if( toFind == null )
        {
            return -1; // Null elements are not stored, so they can never be found
        }

        // Walk from index 0 (top) downward
        for( int i = 0; i < this.list.size(); i++ )
        {
            if( this.list.get( i ).equals( toFind ) )
            {
                return i + 1; // Convert 0-based index to 1-based position
            }
        }
        return -1; // Not found
    }

    /**
     * Returns an iterator over the elements in this stack in top-to-bottom
     * order. The topmost element is returned first.
     *
     * @return a forward iterator from top to bottom.
     */
    @Override
    public Iterator<E> iterator()
    {
        return this.list.iterator();
    }

    /**
     * Compares this stack with {@code that} for equality. Two stacks are equal
     * if they contain the same elements in the same top-to-bottom order.
     *
     * @param that the other stack to compare; may be {@code null}.
     * @return {@code true} if both stacks are equal.
     */
    @Override
    public boolean equals( StackADT<E> that )
    {
        if( that == null || this.size() != that.size() )
        {
            return false; // Different sizes or null reference — cannot be equal
        }

        // Compare element by element from top to bottom
        Iterator<E> thisIt = this.iterator();
        Iterator<E> thatIt = that.iterator();
        while( thisIt.hasNext() )
        {
            if( !thisIt.next().equals( thatIt.next() ) )
            {
                return false; // First mismatch — stacks are not equal
            }
        }
        return true;
    }

    /**
     * Returns the number of elements currently in this stack.
     *
     * @return the current stack depth.
     */
    @Override
    public int size()
    {
        return this.list.size();
    }

    /**
     * Always returns {@code false} because this stack is backed by a dynamic
     * array list that grows without bound.
     *
     * @return {@code false} — overflow is not possible with this implementation.
     */
    @Override
    public boolean stackOverflow()
    {
        return false;
    }
}
