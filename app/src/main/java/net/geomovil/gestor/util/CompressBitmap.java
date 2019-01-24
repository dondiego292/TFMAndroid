package net.geomovil.gestor.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class CompressBitmap {
    private static final String TAG = CompressBitmap.class.getSimpleName();

    public static Bitmap decodeBitmapResource(Resources res, int resId,
                                              int reqWidth, int reqHeight, boolean aspectRatio) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(res, resId, options), reqWidth,
                reqHeight, true);
    }

    public static Bitmap decodeBitmapFile(String filepath,
                                          int reqWidth, int reqHeight, boolean aspectRatio) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap pic = Bitmap.createScaledBitmap(
                BitmapFactory.decodeFile(filepath, options), reqWidth,
                reqHeight, true);
        try {
            ExifInterface exif = new ExifInterface(filepath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            String old_orientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            compress(pic, filepath);
            if (old_orientation != null) {
                ExifInterface newExif = new ExifInterface(filepath);
                newExif.setAttribute(ExifInterface.TAG_ORIENTATION, old_orientation);
                newExif.saveAttributes();
            }
            pic = Bitmap.createBitmap(pic, 0, 0, pic.getWidth(), pic.getHeight(), matrix, true);

        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
        return pic;
    }

    public static void compress(Bitmap pic, String path) {
        int MAX_IMAGE_SIZE = 100 * 1024;
        int COMPRESS_QUALITY = 80;
        try {
            File f = new File(path);
            if (f.length() > MAX_IMAGE_SIZE) {
                FileOutputStream bmpFile = new FileOutputStream(path);
                pic.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, bmpFile);
                bmpFile.flush();
                bmpFile.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    public static Bitmap decodeSampledBitmapFromStream(InputStream is,
                                                       int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;


        BitmapFactory.decodeStream(is, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(is, null, options);
    }


    public static int calculateSampleSize(BitmapFactory.Options options,
                                          int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }
}
