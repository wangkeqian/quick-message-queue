package com.quick.mq.store;

import com.quick.mq.common.utils.MixAll;
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
      if (file.getName().endsWith(".DS_Store")){
        continue;
      }
      if (file.length() != this.mappedFileSize){
        log.error("加载CommitLog文件大小异常 ，请检查");
        return false;
      }

      try {
        MappedFile mappedFile = new MappedFile(file.getPath(), this.mappedFileSize);
        mappedFile.setCommittedPosition(0);
        mappedFile.setFlushedPosition(0);
        mappedFile.setWrotePosition(0);
        this.mappedFiles.add(mappedFile);
        log.info("加载 mappedFile 路径{} 成功", file.getPath());
      } catch (IOException e) {
        log.info("加载 mappedFile 路径{} 异常", file.getPath() ,e);

        return false;
      }

    }

    return true;
  }

  public MappedFile getLastMappedFile() {
    MappedFile mappedFileLast = null;

    while (!this.mappedFiles.isEmpty()){
      try{
        mappedFileLast = this.mappedFiles.get(this.mappedFiles.size() - 1);
        break;
      }catch (Exception ex){
        log.error("获取最后一个MappedFile失败");
        break;
      }
    }
    return mappedFileLast;
  }

  /**
   * 每次创建都顺带多创建一个 优化点
   * @param startOffset
   * @return
   */
  public MappedFile getLastMappedFile(final int startOffset) {
    String nextFilePath = this.storePath + File.separator + MixAll.offset2FileName(startOffset);
    String nextNextFilePath = this.storePath + File.separator + MixAll.offset2FileName(startOffset
        + this.mappedFileSize);
    return doCreateMappedFile(nextFilePath, nextNextFilePath);
  }
  public MappedFile getLastMappedFile(String fileName, final int startOffset) {
    String nextFilePath = this.storePath + File.separator + MixAll.offset2FileName(startOffset);
    String nextNextFilePath = this.storePath + File.separator + fileName + File.separator + MixAll.offset2FileName(startOffset
            + this.mappedFileSize);
    return doCreateMappedFile(nextFilePath, nextNextFilePath);
  }

  private MappedFile doCreateMappedFile(String nextFilePath, String nextNextFilePath) {
    MappedFile mappedFile = null;
    try {
      mappedFile = new MappedFile(nextFilePath, mappedFileSize);
    } catch (Exception e) {
      log.error("创建MappedFile 失败" ,e);
    }
    if (mappedFile != null){
      this.mappedFiles.add(mappedFile);
    }

    return mappedFile;
  }
}
