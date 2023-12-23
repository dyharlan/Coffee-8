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
import static Backend.MachineType.COSMAC_VIP;
import static Backend.MachineType.SUPERCHIP_1_1;
import static Backend.MachineType.XO_CHIP;
import java.awt.Graphics;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.CRC32;
import javax.imageio.ImageIO;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.filechooser.FileNameExtensionFilter;


public final class SwingDisplay extends Chip8SOC implements Runnable {
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
    //MachineType m;
    //an object representing the last frame of the image
    public BufferedImage image;
    public static BufferedImage icon;
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
    private boolean loadedFromConfigFile;
    CRC32 crc32;

    //constructor for default settings
    public SwingDisplay(String verNo, File configFile) throws IOException {
        this();
        loadSettingsFromFile(configFile);
        loadedFromConfigFile = true;
        buildPanel();
        setInitialMachine();
        image = new BufferedImage(IMGWIDTH,IMGHEIGHT,BufferedImage.TYPE_INT_RGB);
        frameBuffer = image.createGraphics();
        f = new JFrame(verNo);
        isRunning = false;
        romStatus = false;
        f.setIconImage(icon);
        panelX = 64 * LOWRES_SCALE_FACTOR;
        panelY = 32 * LOWRES_SCALE_FACTOR;
        //Overriding the paint method of JPanel
        gamePanel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                g2d = (Graphics2D) g;
                super.paintComponent(g2d);
                if (getHiRes()) {
                    g2d.drawImage(image, 0, 0, hiResViewWidth, hiResViewHeight, gamePanel);
                } else {
                    g2d.drawImage(image, 0, 0, lowResViewWidth, lowResViewHeight, gamePanel);
                }
            }
        };
        gamePanel.setPreferredSize(new Dimension(panelX, panelY));
        f.setJMenuBar(mb);
        f.add(gamePanel,BorderLayout.CENTER);
    }
    public SwingDisplay() throws IOException {
        super(true);
        super.setCurrentMachine(MachineType.XO_CHIP); 
        super.setCycles(200);
        LOWRES_SCALE_FACTOR = 10;
        HIRES_SCALE_FACTOR = LOWRES_SCALE_FACTOR/2;
        hiResViewWidth = IMGWIDTH * HIRES_SCALE_FACTOR;
        hiResViewHeight = IMGHEIGHT * HIRES_SCALE_FACTOR;
        lowResViewWidth = IMGWIDTH * LOWRES_SCALE_FACTOR;
        lowResViewHeight = IMGHEIGHT * LOWRES_SCALE_FACTOR;
         //Default Color Scheme
//        planeColors[0] = new Color(0,0,0);
//        planeColors[1] = new Color(0xCC,0xCC,0xCC);
//        planeColors[2] = new Color(237,28,36);
//        planeColors[3] = new Color(66,66,66);
        //Octo Classic
        planeColors = new Color[16];
//        planeColors[0] = new Color(0x99,0x66,0x00);
//        planeColors[1] = new Color(0xFF,0xCC,0x00);
//        planeColors[2] = new Color(0xFF,0x66,0x00);
//        planeColors[3] = new Color(0x66,0x22,0x00);
//        planeColors[4] = new Color(0xBF,0x2A,0xED);
//        planeColors[5] = Color.MAGENTA;
//        planeColors[6] = Color.YELLOW;
//        planeColors[7] = Color.GREEN;
//        planeColors[8] = Color.GRAY;
//        planeColors[9] = new Color(0x4B,0x00,0x82); //INDIGO
//        planeColors[10] = new Color(0xEE,0x82,0xEE); //VIOLET
//        planeColors[11] = new Color(0xAA,0x55,0x00);
//        planeColors[12] = Color.BLACK;
//        planeColors[13] = Color.WHITE;
//        planeColors[14] = Color.BLUE;
//        planeColors[15] =  Color.RED;

//        planeColors[0] = new Color(0x000000);
//        planeColors[1] = new Color(0x0000AA);
//        planeColors[2] = new Color(0x00AA00);
//        planeColors[3] = new Color(0x00AAAA);
//        planeColors[4] = new Color(0xAA0000);
//        planeColors[5] = new Color(0xAA00AA);
//        planeColors[6] = new Color(0xAA5500);
//        planeColors[7] = new Color(0xAAAAAA);
//        planeColors[8] = new Color(0x555555);
//        planeColors[9] = new Color(0x5555FF); //INDIGO
//        planeColors[10] = new Color(0x55FF55); //VIOLET
//        planeColors[11] = new Color(0x55FFFF);
//        planeColors[12] = new Color(0xFF5555);
//        planeColors[13] = new Color(0xFF55FF);
//        planeColors[14] = new Color(0xFFFF55);
//        planeColors[15] =  new Color(0xFFFFFF);

          //vga 16 colors
//        planeColors[0] = new Color(0x000000);
//        planeColors[1] = new Color(0xAA0000);
//        planeColors[2] = new Color(0x00AA00);
//        planeColors[3] = new Color(0x0000AA);
//        planeColors[4] = new Color(0x00AAAA);
//        planeColors[5] = new Color(0xAA00AA);
//        planeColors[6] = new Color(0xAA5500);
//        planeColors[7] = new Color(0xAAAAAA);
//        planeColors[8] = new Color(0x555555);
//        planeColors[9] = new Color(0xFF5555); //INDIGO
//        planeColors[10] = new Color(0x55FF55); //VIOLET
//        planeColors[11] = new Color(0x5555FF);
//        planeColors[12] = new Color(0x55FFFF);
//        planeColors[13] = new Color(0xFF55FF);
//        planeColors[14] = new Color(0xFFFF55);
//        planeColors[15] =  new Color(0xFFFFFF);

        planeColors[0] = new Color(0x000000);
        planeColors[1] = new Color(0xfbf305);
        planeColors[2] = new Color(0xDD8E00);
        planeColors[3] = new Color(0xC22524);
        planeColors[4] = new Color(0xf20884);
        planeColors[5] = new Color(0x4700a5);
        planeColors[6] = new Color(0x0000d3);
        planeColors[7] = new Color(0x02abea);
        planeColors[8] = new Color(0x1fb714);
        planeColors[9] = new Color(0x006412); 
        planeColors[10] = new Color(0x562c05); 
        planeColors[11] = new Color(0x90713a);
        planeColors[12] = new Color(0xC0C0C0);
        planeColors[13] = new Color(0x808080);
        planeColors[14] = new Color(0x404040);
        planeColors[15] =  new Color(0xFFFFFF);
        crc32 = new CRC32();
        icon = ImageIO.read(getClass().getResourceAsStream("/Frontend/icon.png"));

        
    }
    public SwingDisplay(String verNo) throws IOException {
        this();
        loadedFromConfigFile = false;
        buildPanel();
        setInitialMachine();
        image = new BufferedImage(IMGWIDTH,IMGHEIGHT,BufferedImage.TYPE_INT_RGB);
        frameBuffer = image.createGraphics();
        f = new JFrame(verNo);
//        planeColors[0] = Color.ORANGE;
//        planeColors[1] = Color.BLUE;
//        planeColors[2] = Color.RED;
//        planeColors[3] = new Color(149,129,103);
        f.setIconImage(icon);
        isRunning = false;
        romStatus = false;
        
        panelX = 64 * LOWRES_SCALE_FACTOR;
        panelY = 32 * LOWRES_SCALE_FACTOR;
        //Overriding the paint method of JPanel
        gamePanel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                g2d = (Graphics2D) g;
                super.paintComponent(g2d);
                if (getHiRes()) {
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

    
    public void loadSettingsFromFile(File configFile) throws IllegalArgumentException, IOException{
        BufferedReader br = new BufferedReader(new FileReader(configFile));
        String tempStr;
        String[] tempStrArray;
        int lines = 0;
        int currentlySelectedColor = 0;
        Boolean machineTypeParsed = false;
        Boolean[] parsedColors = new Boolean[16];
        Arrays.fill(parsedColors, false);
        Boolean scaleFactorParsed = false;
        Boolean cycleCountParsed = false;
        while((tempStr = br.readLine()) != null){
                //increments line counter by 1
                lines++;
                if(tempStr.trim().startsWith("MachineType")){
                    if(machineTypeParsed){
                        continue;
                    }
                    tempStrArray = tempStr.trim().split("=");
                    if(tempStrArray.length != 2){
                        //JOptionPane.showMessageDialog(f, "No Machine Type entered, resetting to XO-Chip as the default.", "Error", JOptionPane.ERROR_MESSAGE);
                         throw new IllegalArgumentException("No Machine Type entered, please enter a value and try again.");
                    }else{
                        switch(tempStrArray[1]){
                            case "cosmac_vip":
                                super.setCurrentMachine(MachineType.COSMAC_VIP);
                            break;
                            case "superchip1.1":
                                super.setCurrentMachine(MachineType.SUPERCHIP_1_1);
                            break;
                            case "xochip":
                                super.setCurrentMachine(MachineType.XO_CHIP);
                            break;
                            default:
                                throw new IllegalArgumentException("Invalid Machine Type entered, please enter either cosmac_vip, superchip1.1, or xochip and try again.");
                                //JOptionPane.showMessageDialog(f, "Invalid Machine Type entered, resetting to XO-Chip as the default.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                        
                    }
                    //chip8CPU = new Chip8SOC(true, m);
                    machineTypeParsed = true;
                }else if (tempStr.trim().startsWith("Color" + Integer.toString(currentlySelectedColor)) && (currentlySelectedColor < 16)) {
                if (parsedColors[currentlySelectedColor]) {
                    continue;
                }
                tempStrArray = tempStr.trim().split("=");
                if(tempStrArray.length == 2){
                    //needs padding in the beginning to autoconvert to string
                    try {
                        int R = Integer.parseInt("" + tempStrArray[1].charAt(1) + tempStrArray[1].charAt(2), 16);
                        int G = Integer.parseInt("" + tempStrArray[1].charAt(3) + tempStrArray[1].charAt(4), 16);
                        int B = Integer.parseInt("" + tempStrArray[1].charAt(5) + tempStrArray[1].charAt(6), 16);
                        planeColors[currentlySelectedColor] = new Color(R, G, B);
                    } catch (NumberFormatException nfe) {
                        throw nfe;
                    }
                }
                parsedColors[currentlySelectedColor] = true;
                currentlySelectedColor++; 
            } else if (tempStr.trim().startsWith("ScaleFactor")) {
                if (scaleFactorParsed) {
                    continue;
                }
                tempStrArray = tempStr.trim().split("=");
                if (tempStrArray.length != 2) {
                    //JOptionPane.showMessageDialog(f, "No Machine Type entered, resetting to XO-Chip as the default.", "Error", JOptionPane.ERROR_MESSAGE);
                    throw new IllegalArgumentException("No scale factor parsed. please enter a value and try again.");
                } else {
                    try{
                        int givenScaleFactor = Integer.parseInt(tempStrArray[1]);
                        if((givenScaleFactor % 2) == 0){
                            LOWRES_SCALE_FACTOR = givenScaleFactor;
                        }else{
                            throw new IllegalArgumentException("Scale value should be divisible by 2.");
                        }
                    }catch(NumberFormatException nfe){
                        throw nfe;
                    }catch(IllegalArgumentException iae){
                        throw iae;
                    }
                }
                HIRES_SCALE_FACTOR = LOWRES_SCALE_FACTOR/2;
                hiResViewWidth = IMGWIDTH * HIRES_SCALE_FACTOR;
                hiResViewHeight = IMGHEIGHT * HIRES_SCALE_FACTOR;
                lowResViewWidth = IMGWIDTH * LOWRES_SCALE_FACTOR;
                lowResViewHeight = IMGHEIGHT * LOWRES_SCALE_FACTOR;
                scaleFactorParsed = true;
            } else if (tempStr.trim().startsWith("CycleCount")){
                if(cycleCountParsed){
                    continue;
                }
                tempStrArray = tempStr.trim().split("=");
                if(tempStrArray.length == 2){
                    try{
                        setCycles(Integer.parseInt(tempStrArray[1]));
                    }catch(NumberFormatException nfe){
                        JOptionPane.showMessageDialog(f, "Invalid value for cycle count, defaulting to 200.", "Warning!", JOptionPane.QUESTION_MESSAGE);
                    }catch(IllegalArgumentException iae){
                        JOptionPane.showMessageDialog(f, "Cycle count should be greater than or equal to 0, defaulting to 200.", "Warning!", JOptionPane.QUESTION_MESSAGE);
                    }
                }
                cycleCountParsed = true;
                
            }              
        }
    }
    private void saveSettingsToFile(File configFile) throws IOException{
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        PrintWriter pr = new PrintWriter(new FileWriter(configFile));
        switch(super.getCurrentMachine()){
            case COSMAC_VIP:
                    pr.println("MachineType=cosmac_vip");
                    break;
                case SUPERCHIP_1_1:
                    pr.println("MachineType=superchip1.1");
                    break;
                case XO_CHIP:
                    pr.println("MachineType=xochip");
                    break;
                default:
                    pr.println("MachineType=xochip");
                    break;
        }
        int index = 0;
        for(Color c : planeColors){
            pr.println("Color" + index + "=#" + String.format("%06x", c.getRGB() & 0xFFFFFF));
            index++;
        }
        pr.println("ScaleFactor="+LOWRES_SCALE_FACTOR);
        pr.println("CycleCount="+getCycles());
        pr.close();
        
    }
    //initially sets the machine on first startup
    public void setInitialMachine(){
        if (super.getCurrentMachine() != null)
            switch (super.getCurrentMachine()) {
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
    
    //a big ass method that builds all the elements of the top jpanel
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
                     JOptionPane.showMessageDialog(f, "Rom is too large for "+ super.getCurrentMachine().getMachineName() + "!", "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            }
        });
        exitSwitch = new JMenuItem("Exit");
        exitSwitch.addActionListener((e) -> {
            super.closeSound();
            try{
                saveSettingsToFile(new File("config.cfg"));
            }catch(IOException ioe){
                JOptionPane.showMessageDialog(f, "There was a problem saving the new settings, the old ones will be restored.", "Error", JOptionPane.ERROR_MESSAGE);
            }
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
                            JOptionPane.showMessageDialog(f, "Rom is too large for " + MachineType.COSMAC_VIP.getMachineName() + "!", "Error", JOptionPane.ERROR_MESSAGE);

                        } else {
                            MachineType m = MachineType.COSMAC_VIP;
                            super.setCurrentMachine(m);
                            loadROM(rom);
                        }
                    }else{
                       MachineType m = MachineType.COSMAC_VIP; 
                       super.setCurrentMachine(m);
                    }
                }else if(sChip1_1.isSelected()){
                    if(romStatus && rom != null){
                        if (!checkROMSize(rom,MachineType.SUPERCHIP_1_1)) { 
                            machineGroup.clearSelection();
                            setInitialMachine();
                            JOptionPane.showMessageDialog(f, "Rom is too large for " + MachineType.SUPERCHIP_1_1.getMachineName() + "!", "Error", JOptionPane.ERROR_MESSAGE);

                        } else {
                            MachineType m = MachineType.SUPERCHIP_1_1;
                            super.setCurrentMachine(m);
                            loadROM(rom);
                        }
                    }else{
                        MachineType m = MachineType.SUPERCHIP_1_1;
                        super.setCurrentMachine(m); 
                    }    
                }else if (xoChip.isSelected()) {
                    if (romStatus && rom != null) {
                        if (!checkROMSize(rom, MachineType.XO_CHIP)) {
                            machineGroup.clearSelection();
                            setInitialMachine();
                            JOptionPane.showMessageDialog(f, "Rom is too large for " + MachineType.XO_CHIP.getMachineName() + "!", "Error", JOptionPane.ERROR_MESSAGE);

                        } else {
                            MachineType m = MachineType.XO_CHIP;
                            super.setCurrentMachine(m);
                            loadROM(rom);
                        }
                    } else {
                        MachineType m = MachineType.XO_CHIP;
                        super.setCurrentMachine(m);
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
                super.tg.flush();
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
                super.disableSound();
            } else {
                try {
                    super.enableSound();
                } catch (LineUnavailableException | UnsupportedAudioFileException | IOException se) {
                    soundToggle.setSelected(false);
                    JOptionPane.showMessageDialog(f, "An Error Occured when Initializing the sound system. It will be disabled: " + se, "Error", JOptionPane.ERROR_MESSAGE);
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
            if (super.graphics != null && pauseToggle.isSelected()) {
                   repaintImage();
            }

        });
        foregroundColorManager = new JMenuItem("Set Foreground Color (XO-Chip Plane 1)");
        foregroundColorManager.addActionListener((e) -> {
            ColorManager cm = new ColorManager(f,planeColors[1]);
            planeColors[1] = cm.getColor();
            if (super.graphics != null && pauseToggle.isSelected()) {
                   repaintImage();
            }
        });
        plane2ColorManager = new JMenuItem("Set XO-Chip Plane 2 Color");
        plane2ColorManager.addActionListener((e) -> {
            ColorManager cm = new ColorManager(f,planeColors[2]);
            planeColors[2] = cm.getColor();
            if (super.graphics != null && pauseToggle.isSelected()) {
                   repaintImage();
            }
        });
        plane3ColorManager = new JMenuItem("Set XO-Chip Plane 3 Color");
        plane3ColorManager.addActionListener((e) -> {
            ColorManager cm = new ColorManager(f,planeColors[3]);
            planeColors[3] = cm.getColor();
            if (super.graphics != null && pauseToggle.isSelected()) {
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
            CycleManager cyManager = new CycleManager(this,f);
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
    
    //checks the romsize if it is appropriate for the selected machine. Does NOT check if it is a valid ROM.
    public Boolean checkROMSize(File rom){
        Boolean rightSize = true;
        if (super.getCurrentMachine() == MachineType.COSMAC_VIP && rom.length() > 3232L) {
            //JOptionPane.showMessageDialog(null, "Rom is too large for Chip-8!", "Error", JOptionPane.ERROR_MESSAGE);
            rightSize = false;
        } else if (super.getCurrentMachine() == MachineType.SUPERCHIP_1_1 && rom.length() > 3583L) {
            //JOptionPane.showMessageDialog(null, "Rom is too large for Super-Chip!", "Error", JOptionPane.ERROR_MESSAGE);
             rightSize = false;
        } else if (super.getCurrentMachine() == MachineType.XO_CHIP && rom.length() > 65024L) {
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
    
    //This method loads the selected rom. It is also used when resetting the machine.
    public void loadROM(File rom) {
        try {
            synchronized(this){
                stopEmulation();
                romStatus = false;
                DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(rom)));
                int offset = 0x0;
                int currByte = 0;
                chip8Init();
                crc32.reset();
                while (currByte != -1) {
                    currByte = in.read();
                    crc32.update(currByte & 0xFF);
                    mem[0x200 + offset] = currByte & 0xFF;
                    offset += 0x1;
                }
                in.close();
                romStatus = true;
            }
            if (romStatus) {
                this.rom = rom;
                if (pauseToggle.isSelected()) {
                    pauseToggle.setSelected(false);
                }

                SwingUtilities.invokeLater(() -> {
                    update = true;
                    startEmulation();
                });
            } else {
                romStatus = false;
                JOptionPane.showMessageDialog(f, "No ROM has been loaded into the emulator! Please load a ROM and try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ioe) {
            romStatus = false;
            JOptionPane.showMessageDialog(f, "There was a problem loading the ROM file:" + ioe.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    //starts the cpu emulation cycle
    public void startEmulation() {
        if (cpuCycleThread == null) {
            isRunning = true;
            cpuCycleThread = new Thread(this);
            cpuCycleThread.start();
        }
    }
    
    //stops the cpu emulation cycle
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
            synchronized (this) {
                long diff = System.currentTimeMillis() - elapsedTimeFromEpoch;
                elapsedTimeFromEpoch+=diff;
                for (long i = 0; origin < elapsedTimeFromEpoch - frameTime && i < 2; origin += frameTime, i++) {
                    for (int j = 0; j < super.getCycles() && !super.getWaitState(); j++) {
                        if( ((mem[pc] & 0xF0) == 0xD0) && vBlankQuirks && !super.getHiRes() ){
                            j = this.getCycles();
                        }
                        if(!isCpuHalted()){
                            super.cpuExec();
                        }else{
                            stopEmulation();
                            if(getCauseOfHalt().trim() != ""){
                                JOptionPane.showMessageDialog(f, "An error occured dluring the execution of the emulated machine and has been halted: " + getCauseOfHalt(), "Error", JOptionPane.ERROR_MESSAGE); 
                            }
                            break;
                        }
                    }
                    super.updateTimers();
                }
                
                try {
                    cpuCycleThread.sleep((int)frameTime);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            
            repaintImage();
            
        }
    }
    
    //these methods will check if an array is equal. It will exit early if there is an inequality
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
    public void repaintImage() {
        if (!super.update) {
            return;
        }
        //write the pixels into the BufferedImage
        if (super.graphics != null) {
            for (int y = 0; y < super.getMachineHeight(); y++) {
                for (int x = 0; x < super.getMachineWidth(); x++) {
                    //int newPlane = (chip8CPU.graphics[1][(x) + ((y) * chip8CPU.getMachineWidth())] << 1 | chip8CPU.graphics[0][(x) + ((y) * chip8CPU.getMachineWidth())]) & 0x3;
                    int newPlane = (super.graphics[3][(x) + ((y) * super.getMachineWidth())] << 3 | super.graphics[2][(x) + ((y) * super.getMachineWidth())] << 2 | super.graphics[1][(x) + ((y) * super.getMachineWidth())] << 1 | super.graphics[0][(x) + ((y) * super.getMachineWidth())]) & 0xF;
                    //full rewrite of the screen
                    frameBuffer.setColor(planeColors[newPlane]);
                    frameBuffer.fillRect(x, y, 1, 1);
                }
            }
        }
        //apply the changes to the gamePanel
        gamePanel.repaint();
        //tell the emulator that we have successfully updated the framebuffer
        update = false;
    }
    
    class keyHandler extends KeyAdapter{
        @Override
    public void keyPressed(KeyEvent e) {
        if (keyPad == null) {
            return;
        }
        //System.out.println("pressed a key!");
        int keyCode = e.getKeyCode();
        switch (keyCode) {
            case KeyEvent.VK_X:
                keyPress(0);
                break;
            case KeyEvent.VK_1:
                keyPress(1);
                break;
            case KeyEvent.VK_2:
                keyPress(2);
                break;
            case KeyEvent.VK_3:
                keyPress(3);
                break;
            case KeyEvent.VK_Q:
                keyPress(4);
                break;
            case KeyEvent.VK_W:
                keyPress(5);
                break;
            case KeyEvent.VK_E:
                keyPress(6);
                break;
            case KeyEvent.VK_A:
                keyPress(7);
                break;
            case KeyEvent.VK_S:
                keyPress(8);
                break;
            case KeyEvent.VK_D:
                keyPress(9);
                break;
            case KeyEvent.VK_Z:
                keyPress(10);
                break;
            case KeyEvent.VK_C:
                keyPress(11);
                break;
            case KeyEvent.VK_4:
                keyPress(12);
                break;
            case KeyEvent.VK_R:
                keyPress(13);
                break;
            case KeyEvent.VK_F:
                keyPress(14);
                break;
            case KeyEvent.VK_V:
                keyPress(15);
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (keyPad == null) {
            return;
        }
        //System.out.println("released a key!");
        int keyCode = e.getKeyCode();

        switch (keyCode) {
            case KeyEvent.VK_X:
                keyRelease(0);
                break;
            case KeyEvent.VK_1:
                keyRelease(1);
                break;
            case KeyEvent.VK_2:
                keyRelease(2);
                break;
            case KeyEvent.VK_3:
                keyRelease(3);
                break;
            case KeyEvent.VK_Q:
                keyRelease(4);
                break;
            case KeyEvent.VK_W:
                keyRelease(5);
                break;
            case KeyEvent.VK_E:
                keyRelease(6);
                break;
            case KeyEvent.VK_A:
                keyRelease(7);
                break;
            case KeyEvent.VK_S:
                keyRelease(8);
                break;
            case KeyEvent.VK_D:
                keyRelease(9);
                break;
            case KeyEvent.VK_Z:
                keyRelease(10);
                break;
            case KeyEvent.VK_C:
                keyRelease(11);
                break;
            case KeyEvent.VK_4:
                keyRelease(12);
                break;
            case KeyEvent.VK_R:
                keyRelease(13);
                break;
            case KeyEvent.VK_F:
                keyRelease(14);
                break;
            case KeyEvent.VK_V:
                keyRelease(15);
                break;
        }
    }
    }
    

    
    //FX75: Store V0..VX in RPL user flags (X <= 7)
    public void C8INST_FX75(){
        File f = new File("SavedFlags/" + crc32.getValue() + ".scflag");
        
        try{
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }
            try ( DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)))) {

                for (int n = 0; (n <= X); n++) {
                    out.writeInt(v[n] & 0xFF);
                }
                out.flush();
                out.close();
            }catch(IOException ioe){
                throw ioe;
            }
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
    //FX85: Read V0..VX from RPL user flags (X <= 7)
    public void C8INST_FX85(){
        File f = new File("SavedFlags/" + crc32.getValue() + ".scflag");
        if (f.exists()) {
            //copy the flags first to a temporary array before writing it to memory.
            ArrayList<Integer> temp = new ArrayList<>();
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(f)))) {
                for (int n = 0;in.available() > 0; n++) {
                   temp.add(in.readInt() & 0xFF);
                }
                for(int i = 0; i < temp.size(); i++){
                    v[i] = temp.get(i) & 0xFF;
                }
                in.close();
            }catch(EOFException eofe){
                System.err.println("Invalid/broken flags file. It will not be loaded into memory.");
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    
    public void startApp() throws IOException{
        f.setResizable(false);
        f.pack();
        f.setLocationRelativeTo(null);
        f.addKeyListener(new keyHandler());
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeSound();
                try {
                    saveSettingsToFile(new File("config.cfg"));
                } catch (IOException ioe) {
                    JOptionPane.showMessageDialog(f, "There was a problem saving the new settings, the old ones will be restored.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                System.exit(0);
            }
        });
        try{
            super.enableSound();
        }catch(LineUnavailableException|UnsupportedAudioFileException |IOException se ){
           JOptionPane.showMessageDialog(f, "An Error Occured When Initializing the Sound System. It will be disabled: " + se, "Error", JOptionPane.ERROR_MESSAGE); 
        }
        if (super.isSoundEnabled()) {
            soundToggle.setSelected(true);
        } else {
            soundToggle.setSelected(false);
        }
        
        f.setVisible(true);
        

    }

    

}
