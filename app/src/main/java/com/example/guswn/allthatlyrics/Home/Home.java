package com.example.guswn.allthatlyrics.Home;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.guswn.allthatlyrics.Extension.MyRetrofit;
import com.example.guswn.allthatlyrics.Home.Frag1_friends.Home_fragment1;
import com.example.guswn.allthatlyrics.Home.Frag2_social.Home_fragment2_social;
import com.example.guswn.allthatlyrics.Home.Frag3_account.Home_fragment3;
import com.example.guswn.allthatlyrics.Home.Frag4_chat.Home_fragment4;
import com.example.guswn.allthatlyrics.Home.Frag4_chat.UnReadCountResponse;
import com.example.guswn.allthatlyrics.Main.BackPressCloseHandler;
import com.example.guswn.allthatlyrics.Main.Logo;
import com.example.guswn.allthatlyrics.R;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.guswn.allthatlyrics.Home.Frag1_friends.MyAdapter_Friend.AddedChatPeopleList;
import static com.example.guswn.allthatlyrics.Main.Logo.MY_EMAIL_2;
import static com.example.guswn.allthatlyrics.Main.Logo.MY_IDX;
import static com.example.guswn.allthatlyrics.MainActivity.NodeServer;
import static com.example.guswn.allthatlyrics.MainActivity.URL;
import static com.example.guswn.allthatlyrics.MainActivity.URL_withoutslash;

public class Home extends AppCompatActivity {

    @BindView(R.id.bottomNavigationView_main_menu)
    BottomNavigationView bottomNavigationView;
    @BindView(R.id.fragment_container)
    FrameLayout fragment_container;

    private BackPressCloseHandler backPressCloseHandler;
    HomeAPI api;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        backPressCloseHandler = new BackPressCloseHandler(this);

        //레트로핏
        api = new MyRetrofit().create(HomeAPI.class);
        //레트로핏

