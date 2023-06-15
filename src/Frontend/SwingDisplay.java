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
import Backend.Chip8SOC;
import Backend.MachineType;
import java.awt.Graphics;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SwingDisplay extends KeyAdapter implements Runnable {
    //resolution of the buffered image
    protected final int IMGWIDTH = 128;
    protected final int IMGHEIGHT = 64;
    //resolution of the buffered image * scaling factor
    protected int hiResViewWidth;
    protected int hiResViewHeight; 
    protected int lowResViewWidth;
    protected int lowResViewHeight; 
    //scaling factor for both low and hires mode
    protected int LOWRES_SCALE_FACTOR;
    protected int HIRES_SCALE_FACTOR;
    //panel dimensions
    private int panelX;
    private int panelY;
    private Color[] planeColors;
    
    File rom;
    MachineType m;
    
    public BufferedImage image;
    public Graphics2D frameBuffer;
    private Boolean romStatus;
    private Thread cpuCycleThread;
    private Boolean isRunning;
    private Graphics2D g2d;
    protected JFrame f;
    private JMenuBar mb;
        private JMenu fileMenu;
            private JMenuItem loadROM;
            private JMenuItem exitSwitch;
        private JMenu emulationMenu;
            private JMenu machineTypeMenu;
                private ButtonGroup machineGroup;
                private JRadioButtonMenuItem cosmacVIP;
                private JRadioButtonMenuItem sChip1_1;
                private JRadioButtonMenuItem xoChip;
                private ActionListener machineChangeListener;
            private JMenuItem scalingManager;
            private JMenuItem cycleManager;
            private JMenuItem resetSwitch;
            private JCheckBoxMenuItem pauseToggle;
            private JCheckBoxMenuItem soundToggle;
            private JMenu colorManagerMenu;
                private JMenuItem backgroundColorManager;
                private JMenuItem foregroundColorManager;
                private JMenuItem plane2ColorManager;
                private JMenuItem plane3ColorManager;
    public JPanel gamePanel;
    

    Chip8SOC chip8CPU;

    public SwingDisplay(String verNo) throws IOException {
        image = new BufferedImage(IMGWIDTH,IMGHEIGHT,BufferedImage.TYPE_INT_RGB);
        frameBuffer = image.createGraphics();
        f = new JFrame(verNo);
        LOWRES_SCALE_FACTOR = 20;
        HIRES_SCALE_FACTOR = LOWRES_SCALE_FACTOR/2;
        buildPanel();
        m = MachineType.XO_CHIP;
        
        
        hiResViewWidth = IMGWIDTH * HIRES_SCALE_FACTOR;
        hiResViewHeight = IMGHEIGHT * HIRES_SCALE_FACTOR;
        lowResViewWidth = IMGWIDTH * LOWRES_SCALE_FACTOR;
        lowResViewHeight = IMGHEIGHT * LOWRES_SCALE_FACTOR;
        chip8CPU = new Chip8SOC(true, m);
        
        if (chip8CPU.getCurrentMachine() != null)
            switch (chip8CPU.getCurrentMachine()) {
                case COSMAC_VIP:
                    cosmacVIP.setSelected(true);
                    break;
                case SUPERCHIP_1_1:
                    sChip1_1.setSelected(true);
                    break;
                case XO_CHIP:
                    xoChip.setSelected(true);
                    break;
                default:
                    break;
            }
        planeColors = new Color[4];
        planeColors[0] = Color.ORANGE;
        planeColors[1] = Color.BLUE;
        planeColors[2] = Color.RED;
        planeColors[3] = new Color(149,129,103);
        isRunning = false;
        romStatus = false;
        f.setIconImage(ImageIO.read(getClass().getResourceAsStream("icon.png")));
        panelX = chip8CPU.getMachineWidth() * LOWRES_SCALE_FACTOR;
        panelY = chip8CPU.getMachineHeight() * LOWRES_SCALE_FACTOR;
        gamePanel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                g2d = (Graphics2D) g;
                super.paintComponent(g2d);
                
                if (chip8CPU.graphics != null) {
                    for (int y = 0; y < chip8CPU.getMachineHeight(); y++) {
                        for (int x = 0; x < chip8CPU.getMachineWidth(); x++) {
                            int plane = (chip8CPU.graphics[1][(x) + ((y) * chip8CPU.getMachineWidth())] << 1 | chip8CPU.graphics[0][(x) + ((y) * chip8CPU.getMachineWidth())]) & 0x3;                            
                            frameBuffer.setColor(planeColors[plane]);
                            frameBuffer.fillRect(x, y, 1, 1);

                        }
                    }
                }

                if (chip8CPU.getHiRes()) {
                    g2d.drawImage(image, 0, 0, hiResViewWidth, hiResViewHeight, gamePanel);
                } else {
                    g2d.drawImage(image, 0, 0, lowResViewWidth, lowResViewHeight, gamePanel);
                }
            }
        };
        gamePanel.setPreferredSize(new Dimension(panelX, panelY));
        f.add(mb, BorderLayout.NORTH);
        f.add(gamePanel,BorderLayout.CENTER);
       

    }
    
    public void buildPanel() {
        mb = new JMenuBar();
        fileMenu = new JMenu("File");
        mb.add(fileMenu);
        loadROM = new JMenuItem("Load ROM");
        loadROM.addActionListener((e) -> {
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter c8roms = new FileNameExtensionFilter(
                    "Chip-8 ROM Files", "c8", "ch8");
            chooser.setFileFilter(c8roms);
            FileNameExtensionFilter sc8roms = new FileNameExtensionFilter(
                    "Superchip ROM Files", "sc8");
            chooser.addChoosableFileFilter(sc8roms);
            if (rom == null) {
                chooser.setCurrentDirectory(new File("."));
            } else {
                chooser.setCurrentDirectory(rom);
            }
            int returnVal = chooser.showOpenDialog(f);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File tempRom = chooser.getSelectedFile();
                if (m == MachineType.COSMAC_VIP && tempRom.length() > 3232L) {
                    JOptionPane.showMessageDialog(null, "Rom is too large for Chip-8!", "Error", JOptionPane.ERROR_MESSAGE);
                } else if (m == MachineType.SUPERCHIP_1_1 && tempRom.length() > 3583L) {
                    JOptionPane.showMessageDialog(null, "Rom is too large for Super-Chip!", "Error", JOptionPane.ERROR_MESSAGE);
                } else if (m == MachineType.XO_CHIP && tempRom.length() > 65024L) {
                    JOptionPane.showMessageDialog(null, "Rom is too large for XO-Chip!", "Error", JOptionPane.ERROR_MESSAGE);
                }else{
                    loadROM(tempRom);
                }
                
            }
        });
        exitSwitch = new JMenuItem("Exit");
        exitSwitch.addActionListener((e) -> {
            System.exit(0);
        });
        fileMenu.add(loadROM);
        fileMenu.add(exitSwitch);
        emulationMenu = new JMenu("Emulation");
        
        mb.add(emulationMenu);
        machineTypeMenu = new JMenu("Select Machine Type");
            machineGroup = new ButtonGroup();
            cosmacVIP = new JRadioButtonMenuItem("COSMAC VIP");
            sChip1_1 = new JRadioButtonMenuItem("Superchip 1.1");
            xoChip = new JRadioButtonMenuItem("XO-Chip");
            machineGroup.add(cosmacVIP);
            machineGroup.add(sChip1_1);
            machineGroup.add(xoChip);
            machineTypeMenu.add(cosmacVIP);
            machineTypeMenu.add(sChip1_1);
             machineTypeMenu.add(xoChip);

            machineChangeListener = (e) -> {
                if(cosmacVIP.isSelected()){                      
                    if(romStatus && rom != null){          
                            m = MachineType.COSMAC_VIP;
                            chip8CPU.setCurrentMachine(m);
                            loadROM(rom);
                    }else{
                       m = MachineType.COSMAC_VIP; 
                       chip8CPU.setCurrentMachine(m);
                    }
                }else if(sChip1_1.isSelected()){
                    if(romStatus && rom != null){
                            m = MachineType.SUPERCHIP_1_1;
                            chip8CPU.setCurrentMachine(m);
                            loadROM(rom);
                    }else{
                        m = MachineType.SUPERCHIP_1_1;
                        chip8CPU.setCurrentMachine(m);
                    }
                }else if(xoChip.isSelected()){
                    if(romStatus && rom != null){
                            m = MachineType.XO_CHIP;
                            chip8CPU.setCurrentMachine(m);
                            loadROM(rom);
                    }else{
                        m = MachineType.XO_CHIP;
                        chip8CPU.setCurrentMachine(m);
                    }
                }
            };
            cosmacVIP.addActionListener(machineChangeListener);
            sChip1_1.addActionListener(machineChangeListener);
            xoChip.addActionListener(machineChangeListener);
        pauseToggle = new JCheckBoxMenuItem("Pause Emulation");
        pauseToggle.addActionListener((e) -> {
            if (romStatus && pauseToggle.isSelected()) {
                pauseToggle.setSelected(true);
                stopEmulation();
            } else if(romStatus && !pauseToggle.isSelected()) {
                pauseToggle.setSelected(false);
                startEmulation();
            }else{
                
                pauseToggle.setSelected(false);
            }
        });
        soundToggle = new JCheckBoxMenuItem("Enable Sound");
        
        soundToggle.addActionListener((e) -> {
            if (!soundToggle.isSelected()) {
                chip8CPU.disableSound();
            } else {
                try {
                    chip8CPU.enableSound();
                } catch (LineUnavailableException | UnsupportedAudioFileException | IOException se) {
                    soundToggle.setSelected(false);
                    JOptionPane.showMessageDialog(null, "An Error Occured when Initializing the sound system. It will be disabled: " + se, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        resetSwitch = new JMenuItem("Reset Emulator");
        resetSwitch.addActionListener((e) -> {
            if (rom != null) {
                loadROM(rom);
            }
        });
        colorManagerMenu = new JMenu("Colors");
        backgroundColorManager = new JMenuItem("Set Background Color (XO-Chip Plane 0)");
        backgroundColorManager.addActionListener((e) -> {
            ColorManager cm = new ColorManager(f,planeColors[0]);
            planeColors[0] = cm.getColor();
            if (chip8CPU.graphics != null && pauseToggle.isSelected()) {
                   gamePanel.repaint();
            }

        });
        foregroundColorManager = new JMenuItem("Set Foreground Color (XO-Chip Plane 1)");
        foregroundColorManager.addActionListener((e) -> {
            ColorManager cm = new ColorManager(f,planeColors[1]);
            planeColors[1] = cm.getColor();
            if (chip8CPU.graphics != null && pauseToggle.isSelected()) {
                   gamePanel.repaint();
            }
        });
        plane2ColorManager = new JMenuItem("Set XO-Chip Plane 2 Color");
        plane2ColorManager.addActionListener((e) -> {
            ColorManager cm = new ColorManager(f,planeColors[2]);
            planeColors[2] = cm.getColor();
            if (chip8CPU.graphics != null && pauseToggle.isSelected()) {
                   gamePanel.repaint();
            }
        });
        plane3ColorManager = new JMenuItem("Set XO-Chip Plane 3 Color");
        plane3ColorManager.addActionListener((e) -> {
            ColorManager cm = new ColorManager(f,planeColors[3]);
            planeColors[3] = cm.getColor();
            if (chip8CPU.graphics != null && pauseToggle.isSelected()) {
                   gamePanel.repaint();
            }
        });
        scalingManager = new JMenuItem("Set Video Scale");
        scalingManager.addActionListener((e) -> {
            ScalingManager scManager = new ScalingManager(this);
            scManager.showDialog();
        });
        cycleManager = new JMenuItem("Set CPU Cycle Count");
        cycleManager.addActionListener((e) -> {
            CycleManager cyManager = new CycleManager(chip8CPU,f);
            cyManager.showDialog();
        });
        emulationMenu.add(machineTypeMenu);
        emulationMenu.add(scalingManager);
        emulationMenu.add(cycleManager);
        emulationMenu.add(resetSwitch);
        emulationMenu.add(colorManagerMenu);
            colorManagerMenu.add(backgroundColorManager);
            colorManagerMenu.add(foregroundColorManager);
            colorManagerMenu.add(plane2ColorManager);
            colorManagerMenu.add(plane3ColorManager);
        emulationMenu.add(pauseToggle);
        emulationMenu.add(soundToggle);
    }
    public void loadROM(File rom) {

        try {
            synchronized(chip8CPU){
                romStatus = chip8CPU.loadROM(rom);
            }
            if (romStatus) {
                this.rom = rom;
                if (pauseToggle.isSelected()) {
                    pauseToggle.setSelected(false);
                }

                SwingUtilities.invokeLater(() -> {
                     startEmulation();
                });
            } else {
                romStatus = false;
                JOptionPane.showMessageDialog(null, "No ROM has been loaded into the emulator! Please load a ROM and try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ioe) {
            romStatus = false;
            JOptionPane.showMessageDialog(null, "There was a problem loading the ROM file:" + ioe.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public void startEmulation() {
        if (cpuCycleThread == null) {
            isRunning = true;
            cpuCycleThread = new Thread(this);

            cpuCycleThread.start();
        }
    }

    public void stopEmulation() {
        chip8CPU.pauseSound();
        isRunning = false;
        cpuCycleThread = null;
    }
    @Override
    public void run() {
        cpuCycleThread.setPriority(Thread.NORM_PRIORITY);
        while (isRunning) {
            synchronized (chip8CPU) {
                for (int i = 0; i < chip8CPU.getCycles() && !chip8CPU.getWaitState(); i++) {
                    chip8CPU.cpuExec();
                }

                chip8CPU.updateTimers();
                try {
                    cpuCycleThread.sleep(17);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                if (chip8CPU.getVBLankInterrupt() == 1) {
                    chip8CPU.setVBLankInterrupt(2);
                }
            }
            
            gamePanel.repaint();
        }
    }
        @Override
        public void keyPressed(KeyEvent e){
            if(chip8CPU.keyPad == null){
                return;
            }
            int keyCode = e.getKeyCode();
            switch(keyCode){
                case KeyEvent.VK_X:
                    chip8CPU.keyPad[0] = true;
                    
                    break;
                case KeyEvent.VK_1:
                    chip8CPU.keyPad[1] = true;
                    
                    break;
                case KeyEvent.VK_2:
                    chip8CPU.keyPad[2] = true;
                    
                    break;
                case KeyEvent.VK_3:
                    chip8CPU.keyPad[3] = true;
                    
                    break;
                case KeyEvent.VK_Q:
                    chip8CPU.keyPad[4] = true;
                    
                    break;
                case KeyEvent.VK_W:
                    chip8CPU.keyPad[5] = true;
                    
                    break;
                case KeyEvent.VK_E:
                    chip8CPU.keyPad[6] = true;
                    break;
                case KeyEvent.VK_A:
                    chip8CPU.keyPad[7] = true;
                    break;
                case KeyEvent.VK_S:
                    chip8CPU.keyPad[8] = true;
                    break;
                case KeyEvent.VK_D:
                    chip8CPU.keyPad[9] = true;
                    break;
                case KeyEvent.VK_Z:
                    chip8CPU.keyPad[10] = true;
                    break;
                case KeyEvent.VK_C:
                    chip8CPU.keyPad[11] = true;
                    break;
                case KeyEvent.VK_4:
                    chip8CPU.keyPad[12] = true;
                    break;
                case KeyEvent.VK_R:
                    chip8CPU.keyPad[13] = true;
                    break;
                case KeyEvent.VK_F:
                    chip8CPU.keyPad[14] = true;
                    break;
                case KeyEvent.VK_V:
                    chip8CPU.keyPad[15] = true;
                    break;
            }
//            for(int i = 0;i < chip8CPU.keyPad.length;i++){
//                System.out.println(chip8CPU.keyPad[i] + "\t");
//            }
//            System.out.println("");
        }
        
        @Override
        public void keyReleased(KeyEvent e){
            if(chip8CPU.keyPad == null){
                return;
            }
            int keyCode = e.getKeyCode();
            
            switch(keyCode){
                case KeyEvent.VK_X:
                    if(chip8CPU.getWaitState()){
                        chip8CPU.setWaitState(false);
                        chip8CPU.sendKeyStroke(0);
                    }
                    chip8CPU.keyPad[0] = false;
                    break;
                case KeyEvent.VK_1:
                    if(chip8CPU.getWaitState()){
                        chip8CPU.setWaitState(false);
                        chip8CPU.sendKeyStroke(1);
                    }
                    chip8CPU.keyPad[1] = false;
                    break;
                case KeyEvent.VK_2:
                    if(chip8CPU.getWaitState()){
                        chip8CPU.setWaitState(false);
                        chip8CPU.sendKeyStroke(2);
                    }
                    chip8CPU.keyPad[2] = false;
                    break;
                case KeyEvent.VK_3:
                    if(chip8CPU.getWaitState()){
                        chip8CPU.setWaitState(false);
                        chip8CPU.sendKeyStroke(3);
                    }
                    chip8CPU.keyPad[3] = false;
                    break;
                case KeyEvent.VK_Q:
                    if(chip8CPU.getWaitState()){
                        chip8CPU.setWaitState(false);
                        chip8CPU.sendKeyStroke(4);
                    }
                    chip8CPU.keyPad[4] = false;
                    break;
                case KeyEvent.VK_W:
                    if(chip8CPU.getWaitState()){
                        chip8CPU.setWaitState(false);
                        chip8CPU.sendKeyStroke(5);
                    }
                    chip8CPU.keyPad[5] = false;
                    break;
                case KeyEvent.VK_E:
                    if(chip8CPU.getWaitState()){
                        chip8CPU.setWaitState(false);
                        chip8CPU.sendKeyStroke(6);
                    }
                    chip8CPU.keyPad[6] = false;
                    break;
                case KeyEvent.VK_A:
                    if(chip8CPU.getWaitState()){
                        chip8CPU.setWaitState(false);
                        chip8CPU.sendKeyStroke(7);
                    }
                    chip8CPU.keyPad[7] = false;
                    break;
                case KeyEvent.VK_S:
                    if(chip8CPU.getWaitState()){
                        chip8CPU.setWaitState(false);
                        chip8CPU.sendKeyStroke(8);
                    }
                    chip8CPU.keyPad[8] = false;
                    break;
                case KeyEvent.VK_D:
                    if(chip8CPU.getWaitState()){
                        chip8CPU.setWaitState(false);
                        chip8CPU.sendKeyStroke(9);
                    }
                    chip8CPU.keyPad[9] = false;
                    break;
                case KeyEvent.VK_Z:
                    if(chip8CPU.getWaitState()){
                        chip8CPU.setWaitState(false);
                        chip8CPU.sendKeyStroke(10);
                    }
                    chip8CPU.keyPad[10] = false;
                    break;
                case KeyEvent.VK_C:
                    if(chip8CPU.getWaitState()){
                        chip8CPU.setWaitState(false);
                        chip8CPU.sendKeyStroke(11);
                    }
                    chip8CPU.keyPad[11] = false;
                    break;
                case KeyEvent.VK_4:
                    if(chip8CPU.getWaitState()){
                        chip8CPU.setWaitState(false);
                        chip8CPU.sendKeyStroke(12);
                    }
                    chip8CPU.keyPad[12] = false;
                    break;
                case KeyEvent.VK_R:
                    if(chip8CPU.getWaitState()){
                        chip8CPU.setWaitState(false);
                        chip8CPU.sendKeyStroke(13);
                    }
                    chip8CPU.keyPad[13] = false;
                    break;
                case KeyEvent.VK_F:
                    if(chip8CPU.getWaitState()){
                        chip8CPU.setWaitState(false);
                        chip8CPU.sendKeyStroke(14);
                    }
                    chip8CPU.keyPad[14] = false;
                    break;
                case KeyEvent.VK_V:
                    if(chip8CPU.getWaitState()){
                        chip8CPU.setWaitState(false);
                        chip8CPU.sendKeyStroke(15);
                    }
                    chip8CPU.keyPad[15] = false;
                    break;
            }
//                       for (int i = 0; i < chip8CPU.keyPad.length; i++) {
//                System.out.println(chip8CPU.keyPad[i] + "\t");
//            }
//            System.out.println("");
        }
        
    

    public void startApp() throws IOException{
        f.setResizable(false);
        f.pack();
        f.setLocationRelativeTo(null);
        f.addKeyListener(this);
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        try{
            chip8CPU.enableSound();
        }catch(LineUnavailableException|UnsupportedAudioFileException |IOException se ){
           JOptionPane.showMessageDialog(null, "An Error Occured When Initializing the Sound System. It will be disabled: " + se, "Error", JOptionPane.ERROR_MESSAGE); 
        }
        if (chip8CPU.isSoundEnabled()) {
            soundToggle.setSelected(true);
        } else {
            soundToggle.setSelected(false);
        }
        f.setVisible(true);
        

    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException  {
        
        //try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingDisplay d = new SwingDisplay("Coffee-8 1.0rc5 (xo-chip branch)");
            d.startApp();
            
        //}catch(FileNotFoundException fnfe){
            //JOptionPane.showMessageDialog(null, "Rom not found: " + fnfe, "Error", JOptionPane.ERROR_MESSAGE);
            //System.exit(0);
       // }catch(IOException ioe){
            //JOptionPane.showMessageDialog(null, "An I/O Error Occured: " + ioe, "Error", JOptionPane.ERROR_MESSAGE);
            //System.exit(0);
        //}
        
    }
}
