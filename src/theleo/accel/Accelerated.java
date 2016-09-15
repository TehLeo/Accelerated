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

package theleo.accel;

import theleo.accel.shapes.Shape;
import theleo.accel.util.LimitEvent;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

/**
 * A class that handles a collection of Shapes, updates them and
 * fires collision events.
 * 
 * @author Juraj Papp
 */

public class Accelerated {
    public static class ColResult {
        public float[] minTime = new float[2];
        public Shape minS;
        public Shape minT;
//        public Collides minT = new Collides();
        public int type;
        Results copy = new Results();
        public void clear(float limit) {
            minS = null;
            minT = null;
            type = -1;
//            minT.shape = null;
//            minT.next = null;
            minTime[0] = limit;
            minTime[1] = -1;
            copy.reset();
        }
        public boolean hasResult() {
            return minS != null;
        }
//        public void applyTime() {
//            minS.movingFor = minTime[0];
//            if(Log.isEnabled()) Log.log( Color.blue,"applyTime {}", minS.movingFor);
//
//            Collides c = minT;
//            while(c != null) {
//                c.shape.movingFor = minTime[0];
//                if(Log.isEnabled()) Log.log(Color.blue, "applyTime {}", minT.shape.movingFor);
//                nextCollision(minS, minT.shape, minTime);
//                c = c.next;
//            }
//        }
    }
    public static final GroupListener IncludeGroup = new GroupListener() {
        @Override
        public boolean areInGroup(Shape s1, Shape s2) {
//            if(s1.groupId == -1 || s2.groupId == -1) return false;
            return s1.groupId != -1 && s2.groupId != -1 && s1.groupId != s2.groupId;
        }
    };
    public static final GroupListener ExcludeGroup = new GroupListener() {
        @Override
        public boolean areInGroup(Shape s1, Shape s2) {
            return s1.groupId != -1 && s1.groupId == s2.groupId;
        }
    };
    public static class RecalcGroupListener implements GroupListener {
        GroupListener listener = ExcludeGroup;
        HashSet<Shape> collection = new HashSet<Shape>();
//        ArrayList<Shape> toAdd = new ArrayList<Shape>();
        @Override
        public boolean areInGroup(Shape s1, Shape s2) {
            if(listener.areInGroup(s1, s2)) return true;
            return collection.contains(s1) || collection.contains(s2);
        }
        public void setCollection(Shape[] col) {
//            toAdd.clear();
//            toAdd.addAll(col);
            collection.clear();
            for(Shape s : col) {
                collection.add(s);
                if(s.willCollide != null) {
                    s.clear();
                    //throw new IllegalArgumentException();
                }
            }
        }
        public void setCollection(Collection<? extends Shape> col) {
//            toAdd.clear();
//            toAdd.addAll(col);
            collection.clear();
            collection.addAll(col);
            for(Shape s : col) {
                if(s.willCollide != null) {
                    s.clear();
                    //throw new IllegalArgumentException();
                }
            }
        }

    }
    public RecalcGroupListener RecalcGroup = new RecalcGroupListener();
    public static float ZERO_TOLERANCE = 1.1920928955078125E-6f;

    public static float REC_TOLERANCE = 1.1920928955078125E-3f;
 
    public ArrayList<Shape> statics = new ArrayList<Shape>();
    public ArrayList<Shape> spheres = new ArrayList<Shape>();
    public ImpactListener impactListener;
    public GroupListener groupListener = ExcludeGroup;
    
    public static long getHash(Shape s1, Shape s2) {
        return (s1.id<s2.id)?((((long)s1.id)<<32)|s2.id):((((long)s2.id)<<32)|s1.id);
    }
    public static int getPairHash(Shape s1, Shape s2) {
        return (s1.getType()<s2.getType())?(((s1.getType())<<16)|s2.getType())
                :(((s2.getType())<<16)|s1.getType());
    }

