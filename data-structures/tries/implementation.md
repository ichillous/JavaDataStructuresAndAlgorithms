---

# 🛠️ Implementing Tries in Java

![Java Trie Implementation](https://img.shields.io/badge/Java-Trie_Implementation-orange?style=for-the-badge&logo=java)

## 📋 Table of Contents
- [Introduction](#-introduction)
- [TrieNode Class Implementation](#-trienode-class-implementation)
- [Trie Class Implementation](#-trie-class-implementation)
  - [Insertion](#-insertion)
  - [Search](#-search)
  - [Deletion](#-deletion)
- [Additional Trie Operations](#-additional-trie-operations)
  - [StartsWith (Prefix Search)](#-startswith-prefix-search)
  - [Word Suggestions (Autocomplete)](#-word-suggestions-autocomplete)
- [Testing the Trie Implementation](#-testing-the-trie-implementation)
- [Conclusion](#-conclusion)

## 🌟 Introduction

Implementing a Trie in Java involves creating a `TrieNode` class to represent each node in the Trie and a `Trie` class to manage the Trie operations such as insertion, search, and deletion. This implementation will cover the basic operations and extend to additional features like prefix searching and word suggestions.

## 🧩 TrieNode Class Implementation

The `TrieNode` class is the foundational element of the Trie, where each node represents a character of a word. It includes references to its children and a flag to indicate whether the node represents the end of a word.

### Implementation:
```java
class TrieNode {
    TrieNode[] children;
    boolean isEndOfWord;

    public TrieNode() {
        children = new TrieNode[26]; // Assuming only lowercase English letters
        isEndOfWord = false;
    }
}
```

### Explanation:
- **children**: An array of `TrieNode` references, each representing a potential character from 'a' to 'z'.
- **isEndOfWord**: A boolean flag that indicates if the current node marks the end of a word in the Trie.

## 🏗️ Trie Class Implementation

The `Trie` class manages the operations on the Trie, including inserting words, searching for words, and deleting words.

### ➕ Insertion

Inserting a word into the Trie involves traversing the Trie according to the characters in the word and creating new nodes as necessary.

#### Implementation:
```java
class Trie {
    private TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    public void insert(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            int index = c - 'a';
            if (node.children[index] == null) {
                node.children[index] = new TrieNode();
            }
            node = node.children[index];
        }
        node.isEndOfWord = true;
    }
}
```

### 🔍 Search

Searching for a word in the Trie involves traversing the Trie according to the characters in the word. If the traversal reaches a node marked as `isEndOfWord`, the word is present in the Trie.

#### Implementation:
```java
public boolean search(String word) {
    TrieNode node = root;
    for (char c : word.toCharArray()) {
        int index = c - 'a';
        if (node.children[index] == null) {
            return false;
        }
        node = node.children[index];
    }
    return node.isEndOfWord;
}
```

### ➖ Deletion

Deleting a word from the Trie is more complex as it may require removing nodes and ensuring that prefixes shared by other words remain intact.

#### Implementation:
```java
public boolean delete(String word) {
    return delete(root, word, 0);
}

private boolean delete(TrieNode node, String word, int depth) {
    if (node == null) {
        return false;
    }

    if (depth == word.length()) {
        if (!node.isEndOfWord) {
            return false;
        }
        node.isEndOfWord = false;
        return node.children.length == 0;
    }

    int index = word.charAt(depth) - 'a';
    if (delete(node.children[index], word, depth + 1)) {
        node.children[index] = null;
        return !node.isEndOfWord && node.children.length == 0;
    }

    return false;
}
```

## 🔄 Additional Trie Operations

### 🔍 StartsWith (Prefix Search)

This operation checks if there is any word in the Trie that starts with a given prefix. It is similar to the search operation but does not require the node to be marked as the end of a word.

#### Implementation:
```java
public boolean startsWith(String prefix) {
    TrieNode node = root;
    for (char c : prefix.toCharArray()) {
        int index = c - 'a';
        if (node.children[index] == null) {
            return false;
        }
        node = node.children[index];
    }
    return true;
}
```

### 💡 Word Suggestions (Autocomplete)

Autocomplete suggests words based on a given prefix. This involves finding the node corresponding to the last character of the prefix and performing a DFS to find all words that start with that prefix.

#### Implementation:
```java
public List<String> wordSuggestions(String prefix) {
    List<String> results = new ArrayList<>();
    TrieNode node = root;

    for (char c : prefix.toCharArray()) {
        int index = c - 'a';
        if (node.children[index] == null) {
            return results; // Prefix not found
        }
        node = node.children[index];
    }

    findAllWords(node, results, new StringBuilder(prefix));
    return results;
}

private void findAllWords(TrieNode node, List<String> results, StringBuilder currentWord) {
    if (node.isEndOfWord) {
        results.add(currentWord.toString());
    }

    for (int i = 0; i < 26; i++) {
        if (node.children[i] != null) {
            currentWord.append((char) (i + 'a'));
            findAllWords(node.children[i], results, currentWord);
            currentWord.deleteCharAt(currentWord.length() - 1);
        }
    }
}
```

## 🧪 Testing the Trie Implementation

### Example Usage:
```java
public class TrieExample {
    public static void main(String[] args) {
        Trie trie = new Trie();

        trie.insert("cat");
        trie.insert("car");
        trie.insert("dog");
        trie.insert("door");

        System.out.println(trie.search("cat"));  // Output: true
        System.out.println(trie.search("can"));  // Output: false

        System.out.println(trie.startsWith("do")); // Output: true
        System.out.println(trie.startsWith("ca")); // Output: true
        System.out.println(trie.startsWith("bat")); // Output: false

        System.out.println(trie.wordSuggestions("do")); // Output: [dog, door]

        trie.delete("dog");
        System.out.println(trie.search("dog"));  // Output: false
    }
}
```

### Testing:
- **Insert**: Test inserting multiple words with common prefixes.
- **Search**: Test searching for both present and absent words.
- **StartsWith**: Test prefix searching for valid and invalid prefixes.
- **Word Suggestions**: Test autocomplete functionality for different prefixes.
- **Delete**: Test deletion to ensure other words with shared prefixes are unaffected.

## 🎓 Conclusion

Implementing a Trie in Java involves creating a flexible and efficient structure for handling a large set of strings, especially when prefix operations are critical. By extending the basic Trie operations with features like prefix searching and autocomplete, you can build powerful applications that require efficient string management.

Key takeaways:
- **Efficient String Operations**: Tries offer efficient insertion, search, and deletion operations.
- **Prefix Matching**: Tries excel at operations involving prefixes, making them ideal for autocomplete and search applications.
- **Memory Management**: Be mindful of the memory usage when dealing with large datasets.

By mastering Trie implementation, you can efficiently solve complex string-related problems in your Java applications! 💻🚀

---
