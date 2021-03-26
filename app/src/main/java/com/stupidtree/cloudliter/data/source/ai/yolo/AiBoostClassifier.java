/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.stupidtree.cloudliter.data.source.ai.yolo;

import android.app.Application;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.media.Image;
import android.os.Build;
import android.util.Log;

import com.ai.aiboost.AiBoostInterpreter;
import com.stupidtree.accessibility.ai.ImageUtils;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.nnapi.NnApiDelegate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Vector;


public class AiBoostClassifier implements Classifier {

    public static Classifier create(
            final Application context,
            final AssetManager assetManager,
            final String modelFilename,
            final String labelFilename,
            final boolean isQuantized)
            throws IOException {
        final AiBoostClassifier d = new AiBoostClassifier();

        String actualFilename = labelFilename.split("file:///android_asset/")[1];
        InputStream labelsInput = assetManager.open(actualFilename);
        BufferedReader br = new BufferedReader(new InputStreamReader(labelsInput));
        String line;
        while ((line = br.readLine()) != null) {
            d.labels.add(line);
        }
        br.close();
        try {
            AiBoostInterpreter.Options options = new AiBoostInterpreter.Options();
            options.setNumThreads(NUM_THREADS);
            options.setDeviceType(AiBoostInterpreter.Device.QUALCOMM_DSP);
            options.setQComPowerLevel(AiBoostInterpreter.QCOMPowerLEVEL.QCOM_TURBO);
            options.setNativeLibPath(context.getApplicationInfo().nativeLibraryDir);
            InputStream input = assetManager.open(modelFilename);
            int length = input.available();
            byte[] buffer = new byte[length];
            input.read(buffer);
            ByteBuffer modelBuf = ByteBuffer.allocateDirect(length);
            modelBuf.order(ByteOrder.nativeOrder());
            modelBuf.put(buffer);
            int[][] input_shapes = new int[][]{{BATCH_SIZE, INPUT_SIZE, INPUT_SIZE, PIXEL_SIZE}};
            d.aiBoost = new AiBoostInterpreter(modelBuf, input_shapes, options);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return d;
    }

    @Override
    public void enableStatLogging(final boolean logStats) {
    }

    @Override
    public String getStatString() {
        return "";
    }

    @Override
    public void close() {
    }

    @Override
    public void setNumThreads(int num_threads) {

    }

    @Override
    public void setUseNNAPI(boolean isChecked) {

    }


    @Override
    public float getObjThresh() {
        return 0.5f;
    }


    private static final int INPUT_SIZE = 416;
    private static final int NUM_THREADS = 1;
    private static final int[] OUTPUT_WIDTH_TINY = new int[]{2535, 2535};

    private final Vector<String> labels = new Vector<String>();


    private AiBoostInterpreter aiBoost;

    private AiBoostClassifier() {
    }

    //non maximum suppression
    protected ArrayList<Recognition> nms(ArrayList<Recognition> list) {
        ArrayList<Recognition> nmsList = new ArrayList<Recognition>();

        for (int k = 0; k < labels.size(); k++) {
            //1.find max confidence per class
            PriorityQueue<Recognition> pq =
                    new PriorityQueue<Recognition>(
                            50,
                            new Comparator<Recognition>() {
                                @Override
                                public int compare(final Recognition lhs, final Recognition rhs) {
                                    // Intentionally reversed to put high confidence at the head of the queue.
                                    return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                                }
                            });

            for (int i = 0; i < list.size(); ++i) {
                if (list.get(i).getDetectedClass() == k) {
                    pq.add(list.get(i));
                }
            }

            //2.do non maximum suppression
            while (pq.size() > 0) {
                //insert detection with max confidence
                Recognition[] a = new Recognition[pq.size()];
                Recognition[] detections = pq.toArray(a);
                Recognition max = detections[0];
                nmsList.add(max);
                pq.clear();

                for (int j = 1; j < detections.length; j++) {
                    Recognition detection = detections[j];
                    RectF b = detection.getLocation();
                    if (box_iou(max.getLocation(), b) < mNmsThresh) {
                        pq.add(detection);
                    }
                }
            }
        }
        return nmsList;
    }

    protected float mNmsThresh = 0.6f;

    protected float box_iou(RectF a, RectF b) {
        return box_intersection(a, b) / box_union(a, b);
    }

    protected float box_intersection(RectF a, RectF b) {
        float w = overlap((a.left + a.right) / 2, a.right - a.left,
                (b.left + b.right) / 2, b.right - b.left);
        float h = overlap((a.top + a.bottom) / 2, a.bottom - a.top,
                (b.top + b.bottom) / 2, b.bottom - b.top);
        if (w < 0 || h < 0) return 0;
        return w * h;
    }

    protected float box_union(RectF a, RectF b) {
        float i = box_intersection(a, b);
        return (a.right - a.left) * (a.bottom - a.top) + (b.right - b.left) * (b.bottom - b.top) - i;
    }

    protected float overlap(float x1, float w1, float x2, float w2) {
        float l1 = x1 - w1 / 2;
        float l2 = x2 - w2 / 2;
        float left = Math.max(l1, l2);
        float r1 = x1 + w1 / 2;
        float r2 = x2 + w2 / 2;
        float right = Math.min(r1, r2);
        return right - left;
    }

    protected static final int BATCH_SIZE = 1;
    protected static final int PIXEL_SIZE = 3;

    /**
     * Writes Image data into a {@code ByteBuffer}.
     */
    protected ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * BATCH_SIZE * INPUT_SIZE * INPUT_SIZE * PIXEL_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[INPUT_SIZE * INPUT_SIZE];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        for (int i = 0; i < INPUT_SIZE; ++i) {
            for (int j = 0; j < INPUT_SIZE; ++j) {
                final int val = intValues[pixel++];
                byteBuffer.putFloat(((val >> 16) & 0xFF) / 255.0f);
                byteBuffer.putFloat(((val >> 8) & 0xFF) / 255.0f);
                byteBuffer.putFloat((val & 0xFF) / 255.0f);
            }
        }
        return byteBuffer;
    }


