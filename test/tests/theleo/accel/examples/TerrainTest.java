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
package tests.theleo.accel.examples;

import com.jme3.font.BitmapText;
import tests.theleo.accel.Terrains;
import theleo.accel.Accelerated;
import theleo.accel.ImpactListener;
import theleo.accel.Results;
import theleo.accel.jme3.A;
import theleo.accel.jme3.AcceleratedDebug;
import theleo.accel.shapes.Shape;
import static theleo.accel.shapes.SphereShape.*;
import theleo.accel.shapes.SphereShape;
import theleo.accel.shapes.terrain.TerrainMap;
import theleo.accel.shapes.terrain.TerrainShape;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import java.util.Random;
import static com.jme3.math.FastMath.*;
import tests.theleo.accel.DebugUtils;
import tests.theleo.accel.SimpleTest;

/**
 * A test with a Terrain Shape.
 * 
 * @author Juraj Papp
 */
public class TerrainTest extends SimpleTest {
    static {
        instance = new TerrainTest();
        controls = false;
    }
    Char ch;
    Terrains terrain;
    TerrainShape terrainShape;
    Accelerated accel;
    
    @Override
    public void simpleInitApp() {
        super.simpleInitApp(); 
        attachCoordinateAxes(Vector3f.ZERO);
        setPauseOnLostFocus(false);
        cam.setLocation(new Vector3f(-2.646672f, 4.840615f, 3.1731033f));
        cam.lookAtDirection(new Vector3f(0.8071289f, -0.5866779f, 0.065969706f), Vector3f.UNIT_Y);
        initTerrain();
        terrainShape = new TerrainShape(new TerrainMap(terrain.height, terrain.terrainSize));
        
        ch = new Char();
        
        key(KeyInput.KEY_T, new ActionListener() {
            public void onAction(String name, boolean isPressed, float tpf) {
                walk = isPressed;
            }
        });
        key(KeyInput.KEY_F, new AnalogListener() {
            public void onAnalog(String name, float value, float tpf) {
                ch.rotation += tpf*0.7f;
            }
        });
        key(KeyInput.KEY_H, new AnalogListener() {
            public void onAnalog(String name, float value, float tpf) {
                ch.rotation -= tpf*0.7f;
            }
        });
        accel = new Accelerated();
        accel.setImpactListener(new ImpactListener() {
            @Override
            public void onLimit(Shape s) {
                s.vel(0, 0, 0);
                s.acc(0, 0, 0);
                ch.walking = false;
            }
            @Override
            public void collisionEnter(Shape s1, Shape s2) {
            }
            @Override
            public void collisionExit(Shape s1, Shape s2) {
            }
        });
        //ExcludeGroup is the default Group
        accel.groupListener = Accelerated.ExcludeGroup;
        //Shapes with the same groupId will not respond to collision with each other
        ch.shape.groupId = 1;
        ch.footLeft.groupId = 1;
        ch.footRight.groupId = 1;
        accel.add(ch.shape);
        accel.add(ch.footLeft);
        accel.add(ch.footRight);
        
        
        debug(ch.shape);
        debug(ch.footLeft);
        AcceleratedDebug.sphereColor = ColorRGBA.Cyan;
        debug(ch.footRight);
        
        vector(new Link<Vector3f>() {
            public Vector3f read() {
                return ch.gravityCenter();
            }
        }, new Link<Vector3f>() {
            Vector3f dir = new Vector3f();
            public Vector3f read() {
                dir.x = cos(ch.rotation);
                dir.z = -sin(ch.rotation);
                return dir;
            }
        });
        
        BitmapText text = new BitmapText(guiFont);
        text.setSize(guiFont.getCharSet().getRenderedSize());
        text.setText("Press T to walk, Hold F, H to turn\nWASD - move camera");
        text.setLocalTranslation(0, cam.getHeight(), 0);
        guiNode.attachChild(text);
    }
    
