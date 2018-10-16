package com.skateboard.helicalcircle

import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.cos
import kotlin.math.sin

class HelicalCircle(private var radius:Float,private var precision:Int=20) {


    private val vertexSlgl="#version 300 es\n" +
            "\n" +
            "layout(location=0) in vec3 pos;\n" +
            "uniform mat4 model;\n" +
            "uniform mat4 view;\n" +
            "uniform mat4 projection;\n" +
            "void main()\n" +
            "{\n" +
            "   gl_Position=projection*view*model*vec4(pos,1.0);\n" +
            "}"

    private val fragmentSlgl="#version 300 es\n" +
            "precision mediump float;\n" +
            "uniform vec4 aColor;\n" +
            "out vec4 fragColor;\n" +
            "void main()\n" +
            "{\n" +
            "   fragColor=aColor;\n" +
            "}"

    private var programId = 0

    private var VAO = 0

    private var VBO = 0

    private var posList = mutableListOf<Float>()

    private val TAG = "HelicalCircleRender"

    private lateinit var pos:FloatArray

    private var modelMatrix=FloatArray(16)

    private var viewMatrix=FloatArray(16)

    private var projectionMatrix=FloatArray(16)

    var model=GLES30.GL_LINE_STRIP

    var degree:Double=0.0

    fun prepare()
    {
        preparePosList()
        prepareProgram()
    }

    private fun preparePosList() {
        var x0 = 0f
        var y0 = 0f
        var z0 = radius
        var x1 = 0f
        var y1 = 0f
        var z1 = radius

        val h = 360 / precision

        val v = 360 / precision

        for (i in 0 until h) {
            val arc0 = Math.PI / 180 * i * precision
            val arc1 = Math.PI / 180 * (i + 1) * precision
            y0 = (radius * Math.cos(arc0)).toFloat()
            y1 = (radius * Math.cos(arc1)).toFloat()
            for (j in 0 until v) {
                val arc = Math.PI / 180 * j * precision
                x0 = (radius * Math.sin(arc0) * sin(arc)).toFloat()
                z0 = (radius * Math.sin(arc0) * cos(arc)).toFloat()
                x1 = (radius * Math.sin(arc1) * sin(arc)).toFloat()
                z1 = (radius * Math.sin(arc1) * cos(arc)).toFloat()

                posList.add(x0)
                posList.add(y0)
                posList.add(z0)
                posList.add(x1)
                posList.add(y1)
                posList.add(z1)
            }
        }

        pos= posList.toFloatArray()
    }

    private fun prepareProgram()
    {
        programId = createProgram(vertexSlgl, fragmentSlgl)
        val vaoArray = IntArray(1)
        GLES30.glGenVertexArrays(1, vaoArray, 0)
        VAO = vaoArray[0]
        GLES30.glBindVertexArray(VAO)
        val vboArray = IntArray(1)
        GLES30.glGenBuffers(1, vboArray, 0)
        VBO = vboArray[0]
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, VBO)
        val posBuffer = ByteBuffer.allocateDirect(4 * pos.size).order(ByteOrder.nativeOrder()).asFloatBuffer()
        posBuffer.put(pos)
        posBuffer.position(0)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, 4 * pos.size, posBuffer, GLES30.GL_STATIC_DRAW)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 3 * 4, 0)
        GLES30.glEnableVertexAttribArray(0)
    }

    private fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = createShader(GLES30.GL_VERTEX_SHADER, vertexSource)
        val fragmentShader = createShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource)
        return createProgram(vertexShader, fragmentShader)
    }


    private fun createShader(type: Int, source: String): Int {
        val shaderId = GLES30.glCreateShader(type)
        if (shaderId <= 0) {
            Log.d(TAG, "create shader failed")
        }
        GLES30.glShaderSource(shaderId, source)
        GLES30.glCompileShader(shaderId)
        val status = IntArray(1)
        GLES30.glGetShaderiv(shaderId, GLES30.GL_COMPILE_STATUS, status, 0)
        if (status[0] <= 0) {
            Log.d(TAG, "compile shader failed")
            val infoLog = GLES30.glGetShaderInfoLog(shaderId)
            Log.d(TAG, infoLog)
            GLES30.glDeleteShader(shaderId)
        }
        return shaderId
    }

    private fun createProgram(vertexShader: Int, fragmentShader: Int): Int {
        val programId = GLES30.glCreateProgram()
        if (programId <= 0) {
            Log.d(TAG, "create program failed")
        }
        GLES30.glAttachShader(programId, vertexShader)
        GLES30.glAttachShader(programId, fragmentShader)
        GLES30.glLinkProgram(programId)
        val status = IntArray(1)
        GLES30.glGetProgramiv(programId, GLES30.GL_LINK_STATUS, status, 0)
        if (status[0] <= 0) {
            Log.d(TAG, "link program failed")
            val infoLog = GLES30.glGetProgramInfoLog(programId)
            Log.d(TAG, infoLog)
        }
        return programId
    }


    fun rotate(degree:Double)
    {
        this.degree=degree%360
    }

    fun draw(aspect:Float,r:Float=0.0f,g:Float=0.0f,b:Float=1.0f,a:Float=0.5f,scale:Float=1.0f)
    {
        degree+=10
        GLES30.glUseProgram(programId)
        val colorLocation=GLES30.glGetUniformLocation(programId,"aColor")
        GLES30.glUniform4f(colorLocation,r,g,b,a)
        val modelMatrixLocation=GLES30.glGetUniformLocation(programId,"model")
        Matrix.setIdentityM(modelMatrix,0)
        Matrix.scaleM(modelMatrix,0,scale,scale,scale)
        Matrix.rotateM(modelMatrix,0, Math.toRadians(degree).toFloat(),0f,1f,0f)
        GLES30.glUniformMatrix4fv(modelMatrixLocation,1,false,modelMatrix,0)
        val viewMatrixLocation=GLES30.glGetUniformLocation(programId,"view")
        Matrix.setLookAtM(viewMatrix,0,0f,0f,10f,0f,0f,0f,0f,1f,0f)
        GLES30.glUniformMatrix4fv(viewMatrixLocation,1,false,viewMatrix,0)
        val projectionMatrixLocation=GLES30.glGetUniformLocation(programId,"projection")
        Matrix.perspectiveM(projectionMatrix,0,45.0f, aspect,0.1f,100f)
        GLES30.glUniformMatrix4fv(projectionMatrixLocation,1,false,projectionMatrix,0)
        GLES30.glDrawArrays(model,0,pos.size / 3)
    }

}