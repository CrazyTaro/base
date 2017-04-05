package com.taro.base.helper;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.support.design.widget.BottomSheetDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;


import com.taro.base.R;
import com.taro.base.utils.GalleryAndPhotoUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by taro on 16/12/12.
 */

public class AlbumCameraDialogHelper implements View.OnClickListener {
    public static final int REQUEST_CODE_OPEN_ALBUM = 0x110;
    public static final int REQUEST_CODE_OPEN_CAMERA = 0x119;
    public static final String DEFAULT_CACHE_DIRECTORY = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath().concat("/cache");

    @BindView(R.id.tv_layout_photo_album)
    TextView mTvOpenAblum;
    @BindView(R.id.tv_layout_photo_camera)
    TextView mTvCamera;
    @BindView(R.id.tv_layout_photo_cancel)
    TextView mTvCancel;

    private Activity mAct = null;
    private BottomSheetDialog mDialog = null;
    private String mDirectory = null;
    private String mPhotoPath = null;

    public AlbumCameraDialogHelper(Activity context) {
        mAct = context;
        View contentView = LayoutInflater.from(context).inflate(R.layout.layout_photo_camera, null);
        ButterKnife.bind(this, contentView);

        mDialog = new BottomSheetDialog(context);
        mDialog.setContentView(contentView);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setCancelable(true);

        mTvOpenAblum.setOnClickListener(this);
        mTvCamera.setOnClickListener(this);
        mTvCancel.setOnClickListener(this);

        mDirectory = DEFAULT_CACHE_DIRECTORY;
    }

    public void setPhotoCacheDirectory(String path) {
        mDirectory = path;
    }

    public void showDialog() {
        if (!mDialog.isShowing()) {
            mDialog.show();
        }
    }

    public String onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_OPEN_ALBUM) {
            return GalleryAndPhotoUtils.getBmpPathFromContent(mAct, data);
        } else if (requestCode == REQUEST_CODE_OPEN_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                return mPhotoPath;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private void generatePhotoPath() {
        mPhotoPath = mDirectory.concat("/")
                .concat(new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()))
                .concat(".jpg");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_layout_photo_album:
                GalleryAndPhotoUtils.openGallery(mAct, "请选择", REQUEST_CODE_OPEN_ALBUM);
                mDialog.dismiss();
                break;
            case R.id.tv_layout_photo_camera:
                generatePhotoPath();
                GalleryAndPhotoUtils.openCamera(mAct, mPhotoPath, REQUEST_CODE_OPEN_CAMERA);
                mDialog.dismiss();
                break;
            case R.id.tv_layout_photo_cancel:
                mDialog.dismiss();
                break;
        }
    }
}
