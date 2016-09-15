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
 * A class that deals with polynomials.
 * 
 * @author Juraj Papp
 */
public final class Polynomials {
    
    
    public static double eval(double[] coeff, double x) {
        double r = 0; double xx = 1;
        for(int i = coeff.length-1; i >=0; i--, xx*=x) r += coeff[i]*xx;
        return r; 
    }

    public static double[] solveClosed(double[] coeff) {
        return (coeff.length == 5)?solveQuartic(coeff[0], coeff[1], coeff[2], coeff[3], coeff[4]):
                ((coeff.length == 4)?solveCubic(coeff[0], coeff[1], coeff[2], coeff[3]):
                ((coeff.length == 3)?solveQuadric(coeff[0], coeff[1], coeff[2]):
                (coeff.length == 2)?new double[]{solveLinear(coeff[0], coeff[1])}:null));
    }
    public static double[] trimLeadingZeros(double[] array) {
        if(array[0] == 0) {
                int i = 1;
                for(; i < array.length&&array[i]==0; i++);
                double[] c = new double[array.length-i];
                for(int j = 0; j < c.length; j++) c[j] = array[i+j];
                array = c;
        }
        return array;
    }
    /**
     * c0 x^n + c1 x^n-1 + ....
     * @param coeff coefficients of a polynomial
     * @param from from <= to
     * @param to
     * @return true if the polynomial has real root in (from,to)
     */
    public static int hasSolution(double[] coeff, double from, double to) {
        int s1=0, s2=0;
        double ls1 = 2, ls2 = 2;
        for(int r = 0; r < coeff.length; r++) {
            double x1 = 1, x2 = 1;
            double i1 = 0, i2 = 0;
            for(int i = coeff.length-1-r; i >= 0; i--) {
                i1 += coeff[i]*x1; 
                i2 += coeff[i]*x2;
                x1 *= from; x2 *= to;
                coeff[i] *= coeff.length-i-1;
            }
            i1 = i1>=0?1:-1;
            i2 = i2>=0?1:-1;
            if(ls1 != i1) { s1++; ls1 = i1;}
            if(ls2 != i2) { s2++; ls2 = i2;}
        }
        return s1-s2;
    }
    public static double firstSolution(double[] coeff, double from, double to) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    public static double integral(double[] coeff, double from, double to) {
        double[] c = new double[coeff.length];
        for(int i = 0; i < coeff.length-1; i++)
            c[i] = coeff[i]/(coeff.length-i);
        double x1 = 1, x2 = 1;
        double i1 = 0, i2 = 0;
        for(int i = coeff.length-1; i >= 0; i--) {
            i1 += c[i]*x1; i2 += c[i]*x2;
            x1 *= from; x2 *= to;
        }
        System.out.println("i2: " + i2 + ", i1: " + i1);
        return i2-i1;
    }
    public static double[] square(double[] coeff) {
        double[] res = new double[(coeff.length-1)*2+1];
        for(int i = 0; i < coeff.length; i++) {
            for(int j = 0; j < coeff.length; j++) {
                res[res.length - (coeff.length-i-1)-(coeff.length-j-1)-1] += coeff[i]*coeff[j];
            }
        }
        return res;
    }
    /**
     * Solves the equation ax + b = 0
     * @param a
     * @param b
     * @return 
     */
    public static double solveLinear(double a, double b) {
        return -b/a;
    }
    /**
     * Solves the equation ax^2+bx+c=0. Solutions are returned in a sorted array
     * if they exist.
     *     
* @param a coefficient of x^2
     * @param b coefficient of x^1
     * @param c coefficient of x^0
     * @return an array containing the two real roots, or <code>null</code> if
     * no real solutions exist
     */
    public static double[] solveQuadric(double a, double b, double c) {
        //Log.errln("Quadric[a="+a+", b="+b+", c="+c+"]");
        double disc = b * b - 4 * a * c;
        if (disc < 0) {
            return null;
        }
        disc = Math.sqrt(disc);
        double q = ((b < 0) ? -0.5 * (b - disc) : -0.5 * (b + disc));
        double t0 = q / a;
        double t1 = c / q;
// return sorted array
        return (t0 > t1) ? new double[]{t1, t0} : new double[]{t0, t1};
    }
    public static double[] solveQuadric(double a, double b, double c, double[] store2) {
        //Log.errln("Quadric[a="+a+", b="+b+", c="+c+"]");
        double disc = b * b - 4 * a * c;
        if (disc < 0) {
            return null;
        }
        disc = Math.sqrt(disc);
        double q = ((b < 0) ? -0.5 * (b - disc) : -0.5 * (b + disc));
        double t0 = q / a;
        double t1 = c / q;
// return sorted array
        if(t0 > t1) { store2[0] = t1; store2[1] = t0; }
        else { store2[0] = t0; store2[1] = t1; }
        return store2;
    }
    
    
    
