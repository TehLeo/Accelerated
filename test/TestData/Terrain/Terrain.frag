uniform vec4 m_Color;
varying vec4 vertColor;

void main() {
    vec4 color = vec4(1.0);
    color *= vertColor;
    color *= m_Color;
    gl_FragColor = color; 
}

