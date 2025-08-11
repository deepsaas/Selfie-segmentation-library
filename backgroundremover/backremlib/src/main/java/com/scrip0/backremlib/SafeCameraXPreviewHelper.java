package com.scrip0.backremlib;

import android.content.Context;
import android.graphics.Rect;
import android.util.Size;

import androidx.annotation.Nullable;

import com.google.mediapipe.components.CameraXPreviewHelper;

/**
 * Replacement helper that avoids NPE when cameras (e.g., external webcams)
 * have no LENS_INFO_AVAILABLE_FOCAL_LENGTHS. If MediaPipe can compute a focal
 * length, we delegate to super; otherwise we estimate using a conservative FOV.
 */
public class SafeCameraXPreviewHelper extends CameraXPreviewHelper {

  // ~60° horizontal FOV is typical for many webcams/phones.
  private static final double DEFAULT_HORIZONTAL_FOV_DEGREES = 60.0;

  public SafeCameraXPreviewHelper(Context context) {
    super(context);
  }

  @Override
  protected double calculateFocalLengthInPixels(
      @Nullable float[] focalLengthsMm,
      @Nullable Size sensorSizePx,
      @Nullable Rect activeArraySize) {

    // If MediaPipe has enough metadata, use original behavior.
    try {
      if (focalLengthsMm != null && focalLengthsMm.length > 0
          && sensorSizePx != null && sensorSizePx.getWidth() > 0) {
        return super.calculateFocalLengthInPixels(focalLengthsMm, sensorSizePx, activeArraySize);
      }
    } catch (Throwable ignore) {
      // Fall through to safe estimate
    }

    // Fallback: estimate focal length in pixels from an assumed FOV.
    int widthPx = 1080;
    try {
      if (sensorSizePx != null && sensorSizePx.getWidth() > 0) {
        widthPx = sensorSizePx.getWidth();
      } else if (getFrameSize() != null && getFrameSize().getWidth() > 0) {
        widthPx = getFrameSize().getWidth();
      }
    } catch (Throwable ignore) { }

    double fovRad = Math.toRadians(DEFAULT_HORIZONTAL_FOV_DEGREES);
    double focalPx = 0.5 * widthPx / Math.tan(fovRad / 2.0);
    return Math.max(1.0, focalPx);
  }
}
