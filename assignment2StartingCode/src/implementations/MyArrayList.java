package implementations;

import java.lang.reflect.Array;
import java.util.NoSuchElementException;

import utilities.Iterator;
import utilities.ListADT;

/**
 * Array-backed implementation of {@link ListADT}.
 *
 * @param <E> element type stored in the list.
 */
public class MyArrayList<E> implements ListADT<E>
{
	private static final int DEFAULT_CAPACITY = 10;

	private E[] elements;
	private int size;

	/**
	 * Creates an empty list with the default capacity.
	 */
	@SuppressWarnings( "unchecked" )
	public MyArrayList()
	{
		this.elements = (E[]) new Object[DEFAULT_CAPACITY];
		this.size = 0;
	}

	@Override
	public int size()
	{
		return this.size;
	}

	@Override
	public void clear()
	{
		for( int i = 0; i < this.size; i++ )
		{
			this.elements[i] = null;
		}
		this.size = 0;
	}

	@Override
	public boolean add( int index, E toAdd ) throws NullPointerException, IndexOutOfBoundsException
	{
		validateElement( toAdd );
		validateInsertIndex( index );
		ensureCapacity();

		for( int i = this.size; i > index; i-- )
		{
			this.elements[i] = this.elements[i - 1];
		}

		this.elements[index] = toAdd;
		this.size++;
		return true;
	}

	@Override
	public boolean add( E toAdd ) throws NullPointerException
	{
		return add( this.size, toAdd );
	}

	@Override
	public boolean addAll( ListADT<? extends E> toAdd ) throws NullPointerException
	{
		if( toAdd == null )
		{
			throw new NullPointerException();
		}

		Iterator<? extends E> iterator = toAdd.iterator();
		while( iterator.hasNext() )
		{
			add( iterator.next() );
		}
		return true;
	}

	@Override
	public E get( int index ) throws IndexOutOfBoundsException
	{
		validateAccessIndex( index );
		return this.elements[index];
	}

	@Override
	public E remove( int index ) throws IndexOutOfBoundsException
	{
		validateAccessIndex( index );

		E removed = this.elements[index];
		for( int i = index; i < this.size - 1; i++ )
		{
			this.elements[i] = this.elements[i + 1];
		}

		this.elements[this.size - 1] = null;
		this.size--;
		return removed;
	}

	@Override
	public E remove( E toRemove ) throws NullPointerException
	{
		validateElement( toRemove );

		for( int i = 0; i < this.size; i++ )
		{
			if( this.elements[i].equals( toRemove ) )
			{
				return remove( i );
			}
		}
		return null;
	}

	@Override
	public E set( int index, E toChange ) throws NullPointerException, IndexOutOfBoundsException
	{
		validateElement( toChange );
		validateAccessIndex( index );

		E previous = this.elements[index];
		this.elements[index] = toChange;
		return previous;
	}

	@Override
	public boolean isEmpty()
	{
		return this.size == 0;
	}

	@Override
	public boolean contains( E toFind ) throws NullPointerException
	{
		validateElement( toFind );

		for( int i = 0; i < this.size; i++ )
		{
			if( this.elements[i].equals( toFind ) )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public E[] toArray( E[] toHold ) throws NullPointerException
	{
		if( toHold == null )
		{
			throw new NullPointerException();
		}

		E[] result = toHold;
		if( toHold.length < this.size )
		{
			result = (E[]) Array.newInstance( toHold.getClass().getComponentType(), this.size );
		}

		for( int i = 0; i < this.size; i++ )
		{
			result[i] = this.elements[i];
		}

		if( result.length > this.size )
		{
			result[this.size] = null;
		}

		return result;
	}

	@Override
	public Object[] toArray()
	{
		Object[] result = new Object[this.size];
		for( int i = 0; i < this.size; i++ )
		{
			result[i] = this.elements[i];
		}
		return result;
	}

	@Override
	public Iterator<E> iterator()
	{
		return new ArrayListIterator();
	}

	private void validateElement( E element )
	{
		if( element == null )
		{
			throw new NullPointerException();
		}
	}

	private void validateInsertIndex( int index )
	{
		if( index < 0 || index > this.size )
		{
			throw new IndexOutOfBoundsException();
		}
	}

	private void validateAccessIndex( int index )
	{
		if( index < 0 || index >= this.size )
		{
			throw new IndexOutOfBoundsException();
		}
	}

	@SuppressWarnings( "unchecked" )
	private void ensureCapacity()
	{
		if( this.size < this.elements.length )
		{
			return;
		}

		E[] expanded = (E[]) new Object[this.elements.length * 2];
		for( int i = 0; i < this.size; i++ )
		{
			expanded[i] = this.elements[i];
		}
		this.elements = expanded;
	}

	/**
	 * Iterator that walks the list from index 0 to size - 1.
	 */
	private class ArrayListIterator implements Iterator<E>
	{
		private int currentIndex;

		@Override
		public boolean hasNext()
		{
			return this.currentIndex < size;
		}

		@Override
		public E next() throws NoSuchElementException
		{
			if( !hasNext() )
			{
				throw new NoSuchElementException();
			}
			return elements[this.currentIndex++];
		}
	}
}
