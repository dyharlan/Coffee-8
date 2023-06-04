package Frontend;

/*
 * Click nbfs:nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs:nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author dyhar
 */
import Backend.Chip8SOC;
import java.awt.Graphics;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;


public class SwingDisplay {
    
    private Graphics2D g2d;
    private JFrame f;
    private JPanel p;
    private final int DISPLAY_WIDTH = 64;
    private final int DISPLAY_HEIGHT = 32;
    private final int SCALE_FACTOR = 20;
    private int sizeX = 0;
    private int sizeY = 0;
    
    File rom;
    Chip8SOC chip8CPU;
    Dimension size;
    
    
    public SwingDisplay(String verNo)throws FileNotFoundException, IOException,LineUnavailableException, UnsupportedAudioFileException {
        f = new JFrame(verNo);
        sizeX = DISPLAY_WIDTH * SCALE_FACTOR;
        sizeY = DISPLAY_HEIGHT * SCALE_FACTOR;
        rom = new File("C:\\Users\\dyhar\\Downloads\\Brick Breaker (by Kyle Saburao)(2019).ch8");
        p = new JPanel() {
            public void paint(Graphics g) {

                g2d = (Graphics2D) g;
                super.paintComponent(g2d);
                g2d.scale(SCALE_FACTOR, SCALE_FACTOR);

                for (int y = 0; y < 32; y++) {
                    for (int x = 0; x < 64; x++) {
                        if (chip8CPU.graphics[(x) + ((y) * 64)] == 1) {
                            g.setColor(Color.BLUE);
                            g.fillRect(x, y, 1, 1);
                        } else {
                            g.setColor(Color.ORANGE);
                            g.fillRect(x, y, 1, 1);
                        }
                    }
                }

            }
        };
        p.setPreferredSize(new Dimension(sizeX, sizeY));
        f.add(p);
        chip8CPU = new Chip8SOC(rom, p, true);
        
       
    }

    public void startApp() throws InterruptedException {
        //f.setSize(sizeX, sizeY);
        f.setResizable(false);
        f.pack();
        f.setLocationRelativeTo(null);
        f.addKeyListener(chip8CPU);
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        f.setVisible(true);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                chip8CPU.start();
            }
        });

    }
    
    
    public static void main(String[] args) throws Exception {
        SwingDisplay d = new SwingDisplay("Coffee-8 0.5");
        d.startApp();
    }
}
