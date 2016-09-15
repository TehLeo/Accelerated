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

import theleo.accel.Results;
import theleo.accel.util.Utils;
import theleo.accel.util.Vars;
import theleo.accel.util.Vec3f;

/**
 * LineShape represents a line or a line segment.
 * 
 * Currently DIR1 must be equal to DIR2.
 * 
 * @author Juraj Papp
 */

public class LineShape extends Shape {
    public static final class dir1 { public static final byte x=9,y=10,z=11; }
    public static final class dir2 { public static final byte x=12,y=13,z=14; }
   
    public static final byte DIR1 = 9;
    public static final byte DIR2 = 12;
    public static final byte ISPD = 15;
    public static final byte HLEN = 17;
    /**
     * Line defined with point A and direction vector D. <br>
     * Point A is defined as follows: <br>
     * A = POS + VEL*t + 0.5*ACCEL*t^2 <br>
     * D is defined as linear interpolation: <br>
     * D = DIR1 + (DIR2-DIR1)*(t+TimeOffset)*InterpolationSpeed <br>
     * [0-2] POS(x,y,z) <br>
     * [3-5] VEL(x,y,z) <br>
     * [6-8] ACCEL(x,y,z) <br>
     * [9-11] DIR1(x,y,z) <br>
     * [12-14] DIR2(x,y,z) <br>
     * [15] InterpolationSpeed
     * [16] Time Offset
     * [17] Half Length if Segment, or 0 if line
     */

    public LineShape() {
        super(18);
        data[15] = 1f;
    }
    /**
     * Sets the vector data to x,y,z
     * Vectors to set are specified by the type parameter and 
     * are one of: POS, VEL, ACCEL, DIR1, DIR2
     */
    public void set(byte type, float x, float y, float z) {
        data[type] = x;
        data[type+1] = y;
        data[type+2] = z;
    }
    /**
     * Sets this line's direction (DIR1, DIR2) to the values provided
     */
    public void setDir(float x, float y, float z) {
        set(DIR1, x, y, z);
        set(DIR2, x, y, z);
    }
    /**
     * Sets this line's direction to be interpolated from DIR1 to DIR2
     */
    public void setDir(float x, float y, float z, float x2, float y2, float z2) {
        set(DIR1, x, y, z);
        set(DIR2, x2, y2, z2);
        data[16] = 0f;
    }
    /**
     * 
     * The direction vector representing this line is defined as: <br>
     * <code>D = DIR1 + (DIR2-DIR1)*t*InterpolationSpeed</code> <br>
     * <br>
     * This method sets the linearinterpolation speed(default value is 1).<br>
     * Same effect of chaging the interpolation speed can be achieved by modifying the DIR2 vector
     * 
     */
    public void setISpeed(float speed) {
        data[15] = speed;
    }
    public void setLength(float lenght) {
        data[HLEN] = 0.5f*lenght;
    }

    @Override
    public void apply(float time) {
        super.apply(time);
        data[16] += time;
        float tt = time*time*0.5f;
//        System.out.println("pos before " + data[POS] + ", " + tt);
        if(Float.isInfinite(tt) || Float.isNaN(tt)) {
            throw new IllegalArgumentException("Infinity " + time);
        }
        
        for(int i = 0; i < 3; i++) {
            data[POS+i] += data[VEL+i]*time + data[ACC+i]*tt;
            data[VEL+i] += data[ACC+i]*time;
        }
//        System.out.println("pos after " + data[POS] + ", " + tt);
    }
    /**
     * Sets the interpolated direction to DIR1, DIR2,
     * Resets timeoffset to 0
     */
    public void applyDir() {
        for(int i = 0; i < 3; i++) 
            data[DIR1+i] = data[DIR2+i] = data[DIR1+i] + (data[DIR2+i]-data[DIR1+i])*data[15]*data[16];
        data[16] = 0f;
    }
    public void getDirAt(float time, Vec3f store) {
        for(int i = 0; i < 3; i++)
            store.set(i, data[DIR1+i] + (data[DIR2+i]-data[DIR1+i])*data[15]*(time+data[16]));
    }
    @Override
    public void calcTime(Shape s, Results store) {
        switch(s.getType()) {
            case 0: //Sphere
                if(data[HLEN] == 0) Collisions.calcForLineSphere(this, (SphereShape)s, store);
                else Collisions.calcForLineSegSphere(this, (SphereShape)s, store);
                return;
            case 1: //Line
                Collisions.calcForLines(this, (LineShape)s, store);
                return;
        }
        throw new IllegalArgumentException();
    }
    /**
     * Computes and returns the intersection point with line l.
     * @param l - line
     * @return intersection point as a parameter 
     */
    public float getIntersection(LineShape l) {
        Vars vars = Vars.get();
        
        Vec3f dir1 = new Vec3f(vars.farr1);
        Vec3f dir2 = new Vec3f(vars.farr1, 3);
        Vec3f pos1 = new Vec3f(vars.farr1, 6);
        Vec3f pos2 = new Vec3f(vars.farr1, 9);
        Vec3f cross = new Vec3f(vars.farr1, 12);
        Vec3f dc = new Vec3f(vars.farr1, 15);
        
        getDirAt(travelled, dir1);
        l.getDirAt(l.travelled, dir2);
        Utils.glPosAt(this, travelled, pos1);
        Utils.glPosAt(l, l.travelled, pos2);
        dir1.normalize(); dir2.normalize();
        dir1.mult(data[HLEN]*2f); dir2.mult(l.data[HLEN]*2f);
        
        pos2.sub(pos1);
        cross.set(dir1).cross(dir2);
        dc.set(pos2).cross(dir2);
        float lenSq = cross.dot(cross);
        if(lenSq == 0) {
            vars.release();
            return Float.NaN;
        }
        lenSq = dc.dot(cross)/lenSq;
        
        vars.release();
//        if(lenSq < 0 || lenSq > 1) return Float.NaN;
        return lenSq;
    }
    /**
     * Computes and returns the intersection point with sphere s,
     * assuming the line and sphere are touching.
     * @param s - sphere
     * @return intersection point as a parameter 
     */
    public float getIntersection(SphereShape s) {
        Vars vars = Vars.get();
        
        Vec3f dir = new Vec3f(vars.farr1);
        Vec3f pos1 = new Vec3f(vars.farr1, 3);
        Vec3f pos2 = new Vec3f(vars.farr1, 6);
        getDirAt(travelled, dir);
        dir.normalize();
        Utils.glPosAt(this, travelled, pos1);
        Utils.glPosAt(s, s.travelled, pos2);
        
        float res = -dir.dot(pos1.sub(pos2));
        vars.release();
        return res;
    }
    
    public Vec3f getIntersectionPoint(SphereShape s) {
        Vars vars = Vars.get();
        
        Vec3f dir = new Vec3f(vars.farr1);
        Vec3f pos1 = new Vec3f(vars.farr1, 3);
        Vec3f pos2 = new Vec3f(vars.farr1, 6);
        getDirAt(travelled, dir);
        dir.normalize();
        Utils.glPosAt(this, travelled, pos1);
        Utils.glPosAt(s, s.travelled, pos2);
        
        float res = dir.dot(pos2.sub(pos1));
        Vec3f i = pos1.add(dir.mult(res));
        vars.release();
        
        return i;
    }
    
    @Override
    public short getType() {
        return 1;
    }
    
}
