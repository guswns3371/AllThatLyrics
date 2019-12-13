package com.example.guswn.allthatlyrics.Home.Frag4_chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.guswn.allthatlyrics.Extension.MyRetrofit;
import com.example.guswn.allthatlyrics.Home.Frag3_account.Value_3;
import com.example.guswn.allthatlyrics.Home.Home;
import com.example.guswn.allthatlyrics.Main.Logo;
import com.example.guswn.allthatlyrics.MainActivity;
import com.example.guswn.allthatlyrics.R;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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


import static com.example.guswn.allthatlyrics.Home.Frag4_chat.MyAdapter_Chat.outidx;
import static com.example.guswn.allthatlyrics.Home.Frag4_chat.MyAdapter_Chat.outmessage;
import static com.example.guswn.allthatlyrics.Home.Frag4_chat.MyAdapter_Chat.outtime;
import static com.example.guswn.allthatlyrics.Home.Frag4_chat.MyAdapter_Chat.unReadMessage;
import static com.example.guswn.allthatlyrics.Home.Frag4_chat.MyAdapter_InnerChat.AllPeople;
import static com.example.guswn.allthatlyrics.Home.Frag4_chat.MyAdapter_InnerChat.isRead;
import static com.example.guswn.allthatlyrics.Home.Frag4_chat.MyAdapter_InnerChat.isReadList;
import static com.example.guswn.allthatlyrics.Main.Logo.MY_EMAIL_2;
import static com.example.guswn.allthatlyrics.Main.Logo.MY_IDX;
import static com.example.guswn.allthatlyrics.MainActivity.NodeServer;
import static com.example.guswn.allthatlyrics.MainActivity.URL;
import static com.example.guswn.allthatlyrics.MainActivity.URL_withoutslash;

public class Home_fragment4 extends Fragment {

    LinearLayoutManager mLayoutManager;
    ChatAPI api;
    public static MyAdapter_Chat myAdapter;

    ArrayList<ChatInfo> chatInfos;
    boolean isConnected;
    Socket mSocket;
    {
        try {
//         mSocket = IO.socket("https://socket-io-chat.now.sh/");
            mSocket = IO.socket(NodeServer);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }



    @BindView(R.id.chat_recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.chat_tb)
    Toolbar chat_tb;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_home_fragment4,container,false);
        ButterKnife.bind(this,view);
        Log.e("Home_fragment4","4");
        //툴바
        Home activity = (Home) getActivity();
        activity.setSupportActionBar(chat_tb);
        ActionBar actionBar = ((Home) getActivity()).getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        setHasOptionsMenu(true);
        //툴바
//        onSocketConnect();

        api = new MyRetrofit().create(ChatAPI.class);
        //레트로핏

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        //리스트 역순 배열
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        //리스트 역순 배열
        mRecyclerView.setLayoutManager(mLayoutManager);
        chatInfos = new ArrayList<>();

        // chatInfos.add(new ChatInfo("1","title","innercontent","time",null));
        myAdapter = new MyAdapter_Chat(chatInfos,getActivity());
        mRecyclerView.setAdapter(myAdapter);

