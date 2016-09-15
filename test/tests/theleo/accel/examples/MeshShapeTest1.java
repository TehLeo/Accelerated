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

import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import java.util.Random;
import tests.theleo.accel.SimpleTest;
import theleo.accel.*;
import theleo.accel.jme3.*;
import theleo.accel.shapes.*;
import static theleo.accel.shapes.MeshShape.calcForSpherePlane;
import static theleo.accel.shapes.SphereShape.*;
import theleo.accel.util.Vars;

/**
 * Currently mesh shape is not yet fully supported and 
 * should not be used.
 * 
 * @author Juraj Papp
 */
public class MeshShapeTest1 extends SimpleTest {
    static {
        instance = new MeshShapeTest1();
    }
    
    int arrowsIndex = 0;
    

    @Override
    public void simpleInitApp() {
        super.simpleInitApp();
//        cam.setLocation(new Vector3f(22.200424f, 32.164543f, 16.77665f));
        cam.setLocation(new Vector3f(33.63829f, -0.101935446f, 13.745417f));
        cam.lookAtDirection(new Vector3f(-0.9428259f, 0.08776839f, -0.32152152f), Vector3f.UNIT_Y);
       
        A.accel = new Accelerated();
        A.accel.setImpactListener(new ImpactListener() {
            int count = 0;
            public void onLimit(Shape s) {}
            public void collisionEnter(Shape s1, Shape s2) {
                if(s2 instanceof SphereShape) return;
                
                SphereShape s = (SphereShape)s1;
                MeshShape m = (MeshShape)s2;
                                
                Vector3f normal = new Vector3f();
                Vector3f vel = new Vector3f();
                
                Vector3f mpos = new Vector3f(m.data[pos.x], m.data[pos.y], m.data[pos.z]);
                
                A.velNow(s, vel);
                                
                int i = m.collisionIndex;
                switch(m.collisionType) {
                    case MeshShape.CTYPE_FACE:
                        normal.set(m.faces[i], m.faces[i+1], m.faces[i+2]);
                        
                        float dot = normal.dot(vel);
                        dot += dot;
                        vel.subtractLocal(normal.mult(dot));
                        A.setVel(s, vel);
                        
                        break;
                    case MeshShape.CTYPE_EDGE:
                        Vector3f v1 = new Vector3f();
                        Vector3f v2 = new Vector3f();
                        
                        int p = 3*m.edges[i];
                        v1.set(m.verts[p], m.verts[p+1], m.verts[p+2]).addLocal(mpos);
                        p = 3*m.edges[i+1];
                        v2.set(m.verts[p], m.verts[p+1], m.verts[p+2]).addLocal(mpos);
                        
                        Vector3f b = new Vector3f();
                        b.set(v2).subtractLocal(v1);
                        Vector3f a = new Vector3f();
                        A.pos(s, a);
                        a.subtractLocal(v1);
                        Vector3f a1 = new Vector3f(b).multLocal((a.dot(b)/b.dot(b)));
                        Vector3f a2 = new Vector3f(a).subtractLocal(a1);
                        
                        //a2 is the normal of collision
                        normal.set(a2).normalizeLocal();
                        
                        
                        
                        dot = normal.dot(vel);
                        dot += dot;
                        vel.subtractLocal(normal.mult(dot));
                        A.setVel(s, vel);
                        
                        break;
                    case MeshShape.CTYPE_VERTEX:
                        a = new Vector3f();
                        A.pos(s, a);
                        normal.set(a).subtractLocal(m.verts[i], m.verts[i+1], m.verts[i+2])
                                .subtractLocal(mpos).normalizeLocal();
                        
                        dot = normal.dot(vel);
                        dot += dot;
                        vel.subtractLocal(normal.mult(dot));
                        A.setVel(s, vel);
                        
                        break;
                    default: 
                        System.out.println("default " + m.collisionType);
                        s.vel(0, 0, 0);
                        s.acc(0,0,0);
                }
                            
                count++;
            }
            public void collisionExit(Shape s1, Shape s2) {
            }
            public void nextCollision(Shape s1, Shape s2, float[] time) {}
        });
        
        Node n = ((Node)assetManager.loadModel("TestData/Models/accelmeshtest2.j3o"));
        n.depthFirstTraversal(new SceneGraphVisitor() {
            public void visit(Spatial spatial) {
                if(spatial instanceof Geometry) {
                    Mesh m = ((Geometry)spatial).getMesh();
                    MeshShape mesh = A.meshShape(m);
                    A.accel.addStatic(mesh);
                }
            }
        });
        
        DirectionalLight light = new DirectionalLight();
        light.setDirection(new Vector3f(-1f,-1f,0.5f).normalizeLocal());
        light.setColor(new ColorRGBA(89/255f, 50/255f, 22/255f, 1f));
        rootNode.addLight(light);
        rootNode.attachChild(n);
        
        final SphereShape s = new SphereShape(0.4f);

        s.pos(2.735f+0.5f, 10.5f, 0f);
        s.vel(1,0,0);
        s.acc(0, -9f, 0);
                
//        Random rand = new Random(0);
//        for(int y = 0; y < 1; y++)
//            for(int x = 0; x < 1; x++) {
//                for(int z = 0; z < 10; z++) {
//                    SphereShape ss = new SphereShape(0.4f);
//                    ss.pos(x, 13+y*0.2f, z);
//                    ss.vel((rand.nextFloat()-0.5f)*3f,0,(rand.nextFloat()-0.5f)*3f);
//                    ss.acc(0, -9f, 0);
//                    A.accel.add(ss);
//
//                    rootNode.attachChild(AcceleratedDebug.sphere(ss, assetManager));
//                }
//            }
        
        rootNode.attachChild(AcceleratedDebug.sphere(s, assetManager));
        A.accel.add(s);
                     
    }

    long at,total;
    float freq;
    int reset;
    boolean accelupdate = false;
    @Override
    public void simpleUpdate(float tpf) {
        if(accelupdate) {
            if(pause) return;

            long time = System.currentTimeMillis();
            A.accel.update(tpf);
            time = System.currentTimeMillis() - time;
            at += time;
            freq += tpf;
            if(freq > 10) { 
                freq -= 10;
                float num = at/1000f;
                float ttl = (System.currentTimeMillis()-total)/1000f;
                System.out.println("Ratio: "+(num/ttl)+"\n["+num+"]/"+ttl + ", ");
                reset--;
                if(reset == 0) {
                    at = 0;
                    reset = 3;
                    total = System.currentTimeMillis();
                }
            }
        }
        else {
            freq += tpf;
            if(freq > 1) {
                System.out.println("Starting ");
                total = System.currentTimeMillis();
                freq = 0;
                accelupdate = true;
            }
        }
    }
}
