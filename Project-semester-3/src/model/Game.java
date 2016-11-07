package model;

import gui.GameGui;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

public class Game {
    
    int fps = 60;
    int gameHeight= 1080;
    int gameWidth = 1920;
    
    Character character;
    InputHandler handler;
    
    List<Bullet> bullets = new ArrayList();
    List<Enemy> enemys = new ArrayList();
    
    long lastBulletFired = 0;
    
    GameGui gameGui;
    
    
    public static void main(String[] args) {
        new Game();
    }
    
    public Game(){
        run();
        System.exit(-1);
    }
    
    void run(){
        init();
        
        while(character.getLives()>0){
            long time = System.currentTimeMillis();
            
            update();
            draw();
            
            time = (1000 / fps) - (System.currentTimeMillis() - time);
            if(time > 0){
                try {
                    Thread.sleep(time);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    void init(){
        enemys.add(new Enemy());
        
        gameGui = new GameGui(gameWidth,gameHeight);
        
        character = new Character(gameWidth,gameHeight);
       
        handler = new InputHandler(gameGui.getFrame());   
    }
    
    void update(){
        updateCharacterPos();
        updateBullets();
        updateEnemys();
        collisionDetection();
    }
    
    void draw(){
        gameGui.draw(character, bullets, enemys);
    }

    private void updateCharacterPos() {
        if(handler.isKeyDown(KeyEvent.VK_RIGHT)){
            character.moveRight();
        }
        if(handler.isKeyDown(KeyEvent.VK_LEFT)){
            character.moveLeft();
        }
        if(handler.isKeyDown(KeyEvent.VK_UP)){
            character.moveUp();
        }
        if(handler.isKeyDown(KeyEvent.VK_DOWN)){
            character.moveDown();
        }
    }
    
    private void updateBullets(){
        List<Bullet> needToRemove = new ArrayList();
        
        addBullets();
        
        for(Bullet bullet: bullets){
            bullet.updatePos();
            
            if(bullet.getIsOutOfScreen()){
                needToRemove.add(bullet);
            }
        }
        
        for(Bullet bullet: needToRemove){
            bullets.remove(bullet);
        }
    }
    
    private void addBullets(){
        if(handler.isMouseDown(1)){            
            if(lastBulletFired + (60.0/character.getBulletsPerMinute()*1000)<System.currentTimeMillis()){
                //Moeten we echt height en witdh meegeven of kunnen we er anders aan?
                Bullet newBullet = new Bullet(character.getPosX(),character.getPosY(),handler.getEvent(1).getX(),handler.getEvent(1).getY(),gameHeight,gameWidth);
                bullets.add(newBullet);
                lastBulletFired = System.currentTimeMillis();
                enemys.add(new Enemy());
            }
        }
    }
    
    private void updateEnemys(){
        for(Enemy enemy: enemys){
            enemy.updatePos(character.getPosX(),character.getPosY());
        }
    }
    
    private void collisionDetection(){
        /*bullets vs enemysdetection*/
        List<Bullet> bulletsToRemove = new LinkedList();
        List<Enemy> enemysToRemove = new LinkedList();
        
        for(Bullet bullet: bullets){
            for(Enemy enemy: enemys){
                if(bullet.getBounds().intersects(enemy.getBounds())){
                    bulletsToRemove.add(bullet);
                    enemysToRemove.add(enemy);
                }
            }
        }
        
        for(Bullet bullet: bulletsToRemove){
            bullets.remove(bullet);
        }
        for(Enemy enemy: enemysToRemove){
                enemys.remove(enemy);
        }
        
        /*enemys vs character detection*/
        Enemy hittedChar = null;
        for(Enemy enemy: enemys){
            if(character.getBounds().intersects(enemy.getBounds())){
                character.lifeLess();
                hittedChar = enemy;
            }
        }
        if(hittedChar!= null){
            enemys.remove(hittedChar);
        }
    }
    
}
