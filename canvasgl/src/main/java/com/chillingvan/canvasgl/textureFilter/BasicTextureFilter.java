/*
 *
 *  *
 *  *  * Copyright (C) 2016 ChillingVan
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *
 *
 */

package com.chillingvan.canvasgl.textureFilter;

import android.opengl.GLES20;

import com.chillingvan.canvasgl.ICanvasGL;
import com.chillingvan.canvasgl.glcanvas.BasicTexture;
import com.chillingvan.canvasgl.glcanvas.GLES20Canvas;

/**
 * Created by Matthew on 2016/10/14.
 */

public class BasicTextureFilter implements TextureFilter {


    public static final String MATRIX_UNIFORM = GLES20Canvas.MATRIX_UNIFORM;
    public static final String TEXTURE_MATRIX_UNIFORM = GLES20Canvas.TEXTURE_MATRIX_UNIFORM;
    public static final String POSITION_ATTRIBUTE = GLES20Canvas.POSITION_ATTRIBUTE;

    public static final String VARYING_TEXTURE_COORD = "vTextureCoord";
    private static final String UNIFORM_POSITIONX = "mPositionX";
    private static final String UNIFORM_POSITIONY = "mPositionY";
    private float mPositionX = 1.0f;
    private float mPositionY = 1.0f;
    // + "  gl_Position = " + MATRIX_UNIFORM + " * pos * vec2(" + UNIFORM_POSITIONX + "," + UNIFORM_POSITIONY+");\n"
   //         + "uniform float " + UNIFORM_POSITIONX + ";\n"
   //         + "uniform float " + UNIFORM_POSITIONY + ";\n"
    public static final String TEXTURE_VERTEX_SHADER = ""
            + "uniform mat4 " + MATRIX_UNIFORM + ";\n"
            + "uniform mat4 " + TEXTURE_MATRIX_UNIFORM + ";\n"
            + "attribute vec2 " + POSITION_ATTRIBUTE + ";\n"
            + "varying vec2 " + VARYING_TEXTURE_COORD + ";\n"
            + "void main() {\n"
            + "  vec4 pos = vec4(" + POSITION_ATTRIBUTE + ", 0.0, 1.0);\n"
            + "  vec4 pos1 = vec4(" + POSITION_ATTRIBUTE + "* vec2(1.0,0.872), 0.0, 1.0);\n"
            + "  gl_Position = " + MATRIX_UNIFORM + " * pos1;\n"
            + "  " + VARYING_TEXTURE_COORD + " = (" + TEXTURE_MATRIX_UNIFORM + " * pos).xy;\n"
            + "}\n";

    public static final String ALPHA_UNIFORM = GLES20Canvas.ALPHA_UNIFORM;
    public static final String TEXTURE_SAMPLER_UNIFORM = GLES20Canvas.TEXTURE_SAMPLER_UNIFORM;

    public static final String SAMPLER_2D = "sampler2D";
    public static final String TEXTURE_FRAGMENT_SHADER = ""
            + "precision mediump float;\n"
            + "varying vec2 " + VARYING_TEXTURE_COORD + ";\n"
            + "uniform float " + ALPHA_UNIFORM + ";\n"
            + "uniform " + SAMPLER_2D + " " + TEXTURE_SAMPLER_UNIFORM + ";\n"
            + "void main() {\n"
            + "  gl_FragColor = texture2D(" + TEXTURE_SAMPLER_UNIFORM + ", " + VARYING_TEXTURE_COORD + ");\n"
            + "  gl_FragColor *= " + ALPHA_UNIFORM + ";\n"
            + "}\n";
    public static final String SAMPLER_EXTERNAL_OES = "samplerExternalOES";

    @Override
    public String getVertexShader() {
        return TEXTURE_VERTEX_SHADER;
    }

    @Override
    public String getFragmentShader() {
        return TEXTURE_FRAGMENT_SHADER;
    }

    @Override
    public String getOesFragmentProgram() {
        return "#extension GL_OES_EGL_image_external : require\n" + getFragmentShader().replace(SAMPLER_2D, SAMPLER_EXTERNAL_OES);
    }

    @Override
    public void onPreDraw(int program, BasicTexture texture, ICanvasGL canvas) {
        //OpenGLUtil.setFloat(GLES20.glGetUniformLocation(program, UNIFORM_POSITIONX), mPositionX);
        //OpenGLUtil.setFloat(GLES20.glGetUniformLocation(program, UNIFORM_POSITIONY), mPositionY);
    }

    @Override
    public void destroy() {

    }

    public void setPositionX(float x){
        mPositionX = x;
    }

    public void setPositionY(float y){
        mPositionY = y;
    }
}
