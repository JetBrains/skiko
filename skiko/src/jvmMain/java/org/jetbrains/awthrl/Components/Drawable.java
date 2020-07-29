package org.jetbrains.awthrl.Components;

public interface Drawable {
    
    public void redrawLayer();

    public void updateLayer();

    public void disposeLayer();

    public float getContentScale();
}