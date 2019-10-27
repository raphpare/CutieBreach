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
  private float textSizeBoutonJouer = 12;
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
        largeurEtoile[index] = random(0.1, 3);
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
      strokeWeight(1.5);
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
      
      this.textSizeBoutonJouer = this.textSizeBoutonJouer < DIMENSION_TEXTE_BOUTON ?  this.textSizeBoutonJouer + 0.5 : DIMENSION_TEXTE_BOUTON;
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
    float dimension = map(this.DIMENSION_SOLEIL/2 + int(oscillation(millis(), this.DIMENSION_SOLEIL/2, 4000.0f)), 0, this.DIMENSION_SOLEIL, this.DIMENSION_SOLEIL + 50, this.DIMENSION_SOLEIL + 80);
    tint(255, 255);      
    imageRayonSoleil.mask(masqueRayonSoleil);
    image(imageRayonSoleil, this.positionSoleil - (dimension/2 + tempsEcouleEntreDeuxFrames), this.positionSoleil - (dimension/2 + tempsEcouleEntreDeuxFrames), dimension, dimension);
  }

  private void dessinerEtoiles() {
    float acceleration = isKeyPressedUp ? 0.3 : 0;
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
      this.vitesseJeu = this.vitesseJeu * 1.15;
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
    strokeWeight(0.2);
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
    color fill = inactif ? GRIS_FONCE : ROSE;
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
