/*
 * ******************************************************************************
 *   Copyright 2014-2015 Spectra Logic Corporation. All Rights Reserved.
 *   Licensed under the Apache License, Version 2.0 (the "License"). You may not use
 *   this file except in compliance with the License. A copy of the License is located at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file.
 *   This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *   CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations under the License.
 * ****************************************************************************
 */

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
        public void remove() {
            throw new IllegalStateException("remove is not supported on this iterator");
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
