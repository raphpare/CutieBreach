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
    this.position = new PVector(genererNombreAleatoireMinMax(-this.dimension/2, int(width - this.dimension/2)), - this.dimension - 20);
    this.dimension = int(random(40, 60));
    this.image = createImage(DEMENSION_PAR_DEFAUT, DEMENSION_PAR_DEFAUT, ARGB);
    this.image.copy(imageObstacles, DEMENSION_PAR_DEFAUT * int(random(0, 6)), 0, DEMENSION_PAR_DEFAUT, DEMENSION_PAR_DEFAUT, 0, 0, DEMENSION_PAR_DEFAUT, DEMENSION_PAR_DEFAUT);
  }
  
  public void bouger(float vitesseJeu, boolean acceleration) {
    if (!this.actif) {
      return;
    }
    
    if (acceleration) {
      this.position.y += 1.4;
    }

    this.position.y += (this.dimension * 0.02) + vitesseJeu + tempsEcouleEntreDeuxFrames;
    
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