        loadChatRoomList();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            mSocket = IO.socket(NodeServer);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        onSocketConnect();
    }

    private void onSocketConnect() {
        mSocket.on(Socket.EVENT_CONNECT,onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT,onDisConnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR,onConnectionError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT,onConnectionError);
        mSocket.on("chat message",onNewMessage);
        mSocket.connect();
    }
    @Override
    public void onPause() {
        super.onPause();
        Log.e("onPause","onPause");
        //mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT,onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT,onDisConnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR,onConnectionError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT,onConnectionError);
        mSocket.off("chat message",onNewMessage);
        outidx = null;
        outmessage = null;
        outtime = null;
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
                        return;
                    }


                    if (message!=null) {
                        Log.e("dataRecieved_Home_frag4 ", isfile + "/" + useridx + "/" + message + "/" + chatroomidx+"/"+unreadcount_list+"/"+unreadCOUNT);
                        Date TODAY = new Date();
                        SimpleDateFormat TIME = new SimpleDateFormat("hh:mm");
                        String  chattime = TIME.format(TODAY);
                        /***/
                        outidx= chatroomidx;
                        if (isfile.equals("yes")){
                            outmessage="사진";
                        }else if (isfile.equals("no")){
                            outmessage = message;
                        }
                        outtime = chattime;
                        unReadMessage = unreadCOUNT;
                        myAdapter.notifyDataSetChanged();
                        /***/

                    }

                }
            });
        }
    };
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
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

                        Toast.makeText(getActivity(),"Connected list",Toast.LENGTH_SHORT).show();
                        isConnected=true;

                    }
                }
            });
        }
    };
    private Emitter.Listener onDisConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isConnected=false;
                    Toast.makeText(getActivity(),"DisConnected list",Toast.LENGTH_SHORT).show();
                }
            });
        }
    };
    private Emitter.Listener onConnectionError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Toast.makeText(getActivity(),"ConnetionError list",Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.home_chat_tollbar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_toolbar_search :
                Intent intent = new Intent(getActivity(), ChatAddActivity.class);
                getActivity().startActivityForResult(intent,4);
                // Toast.makeText(getActivity(),"setting",Toast.LENGTH_SHORT).show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void loadChatRoomList(){
        Call<ChatResponse> call = api.getChatRoomList();
        final ProgressDialog progressDoalog;
        progressDoalog = new ProgressDialog(getActivity());
        progressDoalog.setMax(100);
        progressDoalog.setMessage("Please Wait");
        progressDoalog.setTitle("Chatting Information Loading...");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // show it
        progressDoalog.show();
        call.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if(!response.isSuccessful()){
                    Log.e("loadChatRoomList_code",""+response.code());
                    return;
                }
                progressDoalog.dismiss();

                ChatResponse res = response.body();
                List<ChatResponse> list = res.getChatList();
                String chatpeopleNUM = res.getChatpeoplenum();
                for(ChatResponse c : list){
                    String idx = c.getChatroom_idx();
                    String chatroomname = c.getChatroomname();
                    String chatroomphoto = c.getChatroomphoto();
                    String chatroomouttime = c.getChatroomouttime();
                    String chatroomoutmessage = c.getChatroomoutmessage();

                    String chatunreadlist = c.getChatunreadlist(); // {"유저idx" : 안읽은 수}
                    Log.e("1_loadChatRoomList ",idx+"/"+chatroomoutmessage+"/"+chatunreadlist);
                    String unreadcount = null;
                    if (chatunreadlist!=null){
                        JsonParser parser = new JsonParser();
                        /**jsonTree 로 제이슨 오브젝트화 시킨다*/
                        JsonElement jsonTree = parser.parse(chatunreadlist);
                        if (jsonTree.isJsonObject()){
                            JsonObject jsonObject = jsonTree.getAsJsonObject();// 제인슨오브젝트 형태를 띤 String 을 JSONObject로 바꿔줌
                            if (jsonObject.get(MY_IDX)!=null){
                                unreadcount = jsonObject.get(MY_IDX).toString();
                            }
                            Log.e("2_loadChatRoomList ",unreadcount+"<>");
                        }else {
                            Log.e("2_loadChatRoomList ","fuck!!");
                        }
                    }


                    // 리사이클러뷰 리스트를 순서를 역순으로 하고 싶을떄
                    //         mLayoutManager.setReverseLayout(true);
                    //        mLayoutManager.setStackFromEnd(true);
                    //를 추가해라
                    String NAMES ="";
                    NAMES +=" (";
                    List<Value_3> ChatUserInfoList = c.getChatUserInfoList();
                    String photo_new= ChatUserInfoList.get(0).getPhoto();

                    /** 단톡방 이름*/
                    for(Value_3 val :ChatUserInfoList){
                        String useridx = val.getIdx();
                        String username = val.getUsername();
                        NAMES += username;
                        NAMES +=",";
                        if(!useridx.equals(MY_IDX)){
                            if(ChatUserInfoList.size()>2){
                                chatroomname = "그룹채팅"+ChatUserInfoList.size();
                            }else {
                                chatroomname = username;
                            }
                            photo_new= val.getPhoto();
                        }
                    }
                    NAMES +=")";
                    /** 단톡방 이름*/

                    /** 데이터 삽입부분*/
                    for(Value_3 val :ChatUserInfoList ){
                        String useridx = val.getIdx();
                        if(useridx.equals(MY_IDX)){
                            ChatInfo info = new ChatInfo(idx,chatroomname+NAMES,chatroomoutmessage,chatroomouttime,photo_new,ChatUserInfoList.size()+"",unreadcount);
                            info.setIsLoaded("loaded");
                            chatInfos.add(info);
                        }
                    }
                    /** 데이터 삽입부분*/
                }

                myAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                progressDoalog.dismiss();
                Log.e("loadChatRoomList_fail","Error : "+t.getMessage());
            }
        });
    }

}
