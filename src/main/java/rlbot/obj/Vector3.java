package rlbot.obj;

public class Vector3 {

    public double x;
    public double y;
    public double z;

    public Vector3(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Vector3 fromFlatbuffer(rlbot.flat.Vector3 vec){
        // Invert the X value so that the axes make more sense.
        return new Vector3(-vec.x(), vec.y(), vec.z());
    }

    public Vector3(){
        this(0, 0, 0);
    }

    public Vector3 plus(Vector3 other){
        return new Vector3(x + other.x, y + other.y, z + other.z);
    }
    
    public Vector3 plus(Vector2 other){
        return new Vector3(x + other.x, y + other.y, z);
    }

    public Vector3 minus(Vector3 other){
        return new Vector3(x - other.x, y - other.y, z - other.z);
    }
    
    public Vector3 minus(Vector2 other){
        return new Vector3(x - other.x, y - other.y, z);
    }

    public Vector3 scaled(double scale){
        return new Vector3(x * scale, y * scale, z * scale);
    }

    /**
     * If magnitude is negative, we will return a vector facing the opposite direction.
     */
    public Vector3 scaledToMagnitude(double magnitude){
        if (isZero()){
            throw new IllegalStateException("Cannot scale up a vector with length zero!");
        }
        double scaleRequired = magnitude / magnitude();
        return scaled(scaleRequired);
    }

    public double distance(Vector3 other){
        double xDiff = x - other.x;
        double yDiff = y - other.y;
        double zDiff = z - other.z;
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
    }

    public double magnitude(){
        return Math.sqrt(magnitudeSquared());
    }

    public double magnitudeSquared(){
        return x * x + y * y + z * z;
    }

    public Vector3 normalised(){

        if (isZero()){
            throw new IllegalStateException("Cannot normalize a vector with length zero!");
        }
        return this.scaled(1 / magnitude());
    }

    public double dotProduct(Vector3 other){
        return x * other.x + y * other.y + z * other.z;
    }

    public boolean isZero(){
        return x == 0 && y == 0 && z == 0;
    }

    public Vector2 flatten(){
        return new Vector2(x, y);
    }

    public double angle(Vector3 v){
        double mag2 = magnitudeSquared();
        double vmag2 = v.magnitudeSquared();
        double dot = dotProduct(v);
        return Math.acos(dot / Math.sqrt(mag2 * vmag2));
    }

    public Vector3 crossProduct(Vector3 v){
        double tx = y * v.z - z * v.y;
        double ty = z * v.x - x * v.z;
        double tz = x * v.y - y * v.x;
        return new Vector3(tx, ty, tz);
    }
    
    public rlbot.vector.Vector3 toFramework(){
        return new rlbot.vector.Vector3(-(float)this.x, (float)this.y, (float)this.z);
    }
    
    public rlbot.obj.Vector3 clone(){
        return new rlbot.obj.Vector3((float)this.x, (float)this.y, Math.max(0F, (float)this.z));
    }
    
    public Vector3 confine(){
    	return new Vector3(Math.max(-4096, Math.min(4096, x)), Math.max(-5120, Math.min(5120, y)), Math.max(0, Math.min(2044, z)));
    }
    
    public Vector3 abs(){
    	return new Vector3(Math.abs(x), Math.abs(y), Math.abs(z));
    }
    
    public Vector3 multiply(Vector3 m){
        return new Vector3(x * m.x, y * m.y, z * m.z);
    }
    
    public Vector3 divide(Vector3 m){
        return new Vector3(x / m.x, y / m.y, z / m.z);
    }

	@Override
	public String toString(){
		return "(" + this.r(x) + ", " + this.r(y) + ", " + this.r(z) + ")";
	}
	
	public Vector3 withX(double x){return new Vector3(x, this.y, this.z);}
	public Vector3 withY(double y){return new Vector3(this.x, y, this.z);}
	public Vector3 withZ(double z){return new Vector3(this.x, this.y, z);}	
	
	public double r(double r){
    	return Math.round(r * 100D) / 100D;
    }
    
}
