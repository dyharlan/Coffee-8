package Backend;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author dyhar
 */
import java.util.*;
import java.awt.event.*;
import java.io.*;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Chip8SOC extends KeyAdapter{

    private int DISPLAY_WIDTH;
    private int DISPLAY_HEIGHT;
    private Boolean vfOrderQuirks;
    private Boolean shiftQuirks;
    private Boolean logicQuirks;
    private Boolean loadStoreQuirks;
    private Boolean clipQuirks;
    private Boolean vBlankQuirks;
    private Boolean IOverflowQuirks;
    private int cycles;
    private int pc; //16-bit Program Counter
    private int I; //12-bit Index register
    private int opcode;
    private int dT; //8-bit delay timer
    private int sT; //sound timer
    private int[] v; //cpu registers
    public int[] graphics; //screen grid??
    private boolean[] keyPad; 
    private int m_WaitForInterrupt;
    private int[] mem; //4kb of ram
    private final int[] charSet = {
        0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
        0x20, 0x60, 0x20, 0x20, 0x70, // 1
        0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
        0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
        0x90, 0x90, 0xF0, 0x10, 0x10, // 4
        0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
        0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
        0xF0, 0x10, 0x20, 0x40, 0x40, // 7
        0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
        0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
        0xF0, 0x90, 0xF0, 0x90, 0x90, // A
        0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
        0xF0, 0x80, 0x80, 0x80, 0xF0, // C
        0xE0, 0x90, 0x90, 0x90, 0xE0, // D
        0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
        0xF0, 0x80, 0xF0, 0x80, 0x80  // F
    };
    int X;
    int Y;
    private pStack cst; //16-bit stack
    private Boolean playSound;
    ToneGenerator tg;
    Random rand;
    MachineType currentMachine;
    private Instruction[] _0x0Instructions;
    private Instruction[] _0x8Instructions;
    private Instruction[] _0xDInstructions;
    private Instruction[] _0xEInstructions;
    private Instruction[] _0xFInstructions;
    //Default machine is COSMAC VIP
    //switch table structure derived from: https://github.com/brokenprogrammer/CHIP-8-Emulator
    public Chip8SOC(Boolean sound, MachineType m) throws FileNotFoundException, IOException { 
        currentMachine = m;
        rand = new Random();
        DISPLAY_WIDTH = m.getDisplayWidth();
        DISPLAY_HEIGHT = m.getDisplayHeight();       
        vfOrderQuirks = m.getQuirks(0);
        shiftQuirks = m.getQuirks(1);
        logicQuirks = m.getQuirks(2);
        loadStoreQuirks = m.getQuirks(3);
        clipQuirks = m.getQuirks(4);
        vBlankQuirks = m.getQuirks(5);
        IOverflowQuirks = m.getQuirks(6);
        cycles = 20;
        playSound = sound;
        fillInstructionTable();
    }
    
    public void fillInstructionTable(){
       int i;
       _0x0Instructions = new Instruction[0xFE];
       for(i = 0; i < _0x0Instructions.length;i++){
          _0x0Instructions[i] = () -> C8INST_UNKNOWN();
       }
       
       _0x0Instructions[0xE0] = () -> C8INST_00E0();
       _0x0Instructions[0xEE] = () -> C8INST_00EE(); 
       
       _0x8Instructions = new Instruction[0xF];
       for(i = 0; i < _0x8Instructions.length ;i++){
          _0x8Instructions[i] = () -> C8INST_UNKNOWN();
       }
       _0x8Instructions[0x0] = () -> C8INST_8XY0();
       _0x8Instructions[0x1] = () -> C8INST_8XY1();
       _0x8Instructions[0x2] = () -> C8INST_8XY2();
       _0x8Instructions[0x3] = () -> C8INST_8XY3();
       _0x8Instructions[0x4] = () -> C8INST_8XY4();
       _0x8Instructions[0x5] = () -> C8INST_8XY5();
       _0x8Instructions[0x6] = () -> C8INST_8XY6();
       _0x8Instructions[0x7] = () -> C8INST_8XY7();
       _0x8Instructions[0xE] = () -> C8INST_8XYE();
       
       _0xDInstructions = new Instruction[0x10];
       _0xDInstructions[0x0] = () -> C8INST_DXY0();
       for(i = 0x1; i < _0xDInstructions.length ;i++){
          _0xDInstructions[i] = () -> C8INST_DXYN();
       }
              
       _0xEInstructions = new Instruction[0xF];
       for(i = 0; i < _0xEInstructions.length ;i++){
          _0xEInstructions[i] = () -> C8INST_UNKNOWN();
       }
       
       _0xEInstructions[0x1] = () -> C8INST_EXA1();
       _0xEInstructions[0xE] = () -> C8INST_EX9E();
       
       _0xFInstructions = new Instruction[0x86];
       for(i = 0; i < _0xFInstructions.length ;i++){
          _0xFInstructions[i] = () -> C8INST_UNKNOWN();
       }
       _0xFInstructions[0x07] = () -> C8INST_FX07();
       _0xFInstructions[0x15] = () -> C8INST_FX15();
       _0xFInstructions[0x18] = () -> C8INST_FX18();
       _0xFInstructions[0x1E] = () -> C8INST_FX1E();
       _0xFInstructions[0x0A] = () -> C8INST_FX0A();
       _0xFInstructions[0x29] = () -> C8INST_FX29();
       _0xFInstructions[0x33] = () -> C8INST_FX33();
       _0xFInstructions[0x55] = () -> C8INST_FX55();
       _0xFInstructions[0x65] = () -> C8INST_FX65();
       
       
    }
    
    public void chip8Init(){
        v = new int[16];
        mem = new int[4096];
        graphics = new int[DISPLAY_WIDTH*DISPLAY_HEIGHT];
        keyPad = new boolean[16];
        for(int c = 0;c<charSet.length;c++){
            mem[0x50+c] = (short) charSet[c];
        }
        dT = 0;
        sT = 0;
        pc = 0x200;
        opcode = 0;
        I = 0;
        cst = new pStack(12);
        X = 0;
        Y = 0;
        m_WaitForInterrupt = 0;
    }
    
    public boolean loadROM(File rom) throws IOException, FileNotFoundException{
        Boolean romStatus = false;
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(rom)));
            int offset = 0x0;
            int currByte = 0;
            chip8Init();
            while (currByte != -1) {
                currByte = in.read();
                mem[0x200 + offset] = currByte & 0xFF;
                offset += 0x1;
            }            
