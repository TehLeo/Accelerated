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
import theleo.accel.shapes.PlaneShape;
import theleo.accel.shapes.Shape;
import theleo.accel.shapes.SphereShape;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Line;
import static theleo.accel.shapes.LineShape.*;
import theleo.accel.shapes.MeshShape;
import static theleo.accel.shapes.SphereShape.rad;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Sphere;

/**
 * Utility class that displays shapes for debugging purposes.
 * 
 * @author Juraj Papp
 */
public class AcceleratedDebug extends Accelerated {
    public AssetManager am;
    public Node node;
    
    public AcceleratedDebug(Node node, AssetManager a) {
        super();
        this.node = node;
        am = a;
    }
    
    @Override
    public void add(Shape s) {
        super.add(s); 
        if(!showShape) return;
        if(s instanceof LineShape)
            node.attachChild(line((LineShape)s, am));
        else if(s instanceof PlaneShape)
            node.attachChild(plane((PlaneShape)s, am));
        else if(s instanceof SphereShape) 
            node.attachChild(sphere((SphereShape)s, am));
    }
    
    @Override
    public void addStatic(Shape p) {
        super.addStatic(p);
        if(!showShape) return;
        if(p instanceof PlaneShape)
            node.attachChild(plane((PlaneShape)p, am));
        else if(p instanceof SphereShape) 
            node.attachChild(sphere((SphereShape)p, am));
    }
    public void _add(Shape s) {
        super.add(s);
    }
    public void _addStatic(Shape p) {
        super.addStatic(p);
    }
    
    public static Geometry debugShape(Shape s, AssetManager am) {
        if(s instanceof LineShape)
            return line((LineShape)s, am);
        if(s instanceof PlaneShape)
            return plane((PlaneShape)s, am);
        if(s instanceof SphereShape) 
            return sphere((SphereShape)s, am);
        if(s instanceof MeshShape) 
            return mesh((MeshShape)s, am);
        return null;
    } 
    
    public static boolean showShape = true;
    public static Mesh sphere = new Sphere(12, 12, 1);
    public static ColorRGBA sphereColor = ColorRGBA.White;
    public static Geometry sphere(SphereShape s, AssetManager am) {
        Geometry sp = new Geometry("Shape["+s.id+"]");
//        sp.setMesh(new Sphere(12, 12, s.data[rad]));
        sp.setMesh(sphere);
        sp.setLocalScale(Math.max(s.data[rad], 0.05f));
        
        Material m = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor("Color", sphereColor);
        if(sphereColor.a != 1f) {
            m.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
            sp.setQueueBucket(Bucket.Transparent);
        }
        sp.setMaterial(m);
    
        sp.getMaterial().getAdditionalRenderState().setWireframe(true);
        
        AccelControl control = new AccelControl(s);
        sp.addControl(control);
        return sp;
    }
    public static Geometry line(LineShape s, AssetManager am) {
        Geometry sp = new Geometry("Shape["+s.id+"]");
        Vector3f a = new Vector3f(s.data[POS], s.data[POS+1], s.data[POS+2]);
        Vector3f dir = new Vector3f();
        A.getDirAt(s, s.travelled, dir);
        dir.normalizeLocal();
        if(s.data[HLEN] != 0) dir.multLocal(s.data[HLEN]);
        else dir.multLocal(0.5f);
//        a.subtractLocal(dir);
        dir.multLocal(2).addLocal(a);
        sp.setMesh(new Line(a, dir));
        sp.getMesh().setLineWidth(2f);
        sp.setCullHint(Spatial.CullHint.Never);
        
        Material m = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor("Color", ColorRGBA.White);
        
        sp.setMaterial(m);
    
        sp.getMaterial().getAdditionalRenderState().setWireframe(true);
        
        LineVelControl control = new LineVelControl(s);
        sp.addControl(control);
        
        return sp;
    }
    public static Node text(String text, BitmapFont font, Node n) {
        BitmapText t = new BitmapText(font);
        t.setSize(0.1f);
        t.setText(text);
        BillboardControl control = new BillboardControl();
        t.addControl(control);
        if(n != null) n.attachChild(t);
        return t;
    }
    public static Geometry plane(PlaneShape p, AssetManager am) {        
        Mesh m = new Mesh();
        m.setBuffer(VertexBuffer.Type.Index, 3, new short[] {
                0,1,2,2,3,0
                });
        float s = 11;
        if(p.b != 0){
        m.setBuffer(VertexBuffer.Type.Position, 3, 
                new float[] {s, -(p.d+s*p.a + s*p.c)/p.b,  s,
                             s, -(p.d+s*p.a - s*p.c)/p.b, -s,
                            -s, -(p.d-s*p.a - s*p.c)/p.b, -s, 
                            -s, -(p.d-s*p.a + s*p.c)/p.b,  s});
        }else if(p.a != 0) {
            m.setBuffer(VertexBuffer.Type.Position, 3, 
                new float[] {-(p.d+s*p.b + s*p.c)/p.a,  s,  s,
                             -(p.d+s*p.b - s*p.c)/p.a,  s, -s,
                             -(p.d-s*p.b - s*p.c)/p.a, -s, -s, 
                             -(p.d-s*p.b + s*p.c)/p.a, -s,  s});
        }
        else {
            m.setBuffer(VertexBuffer.Type.Position, 3, 
                new float[] {s, s,  -(p.d+s*p.a + s*p.b)/p.c,
                             s, -s, -(p.d+s*p.a - s*p.b)/p.c,
                            -s, -s, -(p.d-s*p.a - s*p.b)/p.c, 
                            -s, s,  -(p.d-s*p.a + s*p.b)/p.c});
        }
        
        Material mat = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Gray);
        mat.getAdditionalRenderState().setWireframe(true);
        mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        m.updateBound();
        
        Geometry g = new Geometry("Shape["+p.id+"]", m);
        g.setMaterial(mat);
        return g;
    }
    public static Geometry mesh(MeshShape mshape, AssetManager am) {        
        Mesh m = new Mesh();
        m.setMode(Mesh.Mode.Lines);
        m.setLineWidth(2f);
        
        m.setBuffer(VertexBuffer.Type.Position, 3, mshape.verts);
        m.setBuffer(VertexBuffer.Type.Index, 3, mshape.edges);
        
        Geometry g = new Geometry("MeshShape", m);
        Material mat = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Cyan.mult(0.7f));
        g.setMaterial(mat);
        
        g.setLocalTranslation(mshape.data[pos.x], mshape.data[pos.y], mshape.data[pos.z]);
        
        return g;
    }
    public void setShapeColor(Shape s, ColorRGBA col) {
        Spatial sp = node.getChild("Shape["+s.id+"]");
        if(sp != null && sp instanceof Geometry) ((Geometry)sp).getMaterial().setColor("Color", col);
    }
    public ColorRGBA getShapeColor(Shape s) {
        Spatial sp = node.getChild("Shape["+s.id+"]");
        if(sp != null && sp instanceof Geometry) return (ColorRGBA)((Geometry)sp).getMaterial().getParam("Color").getValue();
        return null;
    }
}
