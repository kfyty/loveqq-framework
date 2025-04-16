package com.kfyty.loveqq.framework.core.lang.util;

import com.kfyty.loveqq.framework.core.support.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;

/**
 * 描述: 链式数组，适用于大量随机插入随机删除
 *
 * @author kfyty725
 * @date 2022/11/19 9:52
 * @email kfyty725@hotmail.com
 */
public class LinkedArrayList<E> extends AbstractList<E> implements List<E>, RandomAccess, Cloneable, Serializable {
    /**
     * 每个数组最小容量
     */
    private static final int MIN_CAPACITY = 16;

    /**
     * 每个数组的容量
     * 该参数对性能影响非常大，建议预计总元素数 / 200
     */
    private final int capacity;

    /**
     * 集合元素数量
     */
    private transient int size;

    /**
     * 头节点
     */
    private transient LinkedArrayNode head;

    /**
     * 尾节点
     */
    private transient LinkedArrayNode tail;

    public LinkedArrayList() {
        this(MIN_CAPACITY);
    }

    public LinkedArrayList(int capacity) {
        super();
        this.size = 0;
        this.capacity = capacity;
        this.head = this.tail = new LinkedArrayNode(capacity);
    }

    public LinkedArrayList(Collection<E> collection) {
        this(Math.max(collection.size() / 200, MIN_CAPACITY), collection);
    }

    public LinkedArrayList(int capacity, Collection<E> collection) {
        this(capacity);
        this.addAll(collection);
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iter();
    }

    @Override
    public boolean add(E e) {
        this.add(this.size, e);
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return this.addAll(this.size, c);
    }

    @Override
    public E get(int index) {
        Pair<Integer, LinkedArrayNode> resolved = this.resolveNode(index);
        return resolved == null ? null : resolved.getValue().get(resolved.getKey());
    }

    @Override
    public E set(int index, E element) {
        Pair<Integer, LinkedArrayNode> resolved = this.resolveNode(index);
        return resolved == null ? null : resolved.getValue().set(resolved.getKey(), element);
    }

    @Override
    public void add(int index, E element) {
        Pair<Integer, LinkedArrayNode> resolved = this.resolveNode(index);
        resolved.getValue().add(resolved.getKey(), element);
        this.size++;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        Pair<Integer, LinkedArrayNode> target = this.resolveNode(index);
        if (target == null) {
            return false;
        }
        target.getValue().addAll(target.getKey(), c);
        return true;
    }

    @Override
    public E remove(int index) {
        Pair<Integer, LinkedArrayNode> resolved = this.resolveNode(index);
        E remove = resolved == null ? null : resolved.getValue().remove(resolved.getKey());
        this.size--;
        return remove;
    }

    @Override
    public int indexOf(Object o) {
        int index = -1;
        for (E e : this) {
            index++;
            if (o == null && e == null || o != null && o.equals(e)) {
                return index;
            }
        }
        return -1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int lastIndexOf(Object o) {
        int index = this.size();
        LinkedArrayNode node = this.tail;
        while (node != null) {
            for (int i = node.nodeSize - 1; i > -1; i--) {
                index--;
                E e = (E) node.element[i];
                if (o == null && e == null || o != null && o.equals(e)) {
                    return index;
                }
            }
            node = node.prior;
        }
        return -1;
    }

    @Override
    public void clear() {
        LinkedArrayNode node = this.head;
        while (node != null) {
            node.nodeSize = 0;
            Arrays.fill(node.element, null);
            LinkedArrayNode next = node.next;
            node.next = node.prior = null;
            node = next;
        }
        this.size = 0;
        this.tail = this.head;
    }

    @Override
    public Object[] toArray() {
        return this.toArray(new Object[0]);
    }

    @Override
    @SuppressWarnings({"unchecked", "SuspiciousSystemArraycopy"})
    public <T> T[] toArray(T[] a) {
        int index = 0;
        LinkedArrayNode node = this.head;
        T[] retValue = a.length >= this.size ? a : (T[]) Array.newInstance(a.getClass().getComponentType(), this.size);
        while (node != null) {
            System.arraycopy(node.element, 0, retValue, index, node.nodeSize);
            index += node.nodeSize;
            node = node.next;
        }
        return retValue;
    }

    @Override
    public LinkedArrayList<E> clone() {
        return new LinkedArrayList<>(this.capacity, this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof List)) {
            return false;
        }
        Iterator<?> self = this.iterator();
        Iterator<?> other = ((List<?>) o).iterator();
        while (self.hasNext() && other.hasNext()) {
            if (!Objects.equals(self.next(), other.next())) {
                return false;
            }
        }
        return !(self.hasNext() || other.hasNext());
    }