    public static int colcount;
    public static class PrivateData {
        public TreeMap<Long, Integer> specSet;
        public HashSet<Long> overlapSet;
        public ArrayList<Shape> closest;
        public ColResult cRes1, cRes2;
        public Results fTmp;
        public HashSet<Shape> l;
        public ArrayDeque<Shape> stack;
    }
    public PrivateData _priv = new PrivateData();
    public Accelerated() {
        PrivateData p = _priv;
        p.cRes1 = cRes1; p.cRes2 = cRes2;
        p.closest = closest; p.fTmp = fTmp;
        p.l = l; p.specSet = specSet;
        p.stack = stack; 
        p.overlapSet = overlapSet;
    }
    
    protected TreeMap<Long, Integer> specSet = new TreeMap<Long, Integer>();
    protected HashSet<Long> overlapSet = new HashSet<Long>();
    protected ArrayList<Shape> closest = new ArrayList<Shape>();

    protected ColResult cRes1 = new ColResult(), cRes2 = new ColResult();
    private Results fTmp = new Results();
    
    //check if eg. can add objects in onLimit, onImpact
    public void add(Shape s) {
        //requires to check all moving objects
        //we need to know their speeds
        //if(Log.isEnabled()) Log.log(Log.purple, "ADD {}", s.id);
        
        s.isStatic = false;
        //optionally applyrecalc before adding to spheres?
        spheres.add(s);
        applyRecalc(s);
    }
    public void addStatic(Shape s) {
        //if(Log.isEnabled()) Log.log(Log.cyan, "ADD STATIC {}", s.id);
        s.isStatic = true;
        //optionally applyrecalc before adding to statics?
        statics.add(s);
        applyRecalc(s);
        //recalc for statics
    }
    protected boolean updateLoop = false;
    public boolean remove(Shape s) {
        if(s.isStatic?statics.remove(s):spheres.remove(s)) {
            //can be in closest, l, stack
            //can have edges
            //may need to do recalcs
            
            //case 1: removed in onLimit
            //case 2: removed in onImpact
            //case 3: removed outside of update
            if(s.willCollide != null) {
                //removing this element requires
                //a recalc
                Shape recalc = s.willCollide;
                s.clear();
                if(updateLoop) {
                    //add it to the l set
                    //update loop will recalc it
                    l.add(recalc);
                }
                else {
                    //call recalc directly
                    applyRecalc(recalc);
                }
            }
                
            if(updateLoop) {
                //element was removed in middle of update loop
                //due to the call to the user's impact listener
                
                //remove s from l set that is to be updated
                l.remove(s);
                //remove s from stack
                stack.remove(s);
            }
            
            if(!s.isStatic && closest.contains(s)) {
                //the removed element is in the closest list
                //recalculate closest
                closest.clear();
                for(int i = 0; i < spheres.size(); i++)
                    updateClosest(spheres.get(i));
                //debug
                getNextCollisionTime();
                //
            }
            
            //todo
            //remove records from overlapSet
            //optionally make a method that cleans it
            //every eg 50 removals
            
            return true;
        }
        return false;
    }
    private HashSet<Shape> l = new HashSet<Shape>();
    private ArrayDeque<Shape> stack = new ArrayDeque<Shape>();
    
