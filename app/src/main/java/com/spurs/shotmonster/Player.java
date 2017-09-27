package com.spurs.shotmonster;

import android.graphics.Bitmap;

/**
 * Created by alfo06-11 on 2017-06-27.
 */

public class Player {

    int width,height;

    Bitmap img;
    int x,y;
    int w,h;

    boolean canMove=false; //움직일 수 있는가?
    double radian; //이동각도
    int speed;

    int angle=0; //회전각도
    int da=2; //회전각도 변화량

    int kind; //플레이어의 종류

    Bitmap[][] imgs;
    int index=0; //날개짓 이미지 번호

    int loop=0;
    int hp=3; //생명

    public Player(int width,int height,Bitmap[][] imgPlayer,int kind) {

        this.width=width;
        this.height=height;

        this.kind=kind;
        imgs=imgPlayer;

        img=imgs[kind][index];

        w=img.getWidth()/2;
        h=img.getHeight()/2;

        //플레이어의 처음 시작 위치..
        x=width/2;
        y=height/2;

        //이동속도
        speed=w/6;
    }

    void move(){

        //날개짓
        loop++;
        if(loop%3==0) {
            index++;
            if (index > 3) index = 0;
            img = imgs[kind][index];
        }

        //회전
        angle+=da;

        //조이패드에 따라 움직이기
        if(canMove){
            x=(int)(x+Math.cos(radian)*speed);
            y=(int)(y-Math.sin(radian)*speed);

            if(x<w) x=w;
            if(x>width-w) x=width-w;
            if(y<h) y=h;
            if(y>height-h) y=height-h;
        }


    }

}
