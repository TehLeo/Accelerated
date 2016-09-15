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
package theleo.accel.util;

/**
 * Preallocated arrays.
 * 
 * @author Juraj Papp
 */
public final class Vars {
    public static final double[] zero2 = new double[] {0,0};
    
    
    static final int MAX_SIZE = 5;
    private static final class VarsStack {
        int index = 0;
        Vars[] vars = new Vars[MAX_SIZE];
    }
    static final VarsStack stack = new VarsStack();

    private boolean isUsed = false;
    public static Vars get() {
        Vars v = stack.vars[stack.index++];
        if(v == null) {
            stack.vars[stack.index-1] = v = new Vars();
        }
        v.isUsed = true;
        return v;
    } 
    public final void release() {
        if (!isUsed || stack.vars[stack.index-1] != this) throw new IllegalStateException();
        isUsed = false;
        stack.index--;        
    }
    public final float[] farr1 = new float[18];
    public final float[] farr2 = new float[18];
    public final float[] farr3 = new float[18];
    public final float[] farr4 = new float[18];
    public final float[] farr5 = new float[18];
    public final double[] d2arr1 = new double[2];
    public final double[] d2arr2 = new double[2];
    public final double[] d4arr1 = new double[4];
//    public final SArray sarr1 = new SArray();
}
