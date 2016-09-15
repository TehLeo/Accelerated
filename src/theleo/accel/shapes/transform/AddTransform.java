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
package theleo.accel.shapes.transform;

import theleo.accel.shapes.Shape;

/**
 * Used to define relative position,vel,accel for a shape.
 * eg. child.trans = new AddTransform(parent);
 * Thus, if parent moves, so does the child.
 * 
 * In order to use this transform, when parent's pos,vel,acc are changed,
 * call apply recalc on the child as well. Not doing so will yield undefined
 * behavior.
 * 
 * @author Juraj Papp
 */
public class AddTransform implements Transform {
    public Shape shape;
    public int maxLen = 9;
    public AddTransform() {}
    public AddTransform(Shape parent) {
        this.shape = parent;
    }
    @Override
    public float[] transform(float[] data, float[] store) {
        if(shape.travelled != 0) shape.apply(shape.travelled);
        float[] d = shape.getData(store);
        int l = Math.min(Math.min(d.length, data.length), maxLen);
        for(int i = 0; i < l; i++) store[i] = d[i]+data[i];
        for(int i = l; i < data.length; i++) store[i] = data[i];
        return store;
    }

}
