package com.sangcomz.fishbun.ui.album;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.sangcomz.fishbun.bean.Album;
import com.sangcomz.fishbun.define.Define;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by sangcomz on 4/3/2016.
 */
public class AlbumPresenter implements AlbumAction {
    private AlbumView view;

    private ContentResolver contentResolver;

    private Observable<ArrayList<Album>> observable;
    private Subscription subscription;

    public AlbumPresenter(AlbumActivity view, ContentResolver contentResolver) {
        this.view = view;
        this.contentResolver = contentResolver;
    }

    @Override
    public void displayAlbum() {
        if (observable == null) {
            observable = getAlbumListObservable();
        }
        subscription = getAlbumListSubscription();
    }


    public String getAllMediaThumbnailsPath(long id) {
        String path = "";
        String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
        String bucketid = String.valueOf(id);
        String sort = MediaStore.Images.Thumbnails._ID + " DESC";
        String[] selectionArgs = {bucketid};

        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor c;

        c = contentResolver.query(images, null,
                selection, selectionArgs, sort);

        if (c != null && c.moveToNext()) {
            selection = MediaStore.Images.Media._ID + " = ?";
            String photoID = c.getString(c.getColumnIndex(MediaStore.Images.Media._ID));
            selectionArgs = new String[]{photoID};

            images = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
            Cursor cursor = contentResolver.query(images, null,
                    selection, selectionArgs, sort);
            if (cursor != null && cursor.moveToNext()) {
                path = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));
            } else
                path = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));
            if (cursor != null) {
                cursor.close();
            }
        } else {
            Log.e("id", "from else");
        }
        if (c != null) {
            c.close();
        }
        return path;
    }

    protected String getFirstImage() {
        String path = "";
        String sort = MediaStore.Images.Thumbnails._ID + " DESC";
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor c;
        c = contentResolver.query(images, null,
                null, null, sort);
        if (c != null) {
            c.moveToFirst();
            path = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));
            c.close();
        }
        return path;
    }


    public Observable<ArrayList<Album>> getAlbumListObservable() {
        return Observable.create(new Observable.OnSubscribe<ArrayList<Album>>() {
            @Override
            public void call(Subscriber<? super ArrayList<Album>> subscriber) {
                final String orderBy = MediaStore.Images.Media.BUCKET_ID;
                String[] projection = new String[]{
                        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                        MediaStore.Images.Media.BUCKET_ID};

                Cursor imagecursor = contentResolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                        null, null, orderBy);

                long previousid = 0;

                int bucketColumn = imagecursor
                        .getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                int bucketcolumnid = imagecursor
                        .getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
                ArrayList<Album> albumList = new ArrayList<>();
                Album totalAlbum = new Album();
                totalAlbum.bucketid = 0;
                totalAlbum.bucketname = Define.TEXT_ALL_VIEW;
                totalAlbum.counter = 0;
                totalAlbum.thumnaliImage = getFirstImage();
                albumList.add(totalAlbum);
                int totalCounter = 0;

                while (imagecursor.moveToNext()) {
                    totalCounter++;
                    long bucketid = imagecursor.getInt(bucketcolumnid);
                    if (previousid != bucketid) {
                        Album album = new Album();
                        album.bucketid = bucketid;
                        album.bucketname = imagecursor.getString(bucketColumn);
                        album.counter++;
                        album.thumnaliImage = getAllMediaThumbnailsPath(bucketid);
                        albumList.add(album);
                        previousid = bucketid;
                    } else {
                        if (albumList.size() > 0)
                            albumList.get(albumList.size() - 1).counter++;
                    }
                    if (imagecursor.isLast()) {
                        albumList.get(0).counter = totalCounter;
                    }
                }
                imagecursor.close();
                subscriber.onNext(albumList);
            }
        });
    }

    public Subscription getAlbumListSubscription() {
        return observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<ArrayList<Album>>() {
                    @Override
                    public void call(ArrayList<Album> albumList) {
                        view.setAlbum(albumList);
                    }
                });
    }


    @Override
    public void stop() {
        if (subscription != null)
            subscription.isUnsubscribed();
    }
}
