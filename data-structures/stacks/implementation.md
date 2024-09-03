
---

# 🛠️ Implementing a Stack in Java

![Java Stack Implementation](https://img.shields.io/badge/Java-Stack_Implementation-blue?style=for-the-badge&logo=java)

## 📋 Table of Contents
- [Introduction](#-introduction)
- [Array-Based Stack Implementation](#-array-based-stack-implementation)
  - [Pros and Cons](#-pros-and-cons)
- [Linked List-Based Stack Implementation](#-linked-list-based-stack-implementation)
  - [Pros and Cons](#-pros-and-cons)
- [Choosing the Right Implementation](#-choosing-the-right-implementation)
- [Common Pitfalls](#-common-pitfalls)
- [Conclusion](#-conclusion)

## 🌟 Introduction

In Java, stacks can be implemented in multiple ways, each with its advantages and disadvantages. The two most common approaches are using an array or a linked list. This document explores both implementations, highlighting their differences and when each might be the better choice.

## 📦 Array-Based Stack Implementation

An array-based stack uses a fixed-size array to store stack elements. This approach is straightforward and efficient for most use cases where the maximum size of the stack is known in advance.

### Basic Structure:
```java
class ArrayStack {
    private int[] stackArray;
    private int topIndex;

    public ArrayStack(int size) {
        stackArray = new int[size];
        topIndex = -1;
    }

    public void push(int value) {
        if (topIndex < stackArray.length - 1) {
            stackArray[++topIndex] = value;
        } else {
            System.out.println("Stack Overflow: Cannot push, stack is full.");
        }
    }

    public int pop() {
        if (topIndex >= 0) {
            return stackArray[topIndex--];
        } else {
            System.out.println("Stack Underflow: Cannot pop, stack is empty.");
            return -1;
        }
    }

    public int peek() {
        if (topIndex >= 0) {
            return stackArray[topIndex];
        } else {
            System.out.println("Stack is empty.");
            return -1;
        }
    }

    public boolean isEmpty() {
        return topIndex == -1;
    }
}
```

### 💡 Pros and Cons

#### Pros:
- **Fast Access**: Direct indexing of elements ensures O(1) time complexity for push, pop, and peek operations.
- **Memory Efficiency**: No additional memory overhead for pointers, unlike linked lists.

#### Cons:
- **Fixed Size**: The stack size must be defined at the time of creation, which may lead to either wasted space or stack overflow.
- **No Dynamic Resizing**: If the stack needs to grow beyond its initial capacity, it cannot do so without reallocating and copying to a larger array.

## 🔗 Linked List-Based Stack Implementation

A linked list-based stack uses nodes, where each node points to the next node. This implementation allows for a dynamically sized stack, where elements can be added or removed as needed.

### Basic Structure:
```java
class StackNode {
    int value;
    StackNode nextNode;

    StackNode(int value) {
        this.value = value;
        this.nextNode = null;
    }
}

class LinkedListStack {
    private StackNode topNode;

    public LinkedListStack() {
        topNode = null;
    }

    public void push(int value) {
        StackNode newNode = new StackNode(value);
        newNode.nextNode = topNode;
        topNode = newNode;
    }

    public int pop() {
        if (topNode == null) {
            System.out.println("Stack Underflow: Cannot pop, stack is empty.");
            return -1;
        } else {
            int poppedValue = topNode.value;
            topNode = topNode.nextNode;
            return poppedValue;
        }
    }

    public int peek() {
        if (topNode == null) {
            System.out.println("Stack is empty.");
            return -1;
        } else {
            return topNode.value;
        }
    }

    public boolean isEmpty() {
        return topNode == null;
    }
}
```

### 💡 Pros and Cons

#### Pros:
- **Dynamic Size**: The stack can grow and shrink as needed, with no fixed size limitations.
- **No Wasted Space**: Memory is allocated only as needed for each element.

#### Cons:
- **Memory Overhead**: Each element requires additional memory for the pointer to the next node.
- **Potentially Slower Access**: Access time can be slightly slower due to the need to follow pointers, although this is generally O(1).

## 🔍 Choosing the Right Implementation

### When to Use Array-Based Stack:
- When the maximum size of the stack is known in advance.
- When memory efficiency is crucial, and the overhead of pointers in a linked list is a concern.
- When consistent O(1) access time is critical, and stack resizing is not an issue.

### When to Use Linked List-Based Stack:
- When the size of the stack can vary significantly, and a dynamic size is needed.
- When stack operations are performed frequently, and there is a risk of stack overflow with a fixed-size array.
- When memory consumption is less of a concern, and the overhead of additional pointers is acceptable.

## ⚠️ Common Pitfalls

1. **Stack Overflow in Array-Based Implementations**: Always be cautious of pushing elements onto a full stack. Consider implementing dynamic resizing if needed.
2. **Null Pointer Exceptions in Linked List-Based Implementations**: Ensure that your stack operations handle cases where the stack is empty.
3. **Memory Management**: In linked list-based stacks, be mindful of memory usage, especially in environments with limited resources.

## 🎓 Conclusion

Both array-based and linked list-based stack implementations have their own strengths and weaknesses. Understanding the specific requirements of your application will help you choose the right implementation. Array-based stacks are ideal for fixed-size scenarios with a known capacity, while linked list-based stacks offer flexibility and dynamic sizing.

Key takeaways:
- **Array-Based Stacks**: Best for fixed-size, memory-efficient scenarios.
- **Linked List-Based Stacks**: Best for dynamic sizing and frequent push/pop operations.

By mastering both implementations, you'll be well-equipped to handle a variety of stack-related problems in Java! 💻🚀

---
