package com.quick.mq.store;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author wangkq
 * @date 2023/8/20
 */
@Slf4j
public class MappedFileQueue {

  private final String storePath;

  protected final CopyOnWriteArrayList<MappedFile> mappedFiles = new CopyOnWriteArrayList<MappedFile>();

  private final int mappedFileSize;

  public MappedFileQueue(String storePath, int mappedFileSize) {
    this.storePath = storePath;
    this.mappedFileSize = mappedFileSize;

  }

  public boolean load() {
    File dir = new File(this.storePath);
    File[] ls = dir.listFiles();
    if (ls != null) {
      return doLoad(Arrays.asList(ls));
    }
    return true;
  }

  private boolean doLoad(List<File> files) {

    files.sort(Comparator.comparing(File::getName));


    for (File file : files) {
      if (file.length() != this.mappedFileSize){
        log.error("加载CommitLog文件大小异常 ，请检查");
        return false;
      }

      try {
        MappedFile mappedFile = new MappedFile(file.getPath(), this.mappedFileSize);
        mappedFile.setCommittedPosition(mappedFileSize);
        mappedFile.setFlushedPosition(mappedFileSize);
        mappedFile.setWrotePosition(mappedFileSize);
        this.mappedFiles.add(mappedFile);
        log.info("加载 mappedFile 路径{} 成功", file.getPath());
      } catch (IOException e) {
        log.info("加载 mappedFile 路径{} 异常", file.getPath() ,e);

        return false;
      }

    }

    return true;
  }
}
