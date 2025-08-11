package com.scrip0.backremlib;

import android.content.Context;
import android.graphics.Rect;
import android.util.Size;

import androidx.annotation.Nullable;

import com.google.mediapipe.components.CameraXPreviewHelper;

/**
 * Avoids NPE when focal lengths / sensor sizes are missing (common on UVC webcams).
 * Falls back to a conservative FOV to estimate focal length in pixels.
 */
public class SafeCameraXPreviewHelper extends CameraXPreviewHelper {
  private static final double DEFAULT_HORIZONTAL_FOV_DEGREES = 60.0; // typical webcam/phone

  public SafeCameraXPreviewHelper(Context context) { super(context); }

  @Override
  protected double calculateFocalLengthInPixels(
      @Nullable float[] focalLengthsMm, @Nullable Size sensorSizePx, @Nullable Rect activeArraySize) {

    if (focalLengthsMm != null && focalLengthsMm.length > 0 &&
        sensorSizePx != null && sensorSizePx.getWidth() > 0) {
      try {
        return super.calculateFocalLengthInPixels(focalLengthsMm, sensorSizePx, activeArraySize);
      } catch (Throwable ignore) {
        // fall through to estimate
      }
    }

    int widthPx = 1080;
    try {
      Size frame = getFrameSize();
      if (frame != null && frame.getWidth() > 0) widthPx = frame.getWidth();
      else if (sensorSizePx != null && sensorSizePx.getWidth() > 0) widthPx = sensorSizePx.getWidth();
    } catch (Throwable ignore) {}

    double fovRad = Math.toRadians(DEFAULT_HORIZONTAL_FOV_DEGREES);
    double focalPx = 0.5 * widthPx / Math.tan(fovRad / 2.0);
    return Math.max(1.0, focalPx);
  }
}
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
