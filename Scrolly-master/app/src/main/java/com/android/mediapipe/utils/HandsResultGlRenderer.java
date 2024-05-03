package com.android.mediapipe.utils;

import android.content.Intent;
import android.opengl.GLES20;
import android.os.Build;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.android.mediapipe.service.SmartAutoClickerService;
import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.solutioncore.ResultGlRenderer;
import com.google.mediapipe.solutions.hands.HandsResult;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class HandsResultGlRenderer implements ResultGlRenderer<HandsResult> {
    private final String TAG = "Hands  ResultGlRenderer";


    //    GlobalActionBarService globalActionBarService = GlobalActionBarService.getSharedInstance();
    SmartAutoClickerService.LocalService localService =
            SmartAutoClickerService.Companion.getLocalServiceInstance();

    private final float[] LEFT_HAND_CONNECTION_COLOR = new float[]{0.2f, 1f, 0.2f, 1f};
    private final float[] RIGHT_HAND_CONNECTION_COLOR = new float[]{2f, 0.1f, 0.21f, 1f};
    private final float CONNECTION_THICKNESS = 20.0f;
    private final float[] LEFT_HAND_HOLLOW_CIRCLE_COLOR = new float[]{0.2f, 1f, 0.2f, 1f};
    private final float[] RIGHT_HAND_HOLLOW_CIRCLE_COLOR = new float[]{1f, 0.2f, 0.2f, 1f};
    private final float HOLLOW_CIRCLE_RADIUS = 0.01f;
    private final float[] LEFT_HAND_LANDMARK_COLOR = new float[]{1f, 0.2f, 0.2f, 1f};
    private final float[] RIGHT_HAND_LANDMARK_COLOR = new float[]{0.2f, 1f, 0.2f, 1f};
    private final float LANDMARK_RADIUS = 0.008f;
    private final int NUM_SEGMENTS = 120;
    private final String VERTEX_SHADER =
            "uniform mat4 uProjectionMatrix;\n"
                    + "attribute vec4 vPosition;\n"
                    + "void main() {\n"
                    + "  gl_Position = uProjectionMatrix * vPosition;\n"
                    + "}";
    private final String FRAGMENT_SHADER =
            "precision mediump float;\n"
                    + "uniform vec4 uColor;\n"
                    + "void main() {\n"
                    + "  gl_FragColor = uColor;\n"
                    + "}";
    private int program;
    private int positionHandle;
    private int projectionMatrixHandle;
    private int colorHandle;

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    @Override
    public void setupRendering() {
        program = GLES20.glCreateProgram();
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
        positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        projectionMatrixHandle = GLES20.glGetUniformLocation(program, "uProjectionMatrix");
        colorHandle = GLES20.glGetUniformLocation(program, "uColor");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void renderResult(HandsResult result, float[] projectionMatrix) {
        if (result == null) {
            return;
        }
        GLES20.glUseProgram(program);
        GLES20.glUniformMatrix4fv(projectionMatrixHandle, 1, false, projectionMatrix, 0);
        GLES20.glLineWidth(CONNECTION_THICKNESS);

        int numHands = result.multiHandLandmarks().size();
        for (int i = 0; i < numHands; ++i) {
            boolean isLeftHand = result.multiHandedness().get(i).getLabel().equals("Left");


            // Drawing single finger forefinger
            LandmarkProto.NormalizedLandmark forefingerlandmark = result.multiHandLandmarks().get(i).getLandmarkList().get(8);
            drawCircle(
                    forefingerlandmark.getX(),
                    forefingerlandmark.getY(),
                    isLeftHand ? LEFT_HAND_LANDMARK_COLOR : RIGHT_HAND_LANDMARK_COLOR);

            drawHollowCircle(
                    forefingerlandmark.getX(),
                    forefingerlandmark.getY(),
                    isLeftHand ? LEFT_HAND_HOLLOW_CIRCLE_COLOR : RIGHT_HAND_HOLLOW_CIRCLE_COLOR);

            // for middle finger
            LandmarkProto.NormalizedLandmark middlelandmark = result.multiHandLandmarks().get(i).getLandmarkList().get(12);
            drawCircle(
                    middlelandmark.getX(),
                    middlelandmark.getY(),
                    isLeftHand ? LEFT_HAND_LANDMARK_COLOR : RIGHT_HAND_LANDMARK_COLOR);

            drawHollowCircle(
                    middlelandmark.getX(),
                    middlelandmark.getY(),
                    isLeftHand ? LEFT_HAND_HOLLOW_CIRCLE_COLOR : RIGHT_HAND_HOLLOW_CIRCLE_COLOR);

            //for thumb points
            LandmarkProto.NormalizedLandmark thumbLandmark = result.multiHandLandmarks().get(i).getLandmarkList().get(4);
            drawCircle(
                    thumbLandmark.getX(),
                    thumbLandmark.getY(),
                    isLeftHand ? LEFT_HAND_LANDMARK_COLOR : RIGHT_HAND_LANDMARK_COLOR);

            drawHollowCircle(
                    thumbLandmark.getX(),
                    thumbLandmark.getY(),
                    isLeftHand ? LEFT_HAND_HOLLOW_CIRCLE_COLOR : RIGHT_HAND_HOLLOW_CIRCLE_COLOR);
            // forefinger bottom part
            LandmarkProto.NormalizedLandmark forefingerbottomLandmark = result.multiHandLandmarks().get(i).getLandmarkList().get(6);
            drawCircle(
                    forefingerbottomLandmark.getX(),
                    forefingerbottomLandmark.getY(),
                    isLeftHand ? LEFT_HAND_LANDMARK_COLOR : RIGHT_HAND_LANDMARK_COLOR);

            drawHollowCircle(
                    forefingerbottomLandmark.getX(),
                    forefingerbottomLandmark.getY(),
                    isLeftHand ? LEFT_HAND_HOLLOW_CIRCLE_COLOR : RIGHT_HAND_HOLLOW_CIRCLE_COLOR);


            double forefingerAndBottom = Math.sqrt(Math.pow(forefingerlandmark.getX() - forefingerbottomLandmark.getX(), 2)
                    + Math.pow(forefingerlandmark.getY() - forefingerbottomLandmark.getY(), 2)
                    + Math.pow(forefingerlandmark.getZ() - forefingerbottomLandmark.getZ(), 2));

            double forfingerToThumb = Math.sqrt(Math.pow(forefingerlandmark.getX() - thumbLandmark.getX(), 2)
                    + Math.pow(forefingerlandmark.getY() - thumbLandmark.getY(), 2)
                    + Math.pow(forefingerlandmark.getZ() - thumbLandmark.getZ(), 2));

            double middlefingerToThumb = Math.sqrt(Math.pow(middlelandmark.getX() - thumbLandmark.getX(), 2)
                    + Math.pow(middlelandmark.getY() - thumbLandmark.getY(), 2)
                    + Math.pow(middlelandmark.getZ() - thumbLandmark.getZ(), 2));



//            localService.start();
            if (localService != null) {
                Log.d("bottomlength", "renderResult: "+ forefingerAndBottom + (forefingerAndBottom < 0.05085614680255601));

                if (forfingerToThumb < 0.050906282163080734) {
                    Log.d("CheckScroll", "renderResultkkkkkkkkk: ");
//                    globalActionBarService.configureScrollButtonUp();
                    localService.configureScrollButtonUp();
//

                }
                if (middlefingerToThumb < 0.050906282163080734) {
                    Log.d("CheckScroll", "renderResultkkkkkkkkk: ");
//                    globalActionBarService.configureScrollButtonDown();
                    localService.configureScrollButtonDown();
//

                }
                if (forefingerAndBottom < 0.05075614680255601){
                    localService.configTap();
                }
            } else {
//                globalActionBarService = new GlobalActionBarService();
                Log.d("CheckScroll", "renderResult:Error ");
            }
            Log.d(TAG, "L1 X-" + forefingerlandmark.getX() + "L1 Y" + forefingerlandmark.getY());
            Log.d(TAG, "L2 X-" + middlelandmark.getX() + "L2 Y" + middlelandmark.getY());
            Log.d(TAG, "L2 X-" + thumbLandmark.getX() + "L2 Y" + thumbLandmark.getY());

        }
    }


    private void drawCircle(float x, float y, float[] colorArray) {
        GLES20.glUniform4fv(colorHandle, 1, colorArray, 0);
        int vertexCount = NUM_SEGMENTS + 2;
        float[] vertices = new float[vertexCount * 3];
        vertices[0] = x;
        vertices[1] = y;
        vertices[2] = 0;
        for (int i = 1; i < vertexCount; i++) {
            float angle = 2.0f * i * (float) Math.PI / NUM_SEGMENTS;
            int currentIndex = 3 * i;
            vertices[currentIndex] = x + (float) (LANDMARK_RADIUS * Math.cos(angle));
            vertices[currentIndex + 1] = y + (float) (LANDMARK_RADIUS * Math.sin(angle));
            vertices[currentIndex + 2] = 0;
        }
        FloatBuffer vertexBuffer =
                ByteBuffer.allocateDirect(vertices.length * 4)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer()
                        .put(vertices);
        vertexBuffer.position(0);
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexCount);
    }

    private void drawHollowCircle(float x, float y, float[] colorArray) {
        GLES20.glUniform4fv(colorHandle, 1, colorArray, 0);
        int vertexCount = NUM_SEGMENTS + 1;
        float[] vertices = new float[vertexCount * 3];
        for (int i = 0; i < vertexCount; i++) {
            float angle = 2.0f * i * (float) Math.PI / NUM_SEGMENTS;
            int currentIndex = 3 * i;
            vertices[currentIndex] = x + (float) (HOLLOW_CIRCLE_RADIUS * Math.cos(angle));
            vertices[currentIndex + 1] = y + (float) (HOLLOW_CIRCLE_RADIUS * Math.sin(angle));
            vertices[currentIndex + 2] = 0;
        }
        FloatBuffer vertexBuffer =
                ByteBuffer.allocateDirect(vertices.length * 4)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer()
                        .put(vertices);
        vertexBuffer.position(0);
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, vertexCount);
    }
}



