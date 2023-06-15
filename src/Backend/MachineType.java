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
package Backend;

/**
 *
 * @author dyharlan
 */
public enum MachineType {
//cosmac VIP settings
//     vfOrderQuirks = false;
//     shiftQuirks = false;
//     logicQuirks = true;
//     loadStoreQuirks = false;
//     clipQuirks = true;
//     vBlankQuirks = true;
//     IOverflowQuirks = true;
//     jumpQuirks = false;

    COSMAC_VIP("COSMAC VIP",64,32, 20,new boolean[]{false,false,true,false,true,true,true,false}),
    
//SuperChip 1.1 settings
//     vfOrderQuirks = false;
//     shiftQuirks = true;
//     logicQuirks = false;
//     loadStoreQuirks = true;
//     clipQuirks = true;
//     vBlankQuirks = false;
//     IOverflowQuirks = true;
//     jumpQuirks = true;
    SUPERCHIP_1_1("Super-Chip 1.1",64,32, 50,new boolean[]{false,true,false,true,true,false,true,true}),
    
//XO-Chip settings
//     vfOrderQuirks = false;
//     shiftQuirks = false;
//     logicQuirks = false;
//     loadStoreQuirks = false;
//     clipQuirks = false;
//     vBlankQuirks = false;
//     IOverflowQuirks = false;
//     jumpQuirks = false;
    XO_CHIP("XO-Chip",64,32, 200,new boolean[]{false,false,false,false,false,false,false,false});
    private final String MACHINE_NAME;
    private final int DISPLAY_WIDTH;
    private final int DISPLAY_HEIGHT;
    private final boolean[] MACHINE_QUIRKS;
    private final int cycles;

   
    public String getMachineName() {
        return MACHINE_NAME;
    }

    public int getDisplayWidth() {
        return DISPLAY_WIDTH;
    }

    public int getDisplayHeight() {
        return DISPLAY_HEIGHT;
    }
    
    public int getCycles() {
        return cycles;
    }
    
    public boolean getQuirks(int i) {
        return MACHINE_QUIRKS[i];
    }
    
    MachineType(String machineName,int displayW, int displayH, int cycles,boolean quirks[]){
        this.MACHINE_NAME = machineName;
        DISPLAY_WIDTH = displayW;
        DISPLAY_HEIGHT = displayH;
        this.cycles = cycles;
        this.MACHINE_QUIRKS = quirks;
    }
    
    public String toString(){
        return MACHINE_NAME;
    }
}
