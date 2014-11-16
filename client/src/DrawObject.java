package client;

public class DrawObject {
    public double x;
    public double y;
    public double z;
    public double theta;
    public double phi;
    public String type;

    public DrawObject(double x, double y,double z, double th, double ph,String type) {
        setPosition(x,y,z);
        setOrientation(th,ph);
        this.type = type;
    }

    public void setPosition(double x,double y,double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setOrientation(double th, double ph) {
        theta = th;
        phi = ph;
    }

}
