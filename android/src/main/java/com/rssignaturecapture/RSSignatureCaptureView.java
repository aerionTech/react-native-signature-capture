package com.rssignaturecapture;

import android.content.Context;

import android.content.res.Resources;
import android.content.res.TypedArray;

import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.MotionEvent;

import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;

import android.util.DisplayMetrics;

import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import java.util.ArrayList;
import java.util.List;

import com.rssignaturecapture.utils.TimedPoint;
import com.rssignaturecapture.utils.ControlTimedPoints;
import com.rssignaturecapture.utils.Bezier;

public class RSSignatureCaptureView extends View {
	private static final float STROKE_WIDTH = 5f;
	private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;

	private boolean mIsEmpty;
	private OnSignedListener mOnSignedListener;
	private int mMinWidth;
	private int mMaxWidth;
	private float mLastTouchX;
	private float mLastTouchY;
	private float mLastVelocity;
	private float mLastWidth;
	private RectF mDirtyRect;

	private List<TimedPoint> mPoints;
	private Paint mPaint = new Paint();
	private Path mPath = new Path();
	private Bitmap mSignatureBitmap = null;

	private float mVelocityFilterWeight;
	private Canvas mSignatureBitmapCanvas = null;
	private SignatureCallback callback;
	private boolean dragged = false;
	private boolean multipleTouchDragged = false;
	private int SCROLL_THRESHOLD = 50;

	private final String encodedImage = "/9j/4QAYRXhpZgAASUkqAAgAAAAAAAAAAAAAAP/sABFEdWNreQABAAQAAABkAAD/4QMraHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLwA8P3hwYWNrZXQgYmVnaW49Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5ZCI/PiA8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4OnhtcHRrPSJBZG9iZSBYTVAgQ29yZSA1LjMtYzAxMSA2Ni4xNDU2NjEsIDIwMTIvMDIvMDYtMTQ6NTY6MjcgICAgICAgICI+IDxyZGY6UkRGIHhtbG5zOnJkZj0iaHR0cDovL3d3dy53My5vcmcvMTk5OS8wMi8yMi1yZGYtc3ludGF4LW5zIyI+IDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bXA6Q3JlYXRvclRvb2w9IkFkb2JlIFBob3Rvc2hvcCBDUzYgKFdpbmRvd3MpIiB4bXBNTTpJbnN0YW5jZUlEPSJ4bXAuaWlkOjI0RTVFNjI4NzYwMjExRTk4QkQzQ0E0OUMyNTVGRDVFIiB4bXBNTTpEb2N1bWVudElEPSJ4bXAuZGlkOjI0RTVFNjI5NzYwMjExRTk4QkQzQ0E0OUMyNTVGRDVFIj4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6MjRFNUU2MjY3NjAyMTFFOThCRDNDQTQ5QzI1NUZENUUiIHN0UmVmOmRvY3VtZW50SUQ9InhtcC5kaWQ6MjRFNUU2Mjc3NjAyMTFFOThCRDNDQTQ5QzI1NUZENUUiLz4gPC9yZGY6RGVzY3JpcHRpb24+IDwvcmRmOlJERj4gPC94OnhtcG1ldGE+IDw/eHBhY2tldCBlbmQ9InIiPz7/7gAOQWRvYmUAZMAAAAAB/9sAhAABAQEBAQEBAQEBAQEBAQEBAQEB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GHRXHJ1DD4EGB5rp9Oh6kopQ81qRT8QoMD2gICAgfCobYwBHiAftgfFRF2qiqf3VA+wQOoCAgICAgICAgIFb3ZsaG0fmatf/AMit+yB59mTp0hfjZaxz4KAoHoQYFtAQEBgbDjaNx5Z5QEBAQEBAQEBAQEBAQEBAQEBAQPhAYEMAQdhBGQfMHYYEY6LSE9R01Oc5+4APVQOkwJIAAAAAA2AAYAHIAboH2AgICAgIDA2nG07zzxzgICAgICAgIELuP+y1H6P8SwInZB/+rYed7fAV1wLiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgVXeP8AZ/8Ayp/igevaxjQ0ePuH42vAsICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgZbuTvXeGTXNaQdiKxDVHl/LAr+w+ECM/ctZZWKzcQMYLKArt+plAPwxnjAu+1Y9vP8AWG9iBmnqJFXo49zPlhfPfAt4CAgICAgQe5HGh1B/dUfGxB+2BH7N/tD/AO8//KkC2gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIFT3o40ij81yD+67fsgSO2/wCx0/6W/wDseBOgICAgICAgICAgICAgICAgICAgICAgICAgICAgIEfVaf8AqaWq9x687mQkejDZ1KeIgUun7K3VnUuvSDsSsklwOJYgdIPx8oFld2zSW1hFrFRUYV69jD9Wc9frt8YEHS9naq73LbQVQ5QVllZjzY7Cg8AdvOBfQEBAQEBAr+6DOhv8PbPwtSB49m/2Z/8Aef8A5UgW0BAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAp+9DOlQ/lvXPkUsH2wJXbf9jp/wBLf/Y8CdAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQECNrE69LqF502EeaqWX5iBXdkbOntTit3V6OigfNDAuoCAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBX90Xq0N3Nehh6Ouf7pMDntL9WirHFGsQ/2yw+TCBZQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAEAgg7QdhHgYEPS6KrR+77Rc+6ykhiD0hc9KjYNg6j4wJkBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBA5dFsRkcZV1KsOYIwRA8dNpq9LWa6urpLlyWIJJIA3gAbAAIEiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB/9k=";

