package com.sangcomz.fishbun.ui.picker;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.sangcomz.fishbun.R;
import com.sangcomz.fishbun.adapter.PickerGridAdapter;
import com.sangcomz.fishbun.bean.Album;
import com.sangcomz.fishbun.bean.ImageBean;
import com.sangcomz.fishbun.define.Define;
import com.sangcomz.fishbun.permission.PermissionCheck;
import com.sangcomz.fishbun.util.UiUtil;

import java.io.File;
import java.util.ArrayList;


public class PickerActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<ImageBean> imageBeans;
    private PickerController pickerController;
    private Album a;
    PermissionCheck permissionCheck;
    private UiUtil uiUtil = new UiUtil();

    PickerGridAdapter adapter;

    private String pathDir = "";

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        transImageFinish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_picker);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(Define.ACTIONBAR_COLOR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            uiUtil.setStatusBarColor(this);
        }
        ActionBar bar = getSupportActionBar();
        if (bar != null) bar.setDisplayHomeAsUpEnabled(true);

        a = (Album) getIntent().getSerializableExtra("album");
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, Define.PHOTO_SPAN_COUNT, GridLayoutManager.VERTICAL, false);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(gridLayoutManager);
        imageBeans = new ArrayList<>();

        pickerController = new PickerController(this, getSupportActionBar(), recyclerView, a.bucketname);

        ArrayList<String> path = getIntent().getStringArrayListExtra(Define.INTENT_PATH);
        if (path != null) {
            for (int i = 0; i < path.size(); i++) {
                imageBeans.add(new ImageBean(i + 1, path.get(i)));
            }
        }
        pickerController.setActionbarTitle(imageBeans.size());

        permissionCheck = new PermissionCheck(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionCheck.CheckStoragePermission())
                new DisplayImage().execute();
        } else
            new DisplayImage().execute();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photo_picker, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_ok) {
            ArrayList<ImageBean> pickedImageBeans = new ArrayList<>();
            for (ImageBean b : imageBeans) {
                if (b.SelectedOrder == null)
                    continue;
                if (b.SelectedOrder > pickedImageBeans.size())
                    pickedImageBeans.add(b);
                else
                    pickedImageBeans.add(b.SelectedOrder, b);
            }
            if (pickedImageBeans.size() == 0) {
//                Toast.makeText(this, getString(R.string.msg_no_slected), Toast.LENGTH_SHORT).show();
                Snackbar.make(recyclerView, Define.MESSAGE_NOTHING_SELECTED, Snackbar.LENGTH_SHORT).show();
            } else {
                ArrayList<String> path = new ArrayList<>();
                for (int i = 0; i < pickedImageBeans.size(); i++) {
                    path.add(pickedImageBeans.get(i).getImgPath());
                }
                Intent i = new Intent();
                i.putStringArrayListExtra(Define.INTENT_PATH, path);
                setResult(RESULT_OK, i);
                finish();
            }
            return true;
        } else if (id == R.id.action_all) {
            for (int i = 0; i < imageBeans.size(); i++) {
                imageBeans.get(i).SelectedOrder = i;
                adapter.notifyItemChanged(i);
            }
            pickerController.setActionbarTitle(imageBeans.size());
        } else if (id == android.R.id.home)
            transImageFinish();
        return super.onOptionsItemSelected(item);
    }

    private class DisplayImage extends AsyncTask<Void, Void, ImageBean[]> {

        @Override
        protected ImageBean[] doInBackground(Void... params) {
            return getAllMediaThumbnailsPath(a.bucketid);
        }


        @Override
        protected void onPostExecute(ImageBean[] result) {
            super.onPostExecute(result);
            for (int i = 0; i < result.length; i++)
                imageBeans.add(result[i]);

            adapter = new PickerGridAdapter(
                imageBeans, pickerController, getPathDir());
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Define.PERMISSION_STORAGE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    new DisplayImage().execute();
                    // permission was granted, yay! do the
                    // calendar task you need to do.
                } else {
                    permissionCheck.showPermissionDialog();
                    finish();
                }
                return;
            }
            default:
                break;
        }
    }

    @NonNull
    private ImageBean[] getAllMediaThumbnailsPath(long id) {
        String path;
        String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
        String bucketid = String.valueOf(id);
        String sort = MediaStore.Images.Media._ID + " DESC";
        String[] selectionArgs = {bucketid};

        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor c;
        if (!bucketid.equals("0")) {
            c = getContentResolver().query(images, null, selection, selectionArgs, sort);
        } else {
            c = getContentResolver().query(images, null, null, null, sort);
        }
        ImageBean[] imageBeans = new ImageBean[c == null ? 0 : c.getCount()];
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    setPathDir(c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA)),
                        c.getString(c.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)));
                    int position = -1;
                    do {
                        path = c.getString(c.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
                        imageBeans[++position] = new ImageBean(-1, path);
                    } while (c.moveToNext());
                }
                c.close();
            } catch (Exception e) {
                if (!c.isClosed()) c.close();
            }
        }
        return imageBeans;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Define.TAKE_A_PICK_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                startFileMediaScan(pickerController.getSavePath());
                adapter.addImage(pickerController.getSavePath(), pickCount++);
            } else {
                new File(pickerController.getSavePath()).delete();
            }
        }
    }

    private int pickCount = 0;

    public void selectPhoto(View v) {
        if (v == null || v.getTag() == null)
            return;
        Object vTag = v.getTag();
        if (!(vTag instanceof ImageBean))
            return;

        ImageBean bean = (ImageBean)vTag;
        if (bean.SelectedOrder == null) {
            bean.SelectedOrder = pickCount++;
            for (int i = 0; i < imageBeans.size(); i++) {
                if (imageBeans.get(i).equals(bean)) {
                    adapter.notifyItemChanged(i);
                    break;
                }
            }
        } else {
            int removed = bean.SelectedOrder;
            bean.SelectedOrder = null;
            for (int i = 0; i < imageBeans.size(); i++) {
                ImageBean b = imageBeans.get(i);
                if (b.equals(bean)) {
                    adapter.notifyItemChanged(i);
                } else if (b.SelectedOrder != null && b.SelectedOrder > removed) {
                    b.SelectedOrder--;
                    adapter.notifyItemChanged(i);
                }
            }
        }
        pickerController.setActionbarTitle(pickCount);
    }

    //MediaScanning
    public void startFileMediaScan(String path) {
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
    }

    private void setPathDir(String path, String fileName) {
        pathDir = path.replace("/" + fileName, "");
    }

    private String getPathDir() {
        if (pathDir.equals("") || a.bucketid == 0)
            pathDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM+"/Camera").getAbsolutePath();
        return pathDir;
    }

    private void transImageFinish() {
        ArrayList<String> path = new ArrayList<>();
        for (ImageBean b : imageBeans) {
            if (b.SelectedOrder == null)
                continue;
            if (b.SelectedOrder > path.size())
                path.add(b.getImgPath());
            else
                path.add(b.SelectedOrder, b.getImgPath());
        }
        Intent i = new Intent();
        i.putStringArrayListExtra(Define.INTENT_PATH, path);
        setResult(Define.TRANS_IMAGES_RESULT_CODE, i);
        finish();
    }
}