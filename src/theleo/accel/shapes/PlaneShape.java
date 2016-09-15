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

import theleo.accel.Accelerated;
import theleo.accel.Results;
import java.util.Arrays;
import theleo.accel.util.Polynomials;
import theleo.accel.util.Utils;
import static theleo.accel.shapes.SphereShape.rad;
import theleo.accel.util.Vars;

/**
 * PlaneShape represents a plane with a  
 * plane equation ax + by + cz + d = 0
 * 
 * @author Juraj Papp
 */
public class PlaneShape extends Shape {
    /**
     * Describe the plane equation ax + by + cz + d = 0
     */
    public float a, b, c, d;
    /**
     * Equals to sqrt(a^2 + b^2 + c^2)
     */
    public float abcSq;

    public PlaneShape() {}
    /**
     * Plane equation ax + by + cz + d = 0
     */
    public PlaneShape(float a, float b, float c, float d) {
        set(a, b, c, d);
    }
    public void set(float aa, float bb, float cc, float dd) {
        this.a = aa; this.b = bb; this.c = cc; this.d = dd;
        abcSq = (float)Math.sqrt(a*a+b*b+c*c);
        //if abcSq == 0f, invalid parameters were supplied
        if(abcSq != 1f) {
            float i = 1f/abcSq;
            a *= i;
            b *= i;
            c *= i;
            d *= i;
            abcSq = 1f;
        }
    }
    public void set(float a, float b, float c, float d, float abcSq) {
        this.a = a; this.b = b; this.c = c; this.d = d; this.abcSq = abcSq;
    }
    @Override
    public void calcTime(Shape s, Results store) {
        switch(s.getType()) {
            case 0: //Sphere
                calcTime((SphereShape)s, store);
                return;
            case 1: //Line
                
                return;
            //case 2: //Plane is static
        }
        throw new IllegalArgumentException();
    }

    @Override
    public boolean intersects(Shape s) {
        switch(s.getType()) {
            case 0: //Sphere
                return Collisions.intersects((SphereShape)s, this);
            case 1: //Line
                
                //return;
        }
        throw new IllegalArgumentException();
    }
    
    @Override
    public short getType() {
        return 2;
    }  
    public void calcTime(SphereShape s, Results store) {
        final float[] d = s.data;
        float q = 0.5f*(a*d[acc.x]+b*d[acc.y]+c*d[acc.z]);
        float r = a*d[vel.x]+b*d[vel.y]+c*d[vel.z];
    
        if(q == 0f) {
            if(r == 0) return;
            float[] time = store.time;
            final float abcRad = abcSq*d[rad];
            q = -a*d[pos.x]-b*d[pos.y]-c*d[pos.z]-this.d;
            if(abcRad*r > 0) {
                time[0] = (-abcRad+q)/r;
                time[1] = (abcRad+q)/r;
            }
            else {
                time[0] = (abcRad+q)/r;
                time[1] = (-abcRad+q)/r;
            }
            store.len = 2;
        }
        else {
            final float term = a*d[pos.x]+b*d[pos.y]+c*d[pos.z]+this.d;
            final float abcRad = abcSq*d[rad];
            final Vars vars = Vars.get();
            double[] roots = Polynomials.solveQuadric(q, r, term-abcRad, vars.d2arr1);
            double[] roots2= Polynomials.solveQuadric(q, r, term+abcRad, vars.d2arr2);
            double[] res;
            vars.release();
            if(roots != null && roots2 != null) {
                res = Utils.mergeSorted(roots, roots2, vars.d4arr1);
            }
            else {
                if(roots != null) res = roots;
                else if(roots2 != null) res = roots2;
                else return;
            }
            store.set(res);
        }
    }
}
