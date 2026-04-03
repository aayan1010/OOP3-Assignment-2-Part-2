package utilities;

import java.util.NoSuchElementException;

public class MyIterator<E> implements Iterator<E> {
    private MyArrayList<E> list;
    private int currentIndex;

    public MyIterator(MyArrayList<E> list) {
        this.list = list;
        this.currentIndex = 0;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < list.size();
    }

    @Override
    public E next() throws NoSuchElementException {
        if (!hasNext()) {
            throw new NoSuchElementException("No more elements in the list");
        }
        return list.get(currentIndex++);
    }
}
