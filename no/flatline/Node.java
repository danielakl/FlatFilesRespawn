package no.flatline;

/**
 * Created by Joppe on 07.11.2017.
 */
class Node implements Comparable<Node>{
    private final char character;
    private final int freq;
    private final Node leftChild;
    private final Node rightChild;

    public Node(final char character, final int freq, final Node leftChild, final Node rightChild){
        this.character = character;
        this.freq = freq;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
    }

    boolean isLeaf(){
        return this.leftChild == null && this.rightChild == null;
    }

    @Override
    public int compareTo(final Node o){
        final int freqCompare = Integer.compare(this.freq, o.freq);
        if(freqCompare != 0)return freqCompare;
        return Integer.compare(this.character, o.character);
    }

    public int getFreq(){
        return this.freq;
    }

    public Node getLeftChild(){
        return leftChild;
    }

    public Node getRightChild(){
        return rightChild;
    }

    public char getCharacter(){
        return character;
    }
}