    private Pair<Integer, LinkedArrayNode> resolveNode(int index) {
        return index < (this.size >> 2) ? this.findNode(index) : this.findNodeReverse(index);
    }

    private Pair<Integer, LinkedArrayNode> findNode(int index) {
        LinkedArrayNode node = this.head;
        while (node != null && index >= node.nodeSize) {
            index -= node.nodeSize;
            node = node.next;
        }
        return node == null ? null : new Pair<>(index, node);
    }

    private Pair<Integer, LinkedArrayNode> findNodeReverse(int index) {
        index = this.size - 1 - index;
        LinkedArrayNode node = this.tail;
        while (node != null && index >= node.nodeSize) {
            index -= node.nodeSize;
            node = node.prior;
        }
        return node == null ? null : new Pair<>(node.nodeSize - 1 - index, node);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.size = 0;
        this.head = this.tail = new LinkedArrayNode(this.capacity);
        for (int i = 0, count = s.readInt(); i < count; i++) {
            this.add((E) s.readObject());
        }
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeInt(this.size);
        LinkedArrayNode node = this.head;
        while (node != null) {
            for (int i = 0; i < node.nodeSize; i++) {
                s.writeObject(node.element[i]);
            }
            node = node.next;
        }
    }

    /**
     * 迭代器
     */
    private class Iter implements Iterator<E> {
        /**
         * 当前游标
         */
        int cursor;

        /**
         * 当前的 size
         */
        int curSize;

        /**
         * 当前的节点游标
         */
        int curNodeCursor;

        /**
         * 当前的节点
         */
        LinkedArrayNode curNode;

        Iter() {
            this.curSize = LinkedArrayList.this.size;
            this.curNode = LinkedArrayList.this.head;
        }

        @Override
        public boolean hasNext() {
            return cursor < LinkedArrayList.this.size;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            if (curSize != LinkedArrayList.this.size) {
                throw new ConcurrentModificationException();
            }

            // 由于 remove() 操作，可能有的节点容量为 0，因此需要找到下一个容量大于 0 的节点
            while (curNode != null && curNodeCursor >= curNode.nodeSize) {
                curNodeCursor = 0;
                curNode = curNode.next;
            }
            cursor++;
            return curNode == null ? null : (E) curNode.element[curNodeCursor++];
        }

        @Override
        public void remove() {
            this.cursor--;
            this.curNode.remove(--curNodeCursor);
            this.curSize--;
            LinkedArrayList.this.size--;
        }
    }

    /**
     * 链式数组节点
     */
    private class LinkedArrayNode {
        /**
         * 元素
         */
        Object[] element;

        /**
         * 节点大小
         */
        int nodeSize;

        /**
         * 前驱节点
         */
        LinkedArrayNode prior;

        /**
         * 后继节点
         */
        LinkedArrayNode next;

        LinkedArrayNode(int capacity) {
            this.nodeSize = 0;
            this.element = new Object[capacity];
        }

        LinkedArrayNode(LinkedArrayNode prior) {
            this(capacity);
            this.prior = prior;
        }

        @SuppressWarnings("unchecked")
        protected E get(int index) {
            this.checkRange(index, size() - 1);
            return (E) this.element[index];
        }

        @SuppressWarnings("unchecked")
        protected E set(int index, E e) {
            this.checkRange(index, size() - 1);
            E result = (E) element[index];
            element[index] = e;
            return result;
        }

        @SuppressWarnings("unchecked")
        protected E remove(int index) {
            this.checkRange(index, size() - 1);
            E result = (E) element[index];
            if (index < nodeSize - 1) {
                System.arraycopy(element, index + 1, element, index, nodeSize - 1 - index);
            }
            element[--nodeSize] = null;
            if (this.nodeSize == 0) {
                this.removeNode(this);
            } else {
                this.shiftNextIfNecessary();
            }
            return result;
        }

