package com.example.guswn.allthatlyrics.ui.chat.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.guswn.allthatlyrics.adapter.FriendAdapter;
import com.example.guswn.allthatlyrics.extension.MyRetrofit;
import com.example.guswn.allthatlyrics.api.FriendAPI;
import com.example.guswn.allthatlyrics.model.FriendModel;
import com.example.guswn.allthatlyrics.response.userResponse3;
import com.example.guswn.allthatlyrics.R;
import com.example.guswn.allthatlyrics.api.ChatAPI;
import com.example.guswn.allthatlyrics.response.ChatResponse;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.guswn.allthatlyrics.adapter.FriendAdapter.AddedChatPeopleList;
import static com.example.guswn.allthatlyrics.ui.auth.LogoActivity.MY_EMAIL_2;
import static com.example.guswn.allthatlyrics.ui.auth.LogoActivity.MY_IDX;

public class ChatAddActivity extends AppCompatActivity{

    FriendAdapter myAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    FriendAPI api;
    ChatAPI api_chat;
    ArrayList<FriendModel> friendInfos2;
    @BindView(R.id.recyclerview_chatadd)
    RecyclerView mRecyclerView;
    @BindView(R.id.chatadd_tb)
    Toolbar chatadd_tb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_add);
        ButterKnife.bind(this);
        setSupportActionBar(chatadd_tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼, 디폴트로 true만 해도 백버튼이 생김
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_black);// 뒤로가기 버튼, 내가 지정할수 있다


        MyRetrofit ret  = new MyRetrofit();
        api = ret.create(FriendAPI.class);
        api_chat = ret.create(ChatAPI.class);



        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        friendInfos2 = new ArrayList<>();
        myAdapter = new FriendAdapter(friendInfos2,ChatAddActivity.this,true);
        mRecyclerView.setAdapter(myAdapter);
        getoneinfo_B();
    }

    public void getoneinfo_B(){
        Log.e("MY_EMAIL","/"+MY_EMAIL_2);
        Call<userResponse3> call = api.getOneInfo(MY_IDX);

        Log.e("doesitstartnow? :","yes3");
        call.enqueue(new Callback<userResponse3>() {
            @Override
            public void onResponse(Call<userResponse3> call, Response<userResponse3> response) {
                if(!response.isSuccessful()){
                    Log.e("getoneinfo_B_code",""+response.code());
                    return;
                }

                userResponse3 val3 = response.body();
                List<userResponse3> value = val3.getUserinfolist();

                //A_TYPE
                for(userResponse3 val : value){
                    String idx = val.getIdx();
                    String email = val.getEmail();
                    String username = val.getUsername();
                    String photo = val.getPhoto();
                    String birthday = val.getBirthday();
                    String introduce = val.getIntroduce();
                    if(email.equals(MY_EMAIL_2)){
                        friendInfos2.add(new FriendModel(FriendModel.A_TYPE,idx,photo,username,introduce,email,birthday));
                    }
                }

                //B_TYPE
                for(userResponse3 val : value){
                    String idx = val.getIdx();
                    String email = val.getEmail();
                    String username = val.getUsername();
                    String photo = val.getPhoto();
                    String birthday = val.getBirthday();
                    String introduce = val.getIntroduce();
                    if(email.equals(MY_EMAIL_2)){
                        //friendInfos.add(new FriendInfo(FriendInfo.A_TYPE,photo,username,introduce));
                    }else {
                        friendInfos2.add(new FriendModel(FriendModel.B_TYPE,idx,photo,username,introduce,email,birthday));
                    }
                }

                myAdapter.notifyDataSetChanged();
                //        myAdapter = new MyAdapter_Friend(friendInfos2,ChatAddActivity.this,true);
                //        mRecyclerView.setAdapter(myAdapter);
                //위 두 코드를 onResponse 에 놓으면 No adapter attached; skipping layout 라는 로그가 나타난다
                // 위 두코드 대신  myAdapter.notifyDataSetChanged(); 를 onResponse 에 놓자

                Log.e("doesitstartnow? :","yes4");
            }

            @Override
            public void onFailure(Call<userResponse3> call, Throwable t) {
                Log.e("getoneinfo_B_fail","Error : "+t.getMessage());
            }
        });
    }

    //툴바
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.chat_add_tb, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }
            case R.id.menu_toolbar_ok:
                String a = "";
                for(String s : AddedChatPeopleList){
                    a+=s+"/";
                }
                AddedChatPeopleList.add(MY_IDX);// 자기 자신도 카톡 방에 들어가야지
                Collections.sort(AddedChatPeopleList);
                Log.e("AddedChatPeopleList1",a);
                Log.e("AddedChatPeopleList2",AddedChatPeopleList.toString());
                String AddedChatPeopleList_json = new Gson().toJson(AddedChatPeopleList);
                Log.e("AddedChatPeopleList3",AddedChatPeopleList_json);


                makechatroom(AddedChatPeopleList.size()+"",AddedChatPeopleList_json);

//                //액티비티에서 프래그 먼트로 이동
//                //Home 액티비티에서 startActivityForResult 를 이용해
//                //프래그먼트를 이어줌
//                Intent resultIntent = new Intent();
//                resultIntent.putExtra("result","연산 결과는 입니다.");
//                setResult(RESULT_OK,resultIntent);
//                finish();
//                //액티비티에서 프래그 먼트로 이동
        }
        return super.onOptionsItemSelected(item);
    }
    //툴바
//    @Override
//    public void onItemClick(int position, View v) { }
//    @Override
//    public void onCheckBoxClick(final int position, View v) {
//
//    }

    public void makechatroom(String ChatPeopleNum,String ChatPeople_json){
        Map<String,String> map = new HashMap<>();
        map.put("ChatPeopleNum",ChatPeopleNum);
        map.put("ChatPeople",ChatPeople_json);
        map.put("MY_IDX",MY_IDX);

        Call<ChatResponse> call = api_chat.makeChatRoom(map);

        final ProgressDialog progressDoalog;
        progressDoalog = new ProgressDialog(this);
        progressDoalog.setMax(100);
        progressDoalog.setMessage("Please Wait");
        progressDoalog.setTitle("Making Chat Room...");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // show it
        progressDoalog.show();
        call.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if(!response.isSuccessful()){
                    Log.e("makechatroom_code",""+response.code());
                    return;
                }progressDoalog.dismiss();


                ChatResponse res = response.body();
                String value= res.getValue();
                String message = res.getMessage();


                Log.e("makechatroom_ChatResponse",value+"/"+message);
                if (value.equals("1")){
                    //액티비티에서 프래그 먼트로 이동
                    //Home 액티비티에서 startActivityForResult 를 이용해
                    //프래그먼트를 이어줌
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("result","연산 결과는 입니다.");
                    setResult(RESULT_OK,resultIntent);
                    finish();
                    //액티비티에서 프래그 먼트로 이동
                }else {
                    AddedChatPeopleList.remove(MY_IDX);
                    Toast.makeText(ChatAddActivity.this,message,Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                progressDoalog.dismiss();
                Log.e("makechatroom_fail","Error : "+t.getMessage());
            }
        });
    }


    public void test(){
        friendInfos2.add(new FriendModel(FriendModel.A_TYPE,"1","1","name","des","email","birthday"));
        for(int i=1;i<20;i++){
            friendInfos2.add(new FriendModel(FriendModel.B_TYPE,i+"","1","name "+i,"des "+i,"email "+i,"birthday "+i));
        }
        myAdapter = new FriendAdapter(friendInfos2,ChatAddActivity.this,true);
        mRecyclerView.setAdapter(myAdapter);
    }
}
