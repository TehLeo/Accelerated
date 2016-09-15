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
package theleo.accel.jme3;

import theleo.accel.Accelerated;
import theleo.accel.shapes.LineShape;

import theleo.accel.shapes.MeshShape;
import theleo.accel.shapes.Shape;
import com.jme3.math.Vector3f;
import static theleo.accel.shapes.SphereShape.*;
import static theleo.accel.shapes.LineShape.*;
import theleo.accel.util.Vars;
import theleo.accel.util.Vec;
import com.jme3.math.FastMath;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.HashSet;

/**
 * Utility class for Jme3. 
 * 
 * @author Juraj Papp
 */
public class A {
    public static Accelerated accel;
    
    /**
     * @param m - mesh with short index buffer, contains triangles
     */
    public static MeshShape meshShape(Mesh m) {
        ShortBuffer vb = (ShortBuffer) m.getBuffer(VertexBuffer.Type.Index).getData();
        HashSet<Integer> set = new HashSet<Integer>();
        
        short a,b;
        int res;
        for(int i = 0; i < vb.limit(); i+=3) {
            a = vb.get(i+2);
            for(int j = 0; j < 3; j++) {
                b = vb.get(i+j);
                res = (a>b)?((((int)b)<<16)|(a&0xFFFF)):((((int)a)<<16)|(b&0xFFFF));
                set.add(res);
                a = b;
            }
        }
        
        int vc = m.getVertexCount();
        int tc = m.getTriangleCount();
        
        FloatBuffer posBuf = (FloatBuffer) m.getBuffer(VertexBuffer.Type.Position).getData();
        
        MeshShape mesh = new MeshShape(tc, tc+tc*3,vc, set.size());
        for(int i = 0; i < posBuf.limit(); i++)
            mesh.verts[i] = posBuf.get(i);
        
        double mx = 0, my = 0, mz = 0;
        for(int i = 0; i < mesh.verts.length; i+=3) {
            mx += mesh.verts[i];
            my += mesh.verts[i+1];
            mz += mesh.verts[i+2];
        }
        float verts = 1f/(mesh.verts.length/3);
        mx *= verts; my *= verts; mz *= verts;
        float r,maxRad=0;
        for(int i = 0; i < mesh.verts.length; i+=3) {
            mesh.verts[i]   -= mx;
            mesh.verts[i+1] -= my;
            mesh.verts[i+2] -= mz;
            r = mesh.verts[i]*mesh.verts[i]+
                    mesh.verts[i+1]*mesh.verts[i+1]+
                    mesh.verts[i+2]*mesh.verts[i+2];
            if(r > maxRad) maxRad = r;
        }
        mesh.pos((float)mx, (float)my, (float)mz);
        mesh.data[rad] = FastMath.sqrt(maxRad);
        
  
        Vector3f pa = new Vector3f();
        Vector3f pb = new Vector3f();
        Vector3f pc = new Vector3f();
        Vector3f pab = new Vector3f();
        Vector3f pac = new Vector3f();
        int fi = 0;
        int ii = 0;
        int k;
        for(int i = 0; i < vb.limit(); i+=3) {
            mesh.index[ii++] = 3;
            for(int j = 0; j < 3; j++)
                mesh.index[ii++] = vb.get(i+j);
            
            k = 3*vb.get(i);
            pa.set(mesh.verts[k],mesh.verts[k+1],mesh.verts[k+2]);
            k = 3*vb.get(i+1);
            pb.set(mesh.verts[k],mesh.verts[k+1],mesh.verts[k+2]);
            k = 3*vb.get(i+2);
            pc.set(mesh.verts[k],mesh.verts[k+1],mesh.verts[k+2]);
            
            //todo: hmm eg use normals if possibles
            pab = pab.set(pb).subtractLocal(pa);
            pac = pac.set(pc).subtractLocal(pa);
            pab.crossLocal(pac).normalizeLocal();
            float d = -pab.dot(pa);
            
            mesh.faces[fi++] = pab.x;
            mesh.faces[fi++] = pab.y;
            mesh.faces[fi++] = pab.z;
            mesh.faces[fi++] = d;
        }
        ii = 0;
        for(int e : set) {
            mesh.edges[ii++] = (short)(e>>16);
            mesh.edges[ii++] = (short)(e&0xFFFF);
        }
        
        return mesh;
    }
    
