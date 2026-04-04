package implementations;

import exceptions.EmptyQueueException;
import utilities.Iterator;
import utilities.QueueADT;

/**
 * Queue implementation backed strictly by {@link MyDLL}.
 *
 * @param <E> element type stored in the queue.
 */
public class MyQueue<E> implements QueueADT<E>
{
	private final MyDLL<E> list;

	/**
	 * Creates an empty queue.
	 */
	public MyQueue()
	{
		this.list = new MyDLL<>();
	}

	@Override
	public void enqueue( E toAdd ) throws NullPointerException
	{
		this.list.add( toAdd );
	}

	@Override
	public E dequeue() throws EmptyQueueException
	{
		if( isEmpty() )
		{
			throw new EmptyQueueException();
		}
		return this.list.remove( 0 );
	}

	@Override
	public E peek() throws EmptyQueueException
	{
		if( isEmpty() )
		{
			throw new EmptyQueueException();
		}
		return this.list.get( 0 );
	}

	@Override
	public void dequeueAll()
	{
		this.list.clear();
	}

	@Override
	public boolean isEmpty()
	{
		return this.list.isEmpty();
	}

	@Override
	public boolean contains( E toFind ) throws NullPointerException
	{
		return this.list.contains( toFind );
	}

	@Override
	public int search( E toFind )
	{
		if( toFind == null )
		{
			return -1;
		}

		Iterator<E> iterator = iterator();
		int position = 1;
		while( iterator.hasNext() )
		{
			if( iterator.next().equals( toFind ) )
			{
				return position;
			}
			position++;
		}
		return -1;
	}

	@Override
	public Iterator<E> iterator()
	{
		return this.list.iterator();
	}

	@Override
	public boolean equals( QueueADT<E> that )
	{
		if( that == null || this.size() != that.size() )
		{
			return false;
		}

		Iterator<E> thisIterator = this.iterator();
		Iterator<E> thatIterator = that.iterator();
		while( thisIterator.hasNext() )
		{
			if( !thisIterator.next().equals( thatIterator.next() ) )
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public Object[] toArray()
	{
		return this.list.toArray();
	}

	@Override
	public E[] toArray( E[] holder ) throws NullPointerException
	{
		return this.list.toArray( holder );
	}

	@Override
	public boolean isFull()
	{
		return false;
	}

	@Override
	public int size()
	{
		return this.list.size();
	}
}
