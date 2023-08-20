package com.quick.mq.store.utils;

import java.io.File;

/**
 * TODO 请说明此类的作用
 *
 * @author wangkq
 * @date 2023/8/20
 */
public class StorePathUtils {


  public static String getAbnormalFilePath(final String storeRootPathDir) {
    return storeRootPathDir + File.separator + "abort";
  }
}