    public void step(SphereShape foot, SphereShape other, float vel) {
        if(ch.walking) return;
        ch.walking = true;
        
        ch.shape.apply();
        float x = cos(ch.rotation)*vel;
        float z = -sin(ch.rotation)*vel;
        
        foot.apply();
        foot.vel(x+x,  1f, z+z);
        foot.acc(0, -1f, 0);
        Results res = new Results();
        Accelerated.calcFor(foot, terrainShape, res);
        float time;
        if(!res.isEmpty() && (time = res.time[1]) != 0f) {
            
            foot.setLimit(time);
            ch.shape.setLimit(time);
            //contact found
            System.out.println("resss " + res + ", limit: " + time);
            
            
            float vely = (other.data[pos.y]+ch.legLength-ch.shape.data[pos.y])/time;
            
            ch.shape.vel(x, vely, z);
            
            accel.applyRecalc(ch.shape, foot);
        }
        else {
            foot.vel(0, 0, 0);
            foot.acc(0, 0, 0);
            accel.applyRecalc(foot);
        }
    }

    @Override
    public void action(int i) {
        switch(i) {
            case 0:
                ch.initFeet();
                break;
            case 1:
                
                step(ch.footLeft, ch.footRight, -.2f);
                break;
            case 2:
                step(ch.footRight, ch.footLeft,-.2f);
                break;
            case 3:
                step(ch.footLeft, ch.footRight, .2f);
                break;
            case 4:
                step(ch.footRight, ch.footLeft, .2f);
                break;
            case 5:
                ch.rotation = HALF_PI;
                step(ch.footLeft, ch.footRight, .1f);
                break;
            case 6:
                ch.rotation = HALF_PI;
                boolean back = ch.backFoot(ch.rotation);
                step(ch.getFoot(back), ch.getFoot(!back), .2f);
                break;
        }
    }
    
    public void debug(SphereShape s) {
        rootNode.attachChild(AcceleratedDebug.sphere(s, assetManager));
    }
    
    public void initTerrain() {
        float[] height = new float[9*9];
        Random r = new Random(1554);
        for(int i = 0; i < height.length; i++) height[i] = r.nextFloat();
        terrain = new Terrains(height, 8, 1);
        Node terrainNode = terrain.loadAll(assetManager);
        rootNode.attachChild(DebugUtils.showNormals(((Geometry)terrainNode.getChild(0)).getMesh(), assetManager));
        rootNode.attachChild(terrainNode);
        
    }
    boolean walk = false;
    @Override
    public void simpleUpdate(float tpf) {
        accel.update(tpf);
        if(walk) {
            boolean back = ch.backFoot(ch.rotation);
            step(ch.getFoot(back), ch.getFoot(!back), .2f);
        }
    }
    static class Char {
        Vector3f center = new Vector3f();
        
        //eg. multiple shapes for character
        SphereShape shape;
        
        boolean walking = false;
        
        //short would be enough
        float rotation = HALF_PI;
        
        //distance from feet to center of gravity
        float legLength = 1f;
        //half the distance of leg apart from each other
        float feetWidth = 0.1f;
        
        
        //center of gravity, rotation
        //mass, velocity
        //location of feet
        
        SphereShape footLeft, footRight;

        public Char() {
            
            shape = new SphereShape(0.5f);
            shape.pos(3, 1.5f+0.2f, 4-0.1f);
            
            footLeft = new SphereShape(0);
            footRight = new SphereShape(0);
            initFeet();
            
        }
        
        public Vector3f gravityCenter() {
            A.glPosNow(shape, center);
            return center;
        }
        
        /**
         * position feet at place
         */
        public void initFeet() {
            
            Vector3f center = gravityCenter();
            footLeft.pos(center.x-feetWidth, center.y-legLength, center.z);
            footRight.pos(center.x+feetWidth, center.y-legLength, center.z);
            footLeft.vel(0, 0, 0);
            footLeft.acc(0, 0, 0);
            footRight.vel(0, 0, 0);
            footRight.acc(0, 0, 0);
            
        }
        public SphereShape getFoot(boolean right) {
            return right?footRight:footLeft;
        }
        /**
         * 
         * @param direction to move, 0 to move (1,0), half pi (0,1)
         * @return true if right foot is back foot
         */
        public boolean backFoot(float direction) {
            footRight.apply();
            footLeft.apply();
            
            direction -= HALF_PI;
            float x2 = cos(direction);
            float y2 = -sin(direction);
            Vector3f b = gravityCenter();
            float right = (footRight.data[pos.x] - b.x)*y2 - (footRight.data[pos.z] - b.z)*x2;
            float left = (footLeft.data[pos.x] - b.x)*y2 - (footLeft.data[pos.z] - b.z)*x2;
            return right < left;
        }
    }
}
