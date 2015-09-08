package nopx.gaussiancameraanalyzer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;

import java.util.Random;


public class AnalyzedCameraPreview extends Activity {

    MySurfaceView mySurfaceView;

    int cameraWidth =640;
    int cameraHeight =360;
    CameraDevice cameraDevice;
    CameraCaptureSession cameraCaptureSession;
    CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
    //
    CameraCaptureSession.StateCallback sessionCallBack = new CameraCaptureSession.StateCallback(){
        @Override
        public void onConfigureFailed(CameraCaptureSession session){
            //TODO HANDLE SESSIONFAIL
            try{
                cameraCaptureSession.close();
            }
            catch(Exception e){}
        }
        @Override
        public void onConfigured(CameraCaptureSession session){
            cameraCaptureSession = session;
        }
    };
    //
    CameraDevice.StateCallback deviceCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            try {
                cameraDevice.close();
            }
            catch(Exception e){}
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            //TODO HANDLE DEVICEOPENING ERROR
            try{
                cameraDevice.close();
            }
            catch(Exception e){}
        }
    };
    //Field_End_____________________________________________________________________________________


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mySurfaceView = new MySurfaceView(this);
        setContentView(mySurfaceView);

        try {
            String[] cameraIds = manager.getCameraIdList();
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraIds[0]);
            StreamConfigurationMap configs = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            configs.getOutputFormats()
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        finally{
            cameraDevice.close();
        }
    }

    protected void onResume(){
        super.onResume();
        mySurfaceView.onResumeMySurfaceView();
    }

    protected void onPause(){
        super.onPause();
        mySurfaceView.onPauseMySurfaceView();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        this.findViewById(android.R.id.content).setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    class MySurfaceView extends SurfaceView implements Runnable {

        Thread thread = null;
        SurfaceHolder surfaceHolder;
        volatile boolean running = false;

        private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        public MySurfaceView(Context context) {
            super(context);
            surfaceHolder = getHolder();
        }

        public void onResumeMySurfaceView() {
            running = true;
            for(int i =0; i < 100; i++){
                if(surfaceHolder.getSurface().isValid()) {
                    Canvas canvas = surfaceHolder.lockCanvas();
                    Paint paint = new Paint();
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(Color.MAGENTA);
                    canvas.drawPaint(paint);
                    surfaceHolder.unlockCanvasAndPost(canvas);
                    break;
                }
            }
            //thread = new Thread(this);
            //thread.start();
        }

        public void onPauseMySurfaceView() {
            boolean retry = true;
            running = false;
            while (retry) {
                try {
                    thread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        @Override

        public void run() {
            try {
                String[] cameraIds = manager.getCameraIdList();
                manager.openCamera(cameraIds[0],deviceCallback,null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            finally{
                cameraDevice.close();
            }
            while(running){
                if(surfaceHolder.getSurface().isValid()) {
                    Canvas canvas = surfaceHolder.lockCanvas();
                    Paint paint = new Paint();
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(Color.MAGENTA);
                    canvas.drawPaint(paint);
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }

    }
}
