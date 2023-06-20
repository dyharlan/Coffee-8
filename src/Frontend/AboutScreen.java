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
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import javax.imageio.ImageIO;
/**
 *
 * @author dyhar
 */
public class AboutScreen {
    JDialog aboutDialog;
    BufferedImage appIcon;
    String alt = "";
    JPanel panel1, panel2, panel3;
    JLabel appIconContainer, repoLink, paragraph;
    JButton okButton;
    JFrame owner;
    public AboutScreen(JFrame owner){
        this.owner = owner;
        aboutDialog = new JDialog(owner, "About Coffee-8", true);
        aboutDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                aboutDialog.dispose();
            }
        });
        try{
            appIcon = ImageIO.read(getClass().getResourceAsStream("icon.png"));
            aboutDialog.setIconImage(appIcon);
        }catch(IOException ioe){
            alt = "Coffee-8 Icon Placeholder";
        }
        panel1 = new JPanel(new FlowLayout());
            appIconContainer = new JLabel(new ImageIcon(appIcon));
            panel1.add(appIconContainer);
        aboutDialog.add(panel1,BorderLayout.NORTH);
        panel2 = new JPanel(new BorderLayout());
            paragraph = new JLabel("",SwingConstants.CENTER);
            paragraph.setText("<html><style>p {text-align: center;}</style><body><p>Coffee-8 Chip-8/Super-Chip/XO-Chip Emulator<br>Copyright 2023 dyharlan<br>This software is licensed under the MIT License.</p></body></html>");
            panel2.add(paragraph, BorderLayout.NORTH);
            repoLink = new JLabel("",SwingConstants.CENTER);
            repoLink.setText("<html><style>p {text-align: center;}</style><body> <p> <a href=\"https://www.github.com/dyharlan/Coffee-8\">GitHub Page for Coffee-8</a> </p>  </body></html>");
            repoLink.addMouseListener(new MouseAdapter(){
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI("https://www.github.com/dyharlan/Coffee-8"));
                    } catch (IOException | URISyntaxException e1) {
                        JOptionPane.showMessageDialog(aboutDialog,
                                "Could not open the hyperlink. Error: " + e1.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            panel2.add(repoLink, BorderLayout.CENTER);
        aboutDialog.add(panel2, BorderLayout.CENTER);
        panel3 = new JPanel();
            okButton = new JButton("Ok");
            okButton.addActionListener((e) -> {
                aboutDialog.dispose();
            });
            panel3.add(okButton);
        aboutDialog.add(panel3, BorderLayout.SOUTH);
        
    }
    
    public void showDialog(){
        if(aboutDialog != null){
            aboutDialog.pack();
            aboutDialog.setResizable(false);
            aboutDialog.setLocationRelativeTo(owner);
            aboutDialog.setVisible(true);
        }
    }
}
