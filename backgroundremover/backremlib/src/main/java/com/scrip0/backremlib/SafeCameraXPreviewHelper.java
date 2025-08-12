package com.scrip0.backremlib;

import android.graphics.Rect;
import android.util.Size;

import androidx.annotation.Nullable;

import com.google.mediapipe.components.CameraXPreviewHelper;

/**
 * Prevents NPE in MediaPipe when LENS_INFO_AVAILABLE_FOCAL_LENGTHS is null/empty
 * (common on external webcams). Falls back to a sensible focal length estimate.
 */
public class SafeCameraXPreviewHelper extends CameraXPreviewHelper {

  private static final double DEFAULT_HORIZONTAL_FOV_DEGREES = 60.0;

  public SafeCameraXPreviewHelper(android.content.Context context) {
    super(context);
  }

  @Override
  protected double calculateFocalLengthInPixels(
      @Nullable float[] focalLengthsMm,
      @Nullable Size sensorSizePx,
      @Nullable Rect activeArraySize) {

    if (focalLengthsMm != null && focalLengthsMm.length > 0
        && sensorSizePx != null && sensorSizePx.getWidth() > 0) {
      try {
        return super.calculateFocalLengthInPixels(focalLengthsMm, sensorSizePx, activeArraySize);
      } catch (Throwable ignored) {
        // fall through
      }
    }

    int widthPx = (sensorSizePx != null && sensorSizePx.getWidth() > 0)
        ? sensorSizePx.getWidth()
        : getFrameWidthOrFallback();

    double fovRad = Math.toRadians(DEFAULT_HORIZONTAL_FOV_DEGREES);
    double focalPx = 0.5 * widthPx / Math.tan(fovRad / 2.0);
    return Math.max(1.0, focalPx);
  }

  private int getFrameWidthOrFallback() {
    try {
      Size s = getFrameSize();
      if (s != null && s.getWidth() > 0) return s.getWidth();
    } catch (Throwable ignored) {}
    return 1080;
  }
}
      }
    }

    // Safe estimate: f(px) = 0.5 * width(px) / tan(HFOV/2)
    int widthPx = (sensorSizePx != null && sensorSizePx.getWidth() > 0)
        ? sensorSizePx.getWidth()
        : getFrameWidthOrFallback();

    double fovRad = Math.toRadians(DEFAULT_HORIZONTAL_FOV_DEGREES);
    double focalPx = 0.5 * widthPx / Math.tan(fovRad / 2.0);
    return Math.max(1.0, focalPx);
  }

  private int getFrameWidthOrFallback() {
    try {
      Size s = getFrameSize();
      if (s != null && s.getWidth() > 0) return s.getWidth();
    } catch (Throwable ignored) {}
    return 1080; // reasonable portrait fallback
  }
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
