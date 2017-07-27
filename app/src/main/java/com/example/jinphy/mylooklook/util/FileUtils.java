package com.example.jinphy.mylooklook.util;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by jinphy on 2017/7/26.
 */

public class FileUtils {

    public static final String FILE_SEPARATOR_DOT = ".";
    public static final String PREFIX_IMAGE = "IMG";
    public static final String PREFIX_AUDIO = "AUD";
    public static final String PREFIX_VIDEO = "VID";

    private FileUtils(){}

    /**
     * 生成一个文件，更多信息请参考
     * @see FileUtils#createFile(String, String, boolean)
     *
     * @param absolutePath 文件的绝对路径，包括目录和文件名
     * @param overrideIfExist 标志是否覆盖已存在的文件
     * @return 生成的文件
     * */
    public static File createFile(@NonNull String absolutePath,boolean overrideIfExist){
        if (overrideIfExist) {
            return createFile(absolutePath);
        }else{
            if (TextUtils.isEmpty(absolutePath) && !absolutePath.contains(File.separator)) {
                return null;
            }else{
                int index = absolutePath.lastIndexOf(File.separator);
                String parent = absolutePath.substring(0, index+1);
                String file = absolutePath.substring(index+1);
                return createFile(parent, file, overrideIfExist);
            }
        }
    }


    /**
     * 该函数生成一个文件，更多信息请参见
     * @see FileUtils#createFile(String, String, String, boolean)
     *
     * @param parent 文件所在的目录
     * @param file 文件名，包括前后缀
     * @param overrideIfExist 标志是否覆盖已存在的文件
     * @return 生成的文件
     * */
    public static File createFile(
            @NonNull String parent,
            @NonNull String file,
            boolean overrideIfExist){

        String prefix = getPrefix(file);
        String suffix = getSuffix(file);


        return createFile(parent, prefix, suffix, overrideIfExist);

    }

    /**
     * 该方法生成一个文件，如果成功则返回生成的文件，否则返回null
     * 参数overrideIfExist标志是否覆盖已存在的文件，如果为true
     * 则新文件将覆盖已存在的同名文件，否则按照重名次数迭代出不同名的文件
     *
     * @param parent 文件所在目录
     * @param prefix 文件名前缀
     * @param suffix 文件名后缀
     * @param overrideIfExist 标志是否覆盖已存在的文件
     * @return 生成的文件
     * */
    public static File createFile(
            @NonNull String parent,
            @NonNull String prefix,
            @NonNull String suffix,boolean overrideIfExist){
        try {
            checkNonNull(parent);
            checkNonNull(prefix);
            checkNonNull(suffix);
        }catch (Exception e){
            e.printStackTrace();
            return  null;
        }
        parent = handleFileNameParent(parent);
        prefix = handleFileNamePrefix(prefix);
        suffix = handleFileNameSuffix(suffix);

        String path = parent+prefix+suffix;

        if (!overrideIfExist){
            // 不覆盖已经存在的文件，所以按次序迭代
            path = generateUnRepeatFileName( parent, prefix, suffix);
        }
        return createFile(path);
    }

    /**
     * 生成一个独一无二的文件名，在文件名中，前缀后面加上创建文件是的时间戳，
     * 例如,/storage/emulate/0/temp/IMG_15654564161312.jpg
     *
     * @param parent 图片文件所在的目录
     * @param prefix 图片文件前椎名，例如 IMG
     * @param suffix 图片文件后缀名，例如 .jpg
     * @return 返回生成的图片文件
     * */
    public static File createImageFile(
            @NonNull String parent,
            String prefix,
            @NonNull String suffix) {

        prefix = wrapPrefix(prefix, PREFIX_IMAGE);

        return createFile(parent,prefix,suffix,true);
    }


