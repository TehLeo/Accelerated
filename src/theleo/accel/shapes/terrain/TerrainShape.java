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
package theleo.accel.shapes.terrain;

import theleo.accel.Results;
import theleo.accel.shapes.Collisions;
import theleo.accel.shapes.Shape;
import theleo.accel.shapes.SphereShape;
import theleo.accel.util.Utils;
import java.util.PriorityQueue;
import theleo.accel.util.Vec3f;
import static theleo.accel.shapes.SphereShape.rad;
import static theleo.accel.util.Pools.*;

/**
 * Collision shape for terrain.
 * 
 * @author Juraj Papp
 */
public class TerrainShape extends Shape {
    public TerrainLink terLink;
    /**
     * max-min tree
     * given index i, children is at 4i+1, 4i+2, 4i+3, 4i+4
     * float data[];
     */
    
    private Results store;
    private SphereShape shape;

    public TerrainShape() {}
    public TerrainShape(TerrainLink link) {
        set(link);
    }
    
    public boolean isTopRect(float x, float z) {
        float CELL_SIZE = terLink.getCellSize();
        float invgrid = 1f/CELL_SIZE;
        return (x+z)*invgrid > (int)(x*invgrid)+(int)(z*invgrid)+1;
    }
    
    public Vec3f getNormal(float x, float z) {
        float CELL_SIZE = terLink.getCellSize();
        float GRID_SIZE = terLink.getSize();
        //assumption: purely 2d for now
        float invgrid = 1f/CELL_SIZE;
        int gx = (int)(x*invgrid);
        int gz = (int)(z*invgrid);        
        
        Vec3f normal = new Vec3f(0,1,0);
        
        if(gx < 0 || gx >= GRID_SIZE || gz < 0 || gz >= GRID_SIZE) {
            return normal;
        }
        if((x+z)*invgrid > gx+gz+1) {
            float h = terLink.getHeight(gx+1, gz+1);
            normal.set(0, terLink.getHeight(gx+1, gz)-h, -CELL_SIZE);
            normal.cross(-CELL_SIZE, terLink.getHeight(gx, gz+1)-h, 0);
        }    
        else {
            float h = terLink.getHeight(gx, gz);
            normal.set(0, terLink.getHeight(gx, gz+1)-h, CELL_SIZE);
            normal.cross(CELL_SIZE, terLink.getHeight(gx+1, gz)-h, 0);
        }
        normal.normalize();
        return normal;
    }
 