//            for (int i = 0; i < 0x900; i++) {
//                if (i % 10 == 0 && i != 0) {
//                    System.out.println(Integer.toHexString(0x195 + i).toUpperCase());
//                    System.out.print("\n");
//                }
//                System.out.print(Integer.toHexString(mem[0x195 + i]) + "\t");
//            }           
            romStatus = true;
        }catch(FileNotFoundException fnfe){
            throw fnfe;
        }catch(IOException ioe){
            throw ioe;
        }
        return romStatus;
    }
    
    public void updateTimers(){
        if(playSound){
            if (sT > 0) {
                //System.out.println(sT);
                tg.playSound();
            } else {
                //System.out.println(sT);
                tg.pauseSound();
            }
        }
        if(dT > 0){
            dT--;
        }
        if(sT > 0){
            sT--;
        }
          
    }
    
    public void keyPressed(KeyEvent e){
            if(keyPad == null){
                return;
            }
            int keyCode = e.getKeyCode();
            switch(keyCode){
                case KeyEvent.VK_X:
                    keyPad[0] = true;
                    break;
                case KeyEvent.VK_1:
                    keyPad[1] = true;
                    break;
                case KeyEvent.VK_2:
                    keyPad[2] = true;
                    break;
                case KeyEvent.VK_3:
                    keyPad[3] = true;
                    break;
                case KeyEvent.VK_Q:
                    keyPad[4] = true;
                    break;
                case KeyEvent.VK_W:
                    keyPad[5] = true;
                    break;
                case KeyEvent.VK_E:
                    keyPad[6] = true;
                    break;
                case KeyEvent.VK_A:
                    keyPad[7] = true;
                    break;
                case KeyEvent.VK_S:
                    keyPad[8] = true;
                    break;
                case KeyEvent.VK_D:
                    keyPad[9] = true;
                    break;
                case KeyEvent.VK_Z:
                    keyPad[10] = true;
                    break;
                case KeyEvent.VK_C:
                    keyPad[11] = true;
                    break;
                case KeyEvent.VK_4:
                    keyPad[12] = true;
                    break;
                case KeyEvent.VK_R:
                    keyPad[13] = true;
                    break;
                case KeyEvent.VK_F:
                    keyPad[14] = true;
                    break;
                case KeyEvent.VK_V:
                    keyPad[15] = true;
                    break;
            }
//            for(int i = 0;i < keyPad.length;i++){
//                System.out.println(keyPad[i] + "\t");
//            }
//            System.out.println("");
        }

        public void keyReleased(KeyEvent e){
            if(keyPad == null){
                return;
            }
            int keyCode = e.getKeyCode();
            
            switch(keyCode){
                case KeyEvent.VK_X:
                    keyPad[0] = false;
                    break;
                case KeyEvent.VK_1:
                    keyPad[1] = false;
                    break;
                case KeyEvent.VK_2:
                    keyPad[2] = false;
                    break;
                case KeyEvent.VK_3:
                    keyPad[3] = false;
                    break;
                case KeyEvent.VK_Q:
                    keyPad[4] = false;
                    break;
                case KeyEvent.VK_W:
                    keyPad[5] = false;
                    break;
                case KeyEvent.VK_E:
                    keyPad[6] = false;
                    break;
                case KeyEvent.VK_A:
                    keyPad[7] = false;
                    break;
                case KeyEvent.VK_S:
                    keyPad[8] = false;
                    break;
                case KeyEvent.VK_D:
                    keyPad[9] = false;
                    break;
                case KeyEvent.VK_Z:
                    keyPad[10] = false;
                    break;
                case KeyEvent.VK_C:
                    keyPad[11] = false;
                    break;
                case KeyEvent.VK_4:
                    keyPad[12] = false;
                    break;
                case KeyEvent.VK_R:
                    keyPad[13] = false;
                    break;
                case KeyEvent.VK_F:
                    keyPad[14] = false;
                    break;
                case KeyEvent.VK_V:
                    keyPad[15] = false;
                    break;
            }
//                       for (int i = 0; i < keyPad.length; i++) {
//                System.out.println(keyPad[i] + "\t");
//            }
//            System.out.println("");
        }
        
        
    
    /*
    * COSMAC VIP vBlank Quirk derived from: https://github.com/lesharris/dorito   
    */

    public Boolean WaitForInterrupt() {
        if (!vBlankQuirks) {
            return false;
        }

        switch (m_WaitForInterrupt) {
            case 0:
                m_WaitForInterrupt = 1;
                return true;
            case 1:
                return true;
            default:
                m_WaitForInterrupt = 0;
                return false;
        }
    }
    public int getMachineWidth(){
        return DISPLAY_WIDTH;
    }
    public int getMachineHeight(){
        return DISPLAY_HEIGHT;
    }
    
    public int getCycles(){
        return cycles;
    }
    
    public void setVBLankInterrupt(int status){
        m_WaitForInterrupt = status;
    }
    
    public int getVBLankInterrupt(){
        return m_WaitForInterrupt;
    }
    
    
    
    public void enableSound() throws IOException,LineUnavailableException, UnsupportedAudioFileException {
        if(tg != null && playSound)
            return;
        if(tg == null){
            playSound = true;
            try {
                tg = new ToneGenerator(playSound);
            } catch (LineUnavailableException | UnsupportedAudioFileException ex) {
                tg = null;
                playSound = false;
                throw ex;
            }
        }else{
            playSound = true;
        }
    }
    public void disableSound(){
        playSound = false;
    }
    
    public void playSound(){
        tg.playSound();
    }
    
    public void pauseSound(){
        tg.pauseSound();
    }
    
    public Boolean isSoundEnabled(){
        return playSound;
    }
    
    //carry operations for 8xxx series opcodes. Derived from OCTO
    public void writeCarry(int dest, int value, boolean flag){
        v[dest] = (value & 0xFF);
        v[0xF] = flag? 1:0;
        //enable vF quirk for certain programs
        if(vfOrderQuirks){
            v[dest] = (value & 0xFF);
        }
    }
    //cpu cycle
    public void cpuExec() {
        //fetch
        //grab opcode and combine them
        opcode = (mem[pc] << 8 | mem[pc+1]);
        //System.out.println(pc);
        X = ((opcode & 0x0F00) >> 8) & 0xF;
        //System.out.println(X);
        Y = ((opcode & 0x00F0) >> 4) & 0xF;
        System.out.println(Integer.toHexString(X));
        //System.out.println(Integer.toHexString(mem[pc]));
        //System.out.println(Integer.toHexString(mem[pc+1]));
        //decode
        c8Instructions[(opcode & 0xF000) >> 12].execute();

        
        
    }
    
    private Instruction[] c8Instructions = new Instruction[]{
        ()-> C8INSTSET_0000(),
        ()-> C8INST_1NNN(),
        ()-> C8INST_2NNN(),
        ()-> C8INST_3XNN(),
        ()-> C8INST_4XNN(),
        ()-> C8INST_5XY0(),
        ()-> C8INST_6XNN(),
        ()-> C8INST_7XNN(),
        ()-> C8INSTSET_8000(),
        ()-> C8INST_9XY0(),
        ()-> C8INST_ANNN(),
        ()-> C8INST_BNNN(),
        ()-> C8INST_CXNN(),
        ()-> C8INSTSET_DXY(),
        ()-> C8INSTSET_E000(),
        ()-> C8INSTSET_F000()
    };
    
    private void C8INST_UNKNOWN(){
        System.out.println("Unknown Opcode: " + Integer.toHexString(opcode));
    }
    
    private void C8INSTSET_0000(){
        _0x0Instructions[(opcode & 0xFF)].execute();
    }
    
    private void C8INST_00E0(){
        for (int x = 0; x < graphics.length; x++) {
            graphics[x] = 0;
        }
        pc += 2;
    }
    
    private void C8INST_00EE(){
        pc = cst.pop();
        pc += 2;
    }
    
    private void C8INST_1NNN(){
        pc = (opcode & 0x0FFF);
    }
    
    private void C8INST_2NNN(){
        cst.push(pc);
        pc = (opcode & 0x0FFF);
    }
    
    private void C8INST_3XNN(){
        if (v[X] == (opcode & 0x00FF)) {
            pc += 4;
        } else
            pc += 2;
    }
    
    private void C8INST_4XNN(){
        if (v[X] != (opcode & 0x00FF)) {
            pc += 4;
        } else
            pc += 2;
    }
    
    private void C8INST_5XY0(){
        if (v[X] == v[Y]) {
            pc += 4;
        } else
            pc += 2;
    }
    
    private void C8INST_6XNN(){
        v[X] = (opcode & 0x00FF) & 0xFF;
        pc += 2;
    }
    
    private void C8INST_7XNN(){
        v[X] = (v[X] +(opcode & 0x00FF)) & 0xFF;
        pc += 2;
    }
    
