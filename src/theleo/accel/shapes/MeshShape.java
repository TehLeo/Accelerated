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
import theleo.accel.util.Vars;
import static theleo.accel.shapes.SphereShape.*;
import theleo.accel.util.Polynomials;
import theleo.accel.util.Utils;
import theleo.accel.util.Vec3f;

/**
 * Experimental. Not yet ready for use.
 * 
 * @author Juraj Papp
 */
public class MeshShape extends Shape {
    public static final byte CTYPE_FACE = 1, CTYPE_EDGE = 2, CTYPE_VERTEX = 3;
    
    /**
     * number of points n,
     * n-face indices,
     */
    public short[] index;
    /**
     * pairs of vertex indices, all unique edges
     */
    public short[] edges;
    /**
     * faces, (a,b,c,d) where ax + by + cz + d = 0 is
     * the plane equation for this face
     * index specifies the number of face vertices and the vertex indices
     */
    public float[] faces;
    /**
     * mesh vertices, (x,y,z) points
     */
    public float[] verts;
    
    public byte collisionType;
    public short collisionIndex;
    
    public MeshShape() {}
    
    public MeshShape(MeshShape m) {
        super(10);
        for(int i = 0; i < 10; i++) data[i] = m.data[i];
        faces = m.faces;
        index = m.index;
        verts = m.verts;
        edges = m.edges;
    }
    
    /**
     * first 10: 3pos,3vel,3acc,bounding sphere radius
     * 
     * numOfFaces
     * indexLen = numOfFaces+numOfEdgesUsed
     * numOfEdges = numOfUniqueEdges
     */
    public MeshShape(int numOfFaces, int indexLen, int numOfVertex, int numOfEdges) {
        super(10);
        faces = new float[numOfFaces*4];
        index = new short[indexLen];
        verts = new float[numOfVertex*3];
        edges = new short[numOfEdges+numOfEdges];
    }

    @Override
    public void setFlags(int[] flags, int len) {
        collisionType = (byte) flags[0];
        collisionIndex = (short) flags[1];
    }
    
