package rlbot.obj;

public class Vector2 {

    public final double x;
    public final double y;

    public Vector2(double x, double y){
        this.x = x;
        this.y = y;
    }

    public Vector2 plus(Vector2 other){
        return new Vector2(x + other.x, y + other.y);
    }

    public Vector2 minus(Vector2 other){
        return new Vector2(x - other.x, y - other.y);
    }

    public Vector2 scaled(double scale){
        return new Vector2(x * scale, y * scale);
    }

    /**
     * If magnitude is negative, we will return a vector facing the opposite direction.
     */
    public Vector2 scaledToMagnitude(double magnitude){
        if(isZero()){
            throw new IllegalStateException("Cannot scale up a vector with length zero!");
        }
        double scaleRequired = magnitude / magnitude();
        return scaled(scaleRequired);
    }

    public double distance(Vector2 other){
        double xDiff = x - other.x;
        double yDiff = y - other.y;
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    public double magnitude(){
        return Math.sqrt(magnitudeSquared());
    }

    public double magnitudeSquared(){
        return x * x + y * y;
    }

    public Vector2 normalised(){
        if(isZero()){
            throw new IllegalStateException("Cannot normalize a vector with length zero!");
        }
        return this.scaled(1 / magnitude());
    }

    public double dotProduct(Vector2 other){
        return x * other.x + y * other.y;
    }

    public boolean isZero(){
        return x == 0 && y == 0;
    }

    public double correctionAngle(Vector2 ideal){
        double currentRad = Math.atan2(y, x);
        double idealRad = Math.atan2(ideal.y, ideal.x);
        if(Math.abs(currentRad - idealRad) > Math.PI){
            if(currentRad < 0) currentRad += Math.PI * 2;
            if(idealRad < 0) idealRad += Math.PI * 2;
        }
        return idealRad - currentRad;
    }

    /**
     * Will always return a positive value <= Math.PI
     */
    public static double angle(Vector2 a, Vector2 b){
        return Math.abs(a.correctionAngle(b));
    }

    public rlbot.vector.Vector3 toFramework(){
        return new rlbot.vector.Vector3(-(float)this.x, (float)this.y, 0);
    }
    
    public Vector2 confine(){
    	return new Vector2(Math.max(-4096, Math.min(4096, x)), Math.max(-5120, Math.min(5120, y)));
    }
    
    public Vector2 confineRatio(){
    	Vector2 v;
    	if(Math.abs(x) * (5120F / 4096F) > Math.abs(y)){
    		v = new Vector2(x > 0 ? 4096 : -4096, y * (4096F / Math.abs(x)));
    		return v;
    	}else{
    		v = new Vector2(x * (5120F / Math.abs(y)), y > 0 ? 5120 : -5120);
    		return v;
    	}
    }

	@Override
	public String toString() {
		return "[" + (int)x + ", " + (int)y + "]";
	}
	
	public Vector2 withX(double x){return new Vector2(x, this.y);}
	public Vector2 withY(double y){return new Vector2(this.x, y);}	
    
}
