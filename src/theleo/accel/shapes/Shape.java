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
package theleo.accel.shapes;

import theleo.accel.shapes.transform.Transform;
import theleo.accel.Accelerated;
import theleo.accel.Results;

/**
 * Abstract Shape class, can be extended to support different shapes.
 * 
 * @author Juraj Papp
 */
public abstract class Shape {
    public static final class pos { public static final byte x=0,y=1,z=2; }
    public static final class vel { public static final byte x=3,y=4,z=5; }
    public static final class acc { public static final byte x=6,y=7,z=8; }
    
    public static final byte POS = 0;
    public static final byte VEL = 3;
    public static final byte ACC = 6;
    
//    public Collides willCollide = new Collides();
    public Shape willCollide;
    public float movingFor = Float.MAX_VALUE;
    public float limit = Float.MAX_VALUE;
    public byte spec=-1;
    public boolean isStatic;
    public float travelled = 0;
    public float[] data;
    public Transform trans;
    
    public int groupId = -1;
    
    private static int $id = 1;
    public int id = $id++;
    public Object userObj = null;

    public Shape() {}
    public Shape(int dataLen) {
        data = new float[dataLen];
    }
    public boolean apply() {
        if(travelled != 0f) {
            apply(travelled);
            return true;
        }
        return false;
    }
    public void apply(float time) {
        float mov = movingFor;
        
        if(movingFor != Float.MAX_VALUE) movingFor -= time;
        if(limit != Float.MAX_VALUE) limit -= time;
        travelled -= time;
        if(travelled < Accelerated.ZERO_TOLERANCE) travelled = 0f;
        
        if(movingFor < 0) {
            if(movingFor > -Accelerated.ZERO_TOLERANCE) movingFor = 0;
            else {
                System.out.println("time" + time);
                System.out.println("trav " + travelled);
                System.err.println("mov " +mov + ", now " + movingFor);
                throw new IllegalArgumentException();
            }
        }
    }
    /**
     * Calculates time in seconds it takes for the given shape s to collide with this
     * object. This object getType() >= s.getType(). Otherwise exception is thrown.
     * @param s - Shape
     * @param store - float[2+] array to store the results
     */
    public abstract void calcTime(Shape s, Results store);
    
    public boolean intersects(Shape s) {
        throw new UnsupportedOperationException();
    }
    /**
     * Each subclass of shape is to return unique type id, starting from 0. <br>
     * Id's already in use: <br>
     * SphereShape: 0
     * LineShape:   1
     * PlaneShape:  2
     * SphereTShape: 3
     * 
     * @return 
     */
    public abstract short getType();
    
    public void clear() {
        if(willCollide != null) {
            willCollide.willCollide = null;
            willCollide.movingFor = willCollide.limit;
            willCollide.spec = -1;
            willCollide = null;
            movingFor = limit;
            spec = -1;
        }
    }
    public void copyFrom(Shape from) {
        id = from.id;
        isStatic = from.isStatic;
        travelled = from.travelled;
        trans = from.trans;
        groupId = from.groupId;
        for(int i = 0; i < from.data.length; i++)
            data[i] = from.data[i];
    }
    public void set(Shape s) {
        s.willCollide = this;
        willCollide = s;
    }
    /**
     * Sets the amount of time after ImpactListener.onLimit() event will be fired
     * on this object.
     * Use if this object is not present in an Accelerated instance.
     * @param limit - time in seconds
     */
    public void setLimit(float limit) {
        this.limit = limit;
        movingFor = limit;
    }
    
    public void clearLimit() {
        limit = Float.MAX_VALUE;
        if(willCollide == null) movingFor = Float.MAX_VALUE;
    }
    /**
     * Returns the data if no transforms exists. Otherwise applies transform to data
     * and stores the result in the given array.
     * @param store - array to store result if needed[might not be used]
     * @return this shape data or transformed data
     */
    public float[] getData(float[] store) {
        if(trans == null) return data;
        if(travelled != 0) apply(travelled);
        return trans.transform(data,store);
    }
    public float pos(int i) {
        return data[POS+i];
    }
    public float vel(int i) {
        return data[VEL+i];
    }
    public float acc(int i) {
        return data[ACC+i];
    }
    public void pos(int i, float v) {
        data[POS+i] = v;
    }
    public void vel(int i, float v) {
        data[VEL+i] = v;
    }
    public void acc(int i, float v) {
        data[ACC+i] = v;
    }
    public void pos(float x, float y, float z) {
        data[POS] = x; data[POS+1] = y; data[POS+2] = z;
    }
    public void vel(float x, float y, float z) {
        data[VEL] = x; data[VEL+1] = y; data[VEL+2] = z;
    }
    public void acc(float x, float y, float z) {
        data[ACC] = x; data[ACC+1] = y; data[ACC+2] = z;
    }    
    public void setFlags(int[] flags, int len) {}
    @Override
    public String toString() {
        if(willCollide == null) return ""+id;
        return id+"("+willCollide.id+")";
    }
}
