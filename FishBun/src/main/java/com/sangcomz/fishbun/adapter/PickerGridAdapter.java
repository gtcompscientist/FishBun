package com.sangcomz.fishbun.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.sangcomz.fishbun.R;
import com.sangcomz.fishbun.bean.ImageBean;
import com.sangcomz.fishbun.define.Define;
import com.sangcomz.fishbun.ui.album.AlbumActivity;
import com.sangcomz.fishbun.ui.picker.PickerController;
import com.sangcomz.fishbun.util.SquareTextView;

import java.util.ArrayList;


public class PickerGridAdapter
        extends RecyclerView.Adapter<PickerGridAdapter.ViewHolder> {
    private static final int TYPE_HEADER = Integer.MIN_VALUE;

    private ArrayList<ImageBean> imageBeans = new ArrayList<>();
    private PickerController pickerController;
    private boolean isHeader = Define.IS_CAMERA;

    String saveDir;

    public class ViewHolderImage extends ViewHolder {


        View layout;
        ImageView imgPhoto;
        SquareTextView txtPickCount;

        public ViewHolderImage(View view) {
            super(view);
            layout = view;
            imgPhoto = (ImageView) view.findViewById(R.id.img_thum);
            txtPickCount = (SquareTextView) view.findViewById(R.id.txt_pick_count);
        }
    }

    public class ViewHolderHeader extends ViewHolder {


        RelativeLayout header;

        public ViewHolderHeader(View view) {
            super(view);
            header = (RelativeLayout) itemView.findViewById(R.id.area_header);
        }
    }

    public PickerGridAdapter(ArrayList<ImageBean> imageBeans, PickerController pickerController,
                             String saveDir) {
        this.imageBeans = imageBeans;
        this.pickerController = pickerController;
        this.saveDir = saveDir;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;


        if (viewType == TYPE_HEADER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_item, parent, false);
            return new ViewHolderHeader(view);
        }

        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.thum_item, parent, false);
        return new ViewHolderImage(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        if (holder instanceof ViewHolderHeader) {
            final ViewHolderHeader vh = (ViewHolderHeader) holder;
            vh.header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pickerController.takePicture(saveDir);
                }
            });
            return;
        }

        if (!(holder instanceof ViewHolderImage))
            return;
        final int imagePos;

        if (isHeader)
            imagePos = position - 1;
        else
            imagePos = position;

        final ViewHolderImage vh = (ViewHolderImage) holder;

        final ImageBean imageBean = imageBeans.get(imagePos);
        final String imgPath = imageBean.getImgPath();

        if (imageBean.SelectedOrder != null) {
            vh.txtPickCount.setVisibility(View.VISIBLE);
            if (Define.ALBUM_PICKER_COUNT <= 1)
                vh.txtPickCount.setText("");
            else
                vh.txtPickCount.setText(String.valueOf(imageBean.SelectedOrder + 1));
        } else
            vh.txtPickCount.setVisibility(View.GONE);


        if (imgPath != null && !imgPath.equals("")) {
            Glide
                    .with(vh.imgPhoto.getContext())
                    .load(imgPath)
//                        .thumbnail(0.9f)
//                        .placeholder(R.drawable.loading_img)
                    .override(Define.PHOTO_PICKER_SIZE, Define.PHOTO_PICKER_SIZE)
                    .crossFade()
                    .centerCrop()
                    .into(vh.imgPhoto);
        }

        vh.layout.setTag(imageBean);
    }

    @Override
    public int getItemCount() {
        if (isHeader)
            return imageBeans.size() + 1;

        return imageBeans.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && isHeader) {
            return TYPE_HEADER;
        }
        return super.getItemViewType(position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        // Define elements of a row here
        public ViewHolder(View itemView) {
            super(itemView);
            // Find view by ID and initialize here
        }

        public void bindView(int position) {
            // bindView() method to implement actions
        }
    }

    public void addImage(String path, int selectionNumber) {
        ImageBean newBean = new ImageBean(-1, path);
        newBean.SelectedOrder = selectionNumber;
        imageBeans.add(0, newBean);

        notifyDataSetChanged();

        if(AlbumActivity.changeAlbumPublishSubject.hasObservers())
            AlbumActivity.changeAlbumPublishSubject.onNext("PATH|" + path);
    }

}