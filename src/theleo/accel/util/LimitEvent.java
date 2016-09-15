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
package theleo.accel.util;

import theleo.accel.shapes.LineShape;
import theleo.accel.shapes.Shape;

/**
 * Interface to execute an action that will happen when
 * an objects reaches a set time limit.
 * 
 * To use events include if(Accelerated.fireEvent(s)) return;
 * in your implementation of ImpactListener.onLimit method.
 * 
 * To add limit event to an object:
 * shape.userObj = new LimitEvent() ...;
 * 
 * Set time with (if shape was not yet added to accelerated instance):
 * shape.setLimit(time);
 * 
 * Otherwise:
 * accelerated.setLimit(shape, time);
 * 
 * @author Juraj Papp
 */
public interface LimitEvent {
    public static final LimitEvent ZERO_VEL = new LimitEvent() {
        @Override
        public void onLimit(Shape s) {
            s.apply();
            s.vel(0, 0, 0);
        }
    };
    public static final LimitEvent ZERO = new LimitEvent() {
        @Override
        public void onLimit(Shape s) {
            s.apply();
            s.vel(0, 0, 0);
            s.acc(0, 0, 0);
        }
    };
    public static final LimitEvent ZERO_LINE = new LimitEvent() {
        @Override
        public void onLimit(Shape s) {
            s.apply();
            s.vel(0, 0, 0);
            s.acc(0, 0, 0);
            ((LineShape)s).applyDir();
        }
    };
    public void onLimit(Shape s);
    
}
