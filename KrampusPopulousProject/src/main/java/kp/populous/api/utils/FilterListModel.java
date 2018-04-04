/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.utils;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import javax.swing.AbstractListModel;

public final class FilterListModel<E> extends AbstractListModel<E>
{
    private final ArrayList<E> elements = new ArrayList<>();
    private final ArrayList<E> filtered = new ArrayList<>();

    @Override
    public int getSize() {
        return filtered.size();
    }

    @Override
    public E getElementAt(int index) {
        return filtered.get(index);
    }
    
    public synchronized final void filter(String text)
    {
        filtered.clear();
        filtered.ensureCapacity(elements.size());
        if(text == null || text.isEmpty())
            filtered.addAll(elements);
        else
        {
            Filter<E> filter = new Filter<>(text);
            elements.stream().filter(filter).forEach(filtered::add);
        }
        fireContentsChanged(this, 0, getSize());
    }
    
    public final void unfilter() { filter(null); }
    
    public final void sort()
    {
        elements.sort((e0, e1) -> String.CASE_INSENSITIVE_ORDER.compare(e0.toString(), e1.toString()));
    }

    public int size() { return elements.size(); }

    public boolean isEmpty() { return elements.isEmpty(); }

    public boolean contains(E elem) { return elements.contains(elem); }

    public int indexOf(Object elem) { return elements.indexOf(elem); }

    public int lastIndexOf(Object elem) { return elements.lastIndexOf(elem); }

    public E elementAt(int index) { return elements.get(index); }

    public E firstElement() { return elements.get(0); }

    public E lastElement() { return elements.get(elements.size() - 1); }

    public void setElementAt(E element, int index) {
        elements.set(index, element);
        fireContentsChanged(this, index, index);
    }

    public void removeElementAt(int index) {
        elements.remove(index);
        fireIntervalRemoved(this, index, index);
    }

    public void insertElementAt(E element, int index) {
        elements.add(index, element);
        fireIntervalAdded(this, index, index);
    }

    public void addElement(E element) {
        int index = elements.size();
        elements.add(element);
        fireIntervalAdded(this, index, index);
    }

    public boolean removeElement(E obj) {
        int index = indexOf(obj);
        boolean rv = elements.remove(obj);
        if (index >= 0) {
            fireIntervalRemoved(this, index, index);
        }
        return rv;
    }

    public void removeAllElements() {
        int index1 = elements.size()-1;
        elements.clear();
        if (index1 >= 0) {
            fireIntervalRemoved(this, 0, index1);
        }
    }

    @Override
    public String toString() {
        return elements.toString();
    }
   
    public Object[] toArray() {
        return elements.toArray();
    }

    public E get(int index) {
        return elements.get(index);
    }

    public E set(int index, E element) {
        E rv = elements.get(index);
        elements.set(index, element);
        fireContentsChanged(this, index, index);
        return rv;
    }

    public void add(int index, E element) {
        elements.add(index, element);
        fireIntervalAdded(this, index, index);
    }

    public E remove(int index) {
        E rv = elements.get(index);
        elements.remove(index);
        fireIntervalRemoved(this, index, index);
        return rv;
    }

    public void clear()
    {
        int index1 = elements.size()-1;
        elements.clear();
        if (index1 >= 0) {
            fireIntervalRemoved(this, 0, index1);
        }
    }

    public void removeRange(int fromIndex, int toIndex) {
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex must be <= toIndex");
        }
        for(int i = toIndex; i >= fromIndex; i--) {
            elements.remove(i);
        }
        fireIntervalRemoved(this, fromIndex, toIndex);
    }
    
    private static final class Filter<E> implements Predicate<E>
    {
        private final Pattern pattern;
        
        private Filter(String inputText) { this.pattern = Pattern.compile(Pattern.quote(Objects.requireNonNull(inputText)), Pattern.CASE_INSENSITIVE); }

        @Override
        public final boolean test(E element)
        {
            String text = element.toString();
            return pattern.matcher(text).find();
        }
    }
}
