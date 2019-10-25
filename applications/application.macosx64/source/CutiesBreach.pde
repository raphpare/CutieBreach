import java.util.Iterator;
import processing.sound.*;
import processing.video.*;

// Couleurs
final color BLANC = #FFFFFF;
final color NOIR = #0E1A1E;
final color GRIS_FONCE = #182E33;
final color ROSE = #FF7BAC;

// Config
final int ZONE_DEGAGEMENT = 24;
final int DIMENSION_TEXTE_BOUTON= 22;

// Sons
SoundFile musiqueInstruction;
SoundFile musiquePartieDemarre;
SoundFile musiquePartieTerminee;
SoundFile sonBoutonClic;
SoundFile sonProjectile;
SoundFile sonAvatarTouche;
SoundFile sonAvatarExplosion;
SoundFile sonObstacTouche;

// Fonts
PFont fontSemiBold;
PFont fontBlack;
PFont fontCoeur;

// Images
PImage imageSoleil;
PImage imageRayonSoleil;
PImage masqueRayonSoleil;
PImage imageLogo;
PImage imageInstruction;
PImage imageObstacles;
PImage imageAvatar;
PImage[] imagesExplosion;
PImage imageGameOver;
PImage imageFondBouton;

// Video
Movie videoPartieTerminee;
PImage masquePartieTerminee;

boolean afficherEcranChargement = true;
boolean jeuEnChargement = true;

// Événement
boolean isKeyPressedUp;
boolean isKeyPressedDown;
boolean isKeyPressedLeft;
boolean isKeyPressedRight;
boolean isKeyPressedSpace;
boolean isKeyPressedX;
boolean isMousePressed;

// Temps
float tempsEcouleEnMillisecondeInitiale = 0;
float tempsEcouleEnSeconde;
float tempsEcouleEnMilliseconde;
float tempsEcouleEntreDeuxFrames;

Jeu jeu;

void setup() {
  size(450, 600);
  frameRate(60);
  pixelDensity(displayDensity());
  background(NOIR);
}

void draw() {  
  if (afficherEcranChargement) {
    dessinerEcranChargement();
    afficherEcranChargement = false;
  } else if (jeuEnChargement) {
   
    fontSemiBold = createFont("fonts/Nunito-SemiBold.ttf", 32);
    fontBlack = createFont("fonts/Nunito-Black.ttf", 32);
    fontCoeur = createFont("fonts/NotoSansJP-Bold.otf", 32);
    
    musiqueInstruction = new SoundFile(this, "sons/musiqueInstruction.mp3");
    musiquePartieDemarre = new SoundFile(this, "sons/musiquePartieDemarre.mp3");
    musiquePartieTerminee = new SoundFile(this, "sons/musiquePartieTerminee.mp3");
    sonBoutonClic = new SoundFile(this, "sons/boutonClic.wav");
    sonProjectile = new SoundFile(this, "sons/projectile.mp3");
    sonAvatarTouche = new SoundFile(this, "sons/avatarTouche.mp3");
    sonAvatarExplosion = new SoundFile(this, "sons/avatarExplosion.mp3");
    sonObstacTouche = new SoundFile(this, "sons/obstacleTouche.mp3");
    
    imageSoleil = loadImage("images/soleil.png");
    imageRayonSoleil = loadImage("images/rayonSoleil.png");
    masqueRayonSoleil = loadImage("images/masqueRayonSoleil.png");
    imageLogo = loadImage("images/logo.png");
    imageInstruction = loadImage("images/instruction.png");
    imageObstacles = loadImage("images/obstacles.png");
    imageAvatar = loadImage("images/avatar.png");
    imagesExplosion = new PImage[20];
    for (int index = 0; index < imagesExplosion.length; index++) {
      imagesExplosion[index] = loadImage("images/explosion/explosion_" + index + ".png");
    }
    imageGameOver = loadImage("images/gameOver.png");
    imageFondBouton = loadImage("images/fondBouton.png");
    
    videoPartieTerminee = new Movie(this, "videos/videoPartieTerminee.mp4");
    masquePartieTerminee = loadImage("videos/masquePartieTerminee.png");
    
    jeu = new Jeu();
    jeuEnChargement = false;
  } else {
    jeu.dessiner();
  }
}