        bottomNavigationView.setOnNavigationItemSelectedListener(navlistner);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new Home_fragment1()).commit();
        onSocketConnect();
    }
   public static Socket mSocket;
    boolean isConnected;
    {
        try {
            mSocket = IO.socket(NodeServer);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT,onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT,onDisConnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR,onConnectionError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT,onConnectionError);
        mSocket.off("chat message",onNewMessage);
        super.onDestroy();
    }

    private void onSocketConnect() {
        mSocket.on(Socket.EVENT_CONNECT,onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT,onDisConnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR,onConnectionError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT,onConnectionError);
        mSocket.on("chat message",onNewMessage);
        mSocket.connect();
    }
    private BottomNavigationView.OnNavigationItemSelectedListener navlistner =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFrag = null;

                    switch (menuItem.getItemId()) {

                        case R.id.menu_home:
                            selectedFrag = new Home_fragment1();
                            break;
                        case R.id.menu_social:
                            selectedFrag = new Home_fragment2_social();
                            break;
                        case R.id.menu_account:
                            selectedFrag = new Home_fragment3();
                            break;
                        case R.id.menu_chat:
                            selectedFrag = new Home_fragment4();
                            break;
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,selectedFrag).commit();
                    return true;
                }
            };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == RESULT_OK){
            Fragment selectedFrag = null;
            Log.e("Home_onActivityResult_requestCode",""+requestCode);
            switch (requestCode){
                // testActivity 에서 요청할 때 보낸 요청 코드 ()
                case 1:
                    selectedFrag = new Home_fragment1();
                    break;
                case 2:
                    selectedFrag = new Home_fragment2_social();
                    break;
                case 3:
                    selectedFrag = new Home_fragment3();
                    break;
                case 4:
                    selectedFrag = new Home_fragment4();
                    AddedChatPeopleList = new ArrayList<>();// 이걸 해줘야 static Arraylist 를 청소 할 수 있다
                    break;
            }
            //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,selectedFrag).commit();
            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, selectedFrag).addToBackStack(null);
            fragmentTransaction.commitAllowingStateLoss(); //fragmentTransaction.commit(); 하면 IllegalExceptionError 뜬다
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("1_dataRecieved_Home ","onNewMessage_Home");
                    JSONObject dataRecieved = (JSONObject) args[0];
                    String useridx,message,chatroomidx,isfile,unreadCOUNT;
                    JSONObject unreadcount_list;
                    try{
                        isfile = dataRecieved.getString("chat_isfile");
                        useridx = dataRecieved.getString("chat_useridx");
                        message = dataRecieved.getString("chat_message");
                        chatroomidx = dataRecieved.getString("chatroom_idx");
                        unreadcount_list = dataRecieved.getJSONObject("chat_unreadcount_list");
                        unreadCOUNT =unreadcount_list.get(MY_IDX).toString();
                    }catch (JSONException e){
                        e.printStackTrace();
                        Log.e("2_dataRecieved_Home ","JSONException : "+e);
                        return;
                    }


                    if (message!=null) {
                        Log.e("2_dataRecieved_Home_Home ", isfile + "/" + useridx + "/" + message + "/" + chatroomidx+"/"+unreadcount_list+"/"+unreadCOUNT);
                        Log.e("unreadcount_list.toString() ",unreadcount_list.toString());
                        Date TODAY = new Date();
                        SimpleDateFormat TIME = new SimpleDateFormat("hh:mm");
                        String  chattime = TIME.format(TODAY);

//                        /**실험 성공*/
//                        outidx= chatroomidx;
//                        outmessage = message;
//                        outtime = chattime;
//                        unReadMessage = unreadCOUNT;
//                        Home_fragment4.myAdapter.notifyDataSetChanged();

                        makeunreadCount_peridx(useridx,chatroomidx,unreadcount_list.toString());
//                        /***/
                    }

                }
            });
        }
    };
    public void makeunreadCount_peridx(String useridx,String chatroomidx,String unreadcount_list){
        Map<String ,String > map = new HashMap<>();
        map.put("user_idx",useridx);
        map.put("chatroom_idx",chatroomidx);
        map.put("unreadcount_list",unreadcount_list);

        Call<UnReadCountResponse> call = api.makeunreadCount_perRoomidx(map);

        call.enqueue(new Callback<UnReadCountResponse>() {
            @Override
            public void onResponse(Call<UnReadCountResponse> call, Response<UnReadCountResponse> response) {
                if(!response.isSuccessful()){
                    Log.e("Home_makeunreadCount_peridx_code",""+response.code());
                    return;
                }
                UnReadCountResponse val = response.body();
                String value = val.getValue();
                String message = val.getMessage();
                String chatroom_idx = val.getChatroom_idx();
                String user_idx = val.getUser_idx();
                String unread_count = val.getUnread_count();
                Log.e("Home_makeunreadCount_peridx ",value+"/"+message+"/"+chatroom_idx+"/"+user_idx+"/"+unread_count);
            }

            @Override
            public void onFailure(Call<UnReadCountResponse> call, Throwable t) {
                Log.e("Home_makeunreadCount_peridx_fail","Error : "+t.getMessage());
            }
        });

    }
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!isConnected){
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("connect_chat_useremail" , MY_EMAIL_2);
                            jsonObject.put("connect_chat_useridx" , Logo.MY_IDX);
                            jsonObject.put("connect_chatroom_idx","no_chatroom");

                            mSocket.emit("connect_user",jsonObject);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Toast.makeText(Home.this,"Connected home",Toast.LENGTH_SHORT).show();
                        isConnected=true;

                    }
                }
            });
        }
    };
    private Emitter.Listener onDisConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isConnected=false;
                    Toast.makeText(Home.this,"DisConnected home",Toast.LENGTH_SHORT).show();
                }
            });
        }
    };
    private Emitter.Listener onConnectionError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Toast.makeText(Home.this,"ConnetionError home",Toast.LENGTH_SHORT).show();
                }
            });
        }
    };
}
