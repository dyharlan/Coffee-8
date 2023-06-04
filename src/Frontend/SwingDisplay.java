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
import Backend.MachineType;
import java.awt.Graphics;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SwingDisplay implements Runnable {

    private Thread cpuCycleThread;
    private Boolean isRunning;
    private Graphics2D g2d;
    private JFrame f;
    private JPanel gamePanel;
    //private final int DISPLAY_WIDTH = 64;
    //private final int DISPLAY_HEIGHT = 32;
    private final int SCALE_FACTOR = 20;
    private int sizeX = 0;
    private int sizeY = 0;

    File rom;
    Chip8SOC chip8CPU;
    Dimension size;

    public SwingDisplay(String verNo) throws FileNotFoundException, IOException, LineUnavailableException, UnsupportedAudioFileException {
        f = new JFrame(verNo);
        rom = new File("D:\\Others\\src\\Coffee-8\\software\\Pong [Paul Vervalin, 1990].ch8");
        chip8CPU = new Chip8SOC(rom, true, MachineType.COSMAC_VIP);
        sizeX = chip8CPU.getMachineWidth() * SCALE_FACTOR;
        sizeY = chip8CPU.getMachineHeight() * SCALE_FACTOR;

        gamePanel = new JPanel() {
            public void paint(Graphics g) {

                g2d = (Graphics2D) g;
                super.paintComponent(g2d);
                g2d.scale(SCALE_FACTOR, SCALE_FACTOR);

                for (int y = 0; y < chip8CPU.getMachineHeight(); y++) {
                    for (int x = 0; x < chip8CPU.getMachineWidth(); x++) {
                        if (chip8CPU.graphics[(x) + ((y) * chip8CPU.getMachineWidth())] == 1) {
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
        gamePanel.setPreferredSize(new Dimension(sizeX, sizeY));
        f.add(gamePanel);

    }

    public void start() {
        if (cpuCycleThread == null) {
            isRunning = true;
            cpuCycleThread = new Thread(this);
            cpuCycleThread.start();
        }
    }

    public void stop() {
        chip8CPU.stopSound();
        isRunning = false;
        cpuCycleThread = null;
    }

    public void run() {
        cpuCycleThread.setPriority(Thread.NORM_PRIORITY);
        while (isRunning) {

            for (int i = 0; i < chip8CPU.getCycles(); i++) {
                chip8CPU.cpuExec();
            }

            chip8CPU.updateTimers();
            try {
                cpuCycleThread.sleep(16);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            if (chip8CPU.getVBLankInterrupt() == 1) {
                chip8CPU.setVBLankInterrupt(2);
            }
            gamePanel.repaint();
        }
    }

    public void startApp() {
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
                start();
            }
        });

    }

    public static void main(String[] args) {
         SwingDisplay d = null;
        try{
            d = new SwingDisplay("Coffee-8 0.5");
            d.startApp();
        }catch(FileNotFoundException fnfe){
            JOptionPane.showMessageDialog(null, "Rom not found: " + fnfe, "Error", JOptionPane.ERROR_MESSAGE);
        }catch(IOException ioe){
            JOptionPane.showMessageDialog(null, "An I/O Error Occured: " + ioe, "Error", JOptionPane.ERROR_MESSAGE);
        }catch( LineUnavailableException|UnsupportedAudioFileException se){
            JOptionPane.showMessageDialog(null, "An Error Occured when Initializing the sound system: " + se, "Error", JOptionPane.ERROR_MESSAGE);
        }
        
    }
}
