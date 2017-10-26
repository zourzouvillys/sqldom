package io.zrz.sqldom.model;

public interface WindowRef {

  interface Visitor<R> {

    R visitWindowName(WindowName expr);

    R visitWindowDefinition(WindowDefinition expr);

  }

  <R> R apply(Visitor<R> visitor);

}
