package com.example.guswn.allthatlyrics.ui.social.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.guswn.allthatlyrics.R;
import com.example.guswn.allthatlyrics.model.ShowGalleryModel;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ShowGalleryActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    @BindView(R.id.show_gall_tb)
    Toolbar show_gall_tb;
    @BindView(R.id.show_gall_spinner)
    Spinner show_gall_spinner;
    private static final String[] paths = {"item 1", "item 2", "item 3"};

    @BindView(R.id.show_gall_img)
    ImageView show_gall_img;
    @BindView(R.id.show_gall_video)
    VideoView show_gall_video;
    @BindView(R.id.show_gall_frame)
    FrameLayout show_gall_frame;

    @BindView(R.id.show_gall_more_btn)
    ImageButton show_gall_more_btn;
    @BindView(R.id.show_gall_camera_btn)
    ImageButton show_gall_camera_btn;
    @BindView(R.id.show_gall_crop_btn)
    ImageButton show_gall_crop_btn;
    @BindView(R.id.show_gall_RV)
    GridView gallery;
    @BindView(R.id.show_gall_select_cnt)
    TextView show_gall_select_cnt;

    PhotoViewAttacher pAttacher;

    @OnClick(R.id.show_gall_crop_btn)
    public void crop(){
        if (selected_onefile!=null){
            File imgFile = new  File(selected_onefile.getFilepath());
            if(imgFile.exists())
            {
                CropImage.activity(Uri.fromFile(imgFile))
                        .start(ShowGalleryActivity.this);
            }else {
                Toast.makeText(ShowGalleryActivity.this,"사진을 선택하세요!!",Toast.LENGTH_SHORT).show();
            }
        }Toast.makeText(ShowGalleryActivity.this,"사진을 선택하세요",Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                pAttacher = new PhotoViewAttacher(show_gall_img);
                Uri resultUri = result.getUri();
                /**test success!!!!*/
                selected_onefile = new ShowGalleryModel(resultUri.getPath(),"image");
//                selectedfile = resultUri.getPath();
                /**test*/
                Glide.with(ShowGalleryActivity.this)
                        .load(resultUri)
                        .apply(new RequestOptions().fitCenter())
                        .into(show_gall_img);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(ShowGalleryActivity.this,error.toString(),Toast.LENGTH_SHORT).show();
            }
        }
        if(resultCode == RESULT_OK){
            Intent resultIntent;
            Log.e("ShowGallery_onActivityResult_requestCode",""+requestCode);
            switch (requestCode){
                // testActivity 에서 요청할 때 보낸 요청 코드 ()
                case 1:
                    resultIntent = new Intent();
                    setResult(RESULT_OK,resultIntent);
                    finish();
                    break;
                case 2:
                    resultIntent = new Intent();
                    setResult(RESULT_OK,resultIntent);
                    finish();
                    break;
                case 3:
                    resultIntent = new Intent();
                    setResult(RESULT_OK,resultIntent);
                    finish();
                    break;
                case 4:
                    resultIntent = new Intent();
                    setResult(RESULT_OK,resultIntent);
                    finish();
                    break;
            }

        }
    }


    int flag=0;
    @OnClick(R.id.show_gall_more_btn)
    public void selectmultiplephoto(){
        switch (flag){
            case 0:
                imageAdapter.isMulti =true;
                imageAdapter.notifyDataSetChanged();

                //selectedfile=null;
                selected_onefile=null;

                show_gall_more_btn.setColorFilter(Color.BLUE);
                show_gall_select_cnt.setVisibility(View.VISIBLE);
                show_gall_crop_btn.setVisibility(View.GONE);
                show_gall_camera_btn.setVisibility(View.GONE);

                flag++;
                break;
            case 1:
                imageAdapter.isMulti =false;
                //multipleSelectedImages = new ArrayList<>();
                Selectedfiles = new ArrayList<>();
                imageAdapter.count=0;
                imageAdapter.notifyDataSetChanged();

                show_gall_more_btn.setColorFilter(Color.GRAY);
                show_gall_select_cnt.setVisibility(View.GONE);
                show_gall_select_cnt.setText("0");
                show_gall_crop_btn.setVisibility(View.VISIBLE);
                show_gall_camera_btn.setVisibility(View.VISIBLE);

                flag=0;
                break;
        }
        Log.e("selectmultiplephoto","selectmultiplephoto  "+imageAdapter.isMulti);
    }

    /** The images. */
    private ArrayList<ShowGalleryModel> images;
    static String selectedfile;
    static ShowGalleryModel selected_onefile;
    ImageAdapter imageAdapter;
    private ArrayList<String> multipleSelectedImages = new ArrayList<>();
    private ArrayList<ShowGalleryModel> Selectedfiles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_gallery);
        ButterKnife.bind(this);


        show_gall_more_btn.setColorFilter(Color.GRAY);
        show_gall_crop_btn.setColorFilter(Color.GRAY);
        show_gall_camera_btn.setColorFilter(Color.GRAY);
        //툴바
        setSupportActionBar(show_gall_tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼, 디폴트로 true만 해도 백버튼이 생김
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.close_b);// 뒤로가기 버튼, 내가 지정할수 있다
        getSupportActionBar().setTitle("");
        //툴바

        //spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ShowGalleryActivity.this,
                android.R.layout.simple_spinner_item,paths);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        show_gall_spinner.setAdapter(adapter);
        show_gall_spinner.setOnItemSelectedListener(this);
        //spinner

        //갤러리
        imageAdapter = new ImageAdapter(this);
        gallery.setAdapter(imageAdapter);

