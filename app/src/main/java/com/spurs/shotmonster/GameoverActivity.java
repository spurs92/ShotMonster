package com.spurs.shotmonster;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class GameoverActivity extends AppCompatActivity {

    TextView tvChampion;
    TextView tvScore;

    ImageView imgChampion;
    ImageView btnChampion;
    ImageView btnScore;

    boolean isChampion=false; //챔피언인가?


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameover);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        tvChampion=(TextView) findViewById(R.id.tv_champion);
        tvScore=(TextView)findViewById(R.id.tv_score);
        btnChampion=(ImageView)findViewById(R.id.btn_champion);
        btnScore=(ImageView)findViewById(R.id.btnScore);

        imgChampion=(ImageView)findViewById(R.id.img_champion);

        Glide.with(this).load(R.drawable.ui_gameover_label_champion).into(btnChampion);
        Glide.with(this).load(R.drawable.ui_gameover_label_yourscore).into(btnScore);

        Intent intent=getIntent();
        Bundle data=intent.getBundleExtra("Data");

        int score=data.getInt("Score",0);
        int coin =data.getInt("Coin",0);

        int yourscore=score+coin*10;
        String s=String.format("%07d",yourscore);
        tvScore.setText(s);

        if(yourscore>G.champion){
            //챔피언 점수 갱신
            G.champion=yourscore;
            isChampion=true;
        }

        s=String.format("%07d",G.champion);
        tvChampion.setText(s);

        //챔피언 이미지 표시
        if(G.imgUri!=null){
            Uri uri=Uri.parse(G.imgUri);
            imgChampion.setImageURI(uri);
        }

    }

    void saveData(){
        //data.xml이라는 문서에 저장
        SharedPreferences pref=getSharedPreferences("data",MODE_PRIVATE);
        SharedPreferences.Editor editor=pref.edit();
        editor.putInt("Gem",G.gem);
        editor.putInt("Champion",G.champion);
        editor.putInt("Kind",G.kind);
        editor.putString("ImgUri",G.imgUri);
        editor.putBoolean("Music",G.isMusic);
        editor.putBoolean("Sound",G.isSound);
        editor.putBoolean("Vibrate",G.isVibrate);
        editor.commit();
    }

    @Override
    protected void onDestroy() {
        saveData(); // 별도의 파일에 저장
        super.onDestroy();
    }

    public void clickChampion(View v){
        if(!isChampion) return;

        //내 스마트폰에 있는 사진을 선택할 수 있도록
        //사진보기 앱(갤러리 or 사진)을 실행

        if(Build.VERSION.SDK_INT<19){ //API19버전 미만(kiket미만)

            Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent,10);

        }else { //API 19이상
            Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent,10);
        }
    }

    //startActivityForResult() 메소드의 호출로 실행되었던 화면(Activity)가 종료되어 이 엑티비티가 다시 보이면 자동으로 실행되는 메소드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode){
            case 10:
                if(resultCode==RESULT_OK){
                    Uri uri=data.getData();
                    imgChampion.setImageURI(uri);

                    Glide.with(this).load(uri).into(imgChampion);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void clickRetry(View v){
        Intent intent=new Intent(this,GameActivity.class);
        startActivity(intent);
        finish();
    }

    public void clickExit(View v){
        finish();
    }
}