//    private Instruction[] _0x8Instructions = new Instruction[]{
//        () -> C8INST_8XY0(),
//        () -> C8INST_8XY1(),
//        () -> C8INST_8XY2(),
//        () -> C8INST_8XY3(),
//        () -> C8INST_8XY4(),
//        () -> C8INST_8XY5(),
//        () -> C8INST_8XY6(),
//        () -> C8INST_8XY7(),
//        null,
//        null,
//        null,
//        null,
//        null,
//        null,
//        () -> C8INST_8XYE(),
//        null
//    };
    
    private void C8INSTSET_8000(){
        _0x8Instructions[(opcode & 0xF)].execute();
    }
    
    private void C8INST_8XY0(){
        v[X] = (v[Y] & 0xFF);
        pc += 2;
    }
    
    private void C8INST_8XY1(){
        v[X] = (v[X] | v[Y]) & 0xFF;
        if (logicQuirks) {
            v[0xF] = 0;
        }
        pc += 2;
    }
    
    private void C8INST_8XY2(){
        v[X] = (v[X] & v[Y]) & 0xFF;
        if (logicQuirks) {
            v[0xF] = 0;
        }
        pc += 2;
    }
    
    private void C8INST_8XY3(){
        v[X] = (v[X] ^ v[Y]) & 0xFF;
        if (logicQuirks) {
            v[0xF] = 0;
        }
        pc += 2; 
    }
    
    private void C8INST_8XY4(){
        int sum = (v[X] + v[Y]);
        v[X] = sum & 0xFF;
        writeCarry(X, sum, (sum > 0xFF));
        pc += 2;
    }
    
    private void C8INST_8XY5(){
        int diff1 = (v[X] - v[Y]);
        v[X] = diff1 & 0xFF;
        writeCarry(X, diff1, (diff1 >= 0x0));
        pc += 2;
    }
    
    private void C8INST_8XY6(){
        if (shiftQuirks) {
            Y = X;
        }

        int set = v[Y] >> 1;
        writeCarry(X, set, (v[Y] & 0x1) == 0x1);
        pc += 2;
    }
    
    private void C8INST_8XY7(){
        int diff2 = (v[Y] - v[X]);
        v[X] = diff2 & 0xFF;
        writeCarry(X, diff2, (diff2 >= 0x0));
        pc += 2;
    }
    
    private void C8INST_8XYE(){
        if (shiftQuirks) {
            Y = X;
        }
        int set2 = v[Y] << 1;
        writeCarry(X, set2, ((v[Y] >> 7) & 0x1) == 0x1);
        pc += 2;
    }
     
    private void C8INST_9XY0(){
        if (v[X] != v[Y]) {
            pc += 4;
        } else
            pc += 2;
    }
    
    private void C8INST_ANNN() {
        I = (opcode & 0x0FFF);
        pc += 2;
    }

    private void C8INST_BNNN() {
        pc = ((opcode & 0x0FFF) + v[0x0]) & 0xFFFF;
    }

    private void C8INST_CXNN() {
        v[X] = (rand.nextInt(0x100) & (opcode & 0x00FF)) & 0xFF;
        pc += 2;
    }
