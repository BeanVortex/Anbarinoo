package ir.darkdeveloper.anbarinoo.model;

import ir.darkdeveloper.anbarinoo.exception.InternalServerException;

public interface UpdateModel<T> {

    default void update(T model) {
        // how works:
        // id = model.id != null || id == null ? model.id : id;
        // useless but I like to keep it here
        var f1 = this.getClass().getDeclaredFields();
        var f2 = model.getClass().getDeclaredFields();
        try {
            for (int i = 0; i < f1.length; i++) {
                f1[i].setAccessible(true);
                f2[i].setAccessible(true);
                f1[i].set(this, f2[i].get(model) != null || f1[i].get(this) == null ? f2[i].get(model) : f1[i].get(this));
                f1[i].setAccessible(false);
                f2[i].setAccessible(false);
            }
        } catch (IllegalAccessException e) {
            throw new InternalServerException("Model update failed", e);
        }
    }
}
