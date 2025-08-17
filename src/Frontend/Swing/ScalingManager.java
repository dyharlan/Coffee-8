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
package Frontend.Swing;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
/**
 *
 * @author dyharlan
 */
public class ScalingManager {
    ButtonListener b;
    int currentScalingMultiplier;
    int newScalingMultiplier;
    JDialog dialog;
    SwingDisplay s;
    JPanel hintPanel,inputPanel,buttonPanel;
    JButton upButton,downButton,okButton,cancelButton;
    JLabel errorLabel;
    JLabel inputField;
    public ScalingManager(SwingDisplay s){
        this.s = s;
        b = new ButtonListener();
        currentScalingMultiplier = s.LOWRES_SCALE_FACTOR;
        newScalingMultiplier = currentScalingMultiplier;
        dialog = new JDialog(s.f,"Set Video Scaling", true);
        
        dialog.addWindowListener(new WindowAdapter() {
            int sizeX = 0;
            int sizeY = 0;
            @Override
            public void windowClosing(WindowEvent e) {
                s.LOWRES_SCALE_FACTOR = currentScalingMultiplier;
                s.HIRES_SCALE_FACTOR = currentScalingMultiplier / 2;
                s.gamePanel.revalidate();
                s.gamePanel.repaint();
                if (s.getHiRes()) {
                    sizeX = 128 * s.HIRES_SCALE_FACTOR;
                    sizeY = 64 * s.HIRES_SCALE_FACTOR;
                } else {
                    sizeX = 64 * s.LOWRES_SCALE_FACTOR;
                    sizeY = 32 * s.LOWRES_SCALE_FACTOR;
                }
                s.gamePanel.setPreferredSize(new Dimension(sizeX, sizeY));
                s.f.pack();
                dialog.dispose();
                s.hiResViewWidth = s.IMGWIDTH * s.HIRES_SCALE_FACTOR;
                s.hiResViewHeight = s.IMGHEIGHT * s.HIRES_SCALE_FACTOR;
                s.lowResViewWidth = s.IMGWIDTH * s.LOWRES_SCALE_FACTOR;
                s.lowResViewHeight = s.IMGHEIGHT * s.LOWRES_SCALE_FACTOR;
            }
        });
        dialog.setLayout(new GridLayout(3,1));
        hintPanel = new JPanel();
        hintPanel.setLayout(new FlowLayout());
            hintPanel.add(new JLabel("<html><style>h2 {text-align: center;}</style><h4>Setting this will change the scaling<br>multiplier for the Chip-8 display.</h2></html>"));
        dialog.add(hintPanel);
        inputPanel = new JPanel(new FlowLayout());
        dialog.add(inputPanel);
            inputPanel.add(new JLabel("<html><style>h4 {text-align: center;}</style><h4>Scaling Multiplier: </h4></html>"));
            inputField = new JLabel("<html><h4>"+ Integer.toString(newScalingMultiplier) +"</h4></html>", SwingConstants.CENTER);
            inputField.setPreferredSize(new Dimension(75,25));
            inputField.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            inputPanel.add(inputField);   
            upButton = new JButton("▲");
            downButton = new JButton("▼");
            inputPanel.add(upButton);
            inputPanel.add(downButton);
            upButton.addActionListener(b);
            downButton.addActionListener(b);
        buttonPanel = new JPanel(new FlowLayout());
        dialog.add(buttonPanel);
            okButton = new JButton("Set");
            cancelButton = new JButton("Cancel");
            
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
            okButton.addActionListener(b);
            cancelButton.addActionListener(b);
       
        
        
        dialog.getRootPane().setDefaultButton(okButton);
    }
    
    public void showDialog(){
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(s.f);
        dialog.setVisible(true);
    }
    
    public class ButtonListener implements ActionListener {

        int sizeX = 0;
        int sizeY = 0;

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println(s.getMachineWidth());
            System.out.println(s.getMachineHeight());
            Object source = e.getSource();
            if (source == upButton) {
                newScalingMultiplier += 2;
                inputField.setText("<html><h4>" + newScalingMultiplier + "</h4></html>");
                applyDisplaySettings(newScalingMultiplier);
            } else if (source == downButton) {
                if(newScalingMultiplier > 4){
                    newScalingMultiplier -= 2;
                    inputField.setText("<html><h4>" + newScalingMultiplier + "</h4></html>");
                    applyDisplaySettings(newScalingMultiplier);
                }
            } else if (source == okButton) {
                dialog.dispose();
            } else if (source == cancelButton) {
                applyDisplaySettings(currentScalingMultiplier);
                dialog.dispose();
            }
            s.hiResViewWidth = s.IMGWIDTH * s.HIRES_SCALE_FACTOR;
            s.hiResViewHeight = s.IMGHEIGHT * s.HIRES_SCALE_FACTOR;
            s.lowResViewWidth = s.IMGWIDTH * s.LOWRES_SCALE_FACTOR;
            s.lowResViewHeight = s.IMGHEIGHT * s.LOWRES_SCALE_FACTOR;
        }

        private void applyDisplaySettings(int scalingMultiplier) {
            s.LOWRES_SCALE_FACTOR = scalingMultiplier;
            s.HIRES_SCALE_FACTOR = scalingMultiplier / 2;
            s.gamePanel.revalidate();
            s.gamePanel.repaint();
            if (s.getHiRes()) {
                sizeX = 128 * s.HIRES_SCALE_FACTOR;
                sizeY = 64 * s.HIRES_SCALE_FACTOR;
            } else {
                sizeX = 64 * s.LOWRES_SCALE_FACTOR;
                sizeY = 32 * s.LOWRES_SCALE_FACTOR;
            }
            s.gamePanel.setPreferredSize(new Dimension(sizeX, sizeY));
            s.f.pack();
        }

    }
}
