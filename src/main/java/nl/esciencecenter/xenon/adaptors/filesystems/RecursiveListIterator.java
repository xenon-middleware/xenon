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