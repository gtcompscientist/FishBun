package com.sangcomz.fishbun.ui.picker;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;

import com.sangcomz.fishbun.bean.Album;
import com.sangcomz.fishbun.define.Define;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import rx.Observable;
import rx.Subscription;

/**
 * Created by sangc on 2015-11-05.
 */
public class PickerPresenter implements PickerAction {
    private PickerView view;
    private RecyclerView recyclerView;
    private RecyclerView.OnItemTouchListener onItemTouchListener;
    private ActionBar actionBar;
    private String bucketTitle;

    private Observable<ArrayList<Album>> observable;
    private Subscription subscription;

    private String savePath;

    PickerPresenter(PickerView view, ActionBar actionBar, RecyclerView recyclerView, String bucketTitle) {
        this.view = view;
        this.recyclerView = recyclerView;
        this.actionBar = actionBar;
        this.bucketTitle = bucketTitle;

        onItemTouchListener = new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                return true;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        };
    }

    @Override
    public void displayImage() {
//        if (observable == null) {
//            observable = getImageListObservable();
//        }
//        subscription = getImageListSubscription();
    }

//    public Intent getFinishIntent(){
//
//    }

//    public Observable<ArrayList<Album>> getImageListObservable() {
//        return Observable.create(new Observable.OnSubscribe<ArrayList<Album>>() {
//            @Override
//            public void call(Subscriber<? super ArrayList<Album>> subscriber) {
//                final String orderBy = MediaStore.Images.Media.BUCKET_ID;
//                String[] projection = new String[]{
//                        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
//                        MediaStore.Images.Media.BUCKET_ID};
//
//                Cursor imagecursor = contentResolver.query(
//                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
//                        null, null, orderBy);
//
//                long previousid = 0;
//
//                int bucketColumn = imagecursor
//                        .getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
//                int bucketcolumnid = imagecursor
//                        .getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
//                ArrayList<Album> albumList = new ArrayList<>();
//                Album totalAlbum = new Album();
//                totalAlbum.bucketid = 0;
//                totalAlbum.bucketname = Define.TEXT_ALL_VIEW;
//                totalAlbum.counter = 0;
//                totalAlbum.thumnaliImage = getFirstImage();
//                albumList.add(totalAlbum);
//                int totalCounter = 0;
//
//                while (imagecursor.moveToNext()) {
//                    totalCounter++;
//                    long bucketid = imagecursor.getInt(bucketcolumnid);
//                    if (previousid != bucketid) {
//                        Album album = new Album();
//                        album.bucketid = bucketid;
//                        album.bucketname = imagecursor.getString(bucketColumn);
//                        album.counter++;
//                        album.thumnaliImage = getAllMediaThumbnailsPath(bucketid);
//                        albumList.add(album);
//                        previousid = bucketid;
//                    } else {
//                        if (albumList.size() > 0)
//                            albumList.get(albumList.size() - 1).counter++;
//                    }
//                    if (imagecursor.isLast()) {
//                        albumList.get(0).counter = totalCounter;
//                    }
//                }
//                imagecursor.close();
//                subscriber.onNext(albumList);
//            }
//        });
//    }
//
//    public Subscription getImageListSubscription() {
//        return observable
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe(new Action1<ArrayList<Album>>() {
//                    @Override
//                    public void call(ArrayList<Album> albumList) {
//                        view.setAlbum(albumList);
//                    }
//                });
//    }



    /**
     * @param isAble true == can clickable
     */
    public void setRecyclerViewClickable(final boolean isAble) {
        if (isAble)
            recyclerView.removeOnItemTouchListener(onItemTouchListener);
        else {
            recyclerView.addOnItemTouchListener(onItemTouchListener);
        }

    }

    public void setActionbarTitle(int total) {
        if (Define.ALBUM_PICKER_COUNT == 1)
            actionBar.setTitle(bucketTitle);
        else
            actionBar.setTitle(bucketTitle + "(" + String.valueOf(total) + "/" + Define.ALBUM_PICKER_COUNT + ")");
    }

    public void takePicture(Context context, String saveDir) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(saveDir); //make a file
                setSavePath(photoFile.getAbsolutePath());
            } catch (IOException ex) {
                ex.printStackTrace();
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                if (context instanceof PickerActivity)
                    ((PickerActivity) context).startActivityForResult(takePictureIntent, Define.TAKE_A_PICK_REQUEST_CODE);


            }
        }
    }

    private File createImageFile(String saveDir) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(saveDir);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        return image;
    }


    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    @Override
    public void stop() {

    }
}
