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
package nl.esciencecenter.xenon.adaptors.filesystems;

import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.filesystems.PathAttributes;

import java.util.Iterator;
import java.util.Stack;
import java.util.function.Function;

// use this to implement recursive listing in case the system
// does not support recursive listing or does not implement recursive listing
// as we expect (we also list directories)
public class RecursiveListIterator implements Iterator<PathAttributes> {

    final Stack<Iterator<PathAttributes>> stack;
    final Function<Path,Iterator<PathAttributes>> list;

    public RecursiveListIterator(Function<Path,Iterator<PathAttributes>> list, Path path) {
        stack = new Stack<>();
        this.list = list;
        Iterator<PathAttributes> it = list.apply(path);
        stack.push(it);
    }

    void popEmpties() {
        while (!stack.empty()) {
            if (!stack.peek().hasNext()) {
                stack.pop();
            } else {
                return;
            }
        }
    }

    @Override
    public boolean hasNext() {
        popEmpties();
        return !stack.isEmpty();
    }

    @Override
    public PathAttributes next() {
        PathAttributes nxt = stack.peek().next();
        if (nxt.isDirectory()) {
            stack.push(list.apply(nxt.getPath()));
        }
        popEmpties();
        return nxt;
    }
}
