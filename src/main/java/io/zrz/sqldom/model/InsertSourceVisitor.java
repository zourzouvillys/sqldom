package io.zrz.sqldom.model;

public interface InsertSourceVisitor<T> {

  T visitSelectStatementInsertSource(SelectStatement stmt);

  T visitValueListInsertSource(ValueListSource values);

}
