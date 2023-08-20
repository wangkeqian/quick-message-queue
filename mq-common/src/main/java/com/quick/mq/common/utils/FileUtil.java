package com.quick.mq.common.utils;

import java.io.File;
import lombok.extern.slf4j.Slf4j;

/**
 * TODO 请说明此类的作用
 *
 * @author wangkq
 * @date 2023/8/20
 */
@Slf4j
public class FileUtil {

  public static void createDirOK(final String dirName) {
    if (dirName != null) {
      File f = new File(dirName);
      if (!f.exists()) {
        boolean result = f.mkdirs();
        log.info(dirName + " mkdir " + (result ? "OK" : "Failed"));
      }
    }
  }

}
