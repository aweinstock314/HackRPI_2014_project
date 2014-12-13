package client;

import javax.media.opengl.GL2;
import org.json.simple.*;

public class DrawObject {
    public float x;
    public float y;
    public float z;
    public float theta;
    public float phi;
    public String type;
    private boolean isPlayer;
    private CameraHandler ch;
    private JSONArray model;

    // might be cleaner to have these as seperate classes, 
    // rather than two constructors on the same class, with unused 
    // fields

    // player constructor
    public DrawObject(float x, float y,float z, float th, float ph, String type, CameraHandler ch) {
        setPosition(x,y,z);
        setOrientation(th,ph);
        this.type = type;
        isPlayer = true;
        this.ch = ch;
        model = null; // TODO: players might need models for collision detection
    }
    // non-player constructor
    public DrawObject(float x, float y,float z, float th, float ph, String type, JSONArray model) {
        setPosition(x,y,z);
        setOrientation(th,ph);
        this.type = type;
        isPlayer = false;
        ch = null;
        this.model = model;
    }

    public void setPosition(float x,float y,float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        if(isPlayer) {
            ch.x = x;
            ch.y = y;
            ch.z = z;
        }
    }

    public void setOrientation(float th, float ph) {
        theta = th;
        phi = ph;
        if(isPlayer) {
            ch.theta = th;
            ch.phi = ph;
        }
    }

    public void draw(GL2 gl2) {
        if(isPlayer || model == null) return;
        //System.out.printf("Drawing an object at (%f, %f, %f)\n", x, y, z);
        gl2.glBegin(gl2.GL_TRIANGLES);
        for(int i = 0; i < (model.size()/3); i+=3) {
            gl2.glColor3f((float)Math.random(), (float)Math.random(), (float)Math.random());
            //TODO: factor in rotation by (theta, phi)
            gl2.glVertex3d(
                ((Number)model.get(i)).floatValue() + x,
                ((Number)model.get(i+1)).floatValue() + y,
                ((Number)model.get(i+2)).floatValue() + z);
        }
        gl2.glEnd();
    }
}