    private ArrayList<Recognition> getDetectionsForTiny(ByteBuffer byteBuffer, Bitmap bitmap) {
        ArrayList<Recognition> detections = new ArrayList<>();
        Map<Integer, Object> outputMap = new HashMap<>();
        outputMap.put(0, ByteBuffer.allocate(OUTPUT_WIDTH_TINY[0] * 4 * 4));
        outputMap.put(1, ByteBuffer.allocate(OUTPUT_WIDTH_TINY[1] * labels.size()*4));
        Object[] inputArray = {byteBuffer};
        aiBoost.getInputTensor(0).rewind();
        aiBoost.getOutputTensor(0).rewind();
        aiBoost.getOutputTensor(1).rewind();
        aiBoost.runForMultipleInputsOutputs(inputArray, outputMap);
        byte[] boxBuf = ((ByteBuffer) outputMap.get(0)).array();
        byte[] scoreBuf = ((ByteBuffer) outputMap.get(1)).array();
        float[][] bboxes = new float[OUTPUT_WIDTH_TINY[0]][4];
        int k=0;
        for(int i=0;i<OUTPUT_WIDTH_TINY[0];i++){
            for(int j=0;j<4;j++){
                bboxes[i][j] = ImageUtils.INSTANCE.getFloat(boxBuf,4*(k++),true);
            }
        }
        float[][] out_score  = new float[OUTPUT_WIDTH_TINY[1]][labels.size()];
        k=0;
        for(int i=0;i<OUTPUT_WIDTH_TINY[1];i++){
            for(int j=0;j<labels.size();j++){
                out_score[i][j] = ImageUtils.INSTANCE.getFloat(scoreBuf,4*(k++),true);
            }
        }
        for (int i = 0; i < 20; i++) {
            float maxClass = 0;
            int detectedClass = -1;
            final float[] classes = new float[labels.size()];
            System.arraycopy(out_score[i], 0, classes, 0, labels.size());
            for (int c = 0; c < labels.size(); ++c) {
                if (classes[c] > maxClass) {
                    detectedClass = c;
                    maxClass = classes[c];
                }
            }
            final float score = maxClass;
            if (score > getObjThresh()) {
                final float xPos = bboxes[i][0];
                final float yPos = bboxes[i][1];
                final float w = bboxes[i][2];
                final float h = bboxes[i][3];
                final RectF rectF = new RectF(
                        Math.max(0, xPos - w / 2),
                        Math.max(0, yPos - h / 2),
                        Math.min(bitmap.getWidth() - 1, xPos + w / 2),
                        Math.min(bitmap.getHeight() - 1, yPos + h / 2));
                detections.add(new Recognition("" + i, labels.get(detectedClass), score, rectF, detectedClass));
            }
        }
        return detections;
    }

    public ArrayList<Recognition> recognizeImage(Bitmap bitmap) {
        ByteBuffer byteBuffer =  convertBitmapToByteBuffer(bitmap);
        ArrayList<Recognition> detections = getDetectionsForTiny(byteBuffer, bitmap);
        return nms(detections);
    }

}