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

import theleo.accel.shapes.LineShape;
import static theleo.accel.shapes.LineShape.HLEN;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Line;
import java.io.IOException;

/**
 * A control that moves a line created by AccelereatedDebug.
 * 
 * @author Juraj Papp
 */
public class LineVelControl implements Control {

    public Control cloneForSpatial(Spatial spatial) {
        LineVelControl r = new LineVelControl(shape);
        r.setSpatial(spatial);
        return r;
    }
    LineShape shape;
    Spatial sp;
    Geometry g;

    public LineVelControl(LineShape shape) {
        this.shape = shape;
    }
    @Override
    public void setSpatial(Spatial spatial) {
        sp = spatial;
        g = (Geometry)spatial;
    }
    Vector3f s = new Vector3f();
    Vector3f e = new Vector3f();
    @Override
    public void update(float tpf) {
        Line l = (Line)g.getMesh();
        
        float data[] = shape.data;        
        A.glPosNow(shape, s);
        A.getDirAt(shape, shape.travelled, e);
        e.normalizeLocal();
        if(data[HLEN] != 0) e.multLocal(data[HLEN]);
        else e.multLocal(0.5f);
//        s.subtractLocal(e);
        e.multLocal(2).addLocal(s);
       
        l.updatePoints(s, e);
    }
    @Override
    public void render(RenderManager rm, ViewPort vp) {}
    @Override
    public void write(JmeExporter ex) throws IOException {}
    @Override
    public void read(JmeImporter im) throws IOException {}
}