    /**
     * works fine for convex so far
     * needs to output all times for concave along with
     * the flags
     */
    public static void calcForMeshSphere(MeshShape m, SphereShape s, Results store) {
        final Vars vars = Vars.get();
        
        final float[] d = vars.farr1;
        final float[] a = s.getData(vars.farr2), b = m.getData(vars.farr3);
        for(int i = 0; i < 9; i++) d[i] = a[i]-b[i];
        d[rad] = a[rad];
                
        //check if spheres overlap now
        boolean check = Collisions.intersectsSphere(s, m);
        if(!check) {
            Collisions.calcForSpheres(d, a[rad]+b[rad], store);
            if(!store.isEmpty()) {
                for(int i = 0; i < store.len; i++) {
                    if(store.time[i] >= 0) {
                        check = true;
                        break;
                    }
                }
                store.reset();
            }
        }
        if(check) {
            float min = Float.NaN;
            float max = Float.NaN;
            
            Vec3f inter = new Vec3f(vars.farr5);
            Vec3f v1 = new Vec3f(vars.farr5, 3);
            Vec3f v2 = new Vec3f(vars.farr5, 6);
            Vec3f pdir = new Vec3f(vars.farr5, 9);
            Vec3f tmp = new Vec3f(vars.farr5, 12);
            Vec3f dir = new Vec3f(vars.farr5, 15);
            
            final float[] faces = m.faces;
            final short[] index = m.index;
            final float[] verts = m.verts;
            final short[] edges = m.edges;
            
            int k = 0;
            int ik = -1;
            
            byte cType = 0;
            short cIndex = 0;
  
            //for each face
            for(int i = 0; i < faces.length; i += 4) {
                calcForSpherePlane(d, faces[i], faces[i+1], faces[i+2], faces[i+3], store);
                
                k += ik+1;
                ik = index[k];
                
                 
                
                if(!store.isEmpty()) {
                    //for each contact time check if it is inside face
                    float time = Float.NaN;
                    time:
                    for(int l = 0; l < store.len; l++) {
                        if(time == store.time[l]) continue;
                        time = store.time[l];
                        
                       
                        
                        if(Accelerated.REC_TOLERANCE > time) continue;
                        
                        Utils.glPosAt(d, time, inter);
                                               
                        int last = 3*index[k+ik];
                        v1.set(verts[last], verts[last+1], verts[last+2]);
                        //for each face edge
                        for(int j = 0; j < ik; j++) {
                            last = 3*index[k+j+1];
                            v2.set(verts[last], verts[last+1], verts[last+2]);
                            pdir.set(v2).sub(v1).cross(faces[i], faces[i+1], faces[i+2]);
                            float side = pdir.dot(tmp.set(inter).sub(v1));
                            if(side > 0) continue time;
                            v1.set(v2);
                        }
                        //intersection found inside plane at time
                        if(min!=min) {
                            min = max = time;
                            
                            cType = 1;
                            cIndex = (short) i;
                        }
                        else {

                            if(time < min) {
                                min = time;
                                cType = 1;
                                cIndex = (short) i;
                            }
                            if(time > max) max = time;
                        }
                    }
                    store.reset();
                }
            }
            float length;
            //for each unique edge
            for(int i = 0; i < edges.length; i+=2) {
                int p = 3*edges[i];
                v1.set(verts[p], verts[p+1], verts[p+2]);
                p = 3*edges[i+1];
                v2.set(verts[p], verts[p+1], verts[p+2]);
                //now calculate line intersection
                Collisions.calcForLineSphere(d, v1, v2, store);
                if(!store.isEmpty()) {
                    //for each time
                    dir.set(v2).sub(v1);
                    length = dir.length();
                    if(length == 0f) continue;
                    dir.mult(1f/length);
                    float time = Float.NaN;
                    for(int l = 0; l < store.len; l++) {
                        if(time == store.time[l]) continue;
                        time = store.time[l];
                        if(Accelerated.REC_TOLERANCE > time) continue;
                        
                        
                        Utils.glPosAt(d, time, inter);
                        inter.sub(v1);
                        float proj = inter.dot(dir);
                        if(proj >= 0f && proj <= length) {
                            if(min!=min) {
                                min = max = time;
                                cType = 2;
                                cIndex = (short) i;
                            }
                            else {

                                if(time < min) {
                                    min = time;
                                    cType = 2;
                                    cIndex = (short) i;
                                }
                                if(time > max) max = time;
                            }
                        }
                    }
                    store.reset();
                }
            }
            final float[] dc = vars.farr4;
            for(int i = 3; i < 9; i++) dc[i] = d[i];
            dc[rad] = a[rad];
            //for each unique vertex
            for(int i = 0; i < verts.length; i+=3) {
                for(int j = pos.x; j <= pos.z; j++)
                    dc[j] = d[j]-verts[i+j];
                Collisions.calcForSpheres(dc, dc[rad], store);
                if(!store.isEmpty()) {
                    float time = Float.NaN;
                    for(int l = 0; l < store.len; l++) {
                        if(time == store.time[l]) continue;
                        time = store.time[l];
                        
                       if(Accelerated.REC_TOLERANCE > time) continue;
                        
                        if(min!=min)  {
                            min = max = time;
                            cType = 3;
                            cIndex = (short) i;
                        }else {

                            if(time < min) {
                                min = time;
                                cType = 3;
                                cIndex = (short) i;
                            }
                            if(time > max) max = time;
                        }
                    }
                    store.reset();
                }
            }
            if(min==min) {
                store.set(min, max);
                store.flags[0] = cType;
                store.flags[1] = cIndex;
                store.flagsLen = 2;
            }
        }
        vars.release();
    }
     public static void calcForSpherePlane(float[] data, float a, float b, float c, float d, Results store) {
        float q = 0.5f*(a*data[acc.x]+b*data[acc.y]+c*data[acc.z]);
        float r = a*data[vel.x]+b*data[vel.y]+c*data[vel.z];
 
        if(q == 0f) {
            if(r == 0) return;
            float[] time = store.time;
            final float abcRad = /*abcSq* */data[rad];
            q = -a*data[pos.x]-b*data[pos.y]-c*data[pos.z]-d;
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
            final float term = a*data[pos.x]+b*data[pos.y]+c*data[pos.z]+d;
            final float abcRad = /*abcSq* */data[rad];
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

    @Override
    public void calcTime(Shape s, Results store) {
        switch(s.getType()) {
            case 0: //SphereShape
                calcForMeshSphere(this, (SphereShape)s, store);
                return;
            case 1: //Line
                    
                return;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public short getType() {
        return 5;
    }
}
