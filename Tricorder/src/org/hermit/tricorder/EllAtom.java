
/**
 * Tricorder: turn your phone into a tricorder.
 * 
 * This is an Android implementation of a Star Trek tricorder, based on
 * the phone's own sensors.  It's also a demo project for sensor access.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License version 2
 *   as published by the Free Software Foundation (see COPYING).
 * 
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 */


package org.hermit.tricorder;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.SurfaceHolder;


/**
 * An atom which draws a simple corner (an ell) in a swoop.
 */
class EllAtom
	extends Element
{

	// ******************************************************************** //
	// Constructor.
	// ******************************************************************** //

	/**
	 * Set up this view.
	 * 
	 * @param	context			Parent application context.
     * @param	sh				SurfaceHolder we're drawing in.
	 * @param	thick			Thickness of the bars we connect to.
	 */
	EllAtom(Tricorder context, SurfaceHolder sh, int thick) {
		super(context, sh);
		
		barThickness = thick;
	}

	   
    // ******************************************************************** //
	// Geometry.
	// ******************************************************************** //

    /**
     * This is called during layout when the size of this element has
     * changed.  This is where we first discover our size, so set
     * our geometry to match.
     * 
	 * @param	bounds		The bounding rect of this element within
	 * 						its parent View.
     */
	@Override
	protected void setGeometry(Rect bounds) {
		super.setGeometry(bounds);
		
		final int l = bounds.left;
		final int r = bounds.right;
		final int t = bounds.top;
		final int b = bounds.bottom;
		final int w = r - l;
		final int h = b - t;

		final int cw = w - STRAIGHTS;
		final int ch = h - STRAIGHTS;
		
		// Create the path which defines the swoop shape.
		outerCurve = new RectF(l, b - ch * 2, l + cw * 2, b);
		innerCurve = new RectF(l + barThickness, b - ch * 2 + barThickness,
							   l + cw * 2 - barThickness, b - barThickness);

		barPath = new Path();
		barPath.moveTo(l, t);
		barPath.lineTo(l, t + STRAIGHTS);
		barPath.arcTo(outerCurve, 180, -90);
		barPath.lineTo(r, b);
		barPath.lineTo(r, b - barThickness);
		barPath.lineTo(r - STRAIGHTS, b - barThickness);
		barPath.arcTo(innerCurve, 90, 90);
		barPath.lineTo(l + barThickness, t);
		barPath.close();
	}

	
    // ******************************************************************** //
	// Appearance.
	// ******************************************************************** //

	/**
	 * Set the background colour of this element.
	 * 
	 * @param	col			The new background colour, in ARGB format.
	 */
	void setBarColor(int col) {
		barColor = col;
	}
	

	/**
	 * Get the background colour of this element.
	 * 
	 * @return				The background colour, in ARGB format.
	 */
	int getBarColor() {
		return barColor;
	}
	

	// ******************************************************************** //
	// View Drawing.
	// ******************************************************************** //
	
	/**
	 * This method is called to ask the element to draw itself.
	 * 
	 * @param	canvas		Canvas to draw into.
	 * @param	paint		The Paint which was set up in initializePaint().
	 */
	@Override
	protected void drawBody(Canvas canvas, Paint paint) {
		// Drawing the bar is easy -- just draw the path.
		paint.setColor(barColor);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawPath(barPath, paint);
	}

	
    // ******************************************************************** //
    // Class Data.
    // ******************************************************************** //
	
	// Lengths of the straight legs of the ell.
	private static final int STRAIGHTS = 4;
	

	// ******************************************************************** //
	// Private Data.
	// ******************************************************************** //
	
	// Thickness of the bars we connect to.
	private int barThickness;

	// Rectangles defining the bounds of the outer and inner curves
	// of the swoop at each end.
	private RectF outerCurve;
	private RectF innerCurve;

	// Path defining the shape of the bar.
	private Path barPath;
	
	// Background colour of the header bar.
	private int barColor = 0xff00ffff;

}

