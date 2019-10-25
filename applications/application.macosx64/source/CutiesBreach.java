import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.Iterator; 
import processing.sound.*; 
import processing.video.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class CutiesBreach extends PApplet {





// Couleurs
final int BLANC = 0xffFFFFFF;
final int NOIR = 0xff0E1A1E;
final int GRIS_FONCE = 0xff182E33;
final int ROSE = 0xffFF7BAC;

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

public void setup() {
  
  frameRate(60);
  
  background(NOIR);
}

public void draw() {  
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

public void keyPressed() {
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

public void keyReleased() {
  setKeyPressedEvent(false);
  
  if (keyCode == ' ') {
    isKeyPressedSpace = false;
  }
  
  if (keyCode == 'x' || keyCode == 'X') {
    isKeyPressedX = false;
  }
}

public void mousePressed() {
  this.isMousePressed = true;
  jeu.definirEtatJeuOnEvent();  
}

public void mouseReleased() {
  this.isMousePressed = false;
}

public void dessinerEcranChargement() {
  textSize(16);
  textAlign(CENTER);
  fill(BLANC);
  text("Chargement...", width/2, height/2);
}

public void setKeyPressedEvent(boolean actif) {
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

public void movieEvent(Movie video) {
  video.read();
}

public void dessinerTexteBordure(String texte, float positionX, float positionY, float largeurBordure, int couleurTexte, int couleurBordure, float opacite) {
    fill(couleurBordure, opacite);
    for (int x = -1; x < largeurBordure; x++) {
      text(texte, positionX + x, positionY);
      text(texte, positionX, positionY + x);
    }
    fill(couleurTexte, opacite);
    text(texte, positionX, positionY);
}

public final int genererNombreAleatoireMinMax(int min, int max) {
  return min + PApplet.parseInt(random(max - min + 1));
}

public final boolean isCercleRectangleEnCollision(float circleX,
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
  
public final boolean isCerclesEnCollision( float circle1X,
                                    float circle1Y,
                                    float circle1radius,
                                    float circle2X,
                                    float circle2Y,
                                    float circle2radius) {
  return (dist(circle1X, circle1Y, circle2X, circle2Y) < circle1radius + circle2radius);
}

public final float oscillation(float temps, float amplitude, float periode) {
  return amplitude * sin(temps * 2 * PI / periode);
}
interface AvatarEtat {
  String
  ACTIF = "actif",
  INACTIF = "inactif",
  TOUCHE = "touche",
  EXPLOSE = "explose",
  MORT = "mort";
}

interface AvatarDirection {
  String
  HAUT = "haut",
  DROITE = "droite",
  BAS = "bas",
  GAUCHE = "gauche";
}

class Avatar {
  final int DEMENSION_PAR_DEFAUT = 160;
  public final int LARGEUR = 80;
  public final int HAUTEUR = 80;
  public final int NB_POINT_VIE_TOTAL = 5;
  private final float VITESSE_DEPLACEMENT = 180.0f;
  public String etat = AvatarEtat.INACTIF;
  public int nbPointVie = NB_POINT_VIE_TOTAL;
  
  private PVector position;
  private PVector positionInitiale;
  private float delaiAvantDessinerEtatActif;
  private float delaiAvantDessinerEtatMort;
  private int conteurImageExplosion = 0;
  private PImage image;
  
  // Projectiles
  public final int NB_PROJECTILES_MAXIMUM = 4;
  public ArrayList<Projectile> projectilesActifs;
  public ArrayList<Projectile> projectilesInactifs;
  public float delaiAvantProchainTir = 200;
  private Projectile projectile;
  private String projectilesType = ProjectileType.LASER;

  Avatar(){
    this.initialiser();
  }
  
  public void initialiser() {
    this.nbPointVie = NB_POINT_VIE_TOTAL;
    this.etat = AvatarEtat.INACTIF;
    this.positionInitiale = new PVector(width / 2.0f, height - (height / 5.0f));
    this.position = new PVector(this.positionInitiale.x, height + HAUTEUR);
    this.initialiserProjectiles();
    this.conteurImageExplosion = 0;
    this.setImage();
  };
  
  public boolean peutEtreTouche() {
    return !(this.etat == AvatarEtat.TOUCHE || this.etat == AvatarEtat.EXPLOSE || this.etat == AvatarEtat.MORT);
  }
  
  public void bouger(String direction, float tempsEcouleEntreDeuxFrames) {
    if (this.etat == AvatarEtat.INACTIF || this.etat == AvatarEtat.EXPLOSE || this.etat == AvatarEtat.MORT) {
      return;
    } 

    float nouvellePositionX = this.position.x;
    float nouvellePositionY = this.position.y;
    float deplacement = VITESSE_DEPLACEMENT * tempsEcouleEntreDeuxFrames;
   
    switch(direction) {
      case AvatarDirection.HAUT:
        nouvellePositionY -= deplacement;
        break;
      case AvatarDirection.DROITE:
        nouvellePositionX += deplacement;
        break;
      case AvatarDirection.BAS:
        nouvellePositionY += deplacement;
        break;
      case AvatarDirection.GAUCHE:
        nouvellePositionX -= deplacement;
        break;
    }
    
    boolean nouvellePositionEstExterieurCaneva = (nouvellePositionX < 0
                                           || nouvellePositionX> width 
                                           || nouvellePositionY < 120
                                           || nouvellePositionY > height);
                                           
     if (nouvellePositionEstExterieurCaneva) {
         return;
     }
     
     this.position = new PVector(nouvellePositionX, nouvellePositionY);
  }
  
  public void basculerTypeProjectiles() {
    this.projectilesType = this.projectilesType == ProjectileType.LASER ? ProjectileType.BALLE : ProjectileType.LASER;
    if (sonBoutonClic.isPlaying()) {
     sonBoutonClic.stop();
    }
    sonBoutonClic.play();
    for (int index = 0; index < this.projectilesInactifs.size(); index++) {
      this.projectilesInactifs.get(index).changerType(this.projectilesType);
    }
  }

  public void dessiner(){
    this.dessinerProjectilesActif();
    
    switch(this.etat) {
      case AvatarEtat.ACTIF:
        this.dessinerActif();
        break;
      case AvatarEtat.INACTIF:
        this.dessinerInactif();
        break;
      case AvatarEtat.TOUCHE:
        this.dessinerTouche();
        break;
      case AvatarEtat.EXPLOSE:
      case AvatarEtat.MORT:
        this.dessinerExplose();
        break;
    }
  }
  
  public void dessinerProjectileInactif() {
    boolean projectileDisponible;
    int largeurProjectileInactif = this.projectilesType == ProjectileType.LASER ? 24: 12;
    int hauteurProjectileInactif = this.projectilesType == ProjectileType.LASER ? 4 : 12;
    float positionProjectileX;
    for (int index = 0; index < this.NB_PROJECTILES_MAXIMUM; ++index) {
      projectileDisponible = this.projectilesInactifs.size() > index;
      
      stroke(ROSE);
      strokeWeight(1);
      fill(projectileDisponible ? ROSE : GRIS_FONCE);
      if (this.projectilesType == ProjectileType.LASER) {
        positionProjectileX = width - ZONE_DEGAGEMENT - (largeurProjectileInactif * index) - (6 * index);
        rect(positionProjectileX - largeurProjectileInactif, 38, largeurProjectileInactif, hauteurProjectileInactif, 2, 2, 2, 2);
      } else {
        positionProjectileX = width - ZONE_DEGAGEMENT - (largeurProjectileInactif/2) - (largeurProjectileInactif * index) - (6 * index);
        ellipse(positionProjectileX, 40, largeurProjectileInactif, hauteurProjectileInactif);
      }
    }
  }
  
  public void lancerProjectile() {
    if (!this.peutLancerProjectile()) {
      return;
    }

    this.projectile = projectilesInactifs.get(0);
    this.projectile.changerType(this.projectilesType);

    this.projectile.definirPositionInitiale(this.position.x, this.position.y);

    this.definirProjectileActif(this.projectile);
    
    this.delaiAvantProchainTir = tempsEcouleEnMilliseconde + 200;
    
    if (sonProjectile.isPlaying()) {
      sonProjectile.stop();
    }
    sonProjectile.play();
  }
  
  public void definirProjectileActif(Projectile projectile) {
    if (this.projectilesActifs.size() >= NB_PROJECTILES_MAXIMUM) {
      return;
    }
    
    projectile.actif = true;
    this.projectilesInactifs.remove(projectile);
    this.projectilesActifs.add(projectile);
  }
  
  public void definirProjectileInactif(Projectile projectile) {
    if (this.projectilesInactifs.size() >= NB_PROJECTILES_MAXIMUM) {
      return;
    }
    projectile.actif = false;
    this.projectilesActifs.remove(projectile);
    this.projectilesInactifs.add(projectile);
  }
  
  public void toucheParObstacle(){
    if (this.etat != AvatarEtat.ACTIF) {
      return;
    }
    this.delaiAvantDessinerEtatActif = tempsEcouleEnMilliseconde + 1000;
    this.etat = AvatarEtat.TOUCHE;
    this.retirerPointDeVie();
    if (!sonAvatarExplosion.isPlaying()) {
      sonAvatarTouche.play();
    }
  }
  
  public void gererActions() {
    if (isKeyPressedUp) {
      this.bouger(AvatarDirection.HAUT, tempsEcouleEntreDeuxFrames);
    }
      
    if (isKeyPressedDown) {
      this.bouger(AvatarDirection.BAS, tempsEcouleEntreDeuxFrames);
    }
  
    if (isKeyPressedLeft) {
      this.bouger(AvatarDirection.GAUCHE, tempsEcouleEntreDeuxFrames);
    }
  
    if (isKeyPressedRight) {
      this.bouger(AvatarDirection.DROITE, tempsEcouleEntreDeuxFrames);
    }
  }
  
  private void initialiserProjectiles() {
    this.projectilesActifs = new ArrayList<Projectile>();
    this.projectilesInactifs = new ArrayList<Projectile>();
    
    for (int index = 0; index < NB_PROJECTILES_MAXIMUM; ++index) {
      this.projectile = new Projectile();
      this.projectile.actif = false;
      this.projectilesInactifs.add(this.projectile);
    }
  }
  
  private boolean peutLancerProjectile() {
    return tempsEcouleEnMilliseconde > this.delaiAvantProchainTir
           && this.etat == AvatarEtat.ACTIF
           && this.projectilesInactifs.size() > 0
           && this.projectilesActifs.size() < NB_PROJECTILES_MAXIMUM;
  }
  
  private void retirerPointDeVie() {
    this.nbPointVie--;
    this.setImage();
    if (this.nbPointVie <= 0) {
      this.etat = AvatarEtat.EXPLOSE;
      this.delaiAvantDessinerEtatMort = tempsEcouleEnSeconde + 3;
      sonAvatarExplosion.play();
      return;
    }
  }
  
  private void setImage() {
    this.image = createImage(DEMENSION_PAR_DEFAUT, DEMENSION_PAR_DEFAUT, ARGB);
    this.image.copy(imageAvatar, DEMENSION_PAR_DEFAUT * (NB_POINT_VIE_TOTAL - this.nbPointVie), 0, DEMENSION_PAR_DEFAUT, DEMENSION_PAR_DEFAUT, 0, 0, DEMENSION_PAR_DEFAUT, DEMENSION_PAR_DEFAUT);
  }
  
  private void dessinerProjectilesActif() {
    for (int index = 0; index < this.projectilesActifs.size(); index++) {
       this.projectile = this.projectilesActifs.get(index);
       if (this.projectile.actif) {
         this.projectile.bouger();
         this.projectile.dessinerActif();
       } else {
         this.definirProjectileInactif(this.projectile);
       }
    }
  }
  
  private void dessinerImage() {
    image(this.image, position.x - LARGEUR/2, position.y - HAUTEUR/2, LARGEUR, HAUTEUR);
  }
  
  private void dessinerActif() {
    noStroke();
    fill(400, 20, 200);
    this.dessinerImage();
  }
  
  private void dessinerInactif() {
    if(this.position.y >= this.positionInitiale.y) {
       this.position.y -= 5 + tempsEcouleEntreDeuxFrames;
    } else {
      this.etat = AvatarEtat.ACTIF;
    }
    this.dessinerActif();
  }
  
  private void dessinerTouche() {
    if (tempsEcouleEnMilliseconde >= this.delaiAvantDessinerEtatActif) {
      this.etat = AvatarEtat.ACTIF;
      this.dessinerActif();
      return;
    }
    float alpha = map(128 + PApplet.parseInt(oscillation(millis(), 128.0f, 500.0f)), 0, 255, 20, 255);
    noStroke();
    filter(GRAY);
    tint(255, alpha);
    this.dessinerImage();
  }
  
  private void dessinerExplose() {
    if (tempsEcouleEnSeconde >= this.delaiAvantDessinerEtatMort) {
      this.etat = AvatarEtat.MORT;
    }
    
    filter(POSTERIZE, 12);
    
    if (this.conteurImageExplosion < imagesExplosion.length) {
      image(imagesExplosion[this.conteurImageExplosion], position.x - LARGEUR/2, position.y - HAUTEUR/2, LARGEUR, HAUTEUR);
      this.conteurImageExplosion ++;
    }
  }
}
interface JeuEtat {
  String
  INSTRUCTION = "instruction",
  DEMARRE = "demarre",
  PARTIE_TERMINEE = "partie-terminee";
}

class Jeu {
  // Jeu
  public String etat = JeuEtat.INSTRUCTION;
  private float delaiAvantChangerVitesseJeu = 5;
  private float vitesseJeu = 1;
  private int pointage = 0;
  
  // Avatar
  private Avatar avatar;
  
  // Obstacles
  public float delaiAvantAfficherProchainObstacle = 800.0f;
  private final int NB_OBSTACLES_MAXIMUM = 18;
  private Obstacle obstacle;
  private Iterator<Obstacle> obstacleIterator;
  private ArrayList<Obstacle> obstaclesActifs;
  private ArrayList<Obstacle> obstaclesInactifs;
  
  // Instruction
  public final int DIMENSION_SOLEIL = 420;
  public final int HAUTEUR_IMAGE_INSTRUCTION = 73;
  private float positionSoleil = - this.DIMENSION_SOLEIL / 2;
  private float positionYImageInstruction = - HAUTEUR_IMAGE_INSTRUCTION;
  private float opaciteLogo = 0;
  private float opaciteEtoiles = 0;
  private float opaciteBoutonJouer = 0;
  private float textSizeBoutonJouer = 20;
  private boolean animationInstructionActif = true;
  private int nbEtoiles = 0;
  private float[] largeurEtoile;
  private PVector[] positionEtoile;
  
  //Entete 
  private final int HAUTEUR_ENTETE = 68;
  
  Jeu(){
    this.initialiser();
  }
  
  public void initialiser() {
    if (this.nbEtoiles == 0) {
      this.nbEtoiles = 200;
      largeurEtoile = new float[this.nbEtoiles];
      positionEtoile = new PVector[this.nbEtoiles];
      for (int index = 0; index < this.nbEtoiles; index++) {
        largeurEtoile[index] = random(0.1f, 3);
        positionEtoile[index] = new PVector(genererNombreAleatoireMinMax(0, width), genererNombreAleatoireMinMax(0, height));
      }
    }
    
    if (!musiqueInstruction.isPlaying()) {
      musiqueInstruction.loop();
    }
  }
  
  public void definirEtatJeuOnEvent() {
    switch(this.etat) {
      case JeuEtat.INSTRUCTION:
      case JeuEtat.PARTIE_TERMINEE: 
        if (this.animationInstructionActif) {
          return;
        }
        this.initialiserEtatDemarre();
        break;
      case JeuEtat.DEMARRE: 
        return;
    }
  }
  
  public void definirActionJeuOnKeyPressed() {
    if (!isKeyPressedSpace) {
      return;
    }
    
    this.definirEtatJeuOnEvent();
  }

  public void dessiner() {
    this.mettreAJourTemps();
    this.dessinerArrierePlan();
    
    switch(this.etat) {
      case JeuEtat.INSTRUCTION: 
        this.dessinerEtatJeuInstruction();
        break;
      case JeuEtat.DEMARRE:
        this.dessinerEtatJeuDemarre();
        
        if (this.avatar.etat == AvatarEtat.MORT) {
          this.initialiserEtatPartieTermine();
        };
        break;
      case JeuEtat.PARTIE_TERMINEE:
        this.dessinerEtatJeuPartieTerminee();
        break;
    }
  }
  
  public void basculerTypeProjectiles() {
    if (this.etat != JeuEtat.DEMARRE || this.avatar == null) {
      return;
    }

    this.avatar.basculerTypeProjectiles();
  }

  public void avatarLancerProjectile() {
    if (this.etat != JeuEtat.DEMARRE || this.avatar == null) {
      return;
    }
    
    this.avatar.lancerProjectile();
  }

  private void initialiserTemps() {
    tempsEcouleEnMillisecondeInitiale = millis();
    this.mettreAJourTemps();
    this.delaiAvantAfficherProchainObstacle = 900.0f;
    this.avatar.delaiAvantProchainTir = 500;
    this.vitesseJeu = 1;
    this.definirDelaiAvantChangerVitesseJeu();
  }

  private void mettreAJourTemps() {
    tempsEcouleEntreDeuxFrames = (millis() - (tempsEcouleEnMilliseconde + tempsEcouleEnMillisecondeInitiale)) / 1000.0f;
    tempsEcouleEnMilliseconde = millis() - tempsEcouleEnMillisecondeInitiale;
    tempsEcouleEnSeconde = tempsEcouleEnMilliseconde/1000.0f;
  }

  private void definirDelaiAvantChangerVitesseJeu() {
    this.delaiAvantChangerVitesseJeu = tempsEcouleEnSeconde + 8;
  }

  private void initialiserEtatDemarre() {
    if (this.etat == JeuEtat.DEMARRE) {
      return;
    }

    this.etat = JeuEtat.DEMARRE;

    this.pointage = 0;
    this.avatar = new Avatar();
    this.initialiserObstacles();
    this.initialiserTemps();

    sonBoutonClic.play();
    if (musiqueInstruction.isPlaying()) {
      musiqueInstruction.stop();
    }
    
    if (musiquePartieTerminee.isPlaying()) {
      musiquePartieTerminee.stop();
    }
    
    if (!musiquePartieDemarre.isPlaying()) {
      musiquePartieDemarre.loop();
    }

    videoPartieTerminee.stop();
  }

  private void initialiserObstacles() {
    this.obstaclesActifs = new ArrayList<Obstacle>();
    this.obstaclesInactifs = new ArrayList<Obstacle>();
    
    for (int index = 0; index < NB_OBSTACLES_MAXIMUM; ++index) {
      obstacle = new Obstacle();
      this.obstaclesInactifs.add(obstacle);
    }
  }

  private void ajouterNouvelleObstacles() {
    if (tempsEcouleEnMilliseconde < this.delaiAvantAfficherProchainObstacle || this.avatar.etat == AvatarEtat.INACTIF) {
      return;
    }
    
    if (this.obstaclesInactifs.size() > 0) {
      obstacle = obstaclesInactifs.get(0);
      
      obstacle.initialiser();
      
      this.obstaclesInactifs.remove(obstacle);
      this.obstaclesActifs.add(obstacle);

      this.delaiAvantAfficherProchainObstacle = obstacle.getDelaiAffichageObstacle();
    }
  }

  private void definirObstacleInactif(Obstacle obstacle) {
    if (this.obstaclesInactifs.size() >= NB_OBSTACLES_MAXIMUM) {
       return;
    }
    
    this.obstaclesActifs.remove(obstacle);
    this.obstaclesInactifs.add(obstacle);
    obstacle.actif = false;
  }
  
  private void detecterCollisionAvatarObstacles() {
    if (!this.avatar.peutEtreTouche()) {
      return;
    }
    for (int index = 0; index < this.obstaclesActifs.size(); ++index) {
      this.obstacle = this.obstaclesActifs.get(index);
      if (isCerclesEnCollision(this.avatar.position.x, this.avatar.position.y, this.avatar.LARGEUR/2, this.obstacle.position.x + this.obstacle.dimension/2, this.obstacle.position.y + this.obstacle.dimension/2, this.obstacle.dimension/2)) {
        this.avatar.toucheParObstacle();
        this.definirObstacleInactif(this.obstacle);
      }
    }
  }
  
  private void detecterCollisionProjectilesObstacles() {
    for (int indexProjectile = 0; indexProjectile < this.avatar.projectilesActifs.size(); ++indexProjectile) {
      Projectile projectile = this.avatar.projectilesActifs.get(indexProjectile);
      for (int indexObstacle = 0; indexObstacle < this.obstaclesActifs.size(); ++indexObstacle) {
        this.obstacle = this.obstaclesActifs.get(indexObstacle);
        if (isCerclesEnCollision(projectile.position.x, projectile.position.y, projectile.getLargeurProjectile()/2, this.obstacle.position.x + this.obstacle.dimension/2, this.obstacle.position.y + this.obstacle.dimension/2, this.obstacle.dimension/2)) {           
          this.avatar.definirProjectileInactif(projectile);
          
          this.definirObstacleInactif(this.obstacle);
          
          if (sonObstacTouche.isPlaying()) {
            sonObstacTouche.stop();
          }
          sonObstacTouche.play();
          this.pointage += 5;
        }
      }
    }
  }

  private void initialiserEtatPartieTermine() {
    if (this.etat == JeuEtat.PARTIE_TERMINEE) {
      return;
    }

    this.etat = JeuEtat.PARTIE_TERMINEE;

    this.avatar = null;
    this.initialiserObstacles();

    
    if (musiquePartieDemarre.isPlaying()) {
      musiquePartieDemarre.stop();
    }
    
    if (!musiquePartieTerminee.isPlaying()) {
      musiquePartieTerminee.loop();
    }

    videoPartieTerminee.loop();
  }
  
  private void dessinerEtatJeuInstruction() {
    if (this.etat != JeuEtat.INSTRUCTION) {
      return;
    }
    this.opaciteEtoiles = this.opaciteEtoiles < 180 ? this.opaciteEtoiles + 2 : 180;
    
    this.opaciteLogo = this.opaciteLogo < 255 ?  this.opaciteLogo + 4 : 255;
    tint(255, this.opaciteLogo);
    image(imageLogo, width/2 - 252/2, height/2 - 60, 252, 34);
    
    if (this.positionSoleil < 0) {
       this.positionSoleil += 2;
    } else {
      positionSoleil = 0;
      this.animationInstructionActif = false;

      stroke(ROSE, this.opaciteBoutonJouer);
      strokeWeight(1.5f);
      fill(GRIS_FONCE, this.opaciteBoutonJouer);
      beginShape();
      vertex(170, 326);
      vertex(width - 146, 326);
      vertex(width - 143, 330);
      vertex(width - 168, 368);
      vertex(width - 174, 372);
      vertex(143, 372);
      vertex(140, 368);
      vertex(165, 329);
      endShape(CLOSE);
      
      textFont(fontBlack);
      textSize(this.textSizeBoutonJouer);
      textAlign(CENTER);
      dessinerTexteBordure("Jouer".toUpperCase(),  width/2, height/2 + 57, 2, GRIS_FONCE, ROSE, this.opaciteBoutonJouer);
      
      this.textSizeBoutonJouer = this.textSizeBoutonJouer < DIMENSION_TEXTE_BOUTON ?  this.textSizeBoutonJouer + 0.5f : DIMENSION_TEXTE_BOUTON;
      this.opaciteBoutonJouer = this.opaciteBoutonJouer < 255 ?  this.opaciteBoutonJouer + 4 : 255;
      
      this.positionYImageInstruction = this.positionYImageInstruction > HAUTEUR_IMAGE_INSTRUCTION ? this.positionYImageInstruction : this.positionYImageInstruction + 4 + tempsEcouleEntreDeuxFrames;
  
      tint(255, 255);
      image(imageInstruction, ZONE_DEGAGEMENT , height - this.positionYImageInstruction - ZONE_DEGAGEMENT, 402, HAUTEUR_IMAGE_INSTRUCTION);
    }
  }

  private void dessinerArrierePlan() {
    background(NOIR);
    this.dessinerRayonSoleil();
    this.dessinerEtoiles();
    if (this.etat == JeuEtat.PARTIE_TERMINEE) {
      tint(255, 255);
      videoPartieTerminee.mask(masquePartieTerminee);
      image(videoPartieTerminee, width/2 - 280/2, height/2 - 280/2, 280, 280);
    }
    this.dessinerSoleil();
  }
  
  private void dessinerRayonSoleil() {
    float dimension = map(this.DIMENSION_SOLEIL/2 + PApplet.parseInt(oscillation(millis(), this.DIMENSION_SOLEIL/2, 4000.0f)), 0, this.DIMENSION_SOLEIL, this.DIMENSION_SOLEIL + 50, this.DIMENSION_SOLEIL + 80);
    tint(255, 255);      
    imageRayonSoleil.mask(masqueRayonSoleil);
    image(imageRayonSoleil, this.positionSoleil - (dimension/2 + tempsEcouleEntreDeuxFrames), this.positionSoleil - (dimension/2 + tempsEcouleEntreDeuxFrames), dimension, dimension);
  }

  private void dessinerEtoiles() {
    float acceleration = isKeyPressedUp ? 0.3f : 0;
    for (int index = 0; index < this.nbEtoiles; index++) {
      stroke(BLANC, this.opaciteEtoiles);
      strokeWeight(this.largeurEtoile[index]);
      point(this.positionEtoile[index].x, this.positionEtoile[index].y);
      
      if (this.etat != JeuEtat.INSTRUCTION) {
        this.positionEtoile[index].y += 0.05f * this.largeurEtoile[index] * this.vitesseJeu + acceleration;
        if (this.positionEtoile[index].y > height + this.largeurEtoile[index] + 5) {
          this.positionEtoile[index].y = - this.largeurEtoile[index] - 5;
        }
      }
    }
  }

  private void dessinerSoleil() {
    tint(255, 255);
    image(imageSoleil, this.positionSoleil - this.DIMENSION_SOLEIL/2, this.positionSoleil - this.DIMENSION_SOLEIL/2, this.DIMENSION_SOLEIL, this.DIMENSION_SOLEIL);
  }
  
  private void dessinerEtatJeuDemarre() {
    if ((tempsEcouleEnSeconde > this.delaiAvantChangerVitesseJeu) && (this.vitesseJeu <= 6)) {
      this.vitesseJeu = this.vitesseJeu * 1.15f;
      this.definirDelaiAvantChangerVitesseJeu();
    }
    
    this.ajouterNouvelleObstacles();
    this.dessinerObstacles();
    
    this.avatar.gererActions();
    this.avatar.dessiner();
    
    this.detecterCollisionAvatarObstacles();
    this.detecterCollisionProjectilesObstacles();
    
    this.dessinerEntete();
  }
  
  private void dessinerEntete() {
    noStroke();
    fill(NOIR, 220);
    rect(0, 0, width, HAUTEUR_ENTETE);
    stroke(ROSE);
    strokeWeight(5);
    line(-1, 0, width + 1, 0);
    
    stroke(BLANC, 40);
    strokeWeight(0.2f);
    line(-1, HAUTEUR_ENTETE, width + 1, HAUTEUR_ENTETE);
    
    textFont(fontSemiBold);
    textSize(12);
    fill(BLANC);
    textAlign(LEFT);
    text("Vies", ZONE_DEGAGEMENT, ZONE_DEGAGEMENT);
    
    textAlign(CENTER);
    text(this.pointage > 1 ? "Points" : "Point", width/2, ZONE_DEGAGEMENT);
    
    textAlign(RIGHT);
    text("Projectiles", width - ZONE_DEGAGEMENT, ZONE_DEGAGEMENT);
    
    this.dessinerPointage();
    this.dessinerNbVieAvatar();
    this.avatar.dessinerProjectileInactif();
  }
  
  private void dessinerPointage() {
    textFont(fontBlack);
    textSize(28);
    textAlign(CENTER);
    dessinerTexteBordure(str(pointage), width/2, 52, 2, GRIS_FONCE, ROSE, 250.0f);
  }
  
  private void dessinerNbVieAvatar() {
    for (int index = 0; index < avatar.NB_POINT_VIE_TOTAL; index++) {
      this.dessinerCoeur(ZONE_DEGAGEMENT + (index * 22), 48, 18, avatar.nbPointVie <= index);
    }
  }
  
  private void dessinerCoeur(float positionX, float positionY, float dimension, boolean inactif) {
    noStroke();
    textFont(fontCoeur);
    textSize(dimension);
    textAlign(LEFT);
    int fill = inactif ? GRIS_FONCE : ROSE;
    dessinerTexteBordure("♥", positionX, positionY, 2, fill, ROSE, 255);
  }

  private void dessinerObstacles() {
    if (this.etat != JeuEtat.DEMARRE) {
      return;
    }

    this.obstacleIterator = obstaclesActifs.iterator();
    
    while (this.obstacleIterator.hasNext()) {
      this.obstacle = obstacleIterator.next();
      this.obstacle.bouger(this.vitesseJeu, isKeyPressedUp && this.avatar.etat != AvatarEtat.EXPLOSE); 
      
      if (this.obstacle.actif) {
          this.obstacle.dessiner();
      } else {
        this.obstacleIterator.remove();
        this.obstaclesInactifs.add(obstacle);
      }
    }
  }
  
  private void dessinerEtatJeuPartieTerminee() {
    tint(255, 255);
    image(imageLogo, width - ZONE_DEGAGEMENT - 150, ZONE_DEGAGEMENT, 150, 20);
    
    image(imageGameOver, width/2 - 266/2, 246, 266, 33);
    
    textFont(fontBlack);
    textSize(40);
    textAlign(CENTER);
    dessinerTexteBordure(str(this.pointage) , width/2, 334, 2, GRIS_FONCE, ROSE, 255);
    
    textFont(fontSemiBold);
    fill(BLANC);
    textSize(16);
    text(this.pointage > 1 ? "points" : "point", width/2, 356);
    
    image(imageFondBouton, width/2 - 167/2, 472, 167, 47);
    
    textFont(fontBlack);
    textSize(DIMENSION_TEXTE_BOUTON);
    textAlign(CENTER);
    dessinerTexteBordure("Rejouer".toUpperCase() , width/2, 504, 2, GRIS_FONCE, ROSE, 255);
  }
}
class Obstacle {
  final int DEMENSION_PAR_DEFAUT = 120;
  public PVector position;
  public int dimension;
  public boolean actif;
  public PImage image;
  
  Obstacle(){
     this.initialiser();
  }
  
  public void initialiser() {
    this.actif = true;
    this.position = new PVector(genererNombreAleatoireMinMax(-this.dimension/2, PApplet.parseInt(width - this.dimension/2)), - this.dimension - 20);
    this.dimension = PApplet.parseInt(random(40, 60));
    this.image = createImage(DEMENSION_PAR_DEFAUT, DEMENSION_PAR_DEFAUT, ARGB);
    this.image.copy(imageObstacles, DEMENSION_PAR_DEFAUT * PApplet.parseInt(random(0, 6)), 0, DEMENSION_PAR_DEFAUT, DEMENSION_PAR_DEFAUT, 0, 0, DEMENSION_PAR_DEFAUT, DEMENSION_PAR_DEFAUT);
  }
  
  public void bouger(float vitesseJeu, boolean acceleration) {
    if (!this.actif) {
      return;
    }
    
    if (acceleration) {
      this.position.y += 1.4f;
    }

    this.position.y += (this.dimension * 0.02f) + vitesseJeu + tempsEcouleEntreDeuxFrames;
    
    this.actif = this.position.y <= height + this.dimension +10;
  }

  public void dessiner() {
    tint(255, 255);
    image(this.image, position.x, position.y, dimension, dimension);
  }
  
  public float getDelaiAffichageObstacle() {
    if (tempsEcouleEnSeconde > 120) {
       return tempsEcouleEnMilliseconde + random(50, 250);
    } else if (tempsEcouleEnSeconde > 40) {
      if (isKeyPressedUp) {
         return tempsEcouleEnMilliseconde + random(50, 100);
      } else {
         return tempsEcouleEnMilliseconde + random(150, 300);
      }
    } else if (tempsEcouleEnSeconde > 20){
      if (isKeyPressedUp) {
         return tempsEcouleEnMilliseconde + random(200, 400);
      } else {
         return tempsEcouleEnMilliseconde + random(500, 900);
      }
      
    } else {
      if (isKeyPressedUp) {
         return tempsEcouleEnMilliseconde + random(800, 1000);
      } else {
         return tempsEcouleEnMilliseconde + random(1000, 1200);
      }
    }
  }
}
interface ProjectileType {
  String
  LASER = "laser",
  BALLE = "balle";
}

class Projectile {
  public final int LARGEUR_LASER = 4;
  private final int HAUTEUR_LASER = 20;
  private final int DIMENSION_BALLE_ACTIF = 8;
  private final float VITESSE_PROJECTILE = 12.0f;
  public PVector position;
  public boolean actif;
  
  private PVector positionInitiale;
  private String type;

  Projectile(){
    position = new PVector(0, 0);
    this.changerType(ProjectileType.LASER);
  }
  
  public void bouger() {
    if (!this.actif) {
      return;
    }
    
    this.position.y -= VITESSE_PROJECTILE + tempsEcouleEntreDeuxFrames;
    
    this.actif = this.position.y >= this.positionInitiale.y - height - (HAUTEUR_LASER * 8);
  }
  
  public void definirPositionInitiale(float positionX, float positionY) {
    this.positionInitiale = new PVector(positionX, positionY);
    this.position = new PVector(positionX, positionY);
    this.actif = true;
  }
  
  public void changerType(String type) {
    this.type = type;
  }
  
  public int getLargeurProjectile(){
    return this.type == ProjectileType.LASER ? LARGEUR_LASER : DIMENSION_BALLE_ACTIF;
  }
  
  public void dessinerActif() {
    if (!actif) {
      return;
    }
    if (this.type == ProjectileType.LASER) {
      stroke(ROSE);
      strokeWeight(LARGEUR_LASER);
      line(position.x, position.y, position.x, position.y + HAUTEUR_LASER);
    } else {
      noStroke();
      fill(ROSE);
      ellipse(position.x, position.y, DIMENSION_BALLE_ACTIF, DIMENSION_BALLE_ACTIF);
    }
  }
}
  public void settings() {  size(450, 600);  pixelDensity(displayDensity()); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "CutiesBreach" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
