package com.skateboard.helicalcircle

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class HelicalCircleRender(private val context: Context, private var radius: Float, private var precision: Int = 10) :
    GLSurfaceView.Renderer {


    private var helicalCircle: HelicalCircle = HelicalCircle(radius, precision)

    private var screenWidth = 0f

    private var screenHeight = 0f

    var scale=0f
    set(value) {

        field=Math.min(value,1.0f)
        field=Math.max(field,0f)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

        helicalCircle.prepare()

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        screenWidth = width.toFloat()
        screenHeight = height.toFloat()
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glClearColor(0f,0f,0f,1.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        val aspect=screenWidth / screenHeight
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)
        helicalCircle.model=GLES30.GL_TRIANGLE_STRIP
        helicalCircle.draw(aspect,r=0.67f,g=0.85f,b=0.98f,a=0.5f,scale = 0.99f*scale)
        helicalCircle.draw(aspect,r=0.85f,g=0.96f,b=0.99f,a=0.5f,scale= 0.5f*scale)
        helicalCircle.draw(aspect,r=0.97f,g=1.0f,b=0.99f,a=0.5f,scale= 0.3f*scale)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        helicalCircle.model=GLES30.GL_LINE_STRIP
        helicalCircle.draw(aspect,r=1.0f,g=1.0f,b=1.0f,a=0.2f,scale = 0.998f*scale)
    }

}