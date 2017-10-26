package io.zrz.sqldom.model;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

import io.zrz.hai.sql.model.ImmutableWithClause;

@Value.Immutable
@Value.Style(visibility = ImplementationVisibility.PUBLIC, depluralize = true)
@Value.Modifiable
public interface WithClause {

  String queryName();

  SqlStatement statement();

  public static WithClause of(String name, SqlStatement stmt) {
    return ImmutableWithClause.builder().queryName(name).statement(stmt).build();
  }

}
