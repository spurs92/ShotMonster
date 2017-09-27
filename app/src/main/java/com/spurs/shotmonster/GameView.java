package com.spurs.shotmonster;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by alfo00 on 2017-06-26.
 */

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    Context context;
    SurfaceHolder holder; //공장장 객체 참조변수

    int width, height; //이 게임뷰의 화면사이즈

    GameThread gThread; //게임작업을 진행하는 하청직원객체

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context= context;

        holder= getHolder(); //공장장 객체 얻어오기
        holder.addCallback(this);

    }//constructor....

    //생성자메소드 실행 후 자동으로 실행되는 메소드
    //이 GameView가 화면에 보이면 자동으로 실행되는 메소드..
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    //surfaceCreated()메소드가 실행된 후 자동으로 실행되는 메소드..
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //게임 시작!!!
        if(gThread==null){//직원객체가 없는가???
            this.width= getWidth();
            this.height= getHeight();

            gThread= new GameThread();//하청직원 객체 생성
            gThread.start();//작업 시작!!(run메소드 실행!)
        }else{
            //이어하기..(Resume..)
            gThread.resumeThread();
        }

    }

    //이 GameView가 화면에 보이지 않으면 자동으로 실행되는 메소드...
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action= event.getAction();
        int x=0, y=0;

        switch ( action ){
            case MotionEvent.ACTION_DOWN:
                x= (int) event.getX();
                y= (int) event.getY();
                gThread.touchDown(x, y);
                break;

            case MotionEvent.ACTION_UP:
                x= (int) event.getX();
                y= (int) event.getY();
                gThread.touchUp(x, y);
                break;

            case MotionEvent.ACTION_MOVE:
                x= (int) event.getX();
                y= (int) event.getY();
                gThread.touchMove(x, y);
                break;
        }

        return true;
    }


    void stopGame(){
        gThread.stopThread();
    }

    void pauseGame(){
        gThread.pauseThread();
    }

    void resumeGame(){
        gThread.resumeThread();
    }



    //Inner class..
    //실제 게임을 진행하는 클래스/////////////////////////////
    class GameThread extends Thread{

        /////FPS조절용 변수들////////////////////////////
        int FPS=60; //초당 60장
        int frameTime= 1000/FPS;   //1장당 기준시간:16ms
        long loopTime;   //while문 1번 도는데 걸린 시간..
        long sleepTime;  //잠잘시간
        long lastTime;  //이전시간
        long currTime;  //현재시간

        int skippedFrame;   //건너띈 프레임 수
        //////////////////////////////////////////////////

        boolean isRun= true;
        boolean isWait= false;

        Bitmap imgBack;
        Bitmap imgJoypad;
        Bitmap[] imgMissile= new Bitmap[3];
        Bitmap[][] imgPlayer= new Bitmap[3][4];
        Bitmap[][] imgEnemy= new Bitmap[3][4];
        Bitmap[][] imgGauge= new Bitmap[2][];
        Bitmap[] imgDust= new Bitmap[6];
        Bitmap[] imgItem= new Bitmap[7];
        Bitmap imgProtect;
        Bitmap imgStrong;
        Bitmap imgBombBtn;

        //폭탄버튼 관련변수
        boolean isBomb=false;  //폭탄버튼이 눌렸는가?
        Rect recBomb;          //폭탄버튼의 사각형영역의 화면상의 좌표
        int bomb=3;            //폭탄개수

        int protRad;    //보호막이미지의 반지름
        int protAngle;  //보호막 이미지의 회전각도

        //조이패드 관련변수
        int jpx, jpy, jpr;
        boolean isJoypad= false; //조이패드를 눌렀는가?

        Paint alphaPaint= new Paint(); //투명도 조절용 Paint객체체

        int backPos=0; //배경이미지 x좌표
        Random rnd= new Random();

        Player me;

        ArrayList<Missile> missiles= new ArrayList<>();
        ArrayList<Enemy> enemies= new ArrayList<>();
        ArrayList<Dust> dusts= new ArrayList<>();
        ArrayList<Item> items= new ArrayList<>();

        int level=1;  //게임의 레벨(1~10)

        int missileGap=3;  //미사일이 발사되는 간격
        int missileTime= missileGap;

        int fastTime=0;   //fast아이템의 지속시간...
        int protectTime=0;//protect아이템의 지속시간...
        int magnetTime=0; //magnet아이템의 지속시간...
        int strongTime=0; //strong아이템의 지속시간...

        int score=0;
        int coin=0;

        //효과음에 관련된 변수들
        SoundPool sp;
        int sd_chdie, sd_bomb,sd_fire,sd_coin,sd_gem,sd_prot,sd_item,sd_mondie;

        //진동관리자
        Vibrator vibrator;

        //비트맵 객체를 생성하는 작업메소드!
        void createBitmaps(){
            Resources res= getResources();
            Bitmap img=null;

            //폭탄버튼 이미지
            int size= height/5;
            img= BitmapFactory.decodeResource(res, R.drawable.btn_bomb);
            imgBombBtn= Bitmap.createScaledBitmap(img, size, size, true);
            img.recycle(); img=null;
            //폭탄버튼의 화면상의 위치좌표 관리객체
            recBomb= new Rect(width-size, height-size, width, height);

            //강화미사일 이미지
            img= BitmapFactory.decodeResource(res, R.drawable.bullet_04);
            imgStrong= Bitmap.createScaledBitmap(img, height/10, height/10, true);
            img.recycle(); img=null;

            //보호막 이미지
            img= BitmapFactory.decodeResource(res, R.drawable.effect_protect);
            imgProtect= Bitmap.createScaledBitmap(img, height/4, height/4, true);
            img.recycle(); img=null;
            protRad= imgProtect.getWidth()/2; //이미지 반지름..

            //아이템 이미지
            for(int i=0; i<7; i++){
                img= BitmapFactory.decodeResource(res, R.drawable.item_0_coin+i);
                imgItem[i]= Bitmap.createScaledBitmap(img, height/16, height/16, true);
                img.recycle(); img=null;
            }

            //Dust 이미지
            float[] r= new float[]{0.5f, 0.7f, 1.0f, 1.4f, 1.8f, 2.2f};
            for(int i=0; i<imgDust.length; i++){
                img= BitmapFactory.decodeResource(res, R.drawable.dust);
                imgDust[i]= Bitmap.createScaledBitmap(img, (int)(height/9*r[i]), (int)(height/9*r[i]), true);
            }

            //Gauge 이미지
            imgGauge[0]= new Bitmap[5];
            for(int i=0; i<5; i++){
                img= BitmapFactory.decodeResource(res, R.drawable.gauge_step5_01+i);
                imgGauge[0][i]= Bitmap.createScaledBitmap(img, height/9, height/36, true);
                img.recycle(); img=null;
            }

            imgGauge[1]= new Bitmap[3];
            for(int i=0; i<3; i++){
                img= BitmapFactory.decodeResource(res, R.drawable.gauge_step3_01+i);
                imgGauge[1][i]= Bitmap.createScaledBitmap(img, height/9, height/36, true);
                img.recycle(); img=null;
            }


            //미사일 이미지
            for(int i=0; i<imgMissile.length; i++){
                img= BitmapFactory.decodeResource(res, R.drawable.bullet_01+i);
                imgMissile[i]= Bitmap.createScaledBitmap(img, height/10, height/10, true);
                img.recycle(); img=null;
            }

            //조이패드 이미지
            img= BitmapFactory.decodeResource(res, R.drawable.img_joypad);
            imgJoypad= Bitmap.createScaledBitmap(img, height/2, height/2, true);
            img.recycle(); img=null;

            //배경이미지
            int n= rnd.nextInt(6); //0~5
            img= BitmapFactory.decodeResource(res, R.drawable.back_01+n);
            imgBack= Bitmap.createScaledBitmap(img, width, height, true);
            img.recycle(); img=null;

            //적군 이미지
            for(int i=0; i<3; i++){
                for(int k=0; k<3; k++){
                    img= BitmapFactory.decodeResource(res, R.drawable.enemy_a_01+k+i*3);
                    imgEnemy[i][k]= Bitmap.createScaledBitmap(img, height/9, height/9, true);
                    img.recycle(); img=null;
                }
                imgEnemy[i][3]= imgEnemy[i][1];
            }

            //플레이어 이미지
            //Red
            for(int i=0; i<3; i++){
                img= BitmapFactory.decodeResource(res, R.drawable.char_a_01+i);
                imgPlayer[0][i]= Bitmap.createScaledBitmap(img, height/8, height/8, true);
                img.recycle(); img=null;
            }
            imgPlayer[0][3]= imgPlayer[0][1];

            //Purple
            for(int i=0; i<3; i++){
                img= BitmapFactory.decodeResource(res, R.drawable.char_b_01+i);
                imgPlayer[1][i]= Bitmap.createScaledBitmap(img, height/8, height/8, true);
                img.recycle(); img=null;
            }
            imgPlayer[1][3]= imgPlayer[1][1];

            //Black
            for(int i=0; i<3; i++){
                img= BitmapFactory.decodeResource(res, R.drawable.char_c_01+i);
                imgPlayer[2][i]= Bitmap.createScaledBitmap(img, height/8, height/8, true);
                img.recycle(); img=null;
            }
            imgPlayer[2][3]= imgPlayer[2][1];


        }

        //모든 자원들(Bitmap ....) 메모리 제거..
        void removeResources(){
            imgBack.recycle(); imgBack=null;
            imgJoypad.recycle(); imgJoypad=null;

            //강화미사일 제거
            imgStrong.recycle(); imgStrong=null;

            //보호막이미지 제거
            imgProtect.recycle(); imgProtect=null;

            //아이템이미지 제거
            for(int i=0; i<imgItem.length; i++){
                imgItem[i].recycle();
                imgItem[i]=null;
            }

            //먼지이미지 제거
            for(int i=0; i<imgDust.length; i++){
                imgDust[i].recycle();
                imgDust[i]=null;
            }

            //Gauge이미지 제거
            for(int i=0; i<imgGauge.length; i++){
                for(int k=0; k<imgGauge[i].length; k++){
                    imgGauge[i][k].recycle();
                    imgGauge[i][k]=null;
                }
            }

            //미사일 제거
            for(int i=0; i<imgMissile.length; i++){
                imgMissile[i].recycle();
                imgMissile[i]=null;
            }

            //적군들 제거.
            for(int i=0; i<3; i++){
                for(int k=0; k<3; k++){
                    imgEnemy[i][k].recycle();
                    imgEnemy[i][k]=null;
                }
                imgEnemy[i][3]=null;
            }

            //플레이어 Bitmap제거
            for(int i=0; i<3; i++){
                for(int k=0; k<3; k++){
                    imgPlayer[i][k].recycle();
                    imgPlayer[i][k]=null;
                }
                imgPlayer[i][3]=null;
            }
        }

        //게임이 시작할 때 설정할 각종 초기값 작업 메소드
        void init(){
            //플레이어 객체 생성
            me= new Player(width, height, imgPlayer, G.kind);

            //조이패드 초기값 설정
            jpr= imgJoypad.getWidth()/2;
            jpx= jpr;
            jpy= height-jpr;

            //텍스트뷰에 값 설정(score, coin....etc)
            setTextViewValue();

            //효과음 등록
            sp=new SoundPool(10, AudioManager.STREAM_MUSIC,0);
            sd_chdie=sp.load(context,R.raw.ch_die,1);
            sd_bomb=sp.load(context,R.raw.explosion_bomb,1);
            sd_fire=sp.load(context,R.raw.fireball,1);
            sd_coin=sp.load(context,R.raw.get_coin,1);
            sd_gem=sp.load(context,R.raw.get_gem,1);
            sd_prot=sp.load(context,R.raw.get_invincible,1);
            sd_item=sp.load(context,R.raw.get_item,1);
            sd_mondie=sp.load(context,R.raw.mon_die,1);

            //진동관리자 객체 얻어오기
            vibrator=(Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);

        }

        //텍스트뷰에 값 설정하는 작업 메소드
        void setTextViewValue(){

            //UI변경 작업은 UI Thread(Main Thread)만 가능
            //Activity클래스의 runOnuiThread()와 같은 능력의 메소드
            post(new Runnable() {
                @Override
                public void run() {
                    GameActivity ac=(GameActivity) context;

                    String s=null;
                    s=String.format("%07d",score);
                    ac.tvScore.setText(s);

                    s=String.format("%04d",score);
                    ac.tvCoin.setText(s);

                    s=String.format("%04d",G.gem);
                    ac.tvGem.setText(s);

                    s=String.format("%04d",bomb);
                    ac.tvBomb.setText(s);

                    s=String.format("%07d",G.champion);
                    ac.tvChampion.setText(s);
                }
            });
        }

        //객체들을 만드는 작업!
        void makeAll(){

            //적군 만들기..
            int p= rnd.nextInt(11-level);
            if(p==0){
                enemies.add( new Enemy(width, height, imgEnemy, me.x, me.y, imgGauge) );
            }


            //미사일 만들기...
            missileTime--;
            if(missileTime<=0){
                //미사일 효과음
                if(G.isSound) sp.play(sd_fire,0.2f,0.2f,0,0,1);
                missiles.add( new Missile(width, height, imgMissile, me.x, me.y, me.angle, me.kind) );
                missileTime= missileGap;
            }

        }

        //객체들이 움직이는 작업!
        void moveAll(){

            //적군들 움직이기..
            for(int i= enemies.size()-1; i>=0; i--){
                Enemy t= enemies.get(i);

                t.move(me.x, me.y);
                if(t.isOut) enemies.remove(i);
                else if(t.isDead){ //맞아 죽었다!!
                    //폭발효과 사운드..
                    if(G.isSound) sp.play(sd_mondie,1,1,1,0,1);

                    //점수획득
                    score+=(t.kind+1)*10;
                    setTextViewValue();

                    dusts.add( new Dust(imgDust, t.x, t.y) );//폭발효과 이미지

                    int p= rnd.nextInt(2);
                    if(p==0) items.add( new Item(width, height, imgItem, t.x, t.y) );//아이템생성

                    enemies.remove(i);
                }
            }

            //아이템들 움직이기..
            for(int i= items.size()-1; i>=0; i--){
                Item t= items.get(i);

                if(magnetTime>0 && t.kind<2) t.move(me.x, me.y);
                else t.move();
                if(t.isDead) items.remove(i);
            }


            //먼지들 움직이기..
            for(int i= dusts.size()-1; i>=0; i--){
                Dust t= dusts.get(i);

                t.move();
                if(t.isDead) dusts.remove(i);
            }

            //미사일들 움직이기..
            for(int i= missiles.size()-1; i>=0; i--){
                Missile t= missiles.get(i);

                t.move();
                if(t.isDead) missiles.remove(i);
            }

            //플레이어 움직이기
            me.move();

            //배경움직이기
            backPos--;
            if(backPos<=-width) backPos+=width;

            //아이템 지속시간 체크메소드호출..
            checkItemEffect();
        }

        //아이템의 지속시간 체크..
        void checkItemEffect(){

            if(fastTime>0){
                fastTime--;
                if(fastTime==0){
                    me.da=2;
                    missileGap=3;
                }
            }

            if(protectTime>0) protectTime--;
            if(magnetTime>0) magnetTime--;
            if(strongTime>0) strongTime--;

        }

        //취득한 아이템의 종류에 따른 액션 작업 메소드
        void actionItem(int kind){
            switch (kind){
                case Item.COIN:
                    if(G.isSound) sp.play(sd_coin,1,1,2,0,1);
                    coin++;
                    setTextViewValue();
                    break;

                case Item.GEM:
                    if(G.isSound) sp.play(sd_gem,1,1,3,0,1);
                    G.gem++;
                    setTextViewValue();
                    break;

                case Item.FAST:
                    if(G.isSound) sp.play(sd_item,1,1,3,0,1);
                    fastTime= FPS*6;//6초정도
                    me.da=8;
                    missileGap=1;
                    break;

                case Item.PROTECT:
                    if(G.isSound) sp.play(sd_prot,0.7f,0.7f,4,0,1);
                    protectTime= FPS*6;
                    break;

                case Item.MAGNET:
                    if(G.isSound) sp.play(sd_item,1,1,3,0,1);
                    magnetTime= FPS*6;
                    break;

                case Item.BOMB:
                    if(G.isSound) sp.play(sd_item,1,1,3,0,1);
                    bomb++;
                    setTextViewValue();
                    break;

                case Item.STRONG:
                    if(G.isSound) sp.play(sd_item,1,1,3,0,1);
                    strongTime= FPS*6;
                    break;
            }
        }

        //각종 충돌체크 작업!
        void checkCollision(){

            //적군과 플레이어의 충돌...
            for(Enemy t: enemies){
                if(protectTime>0){ //보호막 상황
                    if( Math.pow(t.x-me.x, 2)+Math.pow(t.y-me.y, 2)
                            <= Math.pow(t.w+protRad, 2)){
                        t.isDead=true;
                    }
                }else{//보호막이 없는 상황..
                    if( Math.pow(t.x-me.x, 2)+Math.pow(t.y-me.y, 2)
                            <= Math.pow(t.w+me.w, 2)){
                        //적군과 충돌!!
                        t.isDead=true;
                        me.hp--;
                        //진동주기
                        if(G.isVibrate) vibrator.vibrate(500);

                        if(me.hp<=0){
                            //GAME OVER!!!!!!!
                            //플레이어 폭발 효과음
                            if(G.isSound) sp.play(sd_chdie,1,1,6,0,1);


                            //GameoverActivity를 실행해 달라고 본사에 요청
                            Message msg=new Message();
                            Bundle data=new Bundle();

                            data.putInt("Score",score);
                            data.putInt("Coin",coin);

                            msg.setData(data);
                            ((GameActivity)context).handler.sendMessage(msg);
                        }
                        break;
                    }
                }
            }

            //아이템과 플레이어의 충돌..
            for(Item t : items){
                if( Math.pow(t.x-me.x, 2)+Math.pow(t.y-me.y, 2)
                        <= Math.pow(t.w+me.w, 2) ){
                    actionItem(t.kind);//취득한 아이템에 따른 액션!!
                    t.isDead=true; //아이템 없어지기..
                    break;
                }
            }

            //미사일과 적군의 충돌체크..
            for(Missile t : missiles){
                for(Enemy et : enemies){
                    if(Math.pow(t.x-et.x, 2)+Math.pow(t.y-et.y, 2)<=Math.pow(t.w+et.w, 2) ){
                        //맞았다!!!
                        score+=5;
                        setTextViewValue();

                        et.damaged(t.kind+1);
                        if(strongTime==0) t.isDead=true;//미사일 죽었다..
                    }
                }
            }

        }

        //화면에 그리는 작업!
        void drawAll(Canvas canvas){
            //배경그리기...
            canvas.drawBitmap(imgBack, backPos, 0, null);
            canvas.drawBitmap(imgBack, backPos+width, 0, null);

            //적군들 그리기..
            for(Enemy t: enemies){
                canvas.save();
                canvas.rotate(t.angle, t.x, t.y);
                canvas.drawBitmap(t.img, t.x-t.w, t.y-t.h, null);
                canvas.restore();

                //Gauge그리기
                if(t.kind>0) canvas.drawBitmap(t.imgG, t.x-t.w, t.y+t.h, null);
            }

            //미사일들 그리기..
            for(Missile t : missiles){
                canvas.save();
                canvas.rotate(t.angle, t.x, t.y);
                canvas.drawBitmap(strongTime>0?imgStrong:t.img, t.x-t.w, t.y- t.h, null);
                canvas.restore();
            }

            //아이템들 그리기
            for(Item t : items){
                canvas.drawBitmap(t.img, t.x-t.w, t.y-t.h, null);
            }


            //플레이어 그리기.
            canvas.save();
            canvas.rotate(me.angle, me.x, me.y);
            canvas.drawBitmap(me.img, me.x-me.w, me.y-me.h, null);
            canvas.restore();

            //보호막 그리기..
            if(protectTime>0){
                protAngle+=30;
                canvas.save();
                canvas.rotate(protAngle, me.x, me.y);
                canvas.drawBitmap(imgProtect, me.x-protRad, me.y-protRad, null);
                canvas.restore();
            }

            //먼지들 그리기..
            for(Dust t : dusts){
                for(int i=0; i<t.imgs.length; i++){
                    canvas.drawBitmap(t.imgs[i], t.x[i]-t.rad[i], t.y[i]-t.rad[i], null);
                }
            }

            //조이패드 그리기...
            alphaPaint.setAlpha(isJoypad?240:120);
            canvas.drawBitmap(imgJoypad, jpx-jpr, jpy-jpr, alphaPaint);

            //폭탄버튼 그리기..
            alphaPaint.setAlpha(isBomb?240:120);
            canvas.drawBitmap(imgBombBtn, recBomb.left, recBomb.top, alphaPaint);
        }

        //터치이벤트 처리에 관련한 메소드//////
        void touchDown(int x, int y){
            //조이패드 영역안에 있는가?
            if(Math.pow(x-jpx, 2)+Math.pow(y-jpy, 2) <= Math.pow(jpr, 2)){
                me.radian= Math.atan2(jpy-y, x-jpx);//플레이어가 이동할 각도 계산..
                isJoypad=true;
                me.canMove=true; //플레이어는 움직일수 있는 상태로...
            }

            //폭탄버튼 영역인가??
            if(recBomb.contains(x, y)){
                isBomb=true;

                //폭탄개수가 1개이상인가?
                if(bomb>0){
                    //폭탄 효과음
                    if(G.isSound) sp.play(sd_bomb,1,1,5,0,1);
                    bomb--;
                    setTextViewValue();

                    //적군들 모두 제거.
                    for(Enemy t: enemies){
                        if(t.wasShow) t.isDead=true;
                    }
                }
            }
        }

        void touchUp(int x, int y){
            me.canMove=false;
            isJoypad=false;

            isBomb=false;
        }

        void touchMove(int x, int y){
            //조이패드가 눌러졌던 상황인가?
            if(isJoypad){
                me.radian= Math.atan2(jpy-y, x-jpx);//플레이어가 이동할 각도 계산..
            }

        }
        /////////////////////////////////////

        //FPS(Frame per Second)를 조절하는 작업
        void adjustFPS(){
            currTime= System.currentTimeMillis();
            loopTime= currTime- lastTime; //경과시간
            lastTime= currTime;

            sleepTime= frameTime-loopTime;

            //fast
            if(sleepTime>0){
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //slow
            skippedFrame=0;
            while(sleepTime<0 && skippedFrame<5){
                makeAll();
                moveAll();
                checkCollision();

                sleepTime += frameTime;
                skippedFrame++;
            }

        }

        @Override
        public void run() {
            createBitmaps();//이미지 Bitmap객체 생성

            init();//게임의 각종 초기값 설정정

            lastTime= System.currentTimeMillis();
            Canvas canvas= null;
            while (isRun){
                canvas= holder.lockCanvas();

                try {
                    synchronized (holder){
                        makeAll(); //객체들을 만드는 작업!
                        moveAll();//객체들이 움직이는 작업!
                        checkCollision();//각종 충돌체크 작업!

                        adjustFPS();//FPS조절..

                        drawAll( canvas );//화면에 그리는 작업!
                    }
                }finally {
                    if(canvas!=null) holder.unlockCanvasAndPost(canvas);
                }

                //
                synchronized (this){
                    if(isWait){
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }//while...

            removeResources();
        }//run method...

        void stopThread(){
            isRun= false;

            //혹시 잠자고 있는 놈이 있다면 깨워!!
            synchronized (this){
                this.notify();
            }
        }

        void pauseThread(){
            isWait=true;
        }

        void resumeThread(){
            isWait=false;

            synchronized (this){
                this.notify();
            }
        }

    }//GameThread class....
    //////////////////////////////////////////////////////////

}//GameView class......


