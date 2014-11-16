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

    public DrawObject(float x, float y,float z, float th, float ph,String type, boolean player, CameraHandler ch) {
        setPosition(x,y,z);
        setOrientation(th,ph);
        this.type = type;
        isPlayer = player;
        if(isPlayer) {
            this.ch = ch;
        } else { 
            ch = null;
        }
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

    public void draw(GL2 gl2, JSONArray model) {
        System.out.println("x: " + Float.toString(x));
        System.out.println("y: " + Float.toString(y));
        System.out.println("z: " + Float.toString(z));
        System.out.println();
        if(isPlayer) return;
        gl2.glBegin(gl2.GL_TRIANGLES);
        for(int i = 0; i < (model.size()/3); i++) {
            gl2.glColor3f((float)Math.random(), (float)Math.random(), (float)Math.random());
            gl2.glVertex3d(((Number)model.get(i)).floatValue() + x, ((Number)model.get(i+1)).floatValue() + y, ((Number)model.get(i+2)).floatValue() + z);
        }
        gl2.glEnd();
    }
}
