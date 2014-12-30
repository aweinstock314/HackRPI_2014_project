package client;

import javax.media.opengl.GL2;

public class PlayerObject extends DrawObject {
    private CameraHandler ch;

    public PlayerObject(float x, float y,float z, float th, float ph, String type, CameraHandler ch) {
        setPosition(x,y,z);
        setOrientation(th,ph);
        this.type = type;
        this.ch = ch;
        //model = null; // TODO: players might need models for collision detection
        //initializeColor();
    }

    public void draw(GL2 gl2) {}
}
