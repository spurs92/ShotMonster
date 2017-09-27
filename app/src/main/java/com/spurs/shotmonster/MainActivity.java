package com.spurs.shotmonster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.display.VirtualDisplay;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {

    MediaPlayer mp;

    ImageView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        loadData();

        mp=MediaPlayer.create(this,R.raw.dragon_flight);
        mp.setLooping(true);

        title=(ImageView)findViewById(R.id.title);

        Glide.with(this).load(R.drawable.logo_main_title).into(title);

    }

    @Override
    protected void onResume() {
        if(mp!=null && !mp.isPlaying()){
            if(G.isMusic) mp.setVolume(0.5f,0.5f);
            else mp.setVolume(0,0);
            mp.start();
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        if(mp!=null && mp.isPlaying()) mp.pause();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if(mp!=null){
            mp.stop();
            mp.release();
            mp=null;
        }
        super.onDestroy();
    }

    void loadData(){
        SharedPreferences pref=getSharedPreferences("data", MODE_PRIVATE);
        G.gem =pref.getInt("Gem", 0);
        G.champion=pref.getInt("Champion",0);
        G.kind=pref.getInt("Kind",0);

        G.imgUri=pref.getString("ImgUri",null);

        G.isMusic=pref.getBoolean("Music",true);
        G.isSound=pref.getBoolean("Sound",true);
        G.isVibrate=pref.getBoolean("Vibrate",true);
    }

    public void clickStart(View v){
        //GameActivity실행
        Intent intent=new Intent(this,GameActivity.class);
        startActivity(intent);

        //액티비티전환시 애니메이션 효과
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }


    public void clickExit(View v){
        finish();
    }
}
