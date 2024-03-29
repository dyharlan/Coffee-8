/*
 * The MIT License
 *
 * Copyright 2023 dyharlan.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 *
 * @author dyharlan
 */
package Backend;
public class pStack {

    private final int MAX_SIZE;
    private int[] stackArray;
    private int top;

    pStack(int s) {
        MAX_SIZE = s;
        stackArray = new int[MAX_SIZE];
        top = -1;
    }

    public void push(int num) {
        if (top >= stackArray.length - 1) {
            //throw new IllegalStateException("Stack Overflow");
            System.err.println("Stack Overflow");

        } else {
            top++;
            stackArray[top] = num;
        }
    }

    public int pop() {
        if (this.isEmpty() == true) {
            //throw new IllegalStateException("Stack Underflow");
            System.err.println("Stack Underflow");
            return -1;
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
        return top == stackArray.length - 1;
    }

    public void clear() {
        this.top = -1;
    }
    
    public void zeroOut(){
        this.clear();
        for(int i = 0;i < stackArray.length;i++){
            stackArray[i] = 0;
        }
    }

}
