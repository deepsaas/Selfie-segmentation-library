package com.scrip0.backremlib;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.util.Size;

import androidx.annotation.Nullable;

import com.google.mediapipe.components.CameraXPreviewHelper;

/**
 * Drop-in replacement for MediaPipe's CameraXPreviewHelper that gracefully handles
 * devices/cameras (esp. external webcams) that don't report LENS_INFO_AVAILABLE_FOCAL_LENGTHS.
 *
 * If focal lengths are missing, we estimate focal length in pixels using a conservative
 * field-of-view assumption to avoid crashes while keeping segmentation stable.
 */
public class SafeCameraXPreviewHelper extends CameraXPreviewHelper {

  // Conservative default: ~60° horizontal FOV typical for webcams/phones.
  // f(px) = 0.5 * width(px) / tan(FOV/2)
  private static final double DEFAULT_HORIZONTAL_FOV_DEGREES = 60.0;

  public SafeCameraXPreviewHelper(Context context) {
    super(context);
  }

  @Override
  protected double calculateFocalLengthInPixels(
      @Nullable float[] focalLengthsMm,
      @Nullable Size sensorSizePx,
      @Nullable Rect activeArraySize) {

    // If MediaPipe provides data, delegate to super (original behavior).
    if (focalLengthsMm != null && focalLengthsMm.length > 0 &&
        sensorSizePx != null && sensorSizePx.getWidth() > 0) {
      try {
        return super.calculateFocalLengthInPixels(focalLengthsMm, sensorSizePx, activeArraySize);
      } catch (Throwable ignored) {
        // fall through to safe estimate
      }
    }

    // Safe estimate when focal lengths are missing (external webcams commonly hit this).
    // Use preview frame width as proxy (sensor width in px) and 60° default HFOV.
    int widthPx = (sensorSizePx != null && sensorSizePx.getWidth() > 0)
        ? sensorSizePx.getWidth()
        : getFrameWidthOrFallback();

    double fovRad = Math.toRadians(DEFAULT_HORIZONTAL_FOV_DEGREES);
    double focalPx = 0.5 * widthPx / Math.tan(fovRad / 2.0);
    return Math.max(1.0, focalPx);
  }

  private int getFrameWidthOrFallback() {
    try {
      if (getFrameSize() != null && getFrameSize().getWidth() > 0) {
        return getFrameSize().getWidth();
      }
    } catch (Throwable ignored) {}
    // Reasonable portrait width fallback
    return 1080;
  }
}
