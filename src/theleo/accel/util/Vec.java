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

import theleo.accel.shapes.Shape;

/**
 * Few vector operations on arrays.
 * 
 * @author Juraj Papp
 */
public class Vec {
    
    public static float lenSq(Shape s, int vec) {
        return lenSq(s.data, vec, 3);
    }
    public static float lenSq(float[] vec, int off, int len) {
        float l = 0f; len += off;
        for(int i = off; i < len; i++)
            l += vec[i]*vec[i];
        return l;
    }
    
    public static double[] rotate(double[] vec, double[] angles) {
        return mult(cartes(add(spherical(vec), angles)), Math.sqrt(lengthSq(vec)));
    }
    public static double[] rotateN(double[] vecN, double[] angles) {
        return cartes(add(spherical(vecN), angles));
    }
    public static double[] getRotatation(double[] fromVN, double[] toVN) {
        double[] a = Vec.spherical(fromVN);
        double[] b = Vec.spherical(toVN);
        return sub(b, a);
    }
    public static double[] add(double[] vec, double[] vec2) {
        for(int i = 0; i < vec.length; i++) vec[i] += vec2[i];
        return vec;
    }
    public static double[] sub(double[] vec, double[] vec2) {
        for(int i = 0; i < vec.length; i++) vec[i] -= vec2[i];
        return vec;
    }
    public static double[] mult(double[] vec, double scalar) {
        for(int i = 0; i < vec.length; i++) vec[i] *= scalar;
        return vec;
    }
    public static double[] mult(double[] vec, double[] vec2) {
        for(int i = 0; i < vec.length; i++) vec[i] *= vec2[i];
        return vec;
    }
    public static double[] div(double[] vec, double[] vec2) {
        for(int i = 0; i < vec.length; i++) vec[i] /= vec2[i];
        return vec;
    }
    public static double[] norm(double[] vec) {
        double d = lengthSq(vec);
        if(d != 0 && d != 1) mult(vec, 1/Math.sqrt(d));
        return vec;
    }
    public static double[] cartes(double[] angles) {
        return cartes(angles, new double[angles.length+1]);
    }
    public static double[] cartes(double[] a, double[] store) {
        double b = 1.;
        for(int i = 0; i < a.length; i++) { 
            store[i] = b*Math.cos(a[i]);
            b *= Math.sin(a[i]);
        }
        store[a.length] = b;
        return store;
    }
    
    public static double[] spherical(double[] vec) {
        return spherical(vec, new double[vec.length-1]);
    }
    public static double[] spherical(double[] vec, double[] store) {
        double t;
        for(int i = 0; i < vec.length-1; i++) {
//            System.out.println("vec["+i+"] " + vec[i] + " : " + Math.acos(vec[i]/Math.sqrt(lengthSq(vec, i, store.length+1-i))));
            store[i] = ((t=Math.sqrt(lengthSq(vec, i, vec.length-i)))==0.?0.:(Math.acos(vec[i]/t)));
        }
        if(vec[vec.length-1] < 0) store[vec.length-2] = Math.PI+Math.PI-store[store.length-1];
        return store;
    }
    public static double lengthSq(double[] vec) {
        double len = 0;
        for(int i = 0; i < vec.length; i++)
            len += vec[i]*vec[i];
        return len;
    }
    public static double lengthSq(double[] vec, int from, int len) {
        double l = 0; len += from;
        for(int i = from; i < len; i++)
            l += vec[i]*vec[i];
        return l;
    }
}
