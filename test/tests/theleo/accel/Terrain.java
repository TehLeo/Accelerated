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

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;

/**
 * 
 * @author Juraj Papp
 */
public class Terrain extends Geometry {

    public int size;

    public Terrain() { }

    /**
     * 
     * @param name name of the terrain
     * @param size is power of two, size=2^n where n>0
     * @param height height map data, offset+(size+1)^2 &lt;= height.length elements 
     * @param offset offset where height map data begins in height array
     * @param am 
     */
    public Terrain(String name, int size, float[] height, int offset, AssetManager am) {
        super(name);
        setBatchHint(BatchHint.Never);
        this.size = size;
        System.err.println("offset "+ offset);

        Mesh m = new Mesh();

        short[] index = new short[6 * size * size];
        float[] pos = new float[3 * (size + 1) * (size + 1)];

        int i = 0;
        
        for (int y = 0; y < size + 1; y++) {
            for (int x = 0; x < size + 1; x++){
                pos[i++] = x;
                pos[i++] = height[offset+x+y*size];//FastMath.nextRandomFloat() *0.5f;
                pos[i++] = y;
            }
        }
        m.setBuffer(VertexBuffer.Type.Position, 3, pos);
        i = 0;
        int j = 0;
        for (short y = 0; y < size; y++, j += size + 1) {
            for (short x = 0; x < size; x++) {
                index[i++] = (short) (j + x);
                index[i++] = (short) (j + x + size + 1);
                index[i++] = (short) (j + x + 1);

                index[i++] = (short) (j + x + size + 2);
                index[i++] = (short) (j + x + 1);
                index[i++] = (short) (j + x + size + 1);
            }
        }

        m.setBuffer(VertexBuffer.Type.Index, 3, index);
        //new ColorRGBA(251f / 255f, 130f / 255f, 0f, 1f);
////        System.err.println("color orange " + ColorRGBA.Orange);
//        ColorRGBA c = //new ColorRGBA(251f / 255f, 130f / 255f, 0f, 1f).multLocal(0.5f);
//                new ColorRGBA(239f / 255f, 237f / 255f, 154f / 255f, 1f);
            
        byte[] vColor = new byte[4 * (size + 1) * (size + 1)];
        i = 0;
        j = 1;
        int col;
        while(i < vColor.length) {
            //ColorRGBA.randomColor();
//             vColor[i++] = (byte)(c.r*255);
//             vColor[i++] = (byte)(c.g*255);
//             vColor[i++] = (byte)(c.b*255);
            col = (int) (pos[j]+100); j += 3;
            if(col > 255) col = 255;
            vColor[i++] = (byte) col;
             vColor[i++] = (byte) col;
             vColor[i++] = (byte) col;
//            vColor[i++] = c.r*0.8f;
//            vColor[i++] = c.g*0.8f;
//            vColor[i++] = 0.5f;
            vColor[i++] = (byte)-1;
        }
        m.setBuffer(VertexBuffer.Type.Color, 4, vColor);
        m.getBuffer(VertexBuffer.Type.Color).setNormalized(true);

        setMesh(m);
        updateModelBound();
    }
    public Terrain(String name, int size, float[] height, int offset,
            float[] right, int rOff,
            float[] bottom, int bOff, 
            float corner, AssetManager am) {
        super(name);
        setBatchHint(BatchHint.Never);
        this.size = size;
        System.err.println("offset "+ offset);

        Mesh m = new Mesh();

        short[] index = new short[6 * size * size];
        float[] pos = new float[3 * (size + 1) * (size + 1)];

        int i = 0;
        
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++){
                pos[i++] = x;
                pos[i++] = height[offset+x+y*size];//FastMath.nextRandomFloat() *0.5f;
                pos[i++] = y;
            }
            pos[i++] = size;
            pos[i++] = (right==null)?0:right[rOff+y*size];//FastMath.nextRandomFloat() *0.5f;
            pos[i++] = y;
        }
        for (int x = 0; x < size; x++){
                pos[i++] = x;
                pos[i++] = bottom==null?0:bottom[bOff+x];//FastMath.nextRandomFloat() *0.5f;
                pos[i++] = size;
        }
        pos[i++] = size;
        pos[i++] = corner;//FastMath.nextRandomFloat() *0.5f;
        pos[i++] = size;
        m.setBuffer(VertexBuffer.Type.Position, 3, pos);
        i = 0;
        int j = 0;
        for (short y = 0; y < size; y++, j += size + 1) {
            for (short x = 0; x < size; x++) {
                index[i++] = (short) (j + x);
                index[i++] = (short) (j + x + size + 1);
                index[i++] = (short) (j + x + 1);

                index[i++] = (short) (j + x + size + 2);
                index[i++] = (short) (j + x + 1);
                index[i++] = (short) (j + x + size + 1);
            }
        }

        m.setBuffer(VertexBuffer.Type.Index, 3, index);
        //new ColorRGBA(251f / 255f, 130f / 255f, 0f, 1f);
////        System.err.println("color orange " + ColorRGBA.Orange);
        ColorRGBA c = //new ColorRGBA(251f / 255f, 130f / 255f, 0f, 1f).multLocal(0.5f);
                new ColorRGBA(239f / 255f, 237f / 255f, 154f / 255f, 1f);
            
        byte[] vColor = new byte[4 * (size + 1) * (size + 1)];
        i = 0;
        j = 1;
//        int col;
        while(i < vColor.length) {
            //ColorRGBA.randomColor();
             vColor[i++] = (byte)(c.r*255);
             vColor[i++] = (byte)(c.g*255);
             vColor[i++] = (byte)(c.b*255);
//            col = (int) (pos[j]+100); j += 3;
//            if(col > 255) col = 255;
//            vColor[i++] = (byte) col;
//             vColor[i++] = (byte) col;
//             vColor[i++] = (byte) col;
//            vColor[i++] = c.r*0.8f;
//            vColor[i++] = c.g*0.8f;
//            vColor[i++] = 0.5f;
            vColor[i++] = (byte)-1;
        }
        m.setBuffer(VertexBuffer.Type.Color, 4, vColor);
        m.getBuffer(VertexBuffer.Type.Color).setNormalized(true);

        setMesh(m);
        updateModelBound();
    }
     
    public void setMaterial(AssetManager am) {
        Material m = new Material(am, "TestData/Common/ColorVert.j3md");
        setMaterial(m);
    }
    public void setMaterial(AssetManager am, ColorRGBA col) {
        Material m = new Material(am, "TestData/Terrain/Terrain.j3md");
        m.setColor("Color", col);
        setMaterial(m);
    }
}
