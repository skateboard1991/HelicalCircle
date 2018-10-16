package com.skateboard.helicalcircle

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent


class HelicalCircleView(context: Context, attributeSet: AttributeSet?): GLSurfaceView(context,attributeSet) {

    constructor(context:Context):this(context,null)

    private var touchX=0f

    private val render=HelicalCircleRender(context,1.0f,10)

    init {
        setEGLContextClientVersion(3)
        setRenderer(render)
        renderMode=GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action)
        {
            MotionEvent.ACTION_DOWN->{

                touchX=event.x

            }

            MotionEvent.ACTION_MOVE->{

                val scaleOffset=(event.x-touchX)/width
                render.scale+=scaleOffset
                touchX=event.x
            }
        }

        return true
    }
}