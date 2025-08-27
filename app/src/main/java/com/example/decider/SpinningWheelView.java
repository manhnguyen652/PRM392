package com.example.decider;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpinningWheelView extends View {
    
    private Paint segmentPaint;
    private Paint textPaint;
    private Paint arrowPaint;
    private Paint borderPaint;
    
    private List<String> options;
    private List<Integer> colors;
    private RectF wheelRect;
    private Path arrowPath;
    
    private float wheelRotation = 0f;
    private int centerX, centerY;
    private float wheelRadius;
    private float arrowSize;
    
    private OnSpinCompleteListener spinCompleteListener;
    private Random random = new Random();
    
    // Predefined colors for segments
    private static final int[] SEGMENT_COLORS = {
        Color.parseColor("#FF6B6B"), // Red
        Color.parseColor("#4ECDC4"), // Teal
        Color.parseColor("#45B7D1"), // Blue
        Color.parseColor("#96CEB4"), // Green
        Color.parseColor("#FFEAA7"), // Yellow
        Color.parseColor("#DDA0DD"), // Plum
        Color.parseColor("#98D8C8"), // Mint
        Color.parseColor("#F7DC6F"), // Light Yellow
        Color.parseColor("#BB8FCE"), // Light Purple
        Color.parseColor("#85C1E9"), // Light Blue
        Color.parseColor("#F8C471"), // Orange
        Color.parseColor("#82E0AA")  // Light Green
    };
    
    public interface OnSpinCompleteListener {
        void onSpinComplete(String selectedOption);
    }
    
    public SpinningWheelView(Context context) {
        super(context);
        init();
    }
    
    public SpinningWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public SpinningWheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        options = new ArrayList<>();
        colors = new ArrayList<>();
        
        // Initialize paints
        segmentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        segmentPaint.setStyle(Paint.Style.FILL);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(48f);
        textPaint.setFakeBoldText(true);
        
        arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arrowPaint.setColor(Color.parseColor("#2C3E50"));
        arrowPaint.setStyle(Paint.Style.FILL);
        
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(Color.parseColor("#34495E"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(8f);
        
        wheelRect = new RectF();
        arrowPath = new Path();
    }
    
    public void setOptions(List<String> options) {
        this.options = new ArrayList<>(options);
        generateColors();
        invalidate();
    }
    
    private void generateColors() {
        colors.clear();
        for (int i = 0; i < options.size(); i++) {
            colors.add(SEGMENT_COLORS[i % SEGMENT_COLORS.length]);
        }
    }
    
    public void setOnSpinCompleteListener(OnSpinCompleteListener listener) {
        this.spinCompleteListener = listener;
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        centerX = w / 2;
        centerY = h / 2;
        
        // Calculate wheel size (leave space for arrow)
        int minDimension = Math.min(w, h);
        wheelRadius = (minDimension * 0.35f);
        arrowSize = wheelRadius * 0.15f;
        
        // Set wheel bounds
        wheelRect.set(
            centerX - wheelRadius,
            centerY - wheelRadius,
            centerX + wheelRadius,
            centerY + wheelRadius
        );
        
        // Create arrow path (pointing down to wheel)
        createArrowPath();
        
        // Adjust text size based on wheel size
        textPaint.setTextSize(wheelRadius * 0.12f);
    }
    
    private void createArrowPath() {
        arrowPath.reset();
        
        float arrowTop = centerY - wheelRadius - arrowSize * 1.5f;
        float arrowBottom = centerY - wheelRadius + arrowSize * 0.5f;
        float arrowLeft = centerX - arrowSize;
        float arrowRight = centerX + arrowSize;
        
        // Create triangle arrow pointing down
        arrowPath.moveTo(centerX, arrowBottom);
        arrowPath.lineTo(arrowLeft, arrowTop);
        arrowPath.lineTo(arrowRight, arrowTop);
        arrowPath.close();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (options.isEmpty()) {
            return;
        }
        
        canvas.save();
        canvas.rotate(wheelRotation, centerX, centerY);
        
        // Draw wheel segments
        drawWheelSegments(canvas);
        
        canvas.restore();
        
        // Draw fixed arrow (not rotated)
        canvas.drawPath(arrowPath, arrowPaint);
        
        // Draw center circle
        canvas.drawCircle(centerX, centerY, wheelRadius * 0.1f, arrowPaint);
    }
    
    private void drawWheelSegments(Canvas canvas) {
        if (options.isEmpty()) return;
        
        float segmentAngle = 360f / options.size();
        float startAngle = -90f; // Start from top
        
        for (int i = 0; i < options.size(); i++) {
            // Draw segment
            segmentPaint.setColor(colors.get(i));
            canvas.drawArc(wheelRect, startAngle, segmentAngle, true, segmentPaint);
            
            // Draw segment border
            canvas.drawArc(wheelRect, startAngle, segmentAngle, true, borderPaint);
            
            // Draw text
            drawSegmentText(canvas, options.get(i), startAngle + segmentAngle / 2, segmentAngle);
            
            startAngle += segmentAngle;
        }
    }
    
    private void drawSegmentText(Canvas canvas, String text, float angle, float segmentAngle) {
        canvas.save();
        
        // Rotate to text position
        canvas.rotate(angle, centerX, centerY);
        
        // Calculate text position (middle of segment)
        float textRadius = wheelRadius * 0.7f;
        float textX = centerX;
        float textY = centerY - textRadius;
        
        // Adjust text size if segment is small
        float originalTextSize = textPaint.getTextSize();
        if (segmentAngle < 30) {
            textPaint.setTextSize(originalTextSize * 0.8f);
        }
        
        // Truncate text if too long
        String displayText = text;
        if (text.length() > 8) {
            displayText = text.substring(0, 6) + "...";
        }
        
        // Draw text with shadow for better visibility
        textPaint.setColor(Color.BLACK);
        canvas.drawText(displayText, textX + 2, textY + 2, textPaint);
        textPaint.setColor(Color.WHITE);
        canvas.drawText(displayText, textX, textY, textPaint);
        
        // Restore text size
        textPaint.setTextSize(originalTextSize);
        
        canvas.restore();
    }
    
    public void spin() {
        if (options.isEmpty()) return;
        
        // Calculate random rotation (3-6 full rotations + random final position)
        int baseRotations = 3 + random.nextInt(4);
        float finalAngle = random.nextFloat() * 360;
        float totalRotation = baseRotations * 360 + finalAngle;
        
        // Create rotation animator
        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(
            this, "wheelRotation", wheelRotation, wheelRotation + totalRotation);
        rotationAnimator.setDuration(3000 + random.nextInt(2000)); // 3-5 seconds
        rotationAnimator.setInterpolator(new DecelerateInterpolator());
        
        rotationAnimator.addUpdateListener(animation -> invalidate());
        
        rotationAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (spinCompleteListener != null) {
                    String selectedOption = getSelectedOption();
                    spinCompleteListener.onSpinComplete(selectedOption);
                }
            }
        });
        
        rotationAnimator.start();
    }
    
    private String getSelectedOption() {
        if (options.isEmpty()) return "";
        
        // Calculate which segment the arrow is pointing to
        float normalizedRotation = ((wheelRotation % 360) + 360) % 360;
        float segmentAngle = 360f / options.size();
        
        // Arrow points down, so we need to account for that
        // Add 90 degrees to align with our starting position
        float adjustedRotation = (normalizedRotation + 90) % 360;
        
        // Calculate segment index (reverse because wheel rotates clockwise)
        int segmentIndex = (int) ((360 - adjustedRotation) / segmentAngle) % options.size();
        
        return options.get(segmentIndex);
    }
    
    public void setWheelRotation(float rotation) {
        this.wheelRotation = rotation;
        invalidate();
    }
    
    public float getWheelRotation() {
        return wheelRotation;
    }
}
