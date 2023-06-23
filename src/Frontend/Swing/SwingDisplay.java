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

/*
* A class representing the elapsedTimeFromEpoch frame of the display where:
* prevFrame: contains the pixels that are on from the previous frame. Atm, it is a shallow copy, but works fine with a deep copy as well.
* hires: if the previous frame is hi-res or not
* prevColors: the colors in the previous frame
* Original implementation from: https://github.com/JohnEarnest/Octo/
*/
class LastFrame{
    int[][] prevFrame;
    Boolean hires;
    Color[] prevColors;
    //constructor
    LastFrame(int[][] arr2D, Boolean hires, Color[] colorArr){
        prevFrame = new int[arr2D.length][];
//        for(int i = 0; i < arr2D.length; i++){
//            int[] temp = arr2D[i];
//            int length = temp.length;
//            prevFrame[i] = new int[length];
//            System.arraycopy(temp, 0, prevFrame[i], 0, length);
//        }
        //create
        prevFrame = new int[2][];
        prevFrame[0] = arr2D[0].clone();
        prevFrame[1] = arr2D[1].clone();
        this.hires = hires;
        prevColors = new Color[4];
        System.arraycopy(colorArr, 0, prevColors, 0, prevColors.length);
    }
}
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
    //array to store colour palette
    private Color[] planeColors;
    
    File rom;
    MachineType m;
    private LastFrame last;
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
            private JMenu helpMenu;
                private JMenuItem aboutEmulator;
    public JPanel gamePanel;
    

    Chip8SOC chip8CPU;

    public SwingDisplay(String verNo) throws IOException {
        image = new BufferedImage(IMGWIDTH,IMGHEIGHT,BufferedImage.TYPE_INT_RGB);
        frameBuffer = image.createGraphics();
        f = new JFrame(verNo);
        loadDefaults();
        buildPanel();
        setInitialMachine();
//        planeColors[0] = Color.ORANGE;
//        planeColors[1] = Color.BLUE;
//        planeColors[2] = Color.RED;
//        planeColors[3] = new Color(149,129,103);
        
        isRunning = false;
        romStatus = false;
        f.setIconImage(ImageIO.read(getClass().getResourceAsStream("/Frontend/icon.png")));
        panelX = 64 * LOWRES_SCALE_FACTOR;
        panelY = 32 * LOWRES_SCALE_FACTOR;
        gamePanel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                g2d = (Graphics2D) g;
                super.paintComponent(g2d);
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
    
    public void loadDefaults(){
        m = MachineType.XO_CHIP;
        chip8CPU = new Chip8SOC(true, m);
        planeColors = new Color[4];
        planeColors[0] = new Color(0,0,0);
        planeColors[1] = new Color(0xCC,0xCC,0xCC);
        planeColors[2] = new Color(237,28,36);
        planeColors[3] = new Color(66,66,66);
        LOWRES_SCALE_FACTOR = 20;
        HIRES_SCALE_FACTOR = LOWRES_SCALE_FACTOR/2;
        hiResViewWidth = IMGWIDTH * HIRES_SCALE_FACTOR;
        hiResViewHeight = IMGHEIGHT * HIRES_SCALE_FACTOR;
        lowResViewWidth = IMGWIDTH * LOWRES_SCALE_FACTOR;
        lowResViewHeight = IMGHEIGHT * LOWRES_SCALE_FACTOR;
    }
    public void setInitialMachine(){
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
            chooser.addChoosableFileFilter(c8roms);
            FileNameExtensionFilter sc8roms = new FileNameExtensionFilter(
                    "Superchip ROM Files", "sc8");
            chooser.addChoosableFileFilter(sc8roms);
             FileNameExtensionFilter xoChiproms = new FileNameExtensionFilter(
                    "XO-Chip ROM Files", "xo8");
            chooser.addChoosableFileFilter(xoChiproms);
            chooser.setAcceptAllFileFilterUsed(false);
            
            if (rom == null) {
                chooser.setCurrentDirectory(new File("."));
            } else {
                chooser.setCurrentDirectory(rom);
            }
            int returnVal = chooser.showOpenDialog(f);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File tempRom = chooser.getSelectedFile();
                
                if(checkROMSize(tempRom)){
                    loadROM(tempRom);
                }else{
                     JOptionPane.showMessageDialog(null, "Rom is too large for "+ m.getMachineName() +"!", "Error", JOptionPane.ERROR_MESSAGE);
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
                    if (romStatus && rom != null) {
                        if (!checkROMSize(rom,MachineType.COSMAC_VIP)) { 
                            machineGroup.clearSelection();
                            setInitialMachine();
                            JOptionPane.showMessageDialog(null, "Rom is too large for " + MachineType.COSMAC_VIP.getMachineName() + "!", "Error", JOptionPane.ERROR_MESSAGE);

                        } else {
                            m = MachineType.COSMAC_VIP;
                            chip8CPU.setCurrentMachine(m);
                            loadROM(rom);
                        }
                    }else{
                       m = MachineType.COSMAC_VIP; 
                       chip8CPU.setCurrentMachine(m);
                    }
                }else if(sChip1_1.isSelected()){
                    if (!checkROMSize(rom,MachineType.SUPERCHIP_1_1)) { 
                            machineGroup.clearSelection();
                            setInitialMachine();
                            JOptionPane.showMessageDialog(null, "Rom is too large for " + MachineType.SUPERCHIP_1_1.getMachineName() + "!", "Error", JOptionPane.ERROR_MESSAGE);

                        } else {
                            m = MachineType.SUPERCHIP_1_1;
                            chip8CPU.setCurrentMachine(m);
                            loadROM(rom);
                        }
                }else if(xoChip.isSelected()){
                    if (!checkROMSize(rom, MachineType.XO_CHIP)) {
                        machineGroup.clearSelection();
                        setInitialMachine();
                        JOptionPane.showMessageDialog(null, "Rom is too large for " + MachineType.XO_CHIP.getMachineName() + "!", "Error", JOptionPane.ERROR_MESSAGE);

                    } else {
                        m = MachineType.XO_CHIP;
                        chip8CPU.setCurrentMachine(m);
                        loadROM(rom);
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
                chip8CPU.tg.flush();
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
                   repaintImage();
            }

        });
        foregroundColorManager = new JMenuItem("Set Foreground Color (XO-Chip Plane 1)");
        foregroundColorManager.addActionListener((e) -> {
            ColorManager cm = new ColorManager(f,planeColors[1]);
            planeColors[1] = cm.getColor();
            if (chip8CPU.graphics != null && pauseToggle.isSelected()) {
                   repaintImage();
            }
        });
        plane2ColorManager = new JMenuItem("Set XO-Chip Plane 2 Color");
        plane2ColorManager.addActionListener((e) -> {
            ColorManager cm = new ColorManager(f,planeColors[2]);
            planeColors[2] = cm.getColor();
            if (chip8CPU.graphics != null && pauseToggle.isSelected()) {
                   repaintImage();
            }
        });
        plane3ColorManager = new JMenuItem("Set XO-Chip Plane 3 Color");
        plane3ColorManager.addActionListener((e) -> {
            ColorManager cm = new ColorManager(f,planeColors[3]);
            planeColors[3] = cm.getColor();
            if (chip8CPU.graphics != null && pauseToggle.isSelected()) {
                   repaintImage();
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
        
        helpMenu = new JMenu("Help");
        mb.add(helpMenu);
            aboutEmulator = new JMenuItem("About Coffee-8");
            aboutEmulator.addActionListener((e) ->{
                AboutScreen about = new AboutScreen(f);
                about.showDialog();
            });
            helpMenu.add(aboutEmulator);
            
    }
    public Boolean checkROMSize(File rom){
        Boolean rightSize = true;
        if (m == MachineType.COSMAC_VIP && rom.length() > 3232L) {
            //JOptionPane.showMessageDialog(null, "Rom is too large for Chip-8!", "Error", JOptionPane.ERROR_MESSAGE);
            rightSize = false;
        } else if (m == MachineType.SUPERCHIP_1_1 && rom.length() > 3583L) {
            //JOptionPane.showMessageDialog(null, "Rom is too large for Super-Chip!", "Error", JOptionPane.ERROR_MESSAGE);
             rightSize = false;
        } else if (m == MachineType.XO_CHIP && rom.length() > 65024L) {
            //JOptionPane.showMessageDialog(null, "Rom is too large for XO-Chip!", "Error", JOptionPane.ERROR_MESSAGE);
            rightSize = false;
        }
        return rightSize;
    }
    
    public Boolean checkROMSize(File rom, MachineType m){
        Boolean rightSize = true;
        if (m == MachineType.COSMAC_VIP && rom.length() > 3232L) {
            //JOptionPane.showMessageDialog(null, "Rom is too large for Chip-8!", "Error", JOptionPane.ERROR_MESSAGE);
            rightSize = false;
        } else if (m == MachineType.SUPERCHIP_1_1 && rom.length() > 3583L) {
            //JOptionPane.showMessageDialog(null, "Rom is too large for Super-Chip!", "Error", JOptionPane.ERROR_MESSAGE);
             rightSize = false;
        } else if (m == MachineType.XO_CHIP && rom.length() > 65024L) {
            //JOptionPane.showMessageDialog(null, "Rom is too large for XO-Chip!", "Error", JOptionPane.ERROR_MESSAGE);
            rightSize = false;
        }
        return rightSize;
    }
    public void loadROM(File rom) {
        
        try {
            //stopEmulation();
            synchronized(chip8CPU){
                romStatus = chip8CPU.loadROM(rom);
            }
            if (romStatus) {
                this.rom = rom;
                
                if (pauseToggle.isSelected()) {
                    pauseToggle.setSelected(false);
                }

                SwingUtilities.invokeLater(() -> {
                    //clear the elapsedTimeFromEpoch frame each time a new rom is loaded.
                    last = null;
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
        isRunning = false;
        cpuCycleThread = null;
    }
    @Override
    public void run() {
        cpuCycleThread.setPriority(Thread.NORM_PRIORITY);
        double frameTime = 1000/60;
        long elapsedTimeFromEpoch = System.currentTimeMillis();
        double origin = elapsedTimeFromEpoch+frameTime/2;
        
        while (isRunning) {
            synchronized (chip8CPU) {
                long diff = System.currentTimeMillis() - elapsedTimeFromEpoch;
                elapsedTimeFromEpoch+=diff;
                for (long i = 0; origin < elapsedTimeFromEpoch - frameTime && i < 2; origin += frameTime, i++) {
                    for (int j = 0; j < chip8CPU.getCycles() && !chip8CPU.getWaitState(); j++) {
                        try{
                            chip8CPU.cpuExec();
                        }catch(Exception ex){
                            ex.printStackTrace();
                            
                            stopEmulation();
                        }
                       
                    }
                    chip8CPU.updateTimers();
                }
                
                try {
                    cpuCycleThread.sleep((int)frameTime);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                if (chip8CPU.getVBLankInterrupt() == 1) {
                    chip8CPU.setVBLankInterrupt(2);
                }
            }
            
            repaintImage();
            
        }
        

    }
    
    //these methods will check if an array is equal. It will exist early if there is an inequality
    public Boolean arrayEqual(int[] a, int[] b) {
        int length = a.length;
        if (length != b.length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }
    
    public Boolean arrayEqual(Color[] a, Color[] b) {
        int length = a.length;
        if (length != b.length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }
    /*
    * This function will not refresh the BufferedImage if it hasn't changed.
    * Original Implementation from: https://github.com/JohnEarnest/Octo/
    */
    public void repaintImage(){
        //if there is a last frame
        if(last != null){
            //check if the previous frame and the previous palette is the same as the current frame in both planes.
            if(arrayEqual(last.prevFrame[0], chip8CPU.graphics[0]) && arrayEqual(last.prevFrame[1], chip8CPU.graphics[1]) && arrayEqual(last.prevColors,planeColors)){
                //exit early if it is the same.
                return;
            }
            //clear last frame if we've switched from hi res to lowres or vice versa. Also clear it if the color palette has changed
            if (last.hires != chip8CPU.getHiRes() || !arrayEqual(last.prevColors,planeColors))
		last = null; 
        }
        //store the last frame here. Probably redundant?
        int[][] lastPixels = last != null && last.prevFrame != null? last.prevFrame: new int[2][chip8CPU.getMachineWidth() * chip8CPU.getMachineHeight()];
        //write the pixels into the BufferedImage
        if (chip8CPU.graphics != null) {
            for (int y = 0; y < chip8CPU.getMachineHeight(); y++) {
                for (int x = 0; x < chip8CPU.getMachineWidth(); x++) {
                    int newPlane = (chip8CPU.graphics[1][(x) + ((y) * chip8CPU.getMachineWidth())] << 1 | chip8CPU.graphics[0][(x) + ((y) * chip8CPU.getMachineWidth())]) & 0x3;
                    //selectively update each pixel if the last frame exists
                    if (last != null) {
                        int oldPlane = (lastPixels[1][(x) + ((y) * chip8CPU.getMachineWidth())] << 1 | lastPixels[0][(x) + ((y) * chip8CPU.getMachineWidth())]) & 0x3;
                        if (oldPlane != newPlane) {
                            frameBuffer.setColor(planeColors[newPlane]);
                            frameBuffer.fillRect(x, y, 1, 1);
                        }
                    }else{
                        //full rewrite of the screen
                        frameBuffer.setColor(planeColors[newPlane]);
                        frameBuffer.fillRect(x, y, 1, 1);
                    }
                }
            }
        }
        //apply the changes to the gamePanel
        gamePanel.repaint();
        //Instantiate a class corresponding to the elapsedTimeFromEpoch frame of the chip 8
        last = new LastFrame(chip8CPU.graphics, chip8CPU.getHiRes(), planeColors);
    }
    @Override
    public void keyPressed(KeyEvent e) {
        if (chip8CPU.keyPad == null) {
            return;
        }
        int keyCode = e.getKeyCode();
        switch (keyCode) {
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
                chip8CPU.closeSound();
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

}
