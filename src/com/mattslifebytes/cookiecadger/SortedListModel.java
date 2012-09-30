package com.mattslifebytes.cookiecadger;

import java.util.Collection;
import java.util.TreeSet;

import javax.swing.AbstractListModel;

/**
 * A AbstractListModel that should stay sorted because it is stored in a sorted TreeSet.
 */
@SuppressWarnings("serial")
public class SortedListModel extends AbstractListModel<ListItem> {
	
	private TreeSet<ListItem> model;
	
	/**
	 * Default constructor that initializes the sorted TreeSet
	 */
	public SortedListModel() {
		model = new TreeSet<ListItem>();
	}
	
	public SortedListModel(ListItem[] items) {
		model = new TreeSet<ListItem>();
		for(ListItem item : items) {
			model.add(item);
		}
	}
	
	/**
	 * Returns the value at the specified index.
	 * @param index the request index
	 * @return the value at index
	 */
	@Override
	public ListItem getElementAt(int index) {
		return (ListItem) model.toArray()[index];
	}

	/**
	 * Returns the number of elements in the set.
	 * @return the number of elements in the set
	 */
	@Override
	public int getSize() {
		return model.size();
	}
	
	/**
	 * Adds the specified element to this ListModel if it is not already present. More formally, adds the specified element e to this ListModel if the ListModel contains no element e2 such that (e==null ? e2==null : e.equals(e2)). If this ListModel already contains the element, the call leaves the ListModel unchanged and returns false.
	 * @param list the element to be added to the ListModel
	 * @return true if the ListModel did not already contain the specified element, false otherwise
	 */
	public boolean add(ListItem list) {
		return model.add(list);
	}
	
	/**
	 * Adds all of the elements in the specified collection to the ListModel.
	 * @param c collection containing elements to be added to the set.
	 * @return true if this ListModel changed as a result of the call, false otherwise
	 */
	public boolean addAll(Collection<? extends ListItem> c) {
		return model.addAll(c);
	}

	/**
	 * Removes all of the elements from the ListModel. The ListModel will be empty after this call returns.
	 */
	public void clear() {
		model.clear();
	}
	
	/**
	 * Returns true if this ListModel contains the specified element. More formally, returns true if and only if this ListModel contains an element e such that (o==null ? e==null : o.equals(e)). 
	 * @param list ListItem to be checked for containment in this ListModel.
	 * @return true if the ListModel contains the specified ListItem, false otherwise
	 */
	public boolean contains(ListItem list) {
		return model.contains(list);
	}

	/**
	 * Returns true if the ListModel contains no elements.
	 * @return true if the ListModel contains no elements, false otherwise
	 */
	public boolean isEmpty() {
		return model.isEmpty();
	}
	
	/**
	 * Removes the specified element from this ListModel if it is present. More formally, removes an element e such that (o==null ? e==null : o.equals(e)), if this ListModel contains such an element. Returns true if this ListModel contained the element (or equivalently, if this ListModel changed as a result of the call). (This ListModel will not contain the element once the call returns.)
	 * @param list ListItem to be removed from this ListModel, if present.
	 * @return true if this set contained the specified element, false otherwise
	 */
	public boolean remove(ListItem list) {
		return model.remove(list);
	}
	
	/**
	 * Returns a sorted array containing all of the elements in this collection. 
	 * @return a sorted array containing all of the elements in this collection.
	 */
	public ListItem[] toArray() {
		return model.toArray(new ListItem[model.size()]);
	}
}
