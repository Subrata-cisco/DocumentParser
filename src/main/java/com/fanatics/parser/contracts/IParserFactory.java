package com.fanatics.parser.contracts;

public interface IParserFactory {
  public IParser createFactory(int type);
}
