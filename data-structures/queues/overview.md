---

# 📚 Java Data Structures: Queues

![Java Queues](https://img.shields.io/badge/Java-Queues-orange?style=for-the-badge&logo=java)

## 📋 Table of Contents
- [Introduction to Queues](#-introduction-to-queues)
- [Queue Characteristics](#-queue-characteristics)
- [Types of Queues](#-types-of-queues)
  - [Simple Queue](#-simple-queue)
  - [Circular Queue](#-circular-queue)
  - [Priority Queue](#-priority-queue)
  - [Deque (Double-Ended Queue)](#-deque-double-ended-queue)
- [Basic Operations on Queues](#-basic-operations-on-queues)
  - [Enqueue (Insertion)](#-enqueue-insertion)
  - [Dequeue (Deletion)](#-dequeue-deletion)
  - [Peek (Front Element)](#-peek-front-element)
  - [isEmpty (Check if Queue is Empty)](#-isempty-check-if-queue-is-empty)
- [Implementing a Queue in Java](#-implementing-a-queue-in-java)
- [Applications of Queues](#-applications-of-queues)
- [Common Pitfalls and Best Practices](#-common-pitfalls-and-best-practices)
- [Conclusion](#-conclusion)

## 🌟 Introduction to Queues

A **queue** is a linear data structure that follows the First In, First Out (FIFO) principle. This means that the first element added to the queue will be the first one to be removed. Queues are widely used in various algorithms and application areas, such as task scheduling, buffering, and handling asynchronous data.

### Key Characteristics:
- **FIFO Structure**: The first element added is the first one removed.
- **Dynamic Size**: The queue can grow and shrink as elements are enqueued and dequeued.
- **Simple Interface**: Operations typically include enqueue, dequeue, and peek.

## 🔗 Queue Characteristics

- **Enqueue Operation**: Adds an element to the end of the queue.
- **Dequeue Operation**: Removes the front element from the queue.
- **Peek Operation**: Returns the front element without removing it.
- **isEmpty Operation**: Checks if the queue is empty.

## 🛠️ Types of Queues

### 1. Simple Queue

A simple queue, also known as a linear queue, follows the basic FIFO principle, where elements are added at the rear and removed from the front.

### 2. Circular Queue

In a circular queue, the last position is connected back to the first position to make a circle. This approach optimizes space utilization by allowing the queue to reuse the vacant positions left by dequeued elements.

### 3. Priority Queue

A priority queue is a type of queue where each element is associated with a priority, and elements are dequeued based on their priority rather than their position in the queue. The element with the highest priority is dequeued first.

### 4. Deque (Double-Ended Queue)

A deque allows elements to be added or removed from both ends, making it more flexible than a simple queue. Deques can function as both queues and stacks.

## 🔧 Basic Operations on Queues

### ➕ Enqueue (Insertion)

The `enqueue` operation adds an element to the rear of the queue.

```java
class Queue {
    private int[] queueArray;
    private int front;
    private int rear;
    private int maxSize;
    private int currentSize;

    public Queue(int size) {
        maxSize = size;
        queueArray = new int[maxSize];
        front = 0;
        rear = -1;
        currentSize = 0;
    }

    public void enqueue(int value) {
        if (currentSize == maxSize) {
            System.out.println("Queue is full, cannot enqueue value.");
        } else {
            rear = (rear + 1) % maxSize;
            queueArray[rear] = value;
            currentSize++;
        }
    }
}
```

### ➖ Dequeue (Deletion)

The `dequeue` operation removes and returns the front element of the queue.

```java
class Queue {
    private int[] queueArray;
    private int front;
    private int rear;
    private int maxSize;
    private int currentSize;

    public Queue(int size) {
        maxSize = size;
        queueArray = new int[maxSize];
        front = 0;
        rear = -1;
        currentSize = 0;
    }

    public int dequeue() {
        if (currentSize == 0) {
            System.out.println("Queue is empty, cannot dequeue value.");
            return -1;
        } else {
            int dequeuedValue = queueArray[front];
            front = (front + 1) % maxSize;
            currentSize--;
            return dequeuedValue;
        }
    }
}
```

### 👁️ Peek (Front Element)

The `peek` operation returns the front element of the queue without removing it.

```java
class Queue {
    private int[] queueArray;
    private int front;
    private int rear;
    private int maxSize;
    private int currentSize;

    public Queue(int size) {
        maxSize = size;
        queueArray = new int[maxSize];
        front = 0;
        rear = -1;
        currentSize = 0;
    }

    public int peek() {
        if (currentSize == 0) {
            System.out.println("Queue is empty, nothing to peek.");
            return -1;
        } else {
            return queueArray[front];
        }
    }
}
```

### ❓ isEmpty (Check if Queue is Empty)

The `isEmpty` operation checks if the queue is empty.

```java
class Queue {
    private int currentSize;

    public Queue(int size) {
        currentSize = 0;
    }

    public boolean isEmpty() {
        return currentSize == 0;
    }
}
```

## 🚀 Implementing a Queue in Java

### Using an Array:

```java
class ArrayQueue {
    private int[] queueArray;
    private int front;
    private int rear;
    private int maxSize;
    private int currentSize;

    public ArrayQueue(int size) {
        maxSize = size;
        queueArray = new int[maxSize];
        front = 0;
        rear = -1;
        currentSize = 0;
    }

    public void enqueue(int value) {
        if (currentSize < maxSize) {
            rear = (rear + 1) % maxSize;
            queueArray[rear] = value;
            currentSize++;
        } else {
            System.out.println("Queue Overflow: Cannot enqueue, queue is full.");
        }
    }

    public int dequeue() {
        if (currentSize > 0) {
            int dequeuedValue = queueArray[front];
            front = (front + 1) % maxSize;
            currentSize--;
            return dequeuedValue;
        } else {
            System.out.println("Queue Underflow: Cannot dequeue, queue is empty.");
            return -1;
        }
    }

    public int peek() {
        if (currentSize > 0) {
            return queueArray[front];
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

### Using Linked List:

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
                rearNode = null;
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

## 💡 Applications of Queues

Queues are commonly used in:
- **Task Scheduling**: Queues are used to manage tasks in operating systems, such as CPU scheduling.
- **Data Buffers**: Queues are used in buffering data streams for IO operations.
- **Breadth-First Search (BFS)**: Queues are fundamental in graph traversal algorithms like BFS.
- **Asynchronous Data Handling**: Used in handling asynchronous data flows in various applications.

## ⚠️ Common Pitfalls and Best Practices

1. **🚫 Queue Overflow in Array-Based Implementations**: Always check for overflow conditions before enqueueing elements.
2. **🔄 Queue Underflow**: Ensure you check if the queue is empty before performing a dequeue operation.
3. **Circular Queue**:

 Implement circular queues to optimize space usage in array-based implementations.
4. **Efficiency**: Choose the appropriate queue implementation (array vs linked list) based on your application's needs.

## 🎓 Conclusion

Queues are a fundamental data structure in Java that provide a simple and efficient way to manage data using the FIFO principle. Understanding how to implement and use queues is crucial for solving a wide range of problems, from task scheduling to data buffering.

Key takeaways:
- **FIFO Principle**: Queues operate on a First In, First Out basis.
- **Basic Operations**: Master the enqueue, dequeue, peek, and isEmpty operations.
- **Implementation**: Choose between array-based and linked list-based implementations based on your needs.

With practice, you'll be able to leverage queues effectively in your Java applications! 💻🚀

---
