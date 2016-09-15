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
 * A wrapper class that can treat an array as a vector.
 * 
 * @author Juraj Papp
 */
public class Vec3f {
    public int offset;
    public float[] array;

    public Vec3f() {
        this.array = new float[] {0,0,0};
    }
    public Vec3f(float x, float y, float z) {
        this.array = new float[] {x,y,z};
    }
    public Vec3f(Vec3f copy) {
        this.array = new float[] {copy.x(),copy.y(),copy.z()};
    }
    public Vec3f(float[] arr) {
        this.array = arr;
    }
    public Vec3f(float[] array, int offset) {
        this.offset = offset;
        this.array = array;
    }
    
    public float x() { return array[offset]; }
    public float y() { return array[offset+1]; }
    public float z() { return array[offset+2]; }
    
    public Vec3f set(int off, float val) {
        array[offset+off] = val;
        return this;
    }
    public Vec3f set(float x, float y, float z) {
        array[offset] = x; array[offset+1] = y; array[offset+2] = z;
        return this;
    }
    public Vec3f add(float x, float y, float z) {
        array[offset] += x; array[offset+1] += y; array[offset+2] += z;
        return this;
    }
    public Vec3f sub(float x, float y, float z) {
        array[offset] -= x; array[offset+1] -= y; array[offset+2] -= z;
        return this;
    }
    public Vec3f mult(float v) {
        array[offset] *= v; array[offset+1] *= v; array[offset+2] *= v;
        return this;
    }
    public Vec3f mult(float x, float y, float z) {
        array[offset] *= x; array[offset+1] *= y; array[offset+2] *= z;
        return this;
    }
    public Vec3f div(float x, float y, float z) {
        array[offset] /= x; array[offset+1] /= y; array[offset+2] /= z;
        return this;
    }
    public float dot(float x, float y, float z) {
        return array[offset]*x+array[offset+1]*y+array[offset+2]*z;
    }
    public Vec3f cross(float _x, float _y, float _z) {
        final float x = array[offset], y = array[offset+1], z = array[offset+2];
        array[offset]   = y*_z - z*_y;
        array[offset+1] = z*_x - x*_z;
        array[offset+2] = x*_y - y*_x;
        return this;
    }
    public float length() {
        return (float)Math.sqrt(dot(this));
    }
    public float normalize() {
        float len = dot(this);
        if(len != 1f && len != 0f)  {
            len = (float)(1f/Math.sqrt(len));
            array[offset] *= len;
            array[offset+1] *= len;
            array[offset+2] *= len;
        }
        return len;
    }
    
    public Vec3f set(Vec3f v) {
        array[offset] = v.x(); array[offset+1] = v.y(); array[offset+2] = v.z();
        return this;
    }
    public Vec3f add(Vec3f v) {
        array[offset] += v.x(); array[offset+1] += v.y(); array[offset+2] += v.z();
        return this;
    }
    public Vec3f sub(Vec3f v) {
        array[offset] -= v.x(); array[offset+1] -= v.y(); array[offset+2] -= v.z();
        return this;
    }
    public Vec3f mult(Vec3f v) {
        array[offset] *= v.x(); array[offset+1] *= v.y(); array[offset+2] *= v.z();
        return this;
    }
    public Vec3f div(Vec3f v) {
        array[offset] /= v.x(); array[offset+1] /= v.y(); array[offset+2] /= v.z();
        return this;
    }
    public float dot(Vec3f v) {
        return array[offset]*v.x()+array[offset+1]*v.y()+array[offset+2]*v.z();
    }
    public Vec3f cross(Vec3f v) {
        final float x = array[offset], y = array[offset+1], z = array[offset+2];
        array[offset]   = y*v.z() - z*v.y();
        array[offset+1] = z*v.x() - x*v.z();
        array[offset+2] = x*v.y() - y*v.x();
        return this;
    }
}
