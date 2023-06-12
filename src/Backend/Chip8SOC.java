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
import java.io.*;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Chip8SOC{

    private int DISPLAY_WIDTH;
    private int DISPLAY_HEIGHT;
    private Boolean vfOrderQuirks;
    private Boolean shiftQuirks;
    private Boolean logicQuirks;
    private Boolean loadStoreQuirks;
    private Boolean clipQuirks;
    private Boolean vBlankQuirks;
    private Boolean IOverflowQuirks;
    private Boolean jumpQuirks;
    private int waitReg;
    private Boolean waitState;
    private int cycles;
    private int pc; //16-bit Program Counter
    private int I; //12-bit Index register
    private int opcode;
    private int dT; //8-bit delay timer
    private int sT; //sound timer
    private int[] v; //cpu registers
    public int[] graphics; //screen grid??
    public boolean[] keyPad; 
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
        0xF0, 0x80, 0xF0, 0x80, 0x80,  // F
        
        //Hi-Res fonts
        0x3C, 0x7E, 0xE7, 0xC3, 0xC3, 0xC3, 0xC3, 0xE7, 0x7E, 0x3C, // "0"
        0x18, 0x38, 0x58, 0x18, 0x18, 0x18, 0x18, 0x18, 0x18, 0x3C, // "1"
        0x3E, 0x7F, 0xC3, 0x06, 0x0C, 0x18, 0x30, 0x60, 0xFF, 0xFF, // "2"
        0x3C, 0x7E, 0xC3, 0x03, 0x0E, 0x0E, 0x03, 0xC3, 0x7E, 0x3C, // "3"
        0x06, 0x0E, 0x1E, 0x36, 0x66, 0xC6, 0xFF, 0xFF, 0x06, 0x06, // "4"
        0xFF, 0xFF, 0xC0, 0xC0, 0xFC, 0xFE, 0x03, 0xC3, 0x7E, 0x3C, // "5"
        0x3E, 0x7C, 0xC0, 0xC0, 0xFC, 0xFE, 0xC3, 0xC3, 0x7E, 0x3C, // "6"
        0xFF, 0xFF, 0x03, 0x06, 0x0C, 0x18, 0x30, 0x60, 0x60, 0x60, // "7"
        0x3C, 0x7E, 0xC3, 0xC3, 0x7E, 0x7E, 0xC3, 0xC3, 0x7E, 0x3C, // "8"
        0x3C, 0x7E, 0xC3, 0xC3, 0x7F, 0x3F, 0x03, 0x03, 0x3E, 0x7C // "9"            
    };
    int X;
    int Y;
    private pStack cst; //16-bit stack
    private Boolean playSound;
    private Boolean hires;
    ToneGenerator tg;
    Random rand;
    MachineType currentMachine;
    private Instruction[] _0x0Instructions;
    private Instruction[] _0x8Instructions;
    private Instruction[] _0xDInstructions;
    private Instruction[] _0xEInstructions;
    private Instruction[] _0xFInstructions;
    private int[] flags;
    //Default machine is COSMAC VIP
    //switch table structure derived from: https://github.com/brokenprogrammer/CHIP-8-Emulator
    public Chip8SOC(Boolean sound, MachineType m) throws FileNotFoundException, IOException { 
        
        rand = new Random();
        cycles = 20;
        playSound = sound;
        hires = false;
       

        setCurrentMachine(m);
        fillInstructionTable();
    }
    
    public void setCurrentMachine(MachineType m){
        currentMachine = m;
        DISPLAY_WIDTH = m.getDisplayWidth();
        DISPLAY_HEIGHT = m.getDisplayHeight();       
        vfOrderQuirks = m.getQuirks(0);
        shiftQuirks = m.getQuirks(1);
        logicQuirks = m.getQuirks(2);
        loadStoreQuirks = m.getQuirks(3);
        clipQuirks = m.getQuirks(4);
        vBlankQuirks = m.getQuirks(5);
        IOverflowQuirks = m.getQuirks(6);
        jumpQuirks = m.getQuirks(7);
    }
    
    public void fillInstructionTable(){
       int i;
       _0x0Instructions = new Instruction[0x100];
       for(i = 0; i < _0x0Instructions.length;i++){
          _0x0Instructions[i] = () -> C8INST_UNKNOWN();
       }
       _0x0Instructions[0xC0] = () -> C8INST_00CN();
       _0x0Instructions[0xC1] = () -> C8INST_00CN();
       _0x0Instructions[0xC2] = () -> C8INST_00CN();
       _0x0Instructions[0xC3] = () -> C8INST_00CN();
       _0x0Instructions[0xC4] = () -> C8INST_00CN();
       _0x0Instructions[0xC5] = () -> C8INST_00CN();
       _0x0Instructions[0xC6] = () -> C8INST_00CN();
       _0x0Instructions[0xC7] = () -> C8INST_00CN();
       _0x0Instructions[0xC8] = () -> C8INST_00CN();
       _0x0Instructions[0xC9] = () -> C8INST_00CN();
       _0x0Instructions[0xCA] = () -> C8INST_00CN();
       _0x0Instructions[0xCB] = () -> C8INST_00CN();
       _0x0Instructions[0xCC] = () -> C8INST_00CN();
       _0x0Instructions[0xCD] = () -> C8INST_00CN();
       _0x0Instructions[0xCE] = () -> C8INST_00CN();
       _0x0Instructions[0xCF] = () -> C8INST_00CN();
       _0x0Instructions[0xE0] = () -> C8INST_00E0();
       _0x0Instructions[0xEE] = () -> C8INST_00EE(); 
       _0x0Instructions[0xFB] = () -> C8INST_00FB(); 
       _0x0Instructions[0xFC] = () -> C8INST_00FC(); 
       _0x0Instructions[0xFD] = () -> C8INST_00FD(); 
       _0x0Instructions[0xFE] = () -> C8INST_00FE(); 
       _0x0Instructions[0xFF] = () -> C8INST_00FF(); 

       
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
       _0xFInstructions[0x30] = () -> C8INST_FX30();
       _0xFInstructions[0x33] = () -> C8INST_FX33();
       _0xFInstructions[0x55] = () -> C8INST_FX55();
       _0xFInstructions[0x65] = () -> C8INST_FX65();
       _0xFInstructions[0x75] = () -> C8INST_FX75();
       _0xFInstructions[0x85] = () -> C8INST_FX85();
       
       
       
    }
    
    public void chip8Init(){
        hires = false;
        v = new int[16];
        mem = new int[4096];
        graphics = new int[DISPLAY_WIDTH*DISPLAY_HEIGHT];
        flags = new int[16];
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
        waitReg = -1;
        waitState = false;
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
    
    public Boolean getHiRes(){
        return hires;
    }
    
    public MachineType getCurrentMachine(){
        return currentMachine;
    }
    
    public void setHiRes(Boolean flag){
        if(flag){
            hires = true;
            DISPLAY_WIDTH = 128;
            DISPLAY_HEIGHT = 64;
            graphics = new int[DISPLAY_WIDTH*DISPLAY_HEIGHT];
        }else if(!flag){
            hires = false;
            DISPLAY_WIDTH = 64;
            DISPLAY_HEIGHT = 32;
            graphics = new int[DISPLAY_WIDTH*DISPLAY_HEIGHT];

        }
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
    
    public void setVBLankInterrupt(int status){
        m_WaitForInterrupt = status;
    }
    
    public int getVBLankInterrupt(){
        return m_WaitForInterrupt;
    }
    
    public void setCycles(int cycleCount){
        cycles = cycleCount;
    }
    
    public int getCycles(){
        return cycles;
    }
    
    public void enableSound() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        if(tg != null && playSound)
            return;
        if(tg == null){
            playSound = true;
            try {
                tg = new ToneGenerator(playSound);
            } catch (LineUnavailableException | UnsupportedAudioFileException | IOException ex) {
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
        //System.out.println(Integer.toHexString(opcode));
        X = ((opcode & 0x0F00) >> 8) & 0xF;
        //System.out.println(X);
        Y = ((opcode & 0x00F0) >> 4) & 0xF;
        //System.out.println(Integer.toHexString(mem[pc]));
        //System.out.println(Integer.toHexString(mem[pc+1]));
        //decode
        //get 4th nibble, shift 3 nibbles to the right and use as index in the interface array
        c8Instructions[(opcode & 0xF000) >> 12].execute();

        
        
    }
    /*
    * An interface that represents an instruction to execute. Lambda statements are used, but when unrolled,
    * they look like this:
    * public void execute(){
    *   //a function that calls a specific instruction
    * }
    *
    * Idea from here: https://stackoverflow.com/questions/4280727/java-creating-an-array-of-methods
    */
    @FunctionalInterface
    interface Instruction{
        public void execute();
    }
    /*
    * Array of interfaces.
    * this is called under cpuExec() where the index is derived by
    * obtaining the 4th nibble of an opcode.
    * another array is referenced when a nibble as multiple instructions, as is the case for 0x0, 0x8, 0xD,0xE, and 0xFz
    */
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
    //this is called if the opcode executed is either unknown or unimplemented
    private void C8INST_UNKNOWN(){
        System.out.println("Unknown Opcode: " + Integer.toHexString(opcode));
    }
    //execute instructions that have 0x0 as their prefix
    private void C8INSTSET_0000(){
        _0x0Instructions[(opcode & 0xFF)].execute();
    }
    
    private void C8INST_00CN(){
        int height = opcode & 0xF;
        for (var z = graphics.length - 1; z >= 0; z--) {
            graphics[z] = (z >= DISPLAY_WIDTH * height) ? graphics[z - (DISPLAY_WIDTH * height)] : 0;
        }
        pc+=2;
    }
    //00E0: Clear Screen
    private void C8INST_00E0(){
        for (int x = 0; x < graphics.length; x++) {
            graphics[x] = 0;
        }
        pc += 2;
    }
    //00EE: Returns from a subroutine on top of the stack. 
    private void C8INST_00EE(){
        pc = cst.pop();
        pc += 2;
    }
    
    private void C8INST_00FB() {

        for (int y = 0; y < graphics.length; y += DISPLAY_WIDTH) {
            for (int x = DISPLAY_WIDTH - 1; x >= 0; x--) {
                graphics[y + x] = (x > 3) ? graphics[y + x - 4] : 0;
            }
        }
        pc+=2;
    }
    
    private void C8INST_00FC() {

        for (int y = 0; y < graphics.length; y += DISPLAY_WIDTH) {
            for (int x = 0; x < DISPLAY_WIDTH; x++) {
                graphics[y + x] = (x < DISPLAY_WIDTH - 4) ? graphics[y + x + 4] : 0;
            }
        }
        pc+=2;
    }
    //00FD: Exit interpreter
    private void C8INST_00FD(){
        System.exit(0);
    }
    private void C8INST_00FE(){
        setHiRes(false);
        for (int x = 0; x < graphics.length; x++) {
            graphics[x] = 0;
        }
        pc+=2;
    }
    private void C8INST_00FF(){
        setHiRes(true);
        for (int x = 0; x < graphics.length; x++) {
            graphics[x] = 0;
        }
        pc+=2;
    }
    //0x1NNN jump to address NNN
    private void C8INST_1NNN(){
        pc = (opcode & 0x0FFF);
    }
    //0x2NNN calls subroutin at address NNN
    private void C8INST_2NNN(){
        cst.push(pc);
        pc = (opcode & 0x0FFF);
    }
    //0x3XNN skip next instruction if VX == NN
    private void C8INST_3XNN(){
        if (v[X] == (opcode & 0x00FF)) {
            pc += 4;
        } else
            pc += 2;
    }
    //0x4XNN skip next instruction if VX != NN
    private void C8INST_4XNN(){
        if (v[X] != (opcode & 0x00FF)) {
            pc += 4;
        } else
            pc += 2;
    }
    //0x5XY0 skip next instruction if VX == VY
    private void C8INST_5XY0(){
        if (v[X] == v[Y]) {
            pc += 4;
        } else
            pc += 2;
    }
    //0x6XNN set Vx to NN
    private void C8INST_6XNN(){
        v[X] = (opcode & 0x00FF) & 0xFF;
        pc += 2;
    }
    //0x7XNN add NN to Vx w/o changing borrow flag
    private void C8INST_7XNN(){
        v[X] = (v[X] +(opcode & 0x00FF)) & 0xFF;
        pc += 2;
    }
    //Execute instructions that have 0x8 as their prefix
    private void C8INSTSET_8000(){
        _0x8Instructions[(opcode & 0xF)].execute();
    }
    //0x8XY0 set the value of Vx to Vy
    private void C8INST_8XY0(){
        v[X] = (v[Y] & 0xFF);
        pc += 2;
    }
    //0x8XY1 set Vx to (Vx | Vy)
    private void C8INST_8XY1(){
        v[X] = (v[X] | v[Y]) & 0xFF;
        if (logicQuirks) {
            v[0xF] = 0;
        }
        pc += 2;
    }
    //0x8XY2 set Vx to (Vx & Vy)
    private void C8INST_8XY2(){
        v[X] = (v[X] & v[Y]) & 0xFF;
        if (logicQuirks) {
            v[0xF] = 0;
        }
        pc += 2;
    }
    //0x8XY3 set Vx to (Vx ^ Vy)
    private void C8INST_8XY3(){
        v[X] = (v[X] ^ v[Y]) & 0xFF;
        if (logicQuirks) {
            v[0xF] = 0;
        }
        pc += 2; 
    }
    //0x8XY4 add Vy to Vx. VF is set to 1 if there's a carry, 0 otherwise.
    private void C8INST_8XY4(){
        int sum = (v[X] + v[Y]);
        v[X] = sum & 0xFF;
        writeCarry(X, sum, (sum > 0xFF));
        pc += 2;
    }
    //0x8XY5 subtract Vy from Vx. VF is 0 if subtrahend is smaller than minuend.
    private void C8INST_8XY5(){
        int diff1 = (v[X] - v[Y]);
        v[X] = diff1 & 0xFF;
        writeCarry(X, diff1, (diff1 >= 0x0));
        pc += 2;
    }
    //0x8XY6 stores the LSB of VX in VF and shifts VX to the right by 1
    private void C8INST_8XY6(){
        if (shiftQuirks) {
            Y = X;
        }

        int set = v[Y] >> 1;
        writeCarry(X, set, (v[Y] & 0x1) == 0x1);
        pc += 2;
    }
    //0x8XY7 subtract Vx from Vy. VF is 0 if subtrahend is smaller than minuend.
    private void C8INST_8XY7(){
        int diff2 = (v[Y] - v[X]);
        v[X] = diff2 & 0xFF;
        writeCarry(X, diff2, (diff2 >= 0x0));
        pc += 2;
    }
    //0x8XYE stores the MSB of VX in VF and shifts VX to the left by 1
    private void C8INST_8XYE(){
        if (shiftQuirks) {
            Y = X;
        }
        int set2 = v[Y] << 1;
        writeCarry(X, set2, ((v[Y] >> 7) & 0x1) == 0x1);
        pc += 2;
    }
    //0x9XY0 skip next instruction if VX != VY 
    private void C8INST_9XY0(){
        if (v[X] != v[Y]) {
            pc += 4;
        } else
            pc += 2;
    }
    //0xANNN set index register to the value of NNN
    private void C8INST_ANNN() {
        I = (opcode & 0x0FFF);
        pc += 2;
    }
    //0xBNNN Jumps to the address NNN plus cpu->v0
    private void C8INST_BNNN() {
        if(jumpQuirks){
            pc = ((opcode & 0x0FFF) + v[X]) & 0xFFFF;
        }else{
            pc = ((opcode & 0x0FFF) + v[0x0]) & 0xFFFF;
        }
    }
    //0xCXNN generates a random number, binary ANDs it with NN, and stores it in Vx.
    private void C8INST_CXNN() {
        v[X] = (rand.nextInt(0x100) & (opcode & 0x00FF)) & 0xFF;
        pc += 2;
    }
    //Only reason why this is an instruction subset is because of superchip.
    private void C8INSTSET_DXY(){
        _0xDInstructions[(opcode & 0xF)].execute();
    } 
    //In superchip: draw a 16x16 sprite, otherwise, draw normally
    private void C8INST_DXY0(){
        if(currentMachine == MachineType.COSMAC_VIP)
            C8INST_DXYN();
        else {
            if (WaitForInterrupt()) {
                return;
            }
            int x = v[X];
            int y = v[Y];
            v[0xF] = 0;
            int i = I;
            int currPixel = 0;
            int targetPixel = 0;
            for (byte yLine = 0; yLine < 16; yLine++) {

                for (byte xLine = 0; xLine < 16; xLine++) {
                    currPixel = ((mem[i + (yLine * 2) + (xLine > 7 ? 1 : 0)] >> (7 - (xLine % 8))) & 0x1);
                    targetPixel = ((x + xLine) % DISPLAY_WIDTH) + ((y + yLine) % DISPLAY_HEIGHT) * DISPLAY_WIDTH;
                    if (clipQuirks) {
                        if ((x % DISPLAY_WIDTH) + xLine >= DISPLAY_WIDTH || (y % DISPLAY_HEIGHT) + yLine >= DISPLAY_HEIGHT) {
                            currPixel = 0;
                        }
                    }
                    if (currPixel == 0) {
                        continue;
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
        }
        
        pc += 2;
    } 
    /*
    * DXYN derived from Octo and https://github.com/Klairm/chip8
    */
    /*
    * COSMAC VIP vBlank Quirk derived from: https://github.com/lesharris/dorito   
    */
    private void C8INST_DXYN() {
        if (WaitForInterrupt()) {
            return;
        }
        int x = v[X];
        int y = v[Y];
        int n = (int) (opcode & 0x000F);
        v[0xF] = 0;
        int i = I;
        int currPixel = 0;
        int targetPixel = 0;
        for (byte yLine = 0; yLine < n; yLine++) {

            for (byte xLine = 0; xLine < 8; xLine++) {
                currPixel = ((mem[i + yLine] >> (7 - xLine)) & 0x1);
                targetPixel = ((x + xLine) % DISPLAY_WIDTH) + ((y + yLine) % DISPLAY_HEIGHT) * DISPLAY_WIDTH;
                if (clipQuirks) {
                    if ((x % DISPLAY_WIDTH) + xLine >= DISPLAY_WIDTH || (y % DISPLAY_HEIGHT) + yLine >= DISPLAY_HEIGHT) {
                        currPixel = 0;
                    }
                }
                if (currPixel == 0) { 
                    continue; 
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
    //Execute instructions that start with 0xE as their prefix
    private void C8INSTSET_E000(){
        _0xEInstructions[(opcode & 0xF)].execute();
    } 
    //EX9E Skip one instruction when key is pressed. But since pc is incremented here, we skip two.
    private void C8INST_EX9E(){
        if (keyPad[v[X]]) {
            pc += 4;
        } else {
            pc += 2;
        }
    }
    //EXA1 Skip one instruction when key is not pressed. But since pc is incremented here, we skip two.
    private void C8INST_EXA1(){
        if (!keyPad[v[X]]) {
            pc += 4;
        } else {
            pc += 2;
        }
    }
    //Execute instructions that start with 0xF as their prefix
    private void C8INSTSET_F000(){
        _0xFInstructions[(opcode & 0xFF)].execute();
    }
    //FX07: Set vX to the value of the delay timer
    private void C8INST_FX07(){
        v[X] = (dT & 0xFF);
        pc+=2;
    }
    //FX15: set the delay timer to the value in vX
    private void C8INST_FX15(){
        dT = (v[X] & 0xFF);
        pc+=2;
    }
    //FX18: set the sound timer to the value in vX
    private void C8INST_FX18(){
        sT = (v[X] & 0xFF);
        pc+=2;
    }
    //FX1E: Add the value of VX to the register index
    //IF IOverflowQuirks is on:
    //VF is set to 1 if I exceeds 0xFFF, outside of the 12-bit addressing range
    //of the chip8
    //Apparently, this is needed for one game? idk
    private void C8INST_FX1E(){
        if (IOverflowQuirks) {
            I += v[X] & 0xFFF;
            if (I > 0xFFF) {
                v[0xF] = 1;
            }
        } else {
            //Original Behaviour of the COSMAC VIP
            I += v[X] & 0xFFF;
        }
        pc += 2;
    }
    //FX0A: Stops program execution until a key is pressed.
    private void C8INST_FX0A(){
        //for (byte key = 0; key < keyPad.length; key++) {
            //if (keyPad[key]) {
            //    v[X] = (key & 0xFF);
            //    pc += 2;
            //}
        //}
        waitState = true; waitReg = X;
    }
    
    
    
    public void sendKeyStroke(int keyValue){
        v[waitReg] = keyValue;
        pc+=2;
    }
    
    public int getWaitReg() {
        return waitReg;
    }

    public Boolean getWaitState() {
        return waitState;
    }

    public void setWaitReg(int waitReg) {
        this.waitReg = waitReg;
    }

    public void setWaitState(Boolean waitState) {
        this.waitState = waitState;
    }
    
    
    //FX29: Point index register to font in memory
    private void C8INST_FX29(){
        I = ((v[X]*5) +  0x50);
        pc +=2;
    }
    
     private void C8INST_FX30(){
        I = ((v[X]*10) +  0xA0);
        pc +=2;
    }
    //FX33: Get number from vX and
    //store hundreds digit in memory point by I
    //store tens digit in memory point by I+1
    //store ones digit in memory point by I+2
    private void C8INST_FX33(){
        int num = (v[X] & 0xFF);
        mem[I] = ((num / 100) % 10);//hundreds
        mem[I + 1] = ((num / 10) % 10);//tens
        mem[I + 2] = ((num % 10));//ones
        pc += 2;
    }
    //FX55: store the values of registers v0 to vi, where i is the ith register to use
    //in successive memory locations
    private void C8INST_FX55(){
        for (int i = 0; i <= X; i++) {
            mem[I + i] = (v[i] & 0xFF);
        }
        if (!loadStoreQuirks) {
            I = (I + X + 1) & 0xFFFF;
        }
        pc += 2;
    }
    //FX65: store the values of successive memory locations from 0 to i, where i is the ith memory location
    //in i registers from v0 to vi
    private void C8INST_FX65(){
        for (int i = 0; i <= X; i++) {
            v[i] = (mem[I + i] & 0xFF);
        }
        if (!loadStoreQuirks) {
            I = (I + X + 1) & 0xFFFF;
        }
        pc += 2; 
    }
    
     private void C8INST_FX75(){
        for(int n = 0;(n < X) || (n <= 7);n++){
            flags[n] = v[n];
        }
        pc += 2; 
    }
    
     private void C8INST_FX85(){
        for(int n = 0;(n < X) || (n <= 7);n++){
            v[n] = flags[n];
        }
        pc += 2; 
    }
    
}
