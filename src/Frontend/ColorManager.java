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
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ColorManager {
    //Global Vars
    Color c;
    JColorChooser jcc;
    JDialog dialog;
    JPanel previewPanel;
    //Constructor
    ColorManager(JFrame parentComponent, Color c) {
        this.c = c;
        jcc = new JColorChooser(this.c);
        //create a simple preview panel
        previewPanel = new JPanel();
        previewPanel.add(new JLabel("This is the color"));
        previewPanel.setBackground(this.c);
        //Build a custom JColorChooser JDialog
        if (dialog == null) {
            dialog = JColorChooser.createDialog(
                    parentComponent, // parent comp
                    "Pick A Color", // dialog title
                    true, // modality
                    jcc,
                    new okListener(),
                    new cancelListener());
        }
        //set preview panel
        jcc.setPreviewPanel(previewPanel);
        //set a listener for the ok button
        jcc.getSelectionModel().addChangeListener(new okListener());
        dialog.addWindowListener( new WindowAdapter(){
                @Override
                public void windowClosing(WindowEvent e){
                    dialog.dispose();
                }
            });
        dialog.setVisible(true);
        
    }
    //return set color
    public Color getColor(){
        return c;
    }
    //listener for the ok button
    public class okListener implements ActionListener,ChangeListener  {
        @Override
        public void actionPerformed(ActionEvent e) {
            c = jcc.getColor();
        }
        
        @Override
        public void stateChanged(ChangeEvent event) {
            Color newColor = jcc.getColor();
            previewPanel.setBackground(newColor);
        }
    }
    // listener for the cancel button
    public class cancelListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(dialog != null)
                dialog.dispose();
        }
    }
    
    
    
    
    

}
