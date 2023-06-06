/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
        previewPanel.add(new JLabel("tubol"));
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
