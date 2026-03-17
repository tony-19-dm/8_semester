package com.example.astronomy.opengl

object ShaderHelper {
    // Вершинный шейдер для планет (с освещением)
    val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        uniform mat4 uModelMatrix;
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

    // Фрагментный шейдер для планет (с эмиссивной компонентой)
    val fragmentShaderCode = """
        precision mediump float;
        uniform vec4 uColor;
        uniform vec3 uLightPosition;
        uniform float uEmissive;
        varying vec3 vNormal;
        varying vec3 vPosition;
        
        void main() {
            vec3 normal = normalize(vNormal);
            vec3 lightDir = normalize(uLightPosition - vPosition);
            vec3 viewDir = vec3(0.0, 0.0, 1.0);
            
            // Ambient
            float ambient = 0.3;
            
            // Diffuse
            float diffuse = max(dot(normal, lightDir), 0.0);
            
            // Specular
            vec3 reflectDir = reflect(-lightDir, normal);
            float specular = pow(max(dot(viewDir, reflectDir), 0.0), 32.0);
            
            // Emissive (для Солнца uEmissive = 1.0, для планет = 0.0)
            vec3 emissive = uEmissive * uColor.rgb;
            
            vec3 finalColor = uColor.rgb * (ambient + diffuse + specular * 0.5) + emissive;
            gl_FragColor = vec4(finalColor, uColor.a);
        }
    """.trimIndent()

    // Вершинный шейдер для фона (простой, без освещения)
    val bgVertexShaderCode = """
        attribute vec4 aPosition;
        attribute vec2 aTexCoord;
        varying vec2 vTexCoord;
        uniform mat4 uMVPMatrix;
        void main() {
            gl_Position = uMVPMatrix * aPosition;
            vTexCoord = aTexCoord;
        }
    """.trimIndent()

    // Фрагментный шейдер для фона (только текстура)
    val bgFragmentShaderCode = """
        precision mediump float;
        varying vec2 vTexCoord;
        uniform sampler2D uTexture;
        void main() {
            gl_FragColor = texture2D(uTexture, vTexCoord);
        }
    """.trimIndent()

    val blackHoleVertexShader = """
        attribute vec4 aPosition;
        uniform mat4 uMVPMatrix;
        varying vec2 vPosition;
        void main() {
            gl_Position = uMVPMatrix * aPosition;
            vPosition = aPosition.xy;
        }
    """.trimIndent()

    val blackHoleFragmentShader = """
        precision mediump float;
        varying vec2 vPosition;
        uniform float uAlpha;
        void main() {
            float dist = length(vPosition);
            float alpha = 1.0 - dist * dist;
            alpha = clamp(alpha, 0.0, 1.0);
            gl_FragColor = vec4(0.0, 0.0, 0.0, alpha * uAlpha);
        }
    """.trimIndent()
    // Вершинный шейдер с текстурными координатами
    val textureVertexShaderCode = """
    uniform mat4 uMVPMatrix;
    uniform mat4 uModelMatrix;
    attribute vec4 aPosition;
    attribute vec3 aNormal;
    attribute vec2 aTexCoord;
    varying vec3 vNormal;
    varying vec3 vPosition;
    varying vec2 vTexCoord;
    
    void main() {
        gl_Position = uMVPMatrix * aPosition;
        vPosition = vec3(uModelMatrix * aPosition);
        vNormal = vec3(uModelMatrix * vec4(aNormal, 0.0));
        vTexCoord = aTexCoord;
    }
""".trimIndent()

    // Фрагментный шейдер с текстурой и освещением
    val textureFragmentShaderCode = """
    precision mediump float;
    uniform vec3 uLightPosition;
    uniform float uEmissive;
    uniform sampler2D uTexture;
    varying vec3 vNormal;
    varying vec3 vPosition;
    varying vec2 vTexCoord;
    
    void main() {
        vec3 normal = normalize(vNormal);
        vec3 lightDir = normalize(uLightPosition - vPosition);
        vec3 viewDir = vec3(0.0, 0.0, 1.0);
        
        // Ambient
        float ambient = 0.3;
        
        // Diffuse
        float diffuse = max(dot(normal, lightDir), 0.0);
        
        // Specular
        vec3 reflectDir = reflect(-lightDir, normal);
        float specular = pow(max(dot(viewDir, reflectDir), 0.0), 32.0);
        
        // Цвет из текстуры
        vec4 texColor = texture2D(uTexture, vTexCoord);
        
        // Эмиссивная компонента (для Солнца)
        vec3 emissive = uEmissive * texColor.rgb;
        
        vec3 finalColor = texColor.rgb * (ambient + diffuse + specular * 0.5) + emissive;
        gl_FragColor = vec4(finalColor, texColor.a);
    }
""".trimIndent()
}