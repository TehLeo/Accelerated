/**
    Copyright (c) 2016, Juraj Papp
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:
        * Redistributions of source code must retain the above copyright
          notice, this list of conditions and the following disclaimer.
        * Redistributions in binary form must reproduce the above copyright
          notice, this list of conditions and the following disclaimer in the
          documentation and/or other materials provided with the distribution.
        * Neither the name of the copyright holder nor the
          names of its contributors may be used to endorse or promote products
          derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
    DISCLAIMED. IN NO EVENT SHALL COPYRIGHT HOLDER BE LIABLE FOR ANY
    DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package theleo.accel;
/**
 * A class that holds collision times. 
 * 
 * @author Juraj Papp
 */
public class Results {
    /**
     * times of intersections
     */
    public float[] time;
    /**
     * number of intersections
     */
    public int len = 0;
    
    /**
     * flags for certain shapes,
     * eg MeshShape stores the index for face/
     * edge/vertex collision
     */
    public int[] flags = new int[4];
    /**
     * number of flags
     */
    public int flagsLen = 0;
    
    
    
    public Results() { time = new float[4]; }
    public Results(int maxlen) {
        time = new float[maxlen];
    }
    public void add(float f1, float f2) {
        time[len++] = f1;
        time[len++] = f2;
    }
    public void set(float f1, float f2) {
        time[0] = f1;
        time[1] = f2;
        len = 2;
    }
    public void set(float f1, float f2, float f3, float f4) {
        time[0] = f1;
        time[1] = f2;
        time[2] = f3;
        time[3] = f4;
        len = 4;
    }
    public float[] copy() {
        float[] data = new float[len];
        for(int i = 0; i < len; i++) data[i] = time[i];
        return data;
    }
    public void set(Results r) {
        for(int i = 0; i < r.len; i++)
            time[i] = r.time[i];
        len = r.len;
        for(int i = 0; i < r.flagsLen; i++)
            flags[i] = r.flags[i];
        flagsLen = r.flagsLen;
    }
    public void setFlags(Results r) {
        for(int i = 0; i < r.flagsLen; i++)
            flags[i] = r.flags[i];
        flagsLen = r.flagsLen;
    }
    public void set(double[] roots) {
        if((roots.length&1) != 0) throw new IllegalArgumentException();
        for(int i = 0; i < roots.length-1; i++)
            if(roots[i]>roots[i+1]) throw new IllegalArgumentException();
      
        for(int i = 0; i < roots.length; i++)
            time[i] = (float) roots[i];
        len = roots.length;
    }
    public void min(float min) {
        int t = 0;
        for(; t < len; t+=2)
            if(time[t] >= min) break;
        len -= t;
        for(int i = 0; i < len; i++)
            time[i] = time[t+i];
    }
    public void minArray(float min) {
        int t = 0;
        for(; t < len; t++)
            if(time[t] >= min) break;
        len -= t;
        for(int i = 0; i < len; i++)
            time[i] = time[t+i];
    }
    
    public boolean isEmpty() {
        return len == 0;
    };
    public Results reset() {
        len = 0;
        flagsLen = 0;
        return this;
    }

    @Override
    public String toString() {
        int iMax = len - 1;
        if(iMax == -1) return "[]";
        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(time[i]);
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }
    
}
