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
package tests.theleo.accel;

import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Shade utils.
 * 
 * @author Juraj Papp
 */
public class ShadeUtils {
    public static void cloneBuffer(Mesh m, VertexBuffer.Type type) {
        VertexBuffer vb = m.getBuffer(type).clone();
        m.clearBuffer(type);
        m.setBuffer(vb);
    }
    public static FloatBuffer computeNormals(Mesh m) {
        return computeNormals(m.getFloatBuffer(VertexBuffer.Type.Position),
                (ShortBuffer)m.getBuffer(VertexBuffer.Type.Index).getData());
    }
            
    public static FloatBuffer computeNormals(FloatBuffer pos, ShortBuffer idx) {
        Vector3f v1 = new Vector3f();
        Vector3f v2 = new Vector3f();
        
        FloatBuffer n= BufferUtils.createFloatBuffer(pos.limit());
        n.limit(n.capacity());
        n.rewind();
        idx.rewind();
        short a,b,c;
        while(idx.hasRemaining()) {
            a = (short) (3*idx.get()); b = (short) (3*idx.get()); c = (short) (3*idx.get());
            v1.x = -pos.get(a);
            v1.y = -pos.get(a+1);
            v1.z = -pos.get(a+2);
            v2.x = pos.get(c)+v1.x;
            v2.y = pos.get(c+1)+v1.y;
            v2.z = pos.get(c+2)+v1.z;
            v1.addLocal(pos.get(b), pos.get(b+1), pos.get(b+2));
            v1.crossLocal(v2);//.normalizeLocal();
            
            n.put(a, n.get(a)+v1.x);
            n.put(a+1, n.get(a+1)+v1.y);
            n.put(a+2, n.get(a+2)+v1.z);
            
            n.put(b, n.get(b)+v1.x);
            n.put(b+1, n.get(b+1)+v1.y);
            n.put(b+2, n.get(b+2)+v1.z);
            
            n.put(c, n.get(c)+v1.x);
            n.put(c+1, n.get(c+1)+v1.y);
            n.put(c+2, n.get(c+2)+v1.z);
        }
        n.rewind();
        while(n.hasRemaining()) {
            v1.set(n.get(n.position()), n.get(n.position()+1), n.get(n.position()+2));
//            System.err.println("normal " + v1);

            v1.normalizeLocal();

            n.put(v1.x).put(v1.y).put(v1.z);
        }
        return n;
    }
    public static void dirLight(Mesh mesh, Vector3f dir, float minI, float intensity) {
        VertexBuffer color = mesh.getBuffer(VertexBuffer.Type.Color);
        if(color.getData() instanceof FloatBuffer) {
            throw new IllegalArgumentException();
        }
        else 
            dirLight(mesh.getFloatBuffer(VertexBuffer.Type.Normal),
                (ByteBuffer)color.getData(), color.getNumComponents(), dir, minI, intensity);
    }
    public static void dirLight(FloatBuffer normal, FloatBuffer color, final int colorCompNum, Vector3f dir, float minI, float intensity) {
        //assume 3 components per col
        Vector3f v1 = new Vector3f();
        color.rewind();
        normal.rewind();
        float i, j;
        while(color.hasRemaining()) {
            v1.set(normal.get(), normal.get(), normal.get());
            i = v1.dot(dir);
            if(i < minI) i = minI;
            for(j = 0; j < 3; j++)
                color.put(i*color.get(color.position()));
            if(colorCompNum == 4) color.get();
        }
    }
    private static final int threshold = 255;
    private static final int threshold3 = 3*threshold;
    public static void dirLight(FloatBuffer normal, ByteBuffer color, final int colorCompNum, Vector3f dir, float minI, float intensity) {               
        Vector3f v1 = new Vector3f();
        color.rewind();
        normal.rewind();
        int r,g,b;
        float i, j;
        while(color.hasRemaining()) {
            v1.set(normal.get(), normal.get(), normal.get());
            i = v1.dot(dir);
            if(i < minI) i = minI;
            i *= intensity;
//                  color.put(clmp((int)(i*color.get(color.position()))));
                b = color.position();
                r = (int) (i*(color.get(b)&0xff));
                g = (int) (i*(color.get(b+1)&0xff));
                b = (int) (i*(color.get(b+2)&0xff));
                
                int m = r>g?(r>b?r:b):(g>b?g:b);
                if (m <= threshold)
                    color.put((byte)r).put((byte)g).put((byte)b);
                else {
                    int total = r + g + b;
                    if (total >= threshold3) 
                        color.put((byte)-1).put((byte)-1).put((byte)-1);
                    else {
                        int x = (threshold3 - total) / (3 * m - total);
                        int gray = threshold - x * m;
                        color.put((byte)(gray+x*r)).put((byte)(gray+x*g)).put((byte)(gray+x*b));
                    }
                }
//                return int(gray + x * r), int(gray + x * g), int(gray + x * b)
            if(colorCompNum == 4)color.get();
        }
    }
}
