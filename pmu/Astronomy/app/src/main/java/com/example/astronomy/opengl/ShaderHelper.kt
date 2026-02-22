package com.example.astronomy.opengl

object ShaderHelper {
    // Вершинный шейдер с освещением
    val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        uniform mat4 uModelMatrix;     // для преобразования нормалей
        attribute vec4 aPosition;
        attribute vec3 aNormal;
        varying vec3 vNormal;
        varying vec3 vPosition;
        
        void main() {
            gl_Position = uMVPMatrix * aPosition;
            vPosition = vec3(uModelMatrix * aPosition);
            vNormal = vec3(uModelMatrix * vec4(aNormal, 0.0));
        }
    """.trimIndent()

    // Фрагментный шейдер с моделью Фонга
    val fragmentShaderCode = """
        precision mediump float;
        uniform vec4 uColor;
        uniform vec3 uLightPosition;
        varying vec3 vNormal;
        varying vec3 vPosition;
        
        void main() {
            vec3 normal = normalize(vNormal);
            vec3 lightDir = normalize(uLightPosition - vPosition);
            vec3 viewDir = vec3(0.0, 0.0, 1.0); // упрощенно
            
            // Ambient (фоновое)
            float ambient = 0.3;
            
            // Diffuse (рассеянный свет)
            float diffuse = max(dot(normal, lightDir), 0.0);
            
            // Specular (блики)
            vec3 reflectDir = reflect(-lightDir, normal);
            float specular = pow(max(dot(viewDir, reflectDir), 0.0), 32.0);
            
            vec3 finalColor = uColor.rgb * (ambient + diffuse + specular * 0.5);
            gl_FragColor = vec4(finalColor, uColor.a);
        }
    """.trimIndent()
}