package model;

import java.awt.Rectangle;

public class Bullet {
    private int width = 20;
    private int height = 20;
    
    private float posX;
    private float posY;
    
    private int maxX;
    private int maxY;

    private float deltaX;
    private float deltaY;
    
    private int bulletSpeed = 5;
    
    private boolean isOutOfScreen = false;
    
    private int damage;
    //how to implement speed?
    
    public Bullet(int startX, int startY, int clickX,int clickY, int bulletDamage , int maxY, int maxX){
        this.posX = startX;
        this.posY = startY;
        this.maxY = maxY;
        this.maxX = maxX;
        this.damage = bulletDamage;
        
        calculateDirection(clickX,clickY);
    }
    
    private void calculateDirection(int clickX, int clickY){
       deltaX = clickX - posX;
       deltaY = clickY - posY;
       
       int absDeltaX = (int) Math.abs(deltaX);
       int absDeltaY = (int) Math.abs(deltaY);
       //TODO make sure that the speed for every bullet is the same
       if(absDeltaX>absDeltaY){
           deltaY = (float) deltaY/absDeltaX;
           deltaX = deltaX/absDeltaX;
       }else{
           deltaX = (float) deltaX/absDeltaY;
           deltaY = deltaY/absDeltaY;
       }
       
    }
    
    public void updatePos(){
        posX += deltaX * bulletSpeed;
        posY += deltaY * bulletSpeed;
        
        if(posX>maxX || posX<0 || posY>maxY || posY<0){
            isOutOfScreen = true;
        }
    }
    

    
    /*GETTERS AND SETTERS*/
    public int getPosX() {
        return Math.round(posX);
    }

    public int getPosY() {
        return Math.round(posY);
    }
    
    public boolean getIsOutOfScreen(){
        return isOutOfScreen;
    }
    
    public Rectangle getBounds(){
        return new Rectangle(getPosX()-(width/2),getPosY()-(height/2),width,height);
    }
    
    public int getDamage(){
        return damage;
    }
    
    public void setDamage(int damage){
        this.damage = damage;
    }
    
}
