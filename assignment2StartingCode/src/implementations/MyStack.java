package implementations;

import java.util.EmptyStackException;

import utilities.Iterator;
import utilities.StackADT;

/**
 * Stack implementation backed strictly by {@link MyArrayList}.
 *
 * @param <E> element type stored in the stack.
 */
public class MyStack<E> implements StackADT<E>
{
	private final MyArrayList<E> list;

	/**
	 * Creates an empty stack.
	 */
	public MyStack()
	{
		this.list = new MyArrayList<>();
	}

	@Override
	public void push( E toAdd ) throws NullPointerException
	{
		this.list.add( 0, toAdd );
	}

	@Override
	public E pop() throws EmptyStackException
	{
		if( isEmpty() )
		{
			throw new EmptyStackException();
		}
		return this.list.remove( 0 );
	}

	@Override
	public E peek() throws EmptyStackException
	{
		if( isEmpty() )
		{
			throw new EmptyStackException();
		}
		return this.list.get( 0 );
	}

	@Override
	public void clear()
	{
		this.list.clear();
	}

	@Override
	public boolean isEmpty()
	{
		return this.list.isEmpty();
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

		for( int i = 0; i < this.list.size(); i++ )
		{
			if( this.list.get( i ).equals( toFind ) )
			{
				return i + 1;
			}
		}
		return -1;
	}

	@Override
	public Iterator<E> iterator()
	{
		return this.list.iterator();
	}

	@Override
	public boolean equals( StackADT<E> that )
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
	public int size()
	{
		return this.list.size();
	}

	@Override
	public boolean stackOverflow()
	{
		return false;
	}
}
