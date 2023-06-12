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
package Frontend;

/**
 *
 * @author dyharlan
 */
import java.io.*;
public class FileLoader {
    public static void main(String[] args) throws IOException {
        File rom = new File("C:\\Users\\dyhar\\Downloads\\Floppy Bird (by Micheal Wales)(2014).ch8");
        byte[] romArray;
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(rom)));
        romArray = new byte[(int)rom.length()];
//        in.read(romArray);
//        for(int i = 0; i < romArray.length; i++){
//            System.out.print(Integer.toHexString(romArray[i]) + "\t");
//            
//        }
        short x = 0;
        while((x = (short) in.read())!= -1){
            System.out.print(x + "\t");
        }
    }

}
