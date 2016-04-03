package com.sangcomz.fishbun.ui.album;

import com.sangcomz.fishbun.bean.Album;
import com.sangcomz.fishbun.ui.mvp.IView;

import java.util.ArrayList;

/**
 * Created by sangcomz on 4/3/2016.
 */
public interface AlbumView extends IView {
    void initView();

    void setAlbum(ArrayList<Album> albumList);
}
