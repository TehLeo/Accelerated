/*
 * Copyright (c) 2016, Juraj Papp
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the copyright holder nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package tests.theleo.accel.examples;

import theleo.accel.Accelerated;
import theleo.accel.Results;
import theleo.accel.shapes.SphereShape;

/**
 *
 * @author Juraj Papp
 */
public class PredictionExample {
    public static void main(String[] args) {
        SphereShape arrow = new SphereShape(0.5f);
        arrow.pos(0.2f,0.3f,0.1f);
        arrow.vel(0.5f, 0, 0);
        
        SphereShape character = new SphereShape(1.0f);
        character.pos(5.0f,0.0f,0.0f);
        
        Results res = new Results();
        Accelerated.calcFor(arrow, character, res);
        
                    
        res.min(0.0f); // Optionally filter for only future collision, not interested in past one.
        if(res.isEmpty()) {
            System.out.println("Objects will not collide.");
        }
        else {
            System.out.println("Objects collide at " + res.toString());
        }
    }
}