    /**
     * 生成一个独一无二的文件名，在文件名中，前缀后面加上创建文件是的时间戳，
     * 例如,/storage/emulate/0/temp/AUD_15654564161312.mp3
     *
     * @param parent 音频文件所在的目录
     * @param prefix 音频文件前椎名，例如 AUD
     * @param suffix 音频文件后缀名，例如 .mp3
     * @return 返回生成的音频文件
     * */
    public static File createAudioFile(
            @NonNull String parent,
            String prefix,
            @NonNull String suffix) {

        prefix = wrapPrefix(prefix, PREFIX_AUDIO);

        return createFile(parent,prefix,suffix,true);
    }


    /**
     * 生成一个独一无二的文件名，在文件名中，前缀后面加上创建文件是的时间戳，
     * 例如,/storage/emulate/0/temp/VID_15654564161312.mp4
     *
     * @param parent 视频文件所在的目录
     * @param prefix 视频文件前椎名，例如 VID
     * @param suffix 视频文件后缀名，例如 .mp4
     * @return 返回生成的视频文件
     * */
    public static File createVideoFile(
            @NonNull String parent,
            String prefix,
            @NonNull String suffix) {

        prefix = wrapPrefix(prefix, PREFIX_VIDEO);

        return createFile(parent,prefix,suffix,true);
    }

    //=============================================================================
    //-----------------------------------------------------------------------------
    // 根据文件的绝对路径生成一个文件，如果成功则返回生成的文件，否则返回null
    private static File createFile(@NonNull String absolutePath){

        if (TextUtils.isEmpty(absolutePath)) {
            return null;
        }

        File file = new File(absolutePath);
        if (file.getParentFile().mkdirs() || file.getParentFile().exists()) {

            // 判断是否能够生成文件所在的目录
            try {
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();

                return file;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        return null;
    }

    //生成不重名的文件名
    private static String generateUnRepeatFileName(String parent, String prefix, String suffix) {

        int repeats = 1;
        File file = new File(parent + prefix + suffix);

        while (file.exists()) {
            file = new File(parent+prefix+"("+(repeats++)+")"+suffix);
        }
        return file.getAbsolutePath();
    }

    // 获取文件名前缀
    private static String getPrefix(String fileName) {
        if (fileName==null){
            return null;
        }
        fileName = fileName.trim();
        int index = fileName.lastIndexOf(FILE_SEPARATOR_DOT);
        if (index > 0 && index < fileName.length() - 1) {
            return fileName.substring(0, index);
        }
        return fileName;
    }


    // 获取文件名后缀
    private static String getSuffix(String fileName) {
        if (fileName == null) {
            return null;
        }
        fileName = fileName.trim();
        int index = fileName.lastIndexOf(FILE_SEPARATOR_DOT);
        if (index > 0 && index < fileName.length() - 1) {
            return fileName.substring(index);
        }
        return "";
    }

    // 测试不为空（包括null和空串）
    private static void checkNonNull(String str)throws Exception {
        if (TextUtils.isEmpty(str)) {
            throw new Exception("the string is null or empty");
        }
    }

    // 处理文件的父目录
    private static String handleFileNameParent(String parent){
        parent = parent.trim();
        return parent.endsWith(File.separator)?parent:parent+File.separator;
    }

    // 处理文件名前缀
    private static String handleFileNamePrefix(String prefix) {
        prefix = prefix.trim();
        while (prefix.endsWith(FileUtils.FILE_SEPARATOR_DOT)) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return prefix;
    }

    // 处理文件名后缀
    private static String handleFileNameSuffix(String suffix) {
        suffix = suffix.trim();
        int index = suffix.lastIndexOf(FILE_SEPARATOR_DOT);
        if (index == suffix.length() - 1) {
            suffix = FILE_SEPARATOR_DOT + suffix.substring(0, suffix.length() - 1);
        } else if (index < 0) {
            suffix = FILE_SEPARATOR_DOT + suffix;
        } else {
            suffix = suffix.substring(index,suffix.length());
        }
        return suffix;

    }

    private static String wrapPrefix(String prefix, String type){
        if (TextUtils.isEmpty(prefix)) {
            prefix = type+"_";
        }else if (!prefix.endsWith("_")){
            prefix +="_";
        }
        prefix +=System.nanoTime();

        return prefix;
    }

}
