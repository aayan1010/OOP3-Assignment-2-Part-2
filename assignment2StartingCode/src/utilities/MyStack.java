package utilities;

import java.util.EmptyStackException;

public class MyStack<E> implements StackADT<E> {

	private MyArrayList<E> list;

    public MyStack() {
        list = new MyArrayList<>();
    }

    @Override
    public void push(E toAdd) throws NullPointerException {
        if (toAdd == null) throw new NullPointerException("Cannot push a null item");
        list.add(0, toAdd); 
    }

    @Override
    public E pop() throws EmptyStackException {
        if (isEmpty()) throw new EmptyStackException();
        return list.remove(0);
    }

    @Override
    public E peek() throws EmptyStackException {
        if (isEmpty()) throw new EmptyStackException();
        return list.get(0);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public E[] toArray(E[] holder) throws NullPointerException {
        return list.toArray(holder);
    }

    @Override
    public boolean contains(E toFind) throws NullPointerException {
        return list.contains(toFind);
    }

    @Override
    public int search(E toFind) {
        if (toFind == null) return -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(toFind)) {
                return i + 1; 
            }
        }
        return -1; 
    }

    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }

    @Override
    public boolean equals(StackADT<E> that) {
        if (that == null || this.size() != that.size()) {
            return false;
        }
        Iterator<E> thisIter = this.iterator();
        Iterator<E> thatIter = that.iterator();
        
        while (thisIter.hasNext() && thatIter.hasNext()) {
            if (!thisIter.next().equals(thatIter.next())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int size() {
        return list.size();
    }
    
    @Override
    public boolean stackOverflow() {
        return false;
    }
}
