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
import static theleo.accel.shapes.LineShape.*;
import static theleo.accel.shapes.SphereShape.*;
import theleo.accel.util.Polynomials;
import theleo.accel.util.Utils;
import theleo.accel.util.Vars;
import java.util.Arrays;
import theleo.accel.util.Vec3f;

/**
 * A class that contains the necessary maths to compute
 * collisions.
 * 
 * @author Juraj Papp
 */
public class Collisions {
    public static boolean intersectsSphere(float[] d, float radSum) {
        float dist = d[pos.x]*d[pos.x]+d[pos.y]*d[pos.y]+d[pos.z]*d[pos.z];
        return dist <= radSum*radSum;
    }
    public static boolean intersectsSphere(SphereShape s1, Shape s2) {
        final Vars vars = Vars.get();
        final float[] d = vars.farr1;
        final float[] a = s1.getData(vars.farr2), b = s2.getData(vars.farr3);
        for(int i = 0; i < 3; i++) d[i] = a[i]-b[i];
        float rs = a[rad]+b[rad];
        float dist = d[pos.x]*d[pos.x]+d[pos.y]*d[pos.y]+d[pos.z]*d[pos.z];
        vars.release();
        return dist <= rs*rs;
    }
    public static boolean intersects(SphereShape s, PlaneShape p) {
        final Vars vars = Vars.get();
        final float[] a = s.getData(vars.farr1);
        float d = a[pos.x]*p.a+a[pos.y]*p.b+a[pos.z]*p.c+p.d;
        if(d < 0) d = -d;
        vars.release();
        return d <= a[rad];
    }
    
    
    public static void calcForLineSegments(LineShape l1, LineShape l2, Results store) {
        final Vars vars = Vars.get();
        final float[] d = vars.farr1;
        final float[] a = l1.getData(vars.farr2), b = l2.getData(vars.farr3);
        for(int i = 0; i < 9; i++) d[i] = a[i]-b[i];
        
        calcForSpheres(d, l1.data[HLEN]+l2.data[HLEN], store);
        vars.release();
        if(!store.isEmpty()) {
            float[] r = store.copy();
            store.reset();
            Collisions.calcForLines(l1, l2, store);
            if(!store.isEmpty()) {
                float[] r2 = store.copy();
                store.reset();
                Utils.mergeIntervals(r, r.length, r2, r2.length, store);
            }
        }
    }
    /**
     *  uses sphere check, then line check and merges the result
     *  is this alright, efficient?
     *  otherwise use https://en.wikipedia.org/wiki/Line%E2%80%93sphere_intersection
     *  
     */
    public static void calcForLineSegSphere(LineShape l, SphereShape s, Results store) {
        
        
        final Vars vars = Vars.get();
        
        final float[] d = vars.farr1;
        final float[] a = l.getData(vars.farr2), b = s.getData(vars.farr3);
        for(int i = 0; i < 9; i++) d[i] = a[i]-b[i];
        
        boolean stationary = true;
        for(int i = 3; i < 9; i++) if(a[i] != 0f) { stationary = false; break; }
        if(stationary) {
            if(intersectsSphere(d, b[rad]+l.data[HLEN]*2f)) {
                calcForLineSphere(l, s, store);
            }
            vars.release();
        }
        else {
            calcForSpheres(d, b[rad]+l.data[HLEN]*2f, store);
            vars.release();
            if(!store.isEmpty()) {
                float[] r = store.copy();
                System.out.println("r:"+ Arrays.toString(r));
                if(r[0] == r[1] && r.length == 2) {
                    r[0] = 0;
                }
                store.reset();
                calcForLineSphere(l, s, store);
                if(!store.isEmpty()) {
                    float[] r2 = store.copy();
                    System.out.println("r2:"+ Arrays.toString(r2));
                    store.reset();
                    Utils.mergeIntervals(r, r.length, r2, r2.length, store);
                    System.out.println("res: " + store.toString());
                }
            }
        }
    }
//    public static void calcForLineSphere(SphereShape s, Vector3f la, Vector3f lb, Results store) {
//        final float[] b = s.getData(vars.farr3);
//    }
     public static void calcForLineSphere(float[] sphere, Vec3f la, Vec3f lb, Results store) {
        final Vars vars = Vars.get();
        try {
            final float[] d = vars.farr1;
//            final float[] a = l.getData(vars.farr2);
//            final float[] b = s.getData(vars.farr3);
            for(int i = 0; i < 9; i++) d[i] = /*a[i]*/-sphere[i];
            d[pos.x] += la.x();
            d[pos.y] += la.y();
            d[pos.z] += la.z();
            for(int i = 0; i < 3; i++) d[ACC+i] *= 0.5f;
//            for(int i = dir1.x; i <= dir2.z; i++) d[i] = a[i];
            d[dir1.x] = lb.x()-la.x();
            d[dir1.y] = lb.y()-la.y();
            d[dir1.z] = lb.z()-la.z();
            
            double r = sphere[rad];
            double[] c = new double[5];
            c[0] = 
                    +(-d[acc.x]*d[acc.x]-d[acc.y]*d[acc.y])*d[dir1.z]*d[dir1.z]
                    +(2.*d[acc.x]*d[acc.z]*d[dir1.x]+2.*d[acc.y]*d[acc.z]*d[dir1.y])*d[dir1.z]
                    +(-d[acc.x]*d[acc.x]-d[acc.z]*d[acc.z])*d[dir1.y]*d[dir1.y]+2.*d[acc.x]*d[acc.y]*d[dir1.x]*d[dir1.y]
                    +(-d[acc.y]*d[acc.y]-d[acc.z]*d[acc.z])*d[dir1.x]*d[dir1.x];
            c[1] = 
                    +(-2.*d[acc.z]*d[dir1.x]*d[dir1.x]-2.*d[acc.z]*d[dir1.y]*d[dir1.y]+ (2.*d[acc.y]*d[dir1.y]+2.*d[acc.x]*d[dir1.x])*d[dir1.z])*d[vel.z]
                    +(-2.*d[acc.y]*d[dir1.x]*d[dir1.x]+2.*d[acc.x]*d[dir1.x]*d[dir1.y]+2.*d[acc.z]*d[dir1.y]*d[dir1.z]-2.*d[acc.y]*d[dir1.z]*d[dir1.z])*d[vel.y]
                    +(2.*d[acc.y]*d[dir1.x]*d[dir1.y]-2.*d[acc.x]*d[dir1.y]*d[dir1.y]+2.*d[acc.z]*d[dir1.x]*d[dir1.z]-2.*d[acc.x]*d[dir1.z]*d[dir1.z])*d[vel.x];
            c[2] = 
                    +(-d[dir1.x]*d[dir1.x]-d[dir1.y]*d[dir1.y])*d[vel.z]*d[vel.z]
                    +((2.*d[dir1.x]*d[dir1.z])*d[vel.x]+(2.*d[dir1.y]*d[dir1.z])*d[vel.y])*d[vel.z]
                    +(-d[dir1.x]*d[dir1.x]-d[dir1.z]*d[dir1.z])*d[vel.y]*d[vel.y]
                    +((2.*d[dir1.x]*d[dir1.y])*d[vel.x])*d[vel.y]+
                    (-d[dir1.y]*d[dir1.y]-d[dir1.z]*d[dir1.z])*d[vel.x]*d[vel.x]+
                    +(-2.*d[acc.z]*d[dir1.x]*d[dir1.x]-2.*d[acc.z]*d[dir1.y]*d[dir1.y]+(2.*d[acc.y]*d[dir1.y]+2.*d[acc.x]*d[dir1.x])*d[dir1.z])*d[pos.z]
                    +(-2.*d[acc.y]*d[dir1.x]*d[dir1.x]+2.*d[acc.x]*d[dir1.x]*d[dir1.y]+2.*d[acc.z]*d[dir1.y]*d[dir1.z]-2.*d[acc.y]*d[dir1.z]*d[dir1.z])*d[pos.y]
                    +(2.*d[acc.y]*d[dir1.x]*d[dir1.y]-2.*d[acc.x]*d[dir1.y]*d[dir1.y]+2.*d[acc.z]*d[dir1.x]*d[dir1.z]-2.*d[acc.x]*d[dir1.z]*d[dir1.z])*d[pos.x]
                    ;
            c[3] = 
                    ((2.*d[dir1.x]*d[dir1.z])*d[pos.x]+
                    (2.*d[dir1.y]*d[dir1.z])*d[pos.y]+
                    (-2.*d[dir1.y]*d[dir1.y]-2.*d[dir1.x]*d[dir1.x])*d[pos.z])*d[vel.z]+(
                    (2.*d[dir1.x]*d[dir1.y])*d[pos.x]+
                    (-2.*d[dir1.z]*d[dir1.z]-2.*d[dir1.x]*d[dir1.x])*d[pos.y]+
                    (2.*d[dir1.y]*d[dir1.z])*d[pos.z])*d[vel.y]+(
                    (-2.*d[dir1.z]*d[dir1.z]-2.*d[dir1.y]*d[dir1.y])*d[pos.x]+
                    (2.*d[dir1.x]*d[dir1.y])*d[pos.y]+(2.*d[dir1.x]*d[dir1.z])*d[pos.z])*d[vel.x];
              c[4] = 
                      +(d[dir1.x]*d[dir1.x]+d[dir1.y]*d[dir1.y]+d[dir1.z]*d[dir1.z])*r*r
                      +(-d[dir1.x]*d[dir1.x]-d[dir1.y]*d[dir1.y])*d[pos.z]*d[pos.z]
                      +((2.*d[dir1.x]*d[dir1.z])*d[pos.x]+(2.*d[dir1.y]*d[dir1.z])*d[pos.y])*d[pos.z]
                      +(-d[dir1.x]*d[dir1.x]-d[dir1.z]*d[dir1.z])*d[pos.y]*d[pos.y]
                      +(2.*d[dir1.x]*d[dir1.y])*d[pos.x]*d[pos.y]
                      +(-d[dir1.y]*d[dir1.y]-d[dir1.z]*d[dir1.z])*d[pos.x]*d[pos.x]
                    ;
//              System.out.println("coeff " + Arrays.toString(c));
              c = Polynomials.trimLeadingZeros(c);
//              System.out.println("ctrimmed " + Arrays.toString(c));
//              System.out.println(Polynomials.toString(c));
             
              double[] roots = Polynomials.solveClosed(c);
//              System.out.println("Closed " + Arrays.toString(roots));
    //          if(roots != null) store.set(roots, 0);
              if(roots != null) store.set(roots);
        }
        finally {
            vars.release();
        }
   }
   public static void calcForLineSphere(LineShape l, SphereShape s, Results store) {
        final Vars vars = Vars.get();
        try {
            final float[] d = vars.farr1;
            final float[] a = l.getData(vars.farr2), b = s.getData(vars.farr3);
            for(int i = 0; i < 9; i++) d[i] = a[i]-b[i];
            for(int i = 0; i < 3; i++) d[ACC+i] *= 0.5f;
            for(int i = dir1.x; i <= dir2.z; i++) d[i] = a[i];

            double r = b[rad];
            double[] c;
            if(d[dir1.x] == d[dir2.x] && d[dir1.y] == d[dir2.y] && d[dir1.z] == d[dir2.z]) {
                if(l.data[POS] != l.data[POS]) throw new IllegalArgumentException("nan " + l.id);
                c = new double[5];
                c[0] = 
                    +(-d[acc.x]*d[acc.x]-d[acc.y]*d[acc.y])*d[dir1.z]*d[dir1.z]
                    +(2.*d[acc.x]*d[acc.z]*d[dir1.x]+2.*d[acc.y]*d[acc.z]*d[dir1.y])*d[dir1.z]
                    +(-d[acc.x]*d[acc.x]-d[acc.z]*d[acc.z])*d[dir1.y]*d[dir1.y]+2.*d[acc.x]*d[acc.y]*d[dir1.x]*d[dir1.y]
                    +(-d[acc.y]*d[acc.y]-d[acc.z]*d[acc.z])*d[dir1.x]*d[dir1.x];
            c[1] = 
                    +(-2.*d[acc.z]*d[dir1.x]*d[dir1.x]-2.*d[acc.z]*d[dir1.y]*d[dir1.y]+ (2.*d[acc.y]*d[dir1.y]+2.*d[acc.x]*d[dir1.x])*d[dir1.z])*d[vel.z]
                    +(-2.*d[acc.y]*d[dir1.x]*d[dir1.x]+2.*d[acc.x]*d[dir1.x]*d[dir1.y]+2.*d[acc.z]*d[dir1.y]*d[dir1.z]-2.*d[acc.y]*d[dir1.z]*d[dir1.z])*d[vel.y]
                    +(2.*d[acc.y]*d[dir1.x]*d[dir1.y]-2.*d[acc.x]*d[dir1.y]*d[dir1.y]+2.*d[acc.z]*d[dir1.x]*d[dir1.z]-2.*d[acc.x]*d[dir1.z]*d[dir1.z])*d[vel.x];
            c[2] = 
                    +(-d[dir1.x]*d[dir1.x]-d[dir1.y]*d[dir1.y])*d[vel.z]*d[vel.z]
                    +((2.*d[dir1.x]*d[dir1.z])*d[vel.x]+(2.*d[dir1.y]*d[dir1.z])*d[vel.y])*d[vel.z]
                    +(-d[dir1.x]*d[dir1.x]-d[dir1.z]*d[dir1.z])*d[vel.y]*d[vel.y]
                    +((2.*d[dir1.x]*d[dir1.y])*d[vel.x])*d[vel.y]+
                    (-d[dir1.y]*d[dir1.y]-d[dir1.z]*d[dir1.z])*d[vel.x]*d[vel.x]+
                    +(-2.*d[acc.z]*d[dir1.x]*d[dir1.x]-2.*d[acc.z]*d[dir1.y]*d[dir1.y]+(2.*d[acc.y]*d[dir1.y]+2.*d[acc.x]*d[dir1.x])*d[dir1.z])*d[pos.z]
                    +(-2.*d[acc.y]*d[dir1.x]*d[dir1.x]+2.*d[acc.x]*d[dir1.x]*d[dir1.y]+2.*d[acc.z]*d[dir1.y]*d[dir1.z]-2.*d[acc.y]*d[dir1.z]*d[dir1.z])*d[pos.y]
                    +(2.*d[acc.y]*d[dir1.x]*d[dir1.y]-2.*d[acc.x]*d[dir1.y]*d[dir1.y]+2.*d[acc.z]*d[dir1.x]*d[dir1.z]-2.*d[acc.x]*d[dir1.z]*d[dir1.z])*d[pos.x]
                    ;
            c[3] = 
                    ((2.*d[dir1.x]*d[dir1.z])*d[pos.x]+
                    (2.*d[dir1.y]*d[dir1.z])*d[pos.y]+
                    (-2.*d[dir1.y]*d[dir1.y]-2.*d[dir1.x]*d[dir1.x])*d[pos.z])*d[vel.z]+(
                    (2.*d[dir1.x]*d[dir1.y])*d[pos.x]+
                    (-2.*d[dir1.z]*d[dir1.z]-2.*d[dir1.x]*d[dir1.x])*d[pos.y]+
                    (2.*d[dir1.y]*d[dir1.z])*d[pos.z])*d[vel.y]+(
                    (-2.*d[dir1.z]*d[dir1.z]-2.*d[dir1.y]*d[dir1.y])*d[pos.x]+
                    (2.*d[dir1.x]*d[dir1.y])*d[pos.y]+(2.*d[dir1.x]*d[dir1.z])*d[pos.z])*d[vel.x];
              c[4] = 
                      +(d[dir1.x]*d[dir1.x]+d[dir1.y]*d[dir1.y]+d[dir1.z]*d[dir1.z])*r*r
                      +(-d[dir1.x]*d[dir1.x]-d[dir1.y]*d[dir1.y])*d[pos.z]*d[pos.z]
                      +((2.*d[dir1.x]*d[dir1.z])*d[pos.x]+(2.*d[dir1.y]*d[dir1.z])*d[pos.y])*d[pos.z]
                      +(-d[dir1.x]*d[dir1.x]-d[dir1.z]*d[dir1.z])*d[pos.y]*d[pos.y]
                      +(2.*d[dir1.x]*d[dir1.y])*d[pos.x]*d[pos.y]
                      +(-d[dir1.y]*d[dir1.y]-d[dir1.z]*d[dir1.z])*d[pos.x]*d[pos.x]
                    ;
                
                
            }
            else {
                float spd = a[15];
                float off = a[16];
                for(int i = 0; i < 3; i++) d[DIR2+i] = spd*(d[DIR2+i]-d[DIR1+i]);

                final float[] g = vars.farr4;
                for(int i = 0; i < 3; i++) g[i] = off*d[DIR2+i]; 
                    
            
                //factor a bit 
            c = new double[7];
            c[0] = 
                    +(-d[acc.y]*d[acc.y]-d[acc.z]*d[acc.z])*d[dir2.x]*d[dir2.x]
                    +(-d[acc.x]*d[acc.x]-d[acc.z]*d[acc.z])*d[dir2.y]*d[dir2.y]
                    +(-d[acc.x]*d[acc.x]-d[acc.y]*d[acc.y])*d[dir2.z]*d[dir2.z]

                    +(2.*d[acc.x]*d[acc.z]*d[dir2.x]+2.*d[acc.y]*d[acc.z]*d[dir2.y])*d[dir2.z]
                    +2.*d[acc.x]*d[acc.y]*d[dir2.x]*d[dir2.y];
            
            c[1] = 
                    (-2.*d[acc.z]*d[dir2.x]*d[dir2.x]-2.*d[acc.z]*d[dir2.y]*d[dir2.y]+(2.*d[acc.y]*d[dir2.y]+2.*d[acc.x]*d[dir2.x])*d[dir2.z])*d[vel.z]+
                    (-2.*d[acc.y]*d[dir2.x]*d[dir2.x]+2.*d[acc.x]*d[dir2.x]*d[dir2.y]+2.*d[acc.z]*d[dir2.y]*d[dir2.z]-2.*d[acc.y]*d[dir2.z]*d[dir2.z])*d[vel.y]+
                    (2.*d[acc.y]*d[dir2.x]*d[dir2.y]-2.*d[acc.x]*d[dir2.y]*d[dir2.y]+2.*d[acc.z]*d[dir2.x]*d[dir2.z]-2.*d[acc.x]*d[dir2.z]*d[dir2.z])*d[vel.x]+
                    (2.*d[acc.x]*d[acc.z]*d[dir2.x]+2.*d[acc.y]*d[acc.z]*d[dir2.y]+(-2.*d[acc.y]*d[acc.y]-2.*d[acc.x]*d[acc.x])*d[dir2.z])*g[2]+
                    (2.*d[acc.x]*d[acc.y]*d[dir2.x]+(-2.*d[acc.z]*d[acc.z]-2.*d[acc.x]*d[acc.x])*d[dir2.y]+2.*d[acc.y]*d[acc.z]*d[dir2.z])*g[1]+
                    ((-2.*d[acc.z]*d[acc.z]-2.*d[acc.y]*d[acc.y])*d[dir2.x]+2.*d[acc.x]*d[acc.y]*d[dir2.y]+2.*d[acc.x]*d[acc.z]*d[dir2.z])*g[0]+
                    (2.*d[acc.x]*d[acc.z]*d[dir1.x]+2.*d[acc.y]*d[acc.z]*d[dir1.y]+(-2.*d[acc.y]*d[acc.y]-2.*d[acc.x]*d[acc.x])*d[dir1.z])*d[dir2.z]+
                    (2.*d[acc.x]*d[acc.y]*d[dir1.x]+(-2.*d[acc.z]*d[acc.z]-2.*d[acc.x]*d[acc.x])*d[dir1.y]+2.*d[acc.y]*d[acc.z]*d[dir1.z])*d[dir2.y]+
                    ((-2.*d[acc.z]*d[acc.z]-2.*d[acc.y]*d[acc.y])*d[dir1.x]+2.*d[acc.x]*d[acc.y]*d[dir1.y]+2.*d[acc.x]*d[acc.z]*d[dir1.z])*d[dir2.x];
            
            c[2] = 
                    (-d[dir2.x]*d[dir2.x]-d[dir2.y]*d[dir2.y])*d[vel.z]*d[vel.z]+((2.*d[acc.x]*d[dir1.z]-4*d[acc.z]*d[dir1.x])*d[dir2.x]+(2.*d[acc.y]*d[dir1.z]-4*d[acc.z]*d[dir1.y])*d[dir2.y]+
                    (2.*d[acc.y]*d[dir1.y]+2.*d[acc.x]*d[dir1.x])*d[dir2.z]+(2.*d[acc.x]*d[dir2.z]-4*d[acc.z]*d[dir2.x])*g[0]+(2.*d[acc.y]*d[dir2.z]-4*d[acc.z]*d[dir2.y])*g[1]+
                    (2.*d[acc.y]*d[dir2.y]+2.*d[acc.x]*d[dir2.x])*g[2]+2.*d[dir2.x]*d[dir2.z]*d[vel.x]+2.*d[dir2.y]*d[dir2.z]*d[vel.y])*d[vel.z]+(-d[dir2.x]*d[dir2.x]-d[dir2.z]*d[dir2.z])*d[vel.y]*d[vel.y]+(
                    (2.*d[acc.x]*d[dir1.y]-4*d[acc.y]*d[dir1.x])*d[dir2.x]+(2.*d[acc.z]*d[dir1.z]+2.*d[acc.x]*d[dir1.x])*d[dir2.y]+(2.*d[acc.z]*d[dir1.y]-4*d[acc.y]*d[dir1.z])*d[dir2.z]+
                    (2.*d[acc.x]*d[dir2.y]-4*d[acc.y]*d[dir2.x])*g[0]+(2.*d[acc.z]*d[dir2.z]+2.*d[acc.x]*d[dir2.x])*g[1]+(2.*d[acc.z]*d[dir2.y]-4*d[acc.y]*d[dir2.z])*g[2]
                    +2.*d[dir2.x]*d[dir2.y]*
                    d[vel.x])*d[vel.y]+(-d[dir2.y]*d[dir2.y]-d[dir2.z]*d[dir2.z])*d[vel.x]*d[vel.x]+((2.*d[acc.z]*d[dir1.z]+2.*d[acc.y]*d[dir1.y])*d[dir2.x]+(2.*d[acc.y]*d[dir1.x]-4*d[acc.x]*d[dir1.y])*d[dir2.y]+
                    (2.*d[acc.z]*d[dir1.x]-4*d[acc.x]*d[dir1.z])*d[dir2.z]+(2.*d[acc.z]*d[dir2.z]+2.*d[acc.y]*d[dir2.y])*g[0]+(2.*d[acc.y]*d[dir2.x]-4*d[acc.x]*d[dir2.y])*g[1]+
                    (2.*d[acc.z]*d[dir2.x]-4*d[acc.x]*d[dir2.z])*g[2])*d[vel.x]+(-2.*d[acc.z]*d[dir2.x]*d[dir2.x]-2.*d[acc.z]*d[dir2.y]*d[dir2.y]+(2.*d[acc.y]*d[dir2.y]
                    +2.*d[acc.x]*d[dir2.x])*d[dir2.z])*d[pos.z]+
                    (-2.*d[acc.y]*d[dir2.x]*d[dir2.x]+2.*d[acc.x]*d[dir2.x]*d[dir2.y]+2.*d[acc.z]*d[dir2.y]*d[dir2.z]-2.*d[acc.y]*d[dir2.z]*d[dir2.z])*d[pos.y]+
                    (2.*d[acc.y]*d[dir2.x]*d[dir2.y]-2.*d[acc.x]*d[dir2.y]*d[dir2.y]+2.*d[acc.z]*d[dir2.x]*d[dir2.z]-2.*d[acc.x]*d[dir2.z]*d[dir2.z])*d[pos.x]+(-d[acc.x]*d[acc.x]-d[acc.y]*d[acc.y])*g[2]*g[2]+
                    (2.*d[acc.x]*d[acc.z]*d[dir1.x]+2.*d[acc.y]*d[acc.z]*d[dir1.y]+(-2.*d[acc.y]*d[acc.y]-2.*d[acc.x]*d[acc.x])*d[dir1.z]+2.*d[acc.x]*d[acc.z]*g[0]
                    +2.*d[acc.y]*d[acc.z]*g[1])*g[2]+
                    (-d[acc.x]*d[acc.x]-d[acc.z]*d[acc.z])*g[1]*g[1]+(2.*d[acc.x]*d[acc.y]*d[dir1.x]+(-2.*d[acc.z]*d[acc.z]-2.*d[acc.x]*d[acc.x])*d[dir1.y]+2.*d[acc.y]*d[acc.z]*d[dir1.z]
                    +2.*d[acc.x]*d[acc.y]*g[0])*g[1]+
                    (-d[acc.y]*d[acc.y]-d[acc.z]*d[acc.z])*g[0]*g[0]+((-2.*d[acc.z]*d[acc.z]-2.*d[acc.y]*d[acc.y])*d[dir1.x]+2.*d[acc.x]*d[acc.y]*d[dir1.y]+2.*d[acc.x]*d[acc.z]*d[dir1.z])*g[0]+(-
                    d[acc.x]*d[acc.x]-d[acc.y]*d[acc.y])*
                    d[dir1.z]*d[dir1.z]+(2.*d[acc.x]*d[acc.z]*d[dir1.x]+2.*d[acc.y]*d[acc.z]*d[dir1.y])*d[dir1.z]+(-d[acc.x]*d[acc.x]-d[acc.z]*d[acc.z])*d[dir1.y]*d[dir1.y]+2.*d[acc.x]*d[acc.y]*d[dir1.x]*d[dir1.y]+
                    (-d[acc.y]*d[acc.y]-d[acc.z]*d[acc.z])*d[dir1.x]*d[dir1.x];
            c[3] = 
                    (-2.*d[dir1.x]*d[dir2.x]-2.*d[dir1.y]*d[dir2.y]-2.*d[dir2.x]*g[0]-2.*d[dir2.y]*g[1])*d[vel.z]*d[vel.z]+(-2.*d[acc.z]*d[dir1.x]*d[dir1.x]-2.*d[acc.z]*d[dir1.y]*d[dir1.y]+
                    (2.*d[acc.y]*d[dir1.y]+2.*d[acc.x]*d[dir1.x])*d[dir1.z]+(2.*d[acc.x]*d[dir1.z]-4*d[acc.z]*d[dir1.x])*g[0]-2.*d[acc.z]*g[0]*g[0]+(2.*d[acc.y]*d[dir1.z]-
                    4*d[acc.z]*d[dir1.y])*g[1]-
                    2.*d[acc.z]*g[1]*g[1]+(2.*d[acc.y]*g[1]+2.*d[acc.x]*g[0]+2.*d[acc.y]*d[dir1.y]+2.*d[acc.x]*d[dir1.x])*g[2]+2.*d[dir2.x]*d[dir2.z]*d[pos.x]
                    +2.*d[dir2.y]*d[dir2.z]*d[pos.y]+
                    (-2.*d[dir2.y]*d[dir2.y]-2.*d[dir2.x]*d[dir2.x])*d[pos.z]+(2.*d[dir2.x]*g[2]+2.*d[dir2.z]*g[0]+2.*d[dir1.x]*d[dir2.z]+2.*d[dir1.z]*d[dir2.x])*d[vel.x]+
                    (2.*d[dir2.y]*g[2]+2.*d[dir2.z]*g[1]+2.*d[dir1.y]*d[dir2.z]+2.*d[dir1.z]*d[dir2.y])*d[vel.y])*d[vel.z]+
                    (-2.*d[dir1.x]*d[dir2.x]-2.*d[dir1.z]*d[dir2.z]-2.*d[dir2.x]*g[0]-2.*d[dir2.z]*g[2])*d[vel.y]*d[vel.y]+(-2.*d[acc.y]*d[dir1.x]*d[dir1.x]+2.*d[acc.x]*d[dir1.x]*d[dir1.y]
                    +2.*d[acc.z]*d[dir1.y]*d[dir1.z]-2.
                    *d[acc.y]*d[dir1.z]*d[dir1.z]+(2.*d[acc.x]*d[dir1.y]-4*d[acc.y]*d[dir1.x])*g[0]-2.*d[acc.y]*g[0]*g[0]+(2.*d[acc.x]*g[0]+2.*d[acc.z]*d[dir1.z]
                    +2.*d[acc.x]*d[dir1.x])*g[1]+
                    (2.*d[acc.z]*g[1]-4*d[acc.y]*d[dir1.z]+2.*d[acc.z]*d[dir1.y])*g[2]-2.*d[acc.y]*g[2]*g[2]+2.*d[dir2.x]*d[dir2.y]*d[pos.x]+(-2.*d[dir2.z]*d[dir2.z]-
                    2.*d[dir2.x]*d[dir2.x])*d[pos.y]+2.*d[dir2.y]*
                    d[dir2.z]*d[pos.z]+(2.*d[dir2.x]*g[1]+2.*d[dir2.y]*g[0]+2.*d[dir1.x]*d[dir2.y]+2.*d[dir1.y]*d[dir2.x])*d[vel.x])*d[vel.y]+
                    (-2.*d[dir1.y]*d[dir2.y]-2.*d[dir1.z]*d[dir2.z]-2.*d[dir2.y]*g[1]-2.*d[dir2.z]*g[2])*d[vel.x]*d[vel.x]+(2.*d[acc.y]*d[dir1.x]*d[dir1.y]-
                    2.*d[acc.x]*d[dir1.y]*d[dir1.y]+2.*d[acc.z]*d[dir1.x]*d[dir1.z]-2.*
                    d[acc.x]*d[dir1.z]*d[dir1.z]+(2.*d[acc.z]*d[dir1.z]+2.*d[acc.y]*d[dir1.y])*g[0]+(2.*d[acc.y]*g[0]-4*d[acc.x]*d[dir1.y]+2.*d[acc.y]*d[dir1.x])*g[1]-2.*d[acc.x]*g[1]*g[1]+
                    (2.*d[acc.z]*g[0]-4*d[acc.x]*d[dir1.z]+2.*d[acc.z]*d[dir1.x])*g[2]-2.*d[acc.x]*g[2]*g[2]+(-2.*d[dir2.z]*d[dir2.z]-2.*d[dir2.y]*d[dir2.y])*d[pos.x]
                    +2.*d[dir2.x]*d[dir2.y]*d[pos.y]+2.*d[dir2.x]*
                    d[dir2.z]*d[pos.z])*d[vel.x]+((2.*d[acc.x]*d[dir1.z]-4*d[acc.z]*d[dir1.x])*d[dir2.x]+(2.*d[acc.y]*d[dir1.z]-4*d[acc.z]*d[dir1.y])*d[dir2.y]+(2.*d[acc.y]*d[dir1.y]
                    +2.*d[acc.x]*d[dir1.x])*
                    d[dir2.z]+(2.*d[acc.x]*d[dir2.z]-4*d[acc.z]*d[dir2.x])*g[0]+(2.*d[acc.y]*d[dir2.z]-4*d[acc.z]*d[dir2.y])*g[1]+(2.*d[acc.y]*d[dir2.y]
                    +2.*d[acc.x]*d[dir2.x])*g[2])*d[pos.z]+(
                    (2.*d[acc.x]*d[dir1.y]-4*d[acc.y]*d[dir1.x])*d[dir2.x]+(2.*d[acc.z]*d[dir1.z]+2.*d[acc.x]*d[dir1.x])*d[dir2.y]+(2.*d[acc.z]*d[dir1.y]-4*d[acc.y]*d[dir1.z])*d[dir2.z]+
                    (2.*d[acc.x]*d[dir2.y]-4*d[acc.y]*d[dir2.x])*g[0]+(2.*d[acc.z]*d[dir2.z]+2.*d[acc.x]*d[dir2.x])*g[1]+(2.*d[acc.z]*d[dir2.y]-4*d[acc.y]*d[dir2.z])*g[2])*d[pos.y]+(
                    (2.*d[acc.z]*d[dir1.z]+2.*d[acc.y]*d[dir1.y])*d[dir2.x]+(2.*d[acc.y]*d[dir1.x]-4*d[acc.x]*d[dir1.y])*d[dir2.y]+(2.*d[acc.z]*d[dir1.x]-4*d[acc.x]*d[dir1.z])*d[dir2.z]+
                    (2.*d[acc.z]*d[dir2.z]+2.*d[acc.y]*d[dir2.y])*g[0]+(2.*d[acc.y]*d[dir2.x]-4*d[acc.x]*d[dir2.y])*g[1]+(2.*d[acc.z]*d[dir2.x]-4*d[acc.x]*d[dir2.z])*g[2])*d[pos.x];
            c[4] = 
                    (-d[dir1.x]*d[dir1.x]-d[dir1.y]*d[dir1.y]-2.*d[dir1.x]*g[0]-g[0]*g[0]-2.*d[dir1.y]*g[1]-g[1]*g[1])*d[vel.z]*d[vel.z]+(
                    (2.*d[dir2.x]*g[2]+2.*d[dir2.z]*g[0]+2.*d[dir1.x]*d[dir2.z]+2.*d[dir1.z]*d[dir2.x])*d[pos.x]+(2.*d[dir2.y]*g[2]+2.*d[dir2.z]*g[1]+2.*d[dir1.y]*d[dir2.z]
                    +2.*d[dir1.z]*d[dir2.y])*
                    d[pos.y]+(-4*d[dir2.y]*g[1]-4*d[dir2.x]*g[0]-4*d[dir1.y]*d[dir2.y]-4*d[dir1.x]*d[dir2.x])*d[pos.z]+
                    ((2.*d[dir1.x]+2.*g[0])*g[2]+2.*d[dir1.z]*g[0]+2.*d[dir1.x]*d[dir1.z])*d[vel.x]+((2.*d[dir1.y]+2.*g[1])*g[2]+2.*d[dir1.z]*g[1]
                    +2.*d[dir1.y]*d[dir1.z])*d[vel.y]
                    )*d[vel.z]+(-d[dir1.x]*d[dir1.x]-d[dir1.z]*d[dir1.z]-2.*d[dir1.x]*g[0]-g[0]*g[0]-2.*d[dir1.z]*g[2]-g[2]*g[2])*d[vel.y]*d[vel.y]+(
                    (2.*d[dir2.x]*g[1]+2.*d[dir2.y]*g[0]+2.*d[dir1.x]*d[dir2.y]+2.*d[dir1.y]*d[dir2.x])*d[pos.x]+(-4*d[dir2.z]*g[2]-4*d[dir2.x]*g[0]-4*d[dir1.z]*d[dir2.z]-
                    4*d[dir1.x]*d[dir2.x])*
                    d[pos.y]+(2.*d[dir2.y]*g[2]+2.*d[dir2.z]*g[1]+2.*d[dir1.y]*d[dir2.z]+2.*d[dir1.z]*d[dir2.y])*d[pos.z]+
                    ((2.*d[dir1.x]+2.*g[0])*g[1]+2.*d[dir1.y]*g[0]+2.*d[dir1.x]*d[dir1.y])*d[vel.x])*d[vel.y]+
                    (-d[dir1.y]*d[dir1.y]-d[dir1.z]*d[dir1.z]-2.*d[dir1.y]*g[1]-g[1]*g[1]-2.*d[dir1.z]*g[2]-g[2]*g[2])*d[vel.x]*d[vel.x]+(
                    (-4*d[dir2.z]*g[2]-4*d[dir2.y]*g[1]-4*d[dir1.z]*d[dir2.z]-4*d[dir1.y]*d[dir2.y])*d[pos.x]+(2.*d[dir2.x]*g[1]+2.*d[dir2.y]*g[0]+2.*d[dir1.x]*d[dir2.y]
                    +2.*d[dir1.y]*d[dir2.x])*
                    d[pos.y]+(2.*d[dir2.x]*g[2]+2.*d[dir2.z]*g[0]+2.*d[dir1.x]*d[dir2.z]+2.*d[dir1.z]*d[dir2.x])*d[pos.z])*d[vel.x]+(d[dir2.x]*d[dir2.x]+d[dir2.y]*d[dir2.y]+d[dir2.z]*d[dir2.z])*r*r+
                    (-d[dir2.x]*d[dir2.x]-d[dir2.y]*d[dir2.y])*d[pos.z]*d[pos.z]+(-2.*d[acc.z]*d[dir1.x]*d[dir1.x]-2.*d[acc.z]*d[dir1.y]*d[dir1.y]+(2.*d[acc.y]*d[dir1.y]+2.*d[acc.x]*d[dir1.x])*d[dir1.z]+
                    (2.*d[acc.x]*d[dir1.z]-4*d[acc.z]*d[dir1.x])*g[0]-2.*d[acc.z]*g[0]*g[0]+(2.*d[acc.y]*d[dir1.z]-4*d[acc.z]*d[dir1.y])*g[1]-2.*d[acc.z]*g[1]*g[1]+
                    (2.*d[acc.y]*g[1]+2.*d[acc.x]*g[0]+2.*d[acc.y]*d[dir1.y]+2.*d[acc.x]*d[dir1.x])*g[2]+2.*d[dir2.x]*d[dir2.z]*d[pos.x]+2.*d[dir2.y]*d[dir2.z]*d[pos.y])*d[pos.z]+
                    (-d[dir2.x]*d[dir2.x]-d[dir2.z]*d[dir2.z])*d[pos.y]*d[pos.y]+(-2.*d[acc.y]*d[dir1.x]*d[dir1.x]+2.*d[acc.x]*d[dir1.x]*d[dir1.y]+2.*d[acc.z]*d[dir1.y]*d[dir1.z]-2.*d[acc.y]*d[dir1.z]*d[dir1.z]+
                    (2.*d[acc.x]*d[dir1.y]-4*d[acc.y]*d[dir1.x])*g[0]-2.*d[acc.y]*g[0]*g[0]+(2.*d[acc.x]*g[0]+2.*d[acc.z]*d[dir1.z]+2.*d[acc.x]*d[dir1.x])*g[1]+
                    (2.*d[acc.z]*g[1]-4*d[acc.y]*d[dir1.z]+2.*d[acc.z]*d[dir1.y])*g[2]-2.*d[acc.y]*g[2]*g[2]+2.*d[dir2.x]*d[dir2.y]*d[pos.x])*d[pos.y]+(-d[dir2.y]*d[dir2.y]-
                    d[dir2.z]*d[dir2.z])*d[pos.x]*d[pos.x]+(2.*
                    d[acc.y]*d[dir1.x]*d[dir1.y]-2.*d[acc.x]*d[dir1.y]*d[dir1.y]+2.*d[acc.z]*d[dir1.x]*d[dir1.z]-2.*d[acc.x]*d[dir1.z]*d[dir1.z]+(2.*d[acc.z]*d[dir1.z]+2.*d[acc.y]*d[dir1.y])*g[0]+
                    (2.*d[acc.y]*g[0]-4*d[acc.x]*d[dir1.y]+2.*d[acc.y]*d[dir1.x])*g[1]-2.*d[acc.x]*g[1]*g[1]+(2.*d[acc.z]*g[0]-4*d[acc.x]*d[dir1.z]
                    +2.*d[acc.z]*d[dir1.x])*g[2]-2.*d[acc.x]*
                    g[2]*g[2])*d[pos.x]
                    ;
            c[5] = 
                    (((2.*d[dir1.x]+2.*g[0])*g[2]+2.*d[dir1.z]*g[0]+2.*d[dir1.x]*d[dir1.z])*d[pos.x]+
                    ((2.*d[dir1.y]+2.*g[1])*g[2]+2.*d[dir1.z]*g[1]+2.*d[dir1.y]*d[dir1.z])*d[pos.y]+
                    (-2.*g[1]*g[1]-4*d[dir1.y]*g[1]-2.*g[0]*g[0]-4*d[dir1.x]*g[0]-2.*d[dir1.y]*d[dir1.y]-2.*d[dir1.x]*d[dir1.x])*d[pos.z])*d[vel.z]+(
                    ((2.*d[dir1.x]+2.*g[0])*g[1]+2.*d[dir1.y]*g[0]+2.*d[dir1.x]*d[dir1.y])*d[pos.x]+
                    (-2.*g[2]*g[2]-4*d[dir1.z]*g[2]-2.*g[0]*g[0]-4*d[dir1.x]*g[0]-2.*d[dir1.z]*d[dir1.z]-2.*d[dir1.x]*d[dir1.x])*d[pos.y]+
                    ((2.*d[dir1.y]+2.*g[1])*g[2]+2.*d[dir1.z]*g[1]+2.*d[dir1.y]*d[dir1.z])*d[pos.z])*d[vel.y]+(
                    (-2.*g[2]*g[2]-4*d[dir1.z]*g[2]-2.*g[1]*g[1]-4*d[dir1.y]*g[1]-2.*d[dir1.z]*d[dir1.z]-2.*d[dir1.y]*d[dir1.y])*d[pos.x]+
                    ((2.*d[dir1.x]+2.*g[0])*g[1]+2.*d[dir1.y]*g[0]+2.*d[dir1.x]*d[dir1.y])*d[pos.y]+((2.*d[dir1.x]+2.*g[0])*g[2]+2.*d[dir1.z]*g[0]
                    +2.*d[dir1.x]*d[dir1.z])*d[pos.z]
                    )*d[vel.x]+(2.*d[dir1.x]*d[dir2.x]+2.*d[dir1.y]*d[dir2.y]+2.*d[dir1.z]*d[dir2.z]+2.*d[dir2.x]*g[0]+2.*d[dir2.y]*g[1]+2.*d[dir2.z]*g[2])*r*r+
                    (-2.*d[dir1.x]*d[dir2.x]-2.*d[dir1.y]*d[dir2.y]-2.*d[dir2.x]*g[0]-2.*d[dir2.y]*g[1])*d[pos.z]*d[pos.z]+(
                    (2.*d[dir2.x]*g[2]+2.*d[dir2.z]*g[0]+2.*d[dir1.x]*d[dir2.z]+2.*d[dir1.z]*d[dir2.x])*d[pos.x]+(2.*d[dir2.y]*g[2]+2.*d[dir2.z]*g[1]+2.*d[dir1.y]*d[dir2.z]
                    +2.*d[dir1.z]*d[dir2.y])*
                    d[pos.y])*d[pos.z]+(-2.*d[dir1.x]*d[dir2.x]-2.*d[dir1.z]*d[dir2.z]-2.*d[dir2.x]*g[0]-2.*d[dir2.z]*g[2])*d[pos.y]*d[pos.y]+
                    (2.*d[dir1.y]*d[dir2.x]+2.*d[dir1.x]*d[dir2.y]+2.*d[dir2.y]*g[0]+2.*d[dir2.x]*g[1])*d[pos.x]*d[pos.y]+
                    (-2.*d[dir1.y]*d[dir2.y]-2.*d[dir1.z]*d[dir2.z]-2.*d[dir2.y]*g[1]-2.*d[dir2.z]*g[2])*d[pos.x]*d[pos.x]
                    ;
              c[6] = 
                      (d[dir1.x]*d[dir1.x]+d[dir1.y]*d[dir1.y]+d[dir1.z]*d[dir1.z]+2.*d[dir1.x]*g[0]+g[0]*g[0]+2.*d[dir1.y]*g[1]+g[1]*g[1]+2.*d[dir1.z]*g[2]+g[2]*g[2])*r*r+
                        (-d[dir1.x]*d[dir1.x]-d[dir1.y]*d[dir1.y]-2.*d[dir1.x]*g[0]-g[0]*g[0]-2.*d[dir1.y]*g[1]-g[1]*g[1])*d[pos.z]*d[pos.z]+(
                        ((2.*d[dir1.x]+2.*g[0])*g[2]+2.*d[dir1.z]*g[0]+2.*d[dir1.x]*d[dir1.z])*d[pos.x]+((2.*d[dir1.y]+2.*g[1])*g[2]+2.*d[dir1.z]*g[1]
                        +2.*d[dir1.y]*d[dir1.z])*d[pos.y]
                        )*d[pos.z]+(-d[dir1.x]*d[dir1.x]-d[dir1.z]*d[dir1.z]-2.*d[dir1.x]*g[0]-g[0]*g[0]-2.*d[dir1.z]*g[2]-g[2]*g[2])*d[pos.y]*d[pos.y]+
                        (2.*d[dir1.x]*d[dir1.y]+2.*d[dir1.y]*g[0]+(2.*g[0]+2.*d[dir1.x])*g[1])*d[pos.x]*d[pos.y]+
                        (-d[dir1.y]*d[dir1.y]-d[dir1.z]*d[dir1.z]-2.*d[dir1.y]*g[1]-g[1]*g[1]-2.*d[dir1.z]*g[2]-g[2]*g[2])*d[pos.x]*d[pos.x]
                    ;

            }
              c = Polynomials.trimLeadingZeros(c);
              if(c.length > 5) {
                  //here 0, or ZERO_TOLERANCE if present in specset
                double res = Polynomials.firstSolution(c, Accelerated.ZERO_TOLERANCE, 5);
                if(!Double.isNaN(res)) store.set((float)res, (float)res);
              }
              else {
                  double[] roots = Polynomials.solveClosed(c);
                  if(roots != null) store.set(roots);
              }
        }
        finally {
            vars.release();
        }
    }
     /**
      * Stores result of a 1-dimensional collision
      * 
      * @param start - position of point
      * @param vel   - velocity of point
      * @param accel - acceleration of point
      * @param min   - lower bound
      * @param max   - upper bound
      * @param store - result object
      */
     public static void bound(float start, float vel, float accel, float min, float max, Results store) {
        store.len = 0;
        max -= start; min -= start;
        accel *= 0.5f;
        //special case if min == max...
        
        if(vel == 0f && accel == 0f) {
            if(max >= 0 && min <= 0) {
                //[0, Float.MAX_VALUE]
                store.set(0f, Float.MAX_VALUE);
            }
            //nothing
            return;
        }
        if(accel == 0f) {
            max = max/vel; min = min/vel;
            if(min > max) { start = max; max = min; min = start; }
            if(max < 0f) return;
            if(min < 0f) min = 0f;
            //[min, max]
            store.set(min, max);
        }
        else {
            double[] r1, r2;
            if(max < 0f) {
                if(vel >= 0f && accel >= 0f) return;
                r1 = Polynomials.solveQuadric(accel, vel, -max);
                if(r1 == null) return;
                r2 = Polynomials.solveQuadric(accel, vel, -min);
            }
            else if(min > 0f) {
                if(vel <= 0f && accel <= 0f) return;
                r1 = Polynomials.solveQuadric(accel, vel, -min);
                if(r1 == null) return;
                r2 = Polynomials.solveQuadric(accel, vel, -max);
            }
            else {
                r1 = Polynomials.solveQuadric(accel, vel, -max);
                r2 = Polynomials.solveQuadric(accel, vel, -min);
                if(r1 == null) {
                    if(r2 == null) return;
                    if(r2[1] < 0f) return;
                    if(r2[0] < 0f) r2[0] = 0f;
                    r1 = r2; r2 = null;
                }
                else if(r2 != null) {
                    if(r1[0] > r2[0]) {
                        double[] s = r1;
                        r1 = r2;
                        r2 = s;
                    }
                }
            }
            if(r2 == null) {
                //r1
                if(r1[1] < 0f) return;
                if(r1[0] < 0f) r1[0] = 0f; 
                store.set((float)r1[0], (float)r1[1]);
            }
            else {
                //r1[0], r2[0], r1[1], r2[1]
                if(r2[1] < 0f) return;
                if(r1[1] < 0f) {
                    store.set(0f, (float)r2[1]); return;
                }
                if(r2[0] < 0f) {
                    store.set((float)r1[1], (float)r2[1]); return;
                }
                if(r1[0] < 0f) {
                    r1[0] = 0f;
                }
                store.set((float)r1[0], (float)r2[0], (float)r1[1], (float)r2[1]);
           }
        }
    }
    public static void calcForPlanePoint(final SphereShape s, float a, float b, float c, float d, Results store) {
        final float[] dd = s.data;
        float q = 0.5f*(a*dd[acc.x]+b*dd[acc.y]+c*dd[acc.z]);
        float r = a*dd[vel.x]+b*dd[vel.y]+c*dd[vel.z];
        
        if(q == 0f) {
            if(r == 0) return;
            float[] time = store.time;
            q = -a*dd[pos.x]-b*dd[pos.y]-c*dd[pos.z]-d;
//            if(r > 0) {
//                time[0] = (-abcRad+q)/r;
//                time[1] = (abcRad+q)/r;
//            }
//            else {
                time[0] = time[1] = (q)/r;
//            }
            store.len = 2;
        }
        else {
            final float term = a*dd[pos.x]+b*dd[pos.y]+c*dd[pos.z]+d;
            final Vars vars = Vars.get();
            double[] roots = Polynomials.solveQuadric(q, r, term, vars.d2arr1);
            if(roots != null) store.set(roots);
            vars.release();
        }
    }
    public static void calcForTerrain(final SphereShape s, final Results store) {
        
    }
    /**
     * Calculates collision between two circles, considering x, z values
     */
    public static void calcForSpheresXZ(final SphereShape s1, final SphereShape s2, final Results store) {
        final Vars vars = Vars.get();
        
        final float[] d = vars.farr1;
        final float[] a = s1.getData(vars.farr2), b = s2.getData(vars.farr3);
        //can skip y, or support various orientations
        for(int i = 0; i < 9; i++) d[i] = a[i]-b[i];
       
        float bigRadius = a[rad]+b[rad];
        bigRadius = bigRadius*bigRadius;
        
        float termA = 0.25f*(d[acc.x]*d[acc.x]+d[acc.z]*d[acc.z]);
        float termC = d[vel.x]*d[vel.x]+d[vel.z]*d[vel.z];
        double[] roots = 
                (termC == 0)? (d[pos.x]*d[pos.x]+d[pos.z]*d[pos.z] > bigRadius)?null
                :new double[] {0, 0/*Double.POSITIVE_INFINITY ? relly?*/}
                :(termA == 0)?Polynomials.solveQuadric(
                    termC,
                    2f*(d[pos.x]*d[vel.x]+d[pos.z]*d[vel.z]),
                    d[pos.x]*d[pos.x]+d[pos.z]*d[pos.z]-bigRadius)
       
                    :Polynomials.solveQuartic(
                    termA, d[acc.x]*d[vel.x]+d[acc.z]*d[vel.z],
                    d[vel.x]*d[vel.x]+d[vel.z]*d[vel.z]
                    +d[acc.x]*d[pos.x]+d[acc.z]*d[pos.z],
                    2f*(d[pos.x]*d[vel.x]+d[pos.z]*d[vel.z]),
                    d[pos.x]*d[pos.x]+d[pos.z]*d[pos.z]-bigRadius);

        vars.release();
        if(roots == null) return;
//        store.set(roots, 0f);
        store.set(roots);
    }
    public static void calcForSpheres(float[] d, float radSum, final Results store) {
        radSum = radSum*radSum;
        float termA = 0.25f*(d[acc.x]*d[acc.x]+d[acc.y]*d[acc.y]+d[acc.z]*d[acc.z]);
        float termC = d[vel.x]*d[vel.x]+d[vel.y]*d[vel.y]+d[vel.z]*d[vel.z];
        double[] roots = 
                (termA != 0)?Polynomials.solveQuartic(
                    termA, d[acc.x]*d[vel.x]+d[acc.y]*d[vel.y]+d[acc.z]*d[vel.z],
                    d[vel.x]*d[vel.x]+d[vel.y]*d[vel.y]+d[vel.z]*d[vel.z]
                    +d[acc.x]*d[pos.x]+d[acc.y]*d[pos.y]+d[acc.z]*d[pos.z],
                    2f*(d[pos.x]*d[vel.x]+d[pos.y]*d[vel.y]+d[pos.z]*d[vel.z]),
                    d[pos.x]*d[pos.x]+d[pos.y]*d[pos.y]+d[pos.z]*d[pos.z]-radSum):
                ((termC != 0)?Polynomials.solveQuadric(
                    termC,
                    2f*(d[pos.x]*d[vel.x]+d[pos.y]*d[vel.y]+d[pos.z]*d[vel.z]),
                    d[pos.x]*d[pos.x]+d[pos.y]*d[pos.y]+d[pos.z]*d[pos.z]-radSum):
                ((d[pos.x]*d[pos.x]+d[pos.y]*d[pos.y]+d[pos.z]*d[pos.z] > radSum)?
                null:new double[] {0, 0/*Double.POSITIVE_INFINITY ? relly?*/})
                );
//         Log.log(Arrays.toString(roots), Color.magenta);
//        Log.errln("not rot Roots " + Arrays.toString(roots));
        if(roots == null) {
            return;
        }
//        store.set(roots, 0f);
        store.set(roots);
    }
    public static void calcForSpheres(float[] rPos, float[] rVel, float[] rAccel, float radSum, final Results store) {
        radSum = radSum*radSum;
        float termA = 0.25f*(rAccel[0]*rAccel[0]+rAccel[1]*rAccel[1]+rAccel[2]*rAccel[2]);
        float termC = rVel[0]*rVel[0]+rVel[1]*rVel[1]+rVel[2]*rVel[2];
        double[] roots = 
                (termA != 0)?Polynomials.solveQuartic(
                    termA, rAccel[0]*rVel[0]+rAccel[1]*rVel[1]+rAccel[2]*rVel[2],
                    rVel[0]*rVel[0]+rVel[1]*rVel[1]+rVel[2]*rVel[2]
                    +rAccel[0]*rPos[0]+rAccel[1]*rPos[1]+rAccel[2]*rPos[2],
                    2f*(rPos[0]*rVel[0]+rPos[1]*rVel[1]+rPos[2]*rVel[2]),
                    rPos[0]*rPos[0]+rPos[1]*rPos[1]+rPos[2]*rPos[2]-radSum):
                ((termC != 0)?Polynomials.solveQuadric(
                    termC,
                    2f*(rPos[0]*rVel[0]+rPos[1]*rVel[1]+rPos[2]*rVel[2]),
                    rPos[0]*rPos[0]+rPos[1]*rPos[1]+rPos[2]*rPos[2]-radSum):
                ((rPos[0]*rPos[0]+rPos[1]*rPos[1]+rPos[2]*rPos[2] > radSum)?
                null:new double[] {0, 0/*Double.POSITIVE_INFINITY ? relly?*/})
                );
//         Log.log(Arrays.toString(roots), Color.magenta);
//        Log.errln("not rot Roots " + Arrays.toString(roots));
        if(roots == null) {
            return;
        }
//        store.set(roots, 0f);
        store.set(roots);
    }
    public static void calcForSpheres(final SphereShape s1, final SphereShape s2, final Results store) {
        final Vars vars = Vars.get();
        
        final float[] d = vars.farr1;
        final float[] a = s1.getData(vars.farr2), b = s2.getData(vars.farr3);
        for(int i = 0; i < 9; i++) d[i] = a[i]-b[i];
        
        final float termA = 0.25f*(d[acc.x]*d[acc.x]+d[acc.y]*d[acc.y]+d[acc.z]*d[acc.z]);
        final float termC = d[vel.x]*d[vel.x]+d[vel.y]*d[vel.y]+d[vel.z]*d[vel.z];
        float termD = a[rad]+b[rad]; 
        termD = d[pos.x]*d[pos.x]+d[pos.y]*d[pos.y]+d[pos.z]*d[pos.z]-termD*termD;      
        double[] roots = 
                (termA != 0)?Polynomials.solveQuartic(
                    termA, d[acc.x]*d[vel.x]+d[acc.y]*d[vel.y]+d[acc.z]*d[vel.z],
                    termC+d[acc.x]*d[pos.x]+d[acc.y]*d[pos.y]+d[acc.z]*d[pos.z],
                    2f*(d[pos.x]*d[vel.x]+d[pos.y]*d[vel.y]+d[pos.z]*d[vel.z]), termD):
                ((termC != 0)?Polynomials.solveQuadric(
                    termC, 2f*(d[pos.x]*d[vel.x]+d[pos.y]*d[vel.y]+d[pos.z]*d[vel.z]),
                    termD, vars.d2arr1):
                ((termD > 0)?
                null:Vars.zero2)//new double[] {0, 0/*Double.POSITIVE_INFINITY ?*/})
                );
        
         vars.release();
         if(roots != null) store.set(roots);
    }
    
  
    public static void calcForLines(LineShape l1, LineShape l2, Results store) {
        final Vars vars = Vars.get();
        final float[] d = vars.farr1, cr = vars.farr4, dr = vars.farr5;
        final float[] a = l1.getData(vars.farr2), b = l2.getData(vars.farr3);
        for(int i = 0; i < 9; i++) d[i] = a[i]-b[i];
        for(int i = 0; i < 3; i++) {
            d[DIR1+i] = a[DIR1+i] + (a[DIR2+i]-a[DIR1+i])*a[15]*a[16];
            d[DIR2+i] = b[DIR1+i] + (b[DIR2+i]-b[DIR1+i])*b[15]*b[16];
            cr[i] = a[DIR2+i] - a[DIR1+i];
            dr[i] = b[DIR2+i] - b[DIR1+i];
        }
        
        //solve A*t^4 + B*t^3 + C*t^2 + D*t + E = 0
        double tmp1,tmp2,tmp3;
        tmp1 = cr[1]*dr[2]-cr[2]*dr[1];
        tmp2 = cr[2]*dr[0]-cr[0]*dr[2];
        tmp3 = cr[0]*dr[1]-cr[1]*dr[0];
        double A = 0.5*a[15]*b[15]*(d[acc.x]*tmp1+d[acc.y]*tmp2+d[acc.z]*tmp3);
        double B =     a[15]*b[15]*(d[vel.z]*tmp3+d[vel.y]*tmp2+d[vel.x]*tmp1);      
        double C =     a[15]*b[15]*( d[pos.z]*tmp3+ d[pos.y]*tmp2+ d[pos.x]*tmp1);       
        tmp1 = a[15]*(cr[1]*d[dir2.z]-cr[2]*d[dir2.y])+b[15]*(d[dir1.y]*dr[2]-d[dir1.z]*dr[1]);
        tmp2 = a[15]*(cr[2]*d[dir2.x]-cr[0]*d[dir2.z])+b[15]*(d[dir1.z]*dr[0]-d[dir1.x]*dr[2]);
        tmp3 = a[15]*(cr[0]*d[dir2.y]-cr[1]*d[dir2.x])+b[15]*(d[dir1.x]*dr[1]-d[dir1.y]*dr[0]);
        B += 0.5*(d[acc.x]*tmp1+d[acc.y]*tmp2+d[acc.z]*tmp3);
        C += d[vel.z]*tmp3+d[vel.y]*tmp2+d[vel.x]*tmp1;
        double D = d[pos.z]*tmp3+d[pos.y]*tmp2+d[pos.x]*tmp1;
        tmp1 = d[dir1.y]*d[dir2.z]-d[dir1.z]*d[dir2.y];
        tmp2 = d[dir1.z]*d[dir2.x]-d[dir1.x]*d[dir2.z];
        tmp3 = d[dir1.x]*d[dir2.y]-d[dir1.y]*d[dir2.x];
        C += 0.5*(d[acc.x]*tmp1+d[acc.y]*tmp2+d[acc.z]*tmp3);
        D += d[vel.z]*tmp3+d[vel.y]*tmp2+d[vel.x]*tmp1;
        double E = d[pos.x]*tmp1+d[pos.y]*tmp2+d[pos.z]*tmp3;
        
        vars.release();

        double[] roots = null;
        if(A != 0.0) {
            roots = Polynomials.solveQuartic(A, B, C, D, E);
        }
        else if(B != 0.0) {
            //NOTE: can return 3 roots... which is odd number
            roots = Polynomials.solveCubic(B, C, D, E);
            if((roots.length&1) != 0) {
                //NOTE: migth copy wrong result
                store.len = roots.length+1;
                store.time[0] = (float)roots[0];
                for(int i = 0; i < roots.length; i++)
                    store.time[i+1] = (float)roots[i];
                return;
            }
        } 
        else if(C != 0.0) {
            roots = Polynomials.solveQuadric(C, D, E);
        }
        else if(D != 0.0) {
            float val = (float)(-E/D);
            if(val >= 0f) {
                store.len = 2;
                store.time[0] = val;
                store.time[1] = val;
            }
            return;
        }
        if(roots == null) {
            return;
        }
        store.set(roots);
    }
}
