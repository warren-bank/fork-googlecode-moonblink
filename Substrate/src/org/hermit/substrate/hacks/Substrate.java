
/**
 * Substrate: a collection of eye candies for Android.  Various screen
 * hacks from the xscreensaver collection can be viewed standalone, or
 * set as live wallpapers.
 * <br>Copyright 2010 Ian Cameron Smith
 *
 * <p>This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation (see COPYING).
 * 
 * <p>This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */


package org.hermit.substrate.hacks;


import org.hermit.substrate.EyeCandy;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;


/**
 * Substrate: grow crystal-like lines on a computational substrate.  This
 * is a port of the code by J. Tarbell at http://complexification.net/.
 *
 * <p>By j.tarbell, June, 2004<br>
 * Albuquerque, New Mexico<br>
 * complexification.net<br>
 * Copyright © 2003 by J. Tarbell (complex@complexification.net).
 * 
 * <p>"Modifications and extensions of these algorithms are encouraged.
 * Please send me your experiences."
 */
public class Substrate
    extends EyeCandy
{

    // ******************************************************************** //
    // Public Constants.
    // ******************************************************************** //
    
    /**
     * Preferences name for preferences relating to this eye candy.
     */
    public static final String SHARED_PREFS_NAME = "substrate_settings";

    
    // ******************************************************************** //
    // Constructor.
    // ******************************************************************** //
    
    /**
     * Create a substrate drawing instance.
     * 
     * @param  context      Our application context.
     */
    public Substrate(Context context) {
        super(context);

        setCycles(3000, 6000, 10000);
    }


    // ******************************************************************** //
    // Configuration.
    // ******************************************************************** //

    /**
     * Get the shared prefs name for this eye candy,
     * 
     * @return              Shared preferences name.
     */
    @Override
    protected String getPrefsName() {
        return SHARED_PREFS_NAME;
    }
    

    /**
     * This method is called to notify subclasses that the canvas
     * configuration has changed.  This specifies the logical wallpaper
     * size, which may not match the screen size.
     * 
     * @param   width       The width of the canvas.
     * @param   height      The height of the canvas.
     * @param   config      Pixel configuration of the canvas.
     */
    @Override
    protected void onConfigurationSet(int width, int height, Bitmap.Config config) {
    }


    // ******************************************************************** //
    // Preferences.
    // ******************************************************************** //

    /**
     * Read our shared preferences from the given preferences object.
     * Subclasses must implement this to read their own preferences.
     *
     * @param   prefs       The SharedPreferences to read.
     * @param   key         The key of the preference that was changed. 
     */
    @Override
    protected void readPreferences(SharedPreferences prefs, String key) {
        try {
            String sval = prefs.getString("maxCracks", "" + maxCracks);
            maxCracks = Integer.valueOf(sval);
        } catch (Exception e) {
            Log.e(TAG, "Pref: bad maxCracks");
        }
        Log.i(TAG, "Prefs: maxCracks " + maxCracks);

        try {
            String sval = prefs.getString("sandGrains", "" + sandGrains);
            sandGrains = Integer.valueOf(sval);
        } catch (Exception e) {
            Log.e(TAG, "Pref: bad sandGrains");
        }
        Log.i(TAG, "Prefs: sandGrains " + sandGrains);
    }

    
    // ******************************************************************** //
    // Control Methods.
    // ******************************************************************** //

    /**
     * Reset this eye candy back to a blank state.  This will be called
     * at start-up, and to reset back to an initial state when the cycle
     * limit is exceeded.
     */
    @Override
    protected void reset() {
        if (canvasWidth <= 0 || canvasHeight <= 0)
            return;
        
        // erase crack grid
        final int grid = canvasWidth * canvasHeight;
        if (crackGrid == null || crackGrid.length != grid)
            crackGrid = new int[grid];
        for (int y = 0; y < canvasHeight; ++y)
            for (int x = 0; x < canvasWidth; ++x)
                crackGrid[y * canvasWidth + x] = 10001;

        // make random crack seeds
        for (int k = 0; k < 16; k++) {
            int i = random(canvasWidth * canvasHeight - 1);
            crackGrid[i] = random(360);
        }

        // make just three cracks
        if (cracks == null || cracks.length != maxCracks)
            cracks = new Crack[maxCracks];
        numCracks = 0;
        for (int k = 0; k < 3; k++)
            makeCrack();
        nextCrack = 0;

        // Clear to white.
        renderCanvas.drawColor(backgroundColor);
    }


    // ******************************************************************** //
    // Drawing.
    // ******************************************************************** //

    /**
     * Run one iteration of this screen hack, updating its appearance
     * into renderBitmap.  The work done should be restricted to a small
     * unit of work, ideally less than RUN_TIME, in order to not affect
     * the responsiveness of the home screen.
     * 
     * <p>This method will be called multiple times, to accumulate about
     * RUN_TIME ms of work per update.  Hence each call need only do one
     * small work unit.
     * 
     * @param   cycles      The total number of complete algorithm cycles
     *                      completed to date.
     * @return              The number of complete algorithm cycles
     *                      completed following this update.
     *                      May or may not be more than cycles.
     */
    @Override
    protected int iterate(int cycles) {
        final int c = nextCrack;
        if (++nextCrack >= numCracks) {
            nextCrack = 0;
            ++cycles;
        }

        cracks[c].move();

        return cycles;
    }


    // ******************************************************************** //
    // Private Methods.
    // ******************************************************************** //

    private void makeCrack() {
        if (numCracks < maxCracks) {
            // make a new crack instance
            if (cracks[numCracks] == null)
                cracks[numCracks] = new Crack();
            else
                cracks[numCracks].reset();
            numCracks++;
        }
    }


    // ******************************************************************** //
    // Crack Class.
    // ******************************************************************** //
    
    private class Crack {

        Crack() {
            // find placement along existing crack
            findStart();
            sp = new SandPainter();
        }

        void reset() {
            // find placement along existing crack
            findStart();
            sp.reset();
        }

        void findStart() {
            // pick random point
            int px = 0;
            int py = 0;

            // shift until crack is found
            boolean found = false;
            int timeout = 0;
            
            // The timeout logic looks back to front.  But if you fix it,
            // it hangs forever.
            while (!found || timeout++ > 1000) {
                px = random(canvasWidth);
                py = random(canvasHeight);
                if (crackGrid[py * canvasWidth + px] < 10000)
                    found = true;
            }

            if (found) {
                // start crack
                int a = crackGrid[py * canvasWidth + px];
                if (brandom())
                    a -= 90 + (int) random(-2f, 2.1f);
                else
                    a += 90 + (int) random(-2f, 2.1f);

                startCrack(px, py, a);
            }
        }

        void startCrack(int X, int Y, int T) {
            x = X;
            y = Y;
            azimuth = T;
            
            // Save the sin and cos of azimuth for later (frequent) use.
            double azRads = Math.toRadians(azimuth);
            sinAz = (float) Math.sin(azRads);
            cosAz = (float) Math.cos(azRads);
            
            x += 0.61 * cosAz;
            y += 0.61 * sinAz;  
        }

        void move() {
            // continue cracking
            x += 0.42 * cosAz;
            y += 0.42 * sinAz; 

            // bound check
            float z = 0.33f;
            int cx = (int) (x + random(-z, z));  // add fuzz
            int cy = (int) (y + random(-z, z));

            // draw sand painter
            regionColor();

            // draw black crack
            renderPaint.setColor(0x55000000);
            renderCanvas.drawPoint(cx, cy, renderPaint);

            if (cx >= 0 && cx < canvasWidth && cy >= 0 && cy < canvasHeight) {
                // safe to check
                int gval = crackGrid[cy * canvasWidth + cx];
                int gdel = Math.abs(gval - azimuth);
                if (gval > 10000 || gdel < 5) {
                    // continue cracking
                    crackGrid[cy * canvasWidth + cx] = azimuth;
                } else if (gdel > 2) {
                    // crack encountered (not self), stop cracking
                    findStart();
                    makeCrack();
                }
            } else {
                // out of bounds, stop cracking
                findStart();
                makeCrack();
            }
        }

        private void regionColor() {
            // start checking one step away
            float rx = x;
            float ry = y;
            boolean openspace=true;

            // find extents of open space
            while (openspace) {
                // move perpendicular to crack
                rx += 0.81 * sinAz;
                ry -= 0.81 * cosAz;
                int cx = (int) rx;
                int cy = (int) ry;
                if (cx >= 0 && cx < canvasWidth && cy >= 0 && cy < canvasHeight) {
                    // safe to check
                    if (crackGrid[cy * canvasWidth + cx] > 10000)
                        ; // space is open
                    else
                        openspace=false;
                } else
                    openspace=false;
            }
            // draw sand painter
            sp.render(rx, ry, x, y);
        }

        // sand painter
        private SandPainter sp;
 
        private float x, y;
        private int azimuth;    // direction of travel in degrees
        private float cosAz;    // cos(azimuth)
        private float sinAz;    // sin(azimuth)
    }


    // ******************************************************************** //
    // SandPainter Class.
    // ******************************************************************** //

    private class SandPainter {

        SandPainter() {
            reset();
        }

        void reset() {
            c = colourPalette.getRandom();
            g = random(0.01f, 0.1f);
        }

        void render(float x, float y, float ox, float oy) {
            // modulate gain
            g += random(-0.050f, 0.050f);
            float maxg = 1.0f;
            if (g < 0)
                g = 0;
            if (g > maxg)
                g = maxg;

            // lay down grains of sand (transparent pixels)
            renderPaint.setColor(c);
            float w = g / (sandGrains - 1);
            for (int i = 0; i < sandGrains; i++) {
                final float ssiw = (float) Math.sin(Math.sin(i * w));
                final float px = ox + (x - ox) * ssiw;
                final float py = oy + (y - oy) * ssiw;
                final float a = 0.1f - (float) i / (sandGrains * 10.0f);
                
                renderPaint.setAlpha(Math.round(a * 256));
                renderCanvas.drawPoint(px, py, renderPaint);
            }
        }

        // Colour for this SandPainter.
        private int c;

        // Gain; used to modulate the alpha for a "fuzzy" effect.
        private float g;
    }


    // ******************************************************************** //
    // Class Data.
    // ******************************************************************** //

    // Debugging tag.
    @SuppressWarnings("unused")
    private static final String TAG = "Substrate";

    
    // ******************************************************************** //
    // Private Data.
    // ******************************************************************** //

    // The maximum number of cracks we can have on the go at once.
    private int maxCracks = 50;

    // The number of currently-active cracks.
    private int numCracks = 0;

    // Grid of cracks.
    private int[] crackGrid = null;
    private Crack[] cracks = null;
    
    // Index of the next crack to be updated.  We don't update all the cracks
    // every time for performance reasons, so this keeps our place in the
    // list between updates.
    private int nextCrack = 0;

    // Number of grains of sand to paint.
    private int sandGrains = 64;

}