    public static void glPosAt(float d[], float time, Vector3f store) {
        Vars vars = Vars.get();
        final float tt = time*time*0.5f;
        store.set(d[pos.x]+d[vel.x]*time+d[acc.x]*tt,
                  d[pos.y]+d[vel.y]*time+d[acc.y]*tt,
                  d[pos.z]+d[vel.z]*time+d[acc.z]*tt);
        vars.release();
    }
    public static void glPosAt(Shape s, float time, Vector3f store) {
        Vars vars = Vars.get();
        final float tt = time*time*0.5f;
        final float[] d = s.getData(vars.farr1);
        store.set(d[pos.x]+d[vel.x]*time+d[acc.x]*tt,
                  d[pos.y]+d[vel.y]*time+d[acc.y]*tt,
                  d[pos.z]+d[vel.z]*time+d[acc.z]*tt);
        vars.release();
    }
    public static void glPosNow(Shape s, Vector3f store) {
        glPosAt(s, s.travelled, store);
    }
    
    public static void posAt(Shape s, float time, Vector3f store) {
        final float tt = time*time*0.5f;
        final float[] d = s.data;
        store.set(d[pos.x]+d[vel.x]*time+d[acc.x]*tt,
                  d[pos.y]+d[vel.y]*time+d[acc.y]*tt,
                  d[pos.z]+d[vel.z]*time+d[acc.z]*tt);
    }
    public static void posNow(Shape s, Vector3f store) {
        posAt(s, s.travelled, store);
    }
    public static void velAt(Shape s, float time, Vector3f store) {
        final float[] d = s.data;
        store.set(d[vel.x]+d[acc.x]*time,
                  d[vel.y]+d[acc.y]*time,
                  d[vel.z]+d[acc.z]*time);
    }
    public static void velNow(Shape s, Vector3f store) {
        velAt(s, s.travelled, store);
    }
    public static void dir1(LineShape l, Vector3f store) {
        store.set(l.data[dir1.x], l.data[dir1.y], l.data[dir1.z]);
    }
    public static void dir2(LineShape l, Vector3f store) {
        store.set(l.data[dir2.x], l.data[dir2.y], l.data[dir2.z]);
    }
    public static void normal(LineShape l, Vector3f store) {
        dir1(l, store);
        store.crossLocal(l.data[dir2.x], l.data[dir2.y], l.data[dir2.z]);
    }
    public static float angle(LineShape l) {
        final float[] d = l.data;
        float dot = d[dir1.x]*d[dir2.x]+d[dir1.y]*d[dir2.y]+d[dir1.z]*d[dir2.z];
        //is direction normalized?
        dot = dot / FastMath.sqrt(Vec.lenSq(l, DIR1)*Vec.lenSq(l, DIR2));
        return FastMath.acos(dot);
    }
    public static void pos(Shape s, Vector3f store) {
        store.set(s.data[pos.x], s.data[pos.y], s.data[pos.z]);
    }
    public static void vel(Shape s, Vector3f store) {
        store.set(s.data[vel.x], s.data[vel.y], s.data[vel.z]);
    }
    public static void acc(Shape s, Vector3f store) {
        store.set(s.data[acc.x], s.data[acc.y], s.data[acc.z]);
    }
    public static void setPos(Shape s, Vector3f p) {
        s.data[pos.x] = p.x; s.data[pos.y] = p.y; s.data[pos.z] = p.z;
    }
    public static void setVel(Shape s, Vector3f v) {
        s.data[vel.x] = v.x; s.data[vel.y] = v.y; s.data[vel.z] = v.z;
    }
    public static void setAcc(Shape s, Vector3f a) {
        s.data[acc.x] = a.x; s.data[acc.y] = a.y; s.data[acc.z] = a.z;
    }
    public static String pos(Shape s) {
        return toString(s.data, POS, 3);
    }
    public static String vel(Shape s) {
        return toString(s.data, VEL, 3);
    }
    public static String acc(Shape s) {
        return toString(s.data, ACC, 3);
    }
    public static void getDirAt(LineShape shape, float time, Vector3f store) {
        for(int i = 0; i < 3; i++)
            store.set(i, shape.data[DIR1+i] + (shape.data[DIR2+i]-shape.data[DIR1+i])*shape.data[15]*(time+shape.data[16]));
    }
    public static String toString(float[] data, int pos, int len) {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for(int i = 0; i < len; i++) sb.append(data[pos+i]).append(',');
        sb.setCharAt(sb.length()-1, ')');
        return sb.toString();
    }
}
