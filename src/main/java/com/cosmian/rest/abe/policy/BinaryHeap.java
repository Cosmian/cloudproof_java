package com.cosmian.rest.abe.policy;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Heap is basically a tree based data structure. It has nodes. Node comprises
 * of certain elements. Every node contains one element.
 * 
 * Nodes may have children. If in case there are no children, it is called a
 * Leaf.
 * 
 * There are two rules to be followed:
 * 
 * - The value of every node must be less or equal to all the values stored in
 * its children. - It has the least possible height.
 * 
 * Heaps are extremely efficient in extracting the least or greatest element.
 * 
 * Check"https://www.edureka.co/blog/binary-heap-in-java/" for details
 */
public class BinaryHeap {

    private static final int d = 2;
    private int[] heap;
    private int heapSize;

    /**
     * This will initialize our heap with default size.
     * 
     * @param capacity the capacity of the heap
     */
    public BinaryHeap(int capacity) {
        heapSize = 0;
        heap = new int[capacity + 1];
        Arrays.fill(heap, -1);

    }

    /**
     * This will check if the heap is empty or not Complexity: O(1)
     * 
     * @return true if empty
     */
    public boolean isEmpty() {
        return heapSize == 0;
    }

    /**
     * This will check if the heap is full or not Complexity: O(1)
     * 
     * @return true if full
     */
    public boolean isFull() {
        return heapSize == heap.length;
    }

    private int parent(int i) {
        return (i - 1) / d;
    }

    private int kthChild(int i, int k) {
        return d * i + k;
    }

    /**
     * This will insert new element in to heap Complexity: O(log N) As worst case
     * scenario, we need to traverse till the root
     * 
     * @param x the value to insert
     */
    public void insert(int x) {
        if (isFull())
            throw new NoSuchElementException("Heap is full, No space to insert new element");
        heap[heapSize++] = x;
        heapifyUp(heapSize - 1);
    }

    /**
     * This will delete element at index x Complexity: O(log N)
     * 
     * @param x the value to delete
     * @return the key
     * 
     */
    public int delete(int x) {
        if (isEmpty())
            throw new NoSuchElementException("Heap is empty, No element to delete");
        int key = heap[x];
        heap[x] = heap[heapSize - 1];
        heapSize--;
        heapifyDown(x);
        return key;
    }

    /**
     * This method used to maintain the heap property while inserting an element.
     * 
     */
    private void heapifyUp(int i) {
        int temp = heap[i];
        while (i > 0 && temp > heap[parent(i)]) {
            heap[i] = heap[parent(i)];
            i = parent(i);
        }
        heap[i] = temp;
    }

    /**
     * This method used to maintain the heap property while deleting an element.
     * 
     */
    private void heapifyDown(int i) {
        int child;
        int temp = heap[i];
        while (kthChild(i, 1) < heapSize) {
            child = maxChild(i);
            if (temp < heap[child]) {
                heap[i] = heap[child];
            } else
                break;
            i = child;
        }
        heap[i] = temp;
    }

    private int maxChild(int i) {
        int leftChild = kthChild(i, 1);
        int rightChild = kthChild(i, 2);
        return heap[leftChild] > heap[rightChild] ? leftChild : rightChild;
    }

    /**
     * This method used to print all element of the heap
     * 
     */
    public void printHeap() {
        System.out.print("nHeap = ");
        for (int i = 0; i < heapSize; i++)
            System.out.print(heap[i] + " ");
        System.out.println();
    }

    /**
     * This method returns the max element of the heap. complexity: O(1)
     * 
     * @return the max element of the heap
     */
    public int findMax() {
        if (isEmpty())
            throw new NoSuchElementException("Heap is empty.");
        return heap[0];
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof BinaryHeap)) {
            return false;
        }
        BinaryHeap binaryHeap = (BinaryHeap) o;
        return Objects.equals(heap, binaryHeap.heap) && heapSize == binaryHeap.heapSize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(heap, heapSize);
    }

    @Override
    public String toString() {
        return "{" + " heap='" + Arrays.toString(heap) + "'" + ", heapSize='" + heapSize + "'" + "}";
    }

    public static void main(String[] args) {
        BinaryHeap maxHeap = new BinaryHeap(10);
        maxHeap.insert(10);
        maxHeap.insert(4);
        maxHeap.insert(9);
        maxHeap.insert(1);
        maxHeap.insert(7);
        maxHeap.insert(5);
        maxHeap.insert(3);

        maxHeap.printHeap();
        maxHeap.delete(5);
        maxHeap.printHeap();
    }
}