        /**
         * 添加元素到指定索引
         * 1、索引范围检查
         * 2、如果插入索引位置大于等于节点大小，则尝试添加到下一个节点
         * 3、如果节点大小大于等于最大容量，则创建新的节点
         * 4、如果索引位置已有数据，则将该索引及其后面的元素后移
         * 5、将元素插入该索引位置
         *
         * @param index 索引
         * @param e     元素
         */
        protected void add(int index, E e) {
            this.checkRange(index);
            if (index >= nodeSize) {
                this.addToNextIfNecessary(index, e);
                return;
            }
            if (this.nodeSize >= capacity) {
                this.newNextIfNecessary();
            }
            if (this.nodeSize > 0 && index < this.nodeSize && index < capacity - 1) {
                int tempSize = this.nodeSize == capacity ? this.nodeSize - 1 : this.nodeSize;
                System.arraycopy(element, index, element, index + 1, tempSize - index);
            }
            this.element[index] = e;
            if (this.nodeSize < capacity) {
                this.nodeSize++;
            }
        }

        /**
         * 批量添加
         * 首先将当前节点插入位置之后的元素临时保存起来，然后使用要插入的数据将当前节点填满
         * 然后直接创建新的节点，并将插入的数据批量复制到新节点中，再将新节点插入到链式数组中
         * 此时，最后一个新创建的节点可能刚好已满，也可能未满，因此先将第一个临时保存的数据填满最后一个新节点
         * 最后再将剩余的临时保存的元素顺序插入到链式数组中
         *
         * @param index 索引位置
         * @param c     集合元素
         */
        @SuppressWarnings("unchecked")
        protected void addAll(int index, Collection<? extends E> c) {
            int i = 0;
            this.checkRange(index);
            Object[] array = c.toArray();
            Object[] toMove = new Object[i < this.nodeSize ? this.nodeSize - index : 0];

            // 先填满当前节点
            int toFill = Math.min(array.length, LinkedArrayList.this.capacity - index);
            System.arraycopy(this.element, index, toMove, 0, toMove.length);
            System.arraycopy(array, 0, this.element, index, toFill);
            i += toFill;
            this.nodeSize = index + toFill;

            // 创建新节点并批量插入数据
            LinkedArrayNode self = this;
            LinkedArrayNode node = null;
            while (i < array.length) {
                int toCopy = Math.min(array.length - i, LinkedArrayList.this.capacity);
                node = new LinkedArrayNode(self);
                System.arraycopy(array, i, node.element, 0, toCopy);
                i += toCopy;
                node.nodeSize = toCopy;
                node.next = self.next;
                if (self.next != null) {
                    self.next.prior = node;
                }
                self.next = node;
                self = node;
            }

            // 当前节点已经足够插入数据了
            if (node == null) {
                node = self;
            }

            // 将需要移动的先填满最后新增的节点
            int toCopy = Math.min(toMove.length, LinkedArrayList.this.capacity - node.nodeSize);
            System.arraycopy(toMove, 0, node.element, node.nodeSize, toCopy);
            node.nodeSize += toCopy;

            // 剩下的顺序插入，这里更新一下 size，不然范围检查报错
            LinkedArrayList.this.size += array.length - (toMove.length - toCopy);
            if (node.next == null) {
                LinkedArrayList.this.tail = node;
            }
            for (int j = toCopy, k = 0; j < toMove.length; j++, k++) {
                node.add(node.nodeSize + k, (E) toMove[j]);
                LinkedArrayList.this.size++;
            }
        }

        /**
         * 索引范围检查
         *
         * @param index 索引
         */
        private void checkRange(int index) {
            this.checkRange(index, size());
        }

        /**
         * 索引范围检查
         *
         * @param index 索引
         * @param limit 最大限制
         */
        private void checkRange(int index, int limit) {
            if (index < 0 || index > limit) {
                throw new ArrayIndexOutOfBoundsException("Size: " + size() + ", index: " + index);
            }
        }