void keyPressed() {
  setKeyPressedEvent(true);
  jeu.definirActionJeuOnKeyPressed();
  
  if (keyCode == ' ') {
    if (!isKeyPressedSpace) {
      jeu.avatarLancerProjectile();
      jeu.definirEtatJeuOnEvent(); 
    }
    isKeyPressedSpace = true;
  }
  
  
  if (keyCode == 'x' || keyCode == 'X') {
    if (!isKeyPressedX) {
      jeu.basculerTypeProjectiles();
    }
    isKeyPressedX = true;
  }
}

void keyReleased() {
  setKeyPressedEvent(false);
  
  if (keyCode == ' ') {
    isKeyPressedSpace = false;
  }
  
  if (keyCode == 'x' || keyCode == 'X') {
    isKeyPressedX = false;
  }
}

void mousePressed() {
  this.isMousePressed = true;
  jeu.definirEtatJeuOnEvent();  
}

void mouseReleased() {
  this.isMousePressed = false;
}

void dessinerEcranChargement() {
  textSize(16);
  textAlign(CENTER);
  fill(BLANC);
  text("Chargement...", width/2, height/2);
}

void setKeyPressedEvent(boolean actif) {
  if (keyCode == UP || key == 'w' || key == 'W') {
    isKeyPressedUp = jeu.etat == JeuEtat.DEMARRE && actif;
  }
  
  if (keyCode == DOWN || key == 's' || key == 'S') {
    isKeyPressedDown = jeu.etat == JeuEtat.DEMARRE && actif;
  }
  
  if (keyCode == LEFT || key == 'a' || key == 'A') {
    isKeyPressedLeft = jeu.etat == JeuEtat.DEMARRE && actif;
  }
  
  if (keyCode == RIGHT || key == 'd' || key == 'D') {
    isKeyPressedRight = jeu.etat == JeuEtat.DEMARRE && actif;
  }
}

void movieEvent(Movie video) {
  video.read();
}

void dessinerTexteBordure(String texte, float positionX, float positionY, float largeurBordure, color couleurTexte, color couleurBordure, float opacite) {
    fill(couleurBordure, opacite);
    for (int x = -1; x < largeurBordure; x++) {
      text(texte, positionX + x, positionY);
      text(texte, positionX, positionY + x);
    }
    fill(couleurTexte, opacite);
    text(texte, positionX, positionY);
}

final int genererNombreAleatoireMinMax(int min, int max) {
  return min + int(random(max - min + 1));
}

final boolean isCercleRectangleEnCollision(float circleX,
                                           float circleY,
                                           float radius,
                                           float rectangleX,
                                           float rectangleY,
                                           float rectangleWidth,
                                           float rectangleHeight) {
                                             
  float circleDistanceX = abs(circleX - rectangleX - rectangleWidth/2);
  float circleDistanceY = abs(circleY - rectangleY - rectangleHeight/2);
  
  if (circleDistanceX > (rectangleWidth/2 + radius)) { return false; }
  if (circleDistanceY > (rectangleHeight/2 + radius)) { return false; }
   
  if (circleDistanceX <= (rectangleWidth/2)) { return true; }
  if (circleDistanceY <= (rectangleHeight/2)) { return true; }
   
  float cornerDistanceSq = pow(circleDistanceX - rectangleWidth/2, 2)
                           + pow(circleDistanceY - rectangleHeight/2, 2);
   
  return (cornerDistanceSq <= pow(radius,2));
}
  
final boolean isCerclesEnCollision( float circle1X,
                                    float circle1Y,
                                    float circle1radius,
                                    float circle2X,
                                    float circle2Y,
                                    float circle2radius) {
  return (dist(circle1X, circle1Y, circle2X, circle2Y) < circle1radius + circle2radius);
}

final float oscillation(float temps, float amplitude, float periode) {
  return amplitude * sin(temps * 2 * PI / periode);
}
