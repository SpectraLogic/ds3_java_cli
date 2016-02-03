package com.spectralogic.ds3cli.util;

import java.util.Iterator;

public class FilteringIterable<E> implements Iterable<E> {

    private final Iterable<E> wrappedIterable;
    private final FilterFunction<E> filterFunction;

    public FilteringIterable(final Iterable<E> wrappedIterable, final FilterFunction<E> filterFunction) {
        this.wrappedIterable = wrappedIterable;
        this.filterFunction = filterFunction;
    }

    @Override
    public Iterator<E> iterator() {
        return new FilteringIterator<>(wrappedIterable.iterator(), filterFunction);
    }

    private class FilteringIterator<T> implements Iterator<T> {

        private final Iterator<T> wrappedIterator;
        private final FilterFunction<T> filterFunction;
        private T nextItem;

        private FilteringIterator(final Iterator<T> wrappedIterator, final FilterFunction<T> filterFunction) {
            this.wrappedIterator = wrappedIterator;
            this.filterFunction = filterFunction;
            this.nextItem = getNext();
        }

        @Override
        public boolean hasNext() {
            return nextItem != null;
        }

        @Override
        public T next() {
            final T next = this.nextItem;
            this.nextItem = getNext();
            return next;
        }

        private T getNext() {
            while (true) {
                if (wrappedIterator.hasNext()) {
                    final T next = wrappedIterator.next();
                    if (filterFunction.filter(next)) {
                        continue;
                    }

                    return next;
                }
                return null;
            }
        }
    }

    public interface FilterFunction<E> {
        boolean filter(final E item);
    }
}
