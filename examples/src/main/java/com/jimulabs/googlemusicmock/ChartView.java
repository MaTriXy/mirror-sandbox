package com.jimulabs.googlemusicmock;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lintonye on 2014-12-20.
 */
public class ChartView extends View {
    private final Paint mLinePaint;
    private final Paint mCurvePaint;
    private Point[] mPoints;
    private float mSpanY = 1;
    private float mSpanX = 1;
    private Path mPath;
    private int[] mHighlightIndices;
    private List<HighlightDot> mHighlightDots = Collections.emptyList();

    public ChartView(Context context) {
        this(context, null);
    }

    public ChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChartView(final Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final Resources resources = context.getResources();
        mLinePaint = new Paint() {
            {
                setColor(resources.getColor(R.color.line_stroke));
                setStrokeWidth(dp2px(resources, 1));
                setStyle(Paint.Style.STROKE);
            }
        };
        mCurvePaint = new Paint(mLinePaint) {
            {
                setColor(resources.getColor(R.color.curve_stroke));
                setStrokeWidth(dp2px(resources, 3));
                setStrokeCap(Paint.Cap.ROUND);
                setStrokeJoin(Join.ROUND);
                setAntiAlias(true);
            }
        };
    }

    public void setData(Point[] points, int[] highlightIndices) {
        mPoints = points;
        mHighlightIndices = highlightIndices;
        mHighlightDots = createHighlightDots(points, highlightIndices);
        updatePath();
    }

    private List<HighlightDot> createHighlightDots(Point[] points, int[] highlightIndices) {
        List<HighlightDot> dots = new ArrayList<>(highlightIndices.length);
        for (int idx : highlightIndices) {
            Point p = points[idx];
            dots.add(new HighlightDot(this, p.x, p.y));
        }
        return dots;
    }

    public static class HighlightDot {
        public static final int DEFAULT_RADIUS_DP = 15;
        public final int x;
        public final int y;
        private final Paint mStrokePaint;
        private final View mOwner;
        private final Paint mFillPaint;
        private float mRadius;

        public HighlightDot(View owner, int x, int y) {
            this.x = x;
            this.y = y;
            mOwner = owner;
            final Resources resources = mOwner.getContext().getResources();
            mRadius = dp2px(resources, DEFAULT_RADIUS_DP);
            mStrokePaint = new Paint() {
                {
                    setStyle(Style.STROKE);
                    setStrokeWidth(dp2px(resources, 3));
                    setAntiAlias(true);
                    setColor(resources.getColor(R.color.dot_stroke));
                }
            };
            mFillPaint = new Paint() {
                {
                    setStyle(Style.FILL);
                    setColor(resources.getColor(R.color.dot_fill));
                }
            };
        }

        public void setRadius(float radius) {
            mRadius = dp2px(mOwner.getResources(), (int) radius);
//            float strokeWidth = dp2px(mOwner.getContext().getResources(),
//                    (int) Math.max(1, Math.min(5, radius / 2)));
//            mStrokePaint.setStrokeWidth(strokeWidth);
            mOwner.invalidate();
        }

        public void onDraw(Canvas canvas) {
//            canvas.drawRect(x - mRadius, y - mRadius, x + mRadius, y + mRadius, mFillPaint);
//            canvas.drawRect(x - mRadius, y - mRadius, x + mRadius, y + mRadius, mStrokePaint);
            canvas.drawCircle(x, y, mRadius, mFillPaint);
            canvas.drawCircle(x, y, mRadius, mStrokePaint);
        }
    }

    private Path points2Path(Point... points) {
        int midX = getMeasuredWidth() / 2;
        int midY = computeMidY(points);
        float spanY = getSpanY();
        float spanX = getSpanX();

        Path path = new Path();
        path.moveTo(applySpan(points[0].x, midX, spanX),
                applySpan(points[0].y, midY, spanY));

        for (int i = 1; i < points.length; i++) {
            int x = applySpan(points[i].x, midX, spanX);
            int y = applySpan(points[i].y, midY, spanY);
            path.lineTo(x, y);

//            Point p0 = points[i - 1];
//            Point p = points[i];
//            Point m = midpoint(p0, p);
//            path.quadTo(applySpan(m.x, midX, spanX), applySpan(m.y, midY, spanY),
//                    applySpan(p.x, midX, spanX), applySpan(p.y, midY, spanY));
        }
        return path;
    }

    private int computeMidY(Point[] points) {
        int minY = 10000, maxY = 0;
        for (Point p : points) {
            minY = Math.min(minY, p.y);
            maxY = Math.max(maxY, p.y);
        }
        return (minY + maxY) / 2;
    }

    private int applySpan(int v, int mid, float span) {
        return (int) ((v - mid) * span + mid);
    }

    private Point midpoint(Point a, Point b) {
        return new Point((a.x + b.x) / 2, (a.y + b.y) / 2);
    }


    private static float dp2px(Resources resources, int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                resources.getDisplayMetrics());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mPath != null) {
            drawLines(canvas, 8);
            drawCurve(canvas);
            drawHighlightDots(canvas);
        }
    }

    private void drawHighlightDots(Canvas canvas) {
        for (HighlightDot dot : mHighlightDots) {
            dot.onDraw(canvas);
        }
    }

    private void drawCurve(Canvas canvas) {
        if (mPath != null) {
            canvas.drawPath(mPath, mCurvePaint);
        }
    }

    private void drawLines(Canvas canvas, int lineCount) {
        int heightPerLine = getMeasuredHeight() / lineCount;
        int midX = getMeasuredWidth() / 2;
        float spanX = getSpanX();
        for (int i = 0; i < lineCount; i++) {
            float startX = applySpan(0, midX, spanX);
            float startY = i * heightPerLine;
            float stopX = applySpan(getMeasuredWidth(), midX, spanX);
            float stopY = startY;
            canvas.drawLine(startX, startY, stopX, stopY, mLinePaint);
        }
    }

    public float getSpanY() {
        return mSpanY;
    }

    public void setSpanY(float spanY) {
        mSpanY = spanY;
        updatePath();
    }

    private void updatePath() {
        mPath = points2Path(mPoints);
        invalidate();
    }

    public float getSpanX() {
        return mSpanX;
    }

    public void setSpanX(float spanX) {
        mSpanX = spanX;
        updatePath();
    }

    public List<HighlightDot> getHighlightDots() {
        return mHighlightDots;
    }
}