    public static double[] solveQuartic2(double a, double b, double c, double d, double e) {
        a = 1/a; b *= a; c*= a; d *= a; e *= a; a = 1;
        double bb = b*b;
        
        double p = (c - 0.375*bb);
        double q = (d+0.125*bb*b-0.5*b*c);
        double r = (0.0625*bb*c-0.25*b*d-0.01171875*bb*bb+e);
        if(q != 0) {
            double z0 = solveCubicForQuartic(p, 0.25*p*p-r, -0.125*q*q);
//            System.out.println("z0 " + z0);
            double sq2Z = Math.sqrt(2*z0);
            double disc = 2*z0-4*(0.5*p+z0+q/(2*sq2Z));
            if(disc < 0) return null;
            disc = Math.sqrt(disc);
            if(sq2Z > disc) { z0 = sq2Z; sq2Z = disc; disc = z0; }
            return new double[] {-sq2Z-disc, sq2Z-disc, -sq2Z+disc, sq2Z+disc};
        }
        else {
            double d1 = p*p-4*r;
            if(d1 < 0) return null;
            d1 = Math.sqrt(d1);
            double d2 = 0.5*(-p-d1);
            double d3 = 0.5*(-p+d1);
            if(d2 < 0 && d3 < 0) return null;
            if(d2 < 0 || d3 < 0) {
                if(d2 < 0) d2 = d3;
                d2 = Math.sqrt(d2);
                return new double[] {-d2,d2};
            }
            if(d2 > d3) { d1 = d2; d2 = d3; d3 = d1; }
            d2 = Math.sqrt(d2); d3 = Math.sqrt(d3);
            return new double[] {-d3, -d2, d2, d3};
        }
    }
    
