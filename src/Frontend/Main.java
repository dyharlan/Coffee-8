/*
 * The MIT License
 *
 * Copyright 2023 dyhar.
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

import Frontend.Swing.SwingDisplay;
import java.io.*;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JOptionPane;
/*
 *
 * @author dyhar
 */
public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException  {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingDisplay d = null;
        File configFile = new File("config.cfg");
        if(!configFile.exists()){
            configFile.getParentFile().mkdirs();
            configFile.createNewFile();
            d = new SwingDisplay("Coffee-8 1.2.0");
            d.startApp();
        }else{
            try{
                d = new SwingDisplay("Coffee-8 1.2.0", configFile);
            }catch(Exception ex){
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "An error occured while starting Coffee-8! " + ex, "Error starting app", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
            d.startApp();
        }
    }
}
