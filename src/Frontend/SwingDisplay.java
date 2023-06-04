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
    private Boolean initStatus;
    private Graphics2D g2d;
    private JFrame f;
    private JMenuBar mb;
        private JMenu fileMenu;
        
        private JMenu emulationMenu;
            private JCheckBoxMenuItem pauseToggle;
            private JCheckBoxMenuItem soundToggle;
    private JPanel gamePanel;
    
    private final int SCALE_FACTOR = 20;
    private int sizeX = 0;
    private int sizeY = 0;

    File rom;
    Chip8SOC chip8CPU;
    Dimension size;

    public SwingDisplay(String verNo) throws FileNotFoundException, IOException, LineUnavailableException, UnsupportedAudioFileException {
        chip8CPU = new Chip8SOC(true, MachineType.COSMAC_VIP);
        f = new JFrame(verNo);
        mb = new JMenuBar();    
            fileMenu = new JMenu("File");
            mb.add(fileMenu);
            
            emulationMenu = new JMenu("Emulation");
            mb.add(emulationMenu);
            pauseToggle = new JCheckBoxMenuItem("Pause Emulation");
                pauseToggle.addActionListener((e)->{
                        if(isRunning &&  pauseToggle.isSelected()){
                            stop();
                        }else{
                            start();
                        }
                });
            soundToggle = new JCheckBoxMenuItem("Enable Sound");
            if(chip8CPU.isSoundEnabled())
                soundToggle.setSelected(true);
            else
                soundToggle.setSelected(false);
            soundToggle.addActionListener((e)->{
                        if(!soundToggle.isSelected()){
                            chip8CPU.disableSound();
                        }else{
                            try{
                                chip8CPU.enableSound();
                            }catch(LineUnavailableException | UnsupportedAudioFileException se){
                                JOptionPane.showMessageDialog(null, "An Error Occured when Initializing the sound system, it will be disabled: " + se, "Error", JOptionPane.ERROR_MESSAGE);  
                            }catch(IOException ioe){
                                JOptionPane.showMessageDialog(null, "An I/O Error Occured: " + ioe, "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                });
            emulationMenu.add(pauseToggle);
            emulationMenu.add(soundToggle);
       
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
        f.add(mb, BorderLayout.NORTH);
        f.add(gamePanel,BorderLayout.CENTER);
        initStatus = false;
    }

    public void start() {
        if (cpuCycleThread == null) {
            isRunning = true;
            cpuCycleThread = new Thread(this);
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                   cpuCycleThread.start();
                }
            });
        }
    }

    public void stop() {
        chip8CPU.pauseSound();
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

    public void startApp() throws FileNotFoundException, IOException{
        f.setResizable(false);
        f.pack();
        f.setLocationRelativeTo(null);
        f.addKeyListener(chip8CPU);
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        rom = new File("D:\\Others\\src\\Coffee-8\\software\\Pong [Paul Vervalin, 1990].ch8");
        initStatus = chip8CPU.loadRom(rom);
        f.setVisible(true);
        if(initStatus){
            start();
        }else{
            JOptionPane.showMessageDialog(null, "No ROM has been loaded into the emulator! Please load a ROM and try again.", "Error", JOptionPane.ERROR_MESSAGE); 
        }

    }

    public static void main(String[] args) {
        SwingDisplay d = null;
        try{
            d = new SwingDisplay("Coffee-8 0.5");
            d.startApp();
        }catch(FileNotFoundException fnfe){
            JOptionPane.showMessageDialog(null, "Rom not found: " + fnfe, "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }catch(IOException ioe){
            JOptionPane.showMessageDialog(null, "An I/O Error Occured: " + ioe, "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }catch(LineUnavailableException|UnsupportedAudioFileException se){
           JOptionPane.showMessageDialog(null, "An Error Occured when Initializing the sound system, it will be disabled: " + se, "Error", JOptionPane.ERROR_MESSAGE); 
           System.exit(0);
        }
        
    }
}
