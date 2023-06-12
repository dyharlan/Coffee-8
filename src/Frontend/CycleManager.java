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
import Backend.Chip8SOC;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
/**
 *
 * @author dyharlan
 */
public class CycleManager {
    //global variables
    Chip8SOC chip8CPU;
    JDialog dialog;
    JFrame parentFrame;
    JPanel hintPanel,inputPanel,buttonPanel;
    JButton okButton,cancelButton;
    JLabel errorLabel;
    JTextField inputField;
    //constructor
    public CycleManager(Chip8SOC chip8CPU, JFrame f){
        this.chip8CPU = chip8CPU;
        parentFrame = f;
        dialog = new JDialog(f,"Set CPU Cycle Count", true);
        
        dialog.addWindowListener( new WindowAdapter(){
                public void windowClosing(WindowEvent e){
                    dialog.dispose();
                }
            });
        /*
        * Layout of the cycle manager:
        * hintPanel: tells what the cycle setting does
        * inputPanel: contains the input field for setting the cycles
        * buttonPanel: contains the buttons for doing actions
        */
        dialog.setLayout(new GridLayout(3,1));
        hintPanel = new JPanel();
        hintPanel.setLayout(new FlowLayout());
            hintPanel.add(new JLabel("<html><style>h4 {text-align: center;}</style><h4>Setting this value will determine<br>how many CPU instructions will run every 16ms.</h4></html>"));
        dialog.add(hintPanel);
        inputPanel = new JPanel(new FlowLayout());
        dialog.add(inputPanel);
            inputPanel.add(new JLabel("Cycles: "));
            inputField = new JTextField();
            inputField.setText(Integer.toString(chip8CPU.getCycles()));
            inputField.setPreferredSize(new Dimension(75,25));
            inputPanel.add(inputField);   
        buttonPanel = new JPanel(new FlowLayout());
        dialog.add(buttonPanel);
            okButton = new JButton("Set");
            cancelButton = new JButton("Cancel");
            
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
       
        //action listener for the ok button
        okButton.addActionListener((e)->{
            try{
                chip8CPU.setCycles(Integer.parseInt(inputField.getText()));
                dialog.dispose();
            }catch(IllegalArgumentException iae){
                JOptionPane.showMessageDialog(f, "Please enter a valid number and try again.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
            
        });
        //action listener for the cancel button
        cancelButton.addActionListener((e)->{
            dialog.dispose();
        });
         dialog.getRootPane().setDefaultButton(okButton);
    }
    //show dialog method
    public void showDialog(){
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setVisible(true);
    }
}
