package net.geomovil.gestor.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import net.geomovil.gestor.database.Survey;

import java.io.File;

public class FolderInspector {
    /**
     * Verifica si la version del SO es menor a la 19
     * en caso de serlo, manda a crear las carpetas privadas del projecto
     * @param context
     */
    public static void reviewFolderStatus(Activity context){
        if(Build.VERSION.SDK_INT >= 19)
            createPrivateFolders(context);
        else
            createProjectFolders(context);
    }

    private static void createProjectFolders(Activity context) {
        File sdcard = Environment.getExternalStorageDirectory();
        File project_folder = new File(sdcard,
                "Android/data/"+
                        context.getApplicationContext().getPackageName()+"/files");
        if(!project_folder.exists())
            project_folder.mkdirs();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static void createPrivateFolders(Activity context) {
        final File[] dirs = context.getExternalFilesDirs(null);
//        if(dirs != null) {
//            for (File f : dirs) {
//                if (!f.exists()) {
//                    f.mkdirs();
//                }
//            }
//        }
    }

    /**
     * Retorna la carpeta del SD en la que se esta almacenando
     * la informacion del aplicativo
     * @param context
     * @return
     */
    public static File getProjectFolder(Context context){
        if(Build.VERSION.SDK_INT >= 19) {
            final File[] dirs = context.getExternalFilesDirs(null);
            return dirs[0];
        }
        else{
            File sdcard = Environment.getExternalStorageDirectory();
            File project_folder = new File(sdcard,
                    "Android/data/"+
                            context.getApplicationContext().getPackageName()+"/files");
            if(!project_folder.exists())
                project_folder.mkdirs();
            return  project_folder;
        }
    }

    /**
     * Retorna el directorio en el que se van a almacenar las fotos de la encuesta
     * pasada por parametro
     * @param context Contexto de ejecucion
     * @param survey Encuesta
     * @return Directorio de fotos
     */
    public static File getPictureFolder(Activity context, Survey survey){
        File pictureFolder = new File(getProjectFolder(context),survey.getEtiqueta());
        if(!pictureFolder.exists())
            pictureFolder.mkdirs();
        return pictureFolder;
    }
}
