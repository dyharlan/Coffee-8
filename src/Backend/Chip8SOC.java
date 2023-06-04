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
import javax.swing.*;

public class Chip8SOC extends KeyAdapter implements Runnable{
    JPanel screen;
    Thread cpuCycleThread;
    Boolean isRunning;
    Boolean vfOrderQuirks;
    Boolean shiftQuirks;
    Boolean logicQuirks;
    Boolean loadStoreQuirks;
    Boolean clipQuirks;
    Boolean vBlankQuirks;
    int cycles;
    private int pc; //16-bit Program Counter
    private int I; //16-bit Index register
    private int opcode;
    private int dT; //8-bit delay timer
    private int sT; //sound timer
    private int[] v; //cpu registers
    public int[] graphics; //screen grid??
    public boolean[] keyPad = new boolean[16]; 
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
    //COSMAC VIP
    public Chip8SOC(File rom, JPanel screen, Boolean sound) throws FileNotFoundException, IOException,LineUnavailableException, UnsupportedAudioFileException {
        this(rom, sound);
        this.screen = screen;
        vfOrderQuirks = false;
        shiftQuirks = false;
        logicQuirks = true;
        loadStoreQuirks = false;
        clipQuirks = false;
        vBlankQuirks = false;
        cycles = 500;
    }
    //switch table structure derived from: https://github.com/brokenprogrammer/CHIP-8-Emulator
    private Chip8SOC(File rom, Boolean sound) throws FileNotFoundException, IOException,LineUnavailableException, UnsupportedAudioFileException { 
        isRunning = true;
        playSound = sound;
        tg = new ToneGenerator(sound);
        v = new int[16];
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(rom)));
        graphics = new int[64*32];
        mem = new int[4096];
        for(int i = 0;i<charSet.length;i++){
            mem[0x50+i] = (short) charSet[i];
        }
        int offset = 0x0;
        int currByte = 0;
        while(currByte != -1){
            currByte = in.read();
            mem[0x200 + offset] = currByte & 0xFF;
            offset+=0x1;
        }
        for(int i = 0;i < 0x900;i++){
            if(i % 10 == 0 && i != 0){
                System.out.println(Integer.toHexString(0x195+i).toUpperCase());
                System.out.print("\n");
            }
                System.out.print(Integer.toHexString(mem[0x195+i]) + "\t"); 
        }
        dT = 0;
        sT = 0;
        pc = 0x200;
        opcode = 0;
        I = 0;
        cst = new pStack(64);
        X = 0;
        Y = 0;
        m_WaitForInterrupt = 0;
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
        switch (opcode) {
            case 0x00E0: //0x00E0
                for (int x = 0; x < graphics.length; x++) {
                    //for(int y = 0; y < graphics[x].length; y++){
                        graphics[x] = 0;  
                    //}
                }
                pc += 2;
                return;
            case 0x00EE: //Returns from a subroutine. 
                pc = cst.pop();
                pc += 2;
                return;
        }

        
        switch (opcode & 0xF000) {
            case 0x1000: //0x1NNN jump to address NNN
                pc = (opcode & 0x0FFF);
                return;

            case 0x2000: //0x2NNN calls subroutin at address NNN
                cst.push(pc);
                pc = (opcode & 0x0FFF);
                return;

            case 0x3000: //0x3XNN skip next instruction if VX == NN
                if (v[X] == (opcode & 0x00FF)) {
                    pc += 4;
                }
                else
                    pc += 2;
                return;

            //implement 4XNN, 5XY0 and 9XY0
            case 0x4000: //0x4XNN skip next instruction if VX != NN
                if (v[X] != (opcode & 0x00FF)) {
                    pc += 4;
                }
                else
                    pc += 2;
                return;

            case 0x5000: //0x5XY0 skip next instruction if VX == VY
                if (v[X] == v[Y]) {
                    pc += 4;
                }
                else
                    pc += 2;
                return;

            case 0x9000: //0x9XY0 skip next instruction if VX != VY
                if (v[X] != v[Y]) {
                    pc += 4;
                }
                else
                    pc += 2;
                return;

            case 0x6000: //0x6XNN set Vx to NN
                v[X] = (opcode & 0x00FF) & 0xFF;
                pc += 2;
                return;

            case 0x7000: //0x7XNN add NN to Vx w/o changing borrow flag
                v[X] = (v[X] +(opcode & 0x00FF)) & 0xFF;
                pc += 2;
                return;

            case 0x8000:
                switch (opcode & 0x000F) {
                    case 0x0000: //0x8XY0 set the value of Vx to Vy
                        v[X] = (v[Y] & 0xFF);
                        pc += 2;
                        return;

                    case 0x0001: //0x8XY1 set Vx to (Vx | Vy)
                        v[X] = (v[X] | v[Y]) &0xFF;
                        if(logicQuirks){
                            v[0xF] = 0;
                        }
                        pc += 2;
                        return;

                    case 0x0002: //0x8XY2 set Vx to (Vx & Vy)
                        v[X] =(v[X] & v[Y]) &0xFF;
                        if(logicQuirks){
                            v[0xF] = 0;
                        }
                        pc += 2;
                        return;

                    case 0x0003: //0x8XY3 set Vx to (Vx ^ Vy)
                        v[X] = (v[X] ^ v[Y]) &0xFF;
                        if(logicQuirks){
                            v[0xF] = 0;
                        }
                        pc += 2;
                        return;

                    case 0x0004: //0x8XY4 add Vy to Vx. VF is set to 1 if there's a carry, 0 otherwise.
                        int sum = (v[X] + v[Y]);
                        v[X] = sum & 0xFF;
//                        if (sum >= 0xFF) {
//                            v[0xF] = 1;
//                        } else {
//                            v[0xF] = 0;
//                        }
                        writeCarry(X, sum, (sum > 0xFF));
                        pc += 2;
                        return;

                    case 0x0005: //0x8XY5 subtract Vy from Vx. VF is 0 if subtrahend is smaller than minuend.
                        
                        int diff1 = (v[X] - v[Y]);  
                        v[X] = diff1 & 0xFF; 
//                        if (v[X] >= v[Y]) {
//                            v[0xF] = 1;
//                        } else {
//                            v[0xF] = 0;
//                        }
                        writeCarry(X, diff1, (diff1 >= 0x0));
                        pc += 2;
                        return;

                    case 0x0007: //0x8XY7 subtract Vx from Vy. VF is 0 if subtrahend is smaller than minuend.
                        int diff2 = (v[Y] - v[X]);
                        v[X] = diff2 & 0xFF;
//                        if (v[Y] >= v[X]) {
//                            v[0xF] = 1;
//                        } else {
//                            v[0xF] = 0;
//                        }
                        writeCarry(X, diff2, (diff2 >= 0x0));
                        pc += 2;
                        return;

                    case 0x0006: //0x8XY6 stores the LSB of VX in VF and shifts VX to the right by 1
                        if(shiftQuirks){
                            Y = X;
                        }
                        
                        int set = v[Y] >> 1;
//                        if(set){
//                            v[0xF] = 0x1;
//                        }
//                        else{
//                            v[0xF] = 0x0;
//                        }
//                        v[X] = (v[Y] >> 1) & 0xFF;
                        writeCarry(X, set, (v[Y] & 0x1) == 0x1);
                        pc += 2;
                        return;

                    case 0x000E: //0x8XYE stores the MSB of VX in VF and shifts VX to the left by 1
			if(shiftQuirks){
                            Y = X;
                        }
                        int set2 = v[Y] << 1;
                        writeCarry(X, set2, ((v[Y] >> 7) & 0x1) == 0x1);
                        pc += 2;
                        return;
                }
            case 0xA000: //0xANNN set index register to the value of NNN
                I = (opcode & 0x0FFF);
                pc += 2;
                return;
                
            case 0xB000: //0xBNNN Jumps to the address NNN plus cpu->v0
                pc = ((opcode & 0x0FFF) + v[0x0]) & 0xFFFF;
                return;

            case 0xC000: //0xCXNN generates a random number, binary ANDs it with NN, and stores it in Vx.
                Random rand = new Random();
                v[X] = (rand.nextInt(0x100) & (opcode & 0x00FF)) & 0xFF;
                pc += 2;
                return;
            /*
            * DXYN derived from Octo and https://github.com/Klairm/chip8
            */
             /*
              * COSMAC VIP vBlank Quirk derived from: https://github.com/lesharris/dorito   
             */
            case 0xD000:
//                if (WaitForInterrupt()) {
//                    return;
//                }
                int x = v[X];
                int y = v[Y];
                int n = (int) (opcode & 0x000F);
                v[0xF] = 0;
                
                int currPixel = 0;
                int targetPixel = 0;
                for (byte yLine = 0; yLine < n; yLine++) {
                    //currPixel = mem[I + yLine];
                   
                    for (byte xLine = 0; xLine < 8; xLine++) {
                        currPixel = ((mem[I+yLine] >> (7-xLine)) & 0x1);
                        targetPixel = ((x+xLine) % 64) + ((y+yLine) % 32)*64;
                        if (clipQuirks) {
                            if ((x % 64) + xLine >= 64 || (y % 32) + yLine >= 32) {
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
                return;

            case 0xE000:
                switch (opcode & 0x00FF) {
                    case 0x009E:
                        if(keyPad[v[X]]){
                            pc+=4;
                        }
                        else{
                            pc+=2;
                        }
                        return;
                    case 0x00A1:
                        if(!keyPad[v[X]]){
                            pc+=4;
                        }
                        else{
                            pc+=2;
                        }
                        return;
                    default:
                        System.out.println("Unknown OpCode: " + Integer.toHexString(opcode));
                        return;
                }
            case 0xF000:
                switch(opcode & 0x00FF){
                    case 0x0007:
                        v[X] = (dT & 0xFF);
                        pc+=2;
                    return;
                    case 0x0015:
                        dT = (v[X] & 0xFF);
                        pc+=2;
                    return;
                    case 0x0018:
                        sT = (v[X] & 0xFF);
                        pc+=2;
                    return;
                    case 0x001E:
                        I += v[X] & 0xFF;
                        if(I >= 0x1000){
                            v[0xF] = 1;
                        }
                        pc+=2;
                    return;
                    case 0x000A:
                        for(byte key = 0;key < keyPad.length;key++){
                            if(keyPad[key]){
                                v[X] = (key & 0xFF);
                                pc+=2;
                            }
                        }
                    return;
                    case 0x0029:
                        I = ((v[X]*5) +  0x50);
                        pc +=2;
                    return;
                    case 0x0033:
                        int num = (v[X] & 0xFF);
                        mem[I] = ((num / 100) % 10);//hundreds
                        mem[I+1] = ((num / 10) % 10);//tens
                        mem[I+2] = ((num % 10));//ones
                        pc+=2;
                    return;
                    case 0x0055:
                        for(int i = 0; i <= X;i++){
                            mem[I + i] = (v[i] & 0xFF);
                        }
                        if(!loadStoreQuirks){
                            I = (I+X+1) & 0xFFFF;
                        }
                        pc+=2;
                    return;
                    case 0x0065:
                        for(int i = 0; i <= X;i++){
                            v[i] = (mem[I + i] & 0xFF);
                        }
                        if(!loadStoreQuirks){
                            I = (I+X+1) & 0xFFFF;
                        }
                        pc+=2;
                    return;
                    default:
                        System.out.println("Unknown OpCode: " + Integer.toHexString(opcode));
                        return;
                }
            default:
                System.out.println("Unknown OpCode: " + Integer.toHexString(opcode));
                return;
        }
    }
    public void updateTimers(){
        
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
        
        public void start(){
            if (cpuCycleThread == null) {
                isRunning = true;
                cpuCycleThread = new Thread(this);
                cpuCycleThread.start();
            }
        }
        
       public void run() {
        cpuCycleThread.setPriority(Thread.NORM_PRIORITY);
        //long startTime = System.currentTimeMillis();
        while (isRunning) {
//            if ((System.currentTimeMillis() - startTime) >= 16) { // 60 Hz = 1000/60
//                startTime = System.currentTimeMillis();
//                if (m_WaitForInterrupt == 1) {
//                    m_WaitForInterrupt = 2;
//                }
//                if(sT > 0){
//                    tg.startSound();
//                }
//                else{
//                    tg.stopSound();
//                }
//                updateTimers();
//                cpuExec(); // decrease the timers
//                screen.repaint();
//            } else {
//                if (m_WaitForInterrupt == 1) {
//                    m_WaitForInterrupt = 2;
//                }
//                cpuExec(); // don't decrease the timers
//                screen.repaint();
//            }
            for (int i = 0; i < cycles; i++) {
                cpuExec();
            }
            if (sT > 0) {
                System.out.println(sT);
                tg.startSound();
            } else {
                System.out.println(sT);
                tg.stopSound();
            }
            updateTimers();
            try {
                //cpuCycleThread.sleep(msToWait, nsToWait);
                cpuCycleThread.sleep(16);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            screen.repaint();
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
    
}
