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
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;

/**
 * 
 * @author Juraj Papp
 */
public class Terrains {
    public float height[];
    public int terrainSize, numInRow;
    public Terrains(float[] hm, int tSize, int num) {
        height = hm;
        terrainSize = tSize;
        numInRow = num;
    }
    public Node loadAll(AssetManager am) {
        final Vector3f light = new Vector3f(1,1,0).normalizeLocal();
        Node n = new Node("Terrains");
        int t2 = terrainSize*terrainSize;
        for(int y =0;y<numInRow;y++)
            for(int x=0;x<numInRow;x++){
                Terrain t = new Terrain("t", terrainSize, 
                        height, (x+y*numInRow)*t2,
                        (x+1==numInRow)?null:height, (x+1+y*numInRow)*t2, 
                        (y+1==numInRow)?null:height, (x+(y+1)*numInRow)*t2, 
                        ((x+1==numInRow||y+1==numInRow)?0f:height[(x+1+(y+1)*numInRow)*t2]), am);
                t.setMaterial(am);
                t.getMesh().setBuffer(VertexBuffer.Type.Normal, 3, ShadeUtils.computeNormals(t.getMesh()));
                ShadeUtils.dirLight(t.getMesh(), light, 0.5f, 1.20f);
                t.setLocalTranslation(x*terrainSize, 0, y*terrainSize);
                n.attachChild(t);
            }
        return n;
    }

    public float getInterpolatedHeight(float x, float z) {
        int t = terrainSize*numInRow;
        int gx = (int)(x);
        int gz = (int)(z);
        if(gx < 0 || gx >= t || gz < 0 || gz >= t)
            return 0f; // or nan
        int offset = (((int)(x/t))+((int)(z/t))*numInRow)*terrainSize*terrainSize;
        return height[offset+gx+gz*t];
    }
    
    
}
