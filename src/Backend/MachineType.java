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
//Meaning of each quirk:
//     vfOrderQuirks - reset destination register to the value after vF has been set.
//     shiftQuirks - Instead of modifying vY on 8xy6 and 8xyE, we modify vX instead.
//     logicQuirks - reset vF on 8xy1, 8xy2, 8xy3
//     loadStoreQuirks - Increment I by vX;
//     clipQuirks - Clip pixels instead of wrapping it;
//     vBlankQuirks - wait for the start of next frame during screen redraws. Relevant to COSMAP VIP and lowres mode of old SCHIP;
//     IOverflowQuirks - VF is set to 1 if I exceeds 0xFFF, outside of the 12-bit addressing range.
//     jumpQuirks - Instead of jumping to NNN + v0, we jump to NNN + vX;
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
    //SuperChip-Compat 1.1 settings
    //     vfOrderQuirks = false;
//     shiftQuirks = true;
//     logicQuirks = false;
//     loadStoreQuirks = true;
//     clipQuirks = true;
//     vBlankQuirks = true;
//     IOverflowQuirks = true;
//     jumpQuirks = true;
    SUPERCHIP_1_1_COMPAT("Super-Chip 1.1 with COSMAC VIP Compatibility",64,32, 50,new boolean[]{false,true,false,true,true,true,true,true}),
//XO-Chip settings
//     vfOrderQuirks = false;
//     shiftQuirks = false;
//     logicQuirks = false;
//     loadStoreQuirks = false;
//     clipQuirks = false;
//     vBlankQuirks = false;
//     IOverflowQuirks = false;
//     jumpQuirks = false;
    XO_CHIP("XO-Chip",64,32, 200,new boolean[]{false,false,false,false,false,false,false,false}),

    NONE("None",64,32, -1,new boolean[]{false,false,false,false,false,false,false,false});
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