//    private Instruction[] _0xDInstructions = new Instruction[]{
//        () -> C8INST_DXY0(),
//        () -> C8INST_DXYN(),
//        () -> C8INST_DXYN(),
//        () -> C8INST_DXYN(),
//        () -> C8INST_DXYN(),
//        () -> C8INST_DXYN(),
//        () -> C8INST_DXYN(),
//        () -> C8INST_DXYN(),
//        () -> C8INST_DXYN(),
//        () -> C8INST_DXYN(),
//        () -> C8INST_DXYN(),
//        () -> C8INST_DXYN(),
//        () -> C8INST_DXYN(),
//        () -> C8INST_DXYN(),
//        () -> C8INST_DXYN(),
//        () -> C8INST_DXYN(),
//    };
    private void C8INSTSET_DXY(){
        _0xDInstructions[(opcode & 0xF)].execute();
    } 
    
    private void C8INST_DXY0(){
        if(currentMachine == MachineType.COSMAC_VIP)
            C8INST_DXYN();
    } 
    
    private void C8INST_DXYN() {
        if (WaitForInterrupt()) {
            return;
        }
        int x = v[X];
        int y = v[Y];
        int n = (int) (opcode & 0x000F);
        v[0xF] = 0;

        int currPixel = 0;
        int targetPixel = 0;
        for (byte yLine = 0; yLine < n; yLine++) {

            for (byte xLine = 0; xLine < 8; xLine++) {
                currPixel = ((mem[I + yLine] >> (7 - xLine)) & 0x1);
                targetPixel = ((x + xLine) % DISPLAY_WIDTH) + ((y + yLine) % DISPLAY_HEIGHT) * DISPLAY_WIDTH;
                if (clipQuirks) {
                    if ((x % DISPLAY_WIDTH) + xLine >= DISPLAY_WIDTH || (y % DISPLAY_HEIGHT) + yLine >= DISPLAY_HEIGHT) {
                        currPixel = 0;
                    }
                }
                //check if pixel in current sprite row is on
                if (currPixel != 0) {
                    if (graphics[targetPixel] == 1) {
                        this.graphics[targetPixel] = 0;
                        this.v[0xF] = 0x1;
                    } else {
                        graphics[targetPixel] ^= 1;
                    }
                }
            }
        }
        pc += 2;
    }
    