	public interface SignatureCallback {
		void onDragged();
	}

	public RSSignatureCaptureView(Context context, SignatureCallback callback) {

		super(context);
		this.callback = callback;

		//Fixed parameters
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeJoin(Paint.Join.ROUND);

		mMinWidth = convertDpToPx(8);
		mMaxWidth = convertDpToPx(16);
		mVelocityFilterWeight = 0.4f;
		mPaint.setColor(Color.BLACK);

		//Dirty rectangle to update only the changed portion of the view
		mDirtyRect = new RectF();

		clear();

		// set the bg color as white
		this.setBackgroundColor(Color.WHITE);

		// width and height should cover the screen
		this.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	/**
	* Get signature
	*
	* @return
	*/
	public Bitmap getSignature(String watermark) {

		Bitmap signatureBitmap = null;
		int width = this.getWidth();

		// set the signature bitmap
		if (signatureBitmap == null) {
			byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
			Bitmap immutable = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length );
			signatureBitmap = immutable.copy(Bitmap.Config.RGB_565, true);
			signatureBitmap = Bitmap.createScaledBitmap(signatureBitmap, width, width /3 , true);
		}

		Canvas canvas = new Canvas(signatureBitmap);

		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setStyle(Style.FILL);

		int fontSize = (width / 600) * 15;
		fontSize = (fontSize <= 0 ) ? 15 : fontSize;
		paint.setTextSize(fontSize);

		// important for saving signature
		this.draw(canvas);
		
		// Add Time and user details to the signature
		canvas.drawText(watermark, 10,30, paint);

		return signatureBitmap;
	}

	/**
	* clear signature canvas
	*/
	public void clearSignature() {
		clear();
	}

	private void addPoint(TimedPoint newPoint) {
		mPoints.add(newPoint);
		if (mPoints.size() > 2) {
			// To reduce the initial lag make it work with 3 mPoints
			// by copying the first point to the beginning.
			if (mPoints.size() == 3) mPoints.add(0, mPoints.get(0));

			ControlTimedPoints tmp = calculateCurveControlPoints(mPoints.get(0), mPoints.get(1), mPoints.get(2));
			TimedPoint c2 = tmp.c2;
			tmp = calculateCurveControlPoints(mPoints.get(1), mPoints.get(2), mPoints.get(3));
			TimedPoint c3 = tmp.c1;
			Bezier curve = new Bezier(mPoints.get(1), c2, c3, mPoints.get(2));

			TimedPoint startPoint = curve.startPoint;
			TimedPoint endPoint = curve.endPoint;

			float velocity = endPoint.velocityFrom(startPoint);
			velocity = Float.isNaN(velocity) ? 0.0f : velocity;

			velocity = mVelocityFilterWeight * velocity
					+ (1 - mVelocityFilterWeight) * mLastVelocity;

			// The new width is a function of the velocity. Higher velocities
			// correspond to thinner strokes.
			float newWidth = strokeWidth(velocity);

			// The Bezier's width starts out as last curve's final width, and
			// gradually changes to the stroke width just calculated. The new
			// width calculation is based on the velocity between the Bezier's
			// start and end mPoints.
			addBezier(curve, mLastWidth, newWidth);

			mLastVelocity = velocity;
			mLastWidth = newWidth;

			// Remove the first element from the list,
			// so that we always have no more than 4 mPoints in mPoints array.
			mPoints.remove(0);
		}
	}

