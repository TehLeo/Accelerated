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

package theleo.accel.shapes.terrain;

/**
 * Utility class that implements TerrainLink interface.
 * 
 * @author Juraj Papp
 */
public class TerrainMap implements TerrainLink {
    public float[] array;
    public int terrainSize;
    public float cellSize, offsetX, offsetY;
    public TerrainMap() {}
    public TerrainMap(float[] array, int terrainSize) {
        this(array, terrainSize, 1f, 0f, 0f);
    }
    public TerrainMap(float[] array, int terrainSize, float cellSize, float offsetX, float offsetY) {
        this.array = array;
        this.terrainSize = terrainSize;
        this.cellSize = cellSize;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }
    @Override
    public float getHeight(int x, int z) {
        return array[x+z*terrainSize];
    }

    @Override
    public float getCellSize() {
        return cellSize;
    }

    @Override
    public int getSize() {
        return terrainSize;
    }

    @Override
    public float getXOffset() {
        return offsetX;
    }

    @Override
    public float getZOffset() {
        return offsetY;
    }
    
}
