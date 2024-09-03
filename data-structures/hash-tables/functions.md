---

# 🛠️ Hash Table Functions in Java

![Java Hash Table Functions](https://img.shields.io/badge/Java-Hash_Table_Functions-green?style=for-the-badge&logo=java)

## 📋 Table of Contents
- [Introduction](#-introduction)
- [Hash Function](#-hash-function)
  - [Designing a Good Hash Function](#-designing-a-good-hash-function)
  - [Example of a Simple Hash Function](#-example-of-a-simple-hash-function)
- [Collision Resolution Techniques](#-collision-resolution-techniques)
  - [Chaining](#-chaining)
  - [Open Addressing](#-open-addressing)
- [Load Factor and Resizing](#-load-factor-and-resizing)
- [Common Hash Table Operations](#-common-hash-table-operations)
  - [Insertion](#-insertion)
  - [Search](#-search)
  - [Deletion](#-deletion)
- [Best Practices](#-best-practices)
- [Conclusion](#-conclusion)

## 🌟 Introduction

Hash tables are efficient data structures used to store and retrieve key-value pairs. The efficiency of a hash table largely depends on the hash function and collision resolution strategy employed. This document explores the key functions and concepts that make hash tables work effectively.

## 🔑 Hash Function

The **hash function** is the core component of a hash table. It takes a key as input and returns an integer, known as a hash code, which is used to determine the index in the hash table where the corresponding value is stored.

### Designing a Good Hash Function

A good hash function should:
- **Be Deterministic**: The same key should always produce the same hash code.
- **Distribute Uniformly**: Keys should be evenly distributed across the hash table to minimize collisions.
- **Be Fast to Compute**: The hash function should be quick to execute to maintain the overall efficiency of the hash table.
- **Minimize Collisions**: Although collisions are inevitable, a well-designed hash function reduces their likelihood.

### Example of a Simple Hash Function

Here’s an example of a simple hash function for strings:

```java
public int hashFunction(String key, int tableSize) {
    int hashCode = 0;
    for (int i = 0; i < key.length(); i++) {
        hashCode = (31 * hashCode + key.charAt(i)) % tableSize;
    }
    return Math.abs(hashCode);
}
```

In this example:
- The hash code is calculated by iterating over each character in the string and multiplying the current hash code by 31 (a common choice in hash functions) before adding the character's ASCII value.
- The result is then taken modulo `tableSize` to ensure the hash code fits within the bounds of the hash table array.

## 🚧 Collision Resolution Techniques

Collisions occur when two keys produce the same hash code. To handle collisions, hash tables use various resolution techniques.

### 1. Chaining

In chaining, each index of the hash table array contains a linked list (or another data structure) that stores all the key-value pairs hashing to the same index.

#### Example of Chaining:
```java
import java.util.LinkedList;

class HashTableWithChaining {
    private LinkedList<Entry>[] bucketArray;
    private int numBuckets;

    class Entry {
        String key;
        int value;

        Entry(String key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    public HashTableWithChaining(int size) {
        numBuckets = size;
        bucketArray = new LinkedList[numBuckets];
        for (int i = 0; i < numBuckets; i++) {
            bucketArray[i] = new LinkedList<>();
        }
    }

    private int hashFunction(String key) {
        int hashCode = key.hashCode();
        return Math.abs(hashCode % numBuckets);
    }

    public void put(String key, int value) {
        int bucketIndex = hashFunction(key);
        LinkedList<Entry> bucket = bucketArray[bucketIndex];

        for (Entry entry : bucket) {
            if (entry.key.equals(key)) {
                entry.value = value;
                return;
            }
        }

        bucket.add(new Entry(key, value));
    }

    public Integer get(String key) {
        int bucketIndex = hashFunction(key);
        LinkedList<Entry> bucket = bucketArray[bucketIndex];

        for (Entry entry : bucket) {
            if (entry.key.equals(key)) {
                return entry.value;
            }
        }
        return null;
    }
}
```

### 2. Open Addressing

In open addressing, when a collision occurs, the algorithm probes the hash table for the next available slot. Common strategies include:

- **Linear Probing**: Check the next slot sequentially.
- **Quadratic Probing**: Check slots at increasing intervals (e.g., 1, 4, 9, ...).
- **Double Hashing**: Use a second hash function to determine the probe sequence.

#### Example of Linear Probing:
```java
class HashTableWithOpenAddressing {
    private String[] keys;
    private int[] values;
    private int numBuckets;

    public HashTableWithOpenAddressing(int size) {
        numBuckets = size;
        keys = new String[numBuckets];
        values = new int[numBuckets];
    }

    private int hashFunction(String key) {
        int hashCode = key.hashCode();
        return Math.abs(hashCode % numBuckets);
    }

    public void put(String key, int value) {
        int bucketIndex = hashFunction(key);

        while (keys[bucketIndex] != null) {
            if (keys[bucketIndex].equals(key)) {
                values[bucketIndex] = value;
                return;
            }
            bucketIndex = (bucketIndex + 1) % numBuckets;
        }

        keys[bucketIndex] = key;
        values[bucketIndex] = value;
    }

    public Integer get(String key) {
        int bucketIndex = hashFunction(key);

        while (keys[bucketIndex] != null) {
            if (keys[bucketIndex].equals(key)) {
                return values[bucketIndex];
            }
            bucketIndex = (bucketIndex + 1) % numBuckets;
        }
        return null;
    }
}
```

## 📊 Load Factor and Resizing

The **load factor** of a hash table is the ratio of the number of elements to the number of buckets (array size). A high load factor increases the likelihood of collisions, while a low load factor can lead to inefficient use of memory.

### Managing Load Factor:
- **Resizing**: When the load factor exceeds a certain threshold (e.g., 0.75), the hash table should be resized to maintain performance.
- **Rehashing**: After resizing, all existing keys must be rehashed to fit the new bucket array size.

### Example:
```java
class ResizableHashTable {
    private String[] keys;
    private int[] values;
    private int numBuckets;
    private int size;

    public ResizableHashTable(int initialSize) {
        numBuckets = initialSize;
        keys = new String[numBuckets];
        values = new int[numBuckets];
        size = 0;
    }

    private int hashFunction(String key) {
        int hashCode = key.hashCode();
        return Math.abs(hashCode % numBuckets);
    }

    private void resize(int newSize) {
        String[] oldKeys = keys;
        int[] oldValues = values;
        numBuckets = newSize;
        keys = new String[numBuckets];
        values = new int[numBuckets];
        size = 0;

        for (int i = 0; i < oldKeys.length; i++) {
            if (oldKeys[i] != null) {
                put(oldKeys[i], oldValues[i]);
            }
        }
    }

    public void put(String key, int value) {
        if ((1.0 * size) / numBuckets >= 0.75) {
            resize(2 * numBuckets);
        }

        int bucketIndex = hashFunction(key);

        while (keys[bucketIndex] != null) {
            if (keys[bucketIndex].equals(key)) {
                values[bucketIndex] = value;
                return;
            }
            bucketIndex = (bucketIndex + 1) % numBuckets;
        }

        keys[bucketIndex] = key;
        values[bucketIndex] = value;
        size++;
    }

    public Integer get(String key) {
        int bucketIndex = hashFunction(key);

        while (keys[bucketIndex] != null) {
            if (keys[bucketIndex].equals(key)) {
                return values[bucketIndex];
            }
            bucketIndex = (bucketIndex + 1) % numBuckets;
        }
        return null;
    }
}
```

## 🛠️ Common Hash Table Operations

### ➕ Insertion

Insertion involves hashing the key, finding the appropriate index using the hash function, and placing the key-value pair in the hash table. If a collision occurs, the table uses its collision resolution strategy to find an alternative location.

### 🔍 Search

Search operations involve hashing the key to locate the correct bucket or slot. If the key is not found at the initial location, the collision resolution strategy is used to probe for the key.

### ➖ Deletion

Deletion requires locating the key using the same process as search, then removing the key-value pair from the hash table. If using open addressing, care must be taken to ensure the integrity of the probing sequence.

## 🏆 Best Practices

- **

Choose a Good Hash Function**: Ensure your hash function distributes keys evenly and minimizes collisions.
- **Monitor the Load Factor**: Keep an eye on the load factor and resize the hash table when necessary to maintain performance.
- **Handle Collisions Efficiently**: Implement robust collision resolution techniques that suit your use case.
- **Test with Real-World Data**: Ensure your hash table performs well under the conditions it will face in production.

## 🎓 Conclusion

Understanding the key functions and techniques behind hash tables is essential for leveraging their power in Java. A well-designed hash function, effective collision resolution strategy, and proper management of load factors are critical for maintaining the efficiency and performance of hash tables.

Key takeaways:
- **Hash Functions**: The cornerstone of hash tables; design them well.
- **Collision Resolution**: Crucial for handling the inevitable collisions in any hash table.
- **Load Factor**: Monitor and manage it to maintain optimal performance.

By mastering these functions, you'll be well-equipped to implement and maintain efficient hash tables in your Java applications! 💻🚀

---