//    private Instruction[] _0xEInstructions = new Instruction[]{
//        null,
//        () -> C8INST_EXA1(),
//        null,
//        null,
//        null,
//        null,
//        null,
//        null,
//        null,
//        null,
//        null,
//        null,
//        null,
//        null,
//        () -> C8INST_EX9E(),
//        null
//    };
    
    private void C8INSTSET_E000(){
        _0xEInstructions[(opcode & 0xF)].execute();
    } 
    
    private void C8INST_EX9E(){
        if (keyPad[v[X]]) {
            pc += 4;
        } else {
            pc += 2;
        }
    }
    
    private void C8INST_EXA1(){
        if (!keyPad[v[X]]) {
            pc += 4;
        } else {
            pc += 2;
        }
    }
    
    private void C8INSTSET_F000(){
        _0xFInstructions[(opcode & 0xFF)].execute();
    }
    
    private void C8INST_FX07(){
        v[X] = (dT & 0xFF);
        pc+=2;
    }
    
    private void C8INST_FX15(){
        dT = (v[X] & 0xFF);
        pc+=2;
    }
    
    private void C8INST_FX18(){
        sT = (v[X] & 0xFF);
        pc+=2;
    }
    
    private void C8INST_FX1E(){
        if (IOverflowQuirks) {
            I += v[X] & 0xFFF;
            if (I >= 0x1000) {
                v[0xF] = 1;
            }
        } else {
            //Original Behaviour of the COSMAC VIP
            I += v[X] & 0xFFF;
        }
        pc += 2;
    }
    
    private void C8INST_FX0A(){
        for (byte key = 0; key < keyPad.length; key++) {
            if (keyPad[key]) {
                v[X] = (key & 0xFF);
                pc += 2;
            }
        }
    }
    
    private void C8INST_FX29(){
        I = ((v[X]*5) +  0x50);
        pc +=2;
    }
    
    private void C8INST_FX33(){
        int num = (v[X] & 0xFF);
        mem[I] = ((num / 100) % 10);//hundreds
        mem[I + 1] = ((num / 10) % 10);//tens
        mem[I + 2] = ((num % 10));//ones
        pc += 2;
    }
    
    private void C8INST_FX55(){
        for (int i = 0; i <= X; i++) {
            mem[I + i] = (v[i] & 0xFF);
        }
        if (!loadStoreQuirks) {
            I = (I + X + 1) & 0xFFFF;
        }
        pc += 2;
    }
    
    private void C8INST_FX65(){
        for (int i = 0; i <= X; i++) {
            v[i] = (mem[I + i] & 0xFF);
        }
        if (!loadStoreQuirks) {
            I = (I + X + 1) & 0xFFFF;
        }
        pc += 2; 
    }
    
    
    @FunctionalInterface
    interface Instruction{
        public void execute();
    }
}
