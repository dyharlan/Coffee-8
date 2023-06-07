/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Frontend;
import Backend.Chip8SOC;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
/**
 *
 * @author dyhar
 */
public class CycleManager {
    Chip8SOC chip8CPU;
    JDialog dialog;
    JFrame parentFrame;
    JPanel hintPanel,inputPanel,buttonPanel;
    JButton okButton,cancelButton;
    JLabel errorLabel;
    JTextField inputField;
    public CycleManager(Chip8SOC chip8CPU, JFrame f){
        this.chip8CPU = chip8CPU;
        parentFrame = f;
        dialog = new JDialog(f,"Set CPU Cycle Count", true);
        
        dialog.addWindowListener( new WindowAdapter(){
                public void windowClosing(WindowEvent e){
                    dialog.dispose();
                }
            });
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
       
        
        okButton.addActionListener((e)->{
            try{
                chip8CPU.setCycles(Integer.parseInt(inputField.getText()));
                dialog.dispose();
            }catch(IllegalArgumentException iae){
                JOptionPane.showMessageDialog(f, "Please enter a valid number and try again.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
            
        });
        cancelButton.addActionListener((e)->{
            dialog.dispose();
        });
         dialog.getRootPane().setDefaultButton(okButton);
    }
    
    public void showDialog(){
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setVisible(true);
    }
}