    /**
     * Solve a quartic equation of the form ax^4+bx^3+cx^2+cx^1+d=0. The roots
     * are returned in a sorted array of doubles in increasing order.
     *     
* @param a coefficient of x^4
     * @param b coefficient of x^3
     * @param c coefficient of x^2
     * @param d coefficient of x^1
     * @param e coefficient of x^0
     * @return a sorted array of roots, or <code>null</code> if no solutions
     * exist
     */
    public static double[] solveQuartic(double a, double b, double c, double d, double e) {
//        System.out.println("quartic [" + a + ", " + b + ", " + c + ", " + d+ ", " + e + "]");
        double inva = 1 / a;
        double c1 = b * inva;
        double c2 = c * inva;
        double c3 = d * inva;
        double c4 = e * inva;
        //optional, check if its biquadric, then compute
        // cubic resolvant
        double c12 = c1 * c1;
        double p = -0.375 * c12 + c2;
        double q = 0.125 * c12 * c1 - 0.5 * c1 * c2 + c3;
        double r = -0.01171875 * c12 * c12 + 0.0625 * c12 * c2 - 0.25 * c1 * c3 + c4;
        double z = solveCubicForQuartic(-0.5 * p, -r, 0.5 * r * p - 0.125 * q * q);
        double d1 = 2.0 * z - p;
        if (d1 < 0) {
            if (d1 > -1.0e-10) {
                d1 = 0;
            } else {
//                System.out.println("null1: d1: " + d1);
                return null;
            }
        }
        double d2;
        if (d1 < 1.0e-10) {
            d2 = z * z - r;
            if (d2 < 0) {
//                System.out.println("null2");
                return null;
            }
            d2 = Math.sqrt(d2);
        } else {
            d1 = Math.sqrt(d1);
            d2 = 0.5 * q / d1;
        }
// setup usefull values for the quadratic factors
        double q1 = d1 * d1;
        double q2 = -0.25 * c1;
        double pm = q1 - 4 * (z - d2);
        double pp = q1 - 4 * (z + d2);
        if (pm >= 0 && pp >= 0) {
// 4 roots (!)
            pm = Math.sqrt(pm);
            pp = Math.sqrt(pp);
            double[] results = new double[4];
            results[0] = -0.5 * (d1 + pm) + q2;
            results[1] = -0.5 * (d1 - pm) + q2;
            results[2] = 0.5 * (d1 + pp) + q2;
            results[3] = 0.5 * (d1 - pp) + q2;
// tiny insertion sort
            for (int i = 1; i < 4; i++) {
                for (int j = i; j > 0 && results[j - 1] > results[j]; j--) {
                    double t = results[j];
                    results[j] = results[j - 1];
                    results[j - 1] = t;
                }
            }
            return results;
        } else if (pm >= 0) {
            pm = Math.sqrt(pm);
            double[] results = new double[2];
            results[0] = -0.5 * (d1 + pm) + q2;
            results[1] = -0.5 * (d1 - pm) + q2;
            return results;
        } else if (pp >= 0) {
            pp = Math.sqrt(pp);
            double[] results = new double[2];
            results[0] = 0.5 * (d1 - pp) + q2;
            results[1] = 0.5 * (d1 + pp) + q2;
            return results;
        }
//        System.out.println(" null 3");
        return null;
    }
    public static double[] solveCubic(double a, double b, double c, double d) {
        a = 1.0/a;
        b *= a; c *= a; d *= a;
        double b2 = b * b;
        double D0 = (b2 - 3.0 * c) / 9.0;
        double D1 = (b * (b2 - 4.5 * c) + 13.5 * d) / 27.0;
        double D03 = D0 * D0 * D0;
        double D12 = D1 * D1;
        double D = D03 - D12;
        double add = b/3.0;
        if(D >= 0) {
            //three distinct real roots D > 0
            //three same real roots D = 0
            double roots[] = new double[3];
            D = D1 / Math.sqrt(D03);
            double theta = Math.acos(D) / 3.0;
            double sQ = -2.0 * Math.sqrt(D0);
            roots[0] = sQ * Math.cos(theta) - add;
            roots[1] = sQ * Math.cos(theta-2.0*Math.PI/3.0) - add;
            roots[2] = sQ * Math.cos(theta-4.0*Math.PI/3.0) - add;            
            return roots;
        }
        else {
            //1 real root
            double sQ = Math.pow(Math.sqrt(D12 - D03) + Math.abs(D1), 1.0 / 3.0);
            if (D1 < 0) {
                return new double[]{(sQ + D0 / sQ) - add};
            } else {
                return new double[]{-(sQ + D0 / sQ) - add};
            }
        }
    }
    /**
     * Return only one root for the specified cubic equation. This routine is
     * only meant to be called by the quartic solver. It assumes the cubic is of
     * the form: x^3+px^2+qx+r.
     *     
* @param p
     * @param q
     * @param r
     * @return
     */
    private static double solveCubicForQuartic(double b, double c, double d) {
        double b2 = b * b;
        double D0 = (b2 - 3.0 * c) / 9.0;
        double D1 = (b * (b2 - 4.5 * c) + 13.5 * d) / 27.0;
        double D03 = D0 * D0 * D0;
        double D12 = D1 * D1;
        double D = D03 - D12;
        double add = b / 3.0;
        if (D >= 0) {
            D = D1 / Math.sqrt(D03);
            double theta = Math.acos(D) / 3.0;
            double sQ = -2.0 * Math.sqrt(D0);
            return sQ * Math.cos(theta) - add;
        } else {
            double sQ = Math.pow(Math.sqrt(D12 - D03) + Math.abs(D1), 1.0 / 3.0);
            if (D1 < 0) {
                return (sQ + D0 / sQ) - add;
            } else {
                return -(sQ + D0 / sQ) - add;
            }
        }
    }
    public static String toString(double... coeff) {
        if(coeff.length == 0) return "0";
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < coeff.length-1; i++) {
            sb.append(coeff[i]).append("x^").append(coeff.length-1-i);
            if(coeff[i+1]>=0) sb.append('+');
        }
        sb.append(coeff[coeff.length-1]);
        return sb.toString();
    }
}