    public void grid(int x, int z, float t, float t2) {
        Vec3f a = new Vec3f(x, terLink.getHeight(x, z), z);
        Vec3f b = new Vec3f(x+1, terLink.getHeight(x+1, z), z);
        Vec3f c = new Vec3f(x, terLink.getHeight(x, z+1), z+1);
        Vec3f d = new Vec3f(x+1, terLink.getHeight(x+1, z+1), z+1);
       
        float[] tt = new float[]{t, t2};
        Results r1 = triangle(a, b, c, tt);
        Results r2 = triangle(d, c, b, tt);
        float[] time;
        int len;
        if(!r1.isEmpty() && !r2.isEmpty()) {
            time = Utils.mergeSorted(r1.time, r1.len, r2.time, r2.len);
            len = time.length;
        }
        else if(!r1.isEmpty()) {
            time = r1.time; len = r1.len;
        }
        else if(!r2.isEmpty()) {
            time = r2.time; len = r2.len;
        }
        else return;
        for(int i = 0; i < len; i++) {
            if(i != 0 && time[i] == time[i-1]) continue;
            if(store.time.length == store.len) return;
            store.time[store.len++] = time[i];
        }
    }
    public Results triangle(Vec3f a, Vec3f b, Vec3f c, float[] tt) {
        Vec3f ab = new Vec3f(b).sub(a);
        Vec3f ac = new Vec3f(c).sub(a);
        Vec3f norm = new Vec3f(ab).cross(ac);
        norm.normalize();
        Results res = new Results();
        Collisions.calcForPlanePoint(shape, norm.x(), norm.y(), norm.z(), -norm.dot(a), res);
        if(!res.isEmpty()) {
            int i = 0;
//            System.out.println("r " + res);
            Vec3f p = new Vec3f();
            //check time and triangle
            if(tt[0] <= res.time[0] && res.time[0] <= tt[1]) {
                //and inside triangle
                Utils.glPosAt(shape, res.time[0], p);
                float side = (p.x() - b.x())*(c.z() - b.z()) - (p.z() - b.z())*(c.x() - b.x());
                if(side <= 0) i += 1;
            }
            if(tt[0] <= res.time[1] && res.time[1] <= tt[1]) {
                //and inside triangel
                Utils.glPosAt(shape, res.time[1], p);
                float side = (p.x() - b.x())*(c.z() - b.z()) - (p.z() - b.z())*(c.x() - b.x());
                if(side <= 0) i += 2;
            }
            if(i == 0) res.len = 0;
            else if(i == 1) res.time[1] = res.time[0];
            else if(i == 2) res.time[0] = res.time[1];
        }
        return res;
    }
    public void fillSquares(SphereShape s) {
        float CELL_SIZE = terLink.getCellSize();
        float GRID_SIZE = terLink.getSize();
        float invgrid = 1f/CELL_SIZE;
        final float[] d = s.data;
        int gx = (int)(d[pos.x]*invgrid);
        int gz = (int)(d[pos.z]*invgrid);        
        int a, ci;
        //bounds check
        if(gx < 0 || gx >= GRID_SIZE || gz < 0 || gz >= GRID_SIZE) return;
        fill(d, gx, gz);
    }
    private void fill(float[] d, int gx, int gz) {
        float CELL_SIZE = terLink.getCellSize();
        float GRID_SIZE = terLink.getSize();
        int rx = gx+((d[vel.x] > 0)?1:0);
        int rz = gz+((d[vel.z] > 0)?1:0);
        
        int aX = (d[vel.x] > 0) ? 1 : -1;
        int aZ = (d[vel.z] > 0) ? 1 : -1;
        
        float tX; 
        float tZ;
        float tL = 0f;
     
        while(true) {
            
            if(d[vel.x] != 0f && d[acc.x] == 0f) tX = (rx*CELL_SIZE-d[pos.x])/d[vel.x];
            else if(d[acc.x] != 0f) {
                tX = d[vel.x]*d[vel.x]+2*d[acc.x]*(rx*CELL_SIZE-d[pos.x]);
                if(tX >= 0f) tX = (float)(-d[vel.x]+aX*Math.sqrt(tX))/d[acc.x];
                else {
                    //wont go right, thus switch to left
                    aX = -aX;
                    rx+=aX;
                    tX = d[vel.x]*d[vel.x]+2*d[acc.x]*(rx*CELL_SIZE-d[pos.x])*aX;
                    //tX must be > 0
                    tX = (float)(-d[vel.x]+aX*Math.sqrt(tX))/d[acc.x];
                }
            }
            else tX = Float.MAX_VALUE;

            if(d[vel.z] != 0f && d[acc.z] == 0f) tZ = (rz*CELL_SIZE-d[pos.z])/d[vel.z];
            else if(d[acc.z] != 0f) {
                tZ = d[vel.z]*d[vel.z]+2*d[acc.z]*(rz*CELL_SIZE-d[pos.z]);
                if(tZ >= 0f) tZ = (float)(-d[vel.z]+aZ*Math.sqrt(tZ))/d[acc.z];
                else {
                    //wont go up, thus switch to down
                    aZ = -aZ;
                    rz+=aZ;
                    tZ = d[vel.z]*d[vel.z]+2*d[acc.z]*(rz*CELL_SIZE-d[pos.z])*aZ;
                    //tZ must be > 0
                    tZ = (float)(-d[vel.z]+aZ*Math.sqrt(tZ))/d[acc.z];
                }
            }
            else tZ = Float.MAX_VALUE;

            if(tX == Float.MAX_VALUE && tZ == Float.MAX_VALUE) {
                grid(gx, gz, Float.MIN_VALUE, Float.MAX_VALUE);
                return;
            }
            
            //now determine which border is crossed first
            if(tX < tZ) {
                grid(gx, gz, tL, tX); tL = tX;
                gx += aX; rx += aX;
            }
            else {
                grid(gx, gz, tL, tZ); tL = tZ; 
                gz += aZ; rz += aZ;
            }
            if(store.time.length == store.len) return;
            //now check bounds
            if(gx < 0 || gx >= GRID_SIZE || gz < 0 || gz >= GRID_SIZE) return;
        }
    }
    /**
     * Calculates collision times with terrain and sphereshape with radius zero
     * Points below terrain are considered to be colliding,
     * thus if the start of sphereshape trajectory is below terrain,
     * then the results will first contain Float.MIN_VALUE to indicate this.
     * The first value always indicates when the point went below terrain.
     * 
     */
    public void calcForPoint(SphereShape point, Results res) {
        shape = point;
        store = res;
        fillSquares(point);
    }
    public void calc(SphereShape s, int _pos, Interval store) {
        Results rY = results4.reset();
        Collisions.bound(s.data[pos.y], s.data[vel.y], s.data[acc.y],
                data[_pos+1]-s.data[rad], data[_pos]+s.data[rad], rY);
        if(rY.isEmpty()) return;
        
        int len = terLink.getSize();
        float tx = terLink.getXOffset();
        float tz = terLink.getZOffset();
        
        int p = _pos>>1;
        int m,x=0,y=0,sh=1;
        while(p != 0) {
            m=(p+3)%4; 
            p=(p-m-1)>>2; m <<=1;
            x+=table[m]*sh; y+=table[m+1]*sh;
            sh<<=1; len>>=1;
        }
        float cl = terLink.getCellSize();
        tx += x*cl; tz += y*cl;
        cl *= len;
        
        Results rX = results4a.reset();
        Collisions.bound(s.data[pos.x], s.data[vel.x], s.data[acc.x],
                tx-s.data[rad], tx+cl+s.data[rad], rX);
        if(rX.isEmpty()) return;
        Results rXY = results6.reset();
        Utils.mergeIntervals(rY.time, rY.len, rX.time, rX.len, rXY);
        if(rXY.isEmpty()) return;
        rX.len = 0;
        Collisions.bound(s.data[pos.z], s.data[vel.z], s.data[acc.z],
                tz-s.data[rad], tz+cl+s.data[rad], rX);
        if(rX.isEmpty()) return;
        Results rXYZ = results10.reset();
        Utils.mergeIntervals(rXY.time, rXY.len, rX.time, rX.len, rXYZ);
        if(rXYZ.isEmpty()) return;
        store.set(rXYZ.time[0], rXYZ.time[1], _pos);
    }
    public static class Interval implements Comparable<Interval> {
        public int pos=-1;
        public float start, end;
        public void set(float s, float e, int p) {
            start = s; end = e; pos = p;
        }
        @Override
        public int compareTo(Interval o) {
            if(start == o.start) return end<o.end?-1:((end>o.end)?1:0); 
            return start<o.start?-1:1;
        }
        public boolean isEmpty() {
            return pos == -1;
        }
        @Override
        public String toString() {
            return pos==-1?"empty":pos+"["+start+","+end+"]";
        }
    }
    
