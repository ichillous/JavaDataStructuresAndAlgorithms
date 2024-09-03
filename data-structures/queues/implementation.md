
---

# 🛠️ Implementing a Queue in Java

![Java Queue Implementation](https://img.shields.io/badge/Java-Queue_Implementation-green?style=for-the-badge&logo=java)

## 📋 Table of Contents
- [Introduction](#-introduction)
- [Array-Based Queue Implementation](#-array-based-queue-implementation)
  - [Pros and Cons](#-pros-and-cons)
- [Linked List-Based Queue Implementation](#-linked-list-based-queue-implementation)
  - [Pros and Cons](#-pros-and-cons)
- [Circular Queue Implementation](#-circular-queue-implementation)
- [Priority Queue Implementation](#-priority-queue-implementation)
- [Choosing the Right Implementation](#-choosing-the-right-implementation)
- [Common Pitfalls](#-common-pitfalls)
- [Conclusion](#-conclusion)

## 🌟 Introduction

Queues can be implemented in multiple ways in Java, each offering different benefits depending on the specific use case. This document will explore the implementations using arrays, linked lists, and more specialized forms such as circular queues and priority queues.

## 📦 Array-Based Queue Implementation

An array-based queue uses a fixed-size array to store the queue elements. This approach is simple and efficient but comes with the limitation of a fixed size, which may lead to either wasted space or overflow.

### Basic Structure:
```java
class ArrayQueue {
    private int[] queueArray;
    private int frontIndex;
    private int rearIndex;
    private int maxSize;
    private int currentSize;

    public ArrayQueue(int size) {
        maxSize = size;
        queueArray = new int[maxSize];
        frontIndex = 0;
        rearIndex = -1;
        currentSize = 0;
    }

    public void enqueue(int value) {
        if (currentSize < maxSize) {
            rearIndex = (rearIndex + 1) % maxSize;
            queueArray[rearIndex] = value;
            currentSize++;
        } else {
            System.out.println("Queue Overflow: Cannot enqueue, queue is full.");
        }
    }

    public int dequeue() {
        if (currentSize > 0) {
            int dequeuedValue = queueArray[frontIndex];
            frontIndex = (frontIndex + 1) % maxSize;
            currentSize--;
            return dequeuedValue;
        } else {
            System.out.println("Queue Underflow: Cannot dequeue, queue is empty.");
            return -1;
        }
    }

    public int peek() {
        if (currentSize > 0) {
            return queueArray[frontIndex];
        } else {
            System.out.println("Queue is empty.");
            return -1;
        }
    }

    public boolean isEmpty() {
        return currentSize == 0;
    }
}
```

### 💡 Pros and Cons

#### Pros:
- **Fast Access**: Direct indexing of elements ensures O(1) time complexity for enqueue, dequeue, and peek operations.
- **Memory Efficiency**: No additional memory overhead for pointers, unlike linked lists.

#### Cons:
- **Fixed Size**: The queue size must be defined at the time of creation, which can lead to overflow if the capacity is exceeded.
- **No Dynamic Resizing**: Once the queue is full, it cannot accept more elements without resizing the array, which involves copying to a new array.

## 🔗 Linked List-Based Queue Implementation

A linked list-based queue uses nodes where each node points to the next node. This allows the queue to dynamically resize as needed, accommodating a varying number of elements without the fixed-size limitation of arrays.

### Basic Structure:
```java
class QueueNode {
    int value;
    QueueNode nextNode;

    QueueNode(int value) {
        this.value = value;
        this.nextNode = null;
    }
}

class LinkedListQueue {
    private QueueNode frontNode;
    private QueueNode rearNode;

    public LinkedListQueue() {
        frontNode = null;
        rearNode = null;
    }

    public void enqueue(int value) {
        QueueNode newNode = new QueueNode(value);
        if (rearNode == null) {
            frontNode = newNode;
            rearNode = newNode;
        } else {
            rearNode.nextNode = newNode;
            rearNode = newNode;
        }
    }

    public int dequeue() {
        if (frontNode == null) {
            System.out.println("Queue Underflow: Cannot dequeue, queue is empty.");
            return -1;
        } else {
            int dequeuedValue = frontNode.value;
            frontNode = frontNode.nextNode;
            if (frontNode == null) {
                rearNode = null;  // If the queue becomes empty, rear should also be null
            }
            return dequeuedValue;
        }
    }

    public int peek() {
        if (frontNode == null) {
            System.out.println("Queue is empty.");
            return -1;
        } else {
            return frontNode.value;
        }
    }

    public boolean isEmpty() {
        return frontNode == null;
    }
}
```

### 💡 Pros and Cons

#### Pros:
- **Dynamic Size**: The queue can grow and shrink as needed, with no fixed size limitations.
- **No Wasted Space**: Memory is allocated only as needed for each element.

#### Cons:
- **Memory Overhead**: Each element requires additional memory for the pointer to the next node.
- **Potentially Slower Access**: Access time can be slightly slower due to the need to follow pointers, although this is generally O(1).

## 🔄 Circular Queue Implementation

A circular queue is an enhancement of the regular array-based queue where the last position is connected back to the first position to form a circle. This helps in efficiently utilizing the array space.

### Basic Structure:
```java
class CircularQueue {
    private int[] queueArray;
    private int frontIndex;
    private int rearIndex;
    private int maxSize;
    private int currentSize;

    public CircularQueue(int size) {
        maxSize = size;
        queueArray = new int[maxSize];
        frontIndex = 0;
        rearIndex = -1;
        currentSize = 0;
    }

    public void enqueue(int value) {
        if (currentSize < maxSize) {
            rearIndex = (rearIndex + 1) % maxSize;
            queueArray[rearIndex] = value;
            currentSize++;
        } else {
            System.out.println("Queue Overflow: Cannot enqueue, queue is full.");
        }
    }

    public int dequeue() {
        if (currentSize > 0) {
            int dequeuedValue = queueArray[frontIndex];
            frontIndex = (frontIndex + 1) % maxSize;
            currentSize--;
            return dequeuedValue;
        } else {
            System.out.println("Queue Underflow: Cannot dequeue, queue is empty.");
            return -1;
        }
    }

    public int peek() {
        if (currentSize > 0) {
            return queueArray[frontIndex];
        } else {
            System.out.println("Queue is empty.");
            return -1;
        }
    }

    public boolean isEmpty() {
        return currentSize == 0;
    }
}
```

### 💡 Pros and Cons

#### Pros:
- **Efficient Space Utilization**: Circular queues efficiently utilize array space by reusing positions left vacant by dequeued elements.
- **Simple Implementation**: Easier to implement compared to dynamically resizing arrays.

#### Cons:
- **Complex Implementation Logic**: The circular nature can complicate the implementation logic, particularly in managing front and rear indices.

## 🏆 Priority Queue Implementation

A priority queue is a specialized type of queue where each element is associated with a priority. Elements with higher priority are dequeued before those with lower priority, regardless of their order in the queue.

### Basic Structure:
```java
import java.util.PriorityQueue;

class PriorityQueueExample {
    public static void main(String[] args) {
        PriorityQueue<Integer> pq = new PriorityQueue<>();

        pq.add(10);
        pq.add(5);
        pq.add(20);
        pq.add(15);

        while (!pq.isEmpty()) {
            System.out.println(pq.poll());
        }
    }
}
```

### 💡 Pros and Cons

#### Pros:
- **Priority-Based Dequeue**: Elements with higher priority are dequeued before others.
- **Dynamic Resizing**: Most priority queue implementations in Java, such as `PriorityQueue`, automatically resize as needed.

#### Cons:
- **Inconsistent Order**: Elements with the same priority may not maintain their original order.
- **Complexity**: Implementation can be more complex than simple FIFO queues.

## 🔍 Choosing the Right Implementation

### When to Use Array-Based Queue:
- When the maximum size of the queue is known in advance.
- When memory efficiency is crucial, and the overhead of pointers in a linked list is a concern.

### When to Use Linked List-Based Queue:
- When the size of the queue can vary significantly.
- When queue operations are performed frequently, and there's a risk of overflow with a fixed-size array.

### When to Use Circular Queue:
- When optimizing space usage is critical, especially in systems with limited memory.
- When a fixed-size queue is sufficient but you want to avoid the pitfalls of traditional array queues.

### When to Use Priority Queue:
- When elements need to be processed based on priority rather than order.
- In scenarios like task scheduling, where certain tasks must be executed before others.

## ⚠️ Common Pitfalls

1. **Queue Overflow in Array-Based Implementations**: Always ensure the queue does not exceed its fixed size, or consider

 using dynamic resizing.
2. **Null Pointer Exceptions in Linked List-Based Implementations**: Handle cases where the queue might be empty before dequeueing or peeking.
3. **Circular Queue Logic Errors**: Ensure correct management of the front and rear indices to avoid logic errors in circular queue implementations.
4. **Unintentional Priority Handling**: In priority queues, ensure that elements with the same priority are handled according to your specific requirements.

## 🎓 Conclusion

Each queue implementation in Java has its strengths and is suited for different scenarios. Array-based queues are simple and efficient but limited by their fixed size. Linked list-based queues offer dynamic sizing at the cost of memory overhead. Circular queues optimize space usage, while priority queues handle elements based on importance.

Key takeaways:
- **Array-Based Queues**: Best for fixed-size scenarios with known capacity.
- **Linked List-Based Queues**: Ideal for dynamic sizing and frequent enqueue/dequeue operations.
- **Circular Queues**: Efficient for fixed-size queues with optimal space utilization.
- **Priority Queues**: Perfect for scenarios where element order is based on priority.

With a solid understanding of these implementations, you'll be equipped to choose the right queue structure for your Java applications! 💻🚀

---
