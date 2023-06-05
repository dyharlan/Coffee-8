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
    File rom;
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
    private boolean[] keyPad = new boolean[16]; 
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
    
//    public Chip8SOC(File rom, Boolean sound) throws FileNotFoundException, IOException,LineUnavailableException, UnsupportedAudioFileException {
//        this(rom, sound);
//        
//    }
    //Default machine is COSMAC VIP
    //switch table structure derived from: https://github.com/brokenprogrammer/CHIP-8-Emulator
    public Chip8SOC(Boolean sound, MachineType m) throws FileNotFoundException, IOException { 
        DISPLAY_WIDTH = m.getDisplayWidth();
        DISPLAY_HEIGHT = m.getDisplayHeight();       
        graphics = new int[DISPLAY_WIDTH*DISPLAY_HEIGHT];
        vfOrderQuirks = m.getQuirks(0);
        shiftQuirks = m.getQuirks(1);
        logicQuirks = m.getQuirks(2);
        loadStoreQuirks = m.getQuirks(3);
        clipQuirks = m.getQuirks(4);
        vBlankQuirks = m.getQuirks(5);
        IOverflowQuirks = m.getQuirks(6);
        cycles = 1000;
        playSound = sound;
//        try{
//            tg = new ToneGenerator(sound);
//        }catch(LineUnavailableException | UnsupportedAudioFileException ex){
//            playSound = false;
//            throw ex;
//        }
    }
    
    private void chip8Init(){
        v = new int[16];
        mem = new int[4096];
        for(int i = 0;i<charSet.length;i++){
            mem[0x50+i] = (short) charSet[i];
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
    public boolean loadRom(File rom) throws IOException, FileNotFoundException{
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
            
            for (int i = 0; i < 0x900; i++) {
                if (i % 10 == 0 && i != 0) {
                    System.out.println(Integer.toHexString(0x195 + i).toUpperCase());
                    System.out.print("\n");
                }
                System.out.print(Integer.toHexString(mem[0x195 + i]) + "\t");
            }
            
            romStatus = true;
        }catch(FileNotFoundException fnfe){
            romStatus = false;
            throw fnfe;
        }catch(IOException ioe){
            romStatus = false;
            throw ioe;
        }
        return romStatus;
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
    //cpu cyle
    public void cpuExec() {
        //fetch
        //grab opcode and combine them
        opcode = (mem[pc] << 8 | mem[pc+1]);
        X = (opcode & 0x0F00) >> 8;
        Y = (opcode & 0x00F0) >> 4;
        //System.out.println("Current OpCode: " + Integer.toHexString(opcode));
        //decode
        

        
        switch (opcode & 0xF000) {
            case 0x0000:
                switch(opcode & 0x00FF){
                    case 0x00E0: //0x00E0
                        for (int x = 0; x < graphics.length; x++) {
                            graphics[x] = 0;
                        }
                        pc += 2;
                        break;
                    case 0x00EE: //Returns from a subroutine. 
                        pc = cst.pop();
                        pc += 2;
                        break;
                }
            break;
            
            case 0x1000: //0x1NNN jump to address NNN
                pc = (opcode & 0x0FFF);
                break;

            case 0x2000: //0x2NNN calls subroutin at address NNN
                cst.push(pc);
                pc = (opcode & 0x0FFF);
                break;

            case 0x3000: //0x3XNN skip next instruction if VX == NN
                if (v[X] == (opcode & 0x00FF)) {
                    pc += 4;
                }
                else
                    pc += 2;
                break;

            //implement 4XNN, 5XY0 and 9XY0
            case 0x4000: //0x4XNN skip next instruction if VX != NN
                if (v[X] != (opcode & 0x00FF)) {
                    pc += 4;
                }
                else
                    pc += 2;
                break;

            case 0x5000: //0x5XY0 skip next instruction if VX == VY
                if (v[X] == v[Y]) {
                    pc += 4;
                }
                else
                    pc += 2;
                break;

            case 0x9000: //0x9XY0 skip next instruction if VX != VY
                if (v[X] != v[Y]) {
                    pc += 4;
                }
                else
                    pc += 2;
                break;

            case 0x6000: //0x6XNN set Vx to NN
                v[X] = (opcode & 0x00FF) & 0xFF;
                pc += 2;
                break;

            case 0x7000: //0x7XNN add NN to Vx w/o changing borrow flag
                v[X] = (v[X] +(opcode & 0x00FF)) & 0xFF;
                pc += 2;
                break;

            case 0x8000:
                switch (opcode & 0x000F) {
                    case 0x0000: //0x8XY0 set the value of Vx to Vy
                        v[X] = (v[Y] & 0xFF);
                        pc += 2;
                        break;

                    case 0x0001: //0x8XY1 set Vx to (Vx | Vy)
                        v[X] = (v[X] | v[Y]) &0xFF;
                        if(logicQuirks){
                            v[0xF] = 0;
                        }
                        pc += 2;
                        break;

                    case 0x0002: //0x8XY2 set Vx to (Vx & Vy)
                        v[X] =(v[X] & v[Y]) &0xFF;
                        if(logicQuirks){
                            v[0xF] = 0;
                        }
                        pc += 2;
                        break;

                    case 0x0003: //0x8XY3 set Vx to (Vx ^ Vy)
                        v[X] = (v[X] ^ v[Y]) &0xFF;
                        if(logicQuirks){
                            v[0xF] = 0;
                        }
                        pc += 2;
                        break;

                    case 0x0004: //0x8XY4 add Vy to Vx. VF is set to 1 if there's a carry, 0 otherwise.
                        int sum = (v[X] + v[Y]);
                        v[X] = sum & 0xFF;
                        writeCarry(X, sum, (sum > 0xFF));
                        pc += 2;
                        break;

                    case 0x0005: //0x8XY5 subtract Vy from Vx. VF is 0 if subtrahend is smaller than minuend.
                        
                        int diff1 = (v[X] - v[Y]);  
                        v[X] = diff1 & 0xFF; 
                        writeCarry(X, diff1, (diff1 >= 0x0));
                        pc += 2;
                        break;

                    case 0x0007: //0x8XY7 subtract Vx from Vy. VF is 0 if subtrahend is smaller than minuend.
                        int diff2 = (v[Y] - v[X]);
                        v[X] = diff2 & 0xFF;
                        writeCarry(X, diff2, (diff2 >= 0x0));
                        pc += 2;
                        break;

                    case 0x0006: //0x8XY6 stores the LSB of VX in VF and shifts VX to the right by 1
                        if(shiftQuirks){
                            Y = X;
                        }
                        
                        int set = v[Y] >> 1;
                        writeCarry(X, set, (v[Y] & 0x1) == 0x1);
                        pc += 2;
                        break;

                    case 0x000E: //0x8XYE stores the MSB of VX in VF and shifts VX to the left by 1
			if(shiftQuirks){
                            Y = X;
                        }
                        int set2 = v[Y] << 1;
                        writeCarry(X, set2, ((v[Y] >> 7) & 0x1) == 0x1);
                        pc += 2;
                        break;
                }
                break;
            case 0xA000: //0xANNN set index register to the value of NNN
                I = (opcode & 0x0FFF);
                pc += 2;
                break;
                
            case 0xB000: //0xBNNN Jumps to the address NNN plus cpu->v0
                pc = ((opcode & 0x0FFF) + v[0x0]) & 0xFFFF;
                break;

            case 0xC000: //0xCXNN generates a random number, binary ANDs it with NN, and stores it in Vx.
                Random rand = new Random();
                v[X] = (rand.nextInt(0x100) & (opcode & 0x00FF)) & 0xFF;
                pc += 2;
                break;
            /*
            * DXYN derived from Octo and https://github.com/Klairm/chip8
            */
            /*
            * COSMAC VIP vBlank Quirk derived from: https://github.com/lesharris/dorito   
            */
            case 0xD000:
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
                        currPixel = ((mem[I+yLine] >> (7-xLine)) & 0x1);
                        targetPixel = ((x+xLine) % DISPLAY_WIDTH) + ((y+yLine) % DISPLAY_HEIGHT)*DISPLAY_WIDTH;
                        if (clipQuirks) {
                            if ((x % DISPLAY_WIDTH) + xLine >= DISPLAY_WIDTH || (y % DISPLAY_HEIGHT) + yLine >= DISPLAY_HEIGHT) {
                                currPixel = 0;
                            }
                        }
                        //check if pixel in current sprite row is on
                        if (currPixel != 0) {
                            if(graphics[targetPixel] == 1){
                                this.graphics[targetPixel] = 0;
                                this.v[0xF] = 0x1;
                            }
                            else{
                                graphics[targetPixel] ^= 1;
                            }                      
                        }
                    }
                }
                  
                
                pc += 2;
                break;
                
            case 0xE000:
                switch (opcode & 0x00FF) {
                    //EX9E Skip one instruction when key is pressed. But since pc is incremented here, we skip two.
                    case 0x009E:
                        if(keyPad[v[X]]){
                            pc+=4;
                        }
                        else{
                            pc+=2;
                        }
                        break;
                    //EXA1 Skip one instruction when key is not pressed. But since pc is incremented here, we skip two.
                    case 0x00A1:
                        if(!keyPad[v[X]]){
                            pc+=4;
                        }
                        else{
                            pc+=2;
                        }
                        break;
                    default:
                        System.out.println("Unknown OpCode: " + Integer.toHexString(opcode));
                        break;
                }
                break;
            case 0xF000:
                switch(opcode & 0x00FF){
                    //FX07: Set vX to the value of the delay timer
                    case 0x0007:
                        v[X] = (dT & 0xFF);
                        pc+=2;
                    break;
                    //FX15: set the delay timer to the value in vX
                    case 0x0015:
                        dT = (v[X] & 0xFF);
                        pc+=2;
                    break;
                    //FX18: set the sound timer to the value in vX
                    case 0x0018:
                        sT = (v[X] & 0xFF);
                        pc+=2;
                    break;
                    //FX1E: Add the value of VX to the register index
                    //IF IOverflowQuirks is on:
                    //VF is set to 1 if I exceeds 0xFFF, outside of the 12-bit addressing range
                    //of the chip8
                    //Apparently, this is needed for one game? idk
                    case 0x001E:
                        if (IOverflowQuirks) {
                            I += v[X] & 0xFFF;
                            if (I >= 0x1000) {
                                v[0xF] = 1;
                            }
                        }else{
                            //Original Behaviour of the COSMAC VIP
                            I += v[X] & 0xFFF;
                        }
                        pc+=2;
                    break;
                    //FX0A: Stops program execution until a key is pressed.
                    case 0x000A:
                        for(byte key = 0;key < keyPad.length;key++){
                            if(keyPad[key]){
                                v[X] = (key & 0xFF);
                                pc+=2;
                            }
                        }
                    break;
                    //FX29: Point index register to font in memory
                    case 0x0029:
                        I = ((v[X]*5) +  0x50);
                        pc +=2;
                    break;
                    //FX33: Get number from vX and
                    //store hundreds digit in memory point by I
                    //store tens digit in memory point by I+1
                    //store ones digit in memory point by I+2
                    case 0x0033:
                        int num = (v[X] & 0xFF);
                        mem[I] = ((num / 100) % 10);//hundreds
                        mem[I+1] = ((num / 10) % 10);//tens
                        mem[I+2] = ((num % 10));//ones
                        pc+=2;
                    break;
                    //FX55: store the values of registers v0 to vi, where i is the ith register to use
                    //in successive memory locations
                    case 0x0055:
                        for(int i = 0; i <= X;i++){
                            mem[I + i] = (v[i] & 0xFF);
                        }
                        if(!loadStoreQuirks){
                            I = (I+X+1) & 0xFFFF;
                        }
                        pc+=2;
                    break;
                    //FX55: store the values of successive memory locations from 0 to i, where i is the ith memory location
                    //in i registers from v0 to vi
                    case 0x0065:
                        for(int i = 0; i <= X;i++){
                            v[i] = (mem[I + i] & 0xFF);
                        }
                        if(!loadStoreQuirks){
                            I = (I+X+1) & 0xFFFF;
                        }
                        pc+=2;
                    break;
                    default:
                        System.out.println("Unknown OpCode: " + Integer.toHexString(opcode));
                        break;
                }
                break;
            default:
                System.out.println("Unknown OpCode: " + Integer.toHexString(opcode));
                break;
        }
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
    
    
}
