package model;

import data.GameDA;
import gui.GameGui;
import java.util.List;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import model.companions.AutoShooter;
import model.companions.Companion;
import model.companions.LifeSaver;
import model.companions.Miner;
import model.companions.Shooter;

/*import multiplayer.Server;*/

public class Game implements Serializable {

    private Random randomGenerator = new Random();
    private int fps = 60;
    private int gameHeight;
    private int gameWidth;
    private int spawnEnemyX;
    private int spawnEnemyY;
    private int waveCounter;
    private long spawnTimer;
    private Character character;
    private InputHandler handler;
    private List<Bullet> bullets = new ArrayList();
    private List<Enemy> enemies = new ArrayList();
    private List<Geom> geoms = new ArrayList();
    private List<Wave> waves = new ArrayList<>();
    private List<Mine> mines = new ArrayList<>();
    
    private GameGui gameGui;
    private List<Player> players;

    /*private Server server;*/
    private Companion companion;
    private GameDA db = GameDA.getInstance();
    private int characterid = 2;

    public static void main(String[] args) {
        new Game();
    }

    public Game() {
        //TODO characterid , companionid en playerid uit GUI
        run();
        System.exit(-1);
    }

    private void run() {
        init();
        makeWaves();

        long spawnTimer = System.currentTimeMillis();
        waveCounter = 0;
        while (character.getLives() > 0) {
            long time = System.currentTimeMillis();

            update();

            draw();

            /*try {
                sendDataToClient();
            } catch (IOException ex) {
                System.out.println("failed to send data");
            }*/
            time = (1000 / fps) - (System.currentTimeMillis() - time);
            if (time > 0) {
                try {
                    Thread.sleep(time);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

            }
        }
    }

    private void init() {
        /*try {
            server = new Server();
        } catch (IOException ex) {
            System.out.println("failed to make server");
        }*/

        gameGui = new GameGui();
        gameWidth = gameGui.getGameWidth();
        gameHeight = gameGui.getGameHeight();
        
        character =  db.getCharacter(characterid,gameWidth,gameHeight);
        

        handler = new InputHandler(gameGui.getFrame());

        //companion = new Miner(character, mines, handler, 30, 30, 10, 20);
        companion = new LifeSaver(character, 30, 30, 60);
        //companion = new Shooter(character, handler, bullets, 30, 30, 30, 60,5);
        //companion = new AutoShooter(character,bullets,enemies,30,30,30,60,5);
        
        players = db.getPlayers();
        
    }

    private void update() {
        
        spawnEnemy();
        updatePlayerPos();
        updateBullets();
        updateEnemies();
        companion.doMove();
        companion.doSpecialAction();
        collisionDetection();
    }

    private void draw() {
        gameGui.draw(character, bullets, enemies, geoms, companion, mines);
    }

    private void randomSpawnGenerator() {
        int randomInt = randomGenerator.nextInt(4);
        switch (randomInt) {
            case 0:
                spawnEnemyX = 0;
                spawnEnemyY = 40 + randomGenerator.nextInt(gameHeight - 40);
                break;
            case 1:
                spawnEnemyY = 40;
                spawnEnemyX = randomGenerator.nextInt(gameWidth);
                break;
            case 2:
                spawnEnemyX = gameWidth;
                spawnEnemyY = 40 + randomGenerator.nextInt(gameHeight - 40);
                break;
            case 3:
                spawnEnemyY = gameHeight;
                spawnEnemyX = randomGenerator.nextInt(gameWidth);
                break;

        }
    }

    private void makeWaves() {
        //TODO alle waves uit de databank halen 
        /*Wave wave1 = new Wave(2,1,enemies,5);
        Wave wave2 = new Wave(3,1,enemies,5);
        Wave wave3 = new Wave(4,1,enemies,5);
        Wave wave4 = new Wave(5,1,enemies,5);
        
        waves.add(wave1);
        waves.add(wave2);
        waves.add(wave3);
        waves.add(wave4);*/
        
        /*for (int i = 0; i < 200; i++) {
            Wave wave = new Wave(i, 1, enemies, 5);
            waves.add(wave);
        }*/
        
        waves = db.getWaves();
        
    }

    private void spawnEnemy() {
        randomSpawnGenerator();
        Wave wave = waves.get(waveCounter);
        
        int enemyid = wave.getEnemyID();
        if (System.currentTimeMillis() - spawnTimer > wave.getSpawnRateInMs() && wave.getNumberOfEnemiesLeft() != 0) {
            
            Enemy e = db.getEnemy(enemyid, spawnEnemyX, spawnEnemyY);
            enemies.add(e);
            spawnTimer = System.currentTimeMillis();
            wave.reduceNumberOfEnemiesLeft();

        }
        if (wave.getNumberOfEnemiesLeft() == 0 && waveCounter < waves.size() - 1) {
            int delay = wave.getDelay();
            // TODO use this delay (maybe an upgrade screen after 2 seconds ?

            waveCounter++;

        }
    }

    private void updatePlayerPos() {
        if (handler.isKeyDown(KeyEvent.VK_RIGHT)) {
            character.moveRight();

        }
        if (handler.isKeyDown(KeyEvent.VK_LEFT)) {
            character.moveLeft();

        }
        if (handler.isKeyDown(KeyEvent.VK_UP)) {
            character.moveUp();

        }
        if (handler.isKeyDown(KeyEvent.VK_DOWN)) {
            character.moveDown();

        }
    }

    private void updateBullets() {
        List<Bullet> needToRemove = new ArrayList();

        addBullets();

        for (Bullet bullet : bullets) {
            bullet.updatePos();

            if (bullet.getIsOutOfScreen()) {
                needToRemove.add(bullet);
            }
        }

        for (Bullet bullet : needToRemove) {
            bullets.remove(bullet);
        }
    }

    private void addBullets() {

        if (handler.isMouseDown(1)) {
            if (character.getLastBulletFired() + (60.0 / character.getBulletsPerMinute() * 1000) < System.currentTimeMillis()) {
                Bullet newBullet = new Bullet(character.getPosX(), character.getPosY(), handler.getEvent(1).getX(), handler.getEvent(1).getY(), character.getDamage(), gameHeight, gameWidth,character.getBulletSpeed());
                bullets.add(newBullet);
                character.setLastBulletFired(System.currentTimeMillis());
            }
        }
    }

    private void updateEnemies() {
        boolean mayMove = true;

        for (Enemy currentEnemy : enemies) {
            mayMove = true;
            for (Enemy otherEnemy : enemies) {
                if (currentEnemy.getBounds().intersects(otherEnemy.getBounds())) {
                    double distanceCurrentEnemy = Math.sqrt((currentEnemy.getPosX() - character.getPosX()) ^ 2 + (currentEnemy.getPosY() - character.getPosY()));
                    double distanceOtherEnemy = Math.sqrt((otherEnemy.getPosX() - character.getPosX()) ^ 2 + (otherEnemy.getPosY() - character.getPosY()));

                    if (distanceCurrentEnemy > distanceOtherEnemy) {
                        mayMove = false;
                    }
                }
            }
            if (mayMove) {
                currentEnemy.updatePos(character.getPosX(), character.getPosY());
            }
        }

//        for(Enemy enemy: enemies){
//            enemy.updatePos(player.getPosX(),player.getPosY());
//        }
    }

    private void collisionDetection() {
        /*bullets vs enemiesdetection*/
        List<Bullet> bulletsToRemove = new LinkedList();
        List<Enemy> enemiesToRemove = new LinkedList();
        List<Mine> minesToRemove = new LinkedList();

        for (Bullet bullet : bullets) {
            for (Enemy enemy : enemies) {
                if (bullet.getBounds().intersects(enemy.getBounds())) {

                    checkdamage(bullet, enemy);

                    if (enemy.getHp() == 0) {
                        if (drop(enemy.getDropRateGeom())) {
                            Geom geom = new Geom(enemy.getPosX(), enemy.getPosY());
                            geoms.add(geom);
                        }
                        /*if (drop(enemy.getDropRateSpeedBoost())) {
                            System.out.println("speed gaat omhoog !!");
                            character.setMovementSpeedHigher();
                        }
                        if (drop(enemy.getDropRateBPM())) {
                            System.out.println("rate of fire gaat omhoog !!");
                            character.setBPM();
                        }
                        if (drop(enemy.getDropRateExtraEnemy())) {
                            Wave wave = waves.get(waveCounter);
                            // TODO get the amount of extra enemies
                            wave.addExtraEnemies(2);
                        }
                        if (drop(enemy.getDroprateSpeedDown())) {
                            character.setMovementSpeedLower();
                        }*/
                        enemiesToRemove.add(enemy);
                        character.addPoints(enemy.getValue());
                    }
                    if (bullet.getDamage() == 0) {
                        bulletsToRemove.add(bullet);

                    }

                }
            }
        }

        for (Bullet bullet : bulletsToRemove) {
            bullets.remove(bullet);
        }

        /*enemies vs player detection*/
        Enemy hittedChar = null;
        for (Enemy enemy : enemies) {
            if (character.getBounds().intersects(enemy.getBounds())) {
                character.removeLife();
                hittedChar = enemy;
            }
        }
        if (hittedChar != null) {
            enemies.remove(hittedChar);
        }

        /* player vs geoms */
        List<Geom> hittedGeoms = new ArrayList();
        for (Geom geom : geoms) {
            if (character.getBounds().intersects(geom.getBounds())) {
                character.addGeom();
                hittedGeoms.add(geom);
            }
        }
        for (Geom geom : hittedGeoms) {
            geoms.remove(geom);
        }
        /* enemies vs mine */

        for (Enemy enemy : enemies) {
            for (Mine mine : mines) {
                if (enemy.getBounds().intersects(mine.getBounds())) {
                    int newhp = enemy.getHp() - mine.getDamage();
                    if (newhp <= 0) {
                        enemiesToRemove.add(enemy);

                    } else {
                        enemy.setHp(newhp);
                    }
                    minesToRemove.add(mine);

                }
            }
        }
        for (Enemy enemy : enemiesToRemove) {
            enemies.remove(enemy);
        }

        /* player vs mine */
        for (Mine mine : mines) {
            if (mine.getBounds().intersects(character.getBounds())) {
                character.removeLife();
                minesToRemove.add(mine);

            }
        }
        for (Mine mine : minesToRemove) {
            mines.remove(mine);
        }
    }

    private void checkdamage(Bullet bullet, Enemy enemy) {
        int bulletdamage = bullet.getDamage();
        int enemyhp = enemy.getHp();

        if (bulletdamage > enemyhp) {
            bullet.setDamage(bulletdamage - enemyhp);
            enemy.setHp(0);

        } else if (bulletdamage < enemyhp) {
            enemy.setHp(enemyhp - bulletdamage);
            bullet.setDamage(0);
        } else {
            enemy.setHp(0);
            bullet.setDamage(0);
        }
    }

    private boolean drop(double droprate) {
        int maxint = (int) (droprate * 100);

        int randomInt = randomGenerator.nextInt(100);

        if (randomInt < maxint) {
            return true;
        } else {
            return false;
        }

    }

    /*private void sendDataToClient() throws IOException{
        server.sendDataToClient(player, bullets, enemies, geoms);
    }*/
}
