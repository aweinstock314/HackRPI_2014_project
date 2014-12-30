package client;

import javax.media.opengl.GL2;
import org.json.simple.*;

public class ModeledObject extends DrawObject {
    public float r, g, b;
    private JSONArray model;

    public ModeledObject(String type, JSONArray model) {
        this.type = type;
        this.model = model;
        initializeColor();
    }
    public ModeledObject(float x, float y,float z, float th, float ph, String type, JSONArray model) {
        setPosition(x,y,z);
        setOrientation(th,ph);
        this.type = type;
        this.model = model;
        initializeColor();
    }

    private void initializeColor() {
        r = (float)Math.random();
        g = (float)Math.random();
        b = (float)Math.random();
    }

    public void draw(GL2 gl2) {
        if(model == null) { return; }
        //System.out.printf("Drawing an object at (%f, %f, %f)\n", x, y, z);
        gl2.glPushMatrix();
        gl2.glTranslatef(x, y, -z);
        gl2.glRotatef(rad2deg(theta), 0, -1, 0);
        gl2.glRotatef(rad2deg(phi), 1, 0, 0);
        gl2.glBegin(gl2.GL_TRIANGLES);
        for(int i = 0; i < model.size(); i+=3) {
            //gl2.glColor3f((float)Math.random(), (float)Math.random(), (float)Math.random());
            float c1 = ((float)i)/model.size();
            float c2 = .25f + (c1 * .75f);
            gl2.glColor3f(c2*r, c2*g, c2*b);
            gl2.glVertex3d(
                ((Number)model.get(i)).floatValue(),
                ((Number)model.get(i+1)).floatValue(),
                ((Number)model.get(i+2)).floatValue());
        }
        gl2.glEnd();
        gl2.glPopMatrix();
    }
}
