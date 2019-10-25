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
