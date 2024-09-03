---

# 📚 Java Data Structures: Stacks

![Java Stacks](https://img.shields.io/badge/Java-Stacks-yellow?style=for-the-badge&logo=java)

## 📋 Table of Contents
- [Introduction to Stacks](#-introduction-to-stacks)
- [Stack Characteristics](#-stack-characteristics)
- [Basic Operations on Stacks](#-basic-operations-on-stacks)
  - [Push (Insertion)](#-push-insertion)
  - [Pop (Deletion)](#-pop-deletion)
  - [Peek (Top Element)](#-peek-top-element)
  - [isEmpty (Check if Stack is Empty)](#-isempty-check-if-stack-is-empty)
- [Implementing a Stack in Java](#-implementing-a-stack-in-java)
- [Applications of Stacks](#-applications-of-stacks)
- [Common Pitfalls and Best Practices](#-common-pitfalls-and-best-practices)
- [Conclusion](#-conclusion)

## 🌟 Introduction to Stacks

A **stack** is a linear data structure that follows the Last In, First Out (LIFO) principle. This means that the last element added to the stack will be the first one to be removed. Stacks are widely used in various algorithms and application areas, such as expression evaluation, backtracking problems, and function call management.

### Key Characteristics:
- **LIFO Structure**: The last element added is the first one removed.
- **Dynamic Size**: The stack can grow and shrink as elements are pushed and popped.
- **Simple Interface**: Operations are typically limited to push, pop, and peek.

## 🔗 Stack Characteristics

- **Push Operation**: Adds an element to the top of the stack.
- **Pop Operation**: Removes the top element from the stack.
- **Peek Operation**: Returns the top element without removing it.
- **isEmpty Operation**: Checks if the stack is empty.

## 🛠️ Basic Operations on Stacks

### ➕ Push (Insertion)

The `push` operation adds an element to the top of the stack.

```java
class Stack {
    private int maxSize;
    private int[] stackArray;
    private int top;

    public Stack(int size) {
        maxSize = size;
        stackArray = new int[maxSize];
        top = -1;
    }

    public void push(int value) {
        if (top == maxSize - 1) {
            System.out.println("Stack is full, cannot push value.");
        } else {
            stackArray[++top] = value;
        }
    }
}
```

### ➖ Pop (Deletion)

The `pop` operation removes and returns the top element of the stack.

```java
class Stack {
    private int maxSize;
    private int[] stackArray;
    private int top;

    public Stack(int size) {
        maxSize = size;
        stackArray = new int[maxSize];
        top = -1;
    }

    public int pop() {
        if (top == -1) {
            System.out.println("Stack is empty, cannot pop value.");
            return -1;
        } else {
            return stackArray[top--];
        }
    }
}
```

### 👁️ Peek (Top Element)

The `peek` operation returns the top element of the stack without removing it.

```java
class Stack {
    private int maxSize;
    private int[] stackArray;
    private int top;

    public Stack(int size) {
        maxSize = size;
        stackArray = new int[maxSize];
        top = -1;
    }

    public int peek() {
        if (top == -1) {
            System.out.println("Stack is empty, nothing to peek.");
            return -1;
        } else {
            return stackArray[top];
        }
    }
}
```

### ❓ isEmpty (Check if Stack is Empty)

The `isEmpty` operation checks if the stack is empty.

```java
class Stack {
    private int top;

    public Stack(int size) {
        top = -1;
    }

    public boolean isEmpty() {
        return top == -1;
    }
}
```

## 🚀 Implementing a Stack in Java

### Using an Array:

```java
class Stack {
    private int maxSize;
    private int[] stackArray;
    private int top;

    public Stack(int size) {
        maxSize = size;
        stackArray = new int[maxSize];
        top = -1;
    }

    public void push(int value) {
        if (top < maxSize - 1) {
            stackArray[++top] = value;
        } else {
            System.out.println("Stack Overflow: Cannot push, stack is full.");
        }
    }

    public int pop() {
        if (top >= 0) {
            return stackArray[top--];
        } else {
            System.out.println("Stack Underflow: Cannot pop, stack is empty.");
            return -1;
        }
    }

    public int peek() {
        if (top >= 0) {
            return stackArray[top];
        } else {
            System.out.println("Stack is empty.");
            return -1;
        }
    }

    public boolean isEmpty() {
        return top == -1;
    }
}
```

### Using Linked List:

```java
class Node {
    int value;
    Node nextNode;

    Node(int value) {
        this.value = value;
        this.nextNode = null;
    }
}

class Stack {
    private Node topNode;

    public Stack() {
        topNode = null;
    }

    public void push(int value) {
        Node newNode = new Node(value);
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

## 💡 Applications of Stacks

Stacks are commonly used in:
- **Expression Evaluation**: Used for evaluating postfix, prefix, and infix expressions.
- **Backtracking**: Used in algorithms that require exploring all possible options, such as solving mazes.
- **Function Call Management**: Stacks are used to manage function calls in programming languages.
- **Undo Mechanisms**: Used in applications that require undo functionality.

## ⚠️ Common Pitfalls and Best Practices

1. **🚫 Stack Overflow**: Be cautious of pushing too many elements onto the stack, especially with a fixed-size implementation.
2. **🔄 Stack Underflow**: Ensure you check if the stack is empty before performing a pop operation.
3. **Memory Management**: In linked list implementations, be mindful of memory usage, as each node consumes additional memory for the pointer/reference.
4. **Efficiency**: Choose the appropriate stack implementation (array vs linked list) based on your application's needs.

## 🎓 Conclusion

Stacks are a fundamental data structure in Java that offer a simple yet powerful way to manage data using the LIFO principle. Understanding how to implement and use stacks is crucial for solving a wide range of problems, from expression evaluation to managing function calls.

Key takeaways:
- **LIFO Principle**: Stacks operate on a Last In, First Out basis.
- **Basic Operations**: Master the push, pop, peek, and isEmpty operations.
- **Implementation**: Choose between array-based and linked list-based implementations based on your needs.

With practice, you'll be able to leverage stacks effectively in your Java applications! 💻🚀

---
