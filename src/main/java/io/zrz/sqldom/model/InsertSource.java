package io.zrz.sqldom.model;

public interface InsertSource {

  <R> R apply(InsertSourceVisitor<R> visitor);

}
