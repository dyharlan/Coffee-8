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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ColorManager {
    Color c;
    JColorChooser jcc;
    JDialog dialog;
    JPanel previewPanel;

    ColorManager(JFrame parentComponent, Color c) {
        this.c = c;
        jcc = new JColorChooser(this.c);
        previewPanel = new JPanel();
        previewPanel.add(new JLabel("This is the color"));
        previewPanel.setBackground(this.c);
        if (dialog == null) {
            dialog = JColorChooser.createDialog(
                    parentComponent, // parent comp
                    "Pick A Color", // dialog title
                    true, // modality
                    jcc,
                    new okListener(),
                    new cancelListener());
        }
        jcc.setPreviewPanel(previewPanel);
        jcc.getSelectionModel().addChangeListener(new okListener());
        dialog.addWindowListener( new WindowAdapter(){
                public void windowClosing(WindowEvent e){
                    dialog.dispose();
                }
            });
        dialog.setVisible(true);
        
    }
    
    public Color getColor(){
        return c;
    }
    
    public class okListener implements ActionListener,ChangeListener  {
        public void actionPerformed(ActionEvent e) {
            c = jcc.getColor();
        }
        
         public void stateChanged(ChangeEvent event) {
            Color newColor = jcc.getColor();
            previewPanel.setBackground(newColor);
        }
    }
    
    public class cancelListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(dialog != null)
                dialog.dispose();
        }
    }
    
    
    
    
    

}
