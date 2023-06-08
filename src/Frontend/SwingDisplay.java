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
import java.util.concurrent.ArrayBlockingQueue;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SwingDisplay extends KeyAdapter implements Runnable {
    Boolean romStatus;
    private Thread cpuCycleThread;
    private Boolean isRunning;
    private Graphics2D g2d;
    private JFrame f;
    private JMenuBar mb;
        private JMenu fileMenu;
            private JMenuItem loadROM;
            private JMenuItem exitSwitch;
        private JMenu emulationMenu;
            private JMenuItem cycleManager;
            private JMenuItem resetSwitch;
            private JCheckBoxMenuItem pauseToggle;
            private JCheckBoxMenuItem soundToggle;
            private JMenuItem backgroundColorManager;
            private JMenuItem foregroundColorManager;
    private JPanel gamePanel;
    private final int SCALE_FACTOR = 20;
    private int sizeX = 0;
    private int sizeY = 0;
    private Color backgroundColor;
    private Color foregroundColor;
    File rom;
    Chip8SOC chip8CPU;
    Dimension size;
    Timer t;
    ArrayBlockingQueue<Integer> keyQueue;
    public SwingDisplay(String verNo) throws IOException {
        chip8CPU = new Chip8SOC(true, MachineType.COSMAC_VIP);
        f = new JFrame(verNo);
        isRunning = false;
        f.setIconImage(ImageIO.read(getClass().getResourceAsStream("icon.png")));
        buildPanel();
        backgroundColor = Color.ORANGE;
        foregroundColor = Color.BLUE;
        sizeX = chip8CPU.getMachineWidth() * SCALE_FACTOR;
        sizeY = chip8CPU.getMachineHeight() * SCALE_FACTOR;
        
        gamePanel = new JPanel() {
            public void paint(Graphics g) {

                g2d = (Graphics2D) g;
                super.paintComponent(g2d);
                g2d.scale(SCALE_FACTOR, SCALE_FACTOR);

                if (chip8CPU.graphics != null) {
                    for (int y = 0; y < chip8CPU.getMachineHeight(); y++) {
                        for (int x = 0; x < chip8CPU.getMachineWidth(); x++) {
                            if (chip8CPU.graphics[(x) + ((y) * chip8CPU.getMachineWidth())] == 1) {
                                g.setColor(foregroundColor);
                                g.fillRect(x, y, 1, 1);
                                
                            } else {
                                g.setColor(backgroundColor);
                                g.fillRect(x, y, 1, 1);
                            }
                        }
                    } 
                }

            }
        };
        gamePanel.setPreferredSize(new Dimension(sizeX, sizeY));
        f.add(mb, BorderLayout.NORTH);
        f.add(gamePanel,BorderLayout.CENTER);
        romStatus = false;
        keyQueue = new ArrayBlockingQueue<>(4);
        t = new Timer(200, (e)->{
            //System.out.println("checking...");
            if(!keyQueue.isEmpty()){
                while(!keyQueue.isEmpty()){
                    int currKey = keyQueue.poll();
                    chip8CPU.keyPad[currKey] = false;
                    //System.out.println("Cleared: " + currKey);
                }
            }
                
        });
    }
    
    public void buildPanel() {
        mb = new JMenuBar();
        fileMenu = new JMenu("File");
        mb.add(fileMenu);
        loadROM = new JMenuItem("Load ROM");
        loadROM.addActionListener((e) -> {
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "Chip-8 ROM Files", "c8", "ch8");
            chooser.setFileFilter(filter);
            if (rom == null) {
                chooser.setCurrentDirectory(new File("."));
            } else {
                chooser.setCurrentDirectory(rom);
            }
            int returnVal = chooser.showOpenDialog(f);
            if (returnVal == JFileChooser.APPROVE_OPTION) {

                rom = chooser.getSelectedFile();
                loadROM(rom);

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
        pauseToggle = new JCheckBoxMenuItem("Pause Emulation");
        pauseToggle.addActionListener((e) -> {
            if (romStatus && pauseToggle.isSelected()) {
                pauseToggle.setSelected(true);
                if(t.isRunning())
                    t.stop();
                stopEmulation();
            } else if(romStatus && !pauseToggle.isSelected()) {
                pauseToggle.setSelected(false);
                if(!t.isRunning())
                    t.start();
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
        backgroundColorManager = new JMenuItem("Set Background Color");
        backgroundColorManager.addActionListener((e) -> {
            ColorManager cm = new ColorManager(f,backgroundColor);
            backgroundColor = cm.getColor();

        });
        foregroundColorManager = new JMenuItem("Set Foreground Color");
        foregroundColorManager.addActionListener((e) -> {
            ColorManager cm = new ColorManager(f,foregroundColor);
            foregroundColor = cm.getColor();
        });
        
        cycleManager = new JMenuItem("Set CPU Cycle Count");
        cycleManager.addActionListener((e) -> {
            CycleManager cyManager = new CycleManager(chip8CPU,f);
            cyManager.showDialog();
        });
        emulationMenu.add(cycleManager);
        emulationMenu.add(resetSwitch);
        emulationMenu.add(backgroundColorManager);
        emulationMenu.add(foregroundColorManager);
        emulationMenu.add(pauseToggle);
        emulationMenu.add(soundToggle);
    }
    public void loadROM(File rom) {
        try {
            romStatus = chip8CPU.loadROM(rom);
            if (romStatus) {
                if (pauseToggle.isSelected()) {
                    pauseToggle.setSelected(false);
                }
                 SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if(!t.isRunning()){
                           t.start(); 
                        }
                        startEmulation();
                    }
                });
            } else {
                romStatus = false;
                if (t.isRunning()) {
                    t.stop();
                }
                JOptionPane.showMessageDialog(null, "No ROM has been loaded into the emulator! Please load a ROM and try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ioe) {
            romStatus = false;
            if (t.isRunning()) {
                    t.stop();
            }
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
            //System.out.println(chip8CPU.getVBLankInterrupt());
            if (chip8CPU.getVBLankInterrupt() == 1) {
                
                chip8CPU.setVBLankInterrupt(2);
            }
            gamePanel.repaint();
        }
    }
    
        public void keyPressed(KeyEvent e){
            if(chip8CPU.keyPad == null || keyQueue.remainingCapacity() <= 0){
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

        public void keyReleased(KeyEvent e){
            if(chip8CPU.keyPad == null || keyQueue.remainingCapacity() <= 0){
                return;
            }
            int keyCode = e.getKeyCode();
            
            switch(keyCode){
                case KeyEvent.VK_X:
                    //chip8CPU.keyPad[0] = false;
                    if(!keyQueue.contains(0)){
                        keyQueue.offer(0);
                    }
                    break;
                case KeyEvent.VK_1:
                    //chip8CPU.keyPad[1] = false;
                    if(!keyQueue.contains(1)){
                        keyQueue.offer(1);
                    }
                    break;
                case KeyEvent.VK_2:
                    //chip8CPU.keyPad[2] = false;
                    if(!keyQueue.contains(2)){
                        keyQueue.offer(2);
                    }
                    break;
                case KeyEvent.VK_3:
                    //chip8CPU.keyPad[3] = false;
                    if(!keyQueue.contains(3)){
                        keyQueue.offer(3);
                    }
                    break;
                case KeyEvent.VK_Q:
                    //chip8CPU.keyPad[4] = false;
                    if(!keyQueue.contains(4)){
                        keyQueue.offer(4);
                    }
                    break;
                case KeyEvent.VK_W:
                    //chip8CPU.keyPad[5] = false;
                    if(!keyQueue.contains(5)){
                        keyQueue.offer(5);
                    }
                    break;
                case KeyEvent.VK_E:
                    //chip8CPU.keyPad[6] = false;
                    if(!keyQueue.contains(6)){
                        keyQueue.offer(6);
                    }
                    break;
                case KeyEvent.VK_A:
                    //chip8CPU.keyPad[7] = false;
                    if(!keyQueue.contains(7)){
                        keyQueue.offer(7);
                    }
                    break;
                case KeyEvent.VK_S:
                    //chip8CPU.keyPad[8] = false;
                    if(!keyQueue.contains(8)){
                        keyQueue.offer(8);
                    }
                    break;
                case KeyEvent.VK_D:
                    //chip8CPU.keyPad[9] = false;
                    if(!keyQueue.contains(9)){
                        keyQueue.offer(9);
                    }
                    break;
                case KeyEvent.VK_Z:
                    //chip8CPU.keyPad[10] = false;
                    if(!keyQueue.contains(10)){
                        keyQueue.offer(10);
                    }
                    break;
                case KeyEvent.VK_C:
                    //chip8CPU.keyPad[11] = false;
                    if(!keyQueue.contains(11)){
                        keyQueue.offer(11);
                    }
                    break;
                case KeyEvent.VK_4:
                    //chip8CPU.keyPad[12] = false;
                    if(!keyQueue.contains(12)){
                        keyQueue.offer(12);
                    }
                    break;
                case KeyEvent.VK_R:
                    //chip8CPU.keyPad[13] = false;
                    if(!keyQueue.contains(13)){
                        keyQueue.offer(13);
                    }
                    break;
                case KeyEvent.VK_F:
                    //chip8CPU.keyPad[14] = false;
                    if(!keyQueue.contains(14)){
                        keyQueue.offer(14);
                    }
                    break;
                case KeyEvent.VK_V:
                    //chip8CPU.keyPad[15] = false;
                    if(!keyQueue.contains(15)){
                        keyQueue.offer(15);
                    }
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
        SwingDisplay d = null;
        //try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            d = new SwingDisplay("Coffee-8 0.9");
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
