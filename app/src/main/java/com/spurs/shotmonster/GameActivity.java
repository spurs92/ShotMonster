package com.spurs.shotmonster;

import android.content.Intent;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

public class GameActivity extends AppCompatActivity {

    GameView gameView;

    TextView tvScore;
    TextView tvCoin;
    TextView tvGem;
    TextView tvBomb;
    TextView tvChampion;

    View dialog=null;

    MediaPlayer mp;

    ToggleButton toggle_music,toggle_sound,toggle_vibrate;

    ImageView imgCoin,imgGem,imgBomb,btnQuit;
    ImageView check,btnMusic,btnSound,btnVibrate;
    ImageView btnQuitok,btnQuitcancle;
    ImageView lastCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_game);

        gameView=(GameView)findViewById(R.id.gameview);

        tvScore=(TextView)findViewById(R.id.tv_score);
        tvCoin=(TextView)findViewById(R.id.tv_coin);
        tvGem=(TextView)findViewById(R.id.tv_gem);
        tvBomb=(TextView)findViewById(R.id.tv_bomb);
        tvChampion=(TextView)findViewById(R.id.tv_champion);

        imgCoin=(ImageView) findViewById(R.id.img_coin);
        imgGem=(ImageView) findViewById(R.id.img_gem);
        imgBomb=(ImageView) findViewById(R.id.img_bomb);
        btnQuit=(ImageView) findViewById(R.id.btn_quit);

        btnMusic=(ImageView)findViewById(R.id.img_btn_music);
        check=(ImageView) findViewById(R.id.btn_shop_check);
        btnSound=(ImageView) findViewById(R.id.img_btn_sound);
        btnVibrate=(ImageView) findViewById(R.id.img_btn_vibrate);
        btnQuitok=(ImageView) findViewById(R.id.btn_quit_ok);
        btnQuitcancle=(ImageView) findViewById(R.id.btn_quit_cancle);
        lastCheck=(ImageView) findViewById(R.id.btn_setting_check);

        Glide.with(this).load(R.drawable.item_0_coin).into(imgCoin);
        Glide.with(this).load(R.drawable.item_1_gem).into(imgGem);
        Glide.with(this).load(R.drawable.item_5_bomb).into(imgBomb);
        Glide.with(this).load(R.drawable.btn_quit).into(btnQuit);
        Glide.with(this).load(R.drawable.ui_setting_label_music).into(btnMusic);
        Glide.with(this).load(R.drawable.check).into(check);
        Glide.with(this).load(R.drawable.ui_setting_label_sound).into(btnSound);
        Glide.with(this).load(R.drawable.ui_setting_label_vibrate).into(btnVibrate);
        Glide.with(this).load(R.drawable.select_ok).into(btnQuitok);
        Glide.with(this).load(R.drawable.select_cancel).into(btnQuitcancle);
        Glide.with(this).load(R.drawable.check).into(lastCheck);


        mp=MediaPlayer.create(this,R.raw.my_friend_dragon);
        mp.setLooping(true);

        toggle_music=(ToggleButton)findViewById(R.id.toggle_music);
        toggle_sound=(ToggleButton)findViewById(R.id.toggle_sound);
        toggle_vibrate=(ToggleButton)findViewById(R.id.toggle_vibrate);

        toggle_music.setOnCheckedChangeListener(checkedChangeListener);
        toggle_sound.setOnCheckedChangeListener(checkedChangeListener);
        toggle_vibrate.setOnCheckedChangeListener(checkedChangeListener);

        toggle_music.setChecked(G.isMusic);
        toggle_sound.setChecked(G.isSound);
        toggle_vibrate.setChecked(G.isVibrate);

    }

    CompoundButton.OnCheckedChangeListener checkedChangeListener=new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()){
                case R.id.toggle_music:
                    G.isMusic=isChecked;
                    buttonView.setChecked(G.isMusic);

                    if(G.isMusic) mp.setVolume(0.7f, 0.7f);
                    else mp.setVolume(0,0);
                    break;
                case R.id.toggle_sound:
                    G.isSound=isChecked;
                    buttonView.setChecked(G.isSound);
                    break;
                case R.id.toggle_vibrate:
                    G.isVibrate=isChecked;
                    buttonView.setChecked(G.isVibrate);
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if(mp!=null && !mp.isPlaying()){
            if(G.isMusic) mp.setVolume(0.7f,0.7f);
            else mp.setVolume(0,0);

            mp.start();
        }
    }

    @Override
    protected void onPause() {
        gameView.pauseGame();
        super.onPause();
        if(mp!=null && mp.isPlaying()) mp.pause();
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

    void hideDialog(){
        Animation ani=AnimationUtils.loadAnimation(this,R.anim.disappear_dialog);
        ani.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dialog.setVisibility(View.GONE);
                dialog=null;
                gameView.resumeGame();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        dialog.startAnimation(ani);
    }

    //각 다이얼로그의 안에 있는 버튼을 클릭했을때 발동하는 메소드
    public void clickBtn(View v){
        switch (v.getId()){
            case R.id.btn_shop_check:
                hideDialog();
                break;

            case R.id.btn_setting_check:
                hideDialog();
                break;

            case R.id.btn_quit_ok:
                gameView.stopGame();
                finish();
                break;
            case R.id.btn_quit_cancle:
                dialog.setVisibility(View.GONE);
                dialog=null;

                gameView.resumeGame();
                break;
            case R.id.btn_play:
                Animation ani=AnimationUtils.loadAnimation(this,R.anim.disappear_dialog_pause);
                ani.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        dialog.setVisibility(View.GONE);
                        dialog=null;

                        gameView.resumeGame();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                dialog.startAnimation(ani);

                break;
        }
    }

    void showQuitDialog(){
        if(dialog!=null) return;
        gameView.pauseGame();
        dialog=findViewById(R.id.dialog_quit);
        dialog.setVisibility(View.VISIBLE);
        Animation ani= AnimationUtils.loadAnimation(this,R.anim.appear_dialog_quit);
        dialog.startAnimation(ani);
    }

    void showPauseDialog(){
        if(dialog!=null) return;

        gameView.pauseGame();
        dialog=findViewById(R.id.dialog_pause);
        dialog.setVisibility(View.VISIBLE);
        Animation ani=AnimationUtils.loadAnimation(this, R.anim.appear_dialog_pause);
        dialog.startAnimation(ani);
    }

    void showMyDialog(int viewId){
        if(dialog!=null) return;
        gameView.pauseGame();
        dialog=findViewById(viewId);
        dialog.setVisibility(View.VISIBLE);
        Animation ani=AnimationUtils.loadAnimation(this,R.anim.appear_dialog);
        dialog.startAnimation(ani);
    }

    //뒤로가기 버튼 클릭했을 때 자동으로 호출되는 메소드
    @Override
    public void onBackPressed() {
        //quit 다이얼로그 보이기
        showQuitDialog();
    }

    public void clickQuit(View v){
        //quit 다이얼로그 보이기
        showQuitDialog();
    }

    public void clickPause(View v){
        showPauseDialog();
    }

    public void clickShop(View v){
        showMyDialog(R.id.dialog_shop);
    }

    public void clickSetting(View v){
        showMyDialog(R.id.dialog_setting);
    }

    //GameThread의 메세지를 전달할 Handler객체
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {

            gameView.stopGame();

            Bundle data=msg.getData();

            Intent intent=new Intent(GameActivity.this,GameoverActivity.class);
            intent.putExtra("Data",data);

            startActivity(intent);

            finish();
        }
    };

}
