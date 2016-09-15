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

import com.jme3.math.ColorRGBA;
import theleo.accel.ImpactListener;
import theleo.accel.jme3.A;
import theleo.accel.jme3.AcceleratedDebug;
import theleo.accel.shapes.Shape;
import theleo.accel.shapes.SphereShape;
import tests.theleo.accel.SimpleTest;
import static theleo.accel.shapes.SphereShape.*;

import com.jme3.math.Vector3f;
import theleo.accel.Accelerated;

/**
 * Example showing how to initialize the system and add few sphere shapes.
 * 
 * @author Juraj Papp
 */
public class Example1 extends SimpleTest {
    static {
        instance = new Example1();
    }
    @Override
    public void simpleInitApp() {
        super.simpleInitApp();
               
        //Create new instance
        //A.accel = new Accelerated();
        //Use AccelereatedDebug instead to display debug shapes
        A.accel = new AcceleratedDebug(rootNode, assetManager);
        A.accel.setImpactListener(new ImpactListener() {
            Vector3f tvel = new Vector3f();
            @Override
            public void onLimit(Shape s) {
                //Stop shape on limit
                s.vel(0, 0, 0);
                s.acc(0, 0, 0);
            }
            @Override
            public void collisionEnter(Shape sh1, Shape sh2) {
                SphereShape s1 = (SphereShape)sh1;
                SphereShape s2 = (SphereShape)sh2;
                    
                final float[] d2 = s2.data;

                Vector3f normal = new Vector3f();
                A.pos(s1, normal);
                normal.subtractLocal(d2[pos.x], d2[pos.y], d2[pos.z]);
                normal.normalizeLocal();

                Vector3f tmp = new Vector3f();
                A.vel(s1, tmp);
                tmp.subtractLocal(d2[vel.x], d2[vel.y], d2[vel.z]); 
                float c = normal.dot(tmp);

                tvel.set(c*normal.x, c*normal.y, c*normal.z);
                A.vel(s1, tmp); tmp.subtractLocal(tvel);
                A.setVel(s1, tmp);
                A.vel(s2, tmp); tmp.addLocal(tvel);
                A.setVel(s2, tmp);
                
            }

            @Override
            public void collisionExit(Shape s1, Shape s2) {

            }
        });
        SphereShape s;
        
        SphereShape s1 = s =  new SphereShape(0.3f);
        s.vel(0.3f,0,0);
        A.accel.add(s);
        
        SphereShape s2 = s = new SphereShape(0.3f);
        s.pos(5,0,0);
        AcceleratedDebug.sphereColor = ColorRGBA.Red;
        A.accel.add(s);
        
        SphereShape s3 = s = new SphereShape(0.3f);
        s.pos(2,0,0);
        AcceleratedDebug.sphereColor = ColorRGBA.Green;
        
        //Let's add a limit to Green Sphere
        s3.setLimit(3f); //3 seconds
        A.accel.add(s);
        
        SphereShape s4 = s = new SphereShape(0.3f);
        s.pos(2,-2,0);
        s.vel(0,1,0);
        AcceleratedDebug.sphereColor = ColorRGBA.Orange;
        A.accel.add(s);
    }

    @Override
    public void simpleUpdate(float tpf) {
        A.accel.update(tpf);
    }
}
