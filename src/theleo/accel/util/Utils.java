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

import theleo.accel.Results;
import theleo.accel.shapes.Shape;
import static theleo.accel.shapes.SphereShape.*;

/**
 * Few utility methods.
 * 
 * @author Juraj Papp
 */
public class Utils {
//    public static void perpendicular(Vector3f v, Vector3f store) {
//        if(zero(v.x) && zero(v.y)) 
//            //if zero(v.z) error
//            store.set(0f, 1f, 0f);
//        else 
//            store.set(-v.y, v.x, 0f);
//    }
    
    
    public static boolean zero(float f) {
        return f > -0.0001f && f < 0.0001f;
    }
    public static float[] mergeSorted(float[] a, int alen, float b[], int blen) {
        float[] answer = new float[alen + blen];
        int i = 0, j = 0, k = 0;
        while (i < alen && j < blen) {
            if (a[i] < b[j]) answer[k++] = a[i++];
            else answer[k++] = b[j++];
        }
        while (i < alen) answer[k++] = a[i++];
        while (j < blen) answer[k++] = b[j++];
        return answer;
    }
    public static float[] mergeSorted(float[] a, float b[]) {
        float[] answer = new float[a.length + b.length];
        int i = 0, j = 0, k = 0;
        while (i < a.length && j < b.length) {
            if (a[i] < b[j]) answer[k++] = a[i++];
            else answer[k++] = b[j++];
        }
        while (i < a.length) answer[k++] = a[i++];
        while (j < b.length) answer[k++] = b[j++];
        return answer;
    }
    public static double[] mergeSorted(double[] a, double b[]) {
        double[] answer = new double[a.length + b.length];
        int i = 0, j = 0, k = 0;
        while (i < a.length && j < b.length) {
            if (a[i] < b[j]) answer[k++] = a[i++];
            else answer[k++] = b[j++];
        }
        while (i < a.length) answer[k++] = a[i++];
        while (j < b.length) answer[k++] = b[j++];
        return answer;
    }
    public static double[] mergeSorted(double[] a, double b[], double[] store) {
        int i = 0, j = 0, k = 0;
        while (i < a.length && j < b.length) {
            if (a[i] < b[j]) store[k++] = a[i++];
            else store[k++] = b[j++];
        }
        while (i < a.length) store[k++] = a[i++];
        while (j < b.length) store[k++] = b[j++];
        return store;
    }
    public static void mergeIntervals(float[] a, int alen, float[] b, int blen, Results store) {
        int bOff = 0;
        for(int i = 0; i < alen && bOff < blen; i+= 2) {
            if(b[bOff] > a[i+1]) continue;
            if(b[bOff+1] < a[i]) { bOff+=2; i-=2;continue;}
            store.add(Math.max(b[bOff], a[i]), Math.min(b[bOff+1], a[i+1]));
            if(b[bOff+1] < a[i+1]) {
                bOff+=2; i-=2; continue;
            }
        }
    }
    public static void glPosAt(float d[], float time, Vec3f store) {
        Vars vars = Vars.get();
        final float tt = time*time*0.5f;
        store.set(d[pos.x]+d[vel.x]*time+d[acc.x]*tt,
                  d[pos.y]+d[vel.y]*time+d[acc.y]*tt,
                  d[pos.z]+d[vel.z]*time+d[acc.z]*tt);
        vars.release();
    }
    public static void glPosAt(Shape s, float time, Vec3f store) {
        Vars vars = Vars.get();
        final float tt = time*time*0.5f;
        final float[] d = s.getData(vars.farr1);
        store.set(d[pos.x]+d[vel.x]*time+d[acc.x]*tt,
                  d[pos.y]+d[vel.y]*time+d[acc.y]*tt,
                  d[pos.z]+d[vel.z]*time+d[acc.z]*tt);
        vars.release();
    }
    public static void glPosNow(Shape s, Vec3f store) {
        glPosAt(s, s.travelled, store);
    }
    
    public static void posAt(Shape s, float time, Vec3f store) {
        final float tt = time*time*0.5f;
        final float[] d = s.data;
        store.set(d[pos.x]+d[vel.x]*time+d[acc.x]*tt,
                  d[pos.y]+d[vel.y]*time+d[acc.y]*tt,
                  d[pos.z]+d[vel.z]*time+d[acc.z]*tt);
    }
    public static void posNow(Shape s, Vec3f store) {
        posAt(s, s.travelled, store);
    }
    public static void velAt(Shape s, float time, Vec3f store) {
        final float[] d = s.data;
        store.set(d[vel.x]+d[acc.x]*time,
                  d[vel.y]+d[acc.y]*time,
                  d[vel.z]+d[acc.z]*time);
    }
    public static void velNow(Shape s, Vec3f store) {
        velAt(s, s.travelled, store);
    }
}
