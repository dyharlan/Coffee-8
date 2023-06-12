package Backend;


public class pStack {

    private int maxSize;
    private int[] stackArray;
    private int top;

    pStack(int s) {
        maxSize = s;
        stackArray = new int[maxSize];
        top = -1;
    }

    public void push(int num) {
        if (top >= stackArray.length - 1) {
            System.err.println("Stack Overflow");
        } else {
            top++;
            stackArray[top] = num;
        }
    }

    public int pop() {
        if (this.isEmpty() == true) {
            throw new IllegalStateException("Stack Underflow");
        } else {
            return stackArray[top--];
        }
    }

    public boolean isEmpty() {
        return (top == -1);

    }

    public int peek() {
        return stackArray[top];
    }

    public boolean isFull() {
        if (top == stackArray.length - 1) {
            return true;
        } else {
            return false;
        }
    }

    public void clear() {
        this.top = -1;
    }

}
