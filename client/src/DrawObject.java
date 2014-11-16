package client;

import javax.media.opengl.GL2;
import org.json.simple.*;

public class DrawObject {
    public double x;
    public double y;
    public double z;
    public double theta;
    public double phi;
    public String type;
    public boolean isPlayer;

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

    public void draw(GL2 gl2, JSONArray model) {
        if(isPlayer) return;
        gl2.glBegin(gl2.GL_TRIANGLES);
        for(int i = 0; i < (model.size()/3); i++) {
            gl2.glVertex3d(((Number)model.get(i)).floatValue() + x, ((Number)model.get(i+1)).floatValue() + y, ((Number)model.get(i+2)).floatValue() + z);
        }
        gl2.glEnd();
    }
}
