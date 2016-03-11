package com.example.dongja94.samplerecorder;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Gallery;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    SurfaceView surfaceView;
    SurfaceHolder mHolder;
    MediaRecorder mRecorder;
    Gallery gallery;
    ImageAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(this);
        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        gallery = (Gallery)findViewById(R.id.gallery);
        mAdapter = new ImageAdapter();
        gallery.setAdapter(mAdapter);
        Button btn = (Button)findViewById(R.id.btn_start);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecorder();
            }
        });

        btn = (Button)findViewById(R.id.btn_stop);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecorder();
            }
        });
    }

    private void startRecorder() {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);

            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                    "my_video");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            mSavedPath = new File(dir, "my_video_" + System.currentTimeMillis() + ".mp4");

            mRecorder.setOutputFile(mSavedPath.getAbsolutePath());

            mRecorder.setVideoSize(320, 240);
            if (mHolder != null) {
                mRecorder.setPreviewDisplay(mHolder.getSurface());
            }

            try {
                mRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
                mRecorder.release();
                mRecorder = null;
                return;
            }
            mRecorder.start();
        }
    }

    File mSavedPath;

    private void stopRecorder() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;

            addToMediaStore();

            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(mSavedPath.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND);

            mAdapter.add(bitmap);
        }
    }

    private void addToMediaStore() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.TITLE, "my video");
        values.put(MediaStore.Video.Media.DESCRIPTION, "my test video");
        values.put(MediaStore.Video.Media.DISPLAY_NAME, "myvideo");
        values.put(MediaStore.Video.Media.DATA, mSavedPath.getAbsolutePath());
        values.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mpeg");
        Uri uri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mHolder = holder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder = null;
        stopRecorder();
    }
}
