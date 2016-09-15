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

import theleo.accel.jme3.A;
import theleo.accel.shapes.Shape;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.VideoRecorderAppState;
import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.InputListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.SceneProcessor;
import com.jme3.post.filters.GammaCorrectionFilter;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import com.jme3.system.NanoTimer;
import com.jme3.system.Timer;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.concurrent.Callable;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JToolBar;

/**
 * Base class for tests and examples, contains many useful methods. 
 * 
 * @author Juraj Papp
 */
public abstract class SimpleTest extends SimpleApplication {
    public static SimpleTest instance;
    public static boolean stats = true, vsync = true, showSettings = false;
    public boolean pause = false;
    public static boolean wire = false;
    public static boolean record = false;
    public static boolean headless = false;
    public static float camSpeed = 5f; 
    public static boolean jmeheadless = false;
    public static boolean controls = false;
    
    public static AssetManager am;
    public static Node root;
    
    public void simpleInitApp() {
        am = assetManager;
        root = rootNode;
        if(headless) return;
        if(record) stateManager.attach(new VideoRecorderAppState());
        if(controls) {
            JFrame f = new JFrame();
            f.setLayout(new BorderLayout());
            
            JToolBar bar = new JToolBar();
            for(int i = 0; i < 8; i++) {
                JButton b = new JButton("F"+i);
                final int j = i;
                b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        enqueue(new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                 action(j);
                                 return null;
                            }
                        });
                    }
                });
                bar.add(b);
            }
            f.add(bar, BorderLayout.NORTH);
            
            f.pack();
            f.setLocation(0, Toolkit.getDefaultToolkit().getScreenSize().height-f.getSize().height);
            f.setVisible(true);
        }
        getViewPort().setBackgroundColor(ColorRGBA.LightGray);
        initWireFrame();
        initFastCam();
        if(!stats) stateManager.getState(StatsAppState.class).toggleStats();
    }
    public void action(int i) {
    }
    public void msg(Object... o) {
    
    }
    public static void debugThread(Runnable run) {
        Thread debugThread = new Thread(run, "debug") {
            @Override
            public void run() {
                int c = 5000;
                for(int i = 0; i < 5000; i++) {
                    super.run();
                }
            }
        };
        debugThread.start();
    }
    public void gamma(float value) {
        FilterPostProcessor fpp = null;
        
        for(SceneProcessor s : viewPort.getProcessors()) 
            if(s instanceof FilterPostProcessor)
                fpp = (FilterPostProcessor)s;
        if(fpp == null) {
            fpp = new FilterPostProcessor(assetManager);
            viewPort.addProcessor(fpp);
        }
        GammaCorrectionFilter gamma = fpp.getFilter(GammaCorrectionFilter.class);
        if(gamma == null) fpp.addFilter(new GammaCorrectionFilter(value));
        else gamma.setGamma(value);
    }
    public Geometry geom(Mesh m, ColorRGBA c) {
        Geometry g = new Geometry("Geom", m);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", c);
        g.setMaterial(mat);
        return g;
    } 
    public Geometry geomAt(Mesh m, float x, float y, float z, ColorRGBA c) {
        Geometry g = new Geometry("Geom", m);
        g.setLocalTranslation(x, y, z);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", c);
        g.setMaterial(mat);
        return g;
    } 
    public Geometry sphereAt(float radius, Vector3f pos, ColorRGBA color) {
        return sphereAt(radius, pos.x, pos.y, pos.z, color, null);
    }
    public Geometry sphereAt(float radius, float x, float y, float z, ColorRGBA color) {
        return sphereAt(radius, x, y, z, color, null);
    }
    public static DataTable createAccelDataTable() {
        DataTable dt = new DataTable();
        for(int i = 0; i < A.accel.spheres.size(); i++) {
            final Shape s = A.accel.spheres.get(i);
            dt.watch(new DataTable.Evaluable() {
                public String name() {
                    return s.toString() +" "+ s.travelled;
                }
                public String eval() {
                    return s.movingFor+"/"+s.limit;
                }
            });
        }
        return dt;
    }
    public Geometry sphereAt(float radius, Vector3f pos, ColorRGBA color, Node node) {
        Sphere b = new Sphere(12,12,radius);
        Geometry geom = new Geometry("Box", b);
        geom.setLocalTranslation(pos);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        geom.setMaterial(mat);
        if(node != null) node.attachChild(geom);
        return geom;
    }
    public Geometry sphereAt(float radius, float x, float y, float z, ColorRGBA color, Node node) {
        Sphere b = new Sphere(12,12,radius);
        Geometry geom = new Geometry("Box", b);
        geom.setLocalTranslation(x, y, z);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        geom.setMaterial(mat);
        if(node != null) node.attachChild(geom);
        return geom;
    }
    public void triangle(Vector3f a, Vector3f b, Vector3f c, ColorRGBA col, Node n) {
        n.attachChild(line(a, b, col));
        n.attachChild(line(a, c, col));
        n.attachChild(line(b, c, col));
    }
    public Geometry line(Vector3f s, Vector3f e) {
        Line l = new Line(s, e);
        Geometry g = new Geometry("", l);
        Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        g.setMaterial(m);
        return g;
    }
    public Geometry line(Vector3f s, Vector3f e, ColorRGBA c) {
        Line l = new Line(s, e);
        l.setLineWidth(2f);
        Geometry g = new Geometry("", l);
        Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor("Color", c);
        g.setMaterial(m);
        return g;
    }
    public static void main(String[] args) {
        SimpleTest app = instance;
        if(headless) {
            
            app.simpleInitApp();
            Timer timer = new NanoTimer();
            timer.reset();
            float tpf = 0;
            float time;
            while(true) {
                timer.update();
                time = timer.getTimePerFrame();
                tpf += time;
                if(tpf > 0.016f) {
                    app.simpleUpdate(0.016f);
                    tpf -= 0.016f;
                }
//                else Thread.yield();
            }
        }
        else {
            if(record) app.setTimer(new VideoRecorderAppState.IsoTimer(30));
            app.setShowSettings(showSettings);
            if(!showSettings) {
                AppSettings settings = new AppSettings(true);

                settings.put("Width", 800);
                settings.put("Height", 600);
                settings.put("Title", "Accelerated: Tests");
                settings.put("VSync", vsync);
                settings.put("Samples", 4);
                app.setSettings(settings);
            }
            if(jmeheadless)app.start(JmeContext.Type.Headless);
            else app.start();
        }
    }
    private int actionc = 0;
    
    public void trigger(Trigger t, InputListener a) {
        trigger(t, a, "trigger_"+(++actionc));
    }
    public void trigger(Trigger t, InputListener a, String name) {
        inputManager.addMapping(name, t);
        inputManager.addListener(a, name);
    }
    public void key(int keyInput, InputListener a) {
        key(keyInput, a, "trigger_"+(++actionc));
    }
    public void key(int keyInput, InputListener a, String name) {
        trigger(new KeyTrigger(keyInput), a, name);
    }
  
    public void initWireFrame() {
        flyCam.setDragToRotate(true);
        final WireProcessor wp = new WireProcessor(assetManager);
         inputManager.addMapping("f4", new KeyTrigger(KeyInput.KEY_F4));
         inputManager.addListener(new ActionListener() {
             boolean enabled = wire;   
             public void onAction(String name, boolean isPressed, float tpf) {
                 if(!isPressed) return;
                 enabled = !enabled;   
                 if(enabled)  getViewPort().addProcessor(wp);
                 else  getViewPort().removeProcessor(wp);
                }
            }, "f4");
         if(wire)  getViewPort().addProcessor(wp);
    }
        public void initFastCam() {
        String[] mappings = new String[]{
            "FLYCAM_Left",
            "FLYCAM_Right",
            "FLYCAM_Up",
            "FLYCAM_Down",

            "FLYCAM_StrafeLeft",
            "FLYCAM_StrafeRight",
            "FLYCAM_Forward",
            "FLYCAM_Backward",

            "FLYCAM_ZoomIn",
            "FLYCAM_ZoomOut",
            "FLYCAM_RotateDrag",

            "FLYCAM_Rise",
            
            "FLYCAM_Lower",
            "FLYCAM_InvertY"
        };
        inputManager.removeListener(flyCam);
        inputManager.addListener(new AnalogListener() {

            public void onAnalog(String name, float value, float tpf) {
                flyCam.onAnalog(name, value*camSpeed, tpf);
            }
        }, mappings);
        inputManager.addListener(new ActionListener() {

            public void onAction(String name, boolean isPressed, float tpf) {
                flyCam.onAction(name, isPressed, tpf);
            }
        }, mappings);
        inputManager.addMapping("Pos", new KeyTrigger(KeyInput.KEY_F2));
        inputManager.addMapping("P", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addListener(new ActionListener() {
            public void onAction(String name, boolean isPressed, float tpf) {
                if(!isPressed) return;
                if(name.equals("Pos")) {
                    Vector3f pos = cam.getLocation();
                    System.out.println("cam at: " + pos.x + "f, "+pos.y+"f, "+pos.z+"f");
                    pos = cam.getDirection();
                    System.out.println("cam dir: " + pos.x + "f, "+pos.y+"f, "+pos.z+"f");
                }
                else if(name.equals("P")) {
                    pause = !pause;
                }
            }
        }, "Pos", "P");
    }
    public Node attachCoordinateAxes(Vector3f pos) {
        return attachCoordinateAxes(pos, rootNode);
    }
    public Node attachCoordinateAxes(Vector3f pos, Node node) {
        Node coords = new Node("coords");
        Arrow arrow = new Arrow(Vector3f.UNIT_X);
        arrow.setLineWidth(2); 
        putShape(arrow, ColorRGBA.Red.mult(0.9f), coords).setLocalTranslation(pos);

        arrow = new Arrow(Vector3f.UNIT_Y);
        arrow.setLineWidth(2);
        putShape(arrow, ColorRGBA.Green.mult(0.8f), coords).setLocalTranslation(pos);

        arrow = new Arrow(Vector3f.UNIT_Z);
        arrow.setLineWidth(2);
        putShape(arrow, ColorRGBA.Blue.mult(0.9f), coords).setLocalTranslation(pos);
        node.attachChild(coords);
        return coords;
    }
     public static interface Link<T> {
         public T read();
     }
     public static class VectorControl implements Control {
        public Link<Vector3f> pos, dir;
        public Geometry sp;
        public VectorControl(Link<Vector3f> pos, Link<Vector3f> dir) {
            this.pos = pos;
            this.dir = dir;
        }
        @Override
        public Control cloneForSpatial(Spatial spatial) {
            VectorControl vc = new VectorControl(pos, dir);
            spatial.addControl(vc);
            return vc;
        }
        @Override
        public void setSpatial(Spatial spatial) {
            sp = (Geometry)spatial;
        }
        @Override
        public void update(float tpf) {
            sp.setLocalTranslation(pos.read());
            Arrow a = ((Arrow)sp.getMesh());
            a.setArrowExtent(dir.read());
        }
        @Override
        public void render(RenderManager rm, ViewPort vp) {}
        @Override
        public void write(JmeExporter ex) throws IOException {}
        @Override
        public void read(JmeImporter im) throws IOException {}
     }
    public static Geometry vector(Link<Vector3f> pos, Link<Vector3f> dir) {
        Arrow arrow = new Arrow(dir.read());
//        arrow.setLineWidth(2); 
        Geometry g = putShape(arrow, ColorRGBA.Magenta.mult(0.9f), root);
        g.setLocalTranslation(pos.read());
        g.addControl(new VectorControl(pos, dir));
        return g;
    }
    public static Geometry putShape(Mesh shape, ColorRGBA color) {
         return putShape(shape, color, root);
     }
     public static Geometry putShape(Mesh shape, ColorRGBA color, Node node) {
        Geometry g = new Geometry("coordinate axis", shape);
        Material mat = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
//        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", color);
        g.setMaterial(mat);
        node.attachChild(g);
        return g;
    }
}
