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
  private int compteurImageExplosion = 0;
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
    this.compteurImageExplosion = 0;
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
    float alpha = map(128 + int(oscillation(millis(), 128.0f, 500.0f)), 0, 255, 20, 255);
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
    
    if (this.compteurImageExplosion < imagesExplosion.length) {
      image(imagesExplosion[this.compteurImageExplosion], position.x - LARGEUR/2, position.y - HAUTEUR/2, LARGEUR, HAUTEUR);
      this.compteurImageExplosion ++;
    }
  }
}
