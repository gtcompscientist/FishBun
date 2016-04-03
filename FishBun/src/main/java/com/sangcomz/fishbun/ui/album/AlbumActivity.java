package com.sangcomz.fishbun.ui.album;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.sangcomz.fishbun.ItemDecoration.DividerItemDecoration;
import com.sangcomz.fishbun.R;
import com.sangcomz.fishbun.ui.adapter.AlbumListAdapter;
import com.sangcomz.fishbun.bean.Album;
import com.sangcomz.fishbun.define.Define;
import com.sangcomz.fishbun.permission.PermissionCheck;
import com.sangcomz.fishbun.util.UiUtil;

import java.util.ArrayList;


public class AlbumActivity extends AppCompatActivity implements AlbumView {

    private RecyclerView recyclerView;
    private AlbumListAdapter adapter;
    private RelativeLayout noAlbum;
    private AlbumPresenter albumPresenter;
    private int position = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_album);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission();
        if (UiUtil.isLandscape(this))
            ((GridLayoutManager) recyclerView.getLayoutManager()).setSpanCount(2);
        else
            ((GridLayoutManager) recyclerView.getLayoutManager()).setSpanCount(1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        position = recyclerView.computeVerticalScrollOffset();
        albumPresenter.stop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Define.ENTER_ALBUM_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK, data);
                finish();
            } else if (resultCode == Define.TRANS_IMAGES_RESULT_CODE) {
                ArrayList<String> path = data.getStringArrayListExtra(Define.INTENT_PATH);
                if (path != null)
                    adapter.setPath(path);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Define.PERMISSION_STORAGE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    albumPresenter.displayAlbum();
                    // permission was granted, yay! do the
                    // calendar task you need to do.
                } else {
                    PermissionCheck.showPermissionDialog(this);
                    finish();
                }
                return;
            }
        }
    }

    @Override
    public void initView() {
        albumPresenter = new AlbumPresenter(this, getContentResolver());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        noAlbum = (RelativeLayout) findViewById(R.id.no_album);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(Define.ACTIONBAR_COLOR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UiUtil.setStatusBarColor(this);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(gridLayoutManager);
        }
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
    }

    @Override
    public void setAlbum(ArrayList<Album> albumList) {
        noAlbum.setVisibility(View.GONE);
        adapter = new AlbumListAdapter(albumList, getIntent().getStringArrayListExtra(Define.INTENT_PATH));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        recyclerView.scrollBy(0, position);
    }

    @Override
    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionCheck.CheckStoragePermission(this)) {
                albumPresenter.displayAlbum();
            }
        } else
            albumPresenter.displayAlbum();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (Define.IS_BUTTON)
            getMenuInflater().inflate(R.menu.menu_photo_album, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_ok) {
            if (adapter.getPath().size() == 0) {
                Snackbar.make(recyclerView, Define.MESSAGE_NOTHING_SELECTED, Snackbar.LENGTH_SHORT).show();
            } else {
                Intent i = new Intent();
                i.putStringArrayListExtra(Define.INTENT_PATH, adapter.getPath());
                setResult(RESULT_OK, i);
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }


}
