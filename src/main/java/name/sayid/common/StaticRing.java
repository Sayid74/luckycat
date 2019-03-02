package name.sayid.common;

import java.util.AbstractList;
import java.util.List;

/**
 * This class describes a ring structure with static length.
 * The static ring is similar to link structure but when size
 * up to top, it overwrites the first and next overwrites second
 * and so on. So it writes the position is x mod l. The x value
 * is the order of writing times and x value is the static length.
 * @param <T>
 */
public class StaticRing<T> {
    private final T[] _items;
    private volatile long p = 0;

    /**
     * ItemCount identifies the ring size represents the item count the ring
     * can contain. So it can define a static ring structure.
     * @param itemCount It equals the static ring size the capacity
     */
    public StaticRing(int itemCount)
    {
        _items = (T[]) new Object[itemCount];
    }


    /**
     * Enums all items in the ring structure.
     * @return  Return a list instance contains all the items.
     */
    public List<T>
    allItems()
    {
        return new AbstractList<>() {
            @Override
            public int size() {
                return _items.length;
            }

            @Override
            public T get(int index) {
                return _items[index];
            }
        };
    }

    /**
     * Return the last writing position.
     * @return the last writing position. It init with zero just at after
     * constructed
     */
    public long
    footpointPosition()
    {
        return p;
    }

    /**
     * Add a item into ring.
     * @param item It should be added.
     */
    public synchronized void
    addItem(T item)
    {
        _items[(int)(p % (long)_items.length)] = item;
    }

}