    @Override
    public void calcTime(Shape s, Results store) {
        switch(s.getType()) {
            case 0: //SphereShape
                SphereShape sp = (SphereShape)s;
                if(sp.data[rad] == 0f) {
                    calcForPoint(sp, store);
                    return;
                }
                
                Interval a = new Interval();
                calc(sp, 0, a);
                if(a.isEmpty()) return;
                
                PriorityQueue<Interval> queue = intervalQueue;
                queue.clear();
                queue.add(a);
                Interval r = new Interval();
                int base, k;
                int count = 0;
                while(!queue.isEmpty()) {
                    Interval i = queue.poll();
                    base = i.pos<<2;
                    if(base+2 < data.length) {
                        for(k = 2; k <= 8;k+=2) {
                            calc(sp, base+k, r);
                            if(!r.isEmpty()) {
                                queue.add(r); r = new Interval();
                            }
                        }
                    }
                    else {
                        count ++;
                        int p = i.pos>>1;
                        int m,x=0,y=0,sh=1;
                        while(p != 0) {
                            m=(p+3)%4; 
                            p=(p-m-1)>>2; m <<=1;
                            x+=table[m]*sh; y+=table[m+1]*sh;
                            sh<<=1;
                        }
//                        System.out.println(i + ", ["+x+","+y+"]");
//                        System.out.println("height"+terLink.getHeight(x, y));
                        //calculate triangle-sphere collisions
                        
                        
                        
                        store.set(i.start, i.end);
                        return;
                    }
                }
                return;
            case 1: //LineShape
                return;
            case 2: //PlaneShape
                return;
            case 3: //SphereTShape
                return;
            case 4: //TerrainTShape
                return;
        }
    }
    @Override
    public short getType() {
        return 4;
    }
    public static final int[] table = new int[] {0,0, 1,0, 0,1, 1,1};
    public void set(TerrainLink link) {
        terLink = link;
        int tsize = link.getSize();
        int pow = Integer.numberOfTrailingZeros(tsize);
        int size = (1-(2<<(pow+pow+1)))/-3;
        float[] data = this.data = new float[size<<1];
        float s;
        float lx, lz, rx, rz;
        int i = (size - tsize*tsize)<<1; //last depth of tree
        
        int[] stack = new int[pow];
        int xz[] = new int[pow+pow];
        int loop = tsize*tsize;
        int sp;
        for(int j = 0; j < loop; j++) {
            lx = link.getHeight(xz[0], xz[1]);
            lz = link.getHeight(xz[0], xz[1]+1);
            if(lz > lx) { s=lx; lx = lz; lz = s;}
            rx = link.getHeight(xz[0]+1, xz[1]);
            rz = link.getHeight(xz[0]+1, xz[1]+1);
            if(rz > rx) { s=rx; rx = rz; rz = s;}
            
            data[i++] = lx>rx?lx:rx; //max
            data[i++] = lz>rz?rz:lz; //min
            
            stack[0]++;
            sp = 0;
            while(stack[sp] >= 4) {
                stack[sp] = 0;
                if(sp+1 < stack.length) {
                    sp++;
                    stack[sp]++;
                }
            }
            while(sp >= 0) {
                int p = sp+sp;
                xz[p] = table[2*stack[sp]]<<sp;
                xz[p+1] = table[2*stack[sp]+1]<<sp;
                if(sp+1 < stack.length) {
                    xz[p] += xz[p+2];
                    xz[p+1] += xz[p+3];
                }
                sp--;
            }
            
        }
        //now loop for rest of levels
        
        int level = pow-2;
        int i2 = size - tsize*tsize;
        int i1;
        while(level >= -1) {
            i1 = (1-(2<<(level+level+1)))/-3;
            loop = i2-i1;
            i2 += i2;
            i = i1+i1;
            for(int j = 0; j < loop; j++) {
                rx = data[i2];
                lx = data[i2+1];
                for(int k = 2; k < 7; k+=2) {
                    if(data[i2+k]>rx) rx = data[i2+k];
                    if(data[i2+k+1]<lx) lx = data[i2+k+1];
                }
                data[i++] = rx; data[i++] = lx;
                i2 += 8;
            }
            i2 = i1;
            level--;
        }
    }
}
