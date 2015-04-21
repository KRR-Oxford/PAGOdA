package uk.ac.ox.cs.pagoda.model;

public class Trie {
	
	private TrieNode root = new TrieNode("");
	private Status findStatus;  

	public void insert(String key, AnswerTerm term) {
		findStatus.lastNode.insert(key, findStatus.lastIndex, term); 
	}
	
	public AnswerTerm find(String key) {
		findStatus = root.find(key, 0);
		return findStatus.term; 
	}
	
}

class TrieNode {
	
	String relative;
	TrieNode[] children = new TrieNode[256];

	public TrieNode(String s) {
		relative = s;
	}

	public void insert(String key, int index, AnswerTerm term) {
		int nextChar = (int) key.charAt(index); 
		if (children[nextChar] == null) {
			children[nextChar] = new TrieLeaf(key.substring(index), term);
		}
		else {
			TrieNode next = children[nextChar];
			int len = next.isPrefixOf(key, index); 
			if (len == next.relative.length())
				insert(key, index + len, term);
			else {
				TrieNode newNext = new TrieNode(next.relative.substring(0, len)); 
				next.relative = next.relative.substring(len);
				children[nextChar] = newNext; 
				newNext.children[(int) next.relative.charAt(0)] = next; 
				insert(key, index + len, term);
			}
		}
	}

	private int isPrefixOf(String key, int index) {
		int i = 0; 
		for (int j = index; i < relative.length() && j < key.length(); ++i, ++j)
			if (relative.charAt(i) != relative.charAt(j)) 
				break; 
		return i;
	}

	public Status find(String key, int index) {
		TrieNode next = children[(int) key.charAt(index)]; 
		if (next == null)
			return new Status(this, index, null);
		
		if (next instanceof TrieLeaf) 
			if (next.relative.equals(key.substring(index))) 
				return new Status(this, index, ((TrieLeaf) next).term); 
			else {
				return new Status(this, index, null);
			}
		return find(key, index + next.relative.length());
	}
	
}

class TrieLeaf extends TrieNode {
	
	AnswerTerm term; 

	public TrieLeaf(String s, AnswerTerm term) {
		super(s);
		this.term = term; 
	}

}

class Status {
	
	AnswerTerm term; 
	TrieNode lastNode; 
	int lastIndex;
	
	public Status(TrieNode trieNode, int index, AnswerTerm term2) {
		this.lastNode = trieNode; 
		this.lastIndex = index; 
		this.term = term2; 
	}

}