    public void update(float tpf) {
        while(tpf > 0) {
            float t = getNextCollisionTime();
            if(Float.isNaN(t)) { t = tpf; tpf = 0; }
            else if(t >= tpf) {t = tpf; tpf = 0;}
            else tpf -= t;
            updateSingle(t);
        }
    }
    public double elapsed = 0f;
    public void updateSingle(float tpf) {  
        Shape min = null;
        float minTime;
       
        if(!closest.isEmpty()) { min = closest.get(0); }
        if(min!=null&&(minTime=min.movingFor-min.travelled) <= tpf) {
            
//            Log.log("SortedCol " + min.id + ", " + ((min.willColide==null)?null:min.willColide.id)  + ", " + minTime, Color.RED);
            Shape s;
            for(int i = 0; i < statics.size(); i++)  {
                s = statics.get(i);
                s.travelled += minTime;
                s.apply(s.travelled);
            }
          // if(Log.isEnabled()) Log.log(Color.red, "Min {}, {}, {}, {}" , minTime,  min.travelled, min.movingFor, min.limit);
          //  if(min.willCollide != null && Log.isEnabled()) Log.log(Color.red, "MinW {}, {}", min.willCollide.travelled, min.willCollide.movingFor);
    
            l.clear();
            stack.clear();
            for(int i = 0; i < spheres.size(); i++)  {
                s = spheres.get(i);
                s.travelled += minTime;
                s.apply(s.travelled);
                if(s.movingFor <= ZERO_TOLERANCE) stack.add(s);
            }
            //if(stack.size() != 1 && Log.isEnabled()) Log.log(Color.red, "Stack size {}", stack.size());
            updateLoop = true;
            long hash;
            Shape t;
            while(!stack.isEmpty()) {
                s = stack.pop();
                if(!l.contains(s)) {
                    l.add(s);
                    if(s.willCollide == null) {
                        s.movingFor = s.limit = Float.MAX_VALUE;
                        onLimit(s);
                    }
                    else {
                        t = s.willCollide;
                        byte spec = s.spec;
                        s.clear();
                        hash = getHash(s, t);
                        l.add(t);
                        if(spec == -1) {
                            if(overlapSet.contains(hash)) {
                                impactListener.collisionExit(s, t);
                                overlapSet.remove(hash);
                            }
                            else {
                                overlapSet.add(hash);
                                impactListener.collisionEnter(s, t);
                            }
                        }
                        else {
                            Integer rII = specSet.get(hash);
                            if(rII == null) throw new IllegalArgumentException();
                            if(overlapSet.contains(hash)) {
                                impactListener.collisionExit(s, t);
                                overlapSet.remove(hash);
                            }
                            else {
                                overlapSet.add(hash);
                                impactListener.collisionEnter(s, t);
                            }
                            specSet.put(hash, rII+1);
                        }
                    }
                }
            }
            
            
            if(minTime > REC_TOLERANCE) {
                if(!specSet.isEmpty()) {
                    for(int i = 0; i < spheres.size(); i++) 
                        spheres.get(i).spec = -1;
                    for(int i = 0; i < statics.size(); i++) 
                        statics.get(i).spec = -1;
                }
                specSet.clear();
            }
            
            closest.clear();

            //apply group recalc on l
            applyRecalc(l);
            updateLoop = false;

//            if(Log.isEnabled()) Log.log(Color.blue, "NOW C {}", closest.toString());
          
            elapsed += minTime;
//            if(minTime != tpf) {
//                updateSingle(tpf-minTime);
//            }
        }
        else {
            elapsed += tpf;
            //travelled < moving for
            for(int i = 0; i < spheres.size(); i++) {
                Shape s = spheres.get(i);
                if(s.movingFor != Float.MAX_VALUE) {
                    if(s.travelled >= s.movingFor) {
                        System.err.println("next col " + getNextCollisionTime());
                        System.err.println("trav " +s.travelled + " >= " + s.movingFor);
                        throw new IllegalArgumentException();
                    }
                }
            }
            for(int i = 0; i < statics.size(); i++) {
                Shape s = statics.get(i);
                if(s.movingFor != Float.MAX_VALUE) {
                    if(s.travelled >= s.movingFor) {
                        System.err.println("next col " + getNextCollisionTime());
                        System.err.println("trav " +s.travelled + " >= " + s.movingFor);
                        throw new IllegalArgumentException();
                    }
                }
            }
            
            for(int i = 0; i < spheres.size(); i++) 
                spheres.get(i).travelled += tpf;
            for(int i = 0; i < statics.size(); i++) 
                statics.get(i).travelled += tpf;
            
            //travelled < moving for
            for(int i = 0; i < spheres.size(); i++) {
                Shape s = spheres.get(i);
                if(s.movingFor != Float.MAX_VALUE) {
                    if(s.travelled >= s.movingFor) {
                        System.err.println("next col " + getNextCollisionTime());
                        System.err.println("trav " +s.travelled + " >= " + s.movingFor);
                        throw new IllegalArgumentException();
                    }
                }
            }
            for(int i = 0; i < statics.size(); i++) {
                Shape s = statics.get(i);
                if(s.movingFor != Float.MAX_VALUE) {
                    if(s.travelled >= s.movingFor) {
                        System.err.println("next col " + getNextCollisionTime());
                        System.err.println("trav " +s.travelled + " >= " + s.movingFor);
                        throw new IllegalArgumentException();
                    }
                }
            }
            if(tpf > REC_TOLERANCE) {
                if(!specSet.isEmpty()) {
                    for(int i = 0; i < spheres.size(); i++) 
                        spheres.get(i).spec = -1;
                    for(int i = 0; i < statics.size(); i++) 
                        statics.get(i).spec = -1;
                }
                specSet.clear();
            }
        }
    }
    protected void updateClosest(Shape sp) {
//        Log.log("Update Closest " + sp.id + ", " + sp.willCollide.shape, Color.gray);
        if(sp.willCollide == null && sp.movingFor == Float.MAX_VALUE) return;
        if (closest.isEmpty()) {
            closest.add(sp);
        } else {
            Shape s = closest.get(0);
            if (sp.movingFor - sp.travelled < s.movingFor - s.travelled) {
                closest.clear();
                closest.add(sp);
            }
        }
        
//        if(Log.isEnabled()) {
//            String cl = "[";
//            for(Shape s : closest) {
//                cl += s.id+","+s.travelled+"/"+s.movingFor+"/"+s.limit+((s instanceof SphereShape)?"S":"L")+";";
//            }
//            Log.log(Color.ORANGE, "Update{}]", cl);
//        }
    }
    public float getNextCollisionTime() {
        float min = Float.NaN;
        for(int i = 0; i < spheres.size(); i++) {
            Shape s = spheres.get(i);
            if(s.movingFor != Float.MAX_VALUE) {
                float col = s.movingFor-s.travelled;
                if(min != min) min = col;
                else if(col < min) min = col;
            }
        }
        
        if(closest.isEmpty()) {
            if(min == min) {
                System.err.println("min " + min);
                throw new IllegalArgumentException();
            }
            return Float.NaN;
        }
        
        Shape s = closest.get(0);
        float time = s.movingFor-s.travelled;
        
        if(Math.abs(time-min) > ZERO_TOLERANCE) {
            System.err.println("min " + min);
            System.err.println("time " + time);
            throw new IllegalArgumentException();
        }
        return time < 0 ? 0f : time;
    }
    protected void applyGroupRecalc() {
        RecalcGroup.listener = groupListener;
        groupListener = RecalcGroup;
        
        String update = "";
        Iterator<? extends Shape> iter = RecalcGroup.collection.iterator();
        while(iter.hasNext()) {
            Shape s = iter.next();
            iter.remove();
//            if(Log.isEnabled()) Log.log(Log.purple, "RecalcGroup {} {}", s, RecalcGroup.collection.toString());
            if(s.willCollide != null) throw new IllegalArgumentException("error");
            applyRecalc(s);
            update += s + ",";
        }
        groupListener = RecalcGroup.listener;
        closest.clear();
        for(int i = 0; i < spheres.size(); i++)
            updateClosest(spheres.get(i));
        //debug
        getNextCollisionTime();
        
//        if(Log.isEnabled()) Log.log(Color.blue, "closest {}", getNextCollisionTime());
//        if(Log.isEnabled()) Log.log(Color.blue, "NOW C2 {}, [{}]", closest.toString(), update);
    }
    public void applyRecalc(Collection<? extends Shape> col) {
//        if(Log.isEnabled()) Log.log(Color.blue, "-------Recalc Collection");
        RecalcGroup.setCollection(col);
        applyGroupRecalc();
    }
    public void applyRecalc(Shape... col) {
//        if(Log.isEnabled()) Log.log(Color.blue, "-------Recalc Collection");
        RecalcGroup.setCollection(col);
        applyGroupRecalc();
    }
    public void applyRecalc(Shape s) {
        //recalc for s and for any connected
        if(s.willCollide != null) {
            RecalcGroup.collection.clear();
            RecalcGroup.collection.add(s);
            RecalcGroup.collection.add(s.willCollide);
            s.clear();
            applyGroupRecalc();         
            return;
        }
        recalc(s, cRes1);
        if(cRes1.hasResult()) applyRecalc(cRes1);
        else if(s.limit != Float.MAX_VALUE) {
            s.movingFor = s.limit;
            //optimize if can, prolly no need to loop for all spheres
            closest.clear();
            for(int i = 0; i < spheres.size(); i++)
                updateClosest(spheres.get(i));
            //debug
            getNextCollisionTime();
            //
        }
    }
    public void applyRecalc(ColResult result) {
        if(result.minS.movingFor >= result.minTime[0]) {

        }else throw new IllegalArgumentException("MIN S, apply greater time: "+ result.minS.movingFor + " < " + result.minTime[0] + ", " + result.minS.limit);

        if(result.minS.willCollide != null) throw new IllegalArgumentException();
        Shape rec = null;
        if(result.minT.willCollide != null) {
            if(result.minS != result.minT.willCollide)
                rec = result.minT.willCollide;
            result.minT.clear();
        }
        else {
            if(result.minS.spec != -1 || result.minT.spec != -1)
                throw new IllegalArgumentException();
        }
        result.minS.set(result.minT);
        result.minS.movingFor = result.minTime[0];
        result.minT.movingFor = result.minTime[0];
        if(result.type != -1) {
            result.minS.spec = 0;
            result.minT.spec = 0;
        }
        if(result.copy.flagsLen != 0) {
            if(result.minS.getType()>=result.minT.getType()) result.minS.setFlags(result.copy.flags, result.copy.flagsLen);
            else result.minT.setFlags(result.copy.flags, result.copy.flagsLen);
        }

        //If sphere is removed, new closest has to be recalculated
        if(closest.isEmpty()) {
            //optimize if can, prolly no need to loop for all spheres
            for(int i = 0; i < spheres.size(); i++)
              updateClosest(spheres.get(i));
            //debug
            getNextCollisionTime();
            //
        }
        else {
            //optimize if can, prolly no need to loop for all spheres
            closest.clear();
            for(int i = 0; i < spheres.size(); i++)
              updateClosest(spheres.get(i));

            //debug
            getNextCollisionTime();
        }

      if(rec != null) {
          applyRecalc(rec);
      }
    }
    public void recalc(Shape s, ColResult store) {
        if(s.travelled != 0) s.apply(s.travelled);
        store.clear(s.limit);
        Shape min = null;
        final Results fTmp = this.fTmp;
        final float[] minTime = store.minTime;
        final float[] time = fTmp.time;
        Shape t;
        ArrayList<? extends Shape> list = spheres;

        int r1, r2, tI, type, minType=-1;
        long hash = 0;
        final GroupListener grp = groupListener;
        for(int z = s.isStatic?0:1; z >= 0; z--, list = statics)
        for(int i = 0; i < list.size(); i++) {
            t = list.get(i);
            if(s == t || grp.areInGroup(s, t)) {
//                Log.log("Skip " + s.id + ", " + t.id, Log.orange);
                continue;
            }
            calcFor(s, t, fTmp);
            
            
//            if(Log.isEnabled()) Log.log(Color.GRAY, "Rec ({},{}){}", s.id, t.id, (fTmp.len==0?"null":Arrays.toString(time)));
            if(fTmp.len != 0) {
                for(r1 = 0; r1 < fTmp.len && -REC_TOLERANCE > time[r1]; r1++); 
                if(r1 == fTmp.len) continue;
                for(r2 = r1; r2 < fTmp.len && REC_TOLERANCE > time[r2]; r2++);
            
                tI = -1; type = -1;
                if(r1 == r2) tI = r1;
                else if(r2-r1 == 1) {
                    hash = getHash(s, t);
                    tI = ((overlapSet.contains(hash))^(r1&1)==0)?r1:r2;
                }
                else {
                    System.out.println("warn:close col");
//                    if(Log.isEnabled()) Log.log(Log.red, "_rec multiple {}, {}, s: {}, t: {}", r1, r2, s, t);
                    hash = getHash(s, t);
                    Integer tII = specSet.get(hash);
                    if(tII == null) {
                        tI = ((overlapSet.contains(hash))^(r1&1)==0)?r1:(r1+1);
                        specSet.put(hash, tI);
                    }
                    else tI = tII;
                    type = tI;
                    if(tI >= fTmp.len) continue;
                }
//                if(Log.isEnabled()) Log.log(Log.purple, "{}<={}, {}, {}, t {}", time[0], t.movingFor, minTime[0], s.limit, t);
//                if(Log.isEnabled()) Log.log(Log.purple, "t.t{}, t.m{}", t.travelled,t.movingFor);

                   
                
                if(tI == -1 || tI == fTmp.len || (Math.max(time[tI],0f) >= t.movingFor && s.willCollide!=t) || time[tI] >= minTime[0]) 
                    continue;
                
//                if(time[0] < t.movingFor || s.willCollide==t) {}
//                else throw new IllegalArgumentException();
//                if(time[tI] < minTime[0]) {}
//                else throw new IllegalArgumentException();
                 
                minTime[0] = time[tI];
                minTime[1] = (tI+1==fTmp.len)?Float.MAX_VALUE:time[tI+1];
                minType = type;
                min = t;
                store.copy.setFlags(fTmp);
            }
        }
        if(min != null && minTime[0] <= s.limit) {
            store.minS = s;
            store.minT = min;
            store.type = minType;
            if(minTime[0] < 0) {
                minTime[0] = 0;
                if(minTime[1] < 0) minTime[1] = 0;
            }
//            if(Log.isEnabled()) Log.log(Color.gray, "Store {}, {}" ,s.id, store.minT.id);
        }
    }
    public static class CalcResult implements Comparable<CalcResult> {
        public Shape shape;
        public float[] time = new float[2];
        public CalcResult() {}
        public CalcResult(Shape shape, float f, float t) {
            this.shape = shape;
            time[0] = f;
            time[1] = t;
        }
        public int compareTo(CalcResult c) {
            if(time[0] < c.time[0]) return -1;
            if(time[0] == c.time[0]) return 0;
            return 1;
        }
    }
    public List<CalcResult> calcFor(Shape s, List<CalcResult> store) {
        if(store == null) store = new ArrayList<CalcResult>();
        if(s.travelled != 0) s.apply(s.travelled);
        //store.clear(s.limit);
        store.clear();
        final Results fTmp = this.fTmp;
        final float[] time = fTmp.time;
        Shape t;
        ArrayList<? extends Shape> list = spheres;
        int r1, r2, tI, type, minType=-1;
        long hash = 0;
        final GroupListener grp = groupListener;
        for(int z = s.isStatic?0:1; z >= 0; z--, list = statics)
        for(int i = 0; i < list.size(); i++) {
            t = list.get(i);
            if(s.id == t.id || grp.areInGroup(s, t)) {
//                Log.log("Skip " + s.id + ", " + t.id, Log.orange);
                continue;
            }
            calcFor(s, t, fTmp);
            
            
//            if(Log.isEnabled()) Log.log(Color.GRAY, "Rec ({},{}){}", s.id, t.id, (fTmp.len==0?"null":Arrays.toString(time)));
            if(fTmp.len != 0) {
                for(r1 = 0; r1 < fTmp.len && -REC_TOLERANCE > time[r1]; r1++); 
                if(r1 == fTmp.len) continue;
                for(r2 = r1; r2 < fTmp.len && REC_TOLERANCE > time[r2]; r2++);
            
                tI = -1; type = -1;
                if(r1 == r2) tI = r1;
                else if(r2-r1 == 1) {
                    hash = getHash(s, t);
                    tI = ((overlapSet.contains(hash))^(r1&1)==0)?r1:r2;
                }
                else {
//                    if(Log.isEnabled()) Log.log(Log.red, "_rec multiple {}, {}, s: {}, t: {}", r1, r2, s, t);
                    hash = getHash(s, t);
                    Integer tII = specSet.get(hash);
                    if(tII == null) {
                        tI = ((overlapSet.contains(hash))^(r1&1)==0)?r1:(r1+1);
                        specSet.put(hash, tI);
                    }
                    else tI = tII;
                    type = tI;
                    if(tI >= fTmp.len) continue;
                }
                if(tI == -1 || tI == fTmp.len /*NOTE:*/ 
                        /*|| (time[tI] >= t.movingFor && s.willCollide!=t) || time[tI] >= minTime[0]*/) 
                    continue;
                store.add(new CalcResult(t, time[tI], (tI+1==fTmp.len)?Float.MAX_VALUE:time[tI+1]));
            }
        }
        return store;
    }
    public void setLimit(Shape s, float limit) {
        if(s.travelled < limit) {
            if(s.willCollide == null) {
                s.setLimit(limit);
                updateClosest(s);
                //debug
                getNextCollisionTime();
                //
            }
            else {
                //revisit this, maybe implement by group
                s.limit = limit;
                if(s.movingFor >= limit) {
                      if(s.willCollide != null) applyRecalc(s.willCollide);
                }
            }
        }
        else {
            //instacollision
            s.limit = Float.MAX_VALUE;
            onLimit(s);
            applyRecalc(s);
        }
    }
    /**
     * Sets the velocity of an object added to this Accelerated instance 
     * and recalculates any collision changes.
     * Use when SphereShape instance is added to this instance.
     * For changing velocity of an object after impact and otherwise use
     * {@link accel.shapes.SphereShape#initVelocity(float, float, float) }.
     */
    public void setVelocity(Shape s, float x, float y, float z) {
        //first update the current position
        s.apply(s.travelled);
        s.vel(x,y,z);
        
        applyRecalc(s);
    }
    public void setAcceleration(Shape s, float x, float y, float z) {
        s.apply(s.travelled);
        s.acc(x,y,z);
        applyRecalc(s);
    }
    public static void calcFor(Shape s1, Shape s2, Results store) {   
        store.len = 0;
        if(s1.travelled != 0f) s1.apply(s1.travelled);
        if(s2.travelled != 0f) s2.apply(s2.travelled);
        if(s1.getType()>=s2.getType()) s1.calcTime(s2, store);
        else s2.calcTime(s1, store);
        
        if(store.len == 1) {
            store.len = 2;
            store.time[1] = store.time[0];
        }
    }
    public static boolean intersects(Shape s1, Shape s2) {
        if(s1.travelled != 0f) s1.apply(s1.travelled);
        if(s2.travelled != 0f) s2.apply(s2.travelled);
        return (s1.getType()>=s2.getType())?s1.intersects(s2):s2.intersects(s1);
    }
    public void setImpactListener(ImpactListener l) {
        impactListener = l;
    }
    public void onLimit(Shape s) {
        impactListener.onLimit(s);
    }
    public static boolean fireEvent(Shape s) {
        if(s.userObj instanceof LimitEvent) {
            LimitEvent evt = ((LimitEvent)s.userObj);
            s.userObj = null;
            evt.onLimit(s);
            return true;
        }
        return false;
    }
}
