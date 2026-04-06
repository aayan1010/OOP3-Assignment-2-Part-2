package implementations;

import exceptions.EmptyQueueException;
import utilities.Iterator;
import utilities.QueueADT;

/**
 * FIFO (First-In-First-Out) Queue implementation backed strictly by
 * {@link MyDLL} (Doubly Linked List).
 *
 * <p>Elements are enqueued at the tail of the underlying list and dequeued
 * from the head, giving O(1) performance for both operations. The iterator
 * visits elements from head (front) to tail (rear).</p>
 *
 * <p>This implementation does <b>not</b> permit {@code null} elements and has
 * no fixed capacity (i.e. {@link #isFull()} always returns {@code false}).</p>
 *
 * @param <E> the type of elements stored in this queue.
 *
 * @author Jonah Gile
 * @version 1.0
 */
public class MyQueue<E> implements QueueADT<E>
{
    // The underlying doubly linked list that provides all storage
    private final MyDLL<E> list;

    /**
     * Constructs an empty queue with no elements.
     */
    public MyQueue()
    {
        this.list = new MyDLL<>();
    }

    /**
     * Adds {@code toAdd} to the tail (rear) of this queue.
     *
     * @param toAdd the element to enqueue; must not be {@code null}.
     * @throws NullPointerException if {@code toAdd} is {@code null}.
     */
    @Override
    public void enqueue( E toAdd ) throws NullPointerException
    {
        // Append to the end — the DLL's add(E) always appends to the tail
        this.list.add( toAdd );
    }

    /**
     * Removes and returns the element at the head (front) of this queue.
     *
     * @return the element that has been in the queue the longest.
     * @throws EmptyQueueException if the queue contains no elements.
     */
    @Override
    public E dequeue() throws EmptyQueueException
    {
        if( isEmpty() )
        {
            throw new EmptyQueueException( "Cannot dequeue from an empty queue." );
        }
        // Head is always at index 0 of the backing list
        return this.list.remove( 0 );
    }

    /**
     * Returns, without removing, the element at the head (front) of this queue.
     *
     * @return the front element.
     * @throws EmptyQueueException if the queue contains no elements.
     */
    @Override
    public E peek() throws EmptyQueueException
    {
        if( isEmpty() )
        {
            throw new EmptyQueueException( "Cannot peek at an empty queue." );
        }
        // Head is always at index 0 of the backing list
        return this.list.get( 0 );
    }

    /**
     * Removes all elements from this queue. After this call, {@link #size()}
     * returns {@code 0}.
     */
    @Override
    public void dequeueAll()
    {
        this.list.clear();
    }

    /**
     * Returns {@code true} if this queue contains no elements.
     *
     * @return {@code true} when {@link #size()} is zero.
     */
    @Override
    public boolean isEmpty()
    {
        return this.list.isEmpty();
    }

    /**
     * Returns {@code true} if this queue contains at least one element equal
     * to {@code toFind} according to {@link Object#equals(Object)}.
     *
     * @param toFind the element to search for; must not be {@code null}.
     * @return {@code true} if a matching element is found.
     * @throws NullPointerException if {@code toFind} is {@code null}.
     */
    @Override
    public boolean contains( E toFind ) throws NullPointerException
    {
        return this.list.contains( toFind );
    }

    /**
     * Returns the 1-based position of {@code toFind} from the head of this
     * queue. The front element is at position 1. Returns {@code -1} if the
     * element is not present.
     *
     * @param toFind the element to search for.
     * @return the 1-based distance from the head, or {@code -1} if not found.
     */
    @Override
    public int search( E toFind )
    {
        if( toFind == null )
        {
            return -1; // Null elements are never stored
        }

        // Walk from position 1 (front) toward the rear
        Iterator<E> it = iterator();
        int position = 1;
        while( it.hasNext() )
        {
            if( it.next().equals( toFind ) )
            {
                return position; // Return 1-based position
            }
            position++;
        }
        return -1; // Not found
    }

    /**
     * Returns an iterator over the elements in this queue in head-to-tail
     * order. The front element is returned first.
     *
     * @return a forward iterator from head to tail.
     */
    @Override
    public Iterator<E> iterator()
    {
        return this.list.iterator();
    }

    /**
     * Compares this queue with {@code that} for equality. Two queues are equal
     * if they contain the same elements in the same head-to-tail order.
     *
     * @param that the other queue to compare; may be {@code null}.
     * @return {@code true} if both queues are equal.
     */
    @Override
    public boolean equals( QueueADT<E> that )
    {
        if( that == null || this.size() != that.size() )
        {
            return false; // Null or different sizes — cannot be equal
        }

        // Compare element by element from front to rear
        Iterator<E> thisIt = this.iterator();
        Iterator<E> thatIt = that.iterator();
        while( thisIt.hasNext() )
        {
            if( !thisIt.next().equals( thatIt.next() ) )
            {
                return false; // First mismatch — queues are not equal
            }
        }
        return true;
    }

    /**
     * Returns an {@code Object[]} containing all elements from head to tail.
     *
     * @return an array of all queue elements in FIFO order.
     */
    @Override
    public Object[] toArray()
    {
        return this.list.toArray();
    }

    /**
     * Returns an array containing all elements of this queue from head to
     * tail. If {@code holder} is large enough, elements are placed into it;
     * otherwise a new array of the same runtime type is allocated.
     *
     * @param holder the target array; must not be {@code null}.
     * @return an array containing all queue elements in FIFO order.
     * @throws NullPointerException if {@code holder} is {@code null}.
     */
    @Override
    public E[] toArray( E[] holder ) throws NullPointerException
    {
        return this.list.toArray( holder );
    }

    /**
     * Always returns {@code false} because this queue is backed by a dynamic
     * doubly linked list that grows without bound.
     *
     * @return {@code false} — overflow is not possible with this implementation.
     */
    @Override
    public boolean isFull()
    {
        return false;
    }

    /**
     * Returns the number of elements currently in this queue.
     *
     * @return the current queue size.
     */
    @Override
    public int size()
    {
        return this.list.size();
    }
}