//        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1,
//                                    int position, long arg3) {
//                if (null != images && !images.isEmpty()){
////                    Toast.makeText(getApplicationContext(),
////                            "position " + position + " : " + images.get(position),
////                            Toast.LENGTH_SHORT).show();
//
//                    File imgFile = new  File(images.get(position));
//                    if(imgFile.exists())
//                    {
//                        if (imageAdapter.isMulti){
//                           // imageAdapter.count++;
//                            imageAdapter.clickedPos = position;
//                            imageAdapter.notifyDataSetChanged();
//                        }
//                        selectedimage = images.get(position);
//                       // pAttacher = new PhotoViewAttacher(show_gall_img);
////                        CropImage.activity(Uri.fromFile(imgFile))
////                                .start(ShowGalleryActivity.this);
//
////                        show_gall_img.setImageURI(Uri.fromFile(imgFile));
//                        Glide.with(ShowGalleryActivity.this)
//                                .load(images.get(position))
//                                .apply(new RequestOptions().fitCenter())
//                                .into(show_gall_img);
//                    }
//                }
//
//            }
//        });
        //갤러리
    }

    /**
     * The Class ImageAdapter.
     */
    private class ImageAdapter extends BaseAdapter {

        /** The context. */
        private Activity context;

        /**
         * Instantiates a new image adapter.
         *
         * @param localContext
         *            the local context
         */
        public ImageAdapter(Activity localContext) {
            context = localContext;
            images = getAllShownImagesPath(context);
        }

        public int getCount() {
            return images.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }


        boolean isMulti;
        int count;

        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            //Log.e("getView","getView "+position);
            final int[] flag = {0};
            /**original*/
//            ImageView picturesView;
//            if (convertView == null) {
//                picturesView = new ImageView(context);
//                picturesView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                picturesView.setLayoutParams(new GridView.LayoutParams(310, 310));
//            } else {
//                picturesView = (ImageView) convertView;
//            }

            /**성공 customview*/
            View picturesView;
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ImageView imageViewAndroid;
            final TextView textViewAndroid;
            final TextView textViewAndroid2;
            /** final TextView textViewAndroid; 를 밖에다 정의를 하면 getView함수가 마지막으로 실행된 view 개체의 textview 가 textViewAndroid에 정의 된다
             * 그래서 클릭리스너로 textViewAndroid의 정보를 바꾸면 getView함수가 마지막으로 실행된 view 개체의 textview의 정보만 바뀐다! 함부로 뷰 개체를 전역변수로 정의하지마라 */
            if (convertView == null) {
                picturesView = new View(context);
                picturesView = inflater.inflate(R.layout.custom_grid_view, null);
                textViewAndroid = (TextView) picturesView.findViewById(R.id.custom_txt);
                textViewAndroid2 = (TextView) picturesView.findViewById(R.id.custom_videolength_txt);
                imageViewAndroid = (ImageView) picturesView.findViewById(R.id.custom_img);
            } else {
                picturesView = (View) convertView;
                picturesView = inflater.inflate(R.layout.custom_grid_view, null);
                textViewAndroid = (TextView) picturesView.findViewById(R.id.custom_txt);
                textViewAndroid2 = (TextView) picturesView.findViewById(R.id.custom_videolength_txt);
                imageViewAndroid = (ImageView) picturesView.findViewById(R.id.custom_img);
            }

            Log.e("mimeType",images.get(position).getMimtype());
            final String mime = images.get(position).getMimtype();
            if (mime.contains("video")){
                textViewAndroid2.setVisibility(View.VISIBLE);
            }else {
                textViewAndroid2.setVisibility(View.GONE);
            }

            /**선택박스 보이기*/
            if (isMulti){
                textViewAndroid.setVisibility(View.VISIBLE);

//                if (multipleSelectedImages.contains(images.get(position).getFilepath())){
//                textViewAndroid.setText("v");
//                textViewAndroid.setBackground(getDrawable(R.drawable.mybtn_3));
//                flag[0]=1;
//                }else {
//                textViewAndroid.setText("");
//                textViewAndroid.setBackground(getDrawable(R.drawable.mybtn_4));
//                flag[0]=0;
//                }
                ArrayList<String> filepathes = new ArrayList<>();
                for (int a=0; a<Selectedfiles.size(); a++){
                    filepathes.add(Selectedfiles.get(a).getFilepath());
                }
                if (filepathes.contains(images.get(position).getFilepath())){
                    textViewAndroid.setText("v");
                    textViewAndroid.setBackground(getDrawable(R.drawable.mybtn_3));
                    flag[0]=1;
                }else {
                    textViewAndroid.setText("");
                    textViewAndroid.setBackground(getDrawable(R.drawable.mybtn_4));
                    flag[0]=0;
                }
            }else {
                textViewAndroid.setVisibility(View.INVISIBLE);
                textViewAndroid.setText("");
                textViewAndroid.setBackground(getDrawable(R.drawable.mybtn_4));
            }
            /**선택박스 보이기*/
            picturesView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /**이미지 여러장 선택하기*/
                    if (isMulti){
                            switch (flag[0]){
                                case 0:
                                    count++;
                                   // textViewAndroid.setText(count+"");
                                    textViewAndroid.setText("v");
                                    textViewAndroid.setBackground(getDrawable(R.drawable.mybtn_3));
                                    //multipleSelectedImages.add(images.get(position).getFilepath());
                                    Selectedfiles.add(images.get(position));
                                    String cnt =count+"";
                                    show_gall_select_cnt.setText(cnt);
                                    flag[0]++;
                                    //Log.e("multipleSelectedImages",multipleSelectedImages.toString());
                                    break;
                                case 1:
                                    count--;
                                    textViewAndroid.setText("");
                                    textViewAndroid.setBackground(getDrawable(R.drawable.mybtn_4));
                                    //multipleSelectedImages.remove(images.get(position).getFilepath());
                                    Selectedfiles.remove(images.get(position));
                                    String cnt2 =count+"";
                                    show_gall_select_cnt.setText(cnt2);
                                    flag[0]=0;
                                    // Log.e("multipleSelectedImages",multipleSelectedImages.toString());
                                    break;
                            }
//                            if (multipleSelectedImages.size()>10){
//                                count--;
//                                textViewAndroid.setText("");
//                                textViewAndroid.setBackground(getDrawable(R.drawable.mybtn_4));
//                                multipleSelectedImages.remove(images.get(position).getFilepath());
//                                show_gall_select_cnt.setText(count+"");
//                                flag[0]=0;
//
//                                Toast toast =  Toast.makeText(ShowGalleryActivity.this,"최대 10장까지 선택할수 있습니다",Toast.LENGTH_SHORT);
//                                toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL,0,0);
//                                toast.show();
//                            }
                        if (Selectedfiles.size()>10){
                            count--;
                            textViewAndroid.setText("");
                            textViewAndroid.setBackground(getDrawable(R.drawable.mybtn_4));
                            Selectedfiles.remove(images.get(position));
                            show_gall_select_cnt.setText(count+"");
                            flag[0]=0;

                            Toast toast =  Toast.makeText(ShowGalleryActivity.this,"최대 10개까지 선택할수 있습니다",Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL,0,0);
                            toast.show();
                        }

                    }
                    /**이미지 여러장 선택하기*/

                    /**이미지 보이기*/
                    if (null != images && !images.isEmpty()){
//                        File imgFile = new  File(images.get(position));
//                        if(imgFile.exists())
                        String mimetype = images.get(position).getMimtype();
                        if (mimetype.contains("image")){
                            show_gall_crop_btn.setVisibility(View.VISIBLE);
                            show_gall_frame.setVisibility(View.GONE);
                            show_gall_video.setVisibility(View.GONE);
                            show_gall_img.setVisibility(View.VISIBLE);
                            if (show_gall_video.isPlaying()){
                                show_gall_video.stopPlayback();
                            }
                           // selectedfile = images.get(position).getFilepath();
                            selected_onefile = images.get(position);
                            Glide.with(ShowGalleryActivity.this)
                                    .load(images.get(position).getFilepath())
                                    .apply(new RequestOptions().fitCenter())
                                    .into(show_gall_img);
                        }else if (mimetype.contains("video")){
                            show_gall_crop_btn.setVisibility(View.GONE);
                            show_gall_frame.setVisibility(View.VISIBLE);
                            show_gall_video.setVisibility(View.VISIBLE);
                            show_gall_img.setVisibility(View.GONE);

                            //selectedfile = images.get(position).getFilepath();
                            selected_onefile = images.get(position);
                            show_gall_video.setVideoPath(selected_onefile.getFilepath());
                            show_gall_video.start();
                            show_gall_video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    mp.setLooping(true);
                                }
                            });
                            /**비디오뷰 비율에 맞게 조정하기*/
                        }
                    }
                    /**이미지 보이기*/
                }
            });


            /**그리드 이미지*/
            Glide.with(context)
                    .load(images.get(position).getFilepath())
                    .apply(new RequestOptions().centerCrop())
                    .into(imageViewAndroid);
            /**그리드 이미지*/
            return picturesView;
        }

        /**
         * Getting All Images Path.
         *
         * @param activity
         *            the activity
         * @return ArrayList with images Path
         */


        private ArrayList<ShowGalleryModel> getAllShownImagesPath(Activity activity) {
            Uri uri;
            Cursor cursor;
            int column_index_data, column_index_folder_name;
            int column_index_data_video;
            int mime_type;
            ArrayList<ShowGalleryModel> listOfAllImages = new ArrayList<ShowGalleryModel>();
            String absolutePathOfImage = null;
            String mimeType = null;

//            uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI; //image
//            uri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI; //video
            uri = MediaStore.Files.getContentUri("external");//video and image

//            String[] projection = {
//                    MediaStore.MediaColumns.DATA,
//                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME ,
//                    MediaStore.Video.VideoColumns.DATA
//            };
            String[] projection = {
                    MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME ,
                    MediaStore.Video.VideoColumns.DATA,
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.DATE_ADDED,
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.TITLE
            };
            final String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                    + " OR "
                    + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
            final String orderBy = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";
            cursor = activity.getContentResolver().query(uri, projection, selection,
                    null, orderBy);

            column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            column_index_data_video = cursor.getColumnIndexOrThrow( MediaStore.Video.VideoColumns.DATA);
            mime_type = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE);

            while (cursor.moveToNext()) {
                absolutePathOfImage = cursor.getString(column_index_data);
                mimeType = cursor.getString(mime_type);
                //Log.e("_absolutePathOfImage ",absolutePathOfImage);
               // Log.e("_mimeType ",mimeType);
                listOfAllImages.add(new ShowGalleryModel(absolutePathOfImage,mimeType));
            }
            return listOfAllImages;
        }
    }

    /** 툴바관련 코드*/
    //툴바
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.show_gal_toolbar_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //selectedfile=null;
        selected_onefile=null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                //selectedfile=null;
                selected_onefile=null;
                finish();
                return true;
            }
            case R.id.menu_toolbar_next:
                if (selected_onefile!=null){
                    Intent intent = new Intent(ShowGalleryActivity.this,AdvancedEditPhotoActivity.class);
                    if (imageAdapter.isMulti){
                        intent.putExtra("isMulti",true);
                        intent.putParcelableArrayListExtra("multipleSelectedImages",Selectedfiles);
                    }else {
                        intent.putExtra("isMulti",false);
                        intent.putExtra("selectedimage",selected_onefile);
                    }
                    startActivityForResult(intent,2);

                }else {
                    Toast.makeText(ShowGalleryActivity.this,"사진을 선택하세요",Toast.LENGTH_SHORT).show();
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                // Whatever you want to happen when the first item gets selected
                break;
            case 1:
                // Whatever you want to happen when the second item gets selected
                break;
            case 2:
                // Whatever you want to happen when the thrid item gets selected
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    //spinner
    //툴바
    /** 툴바관련 코드*/


}
