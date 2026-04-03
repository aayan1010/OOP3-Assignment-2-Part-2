package implementations;

import java.lang.reflect.Array;
import java.util.NoSuchElementException;

import utilities.Iterator;
import utilities.ListADT;

/**
 * Doubly linked list implementation of {@link ListADT}.
 *
 * @param <E> element type stored in the list.
 */
public class MyDLL<E> implements ListADT<E>
{
	private Node<E> head;
	private Node<E> tail;
	private int size;

	/**
	 * Creates an empty doubly linked list.
	 */
	public MyDLL()
	{
		this.head = null;
		this.tail = null;
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
		Node<E> current = this.head;
		while( current != null )
		{
			Node<E> next = current.next;
			current.previous = null;
			current.next = null;
			current.value = null;
			current = next;
		}

		this.head = null;
		this.tail = null;
		this.size = 0;
	}

	@Override
	public boolean add( int index, E toAdd ) throws NullPointerException, IndexOutOfBoundsException
	{
		validateElement( toAdd );
		validateInsertIndex( index );

		Node<E> newNode = new Node<>( toAdd );

		if( this.size == 0 )
		{
			this.head = newNode;
			this.tail = newNode;
		}
		else if( index == 0 )
		{
			newNode.next = this.head;
			this.head.previous = newNode;
			this.head = newNode;
		}
		else if( index == this.size )
		{
			newNode.previous = this.tail;
			this.tail.next = newNode;
			this.tail = newNode;
		}
		else
		{
			Node<E> current = getNode( index );
			Node<E> previous = current.previous;

			newNode.previous = previous;
			newNode.next = current;
			previous.next = newNode;
			current.previous = newNode;
		}

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
		return getNode( index ).value;
	}

	@Override
	public E remove( int index ) throws IndexOutOfBoundsException
	{
		Node<E> target = getNode( index );
		return unlink( target );
	}

	@Override
	public E remove( E toRemove ) throws NullPointerException
	{
		validateElement( toRemove );

		Node<E> current = this.head;
		while( current != null )
		{
			if( current.value.equals( toRemove ) )
			{
				return unlink( current );
			}
			current = current.next;
		}
		return null;
	}

	@Override
	public E set( int index, E toChange ) throws NullPointerException, IndexOutOfBoundsException
	{
		validateElement( toChange );

		Node<E> current = getNode( index );
		E previous = current.value;
		current.value = toChange;
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

		Node<E> current = this.head;
		int index = 0;
		while( current != null )
		{
			result[index++] = current.value;
			current = current.next;
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
		Node<E> current = this.head;
		int index = 0;
		while( current != null )
		{
			result[index++] = current.value;
			current = current.next;
		}
		return result;
	}

	@Override
	public Iterator<E> iterator()
	{
		return new DLLIterator();
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

	private Node<E> getNode( int index )
	{
		if( index < 0 || index >= this.size )
		{
			throw new IndexOutOfBoundsException();
		}

		if( index < this.size / 2 )
		{
			Node<E> current = this.head;
			for( int i = 0; i < index; i++ )
			{
				current = current.next;
			}
			return current;
		}

		Node<E> current = this.tail;
		for( int i = this.size - 1; i > index; i-- )
		{
			current = current.previous;
		}
		return current;
	}

	private E unlink( Node<E> target )
	{
		Node<E> previous = target.previous;
		Node<E> next = target.next;

		if( previous == null )
		{
			this.head = next;
		}
		else
		{
			previous.next = next;
		}

		if( next == null )
		{
			this.tail = previous;
		}
		else
		{
			next.previous = previous;
		}

		target.previous = null;
		target.next = null;
		E removed = target.value;
		target.value = null;
		this.size--;
		return removed;
	}

	/**
	 * Node used by the doubly linked list.
	 */
	private static class Node<E>
	{
		private E value;
		private Node<E> previous;
		private Node<E> next;

		private Node( E value )
		{
			this.value = value;
		}
	}

	/**
	 * Iterator that walks the list from head to tail.
	 */
	private class DLLIterator implements Iterator<E>
	{
		private Node<E> current = head;

		@Override
		public boolean hasNext()
		{
			return this.current != null;
		}

		@Override
		public E next() throws NoSuchElementException
		{
			if( !hasNext() )
			{
				throw new NoSuchElementException();
			}

			E value = this.current.value;
			this.current = this.current.next;
			return value;
		}
	}
}
