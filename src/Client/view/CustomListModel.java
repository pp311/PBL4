package Client.view;

import java.util.List;

import javax.swing.AbstractListModel;

public class CustomListModel<E> extends AbstractListModel<E> {
    protected List<E> list;
 
    public CustomListModel(List<E> list) {
        this.list = list;
    }
 
    public void addElement(E element) {
        list.add(element);
        int index = list.size();
        fireContentsChanged(element, index, index);
    }
 
    public void fireDataChanged() {
        int index = list.size();
        fireContentsChanged(list.get(index - 1), index, index);
    }
 
    public int getSize() {
        return list.size();
    }
 
    public E getElementAt(int index) {
        return list.get(index);
    }
    public void removeElementAt(int index) {
    	list.remove(index);
    	fireContentsChanged(index, index, index);
    }
}
