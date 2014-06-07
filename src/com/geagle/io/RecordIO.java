package com.geagle.io;

import java.io.File;
import java.io.IOException;

import com.geagle.mode.Record;

public class RecordIO {

  public static Record read(File file) throws IOException {
    if (file.getName().endsWith(".pgn")) {
      return new PgnReader().read(file);
    }
    return null;
  }
}