        /**
         * 移除节点
         *
         * @param node 节点
         */
        private void removeNode(LinkedArrayNode node) {
            if (node.prior == null) {
                if (node.next == null) {
                    return;
                }
                node.next.prior = null;
                LinkedArrayList.this.head = node.next;
                return;
            }
            if (node.next == null) {
                LinkedArrayList.this.tail = node.prior;
                node.prior.next = null;
                return;
            }
            node.prior.next = node.next;
            node.next.prior = node.prior;
        }

        /**
         * 当前节点大小大于等于最大容量，尝试创建新的节点
         * 1、如果 next 为空，则直接创建新的数组节点
         * 2、如果 next 不为空并且容量未满，则将 next 的全部元素后移一位
         * 3、如果 next 不为空并且容量已满，则创建新的数组节点插入其后
         * 4、将当前元素的最后一个元素移动到 next 的第一位元素
         */
        private void newNextIfNecessary() {
            if (this.next == null) {
                LinkedArrayList.this.tail = this.next = new LinkedArrayNode(this);
            } else if (next.nodeSize < capacity) {
                System.arraycopy(next.element, 0, next.element, 1, next.nodeSize);
            } else {
                LinkedArrayNode temp = new LinkedArrayNode(this);
                temp.next = this.next;
                this.next.prior = temp;
                this.next = temp;
            }
            this.next.element[0] = this.element[nodeSize - 1];
            this.next.nodeSize++;
        }

        /**
         * 如果插入索引位置大于等于节点大小，则尝试添加到下一个节点
         * 1、如果当前节点未满并且索引在最大容量范围内，则尝试左移元素，并直接返回
         * 2、否则如果 next 为空，则创建新的数组节点
         * 3、调用 next.add() 插入元素
         *
         * @param index 索引
         * @param e     元素
         */
        private void addToNextIfNecessary(int index, E e) {
            if (this.nodeSize < capacity && index < capacity) {
                this.shiftLeft(index, e);
                return;
            }
            if (this.next == null) {
                LinkedArrayList.this.tail = this.next = new LinkedArrayNode(this);
            }
            this.next.add(index - nodeSize, e);
        }

        /**
         * 尝试左移元素
         * 1、如果 next 为空，则无需左移，直接添加即可
         * 2、计算需要左移的元素数量，如果 next 节点大小小于左移的元素数量，则无需左移，直接调用 next.add() 添加即可
         * 3、需要左移，执行左移，并将该元素插入当前节点最后的位置
         *
         * @param index 索引
         * @param e     元素
         */
        private void shiftLeft(int index, E e) {
            if (this.next == null) {
                this.element[nodeSize++] = e;
                return;
            }
            int move = index - nodeSize;
            if (next.nodeSize < move) {
                this.next.add(index - nodeSize, e);
                return;
            }
            this.doShiftLeft(move);
            this.element[nodeSize++] = e;
        }

        /**
         * 执行左移，即将 next 的前 move 个元素移动到当前节点
         * 1、如果移动后的 next 节点大小小于等于 move，则说明 next 节点已空，直接移除 next 节点
         * 2、否则将 next 节点剩余的元素移动到数组头部
         *
         * @param move 需要移动的元素数量
         */
        private void doShiftLeft(int move) {
            System.arraycopy(next.element, 0, this.element, this.nodeSize, move);
            this.nodeSize += move;
            if (next.nodeSize <= move) {
                Arrays.fill(next.element, 0, next.nodeSize, null);
                this.next = this.next.next;
                this.next.prior = this;
                return;
            }
            System.arraycopy(next.element, move, next.element, 0, next.nodeSize - move);
            Arrays.fill(next.element, next.nodeSize - move, next.nodeSize, null);
            next.nodeSize -= move;
        }

        /**
         * 删除时，尝试移动下一个节点的元素到当前节点，避免碎片化过多
         * 仅当当前节点的空余元素大于等于下一个节点的元素时，执行移动，并删除下一个节点，避免移动元素过多
         */
        private void shiftNextIfNecessary() {
            if (this.next == null || LinkedArrayList.this.capacity - this.nodeSize < this.next.nodeSize) {
                return;
            }
            System.arraycopy(next.element, 0, this.element, this.nodeSize, next.nodeSize);
            Arrays.fill(next.element, 0, next.nodeSize, null);
            this.nodeSize += this.next.nodeSize;
            this.removeNode(this.next);
        }
    }
}
