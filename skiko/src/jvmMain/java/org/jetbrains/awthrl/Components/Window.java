package org.jetbrains.awthrl.Components;

import java.awt.Graphics;
import javax.swing.JFrame;
import org.jetbrains.awthrl.DriverApi.Engine;

public class Window extends JFrame implements Drawable 
{   
    @Override
    public void paint(Graphics g) {
        Engine.get().render(this);
    }

    public void draw() { }

    public native void redrawLayer();

    public native void updateLayer();

    public native void disposeLayer();

    public native float getContentScale();
}