	private void addBezier(Bezier curve, float startWidth, float endWidth) {
		ensureSignatureBitmap();
		float originalWidth = mPaint.getStrokeWidth();
		float widthDelta = endWidth - startWidth;
		float drawSteps = (float) Math.floor(curve.length());

		for (int i = 0; i < drawSteps; i++) {
			// Calculate the Bezier (x, y) coordinate for this step.
			float t = ((float) i) / drawSteps;
			float tt = t * t;
			float ttt = tt * t;
			float u = 1 - t;
			float uu = u * u;
			float uuu = uu * u;

			float x = uuu * curve.startPoint.x;
			x += 3 * uu * t * curve.control1.x;
			x += 3 * u * tt * curve.control2.x;
			x += ttt * curve.endPoint.x;

			float y = uuu * curve.startPoint.y;
			y += 3 * uu * t * curve.control1.y;
			y += 3 * u * tt * curve.control2.y;
			y += ttt * curve.endPoint.y;

			// Set the incremental stroke width and draw.
			mPaint.setStrokeWidth(startWidth + ttt * widthDelta);
			mSignatureBitmapCanvas.drawPoint(x, y, mPaint);
			expandDirtyRect(x, y);
		}

		mPaint.setStrokeWidth(originalWidth);
	}

	private void ensureSignatureBitmap() {
		if (mSignatureBitmap == null) {
			int width = this.getWidth();
			byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
			Bitmap immutable = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length );
			mSignatureBitmap = immutable.copy(Bitmap.Config.ARGB_8888, true);
			mSignatureBitmap = Bitmap.createScaledBitmap(mSignatureBitmap, width, width/3, true);
			mSignatureBitmapCanvas = new Canvas(mSignatureBitmap);
		}
	}

	public void setMinStrokeWidth(int minStrokeWidth) {
		mMinWidth = minStrokeWidth;
	}

	public void setMaxStrokeWidth(int maxStrokeWidth) {
		mMaxWidth = maxStrokeWidth;
	}

	public void setStrokeColor(int color) {
		mPaint.setColor(color);
	}

	private float strokeWidth(float velocity) {
		return Math.max(mMaxWidth / (velocity + 1), mMinWidth);
	}

	private ControlTimedPoints calculateCurveControlPoints(TimedPoint s1, TimedPoint s2, TimedPoint s3) {
		float dx1 = s1.x - s2.x;
		float dy1 = s1.y - s2.y;
		float dx2 = s2.x - s3.x;
		float dy2 = s2.y - s3.y;

		TimedPoint m1 = new TimedPoint((s1.x + s2.x) / 2.0f, (s1.y + s2.y) / 2.0f);
		TimedPoint m2 = new TimedPoint((s2.x + s3.x) / 2.0f, (s2.y + s3.y) / 2.0f);

		float l1 = (float) Math.sqrt(dx1 * dx1 + dy1 * dy1);
		float l2 = (float) Math.sqrt(dx2 * dx2 + dy2 * dy2);

		float dxm = (m1.x - m2.x);
		float dym = (m1.y - m2.y);
		float k = l2 / (l1 + l2);
		TimedPoint cm = new TimedPoint(m2.x + dxm * k, m2.y + dym * k);

		float tx = s2.x - cm.x;
		float ty = s2.y - cm.y;

		return new ControlTimedPoints(new TimedPoint(m1.x + tx, m1.y + ty), new TimedPoint(m2.x + tx, m2.y + ty));
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isEnabled())
			return false;

		float eventX = event.getX();
		float eventY = event.getY();

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				getParent().requestDisallowInterceptTouchEvent(true);
				mPoints.clear();
				mPath.moveTo(eventX, eventY);
				mLastTouchX = eventX;
				mLastTouchY = eventY;
				addPoint(new TimedPoint(eventX, eventY));

			case MotionEvent.ACTION_MOVE:
				resetDirtyRect(eventX, eventY);
				addPoint(new TimedPoint(eventX, eventY));
				if((Math.abs(mLastTouchX - eventX) > SCROLL_THRESHOLD || Math.abs(mLastTouchY - eventY) > SCROLL_THRESHOLD)){
					dragged = true;
				}
				break;

			case MotionEvent.ACTION_UP:
				resetDirtyRect(eventX, eventY);
				addPoint(new TimedPoint(eventX, eventY));
				getParent().requestDisallowInterceptTouchEvent(true);
				setIsEmpty(false);
				sendDragEventToReact();
				break;

			default:
				return false;
		}

		//invalidate();
		invalidate(
				(int) (mDirtyRect.left - mMaxWidth),
				(int) (mDirtyRect.top - mMaxWidth),
				(int) (mDirtyRect.right + mMaxWidth),
				(int) (mDirtyRect.bottom + mMaxWidth));

		return true;
	}

	public void sendDragEventToReact() {
		if (callback != null && dragged) {
			callback.onDragged();
		}
	}

	// all touch events during the drawing
	@Override
	protected void onDraw(Canvas canvas) {
		if (mSignatureBitmap != null) {
			canvas.drawBitmap(mSignatureBitmap, 0, 0, mPaint);
		}
	}


	/**
	 * Called when replaying history to ensure the dirty region includes all
	 * mPoints.
	 *
	 * @param historicalX the previous x coordinate.
	 * @param historicalY the previous y coordinate.
	 */
	private void expandDirtyRect(float historicalX, float historicalY) {
		if (historicalX < mDirtyRect.left) {
			mDirtyRect.left = historicalX;
		} else if (historicalX > mDirtyRect.right) {
			mDirtyRect.right = historicalX;
		}
		if (historicalY < mDirtyRect.top) {
			mDirtyRect.top = historicalY;
		} else if (historicalY > mDirtyRect.bottom) {
			mDirtyRect.bottom = historicalY;
		}
	}

	/**
	 * Resets the dirty region when the motion event occurs.
	 *
	 * @param eventX the event x coordinate.
	 * @param eventY the event y coordinate.
	 */
	private void resetDirtyRect(float eventX, float eventY) {

		// The mLastTouchX and mLastTouchY were set when the ACTION_DOWN motion event occurred.
		mDirtyRect.left = Math.min(mLastTouchX, eventX);
		mDirtyRect.right = Math.max(mLastTouchX, eventX);
		mDirtyRect.top = Math.min(mLastTouchY, eventY);
		mDirtyRect.bottom = Math.max(mLastTouchY, eventY);
	}


	private void setIsEmpty(boolean newValue) {
		mIsEmpty = newValue;
		if (mOnSignedListener != null) {
			if (mIsEmpty) {
				mOnSignedListener.onClear();
			} else {
				mOnSignedListener.onSigned();
			}
		}
	}

	public void clear() {
		dragged = false;
		mPoints = new ArrayList<TimedPoint>();
		mLastVelocity = 0;
		mLastWidth = (mMinWidth + mMaxWidth) / 2;
		mPath.reset();

		if (mSignatureBitmap != null) {
			mSignatureBitmap = null;
			ensureSignatureBitmap();
		}

		setIsEmpty(true);

		invalidate();
	}

	private int convertDpToPx(float dp){
		return Math.round(dp*(getResources().getDisplayMetrics().xdpi/ DisplayMetrics.DENSITY_DEFAULT));
	}

	public interface OnSignedListener {
		public void onSigned();

		public void onClear();
	}
}
