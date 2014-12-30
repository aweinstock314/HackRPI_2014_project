package client;

import javax.media.opengl.GL2;

public abstract class DrawObject {
    public float x;
    public float y;
    public float z;
    public float theta;
    public float phi;
    public String type;

    public void setPosition(float x,float y,float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setOrientation(float th, float ph) {
        theta = th;
        phi = ph;
    }

    float rad2deg(float x) { return (float)((x * 180)/Math.PI); }

    public abstract void draw(GL2 gl2);
}
