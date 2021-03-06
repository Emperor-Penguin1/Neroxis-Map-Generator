package map;

import lombok.Data;
import util.Vector2f;
import util.Vector3f;

@Data
public strictfp class Unit {
    private final String type;
    private final float rotation;
    private Vector3f position;

    public Unit(String type, Vector2f position, float rotation) {
        this(type, new Vector3f(position), rotation);
    }

    public Unit(String type, Vector3f position, float rotation) {
        this.type = type;
        this.position = position;
        this.rotation = rotation;
    }

}
