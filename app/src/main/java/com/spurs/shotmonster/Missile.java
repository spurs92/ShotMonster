package com.spurs.shotmonster;

import android.graphics.Bitmap;

/**
 * Created by alfo06-11 on 2017-06-27.
 */

public class Missile {

    int width,height;

    Bitmap img;
    int x,y;
    int w,h;
    boolean isDead=false;

    double radian; //이동각도
    int speed; //이동속도

    int angle; //회전각도

    int kind; //미사일 종류

    public Missile(int width, int height,Bitmap[] imgMissile, int chx, int chy, int chAng, int chKind) {

        this.width=width;
        this.height=height;

        x=chx;
        y=chy;

        kind=chKind;

        img=imgMissile[kind];
        w=img.getWidth()/2;
        h=img.getHeight()/2;

        speed=w/6;

        angle=chAng;
        radian=Math.toRadians(270-angle);
    }

    void move(){
        x=(int)(x+Math.cos(radian)*speed);
        y=(int)(y-Math.sin(radian)*speed);

        //화면밖으로나가면 제거
        if(x<-w || x>width+w || y<-h || y>height+h) isDead=true;
    }
}
