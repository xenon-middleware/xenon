/*
 * Copyright 2013 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.esciencecenter.xenon.engine.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public class ImmutableArray<T> implements Iterable<T> {

    class ImmutableArrayIterator implements Iterator<T> { 
     
        private int index = 0;

        @Override
        public boolean hasNext() {
            return index < data.length;
        }

        @Override
        public T next() {
            
            if (index >= data.length) { 
                throw new NoSuchElementException("No more elements in ImmutableArray!");
            }
            
            return data[index++];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Cannot remove element from ImmutableArray!");
        }
    }
    
    private final T [] data;
    
    @SuppressWarnings("unchecked")
    public ImmutableArray(T ... elements) { 
        if (elements == null) {
            data = (T[]) new Object[0];
        } else { 
            data = Arrays.copyOf(elements, elements.length);
        }
    }
    
    public T[] asArray() {
        return Arrays.copyOf(data, data.length);        
    }

    public int length() {
        return data.length;
    }
    
    public T get(int index) { 
        return data[index];
    }

    @Override
    public Iterator<T> iterator() {
        return new ImmutableArrayIterator();
    }

    @Override
    public String toString() {
        return Arrays.toString(data);
    }
}
